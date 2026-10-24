package com.slotit.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing using SHA-256.
 */
public class PasswordUtil {
    
    // Fixed simple salt for this project (though random per user is better)
    // We'll use a constant string if generateSalt isn't requested to be saved per user.
    private static final String FIXED_SALT = "SlotItSecureSalt2024!";

    /**
     * Hashes a password using SHA-256 with a salt.
     */
    public static String hashPassword(String password) {
        return hashPassword(password, FIXED_SALT);
    }
    
    /**
     * Verifies a password against a hash.
     */
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        String inputHash = hashPassword(inputPassword);
        return inputHash != null && inputHash.equals(storedHash);
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            
            // Convert to Hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a random salt string (optional for extra security).
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
