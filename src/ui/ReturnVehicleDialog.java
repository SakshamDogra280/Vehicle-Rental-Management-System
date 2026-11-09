package ui;

import dao.BookingDAO;
import model.Booking;
import util.UITheme;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * ReturnVehicleDialog Class
 * Handles vehicle return workflow, overdue penalty calculation, and inventory release.
 * Final Phase: Implements the Vehicle Return & Late-Fee Penalty Engine.
 * Demonstrates: Date arithmetic (ChronoUnit), Business logic computation, Dynamic GUI updates.
 */
public class ReturnVehicleDialog extends JDialog {

    private final Booking booking;
    private final BookingDAO bookingDAO;
    private boolean returnedSuccess = false;

    private JTextField txtActualReturnDate;
    private JLabel lblStatusNotice;
    private JLabel lblLateFeeAmount;
    private JLabel lblTotalPayable;
    private double calculatedLateFee = 0.0;

    // Standard daily late fee rate
    public static final double DAILY_LATE_FEE_RATE = 500.00;

    public ReturnVehicleDialog(JFrame parent, Booking booking) {
        this(parent, booking, new BookingDAO());
    }

    public ReturnVehicleDialog(JFrame parent, Booking booking, BookingDAO bookingDAO) {
        super(parent, "DriveEasy - Vehicle Return & Inspection", true);
        this.booking = booking;
        this.bookingDAO = bookingDAO;

        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents();
        calculatePenalty();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(new Color(248, 250, 252));

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("Vehicle Return Processing");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblId = new JLabel("Booking #" + booking.getBookingId());
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblId.setForeground(new Color(148, 163, 184));

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblId, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content: Summary Info & Return Date Input
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Booking details card
        JPanel infoCard = new JPanel(new GridLayout(4, 2, 8, 6));
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 14, 12, 14)
        ));

        infoCard.add(new JLabel("Vehicle:"));
        infoCard.add(new JLabel("<html><b>" + booking.getVehicleName() + " (" + booking.getVehicleNumber() + ")</b></html>"));

        infoCard.add(new JLabel("Rental Schedule:"));
        infoCard.add(new JLabel(booking.getStartDate() + " to " + booking.getEndDate()));

        infoCard.add(new JLabel("Scheduled Duration:"));
        infoCard.add(new JLabel(booking.getTotalDays() + " day(s)"));

        infoCard.add(new JLabel("Base Fare Paid:"));
        infoCard.add(new JLabel("₹" + String.format("%.2f", booking.getTotalAmount())));

        centerPanel.add(infoCard, BorderLayout.NORTH);

        // Return Input & Penalty Box
        JPanel penaltyBox = new JPanel(new GridBagLayout());
        penaltyBox.setBackground(Color.WHITE);
        penaltyBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 14, 12, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.45;
        penaltyBox.add(new JLabel("Actual Return Date:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.55;
        txtActualReturnDate = new JTextField(LocalDate.now().toString());
        txtActualReturnDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        penaltyBox.add(txtActualReturnDate, gbc);

        // Recalculate button
        gbc.gridx = 1; gbc.gridy = 1;
        JButton btnCheckDate = new JButton("Recalculate Penalty");
        UITheme.styleSecondaryButton(btnCheckDate);
        btnCheckDate.addActionListener(e -> calculatePenalty());
        penaltyBox.add(btnCheckDate, gbc);

        // Notice Badge
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        lblStatusNotice = new JLabel("Checking return schedule...", SwingConstants.CENTER);
        lblStatusNotice.setOpaque(true);
        lblStatusNotice.setBorder(new EmptyBorder(6, 8, 6, 8));
        lblStatusNotice.setFont(new Font("Segoe UI", Font.BOLD, 12));
        penaltyBox.add(lblStatusNotice, gbc);

        // Late fee and total
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        penaltyBox.add(new JLabel("Late Return Fee (₹500/day):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        lblLateFeeAmount = new JLabel("₹0.00");
        lblLateFeeAmount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        penaltyBox.add(lblLateFeeAmount, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        penaltyBox.add(new JLabel("Final Total Amount:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        lblTotalPayable = new JLabel("₹" + String.format("%.2f", booking.getTotalAmount()));
        lblTotalPayable.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalPayable.setForeground(new Color(22, 163, 74));
        penaltyBox.add(lblTotalPayable, gbc);

        centerPanel.add(penaltyBox, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionPanel.setOpaque(false);

        JButton btnClose = new JButton("Cancel");
        UITheme.styleSecondaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());

        JButton btnConfirmReturn = new JButton("Confirm Return & Free Vehicle");
        UITheme.styleSuccessButton(btnConfirmReturn);
        btnConfirmReturn.addActionListener(e -> handleConfirmReturn());

        actionPanel.add(btnClose);
        actionPanel.add(btnConfirmReturn);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void calculatePenalty() {
        String dateStr = txtActualReturnDate.getText().trim();
        LocalDate returnDate = ValidationUtil.parseDate(dateStr);

        if (returnDate == null) {
            lblStatusNotice.setText("⚠️ Invalid date format. Use YYYY-MM-DD.");
            lblStatusNotice.setBackground(new Color(254, 226, 226));
            lblStatusNotice.setForeground(new Color(185, 28, 28));
            return;
        }

        if (returnDate.isBefore(booking.getStartDate())) {
            lblStatusNotice.setText("⚠️ Return date cannot be before rental start date.");
            lblStatusNotice.setBackground(new Color(254, 226, 226));
            lblStatusNotice.setForeground(new Color(185, 28, 28));
            return;
        }

        LocalDate scheduledEnd = booking.getEndDate();
        if (returnDate.isAfter(scheduledEnd)) {
            long overdueDays = ChronoUnit.DAYS.between(scheduledEnd, returnDate);
            calculatedLateFee = overdueDays * DAILY_LATE_FEE_RATE;

            lblStatusNotice.setText("⚠️ OVERDUE by " + overdueDays + " day(s)! Late Penalty applied.");
            lblStatusNotice.setBackground(new Color(254, 226, 226));
            lblStatusNotice.setForeground(new Color(185, 28, 28));

            lblLateFeeAmount.setText("₹" + String.format("%.2f", calculatedLateFee) + " (" + overdueDays + "d × ₹500)");
            lblLateFeeAmount.setForeground(new Color(220, 38, 38));
        } else {
            calculatedLateFee = 0.0;
            lblStatusNotice.setText("✓ On-Time Return (No Late Penalty)");
            lblStatusNotice.setBackground(new Color(220, 252, 231));
            lblStatusNotice.setForeground(new Color(21, 128, 61));

            lblLateFeeAmount.setText("₹0.00");
            lblLateFeeAmount.setForeground(new Color(71, 85, 105));
        }

        double finalTotal = booking.getTotalAmount() + calculatedLateFee;
        lblTotalPayable.setText("₹" + String.format("%.2f", finalTotal));
    }

    private void handleConfirmReturn() {
        LocalDate returnDate = ValidationUtil.parseDate(txtActualReturnDate.getText().trim());
        if (returnDate == null) {
            JOptionPane.showMessageDialog(this, "Please enter a valid return date (YYYY-MM-DD).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        calculatePenalty();

        String confirmPrompt = "Confirm Vehicle Return Summary:\n\n" +
                "Vehicle: " + booking.getVehicleName() + "\n" +
                "Actual Return Date: " + returnDate + "\n" +
                "Base Rental: ₹" + String.format("%.2f", booking.getTotalAmount()) + "\n" +
                "Late Fee Penalty: ₹" + String.format("%.2f", calculatedLateFee) + "\n" +
                "Grand Total: ₹" + String.format("%.2f", (booking.getTotalAmount() + calculatedLateFee)) + "\n\n" +
                "Vehicle will now be marked AVAILABLE in fleet inventory.\nProceed?";

        int choice = JOptionPane.showConfirmDialog(this, confirmPrompt, "Confirm Return", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = bookingDAO.returnVehicle(booking.getBookingId(), booking.getVehicleId(), returnDate, calculatedLateFee);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "🎉 Vehicle Return Processed Successfully!\n\n" +
                    "Booking #" + booking.getBookingId() + " status is now RETURNED.\n" +
                    "Vehicle is now AVAILABLE for new customers.",
                    "Return Completed", JOptionPane.INFORMATION_MESSAGE);
            returnedSuccess = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to process return due to database error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isReturnedSuccess() {
        return returnedSuccess;
    }

    public boolean isReturnSuccessful() {
        return returnedSuccess;
    }
}
