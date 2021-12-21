package fr.jmini.utils.ecentral;

/**
 * Entry representing a mavenMapping entry defined in file: https://git.eclipse.org/c/platform/eclipse.platform.releng.git/tree/publish-to-maven-central/SDK4Mvn.aggr#n34
 */
public class MavenMapping {

    private String namePattern;
    private String groupId;
    private String artifactId;

    private String versionPattern;
    private String versionTemplate;

    public MavenMapping(String namePattern, String groupId, String artifactId) {
        super();
        this.namePattern = namePattern;
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public MavenMapping(String namePattern, String groupId, String artifactId, String versionPattern, String versionTemplate) {
        super();
        this.namePattern = namePattern;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionPattern = versionPattern;
        this.versionTemplate = versionTemplate;
    }

    public String getNamePattern() {
        return namePattern;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersionPattern() {
        return versionPattern;
    }

    public String getVersionTemplate() {
        return versionTemplate;
    }
}
