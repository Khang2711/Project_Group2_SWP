package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.InvalidKeySpecException;

/**
 * Password storage: PBKDF2-SHA256 for new passwords, with MD5 verification
 * compatibility for existing accounts. Old hashes are upgraded at login.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 210000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final String PREFIX = "pbkdf2_sha256";

    private PasswordUtil() {
    }

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password.toCharArray(), salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean matches(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        if (storedHash.startsWith(PREFIX + "$")) {
            try {
                String[] parts = storedHash.split("\\$", -1);
                if (parts.length != 4) {
                    return false;
                }
                int iterations = Integer.parseInt(parts[1]);
                if (iterations < 10000 || iterations > 1000000) {
                    return false;
                }
                byte[] salt = Base64.getDecoder().decode(parts[2]);
                byte[] expected = Base64.getDecoder().decode(parts[3]);
                if (salt.length != SALT_BYTES || expected.length != KEY_BITS / 8) {
                    return false;
                }
                byte[] actual = derive(password.toCharArray(), salt, iterations);
                return MessageDigest.isEqual(expected, actual);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // Legacy MD5 accounts remain usable until upgraded on successful login.
        if (!storedHash.matches("(?i)[a-f0-9]{32}")) {
            return false;
        }
        byte[] expected = storedHash.toLowerCase(java.util.Locale.ROOT)
                .getBytes(StandardCharsets.US_ASCII);
        byte[] actual = md5(password).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }

    public static boolean needsUpgrade(String storedHash) {
        return storedHash != null && !storedHash.startsWith(PREFIX + "$");
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 is unavailable.", e);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(password, '\\0');
        }
    }

    // Kept for existing seed fixtures and legacy password validation.
    public static String md5(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : bytes) {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is not available.", e);
        }
    }
}
