package controller;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.util.*;import java.sql.*;import java.security.*;import java.nio.charset.StandardCharsets;import dao.Sql;import utils.*;
@WebServlet(urlPatterns={"/reset"}) public class ResetServlet extends HttpServlet {
 private static String digest(String value)throws Exception {StringBuilder s=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)))s.append(String.format("%02x",b));return s.toString();}
 protected void doGet(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{q.getRequestDispatcher("/WEB-INF/views/auth/reset.jsp").forward(q,r);}
 protected void doPost(HttpServletRequest q,HttpServletResponse r)throws ServletException,IOException{
  try{
   String token=q.getParameter("token");
   if(token!=null&&!token.isBlank()){
    String pw=q.getParameter("password");if(pw==null||pw.length()<8)throw new IllegalArgumentException("Mật khẩu mới cần ít nhất 8 ký tự.");
    try(Connection c=DBUtils.getConnection()){c.setAutoCommit(false);try{Map<String,Object>x=Sql.one(c,"SELECT userID FROM tblPasswordResets WITH(UPDLOCK,HOLDLOCK) WHERE tokenHash=? AND used=0 AND expiresAt>SYSDATETIME()",digest(token));if(x==null)throw new IllegalArgumentException("Liên kết đã hết hạn hoặc đã sử dụng.");Sql.update(c,"UPDATE tblUsers SET password=? WHERE userID=? AND status=1",PasswordUtils.hash(pw),x.get("userID"));Sql.update(c,"UPDATE tblPasswordResets SET used=1 WHERE userID=?",x.get("userID"));c.commit();q.setAttribute("success","Đã đổi mật khẩu. Bạn có thể đăng nhập.");}catch(Exception e){c.rollback();throw e;}}
   }else{
    String email=q.getParameter("email");if(email==null||!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))throw new IllegalArgumentException("Email không hợp lệ.");
    Long last=(Long)q.getSession().getAttribute("RESET_AT");if(last!=null&&System.currentTimeMillis()-last<60000)throw new IllegalArgumentException("Vui lòng chờ một phút trước khi thử lại.");q.getSession().setAttribute("RESET_AT",System.currentTimeMillis());
    String smtp=DBUtils.config("SMTP_HOST",null),base=DBUtils.config("FCINEMA_PUBLIC_URL",null);if(smtp==null||base==null)throw new IllegalArgumentException("Dịch vụ email chưa được cấu hình. Vui lòng liên hệ rạp.");
    List<Map<String,Object>> rows=Sql.rows("SELECT userID FROM tblUsers WHERE email=? AND status=1",email.trim());
    if(rows.size()==1){String user=rows.get(0).get("userID").toString();
     if(Sql.rows("SELECT tokenHash FROM tblPasswordResets WHERE userID=? AND expiresAt>DATEADD(MINUTE,14,SYSDATETIME())",user).isEmpty()){
      byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String plain=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);String hash=digest(plain);
      Sql.update("INSERT INTO tblPasswordResets(tokenHash,userID,expiresAt,used) VALUES(?,?,DATEADD(MINUTE,15,SYSDATETIME()),0)",hash,user);
      try{send(email,base+"/reset?token="+plain);}catch(Exception ex){Sql.update("DELETE FROM tblPasswordResets WHERE tokenHash=?",hash);throw ex;}
     }
    }
    q.setAttribute("success","Nếu email có tài khoản đang hoạt động, liên kết đặt lại mật khẩu sẽ được gửi và có hiệu lực 15 phút.");
   }
  }catch(Exception e){getServletContext().log("Password reset failed",e);q.setAttribute("error",e instanceof IllegalArgumentException?e.getMessage():"Chưa gửi được email. Vui lòng thử lại sau.");}doGet(q,r);
 }
 private void send(String to,String link)throws Exception{
  Properties p=new Properties();p.setProperty("mail.smtp.host",DBUtils.config("SMTP_HOST",""));p.setProperty("mail.smtp.port",DBUtils.config("SMTP_PORT","587"));p.setProperty("mail.smtp.auth","true");p.setProperty("mail.smtp.starttls.enable","true");p.setProperty("mail.smtp.starttls.required","true");p.setProperty("mail.smtp.connectiontimeout","10000");p.setProperty("mail.smtp.timeout","10000");p.setProperty("mail.smtp.writetimeout","10000");
  jakarta.mail.Session session=jakarta.mail.Session.getInstance(p,new jakarta.mail.Authenticator(){protected jakarta.mail.PasswordAuthentication getPasswordAuthentication(){return new jakarta.mail.PasswordAuthentication(DBUtils.config("SMTP_USER",""),DBUtils.config("SMTP_PASSWORD",""));}});
  jakarta.mail.internet.MimeMessage m=new jakarta.mail.internet.MimeMessage(session);m.setFrom(new jakarta.mail.internet.InternetAddress(DBUtils.config("SMTP_FROM",DBUtils.config("SMTP_USER",""))));m.setRecipients(jakarta.mail.Message.RecipientType.TO,jakarta.mail.internet.InternetAddress.parse(to));m.setSubject("F-Cinema — Đặt lại mật khẩu","UTF-8");m.setText("Mở liên kết để đặt lại mật khẩu (hiệu lực 15 phút):\n"+link+"\nNếu bạn không yêu cầu, hãy bỏ qua email này.","UTF-8");jakarta.mail.Transport.send(m);
 }
}
