package fr.jmini.utils.ecentral;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum Algorithm {

    MD_5("MD5", ".md5"), SHA_1("SHA-1", ".sha1"), SHA_256("SHA-256", ".sha256"), SHA_512("SHA-512", ".sha512");

    private String aglorithm;
    private String extension;

    private Algorithm(String aglorithm, String extension) {
        this.aglorithm = aglorithm;
        this.extension = extension;
    }

    public MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(aglorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not get MessageDigest", e);
        }
    }

    public String getExtension() {
        return extension;
    }
}
