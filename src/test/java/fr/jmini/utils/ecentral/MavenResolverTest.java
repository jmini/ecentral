/**
 *
 */
package fr.jmini.utils.ecentral;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import fr.jmini.utils.mvnutils.Maven;
import fr.jmini.utils.mvnutils.MavenArtifact;
import kong.unirest.Unirest;

class MavenResolverTest {

    @Test
    void testResolveJakartaAnnotation() throws IOException {
        MavenArtifact jakartaAnnotationArtifact = new MavenArtifact("jakarta.annotation", "jakarta.annotation-api", "2.1.1");

        String urlInMavenCentral = Maven.jarMavenCentralUrl(jakartaAnnotationArtifact);

        File result = Unirest.get(urlInMavenCentral)
                .asFile(Files.createTempFile("testMaven", ".jar")
                        .toAbsolutePath()
                        .toString(), StandardCopyOption.REPLACE_EXISTING)
                .getBody();

        MavenResolver resolver = new MavenResolver();
        assertThat(resolver.resolve(result.toPath())).isEqualTo(jakartaAnnotationArtifact);
    }

    @Test
    void testResolvePotential() throws IOException {
        MavenArtifact osgiXmlArtifact = new MavenArtifact("org.osgi", "org.osgi.util.xml", "1.0.2");

        String urlInMavenCentral = Maven.jarMavenCentralUrl(osgiXmlArtifact);

        File result = Unirest.get(urlInMavenCentral)
                .asFile(Files.createTempFile("testMaven", ".jar")
                        .toAbsolutePath()
                        .toString(), StandardCopyOption.REPLACE_EXISTING)
                .getBody();

        MavenResolver resolver = new MavenResolver();
        assertThat(resolver.resolvePotential(new BndEntry("org.osgi.util.xml", Arrays.asList("1.0.1", "1.0.2.202109301733")), result))
                .isEqualTo(
                        osgiXmlArtifact);
    }

}
