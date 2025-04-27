package login;

import com.formdev.flatlaf.FlatLightLaf;
import dao.ConnectionProvider;
import login.UserSession;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.*;

/**
 * SalesTracker with Summary, Charts & Log tabs, auto-refresh, and professional layout.
 */
public class SalesTracker extends JFrame {
    private static final double HIGH_VALUE_THRESHOLD = 500.0;
    private static final int LOW_STOCK_THRESHOLD    = 5;

    // UI components
    private JLabel lblDaySales, lblWeekSales, lblMonthSales, lblYearSales;
    private JLabel lblDayGross, lblWeekGross, lblMonthGross, lblYearGross;
    private JLabel lblDayIn, lblWeekIn, lblMonthIn, lblYearIn;
    private JLabel lblDayOut, lblWeekOut, lblMonthOut, lblYearOut;
    private DefaultTableModel logModel;
    private JTable tblLog;
    private ScheduledExecutorService scheduler;
    private final String userEmail = UserSession.getInstance().getEmail();

    public SalesTracker() {
        super("📊 Detailed Sales & Inventory Flow");
        FlatLightLaf.setup();
        configureUIColors();
        initComponents();
        scheduleAutoRefresh();
        setSize(1200, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void configureUIColors() {
        UIManager.put("Panel.background",          new Color(245,240,255));
        UIManager.put("Button.background",         new Color(230,210,255));
        UIManager.put("Button.foreground",         new Color(70,70,70));
        UIManager.put("Table.selectionBackground", new Color(210,190,255));
    }

    private void initComponents() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Summary", createSummaryPanel());
        tabs.addTab("Charts",  createChartsPanel());
        tabs.addTab("Log",     createLogPanel());

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshAll());

        JPanel center = new JPanel(new BorderLayout(10,10));
        center.add(tabs, BorderLayout.CENTER);
        center.add(btnRefresh, BorderLayout.SOUTH);

        add(center);
        refreshAll();
    }

    private JPanel createSummaryPanel() {
        JLabel lblUser = new JLabel("👤 " + userEmail);
        lblUser.setFont(lblUser.getFont().deriveFont(Font.BOLD, 16f));
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,5));
        hdr.setBackground(UIManager.getColor("Panel.background"));
        hdr.add(lblUser);

        lblDaySales   = new JLabel(); lblWeekSales  = new JLabel();
        lblMonthSales = new JLabel(); lblYearSales  = new JLabel();
        lblDayGross   = new JLabel(); lblWeekGross  = new JLabel();
        lblMonthGross = new JLabel(); lblYearGross  = new JLabel();
        JPanel salesGrid = makeGrid("Sales Summary",
                new String[]{"Today’s Sales","This Week","This Month","This Year"},
                new JLabel[]{lblDaySales,lblWeekSales,lblMonthSales,lblYearSales},
                new String[]{"Today’s Gross","Week Gross","Month Gross","Year Gross"},
                new JLabel[]{lblDayGross,lblWeekGross,lblMonthGross,lblYearGross}
        );

        lblDayIn   = new JLabel(); lblWeekIn   = new JLabel();
        lblMonthIn = new JLabel(); lblYearIn   = new JLabel();
        lblDayOut  = new JLabel(); lblWeekOut  = new JLabel();
        lblMonthOut= new JLabel(); lblYearOut  = new JLabel();
        JPanel flowGrid = makeGrid("Inventory Flow",
                new String[]{"Inbound Today","Inbound Week","Inbound Month","Inbound Year"},
                new JLabel[]{lblDayIn,lblWeekIn,lblMonthIn,lblYearIn},
                new String[]{"Outbound Today","Outbound Week","Outbound Month","Outbound Year"},
                new JLabel[]{lblDayOut,lblWeekOut,lblMonthOut,lblYearOut}
        );

        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(new EmptyBorder(10,10,10,10));
        p.add(hdr, BorderLayout.NORTH);
        p.add(salesGrid, BorderLayout.CENTER);
        p.add(flowGrid,  BorderLayout.SOUTH);
        return p;
    }

    private JPanel makeGrid(String title, String[] row1Titles, JLabel[] row1Labels,
                            String[] row2Titles, JLabel[] row2Labels) {
        JPanel grid = new JPanel(new GridLayout(2,1,0,10));
        grid.setBorder(BorderFactory.createTitledBorder(title));
        JPanel row1 = new JPanel(new GridLayout(1,4,10,10));
        for(int i=0;i<4;i++) row1.add(makeStat(row1Titles[i], row1Labels[i]));
        JPanel row2 = new JPanel(new GridLayout(1,4,10,10));
        for(int i=0;i<4;i++) row2.add(makeStat(row2Titles[i], row2Labels[i]));
        grid.add(row1);
        grid.add(row2);
        return grid;
    }

    private JPanel makeStat(String title, JLabel val) {
        val.setFont(val.getFont().deriveFont(Font.BOLD, 18f));
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIManager.getColor("Panel.background"));
        p.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
        p.add(val, SwingConstants.CENTER);
        return p;
    }

    private JPanel createChartsPanel() {
        ChartPanel salesChart = new ChartPanel(createTimeSeriesChart(
                "Last 7 Days Revenue", "Date", "Revenue",
                "SELECT DATE(sold_at) dt, SUM(price_per_unit*quantity) val " +
                        "FROM sales GROUP BY dt ORDER BY dt DESC LIMIT 7"
        ));

        TimeSeries inSeries  = new TimeSeries("Inbound");
        TimeSeries outSeries = new TimeSeries("Outbound");
        try (Connection c = ConnectionProvider.getCon();
             Statement s = c.createStatement()) {
            LocalDate today = LocalDate.now();
            for(int i=6;i>=0;i--) {
                LocalDate d = today.minusDays(i);
                String date = "'" + d + "'";
                ResultSet rsIn = s.executeQuery(
                        "SELECT COALESCE(SUM(quantity),0) FROM inout_log " +
                                "WHERE type='IN' AND DATE(time)=" + date);
                rsIn.next(); inSeries.add(new Day(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant())), rsIn.getDouble(1));
                ResultSet rsOut = s.executeQuery(
                        "SELECT COALESCE(SUM(quantity),0) FROM inout_log " +
                                "WHERE type='OUT' AND DATE(time)=" + date);
                rsOut.next(); outSeries.add(new Day(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant())), rsOut.getDouble(1));
            }
        } catch (Exception ignored) {}

        TimeSeriesCollection flowData = new TimeSeriesCollection();
        flowData.addSeries(inSeries);
        flowData.addSeries(outSeries);
        JFreeChart flowChart = ChartFactory.createTimeSeriesChart(
                "Last 7 Days Inbound vs Outbound", "Date", "Quantity",
                flowData, true, true, false);
        ((DateAxis)flowChart.getXYPlot().getDomainAxis())
                .setDateFormatOverride(new java.text.SimpleDateFormat("MMM d"));
        ChartPanel flowPanel = new ChartPanel(flowChart);

        JPanel p = new JPanel(new GridLayout(2,1,10,10));
        p.setBorder(new EmptyBorder(10,10,10,10));
        p.add(salesChart);
        p.add(flowPanel);
        return p;
    }

    private JFreeChart createTimeSeriesChart(String title, String domain, String range, String sql) {
        TimeSeries ts = new TimeSeries(title);
        try (Connection c = ConnectionProvider.getCon();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Date d = rs.getDate("dt");
                ts.add(new Day(d), rs.getDouble("val"));
            }
        } catch (Exception ignored) {}
        TimeSeriesCollection dataset = new TimeSeriesCollection(ts);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, domain, range, dataset, false, true, false);
        ((DateAxis)chart.getXYPlot().getDomainAxis())
                .setDateFormatOverride(new java.text.SimpleDateFormat("MMM d"));
        return chart;
    }

    private JPanel createLogPanel() {
        logModel = new DefaultTableModel(
                new String[]{"Time","User","Product","Qty","Revenue","Cost","Profit"},0
        ) { public boolean isCellEditable(int r,int c){return false;} };
        tblLog = new JTable(logModel);
        JScrollPane scroll = new JScrollPane(tblLog);
        scroll.setBorder(BorderFactory.createTitledBorder("Sales & Stock Log"));
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10,10,10,10));
        p.add(scroll,BorderLayout.CENTER);
        return p;
    }

    private void loadSummary() {
        try (Connection c = ConnectionProvider.getCon();
             Statement s = c.createStatement()) {
            LocalDate today = LocalDate.now();
            String todayC = "DATE(sold_at)='" + today + "'";
            String weekC  = "YEARWEEK(sold_at,1)=YEARWEEK(CURDATE(),1)";
            String monthC = "MONTH(sold_at)=MONTH(CURDATE()) AND YEAR(sold_at)=YEAR(CURDATE())";
            String yearC  = "YEAR(sold_at)=YEAR(CURDATE())";

            lblDaySales.setText(""   + sumOrCount(s, "sales",   "quantity", todayC));
            lblWeekSales.setText(""  + sumOrCount(s, "sales",   "quantity", weekC));
            lblMonthSales.setText(""+ sumOrCount(s, "sales",   "quantity", monthC));
            lblYearSales.setText(""  + sumOrCount(s, "sales",   "quantity", yearC));

            lblDayGross.setText   (formatMoney(sumOrCount(s, "sales", "price_per_unit*quantity", todayC)));
            lblWeekGross.setText  (formatMoney(sumOrCount(s, "sales", "price_per_unit*quantity", weekC)));
            lblMonthGross.setText (formatMoney(sumOrCount(s, "sales", "price_per_unit*quantity", monthC)));
            lblYearGross.setText  (formatMoney(sumOrCount(s, "sales", "price_per_unit*quantity", yearC)));

            String inToday  = "type='IN'  AND DATE(time)='" + today + "'";
            String outToday = "type='OUT' AND DATE(time)='" + today + "'";
            lblDayIn.setText  ("" + sumOrCount(s, "inout_log","quantity", inToday));
            lblDayOut.setText ("" + sumOrCount(s, "inout_log","quantity", outToday));
            lblWeekIn.setText ("" + sumOrCount(s, "inout_log","quantity", weekC.replace("sold_at","time")));
            lblWeekOut.setText(""+ sumOrCount(s, "inout_log","quantity", weekC.replace("sold_at","time")));
            lblMonthIn.setText(""+ sumOrCount(s, "inout_log","quantity", monthC.replace("sold_at","time")));
            lblMonthOut.setText(""+ sumOrCount(s, "inout_log","quantity", monthC.replace("sold_at","time")));
            lblYearIn.setText  ("" + sumOrCount(s, "inout_log","quantity", yearC.replace("sold_at","time")));
            lblYearOut.setText ("" + sumOrCount(s, "inout_log","quantity", yearC.replace("sold_at","time")));

            if (Double.parseDouble(lblDayGross.getText().substring(1)) > HIGH_VALUE_THRESHOLD)
                Toast.show(this, "💰 High‐value sales today!");
            if (lowStockExists(c))
                Toast.show(this, "⚠️ Low stock detected!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Summary load error: " + ex.getMessage());
        }
    }

    private double sumOrCount(Statement s, String table, String expr, String where) throws SQLException {
        ResultSet rs = s.executeQuery(
                "SELECT COALESCE(SUM(" + expr + "),0) FROM " + table + " WHERE " + where);
        rs.next(); return rs.getDouble(1);
    }

    private boolean lowStockExists(Connection c) throws SQLException {
        ResultSet rs = c.createStatement().executeQuery(
                "SELECT COUNT(*) FROM products WHERE quantity<" + LOW_STOCK_THRESHOLD);
        rs.next(); return rs.getInt(1) > 0;
    }

    private void loadLog() {
        logModel.setRowCount(0);
        String sql =
                "SELECT s.sold_at AS time, '" + userEmail + "' AS user, p.name, s.quantity," +
                        " (s.price_per_unit*s.quantity) AS revenue," +
                        " (p.buying_price*s.quantity)    AS cost," +
                        " ((s.price_per_unit-p.buying_price)*s.quantity) AS profit" +
                        " FROM sales s" +
                        " JOIN products p ON s.product_id=p.id" +
                        " UNION ALL" +
                        " SELECT io.time, '<SYSTEM>', p.name, io.quantity, 0,0,0" +
                        " FROM inout_log io" +
                        " JOIN products p ON io.product_id=p.id" +
                        " ORDER BY time DESC";
        try (Connection c = ConnectionProvider.getCon();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                logModel.addRow(new Object[]{
                        rs.getTimestamp("time"),
                        rs.getString("user"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("revenue")==0 ? "" : formatMoney(rs.getDouble("revenue")),
                        rs.getDouble("cost")==0    ? "" : formatMoney(rs.getDouble("cost")),
                        rs.getDouble("profit")==0  ? "" : formatMoney(rs.getDouble("profit"))
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Log load error: " + ex.getMessage());
        }
    }

    private void runBatchExpiryCheck() {
        try (Connection c = ConnectionProvider.getCon();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM batches WHERE expiry_date=CURDATE()")) {
            rs.next();
            if (rs.getInt(1) > 0) Toast.show(this, "⚠️ Batches expiring today!");
        } catch (SQLException ignored) {}
    }

    private void refreshAll() {
        loadSummary();
        loadLog();
    }

    private void scheduleAutoRefresh() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refreshAll, 1, 1, TimeUnit.MINUTES);
    }

    private String formatMoney(double v) {
        return String.format("£%.2f", v);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SalesTracker::new);
    }
}
