package ui;

import dao.BookingDAO;
import dao.VehicleDAO;
import model.Booking;
import model.Customer;
import model.User;
import model.Vehicle;
import util.ImageUtil;
import util.UITheme;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * CustomerDashboard Frame
 * Complete dashboard for Customer in DriveEasy:
 * Tab 1: Book a Vehicle (Browse Available Fleet, Filter by Type, Live Search, Photo Preview, Payment Gateway, Atomic Booking)
 * Tab 2: My Booking History (Personal Ledger, Return Vehicle Engine with Overdue Penalty, Cancel Booking, Print/Export Invoice)
 * Final Phase: 100% complete feature set.
 * Demonstrates: Java Swing, Multi-card layouts, Modal Dialogs, Atomic DB transactions.
 */
public class CustomerDashboard extends JFrame {

    private final User customerUser;
    private final Customer customerProfile;
    private final VehicleDAO vehicleDAO;
    private final BookingDAO bookingDAO;

    // Available Vehicles Table & Search
    private JTable tableAvailableVehicles;
    private DefaultTableModel tableModelVehicles;
    private JTextField txtSearchVehicle;
    private JComboBox<String> comboFilterType;
    private JButton btnSearch;
    private JButton btnResetSearch;

    // Booking Form Components
    private JLabel lblVehicleImage;
    private JTextField txtSelectedVehicle;
    private JTextField txtDailyRate;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JLabel lblDaysCalculated;
    private JLabel lblTotalAmountCalculated;
    private JButton btnCalculateFare;
    private JButton btnConfirmBooking;
    private JButton btnClearBookingForm;

    // Booking History Components
    private JTable tableBookingHistory;
    private DefaultTableModel tableModelHistory;
    private JLabel lblHistorySummary;
    private JLabel lblHistoryVehicleImage;
    private JLabel lblHistoryDetailText;
    private JButton btnRefreshHistory;
    private JButton btnViewInvoice;
    private JButton btnReturnVehicle;
    private JButton btnCancelBooking;

    // Currently selected vehicle state
    private Vehicle currentSelectedVehicle = null;

    public CustomerDashboard(User customerUser, Customer customerProfile) {
        this.customerUser = customerUser;
        this.customerProfile = customerProfile;
        this.vehicleDAO = new VehicleDAO();
        this.bookingDAO = new BookingDAO();

        setTitle("DriveEasy - Customer Portal");
        setSize(1080, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadAvailableVehicles();
        loadBookingHistory();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 1. Top Navigation Bar
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(new Color(15, 76, 129));
        navPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        String customerName = (customerProfile != null && customerProfile.getFullName() != null)
                ? customerProfile.getFullName()
                : customerUser.getUsername();

        JLabel lblTitle = new JLabel("DriveEasy - Customer Rental Portal");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel lblUser = new JLabel("Welcome, " + customerName);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(224, 231, 255));

        JButton btnLogout = new JButton("Logout");
        UITheme.styleDangerButton(btnLogout);
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        userPanel.add(lblUser);
        userPanel.add(btnLogout);

        navPanel.add(lblTitle, BorderLayout.WEST);
        navPanel.add(userPanel, BorderLayout.EAST);
        mainPanel.add(navPanel, BorderLayout.NORTH);

        // 2. Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tab 1: Book a Vehicle
        JPanel bookingTab = createBookingPanel();
        tabbedPane.addTab("  Book a Vehicle  ", bookingTab);

        // Tab 2: My Booking History
        JPanel historyTab = createBookingHistoryPanel();
        tabbedPane.addTab("  My Booking History & Invoices  ", historyTab);

        // Auto-refresh when customer switches to the history tab
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                loadBookingHistory();
            } else if (tabbedPane.getSelectedIndex() == 0) {
                loadAvailableVehicles();
            }
        });

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    /**
     * Builds the Vehicle Booking Panel (Catalog Table + Search/Filter + Booking Form)
     */
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT: Available Vehicles Catalog with Search & Filter
        JPanel catalogPanel = new JPanel(new BorderLayout(5, 5));
        catalogPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Available Fleet Catalog ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));

        // Filter & Search Toolbar
        JPanel filterToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filterToolbar.setBackground(new Color(248, 250, 252));
        filterToolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(2, 6, 2, 6)
        ));

        filterToolbar.add(new JLabel("Search Model:"));
        txtSearchVehicle = new JTextField(12);
        filterToolbar.add(txtSearchVehicle);

        filterToolbar.add(new JLabel("Type:"));
        String[] types = {"All Types", "Car", "SUV", "Bike", "Sedan", "Hatchback"};
        comboFilterType = new JComboBox<>(types);
        filterToolbar.add(comboFilterType);

        btnSearch = new JButton("Filter");
        UITheme.stylePrimaryButton(btnSearch);
        btnSearch.addActionListener(e -> applyFleetFilter());
        filterToolbar.add(btnSearch);

        btnResetSearch = new JButton("Reset");
        UITheme.styleSecondaryButton(btnResetSearch);
        btnResetSearch.addActionListener(e -> {
            txtSearchVehicle.setText("");
            comboFilterType.setSelectedIndex(0);
            loadAvailableVehicles();
        });
        filterToolbar.add(btnResetSearch);

        catalogPanel.add(filterToolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Vehicle Number", "Model / Name", "Type", "Rent / Day (₹)"};
        tableModelVehicles = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableAvailableVehicles = new JTable(tableModelVehicles);
        tableAvailableVehicles.setRowHeight(26);
        tableAvailableVehicles.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableAvailableVehicles.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableAvailableVehicles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        catalogPanel.add(new JScrollPane(tableAvailableVehicles), BorderLayout.CENTER);
        panel.add(catalogPanel, BorderLayout.CENTER);

        // RIGHT: Booking Form Panel
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setPreferredSize(new Dimension(420, 0));
        formContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Rental Reservation Form ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));

        // Vehicle Photo Preview Box
        lblVehicleImage = new JLabel("Click any vehicle to preview photo", SwingConstants.CENTER);
        lblVehicleImage.setPreferredSize(new Dimension(390, 180));
        lblVehicleImage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVehicleImage.setForeground(new Color(100, 116, 139));
        lblVehicleImage.setBackground(new Color(241, 245, 249));
        lblVehicleImage.setOpaque(true);
        lblVehicleImage.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));

        JPanel imageWrapper = new JPanel(new BorderLayout());
        imageWrapper.setBorder(new EmptyBorder(8, 12, 4, 12));
        imageWrapper.add(lblVehicleImage, BorderLayout.CENTER);
        formContainer.add(imageWrapper, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBorder(new EmptyBorder(6, 10, 6, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtSelectedVehicle = new JTextField("Click a vehicle from list");
        txtSelectedVehicle.setEditable(false);
        txtSelectedVehicle.setBackground(new Color(241, 245, 249));

        txtDailyRate = new JTextField("₹0.00");
        txtDailyRate.setEditable(false);
        txtDailyRate.setBackground(new Color(241, 245, 249));

        LocalDate today = LocalDate.now();
        txtStartDate = new JTextField(today.toString());
        txtEndDate = new JTextField(today.plusDays(3).toString());

        lblDaysCalculated = new JLabel("Duration: -");
        lblDaysCalculated.setFont(new Font("Segoe UI", Font.BOLD, 13));

        lblTotalAmountCalculated = new JLabel("Total Fare: ₹0.00");
        lblTotalAmountCalculated.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalAmountCalculated.setForeground(new Color(22, 138, 63));

        addFormRow(formFields, gbc, 0, "Selected Vehicle:", txtSelectedVehicle);
        addFormRow(formFields, gbc, 1, "Daily Rate:", txtDailyRate);
        addFormRow(formFields, gbc, 2, "Start Date (YYYY-MM-DD):", txtStartDate);
        addFormRow(formFields, gbc, 3, "End Date (YYYY-MM-DD):", txtEndDate);
        addFormRow(formFields, gbc, 4, "Estimated Days:", lblDaysCalculated);
        addFormRow(formFields, gbc, 5, "Total Amount:", lblTotalAmountCalculated);

        formContainer.add(formFields, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        actionPanel.setBorder(new EmptyBorder(6, 12, 12, 12));

        btnCalculateFare = new JButton("Calculate Total Fare");
        UITheme.styleSecondaryButton(btnCalculateFare);

        btnConfirmBooking = new JButton("Proceed to Payment & Book");
        UITheme.stylePrimaryButton(btnConfirmBooking);

        btnClearBookingForm = new JButton("Clear Form");
        UITheme.styleSecondaryButton(btnClearBookingForm);

        actionPanel.add(btnCalculateFare);
        actionPanel.add(btnConfirmBooking);
        actionPanel.add(btnClearBookingForm);

        formContainer.add(actionPanel, BorderLayout.SOUTH);
        panel.add(formContainer, BorderLayout.EAST);

        // Selection Listener
        tableAvailableVehicles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableAvailableVehicles.getSelectedRow();
                if (selectedRow != -1) {
                    int vehicleId = Integer.parseInt(tableModelVehicles.getValueAt(selectedRow, 0).toString());
                    currentSelectedVehicle = vehicleDAO.getVehicleById(vehicleId);
                    if (currentSelectedVehicle != null) {
                        txtSelectedVehicle.setText(currentSelectedVehicle.getModelName() + " (" + currentSelectedVehicle.getVehicleNumber() + ")");
                        txtDailyRate.setText("₹" + String.format("%.2f", currentSelectedVehicle.getPricePerDay()) + " / day");

                        ImageIcon icon = ImageUtil.getVehicleIcon(currentSelectedVehicle.getModelName(), 390, 180);
                        if (icon != null) {
                            lblVehicleImage.setIcon(icon);
                            lblVehicleImage.setText("");
                        } else {
                            lblVehicleImage.setIcon(null);
                            lblVehicleImage.setText("No photo available for this vehicle");
                        }

                        calculateEstimatedFare(false);
                    }
                }
            }
        });

        btnCalculateFare.addActionListener(e -> calculateEstimatedFare(true));
        btnConfirmBooking.addActionListener(e -> handleConfirmBooking());
        btnClearBookingForm.addActionListener(e -> clearBookingForm());

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.45;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        panel.add(field, gbc);
    }

    private void loadAvailableVehicles() {
        tableModelVehicles.setRowCount(0);
        List<Vehicle> list = vehicleDAO.getAvailableVehicles();
        for (Vehicle v : list) {
            tableModelVehicles.addRow(new Object[]{
                    v.getVehicleId(),
                    v.getVehicleNumber(),
                    v.getModelName(),
                    v.getVehicleType(),
                    String.format("%.2f", v.getPricePerDay())
            });
        }
    }

    private void applyFleetFilter() {
        String keyword = txtSearchVehicle.getText().trim();
        String selectedType = (String) comboFilterType.getSelectedItem();
        tableModelVehicles.setRowCount(0);

        List<Vehicle> list = vehicleDAO.searchAvailableVehicles(keyword, selectedType);
        for (Vehicle v : list) {
            tableModelVehicles.addRow(new Object[]{
                    v.getVehicleId(),
                    v.getVehicleNumber(),
                    v.getModelName(),
                    v.getVehicleType(),
                    String.format("%.2f", v.getPricePerDay())
            });
        }
    }

    private boolean calculateEstimatedFare(boolean showValidationErrors) {
        if (currentSelectedVehicle == null) {
            if (showValidationErrors) {
                JOptionPane.showMessageDialog(this, "Please select an available vehicle from the fleet table first.", "Vehicle Required", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        String startStr = txtStartDate.getText().trim();
        String endStr = txtEndDate.getText().trim();

        LocalDate startDate = ValidationUtil.parseDate(startStr);
        LocalDate endDate = ValidationUtil.parseDate(endStr);

        if (startDate == null || endDate == null) {
            if (showValidationErrors) {
                JOptionPane.showMessageDialog(this, "Please enter valid dates in YYYY-MM-DD format (e.g. " + LocalDate.now() + ").", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        if (!ValidationUtil.isValidDateRange(startDate, endDate)) {
            if (showValidationErrors) {
                JOptionPane.showMessageDialog(this, "End Date cannot be before Start Date.", "Date Order Error", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        long daysDiff = ChronoUnit.DAYS.between(startDate, endDate);
        int totalDays = (int) (daysDiff == 0 ? 1 : daysDiff);

        double totalAmount = totalDays * currentSelectedVehicle.getPricePerDay();

        lblDaysCalculated.setText("Duration: " + totalDays + " day(s)");
        lblTotalAmountCalculated.setText("Total Fare: ₹" + String.format("%.2f", totalAmount));

        return true;
    }

    private void handleConfirmBooking() {
        if (customerProfile == null) {
            JOptionPane.showMessageDialog(this, "Customer profile not found for this user account.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!calculateEstimatedFare(true)) {
            return;
        }

        Vehicle freshVehicle = vehicleDAO.getVehicleById(currentSelectedVehicle.getVehicleId());
        if (freshVehicle == null || !"AVAILABLE".equalsIgnoreCase(freshVehicle.getStatus())) {
            JOptionPane.showMessageDialog(this, "Sorry, this vehicle is no longer available. Please select another.", "Vehicle Unavailable", JOptionPane.WARNING_MESSAGE);
            loadAvailableVehicles();
            clearBookingForm();
            return;
        }

        LocalDate startDate = ValidationUtil.parseDate(txtStartDate.getText().trim());
        LocalDate endDate = ValidationUtil.parseDate(txtEndDate.getText().trim());
        long daysDiff = ChronoUnit.DAYS.between(startDate, endDate);
        int totalDays = (int) (daysDiff == 0 ? 1 : daysDiff);
        double totalAmount = totalDays * currentSelectedVehicle.getPricePerDay();

        // Final Phase: Open Secure Multi-Channel Payment Dialog
        PaymentDialog paymentDialog = new PaymentDialog(this, currentSelectedVehicle.getModelName(), totalDays, totalAmount);
        paymentDialog.setVisible(true);

        PaymentDialog.PaymentResult payResult = paymentDialog.getResult();
        if (payResult == null || !payResult.isPaid()) {
            // User cancelled payment
            return;
        }

        // Create booking object with payment details
        Booking booking = new Booking(
                customerProfile.getCustomerId(),
                currentSelectedVehicle.getVehicleId(),
                startDate,
                endDate,
                totalDays,
                totalAmount,
                "CONFIRMED",
                payResult.getPaymentMethod(),
                payResult.getPaymentStatus(),
                payResult.getTransactionId()
        );

        // Atomic transaction in MySQL
        boolean success = bookingDAO.createBooking(booking);

        if (success) {
            int viewBillChoice = JOptionPane.showConfirmDialog(this,
                    "🎉 Booking Confirmed Successfully!\n\n" +
                    "Vehicle: " + currentSelectedVehicle.getModelName() + "\n" +
                    "Payment Mode: " + payResult.getPaymentMethod() + "\n" +
                    "Transaction ID: " + payResult.getTransactionId() + "\n" +
                    "Total Paid: ₹" + String.format("%.2f", totalAmount) + "\n\n" +
                    "Would you like to view and print your official Tax Invoice right now?",
                    "Booking & Payment Success", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            clearBookingForm();
            loadAvailableVehicles();
            loadBookingHistory();

            if (viewBillChoice == JOptionPane.YES_OPTION) {
                Booking fullBooking = bookingDAO.getBookingById(booking.getBookingId());
                if (fullBooking != null) {
                    new InvoiceDialog(this, fullBooking).setVisible(true);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create booking due to a database error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearBookingForm() {
        currentSelectedVehicle = null;
        txtSelectedVehicle.setText("Click a vehicle from list");
        txtDailyRate.setText("₹0.00");
        lblDaysCalculated.setText("Duration: -");
        lblTotalAmountCalculated.setText("Total Fare: ₹0.00");
        if (lblVehicleImage != null) {
            lblVehicleImage.setIcon(null);
            lblVehicleImage.setText("Click any vehicle to preview photo");
        }
        tableAvailableVehicles.clearSelection();
    }

    // =========================================================================
    // TAB 2: MY BOOKING HISTORY & INVOICES PANEL
    // =========================================================================
    private JPanel createBookingHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT / CENTER: Bookings Table
        JPanel tableContainer = new JPanel(new BorderLayout(5, 5));
        tableContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " My Rental Bookings (Select row for actions) ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));

        String[] cols = {"ID", "Vehicle Rented", "Start Date", "End Date", "Total (₹)", "Payment", "Status", "Booked On"};
        tableModelHistory = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableBookingHistory = new JTable(tableModelHistory);
        tableBookingHistory.setRowHeight(26);
        tableBookingHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableBookingHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableBookingHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableContainer.add(new JScrollPane(tableBookingHistory), BorderLayout.CENTER);

        // Bottom summary and refresh
        JPanel tableToolbar = new JPanel(new BorderLayout());
        lblHistorySummary = new JLabel("Total Bookings: 0 | Total Spent: ₹0.00");
        lblHistorySummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHistorySummary.setForeground(new Color(51, 65, 85));
        lblHistorySummary.setBorder(new EmptyBorder(6, 6, 6, 6));

        btnRefreshHistory = new JButton("Refresh History");
        UITheme.styleSecondaryButton(btnRefreshHistory);
        btnRefreshHistory.addActionListener(e -> loadBookingHistory());

        tableToolbar.add(lblHistorySummary, BorderLayout.WEST);
        tableToolbar.add(btnRefreshHistory, BorderLayout.EAST);
        tableContainer.add(tableToolbar, BorderLayout.SOUTH);

        panel.add(tableContainer, BorderLayout.CENTER);

        // RIGHT: Booking Detail Card & Action Buttons
        JPanel detailContainer = new JPanel(new BorderLayout(8, 8));
        detailContainer.setPreferredSize(new Dimension(360, 0));
        detailContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Rental Actions & Receipt ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));

        // Vehicle photo box
        lblHistoryVehicleImage = new JLabel("Select a booking to view photo", SwingConstants.CENTER);
        lblHistoryVehicleImage.setPreferredSize(new Dimension(340, 160));
        lblHistoryVehicleImage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHistoryVehicleImage.setForeground(new Color(100, 116, 139));
        lblHistoryVehicleImage.setBackground(new Color(241, 245, 249));
        lblHistoryVehicleImage.setOpaque(true);
        lblHistoryVehicleImage.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));

        JPanel imgWrap = new JPanel(new BorderLayout());
        imgWrap.setBorder(new EmptyBorder(6, 8, 4, 8));
        imgWrap.add(lblHistoryVehicleImage, BorderLayout.CENTER);
        detailContainer.add(imgWrap, BorderLayout.NORTH);

        lblHistoryDetailText = new JLabel("<html><div style='padding:8px; color:#64748b;'><p>Select any booking record from the table to see details or execute rental actions.</p></div></html>");
        lblHistoryDetailText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHistoryDetailText.setVerticalAlignment(SwingConstants.TOP);

        detailContainer.add(new JScrollPane(lblHistoryDetailText), BorderLayout.CENTER);

        // Action Buttons under Detail Card: View Bill, Return Vehicle, Cancel Booking
        JPanel btnActionGrid = new JPanel(new GridLayout(3, 1, 6, 6));
        btnActionGrid.setBorder(new EmptyBorder(8, 12, 12, 12));

        btnViewInvoice = new JButton("📄 View & Print Invoice");
        UITheme.stylePrimaryButton(btnViewInvoice);
        btnViewInvoice.addActionListener(e -> handleOpenInvoice());

        btnReturnVehicle = new JButton("🔄 Return Vehicle (Inspection)");
        UITheme.styleSuccessButton(btnReturnVehicle);
        btnReturnVehicle.addActionListener(e -> handleReturnVehicle());

        btnCancelBooking = new JButton("❌ Cancel Booking");
        UITheme.styleDangerButton(btnCancelBooking);
        btnCancelBooking.addActionListener(e -> handleCancelBooking());

        btnActionGrid.add(btnViewInvoice);
        btnActionGrid.add(btnReturnVehicle);
        btnActionGrid.add(btnCancelBooking);

        detailContainer.add(btnActionGrid, BorderLayout.SOUTH);
        panel.add(detailContainer, BorderLayout.EAST);

        // Selection Listener
        tableBookingHistory.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableBookingHistory.getSelectedRow();
                if (row != -1) {
                    populateHistoryDetailCard(row);
                }
            }
        });

        return panel;
    }

    private void loadBookingHistory() {
        if (customerProfile == null || tableModelHistory == null) return;
        tableModelHistory.setRowCount(0);

        List<Booking> list = bookingDAO.getBookingsByCustomerId(customerProfile.getCustomerId());
        double totalSpent = 0.0;

        for (Booking b : list) {
            if (!"CANCELLED".equalsIgnoreCase(b.getBookingStatus())) {
                totalSpent += b.getGrandTotal();
            }
            tableModelHistory.addRow(new Object[]{
                    b.getBookingId(),
                    b.getVehicleName(),
                    b.getStartDate(),
                    b.getEndDate(),
                    String.format("%.2f", b.getGrandTotal()),
                    b.getPaymentMethod() != null ? b.getPaymentMethod() : "Cash",
                    b.getBookingStatus(),
                    b.getBookingDate() != null ? b.getBookingDate().substring(0, 10) : "-"
            });
        }

        lblHistorySummary.setText("Total Bookings: " + list.size() + " | Total Amount: ₹" + String.format("%.2f", totalSpent));

        if (!list.isEmpty() && tableBookingHistory.getSelectedRow() == -1) {
            tableBookingHistory.setRowSelectionInterval(0, 0);
        }
    }

    private void populateHistoryDetailCard(int row) {
        int bookingId = Integer.parseInt(tableModelHistory.getValueAt(row, 0).toString());
        Booking b = bookingDAO.getBookingById(bookingId);
        if (b == null) return;

        ImageIcon icon = ImageUtil.getVehicleIcon(b.getVehicleName(), 340, 160);
        if (icon != null) {
            lblHistoryVehicleImage.setIcon(icon);
            lblHistoryVehicleImage.setText("");
        } else {
            lblHistoryVehicleImage.setIcon(null);
            lblHistoryVehicleImage.setText("No photo available");
        }

        String statusBadgeColor = "#166534";
        String statusBgColor = "#dcfce7";
        if ("CANCELLED".equalsIgnoreCase(b.getBookingStatus())) {
            statusBadgeColor = "#991b1b";
            statusBgColor = "#fee2e2";
        } else if ("RETURNED".equalsIgnoreCase(b.getBookingStatus())) {
            statusBadgeColor = "#1e40af";
            statusBgColor = "#dbeafe";
        }

        String returnInfo = b.getActualReturnDate() != null ? b.getActualReturnDate().toString() : "Not Returned Yet";
        String lateFeeStr = b.getLateFee() > 0 ? "<p style='color:#dc2626; margin:3px 0;'><b>Late Penalty:</b> ₹" + String.format("%.2f", b.getLateFee()) + "</p>" : "";

        String html = "<html><div style='padding:6px; font-family: Segoe UI;'>" +
                "<h3 style='margin:0; color:#1e293b;'>Booking #" + b.getBookingId() + " (" + b.getVehicleName() + ")</h3>" +
                "<p style='color:#64748b; font-size:11px; margin:2px 0 6px 0;'>Txn: " + (b.getTransactionId() != null ? b.getTransactionId() : "N/A") + "</p>" +
                "<hr style='border: 0.5px solid #cbd5e1; margin-bottom:6px;'>" +
                "<p style='margin:3px 0;'><b>Plate No:</b> " + b.getVehicleNumber() + "</p>" +
                "<p style='margin:3px 0;'><b>Dates:</b> " + b.getStartDate() + " to " + b.getEndDate() + " (" + b.getTotalDays() + "d)</p>" +
                "<p style='margin:3px 0;'><b>Actual Return:</b> " + returnInfo + "</p>" +
                "<p style='margin:3px 0;'><b>Base Fare:</b> ₹" + String.format("%.2f", b.getTotalAmount()) + "</p>" +
                lateFeeStr +
                "<p style='margin:3px 0;'><b>Total Amount:</b> <span style='color:#16a34a; font-weight:bold; font-size:14px;'>₹" + String.format("%.2f", b.getGrandTotal()) + "</span></p>" +
                "<p style='margin:5px 0;'><b>Status:</b> <span style='background-color:" + statusBgColor + "; color:" + statusBadgeColor + "; padding:2px 6px; border-radius:4px;'><b>" + b.getBookingStatus() + "</b></span></p>" +
                "</div></html>";

        lblHistoryDetailText.setText(html);
    }

    private void handleOpenInvoice() {
        int row = tableBookingHistory.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to view its invoice.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = Integer.parseInt(tableModelHistory.getValueAt(row, 0).toString());
        Booking b = bookingDAO.getBookingById(bookingId);
        if (b != null) {
            new InvoiceDialog(this, b).setVisible(true);
        }
    }

    private void handleReturnVehicle() {
        int row = tableBookingHistory.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to return.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = Integer.parseInt(tableModelHistory.getValueAt(row, 0).toString());
        Booking b = bookingDAO.getBookingById(bookingId);
        if (b == null) return;

        if (!"CONFIRMED".equalsIgnoreCase(b.getBookingStatus())) {
            JOptionPane.showMessageDialog(this,
                    "This booking is currently marked as '" + b.getBookingStatus() + "'.\nOnly active CONFIRMED rentals can be returned.",
                    "Cannot Return", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ReturnVehicleDialog dialog = new ReturnVehicleDialog(this, b, bookingDAO);
        dialog.setVisible(true);

        if (dialog.isReturnedSuccess()) {
            loadAvailableVehicles();
            loadBookingHistory();
        }
    }

    private void handleCancelBooking() {
        int row = tableBookingHistory.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = Integer.parseInt(tableModelHistory.getValueAt(row, 0).toString());
        Booking b = bookingDAO.getBookingById(bookingId);
        if (b == null) return;

        if (!"CONFIRMED".equalsIgnoreCase(b.getBookingStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Only CONFIRMED bookings can be cancelled.\nCurrent status: " + b.getBookingStatus(),
                    "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking #" + bookingId + " (" + b.getVehicleName() + ")?\n\n" +
                "• Vehicle will be immediately restored to AVAILABLE status in fleet.\n" +
                "• Booking status will become CANCELLED.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = bookingDAO.cancelBooking(b.getBookingId(), b.getVehicleId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " has been cancelled successfully.", "Cancellation Successful", JOptionPane.INFORMATION_MESSAGE);
                loadAvailableVehicles();
                loadBookingHistory();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking due to database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
