package customerside;

import dao.ConnectionProvider;
import util.blockchain.BlockchainService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class MainUI extends JFrame {

    private JTextField txtName, txtEmail, txtPhone;
    private JTextField txtCustomerId;
    private JButton btnCreateProfile, btnViewProducts, btnAddToBasket, btnCheckout;
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private int customerId = -1;  // Default: No customer created
    private BlockchainService blockchainService;

    public MainUI() {
        blockchainService = new BlockchainService();
        initUI();
    }

    private void initUI() {
        setTitle("Customer Profile and Shopping");

        // Profile Creation Panel
        JPanel profilePanel = new JPanel(new GridLayout(4, 2, 10, 10));
        profilePanel.setBorder(BorderFactory.createTitledBorder("Create Profile"));
        profilePanel.add(new JLabel("Name:"));
        txtName = new JTextField();
        profilePanel.add(txtName);
        profilePanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        profilePanel.add(txtEmail);
        profilePanel.add(new JLabel("Phone:"));
        txtPhone = new JTextField();
        profilePanel.add(txtPhone);

        btnCreateProfile = new JButton("Create Profile");
        btnCreateProfile.addActionListener(e -> createProfile());
        profilePanel.add(btnCreateProfile);

        // Product Viewing and Basket
        JPanel productPanel = new JPanel(new BorderLayout());
        productTableModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Price"}, 0);
        productTable = new JTable(productTableModel);
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        btnViewProducts = new JButton("View Products");
        btnViewProducts.addActionListener(e -> loadProducts());
        productPanel.add(btnViewProducts, BorderLayout.SOUTH);

        btnAddToBasket = new JButton("Add to Basket");
        btnAddToBasket.addActionListener(e -> addToBasket());
        productPanel.add(btnAddToBasket, BorderLayout.NORTH);

        btnCheckout = new JButton("Checkout");
        btnCheckout.addActionListener(e -> checkout());
        productPanel.add(btnCheckout, BorderLayout.SOUTH);

        // Layout the panels in the main JFrame
        setLayout(new BorderLayout(10, 10));
        add(profilePanel, BorderLayout.NORTH);
        add(productPanel, BorderLayout.CENTER);

        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    // Method to create a customer profile and store it in SQL
    private void createProfile() {
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtEmail.getText().trim());
                ps.setString(3, txtPhone.getText().trim());

                if (ps.executeUpdate() == 1) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            customerId = rs.getInt(1);  // Get the generated customer ID
                            JOptionPane.showMessageDialog(this, "Profile created successfully!");
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error creating profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to load products into the JTable
    private void loadProducts() {
        try (Connection con = ConnectionProvider.getCon();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products WHERE is_deleted = false")) {
            productTableModel.setRowCount(0);  // Clear existing rows
            while (rs.next()) {
                productTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading products.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to add product to basket
    private void addToBasket() {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            int productId = (int) productTableModel.getValueAt(row, 0);
            String productName = (String) productTableModel.getValueAt(row, 1);
            double price = (double) productTableModel.getValueAt(row, 2);

            // Add to basket logic (store in database)
            try (Connection con = ConnectionProvider.getCon()) {
                String sql = "INSERT INTO basket (customer_id, product_id, quantity, total_amount) VALUES (?, ?, 1, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, customerId);
                    ps.setInt(2, productId);
                    ps.setDouble(3, price);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Added to basket!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding to basket.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product.");
        }
    }

    // Method to checkout and process payment
    private void checkout() {
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "SELECT SUM(total_amount) FROM basket WHERE customer_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double totalAmount = rs.getDouble(1);
                        JOptionPane.showMessageDialog(this, "Total Amount: £" + totalAmount);

                        // Log transaction on Blockchain
                        blockchainService.logPurchase(customerId, 0, 0, totalAmount);  // Simulate blockchain logging

                        // Clear the basket after checkout
                        clearBasket();
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during checkout.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to clear the basket
    private void clearBasket() {
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "DELETE FROM basket WHERE customer_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, customerId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Basket cleared!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error clearing basket.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }
}
