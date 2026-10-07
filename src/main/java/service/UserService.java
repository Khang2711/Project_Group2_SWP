package service;

import java.sql.SQLException;
import java.util.List;
import model.Cinema;
import model.Role;
import model.User;
import repository.CinemaRepository;
import repository.RoleRepository;
import repository.UserRepository;
import util.PasswordUtil;

public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CinemaRepository cinemaRepository;

    public UserService() {
        userRepository = new UserRepository();
        roleRepository = new RoleRepository();
        cinemaRepository = new CinemaRepository();
    }

    public List<User> getAllUsers() throws SQLException {
        return userRepository.getAll();
    }

    public User getUserById(int userId) throws SQLException {
        return userRepository.findById(userId);
    }

    public List<Role> getActiveRoles() throws SQLException {
        return roleRepository.getAllActive();
    }

    public List<Cinema> getActiveCinemas() {
        return cinemaRepository.getAllActive();
    }

    public void saveUser(User user, String password, Integer cinemaId)
            throws SQLException {

        validateUser(user, password, cinemaId);

        Role role = roleRepository.findById(user.getRoleId());
        String roleName = role.getRoleName();

        User sameEmail = userRepository.findByEmail(user.getEmail());

        if (sameEmail != null
                && sameEmail.getUserId() != user.getUserId()) {
            throw new IllegalArgumentException(
                    "Email đã được sử dụng bởi tài khoản khác."
            );
        }

        if (user.getUserId() == 0) {
            createUser(user, password);
        } else {
            updateUser(user, password);
        }

        if ("CUSTOMER".equals(roleName)) {
            userRepository.ensureCustomer(user.getUserId());
        }

        if ("STAFF".equals(roleName)
                || "CINEMA_MANAGER".equals(roleName)) {
            userRepository.saveStaffCinema(
                    user.getUserId(),
                    cinemaId
            );
        }
    }

    public void deactivateUser(int userId, int currentUserId)
            throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản không hợp lệ."
            );
        }

        if (userId == currentUserId) {
            throw new IllegalArgumentException(
                    "Không thể vô hiệu hóa tài khoản đang đăng nhập."
            );
        }

        User user = userRepository.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy tài khoản."
            );
        }

        userRepository.updateStatus(userId, "INACTIVE");
    }

    private void createUser(User user, String password)
            throws SQLException {

        user.setPasswordHash(PasswordUtil.md5(password));
        user.setStatus("ACTIVE");

        int userId = userRepository.insert(user);
        user.setUserId(userId);
    }

    private void updateUser(User user, String password)
            throws SQLException {

        User currentUser = userRepository.findById(user.getUserId());

        if (currentUser == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy tài khoản."
            );
        }

        user.setStatus(currentUser.getStatus());
        user.setAvatarUrl(currentUser.getAvatarUrl());

        if (password == null || password.isEmpty()) {
            user.setPasswordHash(null);
        } else {
            user.setPasswordHash(PasswordUtil.md5(password));
        }

        userRepository.update(user);
    }

    private void validateUser(User user, String password, Integer cinemaId)
            throws SQLException {

        if (user.getFullName() == null
                || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập họ tên."
            );
        }

        user.setFullName(user.getFullName().trim());

        if (user.getFullName().length() > 100) {
            throw new IllegalArgumentException(
                    "Họ tên không được vượt quá 100 ký tự."
            );
        }

        if (user.getEmail() == null
                || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập email."
            );
        }

        user.setEmail(user.getEmail().trim().toLowerCase());

        if (user.getEmail().length() > 100
                || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException(
                    "Email không hợp lệ."
            );
        }

        if (user.getPhone() != null) {
            user.setPhone(user.getPhone().trim());

            if (user.getPhone().isEmpty()) {
                user.setPhone(null);
            } else if (user.getPhone().length() > 20) {
                throw new IllegalArgumentException(
                        "Số điện thoại không được vượt quá 20 ký tự."
                );
            }
        }

        Role role = roleRepository.findById(user.getRoleId());

        if (role == null || !"ACTIVE".equals(role.getStatus())) {
            throw new IllegalArgumentException(
                    "Vai trò không hợp lệ."
            );
        }

        String roleName = role.getRoleName();

        if (("STAFF".equals(roleName)
                || "CINEMA_MANAGER".equals(roleName))) {

            if (cinemaId == null || cinemaId <= 0) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn rạp cho nhân viên hoặc quản lý rạp."
                );
            }

            Cinema cinema = cinemaRepository.findById(cinemaId);

            if (cinema == null || !"ACTIVE".equals(cinema.getStatus())) {
                throw new IllegalArgumentException(
                        "Rạp được chọn không hợp lệ."
                );
            }
        }

        if (user.getUserId() == 0
                && (password == null || password.isEmpty())) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập mật khẩu."
            );
        }
    }
}
