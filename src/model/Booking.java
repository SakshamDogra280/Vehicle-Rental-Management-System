package model;

import java.time.LocalDate;

/**
 * Booking Model Class
 * Represents a vehicle rental transaction in DriveEasy.
 * Final Phase: Supports payment details, actual return tracking, and late fee penalties.
 * Demonstrates: OOP Encapsulation, Date manipulation, Domain Modeling.
 */
public class Booking {
    private int bookingId;
    private int customerId;
    private int vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private double totalAmount;
    private String bookingStatus; // 'CONFIRMED', 'CANCELLED', 'RETURNED'
    private String bookingDate;

    // Final Phase Attributes: Payments & Returns
    private String paymentMethod;    // 'UPI', 'Debit/Credit Card', 'Cash'
    private String paymentStatus;    // 'PAID', 'PENDING'
    private String transactionId;    // e.g. 'TXN-20261104-XXXX'
    private LocalDate actualReturnDate;
    private double lateFee;

    // Display fields for easy JTable rendering
    private String vehicleName;
    private String vehicleNumber;
    private String vehicleType;
    private double pricePerDay;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Default constructor
    public Booking() {
        this.lateFee = 0.0;
        this.paymentStatus = "PAID";
        this.paymentMethod = "Cash";
    }

    // Parameterized constructor for creating a new booking with payment info
    public Booking(int customerId, int vehicleId, LocalDate startDate, LocalDate endDate,
                   int totalDays, double totalAmount, String bookingStatus,
                   String paymentMethod, String paymentStatus, String transactionId) {
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
        this.lateFee = 0.0;
    }

    // Full constructor
    public Booking(int bookingId, int customerId, int vehicleId, LocalDate startDate, LocalDate endDate,
                   int totalDays, double totalAmount, String bookingStatus, String bookingDate,
                   String paymentMethod, String paymentStatus, String transactionId,
                   LocalDate actualReturnDate, double lateFee) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
        this.bookingDate = bookingDate;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
        this.actualReturnDate = actualReturnDate;
        this.lateFee = lateFee;
    }

    // Grand total including any late fee penalties
    public double getGrandTotal() {
        return totalAmount + lateFee;
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public double getLateFee() {
        return lateFee;
    }

    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}
