# DriveEasy - Vehicle Rental Management System
### BCA 5th Semester PBL (Project Based Learning) — Final Phase (100% Complete)

**DriveEasy** is a full-featured desktop Vehicle Rental Management System engineered using **Core Java (Swing & AWT)**, **JDBC**, and **MySQL**. It provides an automated, secure, and end-to-end rental lifecycle: from vehicle catalog browsing and live search to simulated payment checkout, booking management, return inspection with automated late-fee penalty calculation, downloadable/printable tax invoices, and visual business analytics.

---

## 🌟 Key Features & Modules

### 1. Authentication & Security
- **Role-Based Access Control (RBAC):** Distinct routing and permissions for `ADMIN` and `CUSTOMER`.
- **Customer Registration:** Validates full name, email regex, 10-digit Indian phone number, and password matching.
- **Brand Identity:** DriveEasy logo branding, modern color palette, and Enter-key quick submission shortcuts.

### 2. Live Fleet Search & Filtering
- **Customer Catalog:** Real-time search by model/keyword, category filter (`All`, `Car`, `Bike`, `SUV`, `Sedan`, `Hatchback`), and live photo thumbnails.
- **Admin Fleet Directory:** Multi-criteria search and filter by keyword, category, and real-time status (`AVAILABLE` vs `BOOKED`).

### 3. Payment Checkout Simulation (`PaymentDialog`)
- Multi-channel simulated payment gateway:
  - **UPI / QR Code:** Dynamic QR preview with simulated merchant handle `driveeasy@upi` and Transaction ID generator.
  - **Debit / Credit Card:** 16-digit card number validation, MM/YY expiry check, and 3-digit CVV validation.
  - **Cash on Counter:** Verification check for in-person branch pickup.
- Automatically generates unique transaction IDs (`TXN_...`) and marks bookings as `PAID`.

### 4. Vehicle Return & Late-Fee Penalty Engine (`ReturnVehicleDialog`)
- **Inspection & Return Workflow:** Admin and customers can inspect and close active rentals.
- **Automated Penalty Calculation:** Evaluates `ChronoUnit.DAYS` between scheduled return date and actual return date.
  - On-time / early returns: Zero penalty (`₹0.00`).
  - Overdue returns: Flat penalty rate of **₹500.00 / day** overdue.
- **Inventory Synchronization:** Updates booking status to `RETURNED`, records late fee, and frees the vehicle back to `AVAILABLE` inside an atomic MySQL transaction.

### 5. Booking Cancellation Flow
- Customers and Admins can cancel active `CONFIRMED` bookings with one click.
- Restores vehicle status back to `AVAILABLE` immediately, updating booking status to `CANCELLED`.

### 6. Automated & Printable Tax Invoices (`InvoiceDialog`)
- Generates official GST-compliant tax invoices rendered in formatted HTML with:
  - Unique Invoice & Booking reference numbers.
  - Customer profile, contact, and billing address.
  - Vehicle details and rental period breakdown.
  - GST calculation (CGST 9% + SGST 9% = 18% total split).
  - Itemized late fees (if returned overdue) and Grand Total.
- **Save Invoice to File:** Exports invoice directly to text/HTML file on disk.
- **Direct Print Integration:** Hooks directly into native OS printing dialog via `JEditorPane.print()`.

### 7. Business Analytics & Revenue Dashboard (`RevenueChartPanel`)
- **Live KPI Metrics Cards:**
  - 💰 **Total Revenue (₹)**
  - 📋 **Total Bookings**
  - 🚗 **Active Rentals**
  - 🟢 **Available Fleet**
- **Secondary Metrics Banner:** Completed trips, cancellations count, and total late-fee penalty recovered.
- **Pure Java 2D Graphics Bar Chart (`Graphics2D`):**
  - Category-wise revenue breakdown (Car, SUV, Bike, Sedan, Hatchback).
  - Proportional grid lines, dynamic Y-axis scaling, and antialiased rendering without any heavy external charting libraries.

---

## 🏗️ Layered Architecture (MVC)

```
vehicle-rental-system/
├── assets/
│   ├── drive_easy_logo.jpg
│   └── images/
│       ├── honda_city.jpg
│       ├── toyota_innova.jpg
│       ├── royal_enfield.jpg
│       ├── hyundai_creta.jpg
│       └── mahindra_thar.jpg
├── database/
│   └── vehicle_rental.sql
├── lib/
│   └── mysql-connector-j-9.4.0.jar
├── src/
│   ├── Main.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Customer.java
│   │   ├── Vehicle.java
│   │   └── Booking.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── CustomerDAO.java
│   │   ├── VehicleDAO.java
│   │   └── BookingDAO.java
│   ├── ui/
│   │   ├── LoginFrame.java
│   │   ├── RegisterFrame.java
│   │   ├── CustomerDashboard.java
│   │   ├── AdminDashboard.java
│   │   ├── PaymentDialog.java
│   │   ├── ReturnVehicleDialog.java
│   │   ├── InvoiceDialog.java
│   │   └── RevenueChartPanel.java
│   └── util/
│       ├── DBConnection.java
│       ├── ValidationUtil.java
│       ├── UITheme.java
│       └── ImageUtil.java
└── README.md
```

---

## 🗄️ Database Design (`vehicle_rental_db`)

The relational database is fully normalized (3NF) and contains 4 primary tables:

1. **`users`**: Login credentials and role authorization (`ADMIN` or `CUSTOMER`).
2. **`customers`**: Customer contact details, phone, email, and address (`user_id` foreign key).
3. **`vehicles`**: Vehicle catalog with vehicle number, model, category, daily rental price, and status (`AVAILABLE`, `BOOKED`).
4. **`bookings`**:
   - `booking_id` (PK, AUTO_INCREMENT)
   - `customer_id`, `vehicle_id` (FKs)
   - `start_date`, `end_date`, `total_days`, `total_amount`
   - `booking_status` (`CONFIRMED`, `RETURNED`, `CANCELLED`)
   - `payment_method`, `payment_status`, `transaction_id`
   - `actual_return_date`, `late_fee`
   - `booking_date` (TIMESTAMP)

---

## 🚀 How to Run the Project

### Prerequisites
1. **Java Development Kit (JDK 17 or higher)**
2. **MySQL Server (XAMPP MariaDB or standalone MySQL)**

### Step 1: Start MySQL Database
- Open XAMPP Control Panel and start **MySQL** (default port 3306).
- Database connection defaults to `127.0.0.1:3306/vehicle_rental_db`, username `root`, password `""` (empty).
- If needed, verify the database using phpMyAdmin: [http://localhost/phpmyadmin](http://localhost/phpmyadmin).

### Step 2: Compile Source Code
Open PowerShell or Command Prompt in the project root directory:

```powershell
javac -cp "lib/*" -d bin src/model/*.java src/util/*.java src/dao/*.java src/ui/*.java src/Main.java
```

### Step 3: Run the Application
```powershell
java -cp "bin;lib/*" Main
```

---

## 🔑 Default Login Credentials

| Role | Username | Password |
|---|---|---|
| **Admin** | `admin` | `admin123` |
| **Customer 1** | `rahul_sharma` | `password123` |
| **Customer 2** | `saksham` | `password123` |
| **New Customer** | Click *"Create New Account"* on Login Screen |

---

## 👨‍💻 Academic Context
- **Course:** Bachelor of Computer Applications (BCA)
- **Semester:** 5th Semester
- **Curriculum:** Project Based Learning (PBL) Final Evaluation
- **Design Principles:** Object-Oriented Programming (OOP), MVC Layering, ACID Database Transactions, Clean Exception Handling.
