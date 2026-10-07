package service;

import jakarta.mail.MessagingException;
import java.security.SecureRandom;
import java.sql.SQLException;
import model.User;
import repository.UserRepository;
import util.EmailUtil;
import util.PasswordUtil;

public class PasswordResetService {

    private final UserRepository userRepository;
    private final SecureRandom random;

    public PasswordResetService() {
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

        String otp = createOtp();

        EmailUtil.sendOtp(user.getEmail(), otp);

        return otp;
    }

    public void resetPassword(String email,
            String newPassword,
            String confirmPassword)
            throws SQLException {

        email = validateEmail(email);

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException(
                    "Mật khẩu mới phải có ít nhất 6 ký tự."
            );
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Mật khẩu xác nhận không khớp."
            );
        }

        User user = userRepository.findByEmail(email);

        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "Tài khoản không hợp lệ."
            );
        }

        String passwordHash = PasswordUtil.md5(newPassword);

        userRepository.updatePassword(
                user.getUserId(),
                passwordHash
        );
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập email."
            );
        }

        email = email.trim().toLowerCase();

        if (email.length() > 100 || !email.contains("@")) {
            throw new IllegalArgumentException(
                    "Email không hợp lệ."
            );
        }

        return email;
    }

    private String createOtp() {
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}
