package config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/personal_finance";
    private static final String USER = "root";
    private static final String PASS = "";

    /**
     * Establishes a secure connection to the database.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Ensure the JAR is linked in IntelliJ.", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}