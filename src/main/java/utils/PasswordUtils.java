package utils;
import java.security.*;import java.util.*;import javax.crypto.*;import javax.crypto.spec.PBEKeySpec;
public final class PasswordUtils {
 public static String md5(String password){try{byte[] b=MessageDigest.getInstance("MD5").digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
 public static String hash(String password){try{byte[]salt=new byte[16];new SecureRandom().nextBytes(salt);byte[]h=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(password.toCharArray(),salt,210000,256)).getEncoded();return "pbkdf2$210000$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(h);}catch(Exception e){throw new IllegalStateException(e);}}
 public static boolean verify(String password,String stored){try{if(stored.startsWith("pbkdf2$")){String[]p=stored.split("\\$");byte[]h=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(password.toCharArray(),Base64.getDecoder().decode(p[2]),Integer.parseInt(p[1]),256)).getEncoded();return MessageDigest.isEqual(h,Base64.getDecoder().decode(p[3]));}return MessageDigest.isEqual(md5(password).getBytes(),stored.getBytes());}catch(Exception e){return false;}}
}
