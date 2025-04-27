package dao;

import java.sql.*;

public class ConnectionProvider {
    // Updated connection details
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/category_manager";
    private static final String USER = "root";
    private static final String PASSWORD = "admin"; // Empty password since you're using root without password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getCon() {
        try {
            // First, try to create the database if it doesn't exist
            createDatabaseIfNotExists();

            // Then connect to the specific database
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void createDatabaseIfNotExists() {
        String createDbUrl = "jdbc:mysql://127.0.0.1:3306";
        try (Connection conn = DriverManager.getConnection(createDbUrl, USER, PASSWORD)) {
            Statement stmt = conn.createStatement();

            // Create database if not exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS category_manager");

            // Use the database
            stmt.executeUpdate("USE category_manager");



        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Test the connection
    public static void main(String[] args) {
        try (Connection conn = getCon()) {
            if (conn != null) {
                System.out.println("✅ Database connected successfully!");

                // Test table existence
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW TABLES");

                System.out.println("\nExisting tables:");
                while (rs.next()) {
                    System.out.println("- " + rs.getString(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
