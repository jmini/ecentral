package fr.jmini.utils.ecentral;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ECentralTask {

    private static final String GROUP_ID = "fr.jmini.ecentral";
    private static final String ARTIFACT_ID = "eclipse-platform-dependencies";

    private static final TypeToken<List<MavenArtifact>> TYPE_TOKEN = new TypeToken<List<MavenArtifact>>() {
    };
    private Input input;

    public ECentralTask(Input input) {
        if (input == null) {
            throw new IllegalArgumentException("input can't be null");
        }
        this.input = input;
    }

    public void run() {
        try {
            Files.createDirectories(getMavenBomFile().getParent());
            Files.createDirectories(getDataFolder());
            runInternal();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not run", e);
        }
    }

    private void runInternal() throws IOException {
        boolean ignoreExistingData = Objects.equals(System.getProperty("ignoreExistingData"), "true");
        if (ignoreExistingData || !Files.exists(getBndOutputFile())) {
            runBnd();
        }

        if (ignoreExistingData || !Files.exists(getPotentialMavenArtifactsFile())) {
            createPotentialMavenArtifacts();
        }

        if (ignoreExistingData || !Files.exists(getMavenArtifactsFile())) {
            createMavenArtifacts();
        }

        createMavenBomFile();
    }

    private String toVersionKey(MavenArtifact a) {
        return a.getArtifactId()
                .replace('.', '-') + ".version";
    }

    private void runBnd() throws IOException {
        Path bndFile = getDataFolder().resolve("repolist.bndrun");
        StringBuilder sb = new StringBuilder();
        sb.append("-standalone true\n");
        sb.append("-plugin.p2 \\\n");
        sb.append("      aQute.bnd.repository.p2.provider.P2Repository; \\\n");
        sb.append("              url=\"" + input.getUpdateSite() + "\"\n");
        Files.writeString(bndFile, sb.toString(), StandardCharsets.UTF_8);

        String bndFileLocation = bndFile.toAbsolutePath()
                .toString();

        PrintStream old = System.out;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PrintStream ps = new PrintStream(baos);
            System.setOut(ps);

            try (aQute.bnd.main.bnd bnd = new aQute.bnd.main.bnd()) {
                bnd.start(new String[] { "repo", "--workspace", bndFileLocation, "list" });
            } catch (Exception e) {
                throw new RuntimeException("Error while running bnd", e);
            }

            System.out.flush();
            String bndOutput = baos.toString();
            Files.writeString(getBndOutputFile(), bndOutput, StandardCharsets.UTF_8);
        } finally {
            System.setOut(old);
        }
    }

    private void createPotentialMavenArtifacts() throws IOException {
        List<String> bndFileContent = Files.readAllLines(getBndOutputFile());
        List<BndEntry> entries = parseBndOutput(bndFileContent);

        List<MavenArtifact> result = entries.stream()
                .filter(e -> keepEntry(e))
                .map(ECentralTask::toMavenArtifact)
                .collect(Collectors.toList());

        writeArtifactsToFile(getPotentialMavenArtifactsFile(), result);
    }

    /**
     * decide if the entry should be present in the 'potential-maven-artifacts' set
     *
     * @param e
     *            then entry coming from the 'bnd-output' file
     * @return true if the entry should be present
     */
    private boolean keepEntry(BndEntry e) {
        String symbolicName = e.getSymbolicName();
        if (!symbolicName.startsWith("org.eclipse")) {
            return false;
        }
        if (symbolicName.startsWith("org.eclipse.jetty")
                || symbolicName.startsWith("org.eclipse.ecf")
                || symbolicName.startsWith("org.eclipse.emf")
                || symbolicName.endsWith(".source")
                || symbolicName.endsWith(".tests")) {
            return false;
        }
        return true;
    }

    static List<BndEntry> parseBndOutput(List<String> lines) {
        return lines.stream()
                .map(ECentralTask::parseBndLine)
                .collect(Collectors.toList());
    }

    static BndEntry parseBndLine(String line) {
        int start = line.indexOf('[');
        int end = line.indexOf("]", start);

        String symbolicName = line.substring(0, start)
                .trim();

        String versionRange = line.substring(start + 1, end);
        String[] parts = versionRange.split(",");
        List<String> versions = Arrays.stream(parts)
                .map(String::trim)
                .collect(Collectors.toList());

        return new BndEntry(symbolicName, versions);
    }

    static MavenArtifact toMavenArtifact(BndEntry entry) {
        // check the rules in https://git.eclipse.org/c/platform/eclipse.platform.releng.git/tree/publish-to-maven-central/SDK4Mvn.aggr#n34

        String osgiVersion = entry.getVersions()
                .get(entry.getVersions()
                        .size() - 1);
        String mavenVersion = convertVersion(osgiVersion);

        String symbolicName = entry.getSymbolicName();
        if ("org.eclipse.jdt.core.compiler.batch".equals(symbolicName)) {
            return new MavenArtifact("org.eclipse.jdt", "ecj", mavenVersion);
        }
        String groupId;
        if (symbolicName.startsWith("org.eclipse.jdt")) {
            groupId = "org.eclipse.jdt";
        } else if (symbolicName.startsWith("org.eclipse.pde")) {
            groupId = "org.eclipse.pde";
        } else {
            groupId = "org.eclipse.platform";
        }

        return new MavenArtifact(groupId, symbolicName, mavenVersion);
    }

    static String convertVersion(String osgiVersion) {
        //See https://wiki.eclipse.org/CBI/aggregator#Creating_a_Maven-conformant_p2_repo
        String[] parts = osgiVersion.split("\\.");
        if (parts.length > 2) {
            return parts[0] + "." + parts[1] + "." + parts[2];
        }
        return osgiVersion;
    }

    private void createMavenArtifacts() throws IOException {
        List<MavenArtifact> entries = parseArtifactsFile(getPotentialMavenArtifactsFile());

        List<MavenArtifact> result = entries.stream()
                .filter(e -> checkArtifactInMavenCentral(e))
                .collect(Collectors.toList());

        writeArtifactsToFile(getMavenArtifactsFile(), result);
    }

    private static boolean checkArtifactInMavenCentral(MavenArtifact artifact) {
        String centralUrl = computeMavenCentralUrl(artifact);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(centralUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            return connection.getResponseCode() != 404;
        } catch (IOException e) {
            throw new RuntimeException("Could not check if artifac exists in maven central", e);
        }
    }

    static String computeMavenCentralUrl(MavenArtifact artifact) {
        // See https://github.com/eclipse/aether-core/blob/aether-0.9.1.v20140329/aether-util/src/main/java/org/eclipse/aether/util/repository/layout/MavenDefaultLayout.java#L42

        StringBuilder sb = new StringBuilder();
        sb.append("https://repo1.maven.org/maven2/");
        sb.append(subPathInMavenRepo(artifact, ".jar"));
        return sb.toString();
    }

    private static String subPathInMavenRepo(MavenArtifact artifact, String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(artifact.getGroupId()
                .replace('.', '/'));
        sb.append('/');
        sb.append(artifact.getArtifactId());
        sb.append('/');
        sb.append(artifact.getVersion());
        sb.append('/');
        sb.append(artifact.getArtifactId());
        sb.append('-');
        sb.append(artifact.getVersion());
        sb.append(extension);
        return sb.toString();
    }

    private void createMavenBomFile() throws IOException {
        List<MavenArtifact> entries = parseArtifactsFile(getMavenArtifactsFile());
        String content = createMavenBomContent(entries);
        Files.writeString(getMavenBomFile(), content, StandardCharsets.UTF_8);

        writeHash(content, Algorithm.MD_5);
        writeHash(content, Algorithm.SHA_1);
        writeHash(content, Algorithm.SHA_256);
        writeHash(content, Algorithm.SHA_512);
    }

    private void writeHash(String content, Algorithm algorithm) throws IOException {
        String hash = calculateHash(content, algorithm);
        Files.writeString(getMavenBomFile(".pom" + algorithm.getExtension()), hash, StandardCharsets.UTF_8);
    }

    private String createMavenBomContent(List<MavenArtifact> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        sb.append("  <modelVersion>4.0.0</modelVersion>\n");
        sb.append("  <groupId>" + GROUP_ID + "</groupId>\n");
        sb.append("  <artifactId>" + ARTIFACT_ID + "</artifactId>\n");
        sb.append("  <version>" + input.getReleaseVersion() + "</version>\n");
        sb.append("  <packaging>pom</packaging>\n");
        sb.append("  <name>eclipse-platform-dependencies</name>\n");
        sb.append("  <description>Eclipse Platform Dependencies of release " + input.getReleaseName() + "</description>\n");
        sb.append("  <url>https://jmini.github.io/ecentral</url>\n");
        sb.append("  <licenses>\n");
        sb.append("    <license>\n");
        sb.append("      <name>Eclipse Public License - v 2.0</name>\n");
        sb.append("      <url>https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html</url>\n");
        sb.append("    </license>\n");
        sb.append("  </licenses>\n");
        sb.append("  <developers>\n");
        sb.append("    <developer>\n");
        sb.append("      <id>jmini</id>\n");
        sb.append("      <name>Jeremie Bresson</name>\n");
        sb.append("      <email>dev@jmini.fr</email>\n");
        sb.append("    </developer>\n");
        sb.append("  </developers>\n");
        sb.append("  <scm>\n");
        sb.append("    <url>https://github.com/jmini/ecentral</url>\n");
        sb.append("  </scm>\n");
        sb.append("  <properties>\n");
        for (MavenArtifact a : entries) {
            sb.append("    <" + toVersionKey(a) + ">" + a.getVersion() + "</" + toVersionKey(a) + ">\n");
        }
        sb.append("  </properties>\n");
        sb.append("  <dependencyManagement>\n");
        sb.append("    <dependencies>\n");
        for (MavenArtifact a : entries) {
            sb.append("      <dependency>\n");
            sb.append("        <groupId>" + a.getGroupId() + "</groupId>\n");
            sb.append("        <artifactId>" + a.getArtifactId() + "</artifactId>\n");
            sb.append("        <version>${" + toVersionKey(a) + "}</version>\n");
            sb.append("      </dependency>\n");
        }
        sb.append("    </dependencies>\n");
        sb.append("  </dependencyManagement>\n");
        sb.append("</project>\n");
        String s = sb.toString();
        return s;
    }

    static String calculateHash(String content, Algorithm algorithm) {
        MessageDigest digest = algorithm.getMessageDigest();
        byte[] encodedhash = digest.digest(
                content.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void writeArtifactsToFile(Path file, List<MavenArtifact> artifacts) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Files.writeString(file, gson.toJson(artifacts), StandardCharsets.UTF_8);
    }

    private List<MavenArtifact> parseArtifactsFile(Path file) throws IOException {
        Gson gson = new Gson();
        String content = Files.readString(file, StandardCharsets.UTF_8);
        List<MavenArtifact> artifacts = gson.fromJson(content, TYPE_TOKEN.getType());
        return artifacts;
    }

    private Path getDataFolder() {
        return Paths.get("data")
                .resolve(input.getReleaseVersion());
    }

    private Path getBndOutputFile() {
        return getDataFolder().resolve("bnd-output.txt");
    }

    private Path getPotentialMavenArtifactsFile() {
        return getDataFolder().resolve("potential-maven-artifacts.json");
    }

    private Path getMavenArtifactsFile() {
        return getDataFolder().resolve("maven-artifacts.json");
    }

    private Path getMavenBomFile() {
        return getMavenBomFile(".pom");
    }

    private Path getMavenBomFile(String extension) {
        MavenArtifact artifact = new MavenArtifact(GROUP_ID, ARTIFACT_ID, input.getReleaseVersion());
        return Paths.get("repo")
                .resolve(subPathInMavenRepo(artifact, extension));
    }
}
