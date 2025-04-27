package login;

import com.formdev.flatlaf.FlatLightLaf;
import dao.ConnectionProvider;
import util.blockchain.BlockchainService;
import login.EmailUtils;
import login.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.UUID;

/**
 * ManageOrders.java
 * – Pastel-orange FlatLaf theme
 * – Smart price-filter + live product stock deduction
 * – Modal popup for Customer Info
 * – Loyalty points, blockchain audit, email & printing
 */
public class ManageOrders extends JFrame {
    // UI components
    private JComboBox<String> cbFilterMin, cbFilterMax;
    private JTable tblProducts, tblCart;
    private DefaultTableModel prodModel, cartModel;
    private JEditorPane invoicePane;
    private JTextField tfQty, tfRedeemPts;
    private JLabel lblSubtotal, lblLoyaltyAmt, lblTotal, lblPts, lblStatus;
    private JButton btnAdd, btnRemove, btnApplyFilter, btnApplyLoyalty;
    private JButton btnFinalize, btnPrint, btnNextCustomer, btnLogout, btnCustomerInfo;
    private JComboBox<String> cbPayment;

    // Business data
    private final DecimalFormat df = new DecimalFormat("#.00");
    private final List<OrderLine> cart = new ArrayList<>();
    private double subtotal = 0, loyaltyDiscount = 0, total = 0;
    private int customerId = -1;
    private int availablePoints = 0;
    private String customerName = "", customerEmail = "";

    // Services
    private final BlockchainService bc = new BlockchainService();
    private final int userId = UserSession.getInstance().getUserId();

    public ManageOrders() {
        super("📦 Order Management");
        FlatLightLaf.setup();                            // Pastel-orange look
        initComponents();
        loadPriceRanges();
        loadProducts();
        setSize(1200,750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(new EmptyBorder(10,10,10,10));
        root.setBackground(new Color(255,245,235));

        // ─── HEADER ─────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        header.setBackground(root.getBackground());
        lblPts           = new JLabel("Points: 0");
        btnCustomerInfo  = new JButton("👤 Customer Info");
        btnNextCustomer  = new JButton("→ Next Customer");
        btnLogout        = new JButton("⎋ Logout");
        header.add(lblPts);
        header.add(btnCustomerInfo);
        header.add(btnNextCustomer);
        header.add(btnLogout);
        root.add(header, BorderLayout.NORTH);

        // ─── WEST: Filter & Products ────────────
        JPanel west = new JPanel(new BorderLayout(5,5));
        west.setPreferredSize(new Dimension(300,0));
        west.setBackground(root.getBackground());

        JPanel filter = new JPanel(new GridLayout(2,3,5,5));
        filter.setBorder(BorderFactory.createTitledBorder("Filter by £"));
        cbFilterMin    = new JComboBox<>();
        cbFilterMax    = new JComboBox<>();
        btnApplyFilter = new JButton("Apply");
        filter.add(new JLabel("Min:")); filter.add(cbFilterMin); filter.add(new JLabel());
        filter.add(new JLabel("Max:")); filter.add(cbFilterMax); filter.add(btnApplyFilter);
        west.add(filter, BorderLayout.NORTH);

        prodModel = new DefaultTableModel(new String[]{"Product","Price","Stock"},0);
        tblProducts = new JTable(prodModel);
        west.add(new JScrollPane(tblProducts), BorderLayout.CENTER);

        root.add(west, BorderLayout.WEST);

        // ─── CENTER: Cart & Invoice ────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        cartModel = new DefaultTableModel(new String[]{"Product","Qty","Unit","Line"},0);
        tblCart   = new JTable(cartModel);
        split.setLeftComponent(new JScrollPane(tblCart));
        invoicePane = new JEditorPane("text/html","");
        invoicePane.setEditable(false);
        split.setRightComponent(new JScrollPane(invoicePane));
        split.setDividerLocation(600);
        root.add(split, BorderLayout.CENTER);

        // ─── SOUTH: Controls ────────────────────
        JPanel south = new JPanel(new GridBagLayout());
        south.setBackground(root.getBackground());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        tfQty         = new JTextField("1",5);
        btnAdd        = new JButton("➕ Add");
        btnRemove     = new JButton("🗑 Remove");
        tfRedeemPts   = new JTextField("0",5);
        btnApplyLoyalty = new JButton("Redeem");
        cbPayment     = new JComboBox<>(new String[]{"Cash","Card","Mobile"});
        lblSubtotal   = new JLabel("Sub: £0.00");
        lblLoyaltyAmt = new JLabel("Disc: £0.00");
        lblTotal      = new JLabel("Total: £0.00");
        btnFinalize   = new JButton("✅ Finalize");
        btnPrint      = new JButton("🖨 Print");
        lblStatus     = new JLabel(" ");

        gbc.gridx=0; gbc.gridy=0; south.add(new JLabel("Qty:"),gbc);
        gbc.gridx=1;               south.add(tfQty,gbc);
        gbc.gridx=2;               south.add(btnAdd,gbc);
        gbc.gridx=3;               south.add(btnRemove,gbc);

        gbc.gridx=0; gbc.gridy=1; south.add(new JLabel("Pts:"),gbc);
        gbc.gridx=1;               south.add(tfRedeemPts,gbc);
        gbc.gridx=2;               south.add(btnApplyLoyalty,gbc);
        gbc.gridx=3;               south.add(new JLabel("Pay via:"),gbc);
        gbc.gridx=4;               south.add(cbPayment,gbc);

        gbc.gridx=0; gbc.gridy=2; south.add(lblSubtotal,gbc);
        gbc.gridx=1;               south.add(lblLoyaltyAmt,gbc);
        gbc.gridx=2;               south.add(lblTotal,gbc);
        gbc.gridx=3;               south.add(btnFinalize,gbc);
        gbc.gridx=4;               south.add(btnPrint,gbc);

        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=5;
        south.add(lblStatus,gbc);

        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);

        // ─── LISTENERS ─────────────────────────
        btnApplyFilter .addActionListener(e -> filterProducts());
        btnAdd         .addActionListener(e -> addToCart());
        btnRemove      .addActionListener(e -> removeFromCart());
        btnApplyLoyalty.addActionListener(e -> { applyLoyalty(); recalcTotals(); });
        // … inside initComponents(), under btnApplyLoyalty …
        tfRedeemPts.getDocument().addDocumentListener(new SimpleDocListener() {
            @Override
            public void update() {
                applyLoyalty();
                recalcTotals();
            }
        });

        btnFinalize    .addActionListener(e -> finalizeOrder());
        btnPrint       .addActionListener(e -> printReceipt());
        btnNextCustomer.addActionListener(e -> resetForNext());
        btnLogout      .addActionListener(e -> dispose());
        btnCustomerInfo.addActionListener(e -> showCustomerDialog());
        tblProducts.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent m){
                if (m.getClickCount()==2) addToCart();
            }
        });
    }

    // ── Customer dialog & loyalty ─────────────────────────────
    private void showCustomerDialog() {
        JDialog dlg = new JDialog(this,"Customer Details",true);
        dlg.setLayout(new GridLayout(5,2,5,5));
        dlg.setSize(400,250);
        dlg.setLocationRelativeTo(this);

        JTextField tfName    = new JTextField(customerName);
        JTextField tfEmail   = new JTextField(customerEmail);
        JTextField tfPhone   = new JTextField();
        JTextField tfAddress = new JTextField();

        dlg.add(new JLabel("Name:"));    dlg.add(tfName);
        dlg.add(new JLabel("Email:"));   dlg.add(tfEmail);
        dlg.add(new JLabel("Phone:"));   dlg.add(tfPhone);
        dlg.add(new JLabel("Address:")); dlg.add(tfAddress);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> {
            customerName  = tfName.getText().trim();
            customerEmail = tfEmail.getText().trim();
            loadOrCreateCustomer(customerName, customerEmail,
                    tfPhone.getText().trim(),
                    tfAddress.getText().trim());
            dlg.dispose();
        });
        dlg.add(new JLabel()); dlg.add(btnOk);
        dlg.setVisible(true);
    }

    private void loadOrCreateCustomer(String name, String email, String phone, String address) {
        if (name.isBlank() || email.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Name & Email required","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection c = ConnectionProvider.getCon()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT id,points FROM customers WHERE name=? AND email=?");
            ps.setString(1, name);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                customerId     = rs.getInt("id");
                availablePoints= rs.getInt("points");
            } else {
                PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO customers(name,email,phone,address,points) VALUES(?,?,?,?,0)",
                        Statement.RETURN_GENERATED_KEYS);
                ins.setString(1, name);
                ins.setString(2, email);
                ins.setString(3, phone);
                ins.setString(4, address);
                ins.executeUpdate();
                ResultSet rk = ins.getGeneratedKeys();
                rk.next();
                customerId = rk.getInt(1);
                availablePoints = 0;
            }
            lblPts.setText("Points: "+availablePoints);
            lblStatus.setText("Loaded Customer #"+customerId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Customer error: "+ex.getMessage(),
                    "Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Product filtering ────────────────────────────────────────
    private void loadPriceRanges() {
        Set<Double> prices = new TreeSet<>();
        try (Connection c=ConnectionProvider.getCon();
             ResultSet rs=c.createStatement()
                     .executeQuery("SELECT DISTINCT price FROM products")) {
            while (rs.next()) prices.add(rs.getDouble(1));
        } catch (Exception e) { }
        for (double p : prices) {
            String s = df.format(p);
            cbFilterMin.addItem(s);
            cbFilterMax.addItem(s);
        }
        if (cbFilterMin.getItemCount()>0) cbFilterMin.setSelectedIndex(0);
        if (cbFilterMax.getItemCount()>0) cbFilterMax.setSelectedIndex(cbFilterMax.getItemCount()-1);
    }

    private void filterProducts() {
        try {
            double min = Double.parseDouble((String)cbFilterMin.getSelectedItem());
            double max = Double.parseDouble((String)cbFilterMax.getSelectedItem());
            prodModel.setRowCount(0);
            try (Connection c = ConnectionProvider.getCon();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT name,price,quantity FROM products WHERE price BETWEEN ? AND ?")) {
                ps.setDouble(1, min);
                ps.setDouble(2, max);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        prodModel.addRow(new Object[]{
                                rs.getString("name"),
                                df.format(rs.getDouble("price")),
                                rs.getInt("quantity")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Filter error: "+ex.getMessage(),
                    "Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProducts() {
        filterProducts();
    }

    // ── Cart management ───────────────────────────────────────────
    private void addToCart() {
        int r = tblProducts.getSelectedRow();
        if (r<0) { JOptionPane.showMessageDialog(this,"Select a product"); return; }
        String prod = (String)prodModel.getValueAt(r,0);
        int stock = Integer.parseInt(prodModel.getValueAt(r,2).toString());
        int qty;
        try { qty = Integer.parseInt(tfQty.getText().trim()); }
        catch(Exception ex){ JOptionPane.showMessageDialog(this,"Bad quantity"); return; }
        if (qty<1 || qty>stock) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be 1–"+stock);
            return;
        }
        double unit = Double.parseDouble(prodModel.getValueAt(r,1).toString());
        OrderLine L = new OrderLine(prod,unit,qty);
        cart.add(L);
        cartModel.addRow(new Object[]{prod,qty,df.format(unit),df.format(L.line)});
        prodModel.setValueAt(stock-qty, r,2);
        recalcTotals();
    }

    private void removeFromCart() {
        int r = tblCart.getSelectedRow();
        if (r<0) return;
        OrderLine L = cart.remove(r);
        cartModel.removeRow(r);
        for (int i=0;i<prodModel.getRowCount();i++){
            if (prodModel.getValueAt(i,0).equals(L.product)){
                int cur = Integer.parseInt(prodModel.getValueAt(i,2).toString());
                prodModel.setValueAt(cur+L.qty, i,2);
                break;
            }
        }
        recalcTotals();
    }

    // ── Loyalty logic ─────────────────────────────────────────────
    private void applyLoyalty() {
        try {
            int pts = Integer.parseInt(tfRedeemPts.getText().trim());
            if (pts<0 || pts>availablePoints) {
                JOptionPane.showMessageDialog(this,
                        "Pts must be 0–"+availablePoints);
                pts = 0;
                tfRedeemPts.setText("0");
            }
            loyaltyDiscount = pts * 0.005;
        } catch (Exception ex) {
            loyaltyDiscount = 0;
            tfRedeemPts.setText("0");
        }
    }

    private void recalcTotals() {
        subtotal = cart.stream().mapToDouble(o->o.line).sum();
        lblSubtotal.setText("Sub: £"+df.format(subtotal));
        lblLoyaltyAmt.setText("Disc: £"+df.format(loyaltyDiscount));
        total = subtotal - loyaltyDiscount;
        lblTotal.setText("Total: £"+df.format(total));
        rebuildInvoice();
    }

    private void rebuildInvoice() {
        var sb = new StringBuilder("<html><body><h2>Invoice</h2><pre>");
        for (OrderLine L : cart) {
            sb.append(L.product)
                    .append(" ×").append(L.qty)
                    .append(" @£").append(df.format(L.unit))
                    .append(" =£").append(df.format(L.line))
                    .append("\n");
        }
        sb.append("\nSubtotal: £").append(df.format(subtotal))
                .append("\nDisc: -£").append(df.format(loyaltyDiscount))
                .append("\nTotal: £").append(df.format(total))
                .append("</pre></body></html>");
        invoicePane.setText(sb.toString());
    }

    // ── Complete & persist order ────────────────────────────────────
    private void finalizeOrder() {
        if (cart.isEmpty() || customerId<0) {
            JOptionPane.showMessageDialog(this,
                    "Add items & select customer");
            return;
        }
        String ref = UUID.randomUUID().toString().substring(0,8).toUpperCase();
        try (Connection c=ConnectionProvider.getCon()) {
            c.setAutoCommit(false);

            var oh = c.prepareStatement(
                    "INSERT INTO orders(ref,customer_id,subtotal,loyalty_disc,total,created_at) VALUES(?,?,?,?,?,NOW())",
                    Statement.RETURN_GENERATED_KEYS);
            oh.setString(1,ref);
            oh.setInt(2,customerId);
            oh.setDouble(3,subtotal);
            oh.setDouble(4,loyaltyDiscount);
            oh.setDouble(5,total);
            oh.executeUpdate();
            var rk = oh.getGeneratedKeys(); rk.next();
            int orderId = rk.getInt(1);

            var ol = c.prepareStatement(
                    "INSERT INTO order_lines(order_id,product,qty,unit,line) VALUES(?,?,?,?,?)");
            var st = c.prepareStatement(
                    "UPDATE products SET quantity=quantity-? WHERE name=?");
            for (OrderLine L: cart) {
                ol.setInt(1,orderId);
                ol.setString(2,L.product);
                ol.setInt(3,L.qty);
                ol.setDouble(4,L.unit);
                ol.setDouble(5,L.line);
                ol.executeUpdate();
                st.setInt(1,L.qty);
                st.setString(2,L.product);
                st.executeUpdate();
            }

            int usedPts = Integer.parseInt(tfRedeemPts.getText().trim());
            int earned = (int)Math.floor(subtotal);
            var up = c.prepareStatement(
                    "UPDATE customers SET points=points-?+? WHERE id=?");
            up.setInt(1,usedPts);
            up.setInt(2,earned);
            up.setInt(3,customerId);
            up.executeUpdate();

            c.commit();

            bc.recordTransaction("ORDER", ref+"|UID="+userId+"|CID="+customerId);
            EmailUtils.sendEmail(customerEmail,
                    "Invoice #"+ref,
                    invoicePane.getText());

            JOptionPane.showMessageDialog(this,
                    "Order #"+ref+" confirmed\nPts earned: "+earned);
            availablePoints = availablePoints - usedPts + earned;
            lblPts.setText("Points: "+availablePoints);
            lblStatus.setText("Order #"+ref+" saved.");
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this,
                    "Save error: "+ex.getMessage(),
                    "Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Print the receipt ───────────────────────────────────────────
    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Receipt");
        job.setPrintable((g,pf,page)->{
            if (page>0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D)g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            invoicePane.paint(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); }
            catch(Exception ex) { JOptionPane.showMessageDialog(this,"Print error"); }
        }
    }

    // ── Reset for next customer ─────────────────────────────────────
    private void resetForNext() {
        cart.clear();
        cartModel.setRowCount(0);
        subtotal = loyaltyDiscount = total = 0;
        recalcTotals();
        invoicePane.setText("");
        customerId = -1;
        availablePoints = 0;
        lblPts.setText("Points: 0");
        lblStatus.setText("Ready");
        loadProducts();
    }

    // ── Simple holder for cart lines ────────────────────────────────
    private static class OrderLine {
        String product; double unit; int qty; double line;
        OrderLine(String p, double u, int q) {
            product = p; unit = u; qty = q; line = u*q;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageOrders().setVisible(true));
    }

    // ── Helper for live document listening ──────────────────────────
    @FunctionalInterface
    private interface SimpleDocListener extends DocumentListener {
        void update();
        @Override default void insertUpdate(DocumentEvent e) { update(); }
        @Override default void removeUpdate(DocumentEvent e) { update(); }
        @Override default void changedUpdate(DocumentEvent e) { update(); }
    }
}
