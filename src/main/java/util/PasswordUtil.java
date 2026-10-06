package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static String md5(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            byte[] hashBytes = messageDigest.digest(
                    password.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();

            for (byte value : hashBytes) {
                result.append(String.format("%02x", value & 0xff));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is not available.", e);
        }
    }
}