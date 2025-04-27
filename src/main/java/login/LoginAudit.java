// SPDX-License-Identifier: MIT
// Auto-generated wrapper for LoginAudit smart contract (Web3j 4.8.7 compatible)
package login;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.crypto.Credentials;
import org.web3j.tx.TransactionManager;

public class LoginAudit extends Contract {
    public static final String BINARY = "0x..."; // Your contract bytecode (optional)

    protected LoginAudit(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected LoginAudit(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static LoginAudit load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new LoginAudit(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static LoginAudit load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new LoginAudit(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> logAttempt(String email, Boolean success) {
        final Function function = new Function(
                "logAttempt",
                Arrays.asList(new Utf8String(email), new Bool(success)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }
}
