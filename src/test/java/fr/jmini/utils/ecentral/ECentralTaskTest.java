package fr.jmini.utils.ecentral;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

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
    void testConvertVersion() throws Exception {
        assertThat(ECentralTask.convertVersion("3.12.2.v20161117-1814"))
                .as("version")
                .isEqualTo("3.12.2");

        assertThat(ECentralTask.convertVersion("4.12.0.v201504281640"))
                .isEqualTo("4.12.0");
    }

    @Test
    void testComputeMavenCentralUrl() throws Exception {
        MavenArtifact a = new MavenArtifact("org.eclipse.platform", "org.eclipse.ant.core", "3.5.600");
        assertThat(ECentralTask.computeMavenCentralUrl(a))
                .as("url of the jar in maven central")
                .isEqualTo("https://repo1.maven.org/maven2/org/eclipse/platform/org.eclipse.ant.core/3.5.600/org.eclipse.ant.core-3.5.600.jar");

        assertThat(ECentralTask.computeMavenCentralUrl(a, ".pom"))
                .as("url of the pom in maven central")
                .isEqualTo("https://repo1.maven.org/maven2/org/eclipse/platform/org.eclipse.ant.core/3.5.600/org.eclipse.ant.core-3.5.600.pom");

        assertThat(ECentralTask.computeMavenCentralUrl(a, ".jar.asc"))
                .as("url of the armored ASCII file of the jar in maven central")
                .isEqualTo("https://repo1.maven.org/maven2/org/eclipse/platform/org.eclipse.ant.core/3.5.600/org.eclipse.ant.core-3.5.600.jar.asc");

    }

    @Test
    void testCalculateHash() throws Exception {
        String md5 = ECentralTask.calculateHash("test", Algorithm.MD_5);
        assertThat(md5).isEqualTo("098f6bcd4621d373cade4e832627b4f6");

        String sha1 = ECentralTask.calculateHash("test", Algorithm.SHA_1);
        assertThat(sha1).isEqualTo("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");

        String sha256 = ECentralTask.calculateHash("test", Algorithm.SHA_256);
        assertThat(sha256).isEqualTo("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08");

        String sha512 = ECentralTask.calculateHash("test", Algorithm.SHA_512);
        assertThat(sha512).isEqualTo("ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff");
    }
}
