package fr.jmini.utils.ecentral;

import org.junit.jupiter.api.Test;

/**
 * This test class run the code for different update sites.
 */
class RunTest {

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
