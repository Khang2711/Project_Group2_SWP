package service;

import java.sql.SQLException;
import model.Role;
import model.User;
import repository.RoleRepository;
import repository.UserRepository;
import util.PasswordUtil;

public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AuthService() {
        userRepository = new UserRepository();
        roleRepository = new RoleRepository();
    }

    public User login(String email, String password) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập email.");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu.");
        }

        email = email.trim();

        if (email.length() > 100 || !email.contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }

        User user = userRepository.findByEmail(email);

        String passwordHash = PasswordUtil.md5(password);

        if (user == null
                || !passwordHash.equalsIgnoreCase(user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "Email hoặc mật khẩu không đúng."
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "Tài khoản không hoạt động hoặc đã bị khóa."
            );
        }

        // Do not keep the password hash in the login session.
        user.setPasswordHash(null);

        return user;
    }

    public String getRoleName(int roleId) throws SQLException {
        Role role = roleRepository.findById(roleId);

        if (role == null || !"ACTIVE".equals(role.getStatus())) {
            throw new IllegalArgumentException(
                    "Vai trò của tài khoản không hoạt động."
            );
        }

        String roleName = role.getRoleName();

        if (getHomePath(roleName) == null) {
            throw new IllegalArgumentException(
                    "Vai trò của tài khoản chưa được hỗ trợ."
            );
        }

        return roleName;
    }

    public String getHomePath(String roleName) {
        if (roleName == null) {
            return null;
        }

        switch (roleName) {
            case "CUSTOMER":
                return "/user/movies";

            case "STAFF":
                return "/staff/check-in";

            case "CINEMA_MANAGER":
                return "/admin/dashboard";

            case "ADMIN":
                return "/system/users";

            default:
                return null;
        }
    }
}