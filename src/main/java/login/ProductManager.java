package login;

import com.formdev.flatlaf.FlatLightLaf;
import dao.ConnectionProvider;
import login.EmailUtils;
import login.UserSession;
import util.blockchain.BlockchainService;
import util.blockchain.qrcode;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ProductManager with purple pastel styling, unified sales logic,
 * and a Home button to return to the main dashboard.
 */
public class ProductManager extends JFrame {
    private final BlockchainService blockchainService;

    // --- UI Fields ---
    private JTextField txtID, txtName, txtDescription, txtQuantity, txtPrice, txtBuyingPrice, txtLocation, txtExpiry;
    private JComboBox<String> categoryCombo, vatCombo;
    private JTable productTable;
    private DefaultTableModel productTableModel;

    private JTextField txtCatID, txtCatName;
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;

    private JLabel userLabel, salesLabel, currentStockLabel, ethStatusLabel, statusLabel, allergyLabel;
    private JTextArea logArea;
    private Timer summaryTimer;

    // Allergens lookup
    private static final Map<String,String> ALLERGEN_MAP = new HashMap<>();
    static {
        for (String[] a : new String[][]{
                {"milk","Dairy"}, {"bread","Gluten"}, {"egg","Eggs"},
                {"soy","Soy"}, {"peanut","Peanuts"}, {"fish","Fish"}
        }) ALLERGEN_MAP.put(a[0], a[1]);
    }

    public ProductManager() {
        // FlatLaf Light + purple pastel overrides
        FlatLightLaf.setup();
        UIManager.put("Panel.background", new Color(245,240,255));
        UIManager.put("Button.background", new Color(230,210,255));
        UIManager.put("Button.foreground", new Color(70,70,70));
        UIManager.put("Table.selectionBackground", new Color(210,190,255));
        UIManager.put("TextField.background", new Color(255,250,255));

        blockchainService = new BlockchainService();
        initUI();
        loadCategoriesToCombo();
        loadProducts();
        loadCategoriesToTable();
        updateStats();
        startDailySummaryTimer();
    }

    private void initUI() {
        // Header
        userLabel         = new JLabel("👤 " + UserSession.getInstance().getEmail());
        salesLabel        = new JLabel("🛒 Sales: £0.00");
        currentStockLabel = new JLabel("📦 Stock: 0 items");
        ethStatusLabel    = new JLabel(blockchainService.isConnected() ? "✅ Ethereum" : "❌ Blockchain");
        JButton homeBtn   = new JButton("🏠 Home", FontIcon.of(FontAwesomeSolid.HOME,20));
        homeBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new HomePageUI(UserSession.getInstance().getEmail()).setVisible(true));
            dispose();
        });
        JButton aiBtn     = new JButton("AI Assist", FontIcon.of(FontAwesomeSolid.LIGHTBULB,20));
        aiBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "How can I help you today?", "AI Assistant", JOptionPane.PLAIN_MESSAGE)
        );

        JPanel header = new JPanel(new GridLayout(1,6,10,0));
        header.setBorder(new EmptyBorder(5,5,5,5));
        header.add(userLabel);
        header.add(salesLabel);
        header.add(currentStockLabel);
        header.add(ethStatusLabel);
        header.add(homeBtn);
        header.add(aiBtn);

        // Log console
        logArea = new JTextArea(4,80);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log Console"));
        statusLabel = new JLabel("Ready");
        JPanel south = new JPanel(new BorderLayout());
        south.add(statusLabel, BorderLayout.WEST);
        south.add(logScroll,   BorderLayout.CENTER);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📦 Products",   createProductPanel());
        tabs.addTab("📂 Categories", createCategoryPanel());

        // Assemble frame
        JPanel main = new JPanel(new BorderLayout(10,10));
        main.setBorder(new EmptyBorder(10,10,10,10));
        main.add(header, BorderLayout.NORTH);
        main.add(tabs,   BorderLayout.CENTER);
        main.add(south,  BorderLayout.SOUTH);

        setTitle("📦 Purple Pastel Inventory & Category Manager");
        setSize(1280,760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(main);
    }

    private JPanel createProductPanel() {
        productTableModel = new DefaultTableModel(
                new String[]{"ID","Name","Category","Qty","Price","BuyPrice","Location","Expiry"},0
        ) { public boolean isCellEditable(int r,int c){return false;} };
        productTable = new JTable(productTableModel);
        productTable.setSelectionBackground(new Color(210,190,255));
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillProductFormFromTable();
        });

        // Form fields
        txtID          = new JTextField(); txtID.setEditable(false);
        txtName        = new JTextField();
        categoryCombo  = new JComboBox<>();
        txtDescription = new JTextField();
        txtQuantity    = new JTextField("0");
        txtPrice       = new JTextField("0.00");
        txtBuyingPrice = new JTextField("0.00");
        txtLocation    = new JTextField("Shelf A");
        txtExpiry      = new JTextField("YYYY-MM-DD");
        vatCombo       = new JComboBox<>(new String[]{"0","5","20"});
        allergyLabel   = new JLabel("Allergens: None");
        allergyLabel.setForeground(Color.RED);
        txtDescription.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){ updateAllergens(); }
            public void removeUpdate(DocumentEvent e){ updateAllergens(); }
            public void changedUpdate(DocumentEvent e){ updateAllergens(); }
        });

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.add(new JLabel("ID"));            form.add(txtID);
        form.add(new JLabel("Name"));          form.add(txtName);
        form.add(new JLabel("Category"));      form.add(categoryCombo);
        form.add(new JLabel("Description"));   form.add(txtDescription);
        form.add(new JLabel("Quantity"));      form.add(txtQuantity);
        form.add(new JLabel("Selling Price")); form.add(txtPrice);
        form.add(new JLabel("Buying Price"));  form.add(txtBuyingPrice);
        form.add(new JLabel("Location"));      form.add(txtLocation);
        form.add(new JLabel("Expiry Date"));   form.add(txtExpiry);
        form.add(new JLabel("VAT %"));         form.add(vatCombo);
        form.add(new JLabel("Allergens"));     form.add(allergyLabel);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(makeIconButton("Add",    FontAwesomeSolid.PLUS,          e->addProduct()));
        btns.add(makeIconButton("Update", FontAwesomeSolid.PENCIL_ALT,   e->updateProduct()));
        btns.add(makeIconButton("Sell",   FontAwesomeSolid.SHOPPING_CART,e->sellProduct()));
        btns.add(makeIconButton("Delete", FontAwesomeSolid.TRASH,        e->deleteProduct()));
        btns.add(makeIconButton("Clear",  FontAwesomeSolid.SYNC,         e->clearProductForm()));

        JPanel east = new JPanel(new BorderLayout(10,10));
        east.add(form, BorderLayout.CENTER);
        east.add(btns, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        panel.add(east,                          BorderLayout.EAST);
        return panel;
    }

    private JPanel createCategoryPanel() {
        categoryTableModel = new DefaultTableModel(
                new String[]{"ID","Name","Status"},0
        ) { public boolean isCellEditable(int r,int c){return false;} };
        categoryTable = new JTable(categoryTableModel);
        categoryTable.setSelectionBackground(new Color(210,190,255));
        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && categoryTable.getSelectedRow()>=0) {
                int r = categoryTable.getSelectedRow();
                txtCatID.setText(categoryTableModel.getValueAt(r,0).toString());
                txtCatName.setText(categoryTableModel.getValueAt(r,1).toString());
            }
        });

        txtCatID   = new JTextField(); txtCatID.setEditable(false);
        txtCatName = new JTextField();
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.add(new JLabel("ID"));   form.add(txtCatID);
        form.add(new JLabel("Name")); form.add(txtCatName);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(makeIconButton("Add",     FontAwesomeSolid.PLUS,     e->addCategory()));
        btns.add(makeIconButton("Update",  FontAwesomeSolid.PENCIL_ALT,e->updateCategory()));
        btns.add(makeIconButton("Delete",  FontAwesomeSolid.TRASH,    e->deleteCategory()));
        btns.add(makeIconButton("Restore", FontAwesomeSolid.UNDO_ALT, e->restoreCategory()));
        btns.add(makeIconButton("Clear",   FontAwesomeSolid.SYNC,     e->clearCategoryForm()));

        JPanel east = new JPanel(new BorderLayout(10,10));
        east.add(form, BorderLayout.CENTER);
        east.add(btns, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.add(new JScrollPane(categoryTable), BorderLayout.CENTER);
        panel.add(east,                                BorderLayout.EAST);
        return panel;
    }

    private JButton makeIconButton(String text, FontAwesomeSolid icon, ActionListener listener) {
        JButton b = new JButton(text, FontIcon.of(icon,16));
        b.setFocusPainted(false);
        b.addActionListener(listener);
        return b;
    }

    private void loadProducts() {
        productTableModel.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM products WHERE is_deleted=false")) {
            while (rs.next()) {
                Date exp = rs.getDate("expiry_date");
                String expStr = exp==null ? "" : exp.toString();
                productTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getDouble("buying_price"),
                        rs.getString("location"),
                        expStr
                });
            }
            log("🔄 Products loaded");
        } catch (Exception ex) {
            log("❌ Load products error: " + ex.getMessage());
        }
    }

    private void fillProductFormFromTable() {
        int r = productTable.getSelectedRow();
        if (r < 0) return;
        txtID.setText(productTableModel.getValueAt(r,0).toString());
        txtName.setText(productTableModel.getValueAt(r,1).toString());
        categoryCombo.setSelectedItem(productTableModel.getValueAt(r,2));
        txtQuantity.setText(productTableModel.getValueAt(r,3).toString());
        txtPrice.setText(productTableModel.getValueAt(r,4).toString());
        txtBuyingPrice.setText(productTableModel.getValueAt(r,5).toString());
        txtLocation.setText(productTableModel.getValueAt(r,6).toString());
        txtExpiry.setText(productTableModel.getValueAt(r,7).toString());
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement("SELECT description, vat FROM products WHERE id=?")) {
            ps.setInt(1, Integer.parseInt(txtID.getText()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtDescription.setText(rs.getString("description"));
                    vatCombo.setSelectedItem(String.valueOf(rs.getInt("vat")));
                }
            }
        } catch (Exception ex) {
            log("❌ Fetch product details: " + ex.getMessage());
        }
        updateAllergens();
    }

    private void addProduct() {
        String sql = "INSERT INTO products(name,category,description,quantity,price,buying_price,location,vat,expiry_date,is_deleted) VALUES(?,?,?,?,?,?,?,?,?,false)";
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, categoryCombo.getSelectedItem().toString());
            ps.setString(3, txtDescription.getText().trim());
            ps.setInt(4, Integer.parseInt(txtQuantity.getText()));
            ps.setDouble(5, Double.parseDouble(txtPrice.getText()));
            ps.setDouble(6, Double.parseDouble(txtBuyingPrice.getText()));
            ps.setString(7, txtLocation.getText().trim());
            ps.setInt(8, Integer.parseInt(vatCombo.getSelectedItem().toString()));
            ps.setDate(9, java.sql.Date.valueOf(txtExpiry.getText().trim()));
            if (ps.executeUpdate() == 1) {
                try (ResultSet ks = ps.getGeneratedKeys()) {
                    if (ks.next()) {
                        int id = ks.getInt(1);
                        File f = new File("qrcodes/product_" + id + ".png");
                        f.getParentFile().mkdirs();
                        qrcode.generateQRCodeImage(String.valueOf(id),200,200,f.getAbsolutePath());
                        blockchainService.recordTransaction("ADD_PRODUCT", String.valueOf(id));
                        loadProducts();
                        updateStats();
                        log("✅ Product added (ID: " + id + ")");
                        clearProductForm();
                    }
                }
            }
        } catch (Exception ex) {
            log("❌ Add product error: " + ex.getMessage());
        }
    }

    private void updateProduct() {
        if (txtID.getText().isEmpty()) return;
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE products SET name=?,category=?,description=?,quantity=?,price=?,buying_price=?,location=?,vat=?,expiry_date=? WHERE id=?")) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, categoryCombo.getSelectedItem().toString());
            ps.setString(3, txtDescription.getText().trim());
            ps.setInt(4, Integer.parseInt(txtQuantity.getText()));
            ps.setDouble(5, Double.parseDouble(txtPrice.getText()));
            ps.setDouble(6, Double.parseDouble(txtBuyingPrice.getText()));
            ps.setString(7, txtLocation.getText().trim());
            ps.setInt(8, Integer.parseInt(vatCombo.getSelectedItem().toString()));
            ps.setDate(9, Date.valueOf(txtExpiry.getText().trim()));
            ps.setInt(10, Integer.parseInt(txtID.getText()));
            if (ps.executeUpdate() == 1) {
                blockchainService.recordTransaction("UPDATE_PRODUCT", txtID.getText());
                loadProducts();
                updateStats();
                log("✏️ Product updated (ID: " + txtID.getText() + ")");
                clearProductForm();
            }
        } catch (Exception ex) {
            log("❌ Update product error: " + ex.getMessage());
        }
    }

    private void sellProduct() {
        if (txtID.getText().isEmpty()) return;
        int id = Integer.parseInt(txtID.getText());
        String q = JOptionPane.showInputDialog(this, "Enter quantity to sell:", "Sell Product", JOptionPane.PLAIN_MESSAGE);
        if (q == null) return;
        try {
            int soldQty = Integer.parseInt(q);
            int current = Integer.parseInt(txtQuantity.getText());
            if (soldQty <= 0 || soldQty > current) {
                JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double unit = Double.parseDouble(txtPrice.getText());
            double total = soldQty * unit;
            try (Connection con = ConnectionProvider.getCon()) {
                con.setAutoCommit(false);
                try (PreparedStatement ps1 = con.prepareStatement("UPDATE products SET quantity=quantity-? WHERE id=?")) {
                    ps1.setInt(1, soldQty);
                    ps1.setInt(2, id);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = con.prepareStatement(
                        "INSERT INTO sales(product_id,quantity,price_per_unit,total_amount,sold_at) VALUES(?,?,?,?,NOW())")) {
                    ps2.setInt(1, id);
                    ps2.setInt(2, soldQty);
                    ps2.setDouble(3, unit);
                    ps2.setDouble(4, total);
                    ps2.executeUpdate();
                }
                con.commit();
                blockchainService.recordTransaction("SELL_PRODUCT", id + "|" + soldQty);
                loadProducts();
                updateStats();
                log("💰 Sold " + soldQty + " of ID " + id + " (£" + String.format("%.2f", total) + ")");
            }
        } catch (Exception ex) {
            log("❌ Sell product error: " + ex.getMessage());
        }
    }

    private void deleteProduct() {
        if (txtID.getText().isEmpty()) return;
        int id = Integer.parseInt(txtID.getText());
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement("UPDATE products SET is_deleted=true WHERE id=?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 1) {
                blockchainService.recordTransaction("DELETE_PRODUCT", String.valueOf(id));
                loadProducts();
                updateStats();
                log("🗑️ Product deleted (ID: " + id + ")");
                clearProductForm();
            }
        } catch (Exception ex) {
            log("❌ Delete product error: " + ex.getMessage());
        }
    }

    private void clearProductForm() {
        txtID.setText(""); txtName.setText(""); txtDescription.setText("");
        txtQuantity.setText("0"); txtPrice.setText("0.00"); txtBuyingPrice.setText("0.00");
        txtLocation.setText("Shelf A"); txtExpiry.setText("YYYY-MM-DD");
        vatCombo.setSelectedIndex(0);
        if (categoryCombo.getItemCount()>0) categoryCombo.setSelectedIndex(0);
        allergyLabel.setText("Allergens: None");
        productTable.clearSelection();
    }

    private void updateAllergens() {
        String desc = txtDescription.getText().toLowerCase();
        Set<String> found = new LinkedHashSet<>();
        ALLERGEN_MAP.forEach((k,v)->{ if (desc.contains(k)) found.add(v); });
        allergyLabel.setText("Allergens: " + (found.isEmpty() ? "None" : String.join(", ", found)));
    }

    private void loadCategoriesToCombo() {
        categoryCombo.removeAllItems();
        try (Connection con = ConnectionProvider.getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM categories WHERE is_deleted=false")) {
            while (rs.next()) categoryCombo.addItem(rs.getString(1));
        } catch (Exception ex) {
            log("❌ Load categories error: " + ex.getMessage());
        }
    }

    private void loadCategoriesToTable() {
        categoryTableModel.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id,name,is_deleted FROM categories")) {
            while (rs.next()) {
                categoryTableModel.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getBoolean(3) ? "Deleted" : "Active"
                });
            }
            log("🔄 Categories loaded");
        } catch (Exception ex) {
            log("❌ Load categories error: " + ex.getMessage());
        }
    }

    private void addCategory() {
        String name = txtCatName.getText().trim();
        if (name.isEmpty()) {
            log("❌ Category name required");
            return;
        }
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement("INSERT INTO categories(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            if (ps.executeUpdate()==1) {
                try (ResultSet ks = ps.getGeneratedKeys()) {
                    if (ks.next()) {
                        blockchainService.recordTransaction("ADD_CATEGORY", name);
                        loadCategoriesToTable();
                        loadCategoriesToCombo();
                        log("✅ Category added (ID: " + ks.getInt(1) + ")");
                        clearCategoryForm();
                    }
                }
            }
        } catch (Exception ex) {
            log("❌ Add category error: " + ex.getMessage());
        }
    }

    private void updateCategory() {
        if (txtCatID.getText().isEmpty()) return;
        int id = Integer.parseInt(txtCatID.getText());
        String name = txtCatName.getText().trim();
        if (name.isEmpty()) {
            log("❌ Category name required");
            return;
        }
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement("UPDATE categories SET name=? WHERE id=?")) {
            ps.setString(1, name);
            ps.setInt(2, id);
            if (ps.executeUpdate()==1) {
                blockchainService.recordTransaction("UPDATE_CATEGORY", String.valueOf(id));
                loadCategoriesToTable();
                loadCategoriesToCombo();
                log("✏️ Category updated (ID: " + id + ")");
                clearCategoryForm();
            }
        } catch (Exception ex) {
            log("❌ Update category error: " + ex.getMessage());
        }
    }

    private void deleteCategory() {
        if (txtCatID.getText().isEmpty()) return;
        int id = Integer.parseInt(txtCatID.getText());
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement("UPDATE categories SET is_deleted=true WHERE id=?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate()==1) {
                blockchainService.recordTransaction("DELETE_CATEGORY", String.valueOf(id));
                loadCategoriesToTable();
                loadCategoriesToCombo();
                log("🗑️ Category deleted (ID: " + id + ")");
                clearCategoryForm();
            }
        } catch (Exception ex) {
            log("❌ Delete category error: " + ex.getMessage());
        }
    }

    private void restoreCategory() {
        if (txtCatID.getText().isEmpty()) return;
        int id = Integer.parseInt(txtCatID.getText());
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement("UPDATE categories SET is_deleted=false WHERE id=?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate()==1) {
                blockchainService.recordTransaction("RESTORE_CATEGORY", String.valueOf(id));
                loadCategoriesToTable();
                loadCategoriesToCombo();
                log("♻️ Category restored (ID: " + id + ")");
                clearCategoryForm();
            }
        } catch (Exception ex) {
            log("❌ Restore category error: " + ex.getMessage());
        }
    }

    private void clearCategoryForm() {
        txtCatID.setText("");
        txtCatName.setText("");
        categoryTable.clearSelection();
    }

    private void updateStats() {
        try (Connection con = ConnectionProvider.getCon();
             Statement st = con.createStatement()) {
            // Stock
            ResultSet rs = st.executeQuery("SELECT SUM(quantity) FROM products WHERE is_deleted=false");
            if (rs.next())
                currentStockLabel.setText("📦 Stock: " + rs.getInt(1) + " items");
            // POS sales
            rs = st.executeQuery("SELECT COALESCE(SUM(total_amount),0) FROM sales");
            double posSales = rs.next() ? rs.getDouble(1) : 0;
            // Orders sales
            rs = st.executeQuery("SELECT COALESCE(SUM(total),0) FROM orders");
            double orderSales = rs.next() ? rs.getDouble(1) : 0;
            salesLabel.setText(String.format("🛒 Sales: £%.2f", posSales + orderSales));

            log("🔄 Stats updated");
        } catch (Exception ex) {
            log("❌ Update stats error: " + ex.getMessage());
        }
    }

    private void startDailySummaryTimer() {
        summaryTimer = new Timer(24*60*60*1000, e -> sendDailySummaryEmail());
        summaryTimer.setInitialDelay(10000);
        summaryTimer.start();
    }

    private void sendDailySummaryEmail() {
        try (Connection con = ConnectionProvider.getCon();
             Statement st = con.createStatement()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<h2>📋 Daily Inventory Summary - ").append(LocalDateTime.now().toLocalDate()).append("</h2>");
            ResultSet rs = st.executeQuery("SELECT COUNT(*), SUM(quantity) FROM products WHERE is_deleted=false");
            if (rs.next())
                sb.append("<p><b>Items:</b> ").append(rs.getInt(2)).append("</p>");
            rs = st.executeQuery("SELECT SUM(total_amount) FROM sales WHERE DATE(sold_at)=CURDATE()");
            if (rs.next())
                sb.append("<p><b>POS Sales:</b> £").append(String.format("%.2f", rs.getDouble(1))).append("</p>");
            rs = st.executeQuery("SELECT SUM(total) FROM orders WHERE DATE(created_at)=CURDATE()");
            if (rs.next())
                sb.append("<p><b>Order Sales:</b> £").append(String.format("%.2f", rs.getDouble(1))).append("</p>");
            EmailUtils.sendAdminReport("Daily Inventory Summary", sb.toString());
            log("📧 Daily summary email sent.");
        } catch (Exception ex) {
            log("❌ Summary email error: " + ex.getMessage());
        }
    }

    private void log(String msg) {
        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logArea.append("[" + time + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        statusLabel.setText(msg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductManager().setVisible(true));
    }
}
