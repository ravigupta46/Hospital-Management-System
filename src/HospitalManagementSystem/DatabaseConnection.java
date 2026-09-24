package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Responsibility of DatabaseConnection:
 *
 * 1. Centralized Connection Management:
 *    This class is solely responsible for reading database configuration
 *    (URL, Username, Password) and establishing a JDBC connection to MySQL.
 *
 * 2. Security Best Practice (Phase 4):
 *    Credentials are NOT hardcoded into source code. Instead, they are retrieved
 *    from environment variables (DB_URL, DB_USERNAME, DB_PASSWORD).
 *
 * 3. Single Responsibility Principle (SRP):
 *    Application logic (HospitalManagementSystem, Patient, Doctor, Appointment)
 *    does not deal with how connections are established or configured.
 */
public class DatabaseConnection {

    // Default configuration if environment variables are not set
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/hospital";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";

    // Load MySQL JDBC driver once during class loading
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[!] MySQL JDBC Driver not found. Please ensure mysql-connector-j is on your classpath.");
            e.printStackTrace();
        }
    }

    // Private constructor to prevent direct instantiation of this utility class
    private DatabaseConnection() {
    }

    /**
     * Resolves the database URL from environment variable or default.
     */
    private static String getUrl() {
        String envUrl = System.getenv("DB_URL");
        return (envUrl != null && !envUrl.trim().isEmpty()) ? envUrl.trim() : DEFAULT_URL;
    }

    /**
     * Resolves the database username from environment variable or default.
     */
    private static String getUsername() {
        String envUser = System.getenv("DB_USERNAME");
        return (envUser != null && !envUser.trim().isEmpty()) ? envUser.trim() : DEFAULT_USERNAME;
    }

    /**
     * Resolves the database password from environment variable or default.
     */
    private static String getPassword() {
        String envPass = System.getenv("DB_PASSWORD");
        return (envPass != null) ? envPass : DEFAULT_PASSWORD;
    }

    /**
     * Establishes and returns a new java.sql.Connection object to MySQL.
     *
     * @return java.sql.Connection to the MySQL database
     * @throws SQLException if a database connection error occurs
     */
    public static Connection getConnection() throws SQLException {
        String url = getUrl();
        String user = getUsername();
        String pass = getPassword();

        return DriverManager.getConnection(url, user, pass);
    }
}