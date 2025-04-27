package login;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import dao.ConnectionProvider;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String email;
    private int sessionId;
    private long loginTime;
    private Web3j web3;
    private Credentials credentials;

    private UserSession() {
        this.web3 = Web3j.build(new HttpService("http://127.0.0.1:7545"));
        this.credentials = Credentials.create("0xa83c03bf70a5bd8ccced28ab583697734b2f3d8ad6b0f74a74b64dfc0de4e256");
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public Web3j getWeb3() {
        return web3;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void startSession(int userId, String email) {
        this.userId = userId;
        this.email = email;
        this.loginTime = System.currentTimeMillis();
        this.sessionId = createSessionInDB();
    }

    public void endSession() {
        updateSessionEndTime();
        clearSession();
    }

    private int createSessionInDB() {
        try (Connection conn = ConnectionProvider.getCon();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO user_sessions (user_id, login_time, ip_address) VALUES (?, NOW(), ?)",
                     Statement.RETURN_GENERATED_KEYS
             )) {
            ps.setInt(1, userId);
            ps.setString(2, InetAddress.getLocalHost().getHostAddress());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void updateSessionEndTime() {
        try (Connection conn = ConnectionProvider.getCon();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE user_sessions SET logout_time = NOW() WHERE id = ?"
             )) {
            ps.setInt(1, sessionId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logActivity(String actionType, String details, String txHash) {
        try (Connection conn = ConnectionProvider.getCon();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO user_activities (session_id, action_type, details, tx_hash) " +
                             "VALUES (?, ?, ?, ?)"
             )) {
            ps.setInt(1, sessionId);
            ps.setString(2, actionType);
            ps.setString(3, details);
            ps.setString(4, txHash);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logSale(int productId, int quantity, double pricePerUnit, String txHash) {
        try (Connection conn = ConnectionProvider.getCon();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO sales (user_id, session_id, product_id, quantity, " +
                             "price_per_unit, total_amount, tx_hash) VALUES (?, ?, ?, ?, ?, ?, ?)"
             )) {
            double totalAmount = quantity * pricePerUnit;
            ps.setInt(1, userId);
            ps.setInt(2, sessionId);
            ps.setInt(3, productId);
            ps.setInt(4, quantity);
            ps.setDouble(5, pricePerUnit);
            ps.setDouble(6, totalAmount);
            ps.setString(7, txHash);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSessionSummary() {
        StringBuilder summary = new StringBuilder();
        try (Connection conn = ConnectionProvider.getCon()) {
            // Get total sales
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) as total_sales, SUM(total_amount) as total_amount " +
                            "FROM sales WHERE session_id = ?"
            );
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                summary.append(String.format("Total Sales: %d\n", rs.getInt("total_sales")));
                summary.append(String.format("Total Amount: $%.2f\n", rs.getDouble("total_amount")));
            }

            // Get activities
            ps = conn.prepareStatement(
                    "SELECT action_type, COUNT(*) as count FROM user_activities " +
                            "WHERE session_id = ? GROUP BY action_type"
            );
            ps.setInt(1, sessionId);
            rs = ps.executeQuery();
            summary.append("\nActivities:\n");
            while (rs.next()) {
                summary.append(String.format("%s: %d\n",
                        rs.getString("action_type"), rs.getInt("count")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summary.toString();
    }

    // Getters
    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public int getSessionId() { return sessionId; }
    public long getLoginTime() { return loginTime; }

    private void clearSession() {
        userId = 0;
        email = null;
        sessionId = 0;
        loginTime = 0;
    }
}