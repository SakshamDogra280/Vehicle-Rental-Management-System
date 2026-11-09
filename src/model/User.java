package model;

/**
 * User Model Class
 * Represents a user account (Admin or Customer).
 * Demonstrates: OOP Encapsulation with private fields, constructors, getters and setters.
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String role; // 'ADMIN' or 'CUSTOMER'

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(int userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Constructor without userId (useful for new user registration)
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
