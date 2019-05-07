
package ds.udp.file_transfer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yuzo
 */
public class FileIntegrityVerification {

    private String path;

    public FileIntegrityVerification() {
        
    }
    
    public FileIntegrityVerification(String path) {
        this.path = path;
    }
    
    public byte[] getMD5() throws IOException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (InputStream is = Files.newInputStream(Paths.get(path));
                DigestInputStream dis = new DigestInputStream(is, md)) {
        }
        byte[] digest = md.digest();
        return digest;
    }
    
    public boolean compareHash(byte[] hash1, byte[] hash2) {
        return Arrays.equals(hash1, hash2);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}