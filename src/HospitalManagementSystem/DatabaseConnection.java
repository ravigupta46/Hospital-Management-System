package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Responsibility of DatabaseConnection:
 *
 * This class is solely responsible for managing database credentials
 * (URL, username, password) and providing a database connection.
 *
 * It follows the Single Responsibility Principle (SRP):
 * HospitalManagementSystem handles the user menu and flow, while
 * DatabaseConnection handles connecting to MySQL.
 */
public class DatabaseConnection {

    // Database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/hospital";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Admin@123";

    // Load MySQL JDBC Driver once when the class is loaded
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Private constructor to prevent creating instances of this utility class
    private DatabaseConnection() {
    }

    /**
     * Creates and returns a java.sql.Connection object to the MySQL database.
     *
     * @return Connection object to the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}