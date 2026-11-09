package dao;

import model.Vehicle;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * VehicleDAO Class
 * Handles database CRUD operations for Vehicles.
 * Demonstrates: JDBC PreparedStatement, CRUD operations, List Collections, Exception Handling.
 */
public class VehicleDAO {

    /**
     * Adds a new vehicle to the database.
     */
    public boolean addVehicle(Vehicle vehicle) {
        String query = "INSERT INTO vehicles (vehicle_number, model_name, vehicle_type, price_per_day, status) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, vehicle.getVehicleNumber());
            stmt.setString(2, vehicle.getModelName());
            stmt.setString(3, vehicle.getVehicleType());
            stmt.setDouble(4, vehicle.getPricePerDay());
            stmt.setString(5, vehicle.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding vehicle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all vehicles for Admin management table.
     */
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        String query = "SELECT vehicle_id, vehicle_number, model_name, vehicle_type, price_per_day, status FROM vehicles ORDER BY vehicle_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractVehicleFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves only AVAILABLE vehicles for customer booking.
     */
    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> list = new ArrayList<>();
        String query = "SELECT vehicle_id, vehicle_number, model_name, vehicle_type, price_per_day, status FROM vehicles WHERE status = 'AVAILABLE' ORDER BY vehicle_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(extractVehicleFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves a single vehicle by its ID.
     */
    public Vehicle getVehicleById(int vehicleId) {
        String query = "SELECT vehicle_id, vehicle_number, model_name, vehicle_type, price_per_day, status FROM vehicles WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractVehicleFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching vehicle by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates vehicle details.
     */
    public boolean updateVehicle(Vehicle vehicle) {
        String query = "UPDATE vehicles SET vehicle_number = ?, model_name = ?, vehicle_type = ?, price_per_day = ?, status = ? WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, vehicle.getVehicleNumber());
            stmt.setString(2, vehicle.getModelName());
            stmt.setString(3, vehicle.getVehicleType());
            stmt.setDouble(4, vehicle.getPricePerDay());
            stmt.setString(5, vehicle.getStatus());
            stmt.setInt(6, vehicle.getVehicleId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates only vehicle status ('AVAILABLE' or 'BOOKED').
     */
    public boolean updateStatus(int vehicleId, String status) {
        String query = "UPDATE vehicles SET status = ? WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, vehicleId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating vehicle status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a vehicle by ID.
     */
    public boolean deleteVehicle(int vehicleId) {
        String query = "DELETE FROM vehicles WHERE vehicle_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, vehicleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting vehicle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a vehicle number is already in use by another vehicle.
     */
    public boolean isVehicleNumberTaken(String vehicleNumber, int excludeVehicleId) {
        String query = "SELECT vehicle_id FROM vehicles WHERE vehicle_number = ? AND vehicle_id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, vehicleNumber);
            stmt.setInt(2, excludeVehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Filters all vehicles by keyword, vehicle type, and status (used by Admin Fleet Search).
     */
    public List<Vehicle> searchAllVehicles(String keyword, String vehicleType, String status) {
        List<Vehicle> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT vehicle_id, vehicle_number, model_name, vehicle_type, price_per_day, status FROM vehicles WHERE 1=1");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(model_name) LIKE ? OR LOWER(vehicle_number) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (vehicleType != null && !vehicleType.equalsIgnoreCase("All") && !vehicleType.trim().isEmpty()) {
            sql.append(" AND vehicle_type = ?");
            params.add(vehicleType.trim());
        }
        if (status != null && !status.equalsIgnoreCase("All") && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY vehicle_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractVehicleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching all vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Filters available vehicles by keyword and vehicle type.
     */
    public List<Vehicle> searchAvailableVehicles(String keyword, String vehicleType) {
        List<Vehicle> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT vehicle_id, vehicle_number, model_name, vehicle_type, price_per_day, status FROM vehicles WHERE status = 'AVAILABLE'");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(model_name) LIKE ? OR LOWER(vehicle_number) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (vehicleType != null && !vehicleType.equalsIgnoreCase("All") && !vehicleType.trim().isEmpty()) {
            sql.append(" AND vehicle_type = ?");
            params.add(vehicleType.trim());
        }
        sql.append(" ORDER BY vehicle_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractVehicleFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching available vehicles: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // Helper method to map ResultSet row to Vehicle object
    private Vehicle extractVehicleFromResultSet(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(rs.getInt("vehicle_id"));
        vehicle.setVehicleNumber(rs.getString("vehicle_number"));
        vehicle.setModelName(rs.getString("model_name"));
        vehicle.setVehicleType(rs.getString("vehicle_type"));
        vehicle.setPricePerDay(rs.getDouble("price_per_day"));
        vehicle.setStatus(rs.getString("status"));
        return vehicle;
    }
}
