Vehicle Rental Management System

A desktop-based Vehicle Rental Management System developed for BCA 5th Semester PBL (Project Based Learning). The application is built using Java Swing for the graphical user interface, JDBC for database connectivity, and MySQL for persistent data storage.

Project Overview

This project provides an automated desktop solution for renting vehicles (cars and bikes). It replaces manual record-keeping by maintaining vehicle catalogs, customer accounts, and rental booking transactions in a relational database.

The project is structured using a standard layered architecture:

model: Plain Java objects (POJOs) representing entities with private attributes and getters/setters.
dao: Data Access Objects executing SQL queries with PreparedStatements and managing transactions.
ui: Java Swing windows and event handling components.
util: Helper classes for database connection, input validation, custom button styling, and image scaling.
Phase-II Implemented Modules

Authentication & User Management

Role-based login for Admin and Customer accounts.
Customer signup form with validation for email format, 10-digit phone number, and password matching.
Prevents duplicate username and email registrations.

Vehicle Fleet Management (Admin)

View complete vehicle inventory in a JTable.
Add new vehicles with vehicle number, model name, type, and daily rent rate.
Update vehicle specifications and toggle status between AVAILABLE and BOOKED.
Delete vehicles with confirmation prompts.
Live photo preview when selecting a vehicle from the fleet table.

Customer Record & History Tracking (Admin)

View all registered customers.
Interactive rental ledger showing individual customer booking history, duration, and total amount spent.

Vehicle Booking Engine (Customer)

Filtered catalog showing only available vehicles.
Dynamic rental cost calculation: Total Fare = Rental Days × Daily Price.
Atomic database transactions: Reserving a vehicle automatically updates its availability to BOOKED in MySQL.
Built-in date validation preventing invalid booking periods.

Customer Booking Ledger

Customers can inspect their active and completed rental bookings.
Summary card displaying the booked vehicle's image, duration, dates, and confirmed status.
Technology Stack
Programming Language: Java (JDK 21 / Java SE)
GUI Toolkit: Java Swing & AWT
Database: MySQL (via XAMPP / MariaDB)
Database Connector: MySQL Connector/J (JDBC)
Architecture: Layered MVC Pattern
Database Design

The database vehicle_rental_db consists of four normalized tables:

users: Stores login credentials and role definitions (ADMIN / CUSTOMER).
customers: Stores personal profile details linked to users.
vehicles: Maintains vehicle catalog, rent rates, and availability status.
bookings: Records rental transactions with date intervals, duration, and calculated fare.

The database initialization script is located at: database/vehicle_rental.sql.

How to Run the Project
Prerequisites
Java Development Kit (JDK 17 or higher)
MySQL Server (XAMPP or standalone MySQL)

Steps
Start MySQL from XAMPP Control Panel.
Import database/vehicle_rental.sql into phpMyAdmin or MySQL client.
Open terminal in the project root directory and run:

# Compile source files
javac -cp "lib/*" -d bin src/model/*.java src/util/*.java src/dao/*.java src/ui/*.java src/Main.java
# Run application
java -cp "bin;lib/*" Main

Future Scope (Final Phase)
Online payment gateway integration
Automated PDF invoice export
Overdue return calculation and late fees
Graphical revenue analytics on Admin Dashboard
