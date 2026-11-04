package service;

import java.sql.SQLException;
import java.util.Locale;
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

        email = email.trim().toLowerCase(Locale.ROOT);

        if (email.length() > 100 || !email.contains("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
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

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Email chưa được xác thực. Vui lòng xác thực email trước khi đăng nhập."
            );
        }

        user.setPasswordHash(null);

        return user;
    }

    public void register(String fullName, String email, String phone,
            String password, String confirmPassword) throws SQLException {

        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        phone = phone == null ? "" : phone.trim();

        checkRegisterInput(fullName, email, phone, password, confirmPassword);

        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException(
                    "Email này đã được sử dụng."
            );
        }

        Role customerRole = roleRepository.findByName("CUSTOMER");

        if (customerRole == null
                || !"ACTIVE".equals(customerRole.getStatus())) {
            throw new SQLException(
                    "The CUSTOMER role is missing or inactive."
            );
        }

        User user = new User();

        user.setRoleId(customerRole.getRoleId());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(PasswordUtil.md5(password));
        user.setStatus("ACTIVE");
        user.setEmailVerified(false);

        userRepository.insertCustomer(user);
    }

    // Return the role name of an active role.
    public String getRoleName(int roleId) throws SQLException {
        Role role = roleRepository.findById(roleId);

        if (role == null || !"ACTIVE".equals(role.getStatus())) {
            throw new IllegalArgumentException(
                    "Vai trò của tài khoản không hoạt động."
            );
        }

        return role.getRoleName();
    }

    private void checkRegisterInput(String fullName, String email,
            String phone, String password, String confirmPassword) {

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()
                || password == null || password.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập đầy đủ thông tin."
            );
        }

        if (fullName.length() > 100) {
            throw new IllegalArgumentException(
                    "Họ tên không được vượt quá 100 ký tự."
            );
        }

        String emailPattern
                = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

        if (email.length() > 100 || !email.matches(emailPattern)) {
            throw new IllegalArgumentException(
                    "Email không hợp lệ."
            );
        }

        if (!phone.matches("[0-9]{10}")) {
            throw new IllegalArgumentException(
                    "Số điện thoại phải gồm đúng 10 chữ số."
            );
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException(
                    "Mật khẩu phải có ít nhất 6 ký tự."
            );
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Mật khẩu xác nhận không khớp."
            );
        }
    }
}
