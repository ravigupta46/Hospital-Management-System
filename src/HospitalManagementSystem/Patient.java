package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Responsibility of Patient:
 *
 * Encapsulates patient operations and database queries:
 * 1. Register a new patient with input validation (name, age, gender).
 * 2. View all registered patients in a clean table format.
 * 3. Find and display patient details by ID.
 * 4. Verify patient existence for appointment booking.
 *
 * Follows JDBC best practices:
 * - PreparedStatement for SQL injection prevention.
 * - Explicit column names instead of SELECT *.
 * - try-with-resources for automatic resource closing.
 */
public class Patient {

    private final Connection connection;
    private final Scanner scanner;

    public Patient(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Interactively registers a new patient with validation.
     */
    public void addPatient() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("                REGISTER NEW PATIENT              ");
        System.out.println("--------------------------------------------------");

        // 1. Validate Name
        String name = "";
        while (name.isEmpty()) {
            System.out.print("Enter Patient Name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("[!] Patient name cannot be empty. Please enter a valid name.");
            }
        }

        // 2. Validate Age
        int age = -1;
        while (age <= 0 || age > 125) {
            System.out.print("Enter Patient Age (1 - 125): ");
            String ageInput = scanner.nextLine().trim();
            try {
                age = Integer.parseInt(ageInput);
                if (age <= 0 || age > 125) {
                    System.out.println("[!] Please enter a realistic age between 1 and 125.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Age must be a whole number.");
            }
        }

        // 3. Validate Gender
        String gender = "";
        while (gender.isEmpty()) {
            System.out.print("Enter Patient Gender (Male / Female / Other): ");
            gender = scanner.nextLine().trim();
            if (gender.isEmpty()) {
                System.out.println("[!] Gender cannot be empty.");
            }
        }

        // 4. Insert into Database
        String query = "INSERT INTO patients (name, age, gender) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            preparedStatement.setString(3, gender);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        System.out.println("[✓] Patient registered successfully! Assigned Patient ID: " + generatedId);
                    } else {
                        System.out.println("[✓] Patient registered successfully!");
                    }
                }
            } else {
                System.out.println("[!] Failed to register patient. No rows affected.");
            }
        } catch (SQLException e) {
            System.out.println("[!] Database error while registering patient: " + e.getMessage());
        }
    }

    /**
     * Queries and displays all patients in an ASCII formatted table.
     */
    public void viewPatients() {
        String query = "SELECT id, name, age, gender FROM patients ORDER BY id ASC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n==================================================================");
            System.out.println("                         REGISTERED PATIENTS                      ");
            System.out.println("==================================================================");
            System.out.println("+------------+--------------------------------+----------+------------+");
            System.out.println("| Patient Id | Name                           | Age      | Gender     |");
            System.out.println("+------------+--------------------------------+----------+------------+");

            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String gender = resultSet.getString("gender");

                System.out.printf("| %-10d | %-30s | %-8d | %-10s |\n", id, name, age, gender);
            }

            if (!hasData) {
                System.out.println("|                      No patient records found.                 |");
            }
            System.out.println("+------------+--------------------------------+----------+------------+");

        } catch (SQLException e) {
            System.out.println("[!] Database error while retrieving patients: " + e.getMessage());
        }
    }

    /**
     * Interactively searches for a patient by ID and displays their details.
     */
    public void findPatientById() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("                FIND PATIENT BY ID                ");
        System.out.println("--------------------------------------------------");

        int id = -1;
        while (id <= 0) {
            System.out.print("Enter Patient ID: ");
            String input = scanner.nextLine().trim();
            try {
                id = Integer.parseInt(input);
                if (id <= 0) {
                    System.out.println("[!] Patient ID must be a positive number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Patient ID must be a numeric value.");
            }
        }

        String query = "SELECT id, name, age, gender FROM patients WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("\n[✓] Patient Found:");
                    System.out.println("--------------------------------------------------");
                    System.out.println("Patient ID : " + resultSet.getInt("id"));
                    System.out.println("Name       : " + resultSet.getString("name"));
                    System.out.println("Age        : " + resultSet.getInt("age"));
                    System.out.println("Gender     : " + resultSet.getString("gender"));
                    System.out.println("--------------------------------------------------");
                } else {
                    System.out.println("[!] Patient with ID " + id + " was not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("[!] Database error while searching for patient: " + e.getMessage());
        }
    }

    /**
     * Checks if a patient exists by ID (used for foreign key validation).
     *
     * @param id Patient ID
     * @return true if patient exists in database, false otherwise
     */
    public boolean getPatientById(int id) {
        String query = "SELECT id FROM patients WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.out.println("[!] Database error checking patient existence: " + e.getMessage());
            return false;
        }
    }
}