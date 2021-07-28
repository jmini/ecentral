package fr.jmini.utils.ecentral;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ECentralTaskTest {
    @Test
    public void testParseBndEntry() throws Exception {
        BndEntry result = ECentralTask.parseBndLine("com.company.test.foo                     [1.12.300]");

        assertThat(result.getSymbolicName())
                .as("symbolic name")
                .isEqualTo("com.company.test.foo");
        assertThat(result.getVersions())
                .as("versions")
                .containsExactly("1.12.300");
    }

    @Test
    public void testParseBndEntryWithMultipleVersions() throws Exception {
        BndEntry result = ECentralTask.parseBndLine("zzz.yyyyyy.xxxxxxx   [1.1.400.v20180921-1416, 2.2.400.v20191120-1313]");

        assertThat(result.getSymbolicName())
                .as("symbolic name")
                .isEqualTo("zzz.yyyyyy.xxxxxxx");
        assertThat(result.getVersions())
                .as("versions")
                .containsExactly("1.1.400.v20180921-1416", "2.2.400.v20191120-1313");
    }

    @Test
    public void testToMavenArtifact() throws Exception {
        MavenArtifact jdtCore = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.jdt.core", "3.20.0.v20191203-2131"));
        assertThat(jdtCore.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.jdt");
        assertThat(jdtCore.getArtifactId())
                .as("artifactId")
                .isEqualTo("org.eclipse.jdt.core");
        assertThat(jdtCore.getVersion())
                .as("version")
                .isEqualTo("3.20.0");

        MavenArtifact ecj = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.jdt.core.compiler.batch", "3.20.0.v20191203-2131"));
        assertThat(ecj.getGroupId())
                .as("groupId")
                .isEqualTo("org.eclipse.jdt");
        assertThat(ecj.getArtifactId())
                .as("artifactId")
                .isEqualTo("ecj");
        assertThat(ecj.getVersion())
                .as("version")
                .isEqualTo("3.20.0");

        MavenArtifact filebuffers = ECentralTask.toMavenArtifact(new BndEntry("org.eclipse.core.filebuffers", "3.6.800.v20191122-2108"));
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
    public void testConvertVersion() throws Exception {
        assertThat(ECentralTask.convertVersion("3.12.2.v20161117-1814"))
                .as("version")
                .isEqualTo("3.12.2");

        assertThat(ECentralTask.convertVersion("4.12.0.v201504281640"))
                .isEqualTo("4.12.0");
    }

    @Test
    public void testComputeMavenCentralUrl() throws Exception {
        MavenArtifact a = new MavenArtifact("org.eclipse.platform", "org.eclipse.ant.core", "3.5.600");
        assertThat(ECentralTask.computeMavenCentralUrl(a))
                .as("url in maven centraal")
                .isEqualTo("https://repo1.maven.org/maven2/org/eclipse/platform/org.eclipse.ant.core/3.5.600/org.eclipse.ant.core-3.5.600.jar");

    }

    @Test
    public void testCalculateHash() throws Exception {
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
