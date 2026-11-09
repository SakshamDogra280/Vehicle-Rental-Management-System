package dao;

import model.Booking;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BookingDAO Class
 * Handles database operations for vehicle rentals, payments, returns, and analytics.
 * Final Phase: Atomic Transactions, Overdue Penalty calculation, Booking Cancellation, and Reporting.
 * Demonstrates: SQL JOINs, Transactions, PreparedStatements, Aggregate functions.
 */
public class BookingDAO {

    /**
     * Creates a new booking with payment status and marks the vehicle as 'BOOKED'.
     * Atomic transaction ensuring data integrity.
     */
    public boolean createBooking(Booking booking) {
        String insertBookingSql = "INSERT INTO bookings " +
                "(customer_id, vehicle_id, start_date, end_date, total_days, total_amount, " +
                "booking_status, payment_method, payment_status, transaction_id, late_fee) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED', ?, ?, ?, 0.00)";
        String updateVehicleSql = "UPDATE vehicles SET status = 'BOOKED' WHERE vehicle_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Insert booking
            try (PreparedStatement stmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, booking.getCustomerId());
                stmt.setInt(2, booking.getVehicleId());
                stmt.setDate(3, Date.valueOf(booking.getStartDate()));
                stmt.setDate(4, Date.valueOf(booking.getEndDate()));
                stmt.setInt(5, booking.getTotalDays());
                stmt.setDouble(6, booking.getTotalAmount());
                stmt.setString(7, booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "Cash");
                stmt.setString(8, booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "PAID");
                stmt.setString(9, booking.getTransactionId());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        booking.setBookingId(rs.getInt(1));
                    }
                }
            }

            // 2. Mark vehicle as BOOKED
            try (PreparedStatement stmt = conn.prepareStatement(updateVehicleSql)) {
                stmt.setInt(1, booking.getVehicleId());
                stmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            System.err.println("Transaction failed while creating booking: " + e.getMessage());
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

    /**
     * Processes vehicle return.
     * Calculates late penalty if actual return date is after scheduled end date,
     * updates booking status to 'RETURNED', and frees the vehicle back to 'AVAILABLE'.
     * Atomic transaction!
     */
    public boolean returnVehicle(int bookingId, int vehicleId, LocalDate actualReturnDate, double lateFee) {
        String updateBookingSql = "UPDATE bookings SET actual_return_date = ?, late_fee = ?, booking_status = 'RETURNED' WHERE booking_id = ?";
        String updateVehicleSql = "UPDATE vehicles SET status = 'AVAILABLE' WHERE vehicle_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Update booking
            try (PreparedStatement stmt = conn.prepareStatement(updateBookingSql)) {
                stmt.setDate(1, Date.valueOf(actualReturnDate));
                stmt.setDouble(2, lateFee);
                stmt.setInt(3, bookingId);
                stmt.executeUpdate();
            }

            // 2. Mark vehicle back as AVAILABLE
            try (PreparedStatement stmt = conn.prepareStatement(updateVehicleSql)) {
                stmt.setInt(1, vehicleId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Transaction failed during vehicle return: " + e.getMessage());
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

    /**
     * Cancels an active booking and restores the vehicle availability back to 'AVAILABLE'.
     * Atomic transaction!
     */
    public boolean cancelBooking(int bookingId, int vehicleId) {
        String updateBookingSql = "UPDATE bookings SET booking_status = 'CANCELLED' WHERE booking_id = ?";
        String updateVehicleSql = "UPDATE vehicles SET status = 'AVAILABLE' WHERE vehicle_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Update booking status
            try (PreparedStatement stmt = conn.prepareStatement(updateBookingSql)) {
                stmt.setInt(1, bookingId);
                stmt.executeUpdate();
            }

            // 2. Free vehicle back to AVAILABLE
            try (PreparedStatement stmt = conn.prepareStatement(updateVehicleSql)) {
                stmt.setInt(1, vehicleId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Transaction failed during booking cancellation: " + e.getMessage());
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

    /**
     * Retrieves a single booking by ID with full customer and vehicle details (ideal for invoice).
     */
    public Booking getBookingById(int bookingId) {
        String query = "SELECT b.*, v.model_name, v.vehicle_number, v.vehicle_type, v.price_per_day, " +
                "c.full_name, c.phone, c.email " +
                "FROM bookings b " +
                "JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "WHERE b.booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractFullBooking(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all bookings for a specific customer (used in Customer Dashboard).
     */
    public List<Booking> getBookingsByCustomerId(int customerId) {
        List<Booking> list = new ArrayList<>();
        String query = "SELECT b.*, v.model_name, v.vehicle_number, v.vehicle_type, v.price_per_day, " +
                "c.full_name, c.phone, c.email " +
                "FROM bookings b " +
                "JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "WHERE b.customer_id = ? " +
                "ORDER BY b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFullBooking(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all bookings across all customers (used in Admin Dashboard).
     */
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String query = "SELECT b.*, v.model_name, v.vehicle_number, v.vehicle_type, v.price_per_day, " +
                "c.full_name, c.phone, c.email " +
                "FROM bookings b " +
                "JOIN vehicles v ON b.vehicle_id = v.vehicle_id " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "ORDER BY b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractFullBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns aggregate analytics data for Admin charts and KPI cards.
     */
    public Map<String, Object> getAnalyticsSummary() {
        Map<String, Object> summary = new HashMap<>();

        String kpiSql = "SELECT " +
                "COALESCE(SUM(CASE WHEN booking_status != 'CANCELLED' THEN total_amount + late_fee ELSE 0 END), 0) AS total_revenue, " +
                "COUNT(*) AS total_bookings, " +
                "COALESCE(SUM(CASE WHEN booking_status = 'CONFIRMED' THEN 1 ELSE 0 END), 0) AS active_rentals, " +
                "COALESCE(SUM(CASE WHEN booking_status = 'RETURNED' THEN 1 ELSE 0 END), 0) AS completed_rentals, " +
                "COALESCE(SUM(CASE WHEN booking_status = 'CANCELLED' THEN 1 ELSE 0 END), 0) AS cancelled_rentals, " +
                "COALESCE(SUM(late_fee), 0) AS total_late_fees " +
                "FROM bookings";

        String typeRevenueSql = "SELECT v.vehicle_type, " +
                "COALESCE(SUM(b.total_amount + b.late_fee), 0) AS revenue, " +
                "COUNT(b.booking_id) AS booking_count " +
                "FROM vehicles v " +
                "LEFT JOIN bookings b ON v.vehicle_id = b.vehicle_id AND b.booking_status != 'CANCELLED' " +
                "GROUP BY v.vehicle_type";

        try (Connection conn = DBConnection.getConnection()) {
            // KPI metrics
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(kpiSql)) {
                if (rs.next()) {
                    summary.put("totalRevenue", rs.getDouble("total_revenue"));
                    summary.put("totalBookings", rs.getInt("total_bookings"));
                    summary.put("activeRentals", rs.getInt("active_rentals"));
                    summary.put("completedRentals", rs.getInt("completed_rentals"));
                    summary.put("cancelledRentals", rs.getInt("cancelled_rentals"));
                    summary.put("totalLateFees", rs.getDouble("total_late_fees"));
                }
            }

            // Breakdown by vehicle type for chart visualization
            Map<String, Double> typeRevenue = new HashMap<>();
            Map<String, Integer> typeBookings = new HashMap<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(typeRevenueSql)) {
                while (rs.next()) {
                    String type = rs.getString("vehicle_type");
                    typeRevenue.put(type, rs.getDouble("revenue"));
                    typeBookings.put(type, rs.getInt("booking_count"));
                }
            }
            summary.put("revenueByType", typeRevenue);
            summary.put("bookingsByType", typeBookings);

        } catch (SQLException e) {
            System.err.println("Error fetching analytics summary: " + e.getMessage());
            e.printStackTrace();
        }

        return summary;
    }

    // Helper mapper
    private Booking extractFullBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setVehicleId(rs.getInt("vehicle_id"));
        b.setStartDate(rs.getDate("start_date").toLocalDate());
        b.setEndDate(rs.getDate("end_date").toLocalDate());
        b.setTotalDays(rs.getInt("total_days"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setBookingDate(rs.getString("booking_date"));

        // Final Phase Columns
        b.setPaymentMethod(rs.getString("payment_method"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setTransactionId(rs.getString("transaction_id"));
        Date actualDate = rs.getDate("actual_return_date");
        if (actualDate != null) {
            b.setActualReturnDate(actualDate.toLocalDate());
        }
        b.setLateFee(rs.getDouble("late_fee"));

        // Joined display fields
        b.setVehicleName(rs.getString("model_name"));
        b.setVehicleNumber(rs.getString("vehicle_number"));
        b.setVehicleType(rs.getString("vehicle_type"));
        b.setPricePerDay(rs.getDouble("price_per_day"));
        b.setCustomerName(rs.getString("full_name"));
        b.setCustomerPhone(rs.getString("phone"));
        b.setCustomerEmail(rs.getString("email"));

        return b;
    }
}
