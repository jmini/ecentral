package fr.jmini.utils.ecentral;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fr.jmini.utils.mvnutils.Algorithm;
import fr.jmini.utils.mvnutils.Maven;
import fr.jmini.utils.mvnutils.MavenArtifact;

public class ECentralTask {

    private static final String GROUP_ID = "fr.jmini.ecentral";

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

        List<MavenMapping> mavenMappings = readMavenMappings();
        List<MavenArtifact> result = entries.stream()
                .filter(this::keepEntry)
                .map(e -> toMavenArtifact(e, mavenMappings))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        writeArtifactsToFile(getPotentialMavenArtifactsFile(), result);
    }

    static List<MavenMapping> readMavenMappings() throws IOException {
        try (InputStream resource = ECentralTask.class.getResourceAsStream("/mavenMappings.txt")) {
            return new BufferedReader(new InputStreamReader(resource,
                    StandardCharsets.UTF_8)).lines()
                            .map(String::trim)
                            .filter(s -> s.startsWith("<mavenMappings"))
                            .map(ECentralTask::parseMavenMapping)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
        }
    }

    static Optional<MavenMapping> parseMavenMapping(String line) {
        String namePattern = findAttribute(line, "namePattern").orElseThrow(() -> new IllegalStateException("'namePattern' has to be present"));
        String groupId = findAttribute(line, "groupId").orElseThrow(() -> new IllegalStateException("'groupId' has to be present"));
        String artifactId = findAttribute(line, "artifactId").orElseThrow(() -> new IllegalStateException("'artifactId' has to be present"));
        Optional<String> findVersionPattern = findAttribute(line, "versionPattern");
        if (findVersionPattern.isEmpty()) {
            return Optional.of(new MavenMapping(namePattern, groupId, artifactId));
        }
        String versionTemplate = findAttribute(line, "versionTemplate").orElseThrow(() -> new IllegalStateException("'versionTemplate' has to be present"));
        return Optional.of(new MavenMapping(namePattern, groupId, artifactId, findVersionPattern.get(), versionTemplate));
    }

    private static Optional<String> findAttribute(String line, String attributeName) {
        String find = attributeName + "=\"";
        int start = line.indexOf(find);
        if (start < 0) {
            return Optional.empty();
        }
        start = start + find.length();
        int end = line.indexOf("\"", start);
        if (end < 0) {
            return Optional.empty();
        }
        return Optional.of(line.substring(start, end));
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
        return !(symbolicName.endsWith(".source")
                || symbolicName.endsWith(".tests"));
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

    static Optional<MavenArtifact> toMavenArtifact(BndEntry entry, List<MavenMapping> mavenMappings) {
        String symbolicName = entry.getSymbolicName();
        Optional<MavenMapping> findMappting = findMapping(mavenMappings, symbolicName);

        String osgiVersion = entry.getVersions()
                .get(entry.getVersions()
                        .size() - 1);
        if (findMappting.isPresent()) {
            MavenMapping mapping = findMappting.get();
            Matcher nameMatcher = createMatcher(symbolicName, mapping.getNamePattern());
            if (nameMatcher.find()) {
                String groupId = applyMatcher(nameMatcher, mapping.getGroupId());
                String artifactId = applyMatcher(nameMatcher, mapping.getArtifactId());
                String mavenVersion = computeMavenVersion(osgiVersion, mapping, groupId, artifactId);
                return Optional.of(new MavenArtifact(groupId, artifactId, mavenVersion));
            } else {
                throw new IllegalStateException("Unexpected state: the matcher is not matching '" + symbolicName + "'");
            }
        }
        return Optional.empty();
    }

    private static String computeMavenVersion(String osgiVersion, MavenMapping mapping, String groupId, String artifactId) {
        if ("org.eclipse.platform".equals(groupId) && "org.eclipse.equinox.preferences".equals(artifactId) && "3.10.0.v20220503-1634".equals(osgiVersion)) {
            // See https://github.com/eclipse-equinox/equinox.bundles/issues/54
            return "3.10.1";
        } else if ("org.eclipse.platform".equals(groupId) && "org.eclipse.osgi.util".equals(artifactId) && "3.7.0.v20220427-2144".equals(osgiVersion)) {
            // See https://github.com/eclipse-equinox/equinox.framework/issues/70
            return "3.7.1";
        }
        String mavenVersion;
        if (mapping.getVersionPattern() != null) {
            Matcher versionMatcher = createMatcher(osgiVersion, mapping.getVersionPattern());
            mavenVersion = applyMatcher(versionMatcher, mapping.getVersionTemplate());
        } else {
            mavenVersion = convertVersion(osgiVersion);
        }
        return mavenVersion;
    }

    private static Matcher createMatcher(String symbolicName, String pattern) {
        Pattern namePattern = Pattern.compile(pattern);
        return namePattern.matcher(symbolicName);
    }

    private static String applyMatcher(Matcher matcher, String template) {
        return matcher.replaceFirst(template);
    }

    private static Optional<MavenMapping> findMapping(List<MavenMapping> mavenMappings, String symbolicName) {
        for (MavenMapping m : mavenMappings) {
            if (symbolicName.matches(m.getNamePattern())) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
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
                .filter(ECentralTask::checkArtifactInMavenCentral)
                .collect(Collectors.toList());

        writeArtifactsToFile(getMavenArtifactsFile(), result);
    }

    private static boolean checkArtifactInMavenCentral(MavenArtifact artifact) {
        String centralUrl = Maven.jarMavenCentralUrl(artifact);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(centralUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            return connection.getResponseCode() != 404;
        } catch (IOException e) {
            throw new RuntimeException("Could not check if artifact exists in maven central", e);
        }
    }

    private void createMavenBomFile() throws IOException {
        List<MavenArtifact> entries = parseArtifactsFile(getMavenArtifactsFile());
        String content = createMavenBomContent(entries);
        Maven.writeFileToRepositoryWithArmoredFiles(Paths.get("repo"), getBomArtifact(), ".pom", content, Algorithm.MD_5, Algorithm.SHA_1, Algorithm.SHA_256, Algorithm.SHA_512);
    }

    private String createMavenBomContent(List<MavenArtifact> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        sb.append("  <modelVersion>4.0.0</modelVersion>\n");
        sb.append("  <groupId>" + GROUP_ID + "</groupId>\n");
        sb.append("  <artifactId>" + input.getArtifactId() + "</artifactId>\n");
        sb.append("  <version>" + input.getReleaseVersion() + "</version>\n");
        sb.append("  <packaging>pom</packaging>\n");
        sb.append("  <name>eclipse-dependencies</name>\n");
        sb.append("  <description>Eclipse Dependencies of release " + input.getReleaseName() + "</description>\n");
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
        return sb.toString();
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
        return gson.fromJson(content, TYPE_TOKEN.getType());
    }

    private Path getDataFolder() {
        return Paths.get("data")
                .resolve(input.getReleaseVersion());
    }

    private Path getBndOutputFile() {
        return getDataFolder().resolve("bnd-output.txt");
    }

    Path getPotentialMavenArtifactsFile() {
        return getDataFolder().resolve("potential-maven-artifacts.json");
    }

    Path getMavenArtifactsFile() {
        return getDataFolder().resolve("maven-artifacts.json");
    }

    private MavenArtifact getBomArtifact() {
        return new MavenArtifact(GROUP_ID, input.getArtifactId(), input.getReleaseVersion());
    }
}
