package ui;

import dao.BookingDAO;
import dao.CustomerDAO;
import dao.VehicleDAO;
import model.Booking;
import model.Customer;
import model.User;
import model.Vehicle;
import util.ImageUtil;
import util.UITheme;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * AdminDashboard Frame
 * Final Phase: Complete Enterprise-grade Admin Console for DriveEasy.
 * Tab 1: Vehicle Fleet Management (CRUD + Live Search + Filters)
 * Tab 2: Customer Management & Rental Operations (Invoice, Return Inspection, Cancellation)
 * Tab 3: Business Analytics & Revenue Dashboard (Live KPI Cards + Graphics2D Visual Chart)
 * Demonstrates: Java Swing, Multi-tabbed Layouts, JDBC Transactions, 2D Graphics Rendering.
 */
public class AdminDashboard extends JFrame {

    private final User adminUser;
    private final VehicleDAO vehicleDAO;
    private final CustomerDAO customerDAO;
    private final BookingDAO bookingDAO;

    // Vehicle Management Components
    private JLabel lblAdminVehicleThumb;
    private JTextField txtVehicleId;
    private JTextField txtVehicleNumber;
    private JTextField txtModelName;
    private JComboBox<String> comboVehicleType;
    private JTextField txtPricePerDay;
    private JComboBox<String> comboStatus;

    private JButton btnAddVehicle;
    private JButton btnUpdateVehicle;
    private JButton btnDeleteVehicle;
    private JButton btnClearForm;
    private JButton btnRefreshVehicles;

    // Vehicle Filter Components
    private JTextField txtSearchFleet;
    private JComboBox<String> comboFilterFleetType;
    private JComboBox<String> comboFilterFleetStatus;
    private JButton btnSearchFleet;
    private JButton btnResetFleetSearch;

    private JTable tableVehicles;
    private DefaultTableModel tableModelVehicles;
    private JLabel lblFleetSummary;

    // Customer Management Components
    private JTable tableCustomers;
    private DefaultTableModel tableModelCustomers;
    private JTable tableCustomerBookings;
    private DefaultTableModel tableModelCustomerBookings;
    private JLabel lblCustomerHistoryHeader;
    private JLabel lblCustomerHistorySummary;
    private JButton btnRefreshCustomers;

    // Customer Rental Operation Buttons
    private JButton btnAdminViewInvoice;
    private JButton btnAdminProcessReturn;
    private JButton btnAdminCancelRental;
    private int selectedCustomerId = -1;
    private String selectedCustomerName = "";

    // Analytics Tab Components
    private JLabel lblKpiTotalRevenue;
    private JLabel lblKpiTotalBookings;
    private JLabel lblKpiActiveRentals;
    private JLabel lblKpiAvailableFleet;
    private JLabel lblKpiSecondary;
    private RevenueChartPanel revenueChartPanel;
    private JButton btnRefreshAnalytics;

    public AdminDashboard(User adminUser) {
        this.adminUser = adminUser;
        this.vehicleDAO = new VehicleDAO();
        this.customerDAO = new CustomerDAO();
        this.bookingDAO = new BookingDAO();

        setTitle("DriveEasy - Administrator Control Center");
        setSize(1100, 740);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadVehicleData();
        loadCustomerData();
        loadAnalyticsData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 1. Top Navigation Bar
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(new Color(30, 41, 59));
        navPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("DriveEasy - Admin Management Portal");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel lblUser = new JLabel("Logged in as: " + adminUser.getUsername() + " (ADMIN)");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(226, 232, 240));

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

        // Tab 1: Vehicle Management
        JPanel vehicleTab = createVehicleManagementPanel();
        tabbedPane.addTab("  Vehicle Fleet Management  ", vehicleTab);

        // Tab 2: Customer Management & Rental Operations
        JPanel customerTab = createCustomerManagementPanel();
        tabbedPane.addTab("  Customer & Rental Operations  ", customerTab);

        // Tab 3: Business Analytics & Revenue
        JPanel analyticsTab = createAnalyticsPanel();
        tabbedPane.addTab("  Analytics & Revenue  ", analyticsTab);

        // Listener to refresh analytics on tab switch
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 2) {
                loadAnalyticsData();
            }
        });

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    // =========================================================================
    // TAB 1: VEHICLE MANAGEMENT PANEL
    // =========================================================================
    private JPanel createVehicleManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Left Form Panel
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setPreferredSize(new Dimension(380, 0));
        formContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Vehicle Information ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));

        // Vehicle Photo Preview Box for Admin
        lblAdminVehicleThumb = new JLabel("Click any vehicle to preview photo", SwingConstants.CENTER);
        lblAdminVehicleThumb.setPreferredSize(new Dimension(350, 150));
        lblAdminVehicleThumb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAdminVehicleThumb.setForeground(new Color(100, 116, 139));
        lblAdminVehicleThumb.setBackground(new Color(241, 245, 249));
        lblAdminVehicleThumb.setOpaque(true);
        lblAdminVehicleThumb.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));

        JPanel thumbWrapper = new JPanel(new BorderLayout());
        thumbWrapper.setBorder(new EmptyBorder(6, 12, 4, 12));
        thumbWrapper.add(lblAdminVehicleThumb, BorderLayout.CENTER);
        formContainer.add(thumbWrapper, BorderLayout.NORTH);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBorder(new EmptyBorder(8, 10, 8, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtVehicleId = new JTextField();
        txtVehicleId.setEditable(false);
        txtVehicleId.setBackground(new Color(241, 245, 249));

        txtVehicleNumber = new JTextField();
        txtModelName = new JTextField();

        String[] types = {"Car", "Bike", "SUV", "Sedan", "Hatchback"};
        comboVehicleType = new JComboBox<>(types);

        txtPricePerDay = new JTextField();

        String[] statuses = {"AVAILABLE", "BOOKED"};
        comboStatus = new JComboBox<>(statuses);

        addFormRow(formFields, gbc, 0, "Vehicle ID:", txtVehicleId);
        addFormRow(formFields, gbc, 1, "Vehicle No:*", txtVehicleNumber);
        addFormRow(formFields, gbc, 2, "Model / Name:*", txtModelName);
        addFormRow(formFields, gbc, 3, "Vehicle Type:*", comboVehicleType);
        addFormRow(formFields, gbc, 4, "Rent/Day (₹):*", txtPricePerDay);
        addFormRow(formFields, gbc, 5, "Status:", comboStatus);

        formContainer.add(formFields, BorderLayout.CENTER);

        // Buttons Grid for Form
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnAddVehicle = new JButton("Add Vehicle");
        UITheme.styleSuccessButton(btnAddVehicle);

        btnUpdateVehicle = new JButton("Update");
        UITheme.stylePrimaryButton(btnUpdateVehicle);

        btnDeleteVehicle = new JButton("Delete");
        UITheme.styleDangerButton(btnDeleteVehicle);

        btnClearForm = new JButton("Clear Form");
        UITheme.styleSecondaryButton(btnClearForm);

        buttonsPanel.add(btnAddVehicle);
        buttonsPanel.add(btnUpdateVehicle);
        buttonsPanel.add(btnDeleteVehicle);
        buttonsPanel.add(btnClearForm);

        formContainer.add(buttonsPanel, BorderLayout.SOUTH);
        panel.add(formContainer, BorderLayout.WEST);

        // Right Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout(6, 6));
        tableContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Vehicle Fleet Directory ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));

        // Fleet Search & Filter Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchBar.setBackground(new Color(248, 250, 252));
        searchBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSearchFleet = new JTextField(12);
        txtSearchFleet.setToolTipText("Search model name or vehicle number");

        JLabel lblFilterType = new JLabel("Type:");
        lblFilterType.setFont(new Font("Segoe UI", Font.BOLD, 12));
        comboFilterFleetType = new JComboBox<>(new String[]{"All Types", "Car", "Bike", "SUV", "Sedan", "Hatchback"});

        JLabel lblFilterStatus = new JLabel("Status:");
        lblFilterStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        comboFilterFleetStatus = new JComboBox<>(new String[]{"All Status", "AVAILABLE", "BOOKED"});

        btnSearchFleet = new JButton("Filter");
        UITheme.stylePrimaryButton(btnSearchFleet);

        btnResetFleetSearch = new JButton("Reset");
        UITheme.styleSecondaryButton(btnResetFleetSearch);

        searchBar.add(lblSearch);
        searchBar.add(txtSearchFleet);
        searchBar.add(lblFilterType);
        searchBar.add(comboFilterFleetType);
        searchBar.add(lblFilterStatus);
        searchBar.add(comboFilterFleetStatus);
        searchBar.add(btnSearchFleet);
        searchBar.add(btnResetFleetSearch);

        tableContainer.add(searchBar, BorderLayout.NORTH);

        String[] columns = {"ID", "Vehicle Number", "Model Name", "Type", "Price / Day (₹)", "Status"};
        tableModelVehicles = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableVehicles = new JTable(tableModelVehicles);
        tableVehicles.setRowHeight(26);
        tableVehicles.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableVehicles.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableVehicles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tableVehicles);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Table Bottom Toolbar
        JPanel tableToolbar = new JPanel(new BorderLayout());
        lblFleetSummary = new JLabel(" Total: 0 vehicles", SwingConstants.LEFT);
        lblFleetSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFleetSummary.setForeground(new Color(71, 85, 105));

        btnRefreshVehicles = new JButton("Refresh Table");
        UITheme.styleSecondaryButton(btnRefreshVehicles);

        tableToolbar.add(lblFleetSummary, BorderLayout.WEST);
        tableToolbar.add(btnRefreshVehicles, BorderLayout.EAST);

        tableContainer.add(tableToolbar, BorderLayout.SOUTH);
        panel.add(tableContainer, BorderLayout.CENTER);

        // Action Listeners
        btnAddVehicle.addActionListener(e -> handleAddVehicle());
        btnUpdateVehicle.addActionListener(e -> handleUpdateVehicle());
        btnDeleteVehicle.addActionListener(e -> handleDeleteVehicle());
        btnClearForm.addActionListener(e -> clearVehicleForm());
        btnRefreshVehicles.addActionListener(e -> {
            clearFleetFilters();
            loadVehicleData();
        });

        btnSearchFleet.addActionListener(e -> filterVehicleData());
        btnResetFleetSearch.addActionListener(e -> {
            clearFleetFilters();
            loadVehicleData();
        });

        tableVehicles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableVehicles.getSelectedRow();
                if (selectedRow != -1) {
                    populateFormFromSelectedRow(selectedRow);
                }
            }
        });

        return panel;
    }

    // =========================================================================
    // TAB 2: CUSTOMER MANAGEMENT & RENTAL OPERATIONS
    // =========================================================================
    private JPanel createCustomerManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.45);
        splitPane.setDividerLocation(250);

        // Top Section: Customers Table
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Registered Customers (Click a customer to manage bookings & invoices) ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));

        String[] customerCols = {"Customer ID", "Username", "Full Name", "Email", "Phone", "Address"};
        tableModelCustomers = new DefaultTableModel(customerCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableCustomers = new JTable(tableModelCustomers);
        tableCustomers.setRowHeight(25);
        tableCustomers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCustomers.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableCustomers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        topPanel.add(new JScrollPane(tableCustomers), BorderLayout.CENTER);

        JPanel custToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefreshCustomers = new JButton("Refresh Customers");
        UITheme.styleSecondaryButton(btnRefreshCustomers);
        btnRefreshCustomers.addActionListener(e -> loadCustomerData());
        custToolbar.add(btnRefreshCustomers);
        topPanel.add(custToolbar, BorderLayout.SOUTH);

        // Bottom Section: Rental History for Selected Customer + Operations
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Customer Rental Records & Active Contracts ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));

        lblCustomerHistoryHeader = new JLabel("Select a customer above to view and manage their bookings.", SwingConstants.LEFT);
        lblCustomerHistoryHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCustomerHistoryHeader.setForeground(new Color(30, 41, 59));
        lblCustomerHistoryHeader.setBorder(new EmptyBorder(4, 8, 4, 8));
        bottomPanel.add(lblCustomerHistoryHeader, BorderLayout.NORTH);

        String[] bookingCols = {
                "Booking ID", "Vehicle ID", "Vehicle Booked", "Start Date", "End Date",
                "Days", "Amount (₹)", "Late Fee (₹)", "Grand Total (₹)", "Payment", "Status", "Booked On"
        };
        tableModelCustomerBookings = new DefaultTableModel(bookingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableCustomerBookings = new JTable(tableModelCustomerBookings);
        tableCustomerBookings.setRowHeight(25);
        tableCustomerBookings.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCustomerBookings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableCustomerBookings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        bottomPanel.add(new JScrollPane(tableCustomerBookings), BorderLayout.CENTER);

        // Bottom Action Bar: Summary + Action Buttons
        JPanel actionContainer = new JPanel(new BorderLayout(10, 5));
        actionContainer.setBorder(new EmptyBorder(6, 8, 6, 8));

        lblCustomerHistorySummary = new JLabel("Total Bookings: 0 | Total Spent: ₹0.00", SwingConstants.LEFT);
        lblCustomerHistorySummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCustomerHistorySummary.setForeground(new Color(71, 85, 105));
        actionContainer.add(lblCustomerHistorySummary, BorderLayout.WEST);

        JPanel opsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnAdminViewInvoice = new JButton("View / Print Invoice");
        UITheme.stylePrimaryButton(btnAdminViewInvoice);

        btnAdminProcessReturn = new JButton("Process Return");
        UITheme.styleSuccessButton(btnAdminProcessReturn);

        btnAdminCancelRental = new JButton("Cancel Rental");
        UITheme.styleDangerButton(btnAdminCancelRental);

        opsPanel.add(btnAdminViewInvoice);
        opsPanel.add(btnAdminProcessReturn);
        opsPanel.add(btnAdminCancelRental);
        actionContainer.add(opsPanel, BorderLayout.EAST);

        bottomPanel.add(actionContainer, BorderLayout.SOUTH);

        // Customer selection listener
        tableCustomers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableCustomers.getSelectedRow();
                if (row != -1) {
                    selectedCustomerId = Integer.parseInt(tableModelCustomers.getValueAt(row, 0).toString());
                    selectedCustomerName = tableModelCustomers.getValueAt(row, 2).toString();
                    loadCustomerRentalHistory(selectedCustomerId, selectedCustomerName);
                }
            }
        });

        // Booking operation listeners
        btnAdminViewInvoice.addActionListener(e -> handleAdminViewInvoice());
        btnAdminProcessReturn.addActionListener(e -> handleAdminProcessReturn());
        btnAdminCancelRental.addActionListener(e -> handleAdminCancelRental());

        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // TAB 3: BUSINESS ANALYTICS & REVENUE DASHBOARD
    // =========================================================================
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 250, 252));

        // 1. Top Section: 4 Metric KPI Cards
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiGrid.setOpaque(false);

        lblKpiTotalRevenue = new JLabel("₹0.00", SwingConstants.CENTER);
        lblKpiTotalBookings = new JLabel("0", SwingConstants.CENTER);
        lblKpiActiveRentals = new JLabel("0", SwingConstants.CENTER);
        lblKpiAvailableFleet = new JLabel("0", SwingConstants.CENTER);

        kpiGrid.add(createKpiCard("TOTAL REVENUE", lblKpiTotalRevenue, new Color(16, 185, 129)));
        kpiGrid.add(createKpiCard("TOTAL BOOKINGS", lblKpiTotalBookings, new Color(59, 130, 246)));
        kpiGrid.add(createKpiCard("ACTIVE RENTALS", lblKpiActiveRentals, new Color(139, 92, 246)));
        kpiGrid.add(createKpiCard("AVAILABLE FLEET", lblKpiAvailableFleet, new Color(14, 165, 233)));

        // Secondary metrics banner
        JPanel topHeader = new JPanel(new BorderLayout(10, 10));
        topHeader.setOpaque(false);
        topHeader.add(kpiGrid, BorderLayout.CENTER);

        lblKpiSecondary = new JLabel("Completed: 0 | Cancelled: 0 | Late Penalty Collected: ₹0.00", SwingConstants.CENTER);
        lblKpiSecondary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKpiSecondary.setForeground(new Color(100, 116, 139));
        lblKpiSecondary.setBorder(new EmptyBorder(6, 0, 4, 0));
        topHeader.add(lblKpiSecondary, BorderLayout.SOUTH);

        panel.add(topHeader, BorderLayout.NORTH);

        // 2. Center: Revenue Chart Panel
        revenueChartPanel = new RevenueChartPanel();
        revenueChartPanel.setPreferredSize(new Dimension(800, 320));
        panel.add(revenueChartPanel, BorderLayout.CENTER);

        // 3. Bottom Toolbar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);

        btnRefreshAnalytics = new JButton("Refresh Analytics");
        UITheme.stylePrimaryButton(btnRefreshAnalytics);
        btnRefreshAnalytics.addActionListener(e -> loadAnalyticsData());
        bottomBar.add(btnRefreshAnalytics);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        // Decorative color strip at top
        JPanel topStrip = new JPanel();
        topStrip.setBackground(accentColor);
        topStrip.setPreferredSize(new Dimension(0, 4));

        card.add(topStrip, BorderLayout.NORTH);
        card.add(lblTitle, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);

        return card;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    // =========================================================================
    // DATA LOADERS & BUSINESS LOGIC
    // =========================================================================

    /**
     * Loads/refreshes vehicles list from MySQL into JTable.
     */
    private void loadVehicleData() {
        tableModelVehicles.setRowCount(0);
        List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
        int availableCount = 0;
        int bookedCount = 0;

        for (Vehicle v : vehicles) {
            if ("AVAILABLE".equalsIgnoreCase(v.getStatus())) availableCount++;
            else if ("BOOKED".equalsIgnoreCase(v.getStatus())) bookedCount++;

            tableModelVehicles.addRow(new Object[]{
                    v.getVehicleId(),
                    v.getVehicleNumber(),
                    v.getModelName(),
                    v.getVehicleType(),
                    String.format("%.2f", v.getPricePerDay()),
                    v.getStatus()
            });
        }

        lblFleetSummary.setText(String.format(" Total Fleet: %d vehicles (%d Available, %d Booked)",
                vehicles.size(), availableCount, bookedCount));
    }

    /**
     * Filters vehicles list using search keyword, type, and status.
     */
    private void filterVehicleData() {
        String keyword = txtSearchFleet.getText().trim();
        String type = (String) comboFilterFleetType.getSelectedItem();
        if ("All Types".equalsIgnoreCase(type)) type = "All";

        String status = (String) comboFilterFleetStatus.getSelectedItem();
        if ("All Status".equalsIgnoreCase(status)) status = "All";

        List<Vehicle> filtered = vehicleDAO.searchAllVehicles(keyword, type, status);
        tableModelVehicles.setRowCount(0);

        int availableCount = 0;
        int bookedCount = 0;
        for (Vehicle v : filtered) {
            if ("AVAILABLE".equalsIgnoreCase(v.getStatus())) availableCount++;
            else if ("BOOKED".equalsIgnoreCase(v.getStatus())) bookedCount++;

            tableModelVehicles.addRow(new Object[]{
                    v.getVehicleId(),
                    v.getVehicleNumber(),
                    v.getModelName(),
                    v.getVehicleType(),
                    String.format("%.2f", v.getPricePerDay()),
                    v.getStatus()
            });
        }

        lblFleetSummary.setText(String.format(" Filtered: %d vehicles (%d Available, %d Booked)",
                filtered.size(), availableCount, bookedCount));
    }

    private void clearFleetFilters() {
        txtSearchFleet.setText("");
        comboFilterFleetType.setSelectedIndex(0);
        comboFilterFleetStatus.setSelectedIndex(0);
    }

    /**
     * Loads registered customers into customer management table.
     */
    private void loadCustomerData() {
        tableModelCustomers.setRowCount(0);
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer c : customers) {
            tableModelCustomers.addRow(new Object[]{
                    c.getCustomerId(),
                    c.getUsername() != null ? c.getUsername() : "-",
                    c.getFullName(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getAddress() != null ? c.getAddress() : "-"
            });
        }
    }

    /**
     * Loads rental history for a selected customer.
     */
    private void loadCustomerRentalHistory(int customerId, String customerName) {
        tableModelCustomerBookings.setRowCount(0);
        lblCustomerHistoryHeader.setText("Rental Records for: " + customerName + " (Customer ID: " + customerId + ")");

        List<Booking> bookings = bookingDAO.getBookingsByCustomerId(customerId);
        double totalSpent = 0.0;

        for (Booking b : bookings) {
            if (!"CANCELLED".equalsIgnoreCase(b.getBookingStatus())) {
                totalSpent += b.getGrandTotal();
            }
            tableModelCustomerBookings.addRow(new Object[]{
                    b.getBookingId(),
                    b.getVehicleId(),
                    b.getVehicleName(),
                    b.getStartDate(),
                    b.getEndDate(),
                    b.getTotalDays(),
                    String.format("%.2f", b.getTotalAmount()),
                    String.format("%.2f", b.getLateFee()),
                    String.format("%.2f", b.getGrandTotal()),
                    b.getPaymentStatus() != null ? b.getPaymentStatus() : "PAID",
                    b.getBookingStatus(),
                    b.getBookingDate() != null && b.getBookingDate().length() >= 10 ? b.getBookingDate().substring(0, 10) : "-"
            });
        }

        lblCustomerHistorySummary.setText("Total Bookings: " + bookings.size() + " | Total Revenue Generated: ₹" + String.format("%.2f", totalSpent));
    }

    /**
     * Loads live analytics data from DAO into KPI cards and chart.
     */
    @SuppressWarnings("unchecked")
    private void loadAnalyticsData() {
        Map<String, Object> summary = bookingDAO.getAnalyticsSummary();

        double totalRevenue = (Double) summary.getOrDefault("totalRevenue", 0.0);
        int totalBookings = (Integer) summary.getOrDefault("totalBookings", 0);
        int activeRentals = (Integer) summary.getOrDefault("activeRentals", 0);
        int completedRentals = (Integer) summary.getOrDefault("completedRentals", 0);
        int cancelledRentals = (Integer) summary.getOrDefault("cancelledRentals", 0);
        double totalLateFees = (Double) summary.getOrDefault("totalLateFees", 0.0);

        List<Vehicle> allVehicles = vehicleDAO.getAllVehicles();
        long availableFleet = allVehicles.stream().filter(v -> "AVAILABLE".equalsIgnoreCase(v.getStatus())).count();

        lblKpiTotalRevenue.setText("₹" + String.format("%,.2f", totalRevenue));
        lblKpiTotalBookings.setText(String.valueOf(totalBookings));
        lblKpiActiveRentals.setText(String.valueOf(activeRentals));
        lblKpiAvailableFleet.setText(String.valueOf(availableFleet));

        lblKpiSecondary.setText(String.format("Completed Trips: %d  |  Cancelled: %d  |  Total Late Penalty Collected: ₹%.2f",
                completedRentals, cancelledRentals, totalLateFees));

        Map<String, Double> revByType = (Map<String, Double>) summary.get("revenueByType");
        Map<String, Integer> bksByType = (Map<String, Integer>) summary.get("bookingsByType");
        if (revenueChartPanel != null) {
            revenueChartPanel.setData(revByType, bksByType);
        }
    }

    // =========================================================================
    // CUSTOMER BOOKING OPERATIONS (Invoice, Return, Cancel)
    // =========================================================================
    private void handleAdminViewInvoice() {
        int row = tableCustomerBookings.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to view its invoice.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = Integer.parseInt(tableModelCustomerBookings.getValueAt(row, 0).toString());
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking != null) {
            InvoiceDialog dialog = new InvoiceDialog(this, booking);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Could not load booking details for ID: " + bookingId, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAdminProcessReturn() {
        int row = tableCustomerBookings.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an active booking to process vehicle return.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = tableModelCustomerBookings.getValueAt(row, 10).toString();
        if ("RETURNED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This vehicle has already been marked as RETURNED.", "Already Processed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if ("CANCELLED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Cannot return a vehicle for a CANCELLED booking.", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = Integer.parseInt(tableModelCustomerBookings.getValueAt(row, 0).toString());
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Could not load booking details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ReturnVehicleDialog returnDialog = new ReturnVehicleDialog(this, booking);
        returnDialog.setVisible(true);

        if (returnDialog.isReturnSuccessful()) {
            JOptionPane.showMessageDialog(this, "Vehicle return processed successfully! Fleet status updated to AVAILABLE.", "Return Processed", JOptionPane.INFORMATION_MESSAGE);
            loadCustomerRentalHistory(selectedCustomerId, selectedCustomerName);
            loadVehicleData();
            loadAnalyticsData();
        }
    }

    private void handleAdminCancelRental() {
        int row = tableCustomerBookings.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = tableModelCustomerBookings.getValueAt(row, 10).toString();
        if ("CANCELLED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This booking is already cancelled.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if ("RETURNED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Cannot cancel a booking that has already been RETURNED.", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = Integer.parseInt(tableModelCustomerBookings.getValueAt(row, 0).toString());
        int vehicleId = Integer.parseInt(tableModelCustomerBookings.getValueAt(row, 1).toString());
        String vehicleName = tableModelCustomerBookings.getValueAt(row, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel Booking #" + bookingId + " (" + vehicleName + ")?\nThis will release the vehicle back to AVAILABLE status.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = bookingDAO.cancelBooking(bookingId, vehicleId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully! Vehicle is now available for new bookings.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                loadCustomerRentalHistory(selectedCustomerId, selectedCustomerName);
                loadVehicleData();
                loadAnalyticsData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking due to a database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // VEHICLE CRUD FORM HANDLERS
    // =========================================================================
    private void populateFormFromSelectedRow(int row) {
        txtVehicleId.setText(tableModelVehicles.getValueAt(row, 0).toString());
        txtVehicleNumber.setText(tableModelVehicles.getValueAt(row, 1).toString());
        String model = tableModelVehicles.getValueAt(row, 2).toString();
        txtModelName.setText(model);
        comboVehicleType.setSelectedItem(tableModelVehicles.getValueAt(row, 3).toString());
        txtPricePerDay.setText(tableModelVehicles.getValueAt(row, 4).toString());
        comboStatus.setSelectedItem(tableModelVehicles.getValueAt(row, 5).toString());

        // Update photo thumbnail
        if (lblAdminVehicleThumb != null) {
            ImageIcon icon = ImageUtil.getVehicleIcon(model, 350, 150);
            if (icon != null) {
                lblAdminVehicleThumb.setIcon(icon);
                lblAdminVehicleThumb.setText("");
            } else {
                lblAdminVehicleThumb.setIcon(null);
                lblAdminVehicleThumb.setText("No photo available");
            }
        }
    }

    private void clearVehicleForm() {
        txtVehicleId.setText("");
        txtVehicleNumber.setText("");
        txtModelName.setText("");
        comboVehicleType.setSelectedIndex(0);
        txtPricePerDay.setText("");
        comboStatus.setSelectedItem("AVAILABLE");
        if (lblAdminVehicleThumb != null) {
            lblAdminVehicleThumb.setIcon(null);
            lblAdminVehicleThumb.setText("Click any vehicle to preview photo");
        }
        tableVehicles.clearSelection();
    }

    private void handleAddVehicle() {
        String number = txtVehicleNumber.getText().trim();
        String model = txtModelName.getText().trim();
        String type = (String) comboVehicleType.getSelectedItem();
        String priceStr = txtPricePerDay.getText().trim();
        String status = (String) comboStatus.getSelectedItem();

        if (ValidationUtil.isEmpty(number) || ValidationUtil.isEmpty(model) || ValidationUtil.isEmpty(priceStr)) {
            JOptionPane.showMessageDialog(this, "Please fill in all required vehicle details.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price per day must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number (e.g. 1500.00).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (vehicleDAO.isVehicleNumberTaken(number, 0)) {
            JOptionPane.showMessageDialog(this, "Vehicle number '" + number + "' already exists in database.", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehicle newVehicle = new Vehicle(number, model, type, price, status);
        boolean success = vehicleDAO.addVehicle(newVehicle);

        if (success) {
            JOptionPane.showMessageDialog(this, "Vehicle added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearVehicleForm();
            loadVehicleData();
            loadAnalyticsData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add vehicle due to a database error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateVehicle() {
        String idStr = txtVehicleId.getText().trim();
        if (ValidationUtil.isEmpty(idStr)) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int vehicleId = Integer.parseInt(idStr);
        String number = txtVehicleNumber.getText().trim();
        String model = txtModelName.getText().trim();
        String type = (String) comboVehicleType.getSelectedItem();
        String priceStr = txtPricePerDay.getText().trim();
        String status = (String) comboStatus.getSelectedItem();

        if (ValidationUtil.isEmpty(number) || ValidationUtil.isEmpty(model) || ValidationUtil.isEmpty(priceStr)) {
            JOptionPane.showMessageDialog(this, "Please fill in all vehicle details.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price per day must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (vehicleDAO.isVehicleNumberTaken(number, vehicleId)) {
            JOptionPane.showMessageDialog(this, "Vehicle number '" + number + "' already exists for another vehicle.", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehicle vehicle = new Vehicle(vehicleId, number, model, type, price, status);
        boolean success = vehicleDAO.updateVehicle(vehicle);

        if (success) {
            JOptionPane.showMessageDialog(this, "Vehicle updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearVehicleForm();
            loadVehicleData();
            loadAnalyticsData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update vehicle.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteVehicle() {
        String idStr = txtVehicleId.getText().trim();
        if (ValidationUtil.isEmpty(idStr)) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int vehicleId = Integer.parseInt(idStr);
        String modelName = txtModelName.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete vehicle: " + modelName + " (ID: " + vehicleId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = vehicleDAO.deleteVehicle(vehicleId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Vehicle deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearVehicleForm();
                loadVehicleData();
                loadAnalyticsData();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete vehicle. It may be linked to existing bookings.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
