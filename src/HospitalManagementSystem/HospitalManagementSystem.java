package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Responsibility of HospitalManagementSystem:
 *
 * Orchestrates the user interface, menu system, and main application lifecycle:
 * 1. Establishes the database connection via DatabaseConnection.
 * 2. Initializes domain handlers (Patient, Doctor, Appointment).
 * 3. Provides a clean, interactive console menu with safe input handling.
 * 4. Ensures all resources (Database Connection, Scanner) are closed cleanly upon exit.
 */
public class HospitalManagementSystem {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("        STARTING HOSPITAL MANAGEMENT SYSTEM       ");
        System.out.println("==================================================");

        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            System.out.println("[✓] Connected to MySQL database successfully.");

            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection, scanner);
            Appointment appointment = new Appointment(connection, scanner);

            boolean running = true;
            while (running) {
                printMenu();
                int choice = readMenuChoice(scanner);

                switch (choice) {
                    case 1:
                        patient.addPatient();
                        break;
                    case 2:
                        patient.viewPatients();
                        break;
                    case 3:
                        patient.findPatientById();
                        break;
                    case 4:
                        doctor.viewDoctors();
                        break;
                    case 5:
                        doctor.findDoctorById();
                        break;
                    case 6:
                        appointment.bookAppointment(patient, doctor);
                        break;
                    case 7:
                        appointment.viewAppointments();
                        break;
                    case 8:
                        appointment.cancelAppointment();
                        break;
                    case 9:
                        appointment.completeAppointment();
                        break;
                    case 10:
                        System.out.println("\n==================================================");
                        System.out.println("  THANK YOU FOR USING HOSPITAL MANAGEMENT SYSTEM! ");
                        System.out.println("==================================================");
                        running = false;
                        break;
                    default:
                        System.out.println("[!] Invalid choice. Please select an option between 1 and 10.");
                        break;
                }
            }

        } catch (SQLException e) {
            System.err.println("\n[!] Unable to connect to the database. Please verify MySQL configuration.");
            System.err.println("    Error Details: " + e.getMessage());
            System.err.println("    Tip: Check if MySQL is running and verify DB_URL, DB_USERNAME, and DB_PASSWORD environment variables.");
        } finally {
            // Close connection cleanly upon application exit
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("[!] Error closing database connection: " + e.getMessage());
                }
            }
            scanner.close();
        }
    }

    /**
     * Prints the primary console menu.
     */
    private static void printMenu() {
        System.out.println("\n==================================================");
        System.out.println("            HOSPITAL MANAGEMENT SYSTEM            ");
        System.out.println("==================================================");
        System.out.println(" 1. Add Patient");
        System.out.println(" 2. View Patients");
        System.out.println(" 3. Find Patient by ID");
        System.out.println(" 4. View Doctors");
        System.out.println(" 5. Find Doctor by ID");
        System.out.println(" 6. Book Appointment");
        System.out.println(" 7. View Appointments");
        System.out.println(" 8. Cancel Appointment");
        System.out.println(" 9. Complete Appointment");
        System.out.println(" 10. Exit");
        System.out.println("==================================================");
    }

    /**
     * Safely reads the user's menu choice, avoiding Scanner parsing crashes.
     */
    private static int readMenuChoice(Scanner scanner) {
        while (true) {
            System.out.print("Enter your choice (1-10): ");
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a number between 1 and 10.");
            }
        }
    }
}