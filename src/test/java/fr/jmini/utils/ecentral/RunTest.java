package fr.jmini.utils.ecentral;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * This test class run the code for different update sites.
 */
class RunTest {
    @Test
    void run_2024_06() throws Exception {
        Input input = new Input()
                .withReleaseName("2024-06")
                .withArtifactId("eclipse-full-dependencies");
        ECentralTask task = new ECentralTask(input);
        task.run();
        String mavenArtifacts = Files.readString(task.getMavenArtifactsFile(), StandardCharsets.UTF_8);

        assertThatJson(mavenArtifacts).isArray()
                .size()
                .isEqualTo(task.parseBndOutput()
                        .size());
    }

    @Test
    void run_2024_03() throws Exception {
        Input input = new Input()
                .withReleaseName("2024-03")
                .withArtifactId("eclipse-full-dependencies");
        ECentralTask task = new ECentralTask(input);
        task.run();
        String mavenArtifacts = Files.readString(task.getMavenArtifactsFile(), StandardCharsets.UTF_8);

        assertThatJson(mavenArtifacts).isArray()
                .size()
                .isEqualTo(task.parseBndOutput()
                        .size());
    }

    @Test
    void run_2023_12() throws Exception {
        Input input = new Input()
                .withReleaseName("2023-12")
                .withArtifactId("eclipse-full-dependencies");
        ECentralTask task = new ECentralTask(input);
        task.run();
        String mavenArtifacts = Files.readString(task.getMavenArtifactsFile(), StandardCharsets.UTF_8);

        assertThatJson(mavenArtifacts).isArray()
                .size()
                .isEqualTo(task.parseBndOutput()
                        .size());
    }

    @Test
    void run_2023_09() throws Exception {
        Input input = new Input()
                .withReleaseName("2023-09")
                .withArtifactId("eclipse-full-dependencies");
        ECentralTask task = new ECentralTask(input);
        task.run();
        String mavenArtifacts = Files.readString(task.getMavenArtifactsFile(), StandardCharsets.UTF_8);

        assertThatJson(mavenArtifacts).isArray()
                .size()
                .isEqualTo(task.parseBndOutput()
                        .size());
    }

    @Test
    void run_2023_06() throws Exception {
        Input input = new Input()
                .withReleaseName("2023-06")
                .withArtifactId("eclipse-full-dependencies");
        ECentralTask task = new ECentralTask(input);
        task.run();
        String mavenArtifacts = Files.readString(task.getMavenArtifactsFile(), StandardCharsets.UTF_8);

        assertThatJson(mavenArtifacts).isArray()
                .size()
                .isEqualTo(task.parseBndOutput()
                        .size());
    }

    @Test
    void run_2023_03() throws Exception {
        Input input = new Input()
                .withReleaseName("2023-03")
                .withArtifactId("eclipse-full-dependencies");
        ECentralTask task = new ECentralTask(input);
        task.run();
        String mavenArtifacts = Files.readString(task.getMavenArtifactsFile(), StandardCharsets.UTF_8);

        assertThatJson(mavenArtifacts).isArray()
                .size()
                .isEqualTo(task.parseBndOutput()
                        .size());
    }

    @Test
    void run_2022_09() throws Exception {
        Input input = new Input()
                .withReleaseName("2022-09")
                .withArtifactId("eclipse-full-dependencies");
        ECentralTask task = new ECentralTask(input);
        task.run();
        String mavenArtifacts = Files.readString(task.getMavenArtifactsFile(), StandardCharsets.UTF_8);

        assertThatJson(mavenArtifacts).isArray()
                .size()
                .isEqualTo(task.parseBndOutput()
                        .size());
    }

    @Test
    void run_4_32() throws Exception {
        Input input = new Input()
                .withReleaseName("2024-06")
                .withReleaseVersion("4.32")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.32/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_31() throws Exception {
        Input input = new Input()
                .withReleaseName("2024-03")
                .withReleaseVersion("4.31")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.31/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_30() throws Exception {
        Input input = new Input()
                .withReleaseName("2023-12")
                .withReleaseVersion("4.30")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.30/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_29() throws Exception {
        Input input = new Input()
                .withReleaseName("2023-09")
                .withReleaseVersion("4.29")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.29/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_28() throws Exception {
        Input input = new Input()
                .withReleaseName("2023-06")
                .withReleaseVersion("4.28")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.28/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_27() throws Exception {
        Input input = new Input()
                .withReleaseName("2023-03")
                .withReleaseVersion("4.27")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.27/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_26() throws Exception {
        Input input = new Input()
                .withReleaseName("2022-12")
                .withReleaseVersion("4.26")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.26/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_25() throws Exception {
        Input input = new Input()
                .withReleaseName("2022-09")
                .withReleaseVersion("4.25")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.25/");
        ECentralTask task = new ECentralTask(input);
        task.run();
        String mavenArtifacts = Files.readString(task.getMavenArtifactsFile(), StandardCharsets.UTF_8);

        assertThatJson(mavenArtifacts).isArray()
                .size()
                .isEqualTo(task.parseBndOutput()
                        .size());
    }

    @Test
    void run_4_24() throws Exception {
        Input input = new Input()
                .withReleaseName("2022-06")
                .withReleaseVersion("4.24")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.24/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_23() throws Exception {
        Input input = new Input()
                .withReleaseName("2022-03")
                .withReleaseVersion("4.23")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.23/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_22() throws Exception {
        Input input = new Input()
                .withReleaseName("2021-12")
                .withReleaseVersion("4.22")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.22/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_21() throws Exception {
        Input input = new Input()
                .withReleaseName("2021-09")
                .withReleaseVersion("4.21")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.21/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_20() throws Exception {
        Input input = new Input()
                .withReleaseName("2021-06")
                .withReleaseVersion("4.20")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.20/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_19() throws Exception {
        Input input = new Input()
                .withReleaseName("2021-03")
                .withReleaseVersion("4.19")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.19/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_18() throws Exception {
        Input input = new Input()
                .withReleaseName("2020-12")
                .withReleaseVersion("4.18")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.18/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_17() throws Exception {
        Input input = new Input()
                .withReleaseName("2020-09")
                .withReleaseVersion("4.17")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.17/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_16() throws Exception {
        Input input = new Input()
                .withReleaseName("2020-06")
                .withReleaseVersion("4.16")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.16/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_15() throws Exception {
        Input input = new Input()
                .withReleaseName("2020-03")
                .withReleaseVersion("4.15")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.15/");
        new ECentralTask(input).run();
    }

    // tag::4_14_test[]
    @Test
    void run_4_14() throws Exception {
        Input input = new Input()
                .withReleaseName("2019-12")
                .withReleaseVersion("4.14")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.14/");
        new ECentralTask(input).run();
    }
    // end::4_14_test[]

    @Test
    void run_4_13() throws Exception {
        Input input = new Input()
                .withReleaseName("2019-09")
                .withReleaseVersion("4.13")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.13/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_12() throws Exception {
        Input input = new Input()
                .withReleaseName("2019-06")
                .withReleaseVersion("4.12")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.12/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_11() throws Exception {
        Input input = new Input()
                .withReleaseName("2019-03")
                .withReleaseVersion("4.11")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.11/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_10() throws Exception {
        Input input = new Input()
                .withReleaseName("2018-12")
                .withReleaseVersion("4.10")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.10/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_9() throws Exception {
        Input input = new Input()
                .withReleaseName("2018-09")
                .withReleaseVersion("4.9")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.9/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_8() throws Exception {
        Input input = new Input()
                .withReleaseName("Photon")
                .withReleaseVersion("4.8")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.8/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_7() throws Exception {
        Input input = new Input()
                .withReleaseName("Oxygen")
                .withReleaseVersion("4.7")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.7/");
        new ECentralTask(input).run();
    }

    @Test
    void run_4_6() throws Exception {
        Input input = new Input()
                .withReleaseName("Neon")
                .withReleaseVersion("4.6")
                .withUpdateSite("https://download.eclipse.org/eclipse/updates/4.6/");
        new ECentralTask(input).run();
    }
}
