
package login;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

public class BlockchainLogger {

    private static final String PRIVATE_KEY = "0xf76265cace8dde5d5a9733f9a648111ecfc8c8056958ee9593bff526a9f39f34";
    private static final String CONTRACT_ADDRESS = "0xd9145CCE52D386f254917e481eB44e9943F39138";

    public static void logAttempt(String email, boolean success) {
        try {
            Web3j web3 = Web3j.build(new HttpService("http://127.0.0.1:7545"));
            Credentials credentials = Credentials.create(PRIVATE_KEY);

            LoginAudit contract = LoginAudit.load(
                CONTRACT_ADDRESS,
                web3,
                credentials,
                new DefaultGasProvider()
            );

            contract.logAttempt(email, success).send();
            System.out.println("✅ Blockchain log success: " + email + " -> " + success);

        } catch (Exception e) {
            System.err.println("❌ Blockchain log failed: " + e.getMessage());
        }
    }
}
