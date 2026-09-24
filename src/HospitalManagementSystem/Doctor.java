package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Responsibility of Doctor:
 *
 * Encapsulates doctor operations and database queries:
 * 1. View all doctors in a formatted table.
 * 2. Find and display doctor details by ID.
 * 3. Verify doctor existence for appointment booking.
 *
 * Follows JDBC best practices:
 * - PreparedStatement for all queries.
 * - Explicit column selection (id, name, specialization).
 * - try-with-resources for reliable closing of PreparedStatement & ResultSet.
 */
public class Doctor {

    private final Connection connection;
    private final Scanner scanner;

    public Doctor(Connection connection) {
        this(connection, null);
    }

    public Doctor(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Queries and displays all doctors in an ASCII formatted table.
     */
    public void viewDoctors() {
        String query = "SELECT id, name, specialization FROM doctors ORDER BY id ASC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n==================================================================");
            System.out.println("                         AVAILABLE DOCTORS                        ");
            System.out.println("==================================================================");
            System.out.println("+------------+--------------------------------+----------------------+");
            System.out.println("| Doctor Id  | Name                           | Specialization       |");
            System.out.println("+------------+--------------------------------+----------------------+");

            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String specialization = resultSet.getString("specialization");

                System.out.printf("| %-10d | %-30s | %-20s |\n", id, name, specialization);
            }

            if (!hasData) {
                System.out.println("|                      No doctors found.                         |");
            }
            System.out.println("+------------+--------------------------------+----------------------+");

        } catch (SQLException e) {
            System.out.println("[!] Database error while retrieving doctors: " + e.getMessage());
        }
    }

    /**
     * Interactively searches for a doctor by ID and displays their details.
     */
    public void findDoctorById() {
        if (scanner == null) {
            System.out.println("[!] Scanner is not initialized for Doctor class.");
            return;
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println("                 FIND DOCTOR BY ID                ");
        System.out.println("--------------------------------------------------");

        int id = -1;
        while (id <= 0) {
            System.out.print("Enter Doctor ID: ");
            String input = scanner.nextLine().trim();
            try {
                id = Integer.parseInt(input);
                if (id <= 0) {
                    System.out.println("[!] Doctor ID must be a positive number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Doctor ID must be a numeric value.");
            }
        }

        String query = "SELECT id, name, specialization FROM doctors WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("\n[✓] Doctor Found:");
                    System.out.println("--------------------------------------------------");
                    System.out.println("Doctor ID      : " + resultSet.getInt("id"));
                    System.out.println("Name           : " + resultSet.getString("name"));
                    System.out.println("Specialization : " + resultSet.getString("specialization"));
                    System.out.println("--------------------------------------------------");
                } else {
                    System.out.println("[!] Doctor with ID " + id + " was not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("[!] Database error while searching for doctor: " + e.getMessage());
        }
    }

    /**
     * Checks if a doctor exists by ID (used for foreign key validation).
     *
     * @param id Doctor ID
     * @return true if doctor exists in database, false otherwise
     */
    public boolean getDoctorById(int id) {
        String query = "SELECT id FROM doctors WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.out.println("[!] Database error checking doctor existence: " + e.getMessage());
            return false;
        }
    }
}