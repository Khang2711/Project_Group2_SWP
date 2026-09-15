package utils;
import java.sql.*;
public final class DBUtils {
 public static String config(String name,String fallback) {String v=System.getProperty(name);if(v==null||v.isBlank())v=System.getenv(name);return v==null||v.isBlank()?fallback:v;}
 public static Connection getConnection() throws SQLException,ClassNotFoundException {
  Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
  String password="123456";if(password==null)throw new SQLException("Thiếu FCINEMA_DB_PASSWORD.");
  return DriverManager.getConnection(config("FCINEMA_DB_URL","jdbc:sqlserver://localhost:1433;databaseName=FCinemaDB;encrypt=true;trustServerCertificate=true"),config("FCINEMA_DB_USER","sa"),password);
 }
}
