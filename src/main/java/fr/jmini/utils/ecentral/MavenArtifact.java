package fr.jmini.utils.ecentral;

public class MavenArtifact {

    private String groupId;
    private String artifactId;
    private String version;

    public MavenArtifact(String groupId, String artifactId, String version) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }
}
