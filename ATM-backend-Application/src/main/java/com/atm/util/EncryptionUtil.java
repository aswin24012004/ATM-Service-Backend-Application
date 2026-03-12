package com.atm.util;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
public final class EncryptionUtil {
    private EncryptionUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
    private static String getKey() {
        String key = ConfigUtil.get("SECRET");
        if (key == null || key.length() != 32) {
            throw new IllegalArgumentException("SECRET must be 32 characters long");
        }
        return key;
    }
    public static String encrypt(String data) throws Exception {
        String key = getKey();
        // Generate random IV
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        // Prepend IV to ciphertext so we can recover it later
        byte[] combined = new byte[ivBytes.length + encrypted.length];
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        System.arraycopy(encrypted, 0, combined, ivBytes.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }
    public static String decrypt(String encrypted) throws Exception {
        String key = getKey();
        byte[] combined = Base64.getDecoder().decode(encrypted);
        // Extract IV and ciphertext
        byte[] ivBytes = new byte[16];
        byte[] cipherBytes = new byte[combined.length - 16];
        System.arraycopy(combined, 0, ivBytes, 0, 16);
        System.arraycopy(combined, 16, cipherBytes, 0, cipherBytes.length);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] original = cipher.doFinal(cipherBytes);
        return new String(original, StandardCharsets.UTF_8);
    }
}
