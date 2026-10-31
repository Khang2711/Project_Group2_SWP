package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserRepository {

    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = "SELECT u.user_id, u.role_id, u.full_name, "
                + "u.email, u.phone, u.password_hash, u.avatar_url, "
                + "u.status, u.email_verified, u.created_at, u.updated_at, "
                + "s.cinema_id AS staff_cinema_id "
                + "FROM dbo.[User] u "
                + "LEFT JOIN dbo.Staff s ON u.user_id = s.staff_id "
                + "ORDER BY u.user_id";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }

        return users;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT u.user_id, u.role_id, u.full_name, "
                + "u.email, u.phone, u.password_hash, u.avatar_url, "
                + "u.status, u.email_verified, u.created_at, u.updated_at, "
                + "s.cinema_id AS staff_cinema_id "
                + "FROM dbo.[User] u "
                + "LEFT JOIN dbo.Staff s ON u.user_id = s.staff_id "
                + "WHERE u.user_id = ?";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapUser(resultSet);
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT u.user_id, u.role_id, u.full_name, "
                + "u.email, u.phone, u.password_hash, u.avatar_url, "
                + "u.status, u.email_verified, u.created_at, u.updated_at, "
                + "s.cinema_id AS staff_cinema_id "
                + "FROM dbo.[User] u "
                + "LEFT JOIN dbo.Staff s ON u.user_id = s.staff_id "
                + "WHERE u.email = ?";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapUser(resultSet);
            }
        }
    }

    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO dbo.[User] "
                + "(role_id, full_name, email, password_hash, phone, "
                + "avatar_url, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, user.getRoleId());
            statement.setString(2, user.getFullName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPasswordHash());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getAvatarUrl());
            statement.setString(7, user.getStatus());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Cannot get created user id.");
    }

    // Keep the account-management update method used by UserService.
    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE dbo.[User] "
                + "SET full_name = ?, email = ?, phone = ?, "
                + "updated_at = SYSDATETIME() "
                + "WHERE user_id = ?";

        try (Connection connection = new DBContext().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setInt(4, user.getUserId());

            return statement.executeUpdate() > 0;
        }
    }


    public boolean update(User user) throws SQLException {
        if (user.getPasswordHash() == null) {
            return updateWithoutPassword(user);
        }

        String sql = "UPDATE dbo.[User] "
                + "SET role_id = ?, full_name = ?, email = ?, phone = ?, "
                + "password_hash = ?, updated_at = SYSDATETIME() "
                + "WHERE user_id = ?";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, user.getRoleId());
            statement.setString(2, user.getFullName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getPasswordHash());
            statement.setInt(6, user.getUserId());

            return statement.executeUpdate() > 0;
        }
    }

    private boolean updateWithoutPassword(User user) throws SQLException {
        String sql = "UPDATE dbo.[User] "
                + "SET role_id = ?, full_name = ?, email = ?, phone = ?, "
                + "updated_at = SYSDATETIME() "
                + "WHERE user_id = ?";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, user.getRoleId());
            statement.setString(2, user.getFullName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setInt(5, user.getUserId());

            return statement.executeUpdate() > 0;
        }
    }

    public User findProfileById(int userId) throws SQLException {
        String sql = "SELECT u.*, NULL AS staff_cinema_id FROM dbo.[User] u "
                + "JOIN dbo.[Role] r ON u.role_id = r.role_id "
                + "WHERE u.user_id = ? AND u.status = 'ACTIVE' "
                + "AND u.email_verified = 1 AND r.role_name = 'CUSTOMER' "
                + "AND r.status = 'ACTIVE'";
        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                throw new SQLException("Cannot connect to FCinemaDB.");
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapUser(resultSet) : null;
                }
            }
        }
    }

    public boolean isProfileEmailUsed(String email, int userId) throws SQLException {
        String sql = "SELECT user_id FROM dbo.[User] WHERE email = ? AND user_id <> ?";
        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                throw new SQLException("Cannot connect to FCinemaDB.");
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                statement.setInt(2, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        }
    }

    // Update only the fields owned by Profile, in one atomic statement.
    public boolean updateProfile(User user, String originalEmail,
            String originalPasswordHash) throws SQLException {
        String sql = "UPDATE dbo.[User] "
                + "SET full_name = ?, phone = ?, avatar_url = ?, "
                + "password_hash = COALESCE(?, password_hash), "
                + "updated_at = SYSDATETIME() "
                + "WHERE user_id = ? AND email = ? AND password_hash = ? "
                + "AND role_id = ? AND status = 'ACTIVE' AND email_verified = 1";
        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                throw new SQLException("Cannot connect to FCinemaDB.");
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getFullName());
                statement.setString(2, user.getPhone());
                statement.setString(3, user.getAvatarUrl());
                statement.setString(4, user.getPasswordHash());
                statement.setInt(5, user.getUserId());
                statement.setString(6, originalEmail);
                statement.setString(7, originalPasswordHash);
                statement.setInt(8, user.getRoleId());
                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean updatePassword(int userId, String passwordHash)
            throws SQLException {

        String sql = "UPDATE dbo.[User] "
                + "SET password_hash = ?, updated_at = SYSDATETIME() "
                + "WHERE user_id = ?";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean markEmailVerified(int userId) throws SQLException {
        String sql = "UPDATE dbo.[User] "
                + "SET email_verified = 1, updated_at = SYSDATETIME() "
                + "WHERE user_id = ? AND status = 'ACTIVE'";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int userId, String status)
            throws SQLException {

        String sql = "UPDATE dbo.[User] "
                + "SET status = ?, updated_at = SYSDATETIME() "
                + "WHERE user_id = ?";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    public void saveStaffCinema(int userId, int cinemaId)
            throws SQLException {

        if (hasStaff(userId)) {
            String sql = "UPDATE dbo.Staff "
                    + "SET cinema_id = ? "
                    + "WHERE staff_id = ?";

            try (Connection connection = new DBContext().getConnection(); PreparedStatement statement
                    = connection.prepareStatement(sql)) {

                statement.setInt(1, cinemaId);
                statement.setInt(2, userId);
                statement.executeUpdate();
            }

            return;
        }

        String sql = "INSERT INTO dbo.Staff(staff_id, cinema_id) "
                + "VALUES (?, ?)";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, cinemaId);
            statement.executeUpdate();
        }
    }

    public void ensureCustomer(int userId) throws SQLException {
        if (hasCustomer(userId)) {
            return;
        }

        String sql = "INSERT INTO dbo.Customer(customer_id) VALUES (?)";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    private boolean hasStaff(int userId) throws SQLException {
        String sql = "SELECT staff_id FROM dbo.Staff WHERE staff_id = ?";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean hasCustomer(int userId) throws SQLException {
        String sql = "SELECT customer_id "
                + "FROM dbo.Customer "
                + "WHERE customer_id = ?";

        try (Connection connection = new DBContext().getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();

        user.setUserId(resultSet.getInt("user_id"));
        user.setRoleId(resultSet.getInt("role_id"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setAvatarUrl(resultSet.getString("avatar_url"));
        user.setStatus(resultSet.getString("status"));
        user.setEmailVerified(resultSet.getBoolean("email_verified"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        user.setUpdatedAt(resultSet.getTimestamp("updated_at"));

        int cinemaId = resultSet.getInt("staff_cinema_id");

        if (resultSet.wasNull()) {
            user.setCinemaId(null);
        } else {
            user.setCinemaId(cinemaId);
        }

        return user;
    }

    public void insertCustomer(User user) throws SQLException {
        String userSql = "INSERT INTO dbo.[User] "
                + "(role_id, full_name, email, password_hash, phone, status, email_verified) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String customerSql
                = "INSERT INTO dbo.Customer (customer_id) VALUES (?)";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                throw new SQLException("Cannot connect to FCinemaDB.");
            }

            connection.setAutoCommit(false);

            try {
                int userId;

                try (PreparedStatement statement
                        = connection.prepareStatement(
                                userSql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setInt(1, user.getRoleId());
                    statement.setString(2, user.getFullName());
                    statement.setString(3, user.getEmail());
                    statement.setString(4, user.getPasswordHash());
                    statement.setString(5, user.getPhone());
                    statement.setString(6, user.getStatus());
                    statement.setBoolean(7, user.isEmailVerified());

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Cannot insert user.");
                    }

                    try (ResultSet generatedKeys
                            = statement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {
                            throw new SQLException(
                                    "Cannot get the new user ID."
                            );
                        }

                        userId = generatedKeys.getInt(1);
                    }
                }

                try (PreparedStatement statement
                        = connection.prepareStatement(customerSql)) {

                    statement.setInt(1, userId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Cannot insert customer.");
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }
}
