package contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.tuples.generated.Tuple8;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class ProductManager extends Contract {
    public static final String BINARY = "6080604052348015600e575f80fd5b50610cdb8061001c5f395ff3fe608060405234801561000f575f80fd5b5060043610610060575f3560e01c806361b8ce8c14610064578063744e97331461007f5780637acc0b2014610094578063b9db15b4146100bb578063bab8d872146100e0578063ed90c7b7146100f3575b5f80fd5b61006c5f5481565b6040519081526020015b60405180910390f35b61009261008d3660046108d1565b610106565b005b6100a76100a236600461097a565b61022a565b6040516100769897969594939291906109bf565b6100ce6100c936600461097a565b6103ff565b60405161007696959493929190610a29565b6100926100ee366004610a80565b6106a6565b61009261010136600461097a565b610797565b6040518061010001604052805f5481526020018681526020018781526020018581526020018481526020018381526020018281526020015f151581525060015f805481526020019081526020015f205f820151815f015560208201518160010190816101729190610bad565b50604082015160028201906101879082610bad565b506060820151600382019061019c9082610bad565b506080820151600482015560a0820151600582015560c0820151600682015560e0909101516007909101805460ff19169115159190911790555f546040517f259101de1f87b04b9ee0332ce5e3cc508014004bf283424aead6cd8692f5cf9890610207908990610c68565b60405180910390a25f8054908061021d83610c81565b9190505550505050505050565b600160208190525f91825260409091208054918101805461024a90610b29565b80601f016020809104026020016040519081016040528092919081815260200182805461027690610b29565b80156102c15780601f10610298576101008083540402835291602001916102c1565b820191905f5260205f20905b8154815290600101906020018083116102a457829003601f168201915b5050505050908060020180546102d690610b29565b80601f016020809104026020016040519081016040528092919081815260200182805461030290610b29565b801561034d5780601f106103245761010080835404028352916020019161034d565b820191905f5260205f20905b81548152906001019060200180831161033057829003601f168201915b50505050509080600301805461036290610b29565b80601f016020809104026020016040519081016040528092919081815260200182805461038e90610b29565b80156103d95780601f106103b0576101008083540402835291602001916103d9565b820191905f5260205f20905b8154815290600101906020018083116103bc57829003601f168201915b505050506004830154600584015460068501546007909501549394919390925060ff1688565b5f8181526001602052604081206007015460609182918291908190819060ff16156104665760405162461bcd60e51b8152602060048201526012602482015271141c9bd91d58dd081a5cc819195b195d195960721b60448201526064015b60405180910390fd5b5f8781526001602081815260408084208151610100810190925280548252928301805491939284019161049890610b29565b80601f01602080910402602001604051908101604052809291908181526020018280546104c490610b29565b801561050f5780601f106104e65761010080835404028352916020019161050f565b820191905f5260205f20905b8154815290600101906020018083116104f257829003601f168201915b5050505050815260200160028201805461052890610b29565b80601f016020809104026020016040519081016040528092919081815260200182805461055490610b29565b801561059f5780601f106105765761010080835404028352916020019161059f565b820191905f5260205f20905b81548152906001019060200180831161058257829003601f168201915b505050505081526020016003820180546105b890610b29565b80601f01602080910402602001604051908101604052809291908181526020018280546105e490610b29565b801561062f5780601f106106065761010080835404028352916020019161062f565b820191905f5260205f20905b81548152906001019060200180831161061257829003601f168201915b50505050508152602001600482015481526020016005820154815260200160068201548152602001600782015f9054906101000a900460ff161515151581525050905080604001518160200151826060015183608001518460a001518560c001519650965096509650965096505091939550919395565b5f8681526001602052604090206007015460ff16156106fc5760405162461bcd60e51b8152602060048201526012602482015271141c9bd91d58dd081a5cc819195b195d195960721b604482015260640161045d565b5f8681526001602052604090206002016107168682610bad565b505f868152600160208190526040909120016107328582610bad565b505f86815260016020526040902060030161074d8482610bad565b505f86815260016020526040808220600481018590556005018390555187917feca6714fcf568fb34cf9db4a414cb0cc48e93c5d4e1ba0582a46ea3704cf24d591a2505050505050565b5f8181526001602052604090206007015460ff16156107ea5760405162461bcd60e51b815260206004820152600f60248201526e105b1c9958591e4819195b195d1959608a1b604482015260640161045d565b5f818152600160208190526040808320600701805460ff19169092179091555182917fc341cf326465fe622f86c468249e8c0cb03ed23ba376a6a34863a1374ac7d76991a250565b634e487b7160e01b5f52604160045260245ffd5b5f82601f830112610855575f80fd5b813567ffffffffffffffff81111561086f5761086f610832565b604051601f8201601f19908116603f0116810167ffffffffffffffff8111828210171561089e5761089e610832565b6040528181528382016020018510156108b5575f80fd5b816020850160208301375f918101602001919091529392505050565b5f805f805f8060c087890312156108e6575f80fd5b863567ffffffffffffffff8111156108fc575f80fd5b61090889828a01610846565b965050602087013567ffffffffffffffff811115610924575f80fd5b61093089828a01610846565b955050604087013567ffffffffffffffff81111561094c575f80fd5b61095889828a01610846565b969995985095966060810135965060808101359560a090910135945092505050565b5f6020828403121561098a575f80fd5b5035919050565b5f81518084528060208401602086015e5f602082860101526020601f19601f83011685010191505092915050565b88815261010060208201525f6109d961010083018a610991565b82810360408401526109eb818a610991565b905082810360608401526109ff8189610991565b9150508560808301528460a08301528360c083015282151560e08301529998505050505050505050565b60c081525f610a3b60c0830189610991565b8281036020840152610a4d8189610991565b90508281036040840152610a618188610991565b60608401969096525050608081019290925260a0909101529392505050565b5f805f805f8060c08789031215610a95575f80fd5b86359550602087013567ffffffffffffffff811115610ab2575f80fd5b610abe89828a01610846565b955050604087013567ffffffffffffffff811115610ada575f80fd5b610ae689828a01610846565b945050606087013567ffffffffffffffff811115610b02575f80fd5b610b0e89828a01610846565b9699959850939660808101359560a090910135945092505050565b600181811c90821680610b3d57607f821691505b602082108103610b5b57634e487b7160e01b5f52602260045260245ffd5b50919050565b601f821115610ba857805f5260205f20601f840160051c81016020851015610b865750805b601f840160051c820191505b81811015610ba5575f8155600101610b92565b50505b505050565b815167ffffffffffffffff811115610bc757610bc7610832565b610bdb81610bd58454610b29565b84610b61565b6020601f821160018114610c0d575f8315610bf65750848201515b5f19600385901b1c1916600184901b178455610ba5565b5f84815260208120601f198516915b82811015610c3c5787850151825560209485019460019092019101610c1c565b5084821015610c5957868401515f19600387901b60f8161c191681555b50505050600190811b01905550565b602081525f610c7a6020830184610991565b9392505050565b5f60018201610c9e57634e487b7160e01b5f52601160045260245ffd5b506001019056fea2646970667358221220754e0447a54b0739d21d5317f49442b08a88075732907d8132134ed2de503f0664736f6c634300081a0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_ADDPRODUCT = "addProduct";

    public static final String FUNC_DELETEPRODUCT = "deleteProduct";

    public static final String FUNC_UPDATEPRODUCT = "updateProduct";

    public static final String FUNC_GETPRODUCT = "getProduct";

    public static final String FUNC_NEXTID = "nextId";

    public static final String FUNC_PRODUCTS = "products";

    public static final Event PRODUCTADDED_EVENT = new Event("ProductAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event PRODUCTDELETED_EVENT = new Event("ProductDeleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event PRODUCTUPDATED_EVENT = new Event("ProductUpdated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    @Deprecated
    protected ProductManager(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ProductManager(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected ProductManager(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected ProductManager(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> addProduct(String name, String category,
            String description, BigInteger quantity, BigInteger price, BigInteger vat) {
        final Function function = new Function(
                FUNC_ADDPRODUCT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name), 
                new org.web3j.abi.datatypes.Utf8String(category), 
                new org.web3j.abi.datatypes.Utf8String(description), 
                new org.web3j.abi.datatypes.generated.Uint256(quantity), 
                new org.web3j.abi.datatypes.generated.Uint256(price), 
                new org.web3j.abi.datatypes.generated.Uint256(vat)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> deleteProduct(BigInteger id) {
        final Function function = new Function(
                FUNC_DELETEPRODUCT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static List<ProductAddedEventResponse> getProductAddedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PRODUCTADDED_EVENT, transactionReceipt);
        ArrayList<ProductAddedEventResponse> responses = new ArrayList<ProductAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ProductAddedEventResponse typedResponse = new ProductAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.name = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ProductAddedEventResponse getProductAddedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PRODUCTADDED_EVENT, log);
        ProductAddedEventResponse typedResponse = new ProductAddedEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.name = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ProductAddedEventResponse> productAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getProductAddedEventFromLog(log));
    }

    public Flowable<ProductAddedEventResponse> productAddedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PRODUCTADDED_EVENT));
        return productAddedEventFlowable(filter);
    }

    public static List<ProductDeletedEventResponse> getProductDeletedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PRODUCTDELETED_EVENT, transactionReceipt);
        ArrayList<ProductDeletedEventResponse> responses = new ArrayList<ProductDeletedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ProductDeletedEventResponse typedResponse = new ProductDeletedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ProductDeletedEventResponse getProductDeletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PRODUCTDELETED_EVENT, log);
        ProductDeletedEventResponse typedResponse = new ProductDeletedEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ProductDeletedEventResponse> productDeletedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getProductDeletedEventFromLog(log));
    }

    public Flowable<ProductDeletedEventResponse> productDeletedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PRODUCTDELETED_EVENT));
        return productDeletedEventFlowable(filter);
    }

    public static List<ProductUpdatedEventResponse> getProductUpdatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PRODUCTUPDATED_EVENT, transactionReceipt);
        ArrayList<ProductUpdatedEventResponse> responses = new ArrayList<ProductUpdatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ProductUpdatedEventResponse typedResponse = new ProductUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ProductUpdatedEventResponse getProductUpdatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PRODUCTUPDATED_EVENT, log);
        ProductUpdatedEventResponse typedResponse = new ProductUpdatedEventResponse();
        typedResponse.log = log;
        typedResponse.id = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ProductUpdatedEventResponse> productUpdatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getProductUpdatedEventFromLog(log));
    }

    public Flowable<ProductUpdatedEventResponse> productUpdatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PRODUCTUPDATED_EVENT));
        return productUpdatedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> updateProduct(BigInteger id, String name,
            String category, String description, BigInteger quantity, BigInteger price) {
        final Function function = new Function(
                FUNC_UPDATEPRODUCT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id), 
                new org.web3j.abi.datatypes.Utf8String(name), 
                new org.web3j.abi.datatypes.Utf8String(category), 
                new org.web3j.abi.datatypes.Utf8String(description), 
                new org.web3j.abi.datatypes.generated.Uint256(quantity), 
                new org.web3j.abi.datatypes.generated.Uint256(price)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple6<String, String, String, BigInteger, BigInteger, BigInteger>> getProduct(
            BigInteger id) {
        final Function function = new Function(FUNC_GETPRODUCT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple6<String, String, String, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple6<String, String, String, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple6<String, String, String, BigInteger, BigInteger, BigInteger> call()
                            throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple6<String, String, String, BigInteger, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue());
                    }
                });
    }

    public RemoteFunctionCall<BigInteger> nextId() {
        final Function function = new Function(FUNC_NEXTID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple8<BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, Boolean>> products(
            BigInteger param0) {
        final Function function = new Function(FUNC_PRODUCTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}));
        return new RemoteFunctionCall<Tuple8<BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, Boolean>>(function,
                new Callable<Tuple8<BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, Boolean>>() {
                    @Override
                    public Tuple8<BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, Boolean> call(
                            ) throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple8<BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, Boolean>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue(), 
                                (Boolean) results.get(7).getValue());
                    }
                });
    }

    @Deprecated
    public static ProductManager load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new ProductManager(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static ProductManager load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ProductManager(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static ProductManager load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new ProductManager(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static ProductManager load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new ProductManager(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<ProductManager> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(ProductManager.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<ProductManager> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ProductManager.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<ProductManager> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(ProductManager.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<ProductManager> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ProductManager.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }



    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class ProductAddedEventResponse extends BaseEventResponse {
        public BigInteger id;

        public String name;
    }

    public static class ProductDeletedEventResponse extends BaseEventResponse {
        public BigInteger id;
    }

    public static class ProductUpdatedEventResponse extends BaseEventResponse {
        public BigInteger id;
    }
}
