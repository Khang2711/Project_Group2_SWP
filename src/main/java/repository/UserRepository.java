package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.User;

public class UserRepository {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT u.user_id, u.role_id, u.full_name, "
                + "u.email, u.phone, u.password_hash, u.avatar_url, "
                + "u.status, u.created_at, u.updated_at, "
                + "s.cinema_id AS staff_cinema_id "
                + "FROM dbo.[User] u "
                + "LEFT JOIN dbo.Staff s ON u.user_id = s.staff_id "
                + "WHERE u.email = ?";

        try (Connection connection = new DBContext().getConnection()) {
            if (connection == null) {
                throw new SQLException("Cannot connect to FCinemaDB.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, email);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }

                    User user = new User();

                    user.setUserId(resultSet.getInt("user_id"));
                    user.setRoleId(resultSet.getInt("role_id"));
                    user.setFullName(resultSet.getString("full_name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPhone(resultSet.getString("phone"));
                    user.setPasswordHash(
                            resultSet.getString("password_hash")
                    );
                    user.setAvatarUrl(resultSet.getString("avatar_url"));
                    user.setStatus(resultSet.getString("status"));
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
            }
        }
    }
}