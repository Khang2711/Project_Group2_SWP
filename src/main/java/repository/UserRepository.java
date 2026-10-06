package repository;

import db.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;
import model.Role;
import model.Cinema;

/** DAO cho UC-03. Mỗi thao tác tự đóng connection, statement và result set. */
public class UserRepository {
    private static final String SELECT_USER =
            "SELECT u.user_id, u.role_id, u.full_name, u.email, u.phone, "
            + "u.password_hash, u.avatar_url, u.status, u.created_at, u.updated_at, "
            + "r.role_name, s.cinema_id, c.cinema_name FROM dbo.[User] u "
            + "JOIN dbo.[Role] r ON r.role_id = u.role_id "
            + "LEFT JOIN dbo.Staff s ON s.staff_id = u.user_id "
            + "LEFT JOIN dbo.Cinema c ON c.cinema_id = s.cinema_id ";

    private Connection openConnection() throws SQLException {
        Connection connection = new DBContext().getConnection();
        if (connection == null) throw new SQLException("Cannot connect to FCinemaDB.");
        return connection;
    }

    private User readUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setRoleId(rs.getInt("role_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        user.setRoleName(rs.getString("role_name"));
        // Giữ bản ghi Staff cũ để không làm mất lịch sử bán hàng.
        // Chỉ hiển thị chi nhánh khi vai trò hiện tại cần chi nhánh.
        if ("STAFF".equals(user.getRoleName()) || "CINEMA_MANAGER".equals(user.getRoleName())) {
            int cinemaId = rs.getInt("cinema_id");
            user.setCinemaId(rs.wasNull() ? null : cinemaId);
            user.setCinemaName(rs.getString("cinema_name"));
        }
        return user;
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection con = openConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_USER + "ORDER BY u.user_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(readUser(rs));
        }
        return users;
    }

    public User findById(int id) throws SQLException {
        try (Connection con = openConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_USER + "WHERE u.user_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readUser(rs) : null; }
        }
    }

    // Giữ method này để phần đăng nhập tiếp tục sử dụng.
    public User findByEmail(String email) throws SQLException {
        try (Connection con = openConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_USER + "WHERE u.email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readUser(rs) : null; }
        }
    }

    public List<Role> findRoles() throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT role_id, role_name, description, status FROM dbo.[Role] ORDER BY role_id";
        try (Connection con = openConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) roles.add(new Role(rs.getInt("role_id"), rs.getString("role_name"),
                    rs.getString("description"), rs.getString("status")));
        }
        return roles;
    }

    public List<Cinema> findCinemas() throws SQLException {
        List<Cinema> cinemas = new ArrayList<>();
        String sql = "SELECT cinema_id, cinema_name, address, phone, status FROM dbo.Cinema ORDER BY cinema_name";
        try (Connection con = openConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cinemas.add(new Cinema(rs.getInt("cinema_id"), rs.getString("cinema_name"),
                    rs.getString("address"), rs.getString("phone"), rs.getString("status")));
        }
        return cinemas;
    }

    public void save(User user, String roleName) throws SQLException {
        try (Connection con = openConnection()) {
            con.setAutoCommit(false);
            int originalId = user.getUserId();
            try {
                if (user.getUserId() == 0) insert(con, user);
                else update(con, user);
                saveProfile(con, user, roleName);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                user.setUserId(originalId);
                throw ex;
            }
        }
    }

    private void insert(Connection con, User user) throws SQLException {
        String sql = "INSERT INTO dbo.[User] (full_name, email, phone, role_id, status, password_hash) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFields(ps, user);
            ps.setString(6, user.getPasswordHash());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Cannot get new user ID.");
                user.setUserId(keys.getInt(1));
            }
        }
    }

    private void update(Connection con, User user) throws SQLException {
        // Không nhập mật khẩu mới thì giữ nguyên password_hash trong DB.
        String sql = "UPDATE dbo.[User] SET full_name=?, email=?, phone=?, role_id=?, status=?, "
                + "updated_at=SYSDATETIME()";
        if (user.getPasswordHash() != null) sql += ", password_hash=?";
        sql += " WHERE user_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            setFields(ps, user);
            int index = 6;
            if (user.getPasswordHash() != null) ps.setString(index++, user.getPasswordHash());
            ps.setInt(index, user.getUserId());
            if (ps.executeUpdate() != 1) throw new SQLException("User no longer exists.");
        }
    }

    private void setFields(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getFullName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPhone().isEmpty() ? null : user.getPhone());
        ps.setInt(4, user.getRoleId());
        ps.setString(5, user.getStatus());
    }

    private void saveProfile(Connection con, User user, String roleName) throws SQLException {
        if ("CUSTOMER".equals(roleName)) {
            try (PreparedStatement ps = con.prepareStatement(
                    "IF NOT EXISTS (SELECT 1 FROM dbo.Customer WHERE customer_id=?) "
                    + "INSERT INTO dbo.Customer (customer_id) VALUES (?)")) {
                ps.setInt(1, user.getUserId());
                ps.setInt(2, user.getUserId());
                ps.executeUpdate();
            }
        } else if ("STAFF".equals(roleName) || "CINEMA_MANAGER".equals(roleName)) {
            String sql = "UPDATE dbo.Staff SET cinema_id=? WHERE staff_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, user.getCinemaId());
                ps.setInt(2, user.getUserId());
                if (ps.executeUpdate() == 0) {
                    try (PreparedStatement add = con.prepareStatement(
                            "INSERT INTO dbo.Staff (staff_id, cinema_id) VALUES (?, ?)")) {
                        add.setInt(1, user.getUserId());
                        add.setInt(2, user.getCinemaId());
                        add.executeUpdate();
                    }
                }
            }
        }
        // Không DELETE Customer/Staff khi đổi vai trò: Booking/Food_Order đang tham chiếu.
    }

    public boolean deactivate(int id) throws SQLException {
        String sql = "UPDATE dbo.[User] SET status='INACTIVE', updated_at=SYSDATETIME() WHERE user_id=?";
        try (Connection con = openConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
