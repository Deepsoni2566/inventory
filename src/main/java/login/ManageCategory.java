package login;

import contracts.CategoryManager;
import dao.ConnectionProvider;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManageCategory extends JFrame {
    private static final Color PASTEL_BLUE = new Color(176, 196, 222);
    private static final Color BACKGROUND = new Color(245, 245, 250);

    private Web3j web3;
    private Credentials credentials;
    private CategoryManager contract;

    private JTextField txtID, txtName, txtDescription;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtLog;
    private JLabel statusLabel;

    public ManageCategory() {
        setTitle("📦 Smart Category Manager");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        createUI();
        initBlockchain();
        loadCategories();
    }

    private void createUI() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {{
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setBackground(BACKGROUND);
        }};

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(createTablePanel()), createFormWithButtons());
        splitPane.setDividerLocation(650);

        txtLog = new JTextArea(4, 40);
        txtLog.setEditable(false);
        JScrollPane logScroll = new JScrollPane(txtLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log"));

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(logScroll, BorderLayout.SOUTH);
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        setContentPane(mainPanel);
    }

    private JPanel createFormWithButtons() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        txtID = new JTextField(); txtID.setEditable(false);
        txtName = new JTextField(); txtDescription = new JTextField();
        form.add(new JLabel("ID:")); form.add(txtID);
        form.add(new JLabel("Name:")); form.add(txtName);
        form.add(new JLabel("Description:")); form.add(txtDescription);

        JPanel buttons = new JPanel(new GridLayout(1, 5, 5, 5));
        buttons.add(createButton("➕ Add", e -> addCategory()));
        buttons.add(createButton("✏️ Update", e -> updateCategory()));
        buttons.add(createButton("🗑️ Delete", e -> deleteCategory()));
        buttons.add(createButton("♻️ Restore", e -> restoreCategory()));
        buttons.add(createButton("🔄 Refresh", e -> loadCategories()));

        panel.add(form, BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JTable createTablePanel() {
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Description", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtID.setText(table.getValueAt(row, 0).toString());
                txtName.setText(table.getValueAt(row, 1).toString());
                txtDescription.setText(table.getValueAt(row, 2).toString());
            }
        });
        return table;
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(PASTEL_BLUE);
        btn.setFocusPainted(false);
        btn.addActionListener(action);
        return btn;
    }

    private void initBlockchain() {
        try {
            web3 = Web3j.build(new HttpService("http://127.0.0.1:7545"));
            credentials = Credentials.create("0xa83c03bf70a5bd8ccced28ab583697734b2f3d8ad6b0f74a74b64dfc0de4e256");
            contract = CategoryManager.load("0xf8e81D47203A594245E36C48e151709F0C19fBe8", web3, credentials, new DefaultGasProvider());
            log("Connected to Ethereum via Ganache");
        } catch (Exception e) { log("Blockchain connection failed: " + e.getMessage()); }
    }

    private void addCategory() {
        try {
            String name = txtName.getText().trim(), desc = txtDescription.getText().trim();
            if (name.isEmpty() || desc.isEmpty()) throw new Exception("Fill name and description");
            contract.addCategory(name, desc).send();
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("INSERT INTO categories(name, description) VALUES (?, ?)");
            ps.setString(1, name); ps.setString(2, desc); ps.executeUpdate();
            log("✅ Category added: " + name);
            loadCategories(); clearFields();
        } catch (Exception e) { log("❌ Add failed: " + e.getMessage()); }
    }

    private void updateCategory() {
        try {
            int id = Integer.parseInt(txtID.getText());
            String name = txtName.getText().trim(), desc = txtDescription.getText().trim();
            contract.updateCategory(BigInteger.valueOf(id), name, desc).send();
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("UPDATE categories SET name=?, description=? WHERE id=?");
            ps.setString(1, name); ps.setString(2, desc); ps.setInt(3, id); ps.executeUpdate();
            log("✏️ Category updated: ID " + id); loadCategories(); clearFields();
        } catch (Exception e) { log("❌ Update failed: " + e.getMessage()); }
    }

    private void deleteCategory() {
        try {
            int id = Integer.parseInt(txtID.getText());
            contract.removeCategory(BigInteger.valueOf(id)).send();
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("UPDATE categories SET is_deleted=true WHERE id=?");
            ps.setInt(1, id); ps.executeUpdate();
            log("🗑️ Category deleted: ID " + id); loadCategories(); clearFields();
        } catch (Exception e) { log("❌ Delete failed: " + e.getMessage()); }
    }

    private void restoreCategory() {
        try {
            int id = Integer.parseInt(txtID.getText());
            contract.restoreCategory(BigInteger.valueOf(id)).send();
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("UPDATE categories SET is_deleted=false WHERE id=?");
            ps.setInt(1, id); ps.executeUpdate();
            log("♻️ Category restored: ID " + id); loadCategories(); clearFields();
        } catch (Exception e) { log("❌ Restore failed: " + e.getMessage()); }
    }

    private void loadCategories() {
        try {
            tableModel.setRowCount(0);
            Connection con = ConnectionProvider.getCon();
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM categories");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_deleted") ? "Deleted" : "Active"
                });
            }
        } catch (Exception e) { log("❌ Load failed: " + e.getMessage()); }
    }

    private void clearFields() {
        txtID.setText(""); txtName.setText(""); txtDescription.setText("");
    }

    private void log(String message) {
        txtLog.append("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
        statusLabel.setText(message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageCategory().setVisible(true));
    }
}