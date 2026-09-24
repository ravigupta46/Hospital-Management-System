# 🏥 Hospital Management System (Java + JDBC + MySQL)

A robust, console-based **Hospital Management System** engineered in Java using raw JDBC and MySQL. This project demonstrates clean Object-Oriented Programming (OOP), secure database connectivity, transactional appointment scheduling, and relational SQL queries using JOIN.

---

## 📋 Table of Contents
- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture & Design](#-architecture--design)
- [Project Structure](#-project-structure)
- [Database Schema & ER Design](#-database-schema--er-design)
- [Security & Best Practices](#-security--best-practices)
- [Getting Started & Setup](#-getting-started--setup)
- [Application Walkthrough & Console UI](#-application-walkthrough--console-ui)
- [Future Improvements](#-future-improvements)
- [Author](#-author)

---

## 🌟 Overview

The Hospital Management System manages hospital patient records, doctor directories, and transactional appointment bookings. It is designed to be beginner-friendly yet architecturally sound, adhering to the **Single Responsibility Principle (SRP)**, **Defensive Input Validation**, and **Enterprise Database Constraints** without unnecessary third-party frameworks.

---

## 🚀 Key Features

* **Patient Management:**
  * Register new patients with validation (non-empty name, realistic age between 1–125, valid gender).
  * Auto-generated Patient ID from MySQL AUTO_INCREMENT.
  * View all registered patients in a clean ASCII table.
  * Search patient details by ID.

* **Doctor Directory:**
  * View all available doctors and their specializations.
  * Search doctor details by ID.
  * Preloaded with realistic sample specialist data.

* **Appointment Scheduling (Core Feature):**
  * **Interactive Booking:** Links verified Patient ID and Doctor ID.
  * **Strict Date & Time Validation:** Utilizes java.time.LocalDate and LocalTime to reject past dates and past times for today.
  * **Availability Verification:** Checks if the doctor already has an active BOOKED slot.
  * **Transaction Safety:** Managed with connection.setAutoCommit(false), commit(), and ollback().
  * **Database-Level Protection:** Backed by a UNIQUE(doctor_id, appointment_date, appointment_time) constraint against race conditions.

* **Appointment Lifecycle Management:**
  * **View All Appointments:** Leverages a 3-table **SQL JOIN** (ppointments, patients, doctors).
  * **Cancel Appointment:** Validates state transitions (BOOKED $\rightarrow$ CANCELLED). Prevents cancelling completed appointments.
  * **Complete Appointment:** Validates state transitions (BOOKED $\rightarrow$ COMPLETED). Prevents completing cancelled appointments.
  * **History Preservation:** Retains cancelled and completed appointments for auditing.

* **Defensive Console UI:**
  * Robust input handling: completely immune to Scanner desynchronization and non-numeric crashes.

---

## 🏗 Architecture & Design

The application follows a simple, decoupled layered architecture without heavy framework abstractions:

`	ext
       +---------------------------------------------+
       |                  User (CLI)                 |
       +---------------------------------------------+
                              |
                              v
       +---------------------------------------------+
       |   HospitalManagementSystem (Main & Menu)    |
       +---------------------------------------------+
               |              |              |
               v              v              v
       +---------------+ +----------+ +---------------+
       |    Patient    | |  Doctor  | |  Appointment  |
       +---------------+ +----------+ +---------------+
               \              |              /
                \             |             /
                 v            v            v
       +---------------------------------------------+
       |         DatabaseConnection (Security)       |
       +---------------------------------------------+
                              |
                              v
       +---------------------------------------------+
       |               JDBC Driver API               |
       +---------------------------------------------+
                              |
                              v
       +---------------------------------------------+
       |             MySQL Database Engine           |
       +---------------------------------------------+
`

---

## 📂 Project Structure

`	ext
Hospital-Management-System/
├── .gitignore                                 # Git ignore file (excludes build & env files)
├── hospital.sql                               # Full MySQL schema, constraints & sample data
├── README.md                                  # Complete project documentation
├── Hospital ManagementSystem.iml              # IDE configuration
└── src/
    └── HospitalManagementSystem/
        ├── Main.java                          # Clean application entry point
        ├── DatabaseConnection.java            # Environment-based JDBC connection manager
        ├── Patient.java                       # Patient registration, validation & queries
        ├── Doctor.java                        # Doctor directory & lookup operations
        ├── Appointment.java                   # Booking, state transitions, transactions & JOINs
        └── HospitalManagementSystem.java       # Menu controller & user input orchestration
`

---

## 🗄 Database Schema & ER Design

### Entity-Relationship (ER) Model
`	ext
  +-------------+                 +------------------+                 +------------+
  |   PATIENT   | 1 ----------- N |   APPOINTMENT    | N ----------- 1 |   DOCTOR   |
  +-------------+                 +------------------+                 +------------+
  | id (PK)     |                 | id (PK)          |                 | id (PK)    |
  | name        |                 | patient_id (FK)  |                 | name       |
  | age         |                 | doctor_id (FK)   |                 | specialty  |
  | gender      |                 | appointment_date |                 +------------+
  +-------------+                 | appointment_time |
                                  | status           |
                                  | created_at       |
                                  +------------------+
`

### Table Definitions (hospital.sql)

1. **patients**
   * id INT AUTO_INCREMENT PRIMARY KEY
   * 
ame VARCHAR(100) NOT NULL
   * ge INT NOT NULL
   * gender VARCHAR(20) NOT NULL

2. **doctors**
   * id INT AUTO_INCREMENT PRIMARY KEY
   * 
ame VARCHAR(100) NOT NULL
   * specialization VARCHAR(100) NOT NULL

3. **ppointments**
   * id INT AUTO_INCREMENT PRIMARY KEY
   * patient_id INT NOT NULL (Foreign Key referencing patients(id))
   * doctor_id INT NOT NULL (Foreign Key referencing doctors(id))
   * ppointment_date DATE NOT NULL
   * ppointment_time TIME NOT NULL
   * status VARCHAR(20) NOT NULL DEFAULT 'BOOKED' (BOOKED, COMPLETED, CANCELLED)
   * created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   * **Constraint:** UNIQUE (doctor_id, appointment_date, appointment_time) prevents double booking.

---

## 🔒 Security & Best Practices

1. **No Hardcoded Secrets:**
   Database connection credentials are never committed into version control. They are dynamically resolved via environment variables:
   * DB_URL
   * DB_USERNAME
   * DB_PASSWORD
2. **SQL Injection Defense:**
   All dynamic queries strictly utilize parameterized PreparedStatement. User input is never concatenated directly into SQL strings.
3. **Explicit Projections:**
   Columns are selected explicitly (e.g., SELECT id, name, age, gender FROM patients) rather than wildcard SELECT *.
4. **Leak-Proof Resource Management:**
   All PreparedStatement and ResultSet objects are managed inside Java **try-with-resources** blocks, guaranteeing they are immediately closed even during runtime exceptions.
5. **ACID Transaction Safety:**
   Appointment creation handles transactions manually (setAutoCommit(false), commit(), ollback()) to ensure consistent state.

---

## ⚙️ Getting Started & Setup

### 1. Prerequisites
* **Java Development Kit (JDK)**: Version 17 or higher
* **MySQL Server**: Version 8.0 or higher
* **MySQL Connector/J**: JDBC driver jar (mysql-connector-j-9.x.x.jar)

### 2. Database Setup
Open your MySQL terminal or MySQL Workbench and run:
`ash
mysql -u root -p < hospital.sql
`
This initializes the hospital database, creates all three tables with constraints, and populates sample doctors and patients.

### 3. Configure Environment Variables
Set your database credentials in your operating system environment:

* **Windows (PowerShell):**
  `powershell
  $env:DB_URL="jdbc:mysql://localhost:3306/hospital"
  $env:DB_USERNAME="root"
  $env:DB_PASSWORD="your_mysql_password"
  `

* **Linux / macOS:**
  `ash
  export DB_URL="jdbc:mysql://localhost:3306/hospital"
  export DB_USERNAME="root"
  export DB_PASSWORD="your_mysql_password"
  `

*(If variables are omitted, default local configuration jdbc:mysql://localhost:3306/hospital with user oot is used).*

### 4. Compile and Run
Compile all source files:
`ash
javac -cp "lib/mysql-connector-j-9.5.0.jar;src" -d out src/HospitalManagementSystem/*.java
`
Run the application:
`ash
java -cp "lib/mysql-connector-j-9.5.0.jar;out" HospitalManagementSystem.Main
`
*(Or simply open the project in **IntelliJ IDEA / Eclipse**, ensure the MySQL connector library is added to module dependencies, and execute Main.java)*.

---

## 🖥 Application Walkthrough & Console UI

### 1. Main Menu
`	ext
==================================================
        STARTING HOSPITAL MANAGEMENT SYSTEM       
==================================================
[✓] Connected to MySQL database successfully.

==================================================
            HOSPITAL MANAGEMENT SYSTEM            
==================================================
 1. Add Patient
 2. View Patients
 3. Find Patient by ID
 4. View Doctors
 5. Find Doctor by ID
 6. Book Appointment
 7. View Appointments
 8. Cancel Appointment
 9. Complete Appointment
 10. Exit
==================================================
Enter your choice (1-10):
`

### 2. View Doctors
`	ext
==================================================================
                         AVAILABLE DOCTORS                        
==================================================================
+------------+--------------------------------+----------------------+
| Doctor Id  | Name                           | Specialization       |
+------------+--------------------------------+----------------------+
| 1          | Dr. Priya Sharma               | Cardiologist         |
| 2          | Dr. Rajesh Mehta               | Neurologist          |
| 3          | Dr. Anita Desai                | Dermatologist        |
| 4          | Dr. Vikram Patel               | Orthopedic Surgeon   |
| 5          | Dr. Sunita Rao                 | Pediatrician         |
| 6          | Dr. Arvind Nair                | General Physician    |
+------------+--------------------------------+----------------------+
`

### 3. Book Appointment (With Validation)
`	ext
--------------------------------------------------
                 BOOK AN APPOINTMENT              
--------------------------------------------------
Enter Patient ID: 1
Enter Doctor ID: 2
Enter Appointment Date (YYYY-MM-DD): 2026-11-20
Enter Appointment Time (HH:MM in 24-hr format, e.g. 10:30, 14:00): 11:30

[✓] Appointment booked successfully!
    Appointment ID : 2
    Date           : 2026-11-20
    Time           : 11:30
    Status         : BOOKED
`

### 4. View Appointments (SQL JOIN Demonstration)
`	ext
=========================================================================================================
                                         ALL APPOINTMENTS                                                
=========================================================================================================
+----+----------------------+----------------------+--------------------+------------+----------+-----------+
| ID | Patient Name         | Doctor Name          | Specialization     | Date       | Time     | Status    |
+----+----------------------+----------------------+--------------------+------------+----------+-----------+
| 1  | Rahul Verma          | Dr. Priya Sharma     | Cardiologist       | 2026-10-15 | 10:00:00 | BOOKED    |
| 2  | Rahul Verma          | Dr. Rajesh Mehta     | Neurologist        | 2026-11-20 | 11:30:00 | BOOKED    |
+----+----------------------+----------------------+--------------------+------------+----------+-----------+
`

---

## 🔮 Future Improvements

Features planned for upcoming iterations (not currently implemented):
* **User Authentication & Authorization:** Role-based access control (Admin, Doctor, Patient) with secure hashed passwords.
* **RESTful Web Services:** Transitioning backend capabilities to lightweight REST APIs.
* **Web-Based Responsive Dashboard:** An interactive HTML/CSS/JavaScript single-page application.
* **Billing & Invoicing System:** Generating digital receipts for consultations and treatments.
* **Prescription & Medical Records (EMR):** Storage of diagnostic notes and pharmacy prescriptions per visit.

---

## 👤 Author
**Ravi Gupta**  
* GitHub: [@ravigupta46](https://github.com/ravigupta46)