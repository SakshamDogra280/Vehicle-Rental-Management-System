package model;

/**
 * Vehicle Model Class
 * Represents a vehicle in the rental fleet.
 * Demonstrates: OOP Encapsulation with private fields and standard getters/setters.
 */
public class Vehicle {
    private int vehicleId;
    private String vehicleNumber;
    private String modelName;
    private String vehicleType;     // e.g. Car, Bike, SUV
    private double pricePerDay;
    private String status;          // 'AVAILABLE' or 'BOOKED'

    // Default constructor
    public Vehicle() {
    }

    // Parameterized constructor with all fields
    public Vehicle(int vehicleId, String vehicleNumber, String modelName, String vehicleType, double pricePerDay, String status) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.modelName = modelName;
        this.vehicleType = vehicleType;
        this.pricePerDay = pricePerDay;
        this.status = status;
    }

    // Constructor without vehicleId (used when adding a new vehicle)
    public Vehicle(String vehicleNumber, String modelName, String vehicleType, double pricePerDay, String status) {
        this.vehicleNumber = vehicleNumber;
        this.modelName = modelName;
        this.vehicleType = vehicleType;
        this.pricePerDay = pricePerDay;
        this.status = status;
    }

    // Getters and Setters
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return modelName + " (" + vehicleNumber + ") - ₹" + pricePerDay + "/day";
    }
}
