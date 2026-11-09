package dao;

import model.Customer;
import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * UserDAO Class
 * Handles database operations for User authentication and customer registration.
 * Demonstrates: JDBC PreparedStatement, SQL Transactions, Exception Handling.
 */
public class UserDAO {

    /**
     * Authenticates a user with username and password.
     * @param username user login name
     * @param password user login password
     * @return User object if credentials are correct, null otherwise
     */
    public User authenticate(String username, String password) {
        String query = "SELECT user_id, username, password, role FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks if a username is already registered.
     */
    public boolean isUsernameTaken(String username) {
        String query = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Checks if an email is already registered in customers table.
     */
    public boolean isEmailTaken(String email) {
        String query = "SELECT customer_id FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Registers a new customer using a database transaction.
     * Inserts into 'users' first, gets generated user_id, then inserts into 'customers'.
     * @return true if both inserts succeed, false otherwise
     */
    public boolean registerCustomer(User user, Customer customer) {
        String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'CUSTOMER')";
        String insertCustomerSql = "INSERT INTO customers (user_id, full_name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // Start transaction
            conn.setAutoCommit(false);

            // Step 1: Insert user
            int generatedUserId;
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, user.getUsername());
                userStmt.setString(2, user.getPassword());
                userStmt.executeUpdate();

                try (ResultSet rs = userStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedUserId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Step 2: Insert customer details
            try (PreparedStatement custStmt = conn.prepareStatement(insertCustomerSql)) {
                custStmt.setInt(1, generatedUserId);
                custStmt.setString(2, customer.getFullName());
                custStmt.setString(3, customer.getEmail());
                custStmt.setString(4, customer.getPhone());
                custStmt.setString(5, customer.getAddress());
                custStmt.executeUpdate();
            }

            // Commit transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Transaction failed during customer registration: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }
}
