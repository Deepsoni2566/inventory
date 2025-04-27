package util.blockchain;

import contracts.CategoryManager;
import contracts.ProductManager;
import io.reactivex.Flowable;
import login.UserSession;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class BlockchainService {
    private final Web3j web3;
    private final Credentials credentials;
    private final RawTransactionManager txManager;
    private final CategoryManager categoryManager;
    private final ProductManager productManager;

    private static final String CATEGORY_CONTRACT_ADDRESS =
            System.getenv().getOrDefault("CATEGORY_CONTRACT_ADDRESS", "0xD7ACd2a9FD159E69Bb102A1ca21C9a3e3A5F771B");

    private static final String PRODUCT_CONTRACT_ADDRESS =
            System.getenv().getOrDefault("PRODUCT_CONTRACT_ADDRESS", "0xDA0bab807633f07f013f94DD0E6A4F96F8742B53");

    public BlockchainService() {
        UserSession session = UserSession.getInstance();
        this.web3 = session.getWeb3();
        this.credentials = session.getCredentials();
        this.txManager = new RawTransactionManager(web3, credentials);

        this.categoryManager = CategoryManager.load(CATEGORY_CONTRACT_ADDRESS, web3, credentials, new DefaultGasProvider());
        this.productManager = ProductManager.load(PRODUCT_CONTRACT_ADDRESS, web3, credentials, new DefaultGasProvider());
    }

    public boolean isConnected() {
        try {
            web3.web3ClientVersion().send();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===== CATEGORY METHODS =====

    public CompletableFuture<TransactionReceipt> addCategory(String name, String description) {
        return categoryManager.addCategory(name, description).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> updateCategory(BigInteger id, String name, String description) {
        return categoryManager.updateCategory(id, name, description).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> removeCategory(BigInteger id) {
        return categoryManager.removeCategory(id).sendAsync();
    }

    // ===== PRODUCT METHODS =====

    public CompletableFuture<TransactionReceipt> addProduct(String name, String category, String description,
                                                            BigInteger quantity, BigInteger price, BigInteger vat) {
        return productManager.addProduct(name, category, description, quantity, price, vat).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> updateProduct(BigInteger id, String name, String category,
                                                               String description, BigInteger quantity, BigInteger price) {
        return productManager.updateProduct(id, name, category, description, quantity, price).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> deleteProduct(BigInteger id) {
        return productManager.deleteProduct(id).sendAsync();
    }

    // ===== EVENTS =====

    public Flowable<CategoryManager.CategoryAddedEventResponse> subscribeCategoryEvents() {
        return categoryManager.categoryAddedEventFlowable(
                DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST);
    }

    public Flowable<ProductManager.ProductAddedEventResponse> subscribeProductEvents() {
        return productManager.productAddedEventFlowable(
                DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST);
    }

    // ===== AUDIT LOGGING =====

    public void recordTransaction(String action, String data) {
        try {
            String payload = action + ":" + data;
            String hexPayload = Numeric.toHexString(payload.getBytes(StandardCharsets.UTF_8));

            EthSendTransaction tx = txManager.sendTransaction(
                    DefaultGasProvider.GAS_PRICE,
                    DefaultGasProvider.GAS_LIMIT,
                    credentials.getAddress(),
                    hexPayload,
                    BigInteger.ZERO
            );

            if (tx.hasError()) {
                System.err.println("❌ Audit TX Error: " + tx.getError().getMessage());
            } else {
                System.out.println("✅ Audit TX Hash: " + tx.getTransactionHash());
            }
        } catch (Exception e) {
            System.err.println("❌ Blockchain recordTransaction failed: " + e.getMessage());
        }
    }

    // ===== LOG SALE =====

    public void logSale(String productName, int quantity, double total, int userId, String note) {
        try {
            String data = String.format(
                    "Sale | Product: %s | Qty: %d | Total: £%.2f | User: %d | %s",
                    productName, quantity, total, userId, note
            );
            recordTransaction("SALE", data);
            System.out.println("✅ Sale logged to blockchain.");
        } catch (Exception e) {
            System.err.println("❌ Error logging sale: " + e.getMessage());
        }
    }

    // ===== LOG PURCHASE =====

    /**
     * Log a purchase transaction for blockchain audit.
     *
     * @param customerId  Customer ID
     * @param productId   Product ID
     * @param quantity    Quantity purchased
     * @param totalAmount Total purchase amount
     */
    public void logPurchase(int customerId, int productId, int quantity, double totalAmount) {
        try {
            // Prepare the data to be logged on the blockchain
            String data = String.format("Purchase | Customer: %d | Product: %d | Qty: %d | Total: £%.2f",
                    customerId, productId, quantity, totalAmount);

            // Log the transaction to the blockchain
            recordTransaction("PURCHASE", data);
            System.out.println("✅ Purchase logged to blockchain.");
        } catch (Exception e) {
            System.err.println("❌ Error logging purchase: " + e.getMessage());
        }
    }
}
