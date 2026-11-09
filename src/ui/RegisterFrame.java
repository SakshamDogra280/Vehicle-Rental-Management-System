package ui;

import dao.UserDAO;
import model.Customer;
import model.User;
import util.UITheme;
import util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * RegisterFrame Class
 * Provides the graphical interface for new customer registration.
 * Demonstrates: Java Swing (JFrame, JPanel, GridBagLayout), Event Handling (ActionListener), Form Validation.
 */
public class RegisterFrame extends JFrame {

    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegister;
    private JButton btnBackToLogin;

    private final UserDAO userDAO;
    private final JFrame parentLoginFrame;

    public RegisterFrame(JFrame parentLoginFrame) {
        this.parentLoginFrame = parentLoginFrame;
        this.userDAO = new UserDAO();

        setTitle("Vehicle Rental - Customer Registration");
        setSize(480, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        JLabel lblTitle = new JLabel("Customer Registration", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(33, 37, 41));

        JLabel lblSubtitle = new JLabel("Create an account to rent vehicles", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(Color.GRAY);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtFullName = new JTextField(18);
        txtEmail = new JTextField(18);
        txtPhone = new JTextField(18);
        txtAddress = new JTextField(18);
        txtUsername = new JTextField(18);
        txtPassword = new JPasswordField(18);
        txtConfirmPassword = new JPasswordField(18);

        // Helper to add form rows
        addFormRow(formPanel, gbc, 0, "Full Name:", txtFullName);
        addFormRow(formPanel, gbc, 1, "Email:", txtEmail);
        addFormRow(formPanel, gbc, 2, "Phone (10 digits):", txtPhone);
        addFormRow(formPanel, gbc, 3, "City / Address:", txtAddress);
        addFormRow(formPanel, gbc, 4, "Choose Username:", txtUsername);
        addFormRow(formPanel, gbc, 5, "Password:", txtPassword);
        addFormRow(formPanel, gbc, 6, "Confirm Password:", txtConfirmPassword);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnRegister = new JButton("Register Now");
        UITheme.styleSuccessButton(btnRegister);

        btnBackToLogin = new JButton("Back to Login");
        UITheme.styleSecondaryButton(btnBackToLogin);

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBackToLogin);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event Handling
        btnRegister.addActionListener(e -> handleRegistration());

        btnBackToLogin.addActionListener(e -> {
            dispose();
            if (parentLoginFrame != null) {
                parentLoginFrame.setVisible(true);
            }
        });
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void handleRegistration() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();

        // 1. Empty field checks
        if (ValidationUtil.isEmpty(fullName) || ValidationUtil.isEmpty(email) ||
            ValidationUtil.isEmpty(phone) || ValidationUtil.isEmpty(username) ||
            ValidationUtil.isEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Email validation
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address (e.g., student@example.com).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Phone validation
        if (!ValidationUtil.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit phone number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4. Password matching check
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match. Please re-enter.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 5. Uniqueness checks in database
        if (userDAO.isUsernameTaken(username)) {
            JOptionPane.showMessageDialog(this, "Username '" + username + "' is already taken. Please choose another.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userDAO.isEmailTaken(email)) {
            JOptionPane.showMessageDialog(this, "Email '" + email + "' is already registered. Please login or use another email.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 6. Save customer in database
        User newUser = new User(username, password, "CUSTOMER");
        Customer newCustomer = new Customer(fullName, email, phone, address);

        boolean isSuccess = userDAO.registerCustomer(newUser, newCustomer);
        if (isSuccess) {
            JOptionPane.showMessageDialog(this, "Registration Successful!\nYou can now login with your credentials.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (parentLoginFrame != null) {
                parentLoginFrame.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to register customer due to a database error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
