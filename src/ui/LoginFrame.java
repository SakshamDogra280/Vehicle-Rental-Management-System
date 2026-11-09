package ui;

import dao.CustomerDAO;
import dao.UserDAO;
import model.Customer;
import model.User;
import util.ImageUtil;
import util.UITheme;
import util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * LoginFrame Class
 * Main login window for both Admin and Customer users.
 * Demonstrates: Java Swing GUI, ActionListeners, User Authentication with MySQL.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;

    private final UserDAO userDAO;
    private final CustomerDAO customerDAO;

    public LoginFrame() {
        this.userDAO = new UserDAO();
        this.customerDAO = new CustomerDAO();

        setTitle("DriveEasy - Vehicle Rental Management System");
        setSize(460, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Header Panel with Title and DriveEasy Brand Logo
        JPanel headerPanel = new JPanel(new BorderLayout(0, 6));

        JLabel lblTitle = new JLabel("Vehicle Rental System", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(24, 43, 73));
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        // DriveEasy Official Application Logo (1:1 aspect ratio)
        ImageIcon logoIcon = ImageUtil.getAppLogo(170, 170);
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon, SwingConstants.CENTER);
            lblLogo.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            headerPanel.add(lblLogo, BorderLayout.CENTER);
        }

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Add to GridBag
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(lblUsername, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formPanel.add(lblPassword, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Bottom Actions Panel
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 8));

        btnLogin = new JButton("Login");
        UITheme.stylePrimaryButton(btnLogin);

        btnRegister = new JButton("New Customer? Register Here");
        UITheme.styleSecondaryButton(btnRegister);

        JLabel lblHint = new JLabel("Demo: admin/admin123 | rahul_sharma/rahul123", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(Color.GRAY);

        bottomPanel.add(btnLogin);
        bottomPanel.add(btnRegister);
        bottomPanel.add(lblHint);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Action Listeners
        btnLogin.addActionListener(e -> handleLogin());
        
        // Allow pressing Enter key to trigger login
        getRootPane().setDefaultButton(btnLogin);

        btnRegister.addActionListener(e -> {
            setVisible(false);
            new RegisterFrame(this).setVisible(true);
        });
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (ValidationUtil.isEmpty(username) || ValidationUtil.isEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Authentication Success: Navigate based on Role
        dispose(); // Close login window

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            JOptionPane.showMessageDialog(null, "Welcome, Admin " + user.getUsername() + "!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
            new AdminDashboard(user).setVisible(true);
        } else if ("CUSTOMER".equalsIgnoreCase(user.getRole())) {
            Customer customerProfile = customerDAO.getCustomerByUserId(user.getUserId());
            JOptionPane.showMessageDialog(null, "Welcome back, " + user.getUsername() + "!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
            new CustomerDashboard(user, customerProfile).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Unknown role assigned to user.", "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
