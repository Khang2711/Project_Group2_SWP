package repository;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Role;

public class RoleRepository {

    public Role findById(int roleId) throws SQLException {
        String sql = "SELECT role_id, role_name, description, status "
                + "FROM dbo.[Role] WHERE role_id = ?";

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, roleId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRole(resultSet);
            }
        }
    }

    public Role findByName(String roleName) throws SQLException {
        String sql = "SELECT role_id, role_name, description, status "
                + "FROM dbo.[Role] WHERE role_name = ?";

        try (Connection connection = new DBContext().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, roleName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRole(resultSet);
            }
        }
    }

    private Role mapRole(ResultSet resultSet) throws SQLException {
        Role role = new Role();

        role.setRoleId(resultSet.getInt("role_id"));
        role.setRoleName(resultSet.getString("role_name"));
        role.setDescription(resultSet.getString("description"));
        role.setStatus(resultSet.getString("status"));

        return role;
    }
}