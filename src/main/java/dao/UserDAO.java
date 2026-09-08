package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.User;
import utils.DBUtils;

public class UserDAO {

    private User mapUser(ResultSet rs) throws Exception {
        return new User(
                rs.getString("userID"),
                rs.getString("fullName"),
                rs.getString("password"),
                rs.getString("roleID")
        );
    }

    public User checkLogin(String userID, String password) throws Exception {
        String sql = "SELECT userID, fullName, password, roleID FROM tblUsers WHERE userID = ? AND password = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, userID);
            stm.setString(2, password);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User getUserByID(String userID) throws Exception {
        String sql = "SELECT userID, fullName, password, roleID FROM tblUsers WHERE userID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, userID);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public boolean isUserIDExists(String userID) throws Exception {
        String sql = "SELECT userID FROM tblUsers WHERE userID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, userID);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean createUser(User user) throws Exception {
        String sql = "INSERT INTO tblUsers(userID, fullName, password, roleID) VALUES(?, ?, ?, ?)";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, user.getUserID());
            stm.setString(2, user.getFullName());
            stm.setString(3, user.getPassword());
            stm.setString(4, user.getRoleID());

            return stm.executeUpdate() > 0;
        }
    }

    public boolean updateProfile(String userID, String fullName) throws Exception {
        String sql = "UPDATE tblUsers SET fullName = ? WHERE userID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, fullName);
            stm.setString(2, userID);
            return stm.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(String userID, String newPassword) throws Exception {
        String sql = "UPDATE tblUsers SET password = ? WHERE userID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, newPassword);
            stm.setString(2, userID);
            return stm.executeUpdate() > 0;
        }
    }
}
