-- ==========================================================
-- Hospital Management System Database Schema
-- Database: hospital
-- ==========================================================

CREATE DATABASE IF NOT EXISTS hospital;
USE hospital;

-- Drop child tables first to respect foreign key constraints
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;

-- 1. Patients Table
CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL
);

-- 2. Doctors Table
CREATE TABLE doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL
);

-- 3. Appointments Table
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    -- Prevent duplicate bookings for the same doctor at the same date and time
    CONSTRAINT unique_doctor_slot UNIQUE (doctor_id, appointment_date, appointment_time)
);

-- ==========================================================
-- Sample Data
-- ==========================================================

-- Realistic sample doctors
INSERT INTO doctors (name, specialization) VALUES
('Dr. Priya Sharma', 'Cardiologist'),
('Dr. Rajesh Mehta', 'Neurologist'),
('Dr. Anita Desai', 'Dermatologist'),
('Dr. Vikram Patel', 'Orthopedic Surgeon'),
('Dr. Sunita Rao', 'Pediatrician'),
('Dr. Arvind Nair', 'General Physician');

-- Sample patients
INSERT INTO patients (name, age, gender) VALUES
('Rahul Verma', 29, 'Male'),
('Pooja Gupta', 34, 'Female'),
('Amit Kumar', 52, 'Male');

-- Sample appointment
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES
(1, 1, '2026-10-15', '10:00:00', 'BOOKED');