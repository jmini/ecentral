package fr.jmini.utils.ecentral;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import fr.jmini.utils.mvnutils.MavenArtifact;

class ECentralTaskTest {
    @Test
    void testParseBndEntry() throws Exception {
        BndEntry result = ECentralTask.parseBndLine("com.company.test.foo                     [1.12.300]");

        assertThat(result.getSymbolicName())
                .as("symbolic name")
                .isEqualTo("com.company.test.foo");
        assertThat(result.getVersions())
                .as("versions")
                .containsExactly("1.12.300");
    }

    @Test
    void testAddArtifact() {
        Map<MavenArtifact, MavenArtifact> map = new HashMap<>();

        MavenArtifact a2 = new MavenArtifact("c", "d", "1.0");
        ECentralTask.addArtifact(a2, map);

        assertThat(map).containsValues(a2);

        MavenArtifact a3 = new MavenArtifact("c", "d", "2.5");
        ECentralTask.addArtifact(a3, map);

        assertThat(map).containsValues(a3);

        MavenArtifact a1 = new MavenArtifact("a", "b", "1.0");
        ECentralTask.addArtifact(a1, map);

        assertThat(map).containsValues(a1, a3);

        MavenArtifact a4 = new MavenArtifact("c", "d", "2.0");
        ECentralTask.addArtifact(a4, map);

        assertThat(map).containsValues(a1, a3);

    }

    @Test
    void testParseBndEntryWithMultipleVersions() throws Exception {
        BndEntry result = ECentralTask.parseBndLine("zzz.yyyyyy.xxxxxxx   [1.1.400.v20180921-1416, 2.2.400.v20191120-1313]");

        assertThat(result.getSymbolicName())
                .as("symbolic name")
                .isEqualTo("zzz.yyyyyy.xxxxxxx");
        assertThat(result.getVersions())
                .as("versions")
                .containsExactly("1.1.400.v20180921-1416", "2.2.400.v20191120-1313");
    }

    @Test
    void testParseMavenMappingWithoutVersion() throws Exception {
        Optional<MavenMapping> result = ECentralTask.parseMavenMapping("<mavenMappings namePattern=\"(org\\.eclipse\\.jdt)\\.core\\.compiler\\.batch\" groupId=\"$1\" artifactId=\"ecj\"/>");

        assertThat(result).isPresent();
        MavenMapping mavenMapping = result.get();

        assertThat(mavenMapping.getNamePattern())
                .as("namePattern")
                .isEqualTo("(org\\.eclipse\\.jdt)\\.core\\.compiler\\.batch");
        assertThat(mavenMapping.getGroupId())
                .as("groupId")
                .isEqualTo("$1");
        assertThat(mavenMapping.getArtifactId())
                .as("artifactId")
                .isEqualTo("ecj");
    }

    @Test
    void testParseMavenMappingWithVersion() throws Exception {
        Optional<MavenMapping> result = ECentralTask.parseMavenMapping(
                "  <mavenMappings namePattern=\"org\\.apache\\.(commons)\\.([^.]+)\" groupId=\"$1-$2\" artifactId=\"$1-$2\" versionPattern=\"([^.]+)\\.([^.]+)\\.0(?:\\..*)?\" versionTemplate=\"$1.$2\"/>");

        assertThat(result).isPresent();
        MavenMapping mavenMapping = result.get();

        assertThat(mavenMapping.getNamePattern())
                .as("namePattern")
                .isEqualTo("org\\.apache\\.(commons)\\.([^.]+)");
        assertThat(mavenMapping.getGroupId())
                .as("groupId")
                .isEqualTo("$1-$2");
        assertThat(mavenMapping.getArtifactId())
                .as("artifactId")
                .isEqualTo("$1-$2");
        assertThat(mavenMapping.getVersionPattern())
                .as("versionPattern")
                .isEqualTo("([^.]+)\\.([^.]+)\\.0(?:\\..*)?");
        assertThat(mavenMapping.getVersionTemplate())
                .as("versionTemplate")
                .isEqualTo("$1.$2");
    }

    @Test
    void testXercesToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.xerces", "2.12.2.v20230928-1306"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("xerces");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("xercesImpl");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.12.2");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.xml.resolver", "1.2.0.v20230928-1222"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("xml-resolver");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("xml-resolver");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.2");
        }
    }

    @Test
    void testLuceneToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.lucene.analysis-smartcn", "9.8.0.v20230929-1030"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.lucene");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("lucene-analysis-smartcn");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("9.8.0");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.lucene.core", "9.8.0.v20230929-1030"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.lucene");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("lucene-core");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("9.8.0");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.lucene.queries", "9.8.0.v20230929-1030"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.lucene");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("lucene-queries");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("9.8.0");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.lucene.queryparser", "9.8.0.v20230929-1030"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.lucene");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("lucene-queryparser");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("9.8.0");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.lucene.sandbox", "9.8.0.v20230929-1030"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.lucene");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("lucene-sandbox");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("9.8.0");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.lucene.analysis-common", "9.8.0.v20230929-1030"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.lucene");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("lucene-analysis-common");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("9.8.0");
        }
    }

    @Test
    void testApacheCommonsToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.commons.collections", "3.2.2.v201511171945"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("commons-collections");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("commons-collections");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("3.2.2");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.commons.httpclient", "3.1.0.v201012070820"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("commons-httpclient");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("commons-httpclient");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("3.1");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.commons.jxpath", "1.3.0.v200911051830"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("commons-jxpath");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("commons-jxpath");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.3");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.commons.lang", "2.6.0.v201404270220"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("commons-lang");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("commons-lang");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.6");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.commons.logging", "1.2.0.v20180409-1502"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("commons-logging");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("commons-logging");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.2");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.commons.net", "3.2.0.v201305141515"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("commons-net");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("commons-net");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("3.2");
        }
    }

    @Test
    void testApacheHttpToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.httpcomponents.httpcore", "4.4.16.v20221207-1049"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.httpcomponents");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("httpcore-osgi");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("4.4.16");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.httpcomponents.core5.httpcore5-h2", "5.2.3.v20230922-1600"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.httpcomponents.core5");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("httpcore5-h2");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("5.2.3");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.httpcomponents.core5.httpcore5", "5.2.3.v20230922-1600"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.httpcomponents.core5");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("httpcore5");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("5.2.3");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.httpcomponents.client5.httpclient5-win", "5.2.1.v20230802-0847"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.httpcomponents.client5");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("httpclient5-win");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("5.2.1");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.httpcomponents.client5.httpclient5", "5.2.1.v20230802-0806"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.httpcomponents.client5");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("httpclient5");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("5.2.1");
        }
    }

    @Test
    void testDefaultToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.codelibs.nekohtml", "2.1.2.v20230802-0829"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.codelibs");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("nekohtml");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.1.2");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("lpg.runtime.java", "2.0.17.v201004271640"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("lpg.runtime");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("java");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.0.17-v201004271640");
        }
    }

    @Test
    void testJnaToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("com.sun.jna", "5.13.0.v20230812-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("net.java.dev.jna");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("jna");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("5.13.0");
        }
    }

    @Test
    void testAntToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.ant", "1.10.14.v20230922-1200"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.ant");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("ant");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.10.14");
        }
    }

    @Test
    void testJunitToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.junit", "4.13.2.v20230809-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("junit");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("junit");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("4.13.2");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.hamcrest.core", "2.2.0.v20230809-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.hamcrest");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("hamcrest-core");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.2");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.hamcrest.library", "1.3.0.v20180524-2246"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.hamcrest");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("hamcrest-library");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.3");
        }
    }

    @Test
    void testJdomToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.jdom", "1.1.3.v20230812-1600"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.jdom");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("jdom");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.1.3");
        }
    }

    @Test
    void testGlassfishToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.glassfish.hk2.osgi-resource-locator", "2.5.0.v20161103-1916"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.glassfish.hk2");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("osgi-resource-locator");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.5.0-b42");
        }
    }

    @Test
    void testEcfToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.ecf", "3.11.0.v20230507-1923"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.eclipse.ecf");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("org.eclipse.ecf");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("3.11.0");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.osgi.services.remoteserviceadmin", "1.6.301.v20231021-2050"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.eclipse.ecf");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("org.eclipse.osgi.services.remoteserviceadmin");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.6.301");
        }
    }

    @Test
    void testJavaxToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("jakarta.servlet.jsp", "3.0.0.v20210105-0527"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("jakarta.servlet.jsp");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("jakarta.servlet.jsp-api");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("3.0.0");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("javax.activation", "1.2.2.v20221203-1659"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("com.sun.activation");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("jakarta.activation");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.2.2");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("jakarta.xml.bind", "2.3.3.v20201118-1818"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("jakarta.xml.bind");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("jakarta.xml.bind-api");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.3.3");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("jakarta.el", "4.0.0.v20210105-0527"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("jakarta.el");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("jakarta.el-api");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("4.0.0");
        }
    }

    @Test
    void testGsonToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("com.google.gson", "2.10.1.v20230109-0753"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("com.google.code.gson");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("gson");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.10.1");
        }
    }

    @Test
    void testJschToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("com.jcraft.jsch", "0.1.55.v20230916-1400"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("com.jcraft");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("jsch");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("0.1.55");
        }
    }

    @Test
    void testBatikToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = ECentralTask.readMavenMappings();

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.xmlgraphics", "2.9.0.v20230916-1600"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("xmlgraphics-commons");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("2.9");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.gvt", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-gvt");
            assertThat(mavenArtifact.getVersion())

                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.i18n", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-i18n");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.parser", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-parser");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.script", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-script");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.shared.resources", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-shared-resources");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.svggen", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-svggen");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.transcoder", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-transcoder");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.util", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-util");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.xml", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-xml");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.ext", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-ext");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.dom.svg", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-svg-dom");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }
        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.dom", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-dom");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.css", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-css");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.constants", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-constants");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.codec", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-codec");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.bridge", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-bridge");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.awt.util", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-awt-util");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

        {
            MavenArtifact mavenArtifact = ECentralTask.toMavenArtifact(new BndEntry("org.apache.batik.anim", "1.17.0.v20231009-1000"), mavenMappings)
                    .orElseThrow();
            assertThat(mavenArtifact.getGroupId())
                    .as("groupId")
                    .isEqualTo("org.apache.xmlgraphics");
            assertThat(mavenArtifact.getArtifactId())
                    .as("artifactId")
                    .isEqualTo("batik-anim");
            assertThat(mavenArtifact.getVersion())
                    .as("version")
                    .isEqualTo("1.17");
        }

    }

}
