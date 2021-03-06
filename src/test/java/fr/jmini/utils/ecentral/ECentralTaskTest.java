package fr.jmini.utils.ecentral;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
    void testToMavenArtifact() throws Exception {
        List<MavenMapping> mavenMappings = List.of(
                new MavenMapping("(org\\.eclipse\\.jdt)\\.core\\.compiler\\.batch", "$1", "ecj"),
                new MavenMapping("(org\\.eclipse\\.jdt)(.*)", "$1", "$1$2"),
                new MavenMapping("(org\\.eclipse)(.*)$", "$1.platform", "$1$2"));

        MavenArtifact jdtCore = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.jdt.core", "3.20.0.v20191203-2131"), mavenMappings)
                .orElseThrow();
        assertThat(jdtCore.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.jdt");
        assertThat(jdtCore.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.eclipse.jdt.core");
        assertThat(jdtCore.getVersion())
                .as("version")
                .isEqualTo("3.20.0");

        MavenArtifact ecj = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.jdt.core.compiler.batch", "3.20.0.v20191203-2131"), mavenMappings)
                .orElseThrow();
        assertThat(ecj.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.jdt");
        assertThat(ecj.getArtifactId())
                .as("artifactId")
                .isEqualTo("ecj");
        assertThat(ecj.getVersion())
                .as("version")
                .isEqualTo("3.20.0");

        MavenArtifact filebuffers = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.core.filebuffers", "3.6.800.v20191122-2108"), mavenMappings)
                .orElseThrow();
        assertThat(filebuffers.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.platform");
        assertThat(filebuffers.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.eclipse.core.filebuffers");
        assertThat(filebuffers.getVersion())
                .as("version")
                .isEqualTo("3.6.800");
    }

    @Test
    void testToMavenArtifactIcu() throws Exception {
        List<MavenMapping> icuMavenMappings = List.of(new MavenMapping("com\\.ibm\\.icu", "com.ibm.icu", "icu4j", "([^.]+)\\.([^.]+)\\.0(?:\\..*)?", "$1.$2"));
        MavenArtifact icu = ECentralTask.toMavenArtifact(new BndEntry("com.ibm.icu", "64.2.0.v20190507-1337"), icuMavenMappings)
                .orElseThrow();
        assertThat(icu.getGroupId())
                .as("groupId")
                .isEqualTo("com.ibm.icu");
        assertThat(icu.getArtifactId())
                .as("artifactId")
                .isEqualTo("icu4j");
        assertThat(icu.getVersion())
                .as("version")
                .isEqualTo("64.2");
    }

    @Test
    void testToMavenArtifactPde() throws Exception {
        List<MavenMapping> pdeMavenMappings = List.of(new MavenMapping("(org\\.eclipse\\.pde)(.*)", "$1", "$1$2"));

        MavenArtifact pde = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.pde", "3.13.800.v20191210-0610"), pdeMavenMappings)
                .orElseThrow();
        assertThat(pde.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.pde");
        assertThat(pde.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.eclipse.pde");
        assertThat(pde.getVersion())
                .as("version")
                .isEqualTo("3.13.800");

        MavenArtifact pdeCore = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.pde.core", "3.13.200.v20191202-2135"), pdeMavenMappings)
                .orElseThrow();
        assertThat(pdeCore.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.pde");
        assertThat(pdeCore.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.eclipse.pde.core");
        assertThat(pdeCore.getVersion())
                .as("version")
                .isEqualTo("3.13.200");
    }

    @Test
    void testToMavenArtifactOSGi() throws Exception {
        List<MavenMapping> mappings = ECentralTask.readMavenMappings();

        MavenArtifact utilfunction = ECentralTask.toMavenArtifact(new BndEntry("org.osgi.util.function", "1.2.0.202109301733"), mappings)
                .orElseThrow();
        assertThat(utilfunction.getGroupId())
                .as("groupId")
                .isEqualTo("org.osgi");
        assertThat(utilfunction.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.osgi.util.function");
        assertThat(utilfunction.getVersion())
                .as("version")
                .isEqualTo("1.2.0");

        MavenArtifact servicePrefs = ECentralTask.toMavenArtifact(new BndEntry("org.osgi.service.prefs", "1.1.2.202109301733"), mappings)
                .orElseThrow();
        assertThat(servicePrefs.getGroupId())
                .as("groupId")
                .isEqualTo("org.osgi");
        assertThat(servicePrefs.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.osgi.service.prefs");
        assertThat(servicePrefs.getVersion())
                .as("version")
                .isEqualTo("1.1.2");
    }

    @Test
    void testToMavenArtifact_4_23_fixes() throws Exception {
        List<MavenMapping> mappings = ECentralTask.readMavenMappings();

        // See https://github.com/eclipse-equinox/equinox.bundles/issues/54
        MavenArtifact equinoxPreferences = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.equinox.preferences", "3.10.0.v20220503-1634"), mappings)
                .orElseThrow();
        assertThat(equinoxPreferences.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.platform");
        assertThat(equinoxPreferences.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.eclipse.equinox.preferences");
        assertThat(equinoxPreferences.getVersion())
                .as("version")
                .isEqualTo("3.10.1"); // 3.10.0 is broken on maven central, a fix was published

        MavenArtifact osgiUtil = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.osgi.util", "3.7.0.v20220427-2144"), mappings)
                .orElseThrow();
        assertThat(osgiUtil.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.platform");
        assertThat(osgiUtil.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.eclipse.osgi.util");
        assertThat(osgiUtil.getVersion())
                .as("version")
                .isEqualTo("3.7.1"); // 3.7.0 is broken on maven central, a fix was published
    }

    @Test
    void testConvertVersion() throws Exception {
        assertThat(ECentralTask.convertVersion("3.12.2.v20161117-1814"))
                .as("version")
                .isEqualTo("3.12.2");

        assertThat(ECentralTask.convertVersion("4.12.0.v201504281640"))
                .isEqualTo("4.12.0");
    }

}
