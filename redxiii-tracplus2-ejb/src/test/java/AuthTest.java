
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;





/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dfilgueiras
 */
public class AuthTest {
 
    public static void main(String[] args) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            
            byte[] encoded = digest.digest("dfilgueiras:v2com-tracplus:1q2w3e4r".getBytes());
            
            String hash = Hex.encodeHexString(encoded);
            
            System.out.println(hash);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AuthTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
}
