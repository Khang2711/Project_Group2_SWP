package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides database connection for the FCinema application.
 *
 * @author Kiet
 */
public class DBContext {

    protected Connection conn = null;

    /**
     * Creates a database connection.
     */
    public DBContext() {

        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String dbURL = "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=FCinemaDB;"
                    + "user=sa;"
                    + "password=12345;"
                    + "encrypt=true;trustServerCertificate=true;";

            conn = DriverManager.getConnection(dbURL);

        } catch (SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Connection getConnection() {
    return conn;
}
}
