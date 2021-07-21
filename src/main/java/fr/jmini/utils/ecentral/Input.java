package fr.jmini.utils.ecentral;

public class Input {

    private String releaseName;

    private String releaseVersion;

    private String updateSite;

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public Input withReleaseName(String name) {
        setReleaseName(name);
        return this;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public Input withReleaseVersion(String version) {
        setReleaseVersion(version);
        return this;
    }

    public String getUpdateSite() {
        return updateSite;
    }

    public void setUpdateSite(String updateSite) {
        this.updateSite = updateSite;
    }

    public Input withUpdateSite(String site) {
        setUpdateSite(site);
        return this;
    }
}
