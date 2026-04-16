package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a factory method for acquiring MySQL database connections.
 * Connection parameters (URL, user, password) are configured as
 * compile-time constants — update them to match your local MySQL setup.
 */
public class DBConnection {
    // Database credentials - Update these as per your local setup
    private static final String URL = "jdbc:mysql://localhost:3306/student_attendance_db";
    private static final String USER = "root"; 
    private static final String PASSWORD = "4321"; // Replace with your MySQL password

    // Static block to load the driver class
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found! Include the library in your classpath.");
            e.printStackTrace();
        }
    }

    /**
     * Resets/Establishes a connection to the database.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }
}
