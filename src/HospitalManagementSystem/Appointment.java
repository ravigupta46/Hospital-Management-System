package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Responsibility of Appointment:
 *
 * Encapsulates appointment scheduling and lifecycle operations:
 * 1. Book appointment with patient/doctor validation, date/time checks, and availability check.
 * 2. View appointments utilizing SQL JOIN (patients, doctors, appointments).
 * 3. Cancel appointment with status transition validation (BOOKED -> CANCELLED).
 * 4. Complete appointment with status transition validation (BOOKED -> COMPLETED).
 *
 * Demonstrates Key Interview Concepts:
 * - Transaction Safety (setAutoCommit, commit, rollback).
 * - SQL JOIN across 3 relational tables.
 * - java.time (LocalDate, LocalTime) validation.
 * - Defensive coding against database UNIQUE constraints (Error 1062).
 * - try-with-resources on PreparedStatement and ResultSet.
 */
public class Appointment {

    private final Connection connection;
    private final Scanner scanner;

    public Appointment(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Books a new appointment with comprehensive validation and transaction safety.
     *
     * @param patient Patient helper for existence verification
     * @param doctor  Doctor helper for existence verification
     */
    public void bookAppointment(Patient patient, Doctor doctor) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("                 BOOK AN APPOINTMENT              ");
        System.out.println("--------------------------------------------------");

        // 1. Validate Patient ID
        int patientId = readPositiveInt("Enter Patient ID: ");
        if (!patient.getPatientById(patientId)) {
            System.out.println("[!] Patient with ID " + patientId + " does not exist. Please register the patient first.");
            return;
        }

        // 2. Validate Doctor ID
        int doctorId = readPositiveInt("Enter Doctor ID: ");
        if (!doctor.getDoctorById(doctorId)) {
            System.out.println("[!] Doctor with ID " + doctorId + " does not exist.");
            return;
        }

        // 3. Validate Date (Cannot be in the past)
        LocalDate appointmentDate = null;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (appointmentDate == null) {
            System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
            String dateInput = scanner.nextLine().trim();
            try {
                LocalDate parsedDate = LocalDate.parse(dateInput, dateFormatter);
                if (parsedDate.isBefore(LocalDate.now())) {
                    System.out.println("[!] Cannot book appointments in the past. Please enter today or a future date.");
                } else {
                    appointmentDate = parsedDate;
                }
            } catch (DateTimeParseException e) {
                System.out.println("[!] Invalid date format. Please use YYYY-MM-DD (e.g. 2026-10-15).");
            }
        }

        // 4. Validate Time (Cannot be in the past if booked for today)
        LocalTime appointmentTime = null;
        while (appointmentTime == null) {
            System.out.print("Enter Appointment Time (HH:MM in 24-hr format, e.g. 10:30, 14:00): ");
            String timeInput = scanner.nextLine().trim();
            try {
                // Support H:MM or HH:MM
                if (timeInput.length() == 4 && timeInput.charAt(1) == ':') {
                    timeInput = "0" + timeInput;
                }
                LocalTime parsedTime = LocalTime.parse(timeInput);
                if (appointmentDate.equals(LocalDate.now()) && parsedTime.isBefore(LocalTime.now())) {
                    System.out.println("[!] Cannot book an appointment for a past time today.");
                } else {
                    appointmentTime = parsedTime;
                }
            } catch (DateTimeParseException e) {
                System.out.println("[!] Invalid time format. Please use HH:MM (e.g. 09:30 or 15:00).");
            }
        }

        // 5. Check Doctor Availability for active appointments
        if (!isDoctorAvailable(doctorId, appointmentDate, appointmentTime)) {
            System.out.println("[!] Doctor is already booked for this time slot.");
            return;
        }

        // 6. Insert Appointment with Transaction Management
        String insertQuery = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) " +
                             "VALUES (?, ?, ?, ?, 'BOOKED')";

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false); // Phase 11: Begin Transaction

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, patientId);
                preparedStatement.setInt(2, doctorId);
                preparedStatement.setDate(3, java.sql.Date.valueOf(appointmentDate));
                preparedStatement.setTime(4, java.sql.Time.valueOf(appointmentTime));

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    connection.commit(); // Phase 11: Commit Transaction
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int appointmentId = generatedKeys.getInt(1);
                            System.out.println("\n[✓] Appointment booked successfully!");
                            System.out.println("    Appointment ID : " + appointmentId);
                            System.out.println("    Date           : " + appointmentDate);
                            System.out.println("    Time           : " + appointmentTime);
                            System.out.println("    Status         : BOOKED");
                        } else {
                            System.out.println("\n[✓] Appointment booked successfully!");
                        }
                    }
                } else {
                    connection.rollback(); // Rollback if no row was affected
                    System.out.println("[!] Failed to book appointment. Please try again.");
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback on SQL error
            } catch (SQLException rollbackEx) {
                System.out.println("[!] Rollback failed: " + rollbackEx.getMessage());
            }

            // MySQL Error Code 1062 = ER_DUP_ENTRY (Unique constraint violation)
            if (e.getErrorCode() == 1062) {
                System.out.println("[!] Doctor is already booked for this date and time.");
            } else {
                System.out.println("[!] Database error while booking appointment: " + e.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit); // Restore auto-commit state
            } catch (SQLException e) {
                System.out.println("[!] Failed to restore auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if a doctor has an active ('BOOKED') appointment at the specified date and time.
     *
     * @param doctorId Doctor ID
     * @param date     Appointment date
     * @param time     Appointment time
     * @return true if doctor is free, false if already booked
     */
    public boolean isDoctorAvailable(int doctorId, LocalDate date, LocalTime time) {
        String query = "SELECT COUNT(*) FROM appointments " +
                       "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'BOOKED'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setDate(2, java.sql.Date.valueOf(date));
            preparedStatement.setTime(3, java.sql.Time.valueOf(time));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("[!] Error checking doctor availability: " + e.getMessage());
        }
        return false;
    }

    /**
     * Displays all appointments joining patients and doctors tables (Phase 9).
     */
    public void viewAppointments() {
        String query = "SELECT " +
                       "    a.id, " +
                       "    p.name AS patient_name, " +
                       "    d.name AS doctor_name, " +
                       "    d.specialization, " +
                       "    a.appointment_date, " +
                       "    a.appointment_time, " +
                       "    a.status " +
                       "FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN doctors d ON a.doctor_id = d.id " +
                       "ORDER BY a.appointment_date ASC, a.appointment_time ASC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n=========================================================================================================");
            System.out.println("                                         ALL APPOINTMENTS                                                ");
            System.out.println("=========================================================================================================");
            System.out.println("+----+----------------------+----------------------+--------------------+------------+----------+-----------+");
            System.out.println("| ID | Patient Name         | Doctor Name          | Specialization     | Date       | Time     | Status    |");
            System.out.println("+----+----------------------+----------------------+--------------------+------------+----------+-----------+");

            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                int id = resultSet.getInt("id");
                String patientName = resultSet.getString("patient_name");
                String doctorName = resultSet.getString("doctor_name");
                String specialization = resultSet.getString("specialization");
                java.sql.Date date = resultSet.getDate("appointment_date");
                java.sql.Time time = resultSet.getTime("appointment_time");
                String status = resultSet.getString("status");

                System.out.printf("| %-2d | %-20s | %-20s | %-18s | %-10s | %-8s | %-9s |\n",
                        id, patientName, doctorName, specialization, date, time, status);
            }

            if (!hasData) {
                System.out.println("|                                       No appointments found.                                          |");
            }
            System.out.println("+----+----------------------+----------------------+--------------------+------------+----------+-----------+");

        } catch (SQLException e) {
            System.out.println("[!] Database error while retrieving appointments: " + e.getMessage());
        }
    }

    /**
     * Cancels an existing appointment with state transition validation (BOOKED -> CANCELLED).
     */
    public void cancelAppointment() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("               CANCEL APPOINTMENT                 ");
        System.out.println("--------------------------------------------------");

        int id = readPositiveInt("Enter Appointment ID to cancel: ");
        String currentStatus = getAppointmentStatus(id);

        if (currentStatus == null) {
            System.out.println("[!] Appointment with ID " + id + " was not found.");
            return;
        }

        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            System.out.println("[!] Appointment ID " + id + " is already CANCELLED.");
            return;
        }

        if ("COMPLETED".equalsIgnoreCase(currentStatus)) {
            System.out.println("[!] Cannot cancel an appointment that has already been marked as COMPLETED.");
            return;
        }

        // Allowed transition: BOOKED -> CANCELLED
        String updateQuery = "UPDATE appointments SET status = 'CANCELLED' WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, id);
            int affected = preparedStatement.executeUpdate();
            if (affected > 0) {
                System.out.println("[✓] Appointment ID " + id + " has been successfully CANCELLED.");
            } else {
                System.out.println("[!] Failed to cancel appointment.");
            }
        } catch (SQLException e) {
            System.out.println("[!] Database error while cancelling appointment: " + e.getMessage());
        }
    }

    /**
     * Marks an existing appointment as completed with state transition validation (BOOKED -> COMPLETED).
     */
    public void completeAppointment() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("              COMPLETE APPOINTMENT                ");
        System.out.println("--------------------------------------------------");

        int id = readPositiveInt("Enter Appointment ID to complete: ");
        String currentStatus = getAppointmentStatus(id);

        if (currentStatus == null) {
            System.out.println("[!] Appointment with ID " + id + " was not found.");
            return;
        }

        if ("COMPLETED".equalsIgnoreCase(currentStatus)) {
            System.out.println("[!] Appointment ID " + id + " is already marked as COMPLETED.");
            return;
        }

        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            System.out.println("[!] Cannot complete an appointment that has been CANCELLED.");
            return;
        }

        // Allowed transition: BOOKED -> COMPLETED
        String updateQuery = "UPDATE appointments SET status = 'COMPLETED' WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, id);
            int affected = preparedStatement.executeUpdate();
            if (affected > 0) {
                System.out.println("[✓] Appointment ID " + id + " has been marked as COMPLETED.");
            } else {
                System.out.println("[!] Failed to update appointment status.");
            }
        } catch (SQLException e) {
            System.out.println("[!] Database error while completing appointment: " + e.getMessage());
        }
    }

    /**
     * Fetches current status of an appointment.
     *
     * @param id Appointment ID
     * @return Status string ('BOOKED', 'COMPLETED', 'CANCELLED') or null if not found
     */
    private String getAppointmentStatus(int id) {
        String query = "SELECT status FROM appointments WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("status");
                }
            }
        } catch (SQLException e) {
            System.out.println("[!] Error fetching appointment status: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper to read a positive integer from the console safely without Scanner bugs.
     */
    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("[!] Please enter a positive number greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a valid numeric value.");
            }
        }
    }
}