package login;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified Home Page for the application, integrating Inventory, Orders, and Category modules.
 */
public class Login extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnToggle;
    private JCheckBox chkRememberMe;
    private JLabel lblError;
    private Map<String, String> credentials;

    public Login() {
        initLookAndFeel();
        initCredentials();
        initLoginComponents();
    }

    private void initLookAndFeel() {
        FlatLightLaf.setup();
    }

    private void initCredentials() {
        credentials = new HashMap<>();
        credentials.put("admin", "admin");
        credentials.put("1", "2");
        credentials.put("sonideep022", "admin");
    }

    private void initLoginComponents() {
        setTitle("🔒 Secure Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 360);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(248, 244, 255));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("👋 Welcome Back");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(90, 90, 140));
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(20));

        txtEmail = new JTextField();
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtEmail.setBorder(BorderFactory.createTitledBorder("📧 Username/ID"));
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(10));

        JPanel passPanel = new JPanel(new BorderLayout(5, 0));
        passPanel.setBackground(panel.getBackground());
        txtPassword = new JPasswordField();
        txtPassword.setBorder(BorderFactory.createTitledBorder("🔑 Password"));
        passPanel.add(txtPassword, BorderLayout.CENTER);
        btnToggle = new JButton("👁");
        btnToggle.setToolTipText("Show/Hide Password");
        btnToggle.setBackground(new Color(235, 235, 255));
        btnToggle.addActionListener(e -> togglePassword());
        passPanel.add(btnToggle, BorderLayout.EAST);
        panel.add(passPanel);
        panel.add(Box.createVerticalStrut(10));

        chkRememberMe = new JCheckBox("Remember Me");
        chkRememberMe.setBackground(panel.getBackground());
        panel.add(chkRememberMe);
        panel.add(Box.createVerticalStrut(15));

        btnLogin = new JButton("🔑 Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setPreferredSize(new Dimension(120, 40));
        btnLogin.setMaximumSize(btnLogin.getPreferredSize());
        btnLogin.setBackground(new Color(210, 210, 255));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.addActionListener(this::performLogin);
        panel.add(btnLogin);
        panel.add(Box.createVerticalStrut(15));

        lblError = new JLabel(" ");
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setForeground(new Color(200, 50, 50));
        panel.add(lblError);

        setContentPane(panel);
    }

    private void togglePassword() {
        if (txtPassword.getEchoChar() != '\u0000') {
            txtPassword.setEchoChar((char) 0);
        } else {
            txtPassword.setEchoChar('*');
        }
    }

    private void performLogin(ActionEvent evt) {
        String user = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("⚠️ Fill in all fields");
            return;
        }
        String expected = credentials.get(user);
        if (expected != null && expected.equals(pass)) {
            lblError.setText(" ");
            UserSession.getInstance().startSession(user.hashCode(), user);
            SwingUtilities.invokeLater(() -> new HomePageUI(user).setVisible(true));
            dispose();
        } else {
            lblError.setText("🚫 Invalid user or password");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}

class HomePageUI extends JFrame {
    public HomePageUI(String userEmail) {
        initLookAndFeel();
        initComponents(userEmail);
    }

    private void initLookAndFeel() {
        FlatLightLaf.setup();
    }

    private void initComponents(String userEmail) {
        setTitle("🏠 Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 250, 240));

        JLabel lblWelcome = new JLabel("👋 Hello, " + userEmail + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setBorder(new EmptyBorder(20, 0, 10, 0));
        lblWelcome.setForeground(new Color(80, 80, 100));
        main.add(lblWelcome, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(2, 2, 20, 20));
        menu.setBorder(new EmptyBorder(30, 50, 30, 50));
        menu.setBackground(main.getBackground());

        JButton btnInventory = new JButton("📦 Inventory");
        btnInventory.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnInventory.setBackground(new Color(230, 240, 255));
        btnInventory.addActionListener(e -> new ProductManager().setVisible(true));

        JButton btnOrders = new JButton("🛒 Orders");
        btnOrders.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnOrders.setBackground(new Color(230, 240, 255));
        btnOrders.addActionListener(e -> new ManageOrders().setVisible(true));

        JButton btnCategories = new JButton("📂 Categories");
        btnCategories.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnCategories.setBackground(new Color(230, 240, 255));
        btnCategories.addActionListener(e -> new ManageCategory().setVisible(true));

        JButton btnLogout = new JButton("🔓 Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnLogout.setBackground(new Color(255, 220, 220));
        btnLogout.addActionListener(e -> {
            UserSession.getInstance().endSession();
            dispose();
            new Login().setVisible(true);
        });

        menu.add(btnInventory);
        menu.add(btnOrders);
        menu.add(btnCategories);
        menu.add(btnLogout);

        main.add(menu, BorderLayout.CENTER);
        setContentPane(main);
    }
}
