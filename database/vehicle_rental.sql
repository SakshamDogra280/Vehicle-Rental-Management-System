
-- ==========================================================

CREATE DATABASE IF NOT EXISTS vehicle_rental_db;
USE vehicle_rental_db;

-- 1. Table: users (Stores Login Credentials & Roles)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN' or 'CUSTOMER'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Table: customers (Stores Customer Profile Details)
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    address VARCHAR(255),
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Table: vehicles (Stores Fleet Information)
CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) NOT NULL UNIQUE,
    model_name VARCHAR(100) NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL, -- 'Car', 'Bike', 'SUV'
    price_per_day DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE' -- 'AVAILABLE' or 'BOOKED'
);

-- 4. Table: bookings (Stores Rental Booking Transactions)
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    booking_status VARCHAR(20) DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'CANCELLED', 'RETURNED'
    payment_method VARCHAR(30) DEFAULT 'Cash', -- 'UPI', 'Debit/Credit Card', 'Cash'
    payment_status VARCHAR(20) DEFAULT 'PAID', -- 'PAID', 'PENDING'
    transaction_id VARCHAR(50) DEFAULT NULL,
    actual_return_date DATE DEFAULT NULL,
    late_fee DECIMAL(10, 2) DEFAULT 0.00,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_vehicle FOREIGN KEY (vehicle_id) 
        REFERENCES vehicles(vehicle_id) ON DELETE CASCADE
);

-- ==========================================================
-- Sample Seed Data for Evaluation & Testing
-- ==========================================================

-- Insert Default Admin (Password: admin123)
INSERT IGNORE INTO users (user_id, username, password, role) 
VALUES (1, 'admin', 'admin123', 'ADMIN');

-- Insert Sample Customer User (Password: rahul123)
INSERT IGNORE INTO users (user_id, username, password, role) 
VALUES (2, 'rahul_sharma', 'rahul123', 'CUSTOMER');

-- Insert Sample Customer Profile linked to user_id = 2
INSERT IGNORE INTO customers (customer_id, user_id, full_name, email, phone, address) 
VALUES (1, 2, 'Rahul Sharma', 'rahul@example.com', '9876543210', 'Delhi, India');

-- Insert Sample Vehicles
INSERT IGNORE INTO vehicles (vehicle_id, vehicle_number, model_name, vehicle_type, price_per_day, status) VALUES
(1, 'DL-01-AB-1234', 'Honda City', 'Car', 1800.00, 'AVAILABLE'),
(2, 'HR-26-CD-5678', 'Toyota Innova Crysta', 'SUV', 2800.00, 'AVAILABLE'),
(3, 'UP-16-EF-9012', 'Royal Enfield Classic 350', 'Bike', 800.00, 'AVAILABLE'),
(4, 'DL-04-GH-3456', 'Hyundai Creta', 'SUV', 2200.00, 'AVAILABLE');

-- Insert 1 Sample Booking for demonstrating booking history
INSERT IGNORE INTO bookings (booking_id, customer_id, vehicle_id, start_date, end_date, total_days, total_amount, booking_status)
VALUES (1, 1, 1, '2026-10-01', '2026-10-04', 3, 5400.00, 'CONFIRMED');
