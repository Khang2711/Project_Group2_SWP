package service;

import java.sql.SQLException;
import java.util.List;
import model.User;
import model.Role;
import model.Cinema;
import repository.UserRepository;
import util.PasswordUtil;

/** Kiểm tra dữ liệu trước khi gọi DAO. */
public class UserService {
    private final UserRepository repository;
    public UserService() { this.repository = new UserRepository(); }
    public UserService(UserRepository repository) { this.repository = repository; }
    public List<User> findAll() throws SQLException { return repository.findAll(); }
    public User findById(int id) throws SQLException { return repository.findById(id); }
    public List<Role> findRoles() throws SQLException { return repository.findRoles(); }
    public List<Cinema> findCinemas() throws SQLException { return repository.findCinemas(); }

    public boolean isActiveAdmin(User user) throws SQLException {
        if (user == null || !"ACTIVE".equals(user.getStatus())) return false;
        for (Role role : findRoles()) {
            if (role.getRoleId() == user.getRoleId()) {
                return "ADMIN".equals(role.getRoleName()) && "ACTIVE".equals(role.getStatus());
            }
        }
        return false;
    }

    public void save(User user, String password, int adminId) throws SQLException {
        user.setFullName(clean(user.getFullName()));
        user.setEmail(clean(user.getEmail()));
        user.setPhone(clean(user.getPhone()));
        if (user.getFullName().isEmpty() || user.getFullName().length() > 100)
            throw new IllegalArgumentException("Họ tên phải có từ 1 đến 100 ký tự.");
        if (user.getEmail().length() > 100 || !user.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Email không hợp lệ hoặc dài quá 100 ký tự.");
        if (!user.getPhone().isEmpty() && !user.getPhone().matches("\\+?[0-9]{9,15}"))
            throw new IllegalArgumentException("Số điện thoại gồm 9–15 chữ số, có thể bắt đầu bằng +.");
        if (!"ACTIVE".equals(user.getStatus()) && !"INACTIVE".equals(user.getStatus()))
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        User old = user.getUserId() == 0 ? null : findById(user.getUserId());
        if (user.getUserId() != 0 && old == null)
            throw new IllegalArgumentException("Tài khoản không tồn tại.");
        Role selected = null;
        for (Role role : findRoles()) if (role.getRoleId() == user.getRoleId()) selected = role;
        if (selected == null || !"ACTIVE".equals(selected.getStatus()))
            throw new IllegalArgumentException("Vui lòng chọn vai trò đang hoạt động.");
        String name = selected.getRoleName();
        if (!"ADMIN".equals(name) && !"CUSTOMER".equals(name)
                && !"STAFF".equals(name) && !"CINEMA_MANAGER".equals(name))
            throw new IllegalArgumentException("Vai trò này chưa được hỗ trợ.");
        if (user.getUserId() == adminId && (!"ADMIN".equals(name) || !"ACTIVE".equals(user.getStatus())))
            throw new IllegalArgumentException("Bạn không thể tự gỡ quyền admin hoặc vô hiệu hóa chính mình.");
        if ("STAFF".equals(name) || "CINEMA_MANAGER".equals(name)) {
            boolean valid = false;
            for (Cinema cinema : findCinemas()) {
                if (Integer.valueOf(cinema.getCinemaId()).equals(user.getCinemaId())) {
                    // Cho phép giữ chi nhánh cũ đã ngừng hoạt động; không gán mới vào đó.
                    valid = "ACTIVE".equals(cinema.getStatus())
                            || (old != null && user.getCinemaId().equals(old.getCinemaId()));
                }
            }
            if (!valid) throw new IllegalArgumentException("Nhân viên và quản lý rạp cần một chi nhánh hợp lệ.");
        } else user.setCinemaId(null);
        User sameEmail = repository.findByEmail(user.getEmail());
        if (sameEmail != null && sameEmail.getUserId() != user.getUserId())
            throw new IllegalArgumentException("Email đã được sử dụng.");
        if (password == null) password = "";
        if (old == null && password.isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu cho tài khoản mới.");
        if (!password.isEmpty() && (password.length() < 6 || password.length() > 100))
            throw new IllegalArgumentException("Mật khẩu phải có từ 6 đến 100 ký tự.");
        user.setPasswordHash(password.isEmpty() ? null : PasswordUtil.md5(password));
        repository.save(user, name);
    }

    public void deactivate(int id, int adminId) throws SQLException {
        if (id == adminId) throw new IllegalArgumentException("Bạn không thể tự vô hiệu hóa chính mình.");
        if (!repository.deactivate(id)) throw new IllegalArgumentException("Tài khoản không tồn tại.");
    }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
