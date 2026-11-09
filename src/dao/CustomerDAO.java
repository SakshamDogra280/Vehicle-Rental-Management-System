package dao;

import model.Customer;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerDAO Class
 * Handles database operations for customer records.
 * Demonstrates: PreparedStatement, ResultSet mapping, Java Collections (List).
 */
public class CustomerDAO {

    /**
     * Retrieves customer details associated with a specific user ID.
     */
    public Customer getCustomerByUserId(int userId) {
        String query = "SELECT customer_id, user_id, full_name, email, phone, address FROM customers WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves customer details by customer ID.
     */
    public Customer getCustomerById(int customerId) {
        String query = "SELECT customer_id, user_id, full_name, email, phone, address FROM customers WHERE customer_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all registered customers for Admin view.
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customerList = new ArrayList<>();
        String query = "SELECT c.customer_id, c.user_id, c.full_name, c.email, c.phone, c.address, u.username " +
                       "FROM customers c " +
                       "JOIN users u ON c.user_id = u.user_id " +
                       "ORDER BY c.customer_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer customer = extractCustomerFromResultSet(rs);
                customer.setUsername(rs.getString("username"));
                customerList.add(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customerList;
    }

    // Helper method to map ResultSet to Customer model object
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setUserId(rs.getInt("user_id"));
        customer.setFullName(rs.getString("full_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        return customer;
    }
}
