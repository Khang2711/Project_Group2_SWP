package util;

import db.DBContext;
import java.sql.Connection;
import java.sql.SQLException;

/** Chạy main trong IDE để kiểm tra DB; không tạo bảng hay ghi dữ liệu. */
public class DBConnectionCheck {
    public static void main(String[] args) {
        try (Connection connection = new DBContext().getConnection()) {
            System.out.println("Ket noi thanh cong: " + connection.getCatalog());
        } catch (SQLException ex) {
            System.err.println("Khong ket noi duoc SQL Server: " + ex.getMessage());
        }
    }
}
