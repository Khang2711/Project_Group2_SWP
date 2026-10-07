package util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    public static void sendOtp(String toEmail, String otp)
            throws MessagingException {

        String fromEmail = System.getenv("FCINEMA_EMAIL");
        String appPassword = System.getenv(
                "FCINEMA_EMAIL_APP_PASSWORD"
        );

        if (fromEmail == null || fromEmail.trim().isEmpty()
                || appPassword == null
                || appPassword.trim().isEmpty()) {

            throw new MessagingException(
                    "Chưa cấu hình email gửi OTP."
            );
        }

        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);

        Session mailSession = Session.getInstance(
                properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication
                            getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                fromEmail,
                                appPassword
                        );
                    }
                }
        );

        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
        );

        message.setSubject(
                "F-Cinema - Mã OTP đặt lại mật khẩu",
                "UTF-8"
        );

        message.setText(
                "Mã OTP đặt lại mật khẩu F-Cinema của bạn là: "
                + otp
                + "\n\nMã OTP có hiệu lực trong 5 phút."
                + "\nNếu bạn không yêu cầu đặt lại mật khẩu, "
                + "hãy bỏ qua email này.",
                "UTF-8"
        );

        Transport.send(message);
    }
}
