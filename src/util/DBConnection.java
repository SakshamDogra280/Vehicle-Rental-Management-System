package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection Utility Class
 * Manages database connection with MySQL for the Vehicle Rental Management System.
 * BCA 5th Semester PBL Project.
 */
public class DBConnection {

    // Database connection parameters
    // NOTE: Change USER or PASSWORD below if your MySQL setup has different credentials
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/vehicle_rental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default is empty "" for XAMPP (change if you set a password)

    // Static block to register MySQL JDBC Driver once when the class loads
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Please add mysql-connector-j jar to the classpath.");
            e.printStackTrace();
        }
    }

    /**
     * Obtains a new database connection.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Test method to verify database connectivity independently.
     */
    public static void main(String[] args) {
        System.out.println("Testing MySQL Database Connection...");
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS: Connected to MySQL database [vehicle_rental_db] successfully!");
            }
        } catch (SQLException e) {
            System.err.println("FAILED: Could not connect to MySQL database.");
            System.err.println("Error details: " + e.getMessage());
            System.err.println("\nChecklist:");
            System.err.println("1. Is MySQL server running?");
            System.err.println("2. Does the database 'vehicle_rental_db' exist? (Run vehicle_rental.sql)");
            System.err.println("3. Are root username and password correct in DBConnection.java?");
        }
    }
}
