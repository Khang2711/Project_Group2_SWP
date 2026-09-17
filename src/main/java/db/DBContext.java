package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Kết nối SQL Server theo file mẫu. Dùng cho môi trường học local. */
public class DBContext {
    private final String DB_URL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=FCinemaDB;encrypt=false";
    private final String DB_USER = "sa";
    private final String DB_PWD = "123456"; // Đổi theo SQL Server trên máy bạn.

    public Connection getConnection() throws SQLException {
        // Tạo kết nối khi DAO cần; không giữ kết nối dùng chung cho nhiều request.
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
    }
}
