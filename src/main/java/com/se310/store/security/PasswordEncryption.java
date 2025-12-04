package com.se310.store.security;

import com.se310.store.config.ConfigLoader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * PasswordEncryption - Utility class for encrypting and decrypting passwords using AES encryption.
 *
 * This class demonstrates security best practices:
 * - Passwords are never stored in plain text
 * - Encryption key is externalized in configuration
 * - Uses industry-standard AES encryption
 * - Provides both encryption and verification methods
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since   2025-11-13
 */
public class PasswordEncryption {

    private static final String ALGORITHM = "AES";
    private static SecretKeySpec secretKey;

    // Private constructor to prevent instantiation
    private PasswordEncryption() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Initialize the secret key from the encryption key in configuration
     *
     * @param encryptionKey The encryption key from application.properties
     */
    private static void setKey(String encryptionKey) {
        try {
            byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // Use only first 128 bits (16 bytes) for AES-128
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing encryption key", e);
        }
    }

    /**
     * Encrypt a password using AES encryption
     *
     * @param password The plain text password to encrypt
     * @return The encrypted password as a Base64-encoded string
     */
    public static String encrypt(String password) {
        try {
            setKey(ConfigLoader.getEncryptionKey());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting password", e);
        }
    }

    /**
     * Decrypt an encrypted password
     *
     * @param encryptedPassword The encrypted password as a Base64-encoded string
     * @return The decrypted plain text password
     */
    public static String decrypt(String encryptedPassword) {
        try {
            setKey(ConfigLoader.getEncryptionKey());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting password", e);
        }
    }

    /**
     * Verify if a plain text password matches an encrypted password
     *
     * @param plainPassword The plain text password to verify
     * @param encryptedPassword The encrypted password to compare against
     * @return true if the passwords match, false otherwise
     */
    public static boolean verify(String plainPassword, String encryptedPassword) {
        try {
            String decrypted = decrypt(encryptedPassword);
            return plainPassword.equals(decrypted);
        } catch (Exception e) {
            // If decryption fails, passwords don't match
            return false;
        }
    }

    /**
     * Check if a password is already encrypted
     * This is a simple heuristic - encrypted passwords are Base64-encoded and typically longer
     *
     * @param password The password to check
     * @return true if the password appears to be encrypted, false otherwise
     */
    public static boolean isEncrypted(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Encrypted passwords are Base64-encoded and typically longer than plain text
        // Also check if it's valid Base64
        try {
            Base64.getDecoder().decode(password);
            // If it decodes successfully and is reasonably long, it's likely encrypted
            return password.length() > 20;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}