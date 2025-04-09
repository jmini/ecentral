package fr.jmini.utils.ecentral;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.jmini.utils.mvnutils.MavenArtifact;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

/**
 * @author arnaud-mergey
 *
 */
public class MavenResolver {

    private static final String SUFFIX = "pom.xml";
    private static final String PREFIX = "META-INF/maven/";

    /**
     *
     */
    public MavenResolver() {
        Unirest.config()
                .retryAfter(true);
    }

    public MavenArtifact resolvePotential(BndEntry e, File file) throws IOException {
        try (JarFile jarFile = new JarFile(file)) {
            Optional<JarEntry> o = jarFile.stream()
                    .filter(jarEntry -> jarEntry.getName()
                            .endsWith(SUFFIX) && jarEntry.getName()
                                    .startsWith(PREFIX))
                    .findFirst();
            if (o.isPresent()) {
                String name = o.get()
                        .getName()
                        .substring(PREFIX.length(), o.get()
                                .getName()
                                .length() - (SUFFIX.length() + 1));

                return new MavenArtifact(name.substring(0, name.indexOf('/')), name.substring(name.indexOf('/') + 1), ECentralTask.convertVersion(e.getVersions()
                        .get(e.getVersions()
                                .size() - 1)));
            } else {
                return null;
            }
        }
    }

    public MavenArtifact resolve(Path path) {
        for (int i = 0; i < 10; i++) {
            try {
                return doResolve(path);
            } catch (IllegalStateException e) {
                //maven central can blacklist, so we need some throttling
                try {
                    TimeUnit.SECONDS.sleep(1 + i * i);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    Thread.currentThread()
                            .interrupt();
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        System.err.println("Exception while checking " + path);

        return null;
    }

    private MavenArtifact doResolve(Path path) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[8192];
        try (InputStream stream = Files.newInputStream(path)) {
            int read;
            while ((read = stream.read(buffer)) > -1) {
                if (read > 0) {
                    digest.update(buffer, 0, read);
                }
            }
        }
        String sha1Hash = toHexString(digest.digest());
        GetRequest request = Unirest
                .get("https://search.maven.org/solrsearch/select")
                .queryString("q", "1:" + sha1Hash)
                .queryString("wt", "json");
        HttpResponse<JsonNode> httpresponse = request.asJson();
        if (httpresponse.getStatus() == 403) {
            throw new IllegalStateException();
        }

        JSONObject node = httpresponse
                .getBody()
                .getObject();
        if (node.has("response")) {
            JSONObject response = node.getJSONObject("response");
            if (response.has("numFound") && response.getInt("numFound") > 0) {

                JSONArray array = response.getJSONArray("docs");
                long timestamp = Long.MAX_VALUE;
                String groupId = null;
                JSONObject coordinates = null;
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    long t = o.getLong("timestamp");
                    String g = o.getString("g");
                    if (g.equals(groupId)) {
                        //multiple artifacts in same group, we keep the most recent
                        if (t > timestamp) {
                            coordinates = o;
                        }
                    } else {
                        //multiple artifacts in multiple group, we keep the first
                        if (t < timestamp) {
                            groupId = g;
                            timestamp = t;
                            coordinates = o;
                        }
                    }
                }
                return new MavenArtifact(coordinates.getString("g"), coordinates.getString("a"), coordinates.getString("v"));
            }
        }

        return null;

    }

    private static String toHexString(byte[] bytes) {
        return IntStream.range(0, bytes.length)
                .mapToObj(i -> String.format("%02X", bytes[i]))
                .collect(Collectors.joining());
    }

}