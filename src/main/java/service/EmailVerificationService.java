package service;

import jakarta.mail.MessagingException;
import java.sql.SQLException;
import java.util.Locale;
import java.security.SecureRandom;
import model.User;
import repository.UserRepository;
import util.EmailUtil;

public class EmailVerificationService {

    private final UserRepository userRepository;
    private final SecureRandom random;

    public EmailVerificationService() {
        userRepository = new UserRepository();
        random = new SecureRandom();
    }

    public String sendOtp(String email)
            throws SQLException, MessagingException {

        email = validateEmail(email);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Email chưa được đăng ký trong hệ thống."
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "Tài khoản hiện không hoạt động."
            );
        }

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Email này đã được xác thực. Bạn có thể đăng nhập."
            );
        }

        String otp = createOtp();
        EmailUtil.sendVerificationOtp(user.getEmail(), otp);
        return otp;
    }

    public void markEmailVerified(String email) throws SQLException {
        email = validateEmail(email);
        User user = userRepository.findByEmail(email);

        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "Tài khoản không hợp lệ hoặc không hoạt động."
            );
        }

        if (user.isEmailVerified()) {
            return;
        }

        if (!userRepository.markEmailVerified(user.getUserId())) {
            throw new SQLException("Could not update email verification status.");
        }
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập email.");
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        if (email.length() > 100 || !email.contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }

        return email;
    }

    private String createOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }
}
