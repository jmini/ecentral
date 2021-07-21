package fr.jmini.utils.ecentral;

import java.util.List;

public class BndEntry {

    private String symbolicName;
    private List<String> versions;

    public BndEntry(String symbolicName, String version) {
        this(symbolicName, List.of(version));
    }

    public BndEntry(String symbolicName, List<String> versions) {
        this.symbolicName = symbolicName;
        this.versions = versions;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public List<String> getVersions() {
        return versions;
    }

}
