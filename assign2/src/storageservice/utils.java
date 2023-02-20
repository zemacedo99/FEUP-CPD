package storageservice;
import java.net.ServerSocket;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class utils {
    
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");
 
        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash)
    {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);
 
        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));
 
        // Pad with leading zeros
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }
 
        return hexString.toString();
    }

    public static String sha_256(String input)
    {
        String afterSHA = "fail hash";
        try
        {   
            afterSHA = toHexString(getSHA(input));
        }
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown for incorrect algorithm: " + e);
        }
        return afterSHA;
    }


    public int getFreeTcpPort(){
        int freePort = -1;
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            freePort = serverSocket.getLocalPort();
            return freePort;
        } catch (IOException e) {
            e.printStackTrace();
            return freePort;
        }
        
        
    }


}
