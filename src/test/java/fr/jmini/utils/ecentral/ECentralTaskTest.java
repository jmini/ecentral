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
}
