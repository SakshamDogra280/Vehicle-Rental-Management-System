package ui;

import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * PaymentDialog Class
 * Simulates a secure, professional multi-channel payment gateway (UPI, Card, Cash).
 * Final Phase: Enables realistic payment processing, transaction ID generation, and payment status tracking.
 * Demonstrates: JDialog modality, CardLayout, Input validation, Event-Driven Programming.
 */
public class PaymentDialog extends JDialog {

    public static class PaymentResult {
        private final boolean paid;
        private final String paymentMethod;
        private final String paymentStatus;
        private final String transactionId;

        public PaymentResult(boolean paid, String paymentMethod, String paymentStatus, String transactionId) {
            this.paid = paid;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.transactionId = transactionId;
        }

        public boolean isPaid() {
            return paid;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public String getTransactionId() {
            return transactionId;
        }
    }

    private final double totalAmount;
    private final String vehicleName;
    private final int rentalDays;
    private PaymentResult result = null;

    // Card inputs
    private JTextField txtCardNumber;
    private JTextField txtCardHolder;
    private JTextField txtCardExpiry;
    private JPasswordField txtCardCvv;

    // UPI inputs
    private JTextField txtUpiId;

    // Radio buttons
    private JRadioButton rbUpi;
    private JRadioButton rbCard;
    private JRadioButton rbCash;

    private JPanel methodCardsPanel;
    private CardLayout cardLayout;

    public PaymentDialog(JFrame parent, String vehicleName, int rentalDays, double totalAmount) {
        super(parent, "DriveEasy - Secure Payment Gateway", true);
        this.vehicleName = vehicleName;
        this.rentalDays = rentalDays;
        this.totalAmount = totalAmount;

        setSize(520, 560);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(new Color(248, 250, 252));

        // 1. Header with Amount Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("DriveEasy Payment Checkout");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblAmount = new JLabel("Total: ₹" + String.format("%.2f", totalAmount));
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAmount.setForeground(new Color(74, 222, 128)); // Bright Green

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblAmount, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content: Order Summary + Payment Method Switcher
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Rental Summary Box
        JPanel summaryBox = new JPanel(new GridLayout(2, 2, 8, 4));
        summaryBox.setBackground(Color.WHITE);
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        summaryBox.add(new JLabel("Vehicle: " + vehicleName));
        summaryBox.add(new JLabel("Rental Duration: " + rentalDays + " day(s)"));
        summaryBox.add(new JLabel("Billing Currency: INR (₹)"));
        summaryBox.add(new JLabel("Status: Payment Due"));
        centerPanel.add(summaryBox, BorderLayout.NORTH);

        // Payment Method Radio Buttons
        JPanel methodSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        methodSelectPanel.setOpaque(false);
        methodSelectPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Choose Payment Method ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12)
        ));

        rbUpi = new JRadioButton("UPI (GPay / PhonePe)", true);
        rbCard = new JRadioButton("Credit / Debit Card");
        rbCash = new JRadioButton("Pay on Pickup / Cash");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbUpi);
        bg.add(rbCard);
        bg.add(rbCash);

        methodSelectPanel.add(rbUpi);
        methodSelectPanel.add(rbCard);
        methodSelectPanel.add(rbCash);

        // CardLayout Panel switching between UPI, Card, and Cash views
        cardLayout = new CardLayout();
        methodCardsPanel = new JPanel(cardLayout);
        methodCardsPanel.setOpaque(false);

        methodCardsPanel.add(createUpiPanel(), "UPI");
        methodCardsPanel.add(createCardPanel(), "CARD");
        methodCardsPanel.add(createCashPanel(), "CASH");

        rbUpi.addActionListener(e -> cardLayout.show(methodCardsPanel, "UPI"));
        rbCard.addActionListener(e -> cardLayout.show(methodCardsPanel, "CARD"));
        rbCash.addActionListener(e -> cardLayout.show(methodCardsPanel, "CASH"));

        JPanel midWrap = new JPanel(new BorderLayout(5, 5));
        midWrap.setOpaque(false);
        midWrap.add(methodSelectPanel, BorderLayout.NORTH);
        midWrap.add(methodCardsPanel, BorderLayout.CENTER);

        centerPanel.add(midWrap, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionPanel.setOpaque(false);

        JButton btnCancel = new JButton("Cancel");
        UITheme.styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        JButton btnPay = new JButton("Pay ₹" + String.format("%.2f", totalAmount) + " & Confirm");
        UITheme.styleSuccessButton(btnPay);
        btnPay.addActionListener(e -> handlePayment());

        actionPanel.add(btnCancel);
        actionPanel.add(btnPay);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createUpiPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblUpiHint = new JLabel("<html><b>Scan QR or enter UPI Virtual Payment Address (VPA):</b></html>");
        p.add(lblUpiHint, BorderLayout.NORTH);

        // Simulated QR Code Box
        JPanel qrBox = new JPanel(new GridBagLayout());
        qrBox.setPreferredSize(new Dimension(140, 110));
        qrBox.setBackground(new Color(241, 245, 249));
        qrBox.setBorder(BorderFactory.createDashedBorder(new Color(100, 116, 139), 2, 2));

        JLabel lblQr = new JLabel("<html><center><font size='5'>📱</font><br><b>[ UPI QR CODE ]</b><br><font color='#2563eb'>driveeasy@icici</font></center></html>");
        qrBox.add(lblQr);
        p.add(qrBox, BorderLayout.WEST);

        // UPI ID Input
        JPanel upiInputPanel = new JPanel(new GridBagLayout());
        upiInputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        upiInputPanel.add(new JLabel("Enter UPI ID (e.g. mobile@upi):"), gbc);

        gbc.gridy = 1;
        txtUpiId = new JTextField("customer@oksbi");
        txtUpiId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        upiInputPanel.add(txtUpiId, gbc);

        gbc.gridy = 2;
        JLabel lblSecure = new JLabel("<html><font color='#16a34a'>🔒 Verified Merchant: DriveEasy Rentals Pvt Ltd</font></html>");
        lblSecure.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        upiInputPanel.add(lblSecure, gbc);

        p.add(upiInputPanel, BorderLayout.CENTER);
        return p;
    }

    private JPanel createCardPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 14, 10, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCardNumber = new JTextField("4532 8901 2345 6789");
        txtCardHolder = new JTextField("Saksham Dogra");
        txtCardExpiry = new JTextField("12/28");
        txtCardCvv = new JPasswordField("892");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        p.add(new JLabel("Card Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7; gbc.gridwidth = 3;
        p.add(txtCardNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        p.add(new JLabel("Cardholder Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3;
        p.add(txtCardHolder, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        p.add(new JLabel("Expiry (MM/YY):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1;
        p.add(txtCardExpiry, gbc);

        gbc.gridx = 2; gbc.gridy = 2;
        p.add(new JLabel("CVV:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2;
        p.add(txtCardCvv, gbc);

        return p;
    }

    private JPanel createCashPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblCashInfo = new JLabel("<html>" +
                "<h3>💵 Cash on Delivery / Counter Pickup</h3>" +
                "<p style='color:#475569;'>You can pay the full rental amount of <b>₹" + String.format("%.2f", totalAmount) +
                "</b> in cash at our counter when picking up your vehicle keys.</p>" +
                "<p style='color:#16a34a; margin-top:8px;'>✓ No online prepayment required.<br>✓ Receipt will be stamped upon key handover.</p>" +
                "</html>");
        p.add(lblCashInfo, BorderLayout.CENTER);
        return p;
    }

    private void handlePayment() {
        String method;
        String status = "PAID";
        String txnId = "TXN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        if (rbUpi.isSelected()) {
            method = "UPI";
            String upi = txtUpiId.getText().trim();
            if (upi.isEmpty() || !upi.contains("@")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid UPI ID (e.g. mobile@upi).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else if (rbCard.isSelected()) {
            method = "Debit/Credit Card";
            String card = txtCardNumber.getText().replaceAll("\\s+", "");
            if (card.length() < 12) {
                JOptionPane.showMessageDialog(this, "Please enter a valid 16-digit card number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtCardHolder.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter cardholder name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            method = "Cash on Pickup";
            status = "PENDING"; // Paid on counter
            txnId = "CASH-COUNTER-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        // Show simulated authorization message
        JOptionPane.showMessageDialog(this,
                "✓ Payment Authorization Successful!\n\n" +
                "Method: " + method + "\n" +
                "Transaction ID: " + txnId + "\n" +
                "Amount Paid / Authorized: ₹" + String.format("%.2f", totalAmount) + "\n\n" +
                "Your booking will now be confirmed.",
                "Payment Success", JOptionPane.INFORMATION_MESSAGE);

        result = new PaymentResult(true, method, status, txnId);
        dispose();
    }

    public PaymentResult getResult() {
        return result;
    }
}
