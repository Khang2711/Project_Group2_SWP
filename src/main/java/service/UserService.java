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

    public String getRoleName(int roleId) throws SQLException {
        Role role = roleRepository.findById(roleId);

        if (role == null) {
            return null;
        }

        return role.getRoleName();
    }

    public boolean canEditAccount(User user) throws SQLException {
        if (user == null) {
            return false;
        }

        String roleName = getRoleName(user.getRoleId());

        return "STAFF".equals(roleName)
                || "CINEMA_MANAGER".equals(roleName);
    }

    public void createUser(User user, String password, Integer cinemaId)
            throws SQLException {

        validateCreateUser(user, password, cinemaId);

        User sameEmail = userRepository.findByEmail(user.getEmail());

        if (sameEmail != null) {
            throw new IllegalArgumentException(
                    "Email đã được sử dụng bởi tài khoản khác."
            );
        }

        user.setPasswordHash(PasswordUtil.md5(password));
        user.setStatus("ACTIVE");

        int userId = userRepository.insert(user);
        user.setUserId(userId);

        saveProfileByRole(userId, user.getRoleId(), cinemaId);
    }

    public void updateRoleAndCinema(int userId,
            int roleId,
            Integer cinemaId) throws SQLException {

        User currentUser = userRepository.findById(userId);

        if (currentUser == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy tài khoản."
            );
        }

        if (!canEditAccount(currentUser)) {
            throw new IllegalArgumentException(
                    "Chỉ được chỉnh sửa tài khoản STAFF hoặc CINEMA_MANAGER."
            );
        }

        validateRoleAndCinema(roleId, cinemaId);

        currentUser.setRoleId(roleId);
        currentUser.setPasswordHash(null);

        userRepository.update(currentUser);

        saveProfileByRole(userId, roleId, cinemaId);
    }

    public void deactivateUser(int userId, int currentUserId)
            throws SQLException {

        User user = getExistingUser(userId);

        if (userId == currentUserId) {
            throw new IllegalArgumentException(
                    "Không thể vô hiệu hóa tài khoản đang đăng nhập."
            );
        }

        userRepository.updateStatus(userId, "INACTIVE");
    }

    public void activateUser(int userId) throws SQLException {
        getExistingUser(userId);
        userRepository.updateStatus(userId, "ACTIVE");
    }

    private User getExistingUser(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản không hợp lệ."
            );
        }

        User user = userRepository.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy tài khoản."
            );
        }

        return user;
    }

    private void validateCreateUser(User user,
            String password,
            Integer cinemaId) throws SQLException {

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

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập mật khẩu."
            );
        }

        validateRoleAndCinema(user.getRoleId(), cinemaId);
    }

    private void validateRoleAndCinema(int roleId, Integer cinemaId)
            throws SQLException {

        Role role = roleRepository.findById(roleId);

        if (role == null || !"ACTIVE".equals(role.getStatus())) {
            throw new IllegalArgumentException(
                    "Vai trò không hợp lệ."
            );
        }

        String roleName = role.getRoleName();

        if ("STAFF".equals(roleName)
                || "CINEMA_MANAGER".equals(roleName)) {

            if (cinemaId == null || cinemaId <= 0) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn rạp cho STAFF hoặc CINEMA_MANAGER."
                );
            }

            Cinema cinema = cinemaRepository.findById(cinemaId);

            if (cinema == null || !"ACTIVE".equals(cinema.getStatus())) {
                throw new IllegalArgumentException(
                        "Rạp được chọn không hợp lệ."
                );
            }
        }
    }

    private void saveProfileByRole(int userId,
            int roleId,
            Integer cinemaId) throws SQLException {

        String roleName = getRoleName(roleId);

        if ("CUSTOMER".equals(roleName)) {
            userRepository.ensureCustomer(userId);
            return;
        }

        if ("STAFF".equals(roleName)
                || "CINEMA_MANAGER".equals(roleName)) {
            userRepository.saveStaffCinema(userId, cinemaId);
        }
    }
}
