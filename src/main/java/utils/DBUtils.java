package utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtils {
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Properties config = new Properties();
        try (InputStream input = DBUtils.class.getResourceAsStream("/db.local.properties")) {
            if (input == null) {
                throw new SQLException("Missing db.local.properties. Copy src/main/resources/db.example.properties to db.local.properties and configure your local database, then rebuild.");
            }
            config.load(input);
        } catch (IOException ex) {
            throw new SQLException("Cannot read local database configuration.", ex);
        }
        String url = config.getProperty("db.url");
        String user = config.getProperty("db.user");
        String password = config.getProperty("db.password");
        if (url == null || url.trim().isEmpty() || user == null || user.trim().isEmpty()
                || password == null || "CHANGE_ME".equals(password)) {
            throw new SQLException("Configure db.url, db.user and db.password in db.local.properties, then rebuild.");
        }
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, user, password);
    }
}
