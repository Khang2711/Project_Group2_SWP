package service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import model.User;
import repository.UserRepository;
import util.PasswordUtil;

public class ProfileService {

    private final UserRepository userRepository = new UserRepository();

    public User getProfile(int userId) throws SQLException {
        User user = findCustomer(userId);
        user.setPasswordHash(null);
        return user;
    }

    public User updateProfile(int userId, String fullName, String email,
            String phone, String avatarUrl, String currentPassword,
            String newPassword) throws SQLException {

        User currentUser = findCustomer(userId);

        fullName = clean(fullName);
        email = currentUser.getEmail();
        phone = clean(phone);
        avatarUrl = clean(avatarUrl);

        currentPassword = currentPassword == null ? "" : currentPassword;
        newPassword = newPassword == null ? "" : newPassword;

        validateInput(fullName, email, phone, avatarUrl);

        boolean passwordChanged = !newPassword.isEmpty();

        if (passwordChanged
                && (currentPassword.isEmpty()
                || !PasswordUtil.md5(currentPassword).equalsIgnoreCase(
                        currentUser.getPasswordHash()))) {

            throw new IllegalArgumentException(
                    "Mật khẩu hiện tại không đúng.");
        }

        if (passwordChanged && newPassword.length() < 6) {
            throw new IllegalArgumentException(
                    "Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        User updatedUser = new User();

        updatedUser.setUserId(userId);
        updatedUser.setRoleId(currentUser.getRoleId());
        updatedUser.setFullName(fullName);
        updatedUser.setEmail(currentUser.getEmail());
        updatedUser.setPhone(phone);
        updatedUser.setAvatarUrl(avatarUrl.isEmpty() ? null : avatarUrl);
        updatedUser.setStatus(currentUser.getStatus());
        updatedUser.setEmailVerified(currentUser.isEmailVerified());
        updatedUser.setCreatedAt(currentUser.getCreatedAt());

        if (passwordChanged) {
            updatedUser.setPasswordHash(PasswordUtil.md5(newPassword));
        }

        if (!userRepository.updateProfile(
                updatedUser,
                currentUser.getEmail(),
                currentUser.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Tài khoản đã thay đổi hoặc không còn hoạt động. "
                    + "Vui lòng đăng nhập lại.");
        }

        updatedUser.setPasswordHash(null);
        return updatedUser;
    }

    private User findCustomer(int userId) throws SQLException {
        User user = userRepository.findProfileById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Tài khoản không tồn tại "
                    + "hoặc không có quyền truy cập Profile.");
        }

        return user;
    }

    private void validateInput(String fullName, String email,
            String phone, String avatarUrl) {

        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new IllegalArgumentException(
                    "Họ tên phải có từ 1 đến 100 ký tự.");
        }

        if (email.length() > 100
                || !email.matches("^[^\\s@]+@[^\\s@]+$")) {

            throw new IllegalArgumentException(
                    "Email phải có dạng text@text và không chứa khoảng trắng.");
        }

        if (!phone.matches("[0-9]{10}")) {
            throw new IllegalArgumentException(
                    "Số điện thoại phải gồm đúng 10 chữ số.");
        }

        if (avatarUrl.length() > 500) {
            throw new IllegalArgumentException(
                    "URL ảnh đại diện không được vượt quá 500 ký tự.");
        }

        if (!avatarUrl.isEmpty()
                && !new AvatarService().isAvatarPath(avatarUrl)) {
            try {
                URI uri = new URI(avatarUrl);

                if (!("http".equalsIgnoreCase(uri.getScheme())
                        || "https".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null
                        || uri.getUserInfo() != null) {

                    throw new IllegalArgumentException(
                            "Ảnh đại diện phải là URL "
                            + "http hoặc https hợp lệ.");
                }

            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(
                        "URL ảnh đại diện không hợp lệ.");
            }
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
