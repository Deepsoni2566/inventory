package contracts;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.tx.Contract.EventValuesWithLog;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * Auto-generated (and enhanced) wrapper for the CategoryManager smart contract.
 */
public class CategoryManager extends Contract {
    public static final String BINARY = "<YOUR-BINARY-HERE>";

    public static final String FUNC_ADDCATEGORY = "addCategory";
    public static final String FUNC_UPDATECATEGORY = "updateCategory";
    public static final String FUNC_REMOVECATEGORY = "removeCategory";
    public static final String FUNC_RESTORECATEGORY = "restoreCategory";
    public static final String FUNC_GETALLCATEGORIES = "getAllCategories";

    public static final Event CATEGORYADDED_EVENT = new Event("CategoryAdded",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Utf8String>(false) {}
            )
    );

    protected CategoryManager(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    public static CategoryManager load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new CategoryManager(contractAddress, web3j, credentials, gasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> addCategory(String name, String description) {
        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ADDCATEGORY,
                Arrays.asList(new Utf8String(name), new Utf8String(description)),
                Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateCategory(BigInteger id, String name, String description) {
        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_UPDATECATEGORY,
                Arrays.asList(new Uint256(id), new Utf8String(name), new Utf8String(description)),
                Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> removeCategory(BigInteger id) {
        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REMOVECATEGORY,
                Arrays.asList(new Uint256(id)),
                Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> restoreCategory(BigInteger id) {
        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RESTORECATEGORY,
                Arrays.asList(new Uint256(id)),
                Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<List<Type>> getAllCategories() {
        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_GETALLCATEGORIES,
                Collections.emptyList(),
                Arrays.asList(
                        new TypeReference<DynamicArray<Uint256>>() {},
                        new TypeReference<DynamicArray<Utf8String>>() {},
                        new TypeReference<DynamicArray<Bool>>() {},
                        new TypeReference<DynamicArray<Utf8String>>() {},
                        new TypeReference<DynamicArray<Uint256>>() {}
                )
        );
        return executeRemoteCallMultipleValueReturn(function);
    }

    public List<CategoryAddedEventResponse> getCategoryAddedEvents(TransactionReceipt receipt) {
        List<EventValuesWithLog> values = extractEventParametersWithLog(CATEGORYADDED_EVENT, receipt);
        List<CategoryAddedEventResponse> responses = new ArrayList<>();
        for (EventValuesWithLog ev : values) {
            CategoryAddedEventResponse resp = new CategoryAddedEventResponse();
            resp.log = ev.getLog();
            resp.id = (BigInteger) ev.getIndexedValues().get(0).getValue();
            resp.name = (String) ev.getNonIndexedValues().get(0).getValue();
            responses.add(resp);
        }
        return responses;
    }

    public Flowable<CategoryAddedEventResponse> categoryAddedEventFlowable(DefaultBlockParameter start, DefaultBlockParameter end) {
        EthFilter filter = new EthFilter(start, end, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CATEGORYADDED_EVENT));
        return web3j.ethLogFlowable(filter).map(log -> {
            EventValues ev = extractEventParameters(CATEGORYADDED_EVENT, log);
            CategoryAddedEventResponse resp = new CategoryAddedEventResponse();
            resp.log = log;
            resp.id = (BigInteger) ev.getIndexedValues().get(0).getValue();
            resp.name = (String) ev.getNonIndexedValues().get(0).getValue();
            return resp;
        });
    }

    public static class CategoryAddedEventResponse {
        public Log log;
        public BigInteger id;
        public String name;
    }
}
