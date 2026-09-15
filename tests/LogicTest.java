import security.Access;import utils.PasswordUtils;import service.VNPay;import java.util.*;
class LogicTest {
 static int n=0;static void ok(boolean b,String message){if(!b)throw new AssertionError(message);n++;}
 public static void main(String[]a)throws Exception {
  String[] paths={"/movies","/movie","/booking/seats","/checkout","/booking/orders","/tickets/qr","/admin/movies","/admin/rooms","/admin/pricing","/system/users","/staff/checkin","/unknown"};
  String[]roles={null,"US","MG","AD","ST","ZZ"};for(String role:roles)for(String path:paths){boolean expected=(role==null&&(path.equals("/movies")||path.equals("/movie")))||("US".equals(role)&&List.of("/movies","/movie","/booking/seats","/checkout","/booking/orders","/tickets/qr").contains(path))||("MG".equals(role)&&path.startsWith("/admin/"))||("AD".equals(role)&&path.equals("/system/users"))||("ST".equals(role)&&path.equals("/staff/checkin"));ok(Access.allowed(role,path)==expected,"role "+role+" path "+path);}
  String hash=PasswordUtils.hash("Cinema-Test-2026!");ok(PasswordUtils.verify("Cinema-Test-2026!",hash),"PBKDF2 valid");ok(!PasswordUtils.verify("wrong",hash),"PBKDF2 wrong");ok(!hash.equals(PasswordUtils.hash("Cinema-Test-2026!")),"random salts");ok(PasswordUtils.verify("123456","e10adc3949ba59abbe56e057f20f883e"),"legacy MD5");ok(!PasswordUtils.verify(null,hash),"null password");
  System.setProperty("VNPAY_TMN_CODE","TEST1234");System.setProperty("VNPAY_HASH_SECRET","test-secret");Map<String,String>p=new TreeMap<>();p.put("vnp_TmnCode","TEST1234");p.put("vnp_Amount","8500000");p.put("vnp_TxnRef","ODTEST");String signature=VNPay.sign(VNPay.query(p));System.out.println("SIGNATURE="+signature);p.put("vnp_SecureHash",signature);ok(VNPay.verify(p),"valid callback signature");p.put("vnp_Amount","100");ok(!VNPay.verify(p),"amount tampering");p.put("vnp_Amount","8500000");p.put("vnp_TmnCode","OTHER");ok(!VNPay.verify(p),"wrong merchant");
  System.out.println("PASS "+n+" pure Java assertions. SQL, Servlet container, SMTP and gateway integration not exercised.");
 }
}
