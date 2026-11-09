package model;

/**
 * Customer Model Class
 * Represents customer profile details linked to a User account.
 * Demonstrates: OOP Encapsulation.
 */
public class Customer {
    private int customerId;
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String username; // Username from linked user account

    // Default constructor
    public Customer() {
    }

    // Parameterized constructor
    public Customer(int customerId, int userId, String fullName, String email, String phone, String address) {
        this.customerId = customerId;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Constructor without customerId and userId (used when filling registration form)
    public Customer(String fullName, String email, String phone, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
