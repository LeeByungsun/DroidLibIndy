package indy.hyperledger.org.droidlibindy;


import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.IssuerCreateAndStoreCredentialDefResult;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.ProverCreateCredentialRequestResult;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import indy.hyperledger.org.droidlibindy.util.PrintLog;
import indy.hyperledger.org.droidlibindy.util.StorageUtils;

public class AnoncredsIntegration{


    private static Boolean walletOpened = false;
    private String TAG = "DroidLibIndy";

    static Wallet wallet = null;
    static String gvtSchemaId;
    static String gvtSchema;
    static String xyzSchemaId;
    static String xyzSchema;
    static String issuer1gvtCredDefId;
    static String issuer1gvtCredDef;
    static String issuer1xyzCredDef;
    static String issuer1GvtCredOffer;
    static String issuer2GvtCredOffer;
    static String issuer1GvtCredReq;
    static String issuer1GvtCredReqMetadata;
    String CREDENTIALS = "{\"key\":\"8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY\", \"key_derivation_method\":\"RAW\"}";
    String masterSecretId = "master_secret_name";
    String issuerDid = "NcYxiDXkpYi6ov5FcYDi1e";
    String proverDid = "CnEDk9HrMnmiHXEV1WFgbVCRteYnPqsJwrTdcZaNhFVW";
    String defaultCredentialDefinitionConfig = "{\"support_revocation\":false}";
    String tag = "tag1";
    String gvtSchemaName = "gvt";
    String schemaVersion = "1.0";
    String gvtSchemaAttributes = "[\"name\", \"age\", \"sex\", \"height\"]";
    String credentialId1 = "id1";
    String credentialId2 = "id2";
    String credentialIdX = "idX";

    long startTime = 0;
    long endTime = 0;
    long time = 0;
    // note that encoding is not standardized by Indy except that 32-bit integers are encoded as themselves. IS-786
    String gvtCredentialValuesJson = new JSONObject("{\n" +
            "               \"sex\":{\"raw\":\"male\",\"encoded\":\"5944657099558967239210949258394887428692050081607692519917050011144233115103\"},\n" +
            "               \"name\":{\"raw\":\"Alex\",\"encoded\":\"1139481716457488690172217916278103335\"},\n" +
            "               \"height\":{\"raw\":\"175\",\"encoded\":\"175\"},\n" +
            "               \"age\":{\"raw\":\"28\",\"encoded\":\"28\"}\n" +
            "        }").toString();
    String xyzCredentialValuesJson = new JSONObject("{\n" +
            "               \"status\":{\"raw\":\"partial\",\"encoded\":\"51792877103171595686471452153480627530895\"},\n" +
            "               \"period\":{\"raw\":\"8\",\"encoded\":\"8\"}\n" +
            "        }").toString();
    String proofRequest = new JSONObject("{\n" +
            "                   \"nonce\":\"123432421212\",\n" +
            "                   \"name\":\"proof_req_1\",\n" +
            "                   \"version\":\"0.1\", " +
            "                   \"requested_attributes\":{" +
            "                          \"attr1_referent\":{\"name\":\"name\"}" +
            "                    },\n" +
            "                    \"requested_predicates\":{" +
            "                          \"predicate1_referent\":{\"name\":\"age\",\"p_type\":\">=\",\"p_value\":18}" +
            "                    }" +
            "               }").toString();


    final String WALLET = "Wallet1";
    final String TYPE = "default";
    final String WALLET_CREDENTIALS =
            new JSONObject()
                    .put("key", "key")
                    .toString();
    final String WALLET_CONFIG =
            new JSONObject()
                    .put("id", WALLET)
                    .put("storage_type", TYPE)
                    .toString();

    public AnoncredsIntegration() throws JSONException{

    }


    public void initCommonWallet() throws Exception{

        if(walletOpened){
            return;
        }
        StorageUtils.cleanupStorage();

        String walletConfig =
                new JSONObject()
                        .put("id", "anoncredsCommonWallet")
                        .toString();
        startTime = DroidLibIndy.getCurrentTime();
        PrintLog.i( "wallet open Start");
        try{
            Wallet.createWallet(WALLET_CONFIG, WALLET_CREDENTIALS).get();
        }catch(ExecutionException e){
            PrintLog.e( e.getMessage());
            if(e.getMessage().indexOf("WalletExistsException") >= 0){
                // ignore
            }else{
                throw new RuntimeException(e);
            }
        }
        wallet = Wallet.openWallet(WALLET_CONFIG, WALLET_CREDENTIALS).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "wallet open end : "+time);
        PrintLog.i( "IssuerCreateSchemaResult Start");
        startTime = DroidLibIndy.getCurrentTime();
        AnoncredsResults.IssuerCreateSchemaResult createSchemaResult =
                Anoncreds.issuerCreateSchema(issuerDid, gvtSchemaName, schemaVersion, gvtSchemaAttributes).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "IssuerCreateSchemaResult end : "+time);
        gvtSchemaId = createSchemaResult.getSchemaId();
        PrintLog.e( "gvtSchemaId = "+gvtSchemaId);
        gvtSchema = createSchemaResult.getSchemaJson();
        PrintLog.e( "gvtSchema = "+gvtSchema);



        String xyzSchemaAttributes = "[\"status\", \"period\"]";
        String xyzSchemaName = "xyz";
        PrintLog.i( "issuerCreateSchema Start");
        startTime = DroidLibIndy.getCurrentTime();
        createSchemaResult = Anoncreds.issuerCreateSchema(issuerDid, xyzSchemaName, schemaVersion, xyzSchemaAttributes).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "issuerCreateSchema end : "+time);
        xyzSchemaId = createSchemaResult.getSchemaId();
        PrintLog.e( "xyzSchemaId = "+xyzSchemaId);
        xyzSchema = createSchemaResult.getSchemaJson();
        PrintLog.e( "xyzSchema = "+xyzSchema);
        //Issue GVT issuer1GvtCredential by Issuer1
        IssuerCreateAndStoreCredentialDefResult issuer1CreateGvtCredDefResult = null;
        try{
            PrintLog.i( "GVT issuerCreateAndStoreCredentialDef start");
            startTime = DroidLibIndy.getCurrentTime();
            issuer1CreateGvtCredDefResult =
                    Anoncreds.issuerCreateAndStoreCredentialDef(wallet, issuerDid, gvtSchema, tag, null, defaultCredentialDefinitionConfig).get();
            endTime = DroidLibIndy.getCurrentTime();
            time = endTime-startTime;
            PrintLog.i( "GVT issuerCreateAndStoreCredentialDef end : "+time);
        }catch(ExecutionException e){
            PrintLog.e( e.getMessage());
//            if(e.getMessage().indexOf("CredDefAlreadyExistsException") >= 0){
//                // ignore
//            }else{
                throw new RuntimeException(e);
//            }
        }
        issuer1gvtCredDefId = issuer1CreateGvtCredDefResult.getCredDefId();
        PrintLog.e( "issuer1gvtCredDefId = "+issuer1gvtCredDefId);
        issuer1gvtCredDef = issuer1CreateGvtCredDefResult.getCredDefJson();
        PrintLog.e( "issuer1gvtCredDef = "+issuer1gvtCredDef);

        //Issue XYZ issuer1GvtCredential by Issuer1
        IssuerCreateAndStoreCredentialDefResult issuer1CreateXyzCredDefResult = null;
        try{
            PrintLog.i( "XYZ issuerCreateAndStoreCredentialDef start");
            startTime = DroidLibIndy.getCurrentTime();
            issuer1CreateXyzCredDefResult =
                    Anoncreds.issuerCreateAndStoreCredentialDef(wallet, issuerDid, xyzSchema, tag, null, defaultCredentialDefinitionConfig).get();
            endTime = DroidLibIndy.getCurrentTime();
            time = endTime-startTime;
            PrintLog.i( "XYZ issuerCreateAndStoreCredentialDef end : "+time);
        }catch(ExecutionException e){
            PrintLog.e( e.getMessage());
//            if(e.getMessage().indexOf("WalletExistsException") >= 0){
//                // ignore
//            }else{
                throw new RuntimeException(e);
//            }
        }
        String issuer1xyzCredDefId = issuer1CreateXyzCredDefResult.getCredDefId();
        PrintLog.e( "issuer1xyzCredDefId = "+issuer1xyzCredDefId);
        issuer1xyzCredDef = issuer1CreateXyzCredDefResult.getCredDefJson();
        PrintLog.e( "issuer1xyzCredDef = "+issuer1xyzCredDef);
//
//        //Issue GVT issuer1GvtCredential by Issuer2
//        String issuerDid2 = "VsKV7grR1BUE29mG2Fm2kX";
//        IssuerCreateAndStoreCredentialDefResult issuer2CreateGvtCredDefResult =
//                Anoncreds.issuerCreateAndStoreCredentialDef(wallet, issuerDid2, gvtSchema, tag, null, defaultCredentialDefinitionConfig).get();
//        String issuer2gvtCredDefId = issuer2CreateGvtCredDefResult.getCredDefId();
//        String issuer2gvtCredDef = issuer2CreateGvtCredDefResult.getCredDefJson();
//
        PrintLog.i( "gvt issuerCreateCredentialOffer start");
        startTime = DroidLibIndy.getCurrentTime();
        issuer1GvtCredOffer = Anoncreds.issuerCreateCredentialOffer(wallet, issuer1gvtCredDefId).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "gvt issuerCreateCredentialOffer end : "+time);
        PrintLog.e( "issuer1GvtCredOffer = "+issuer1GvtCredOffer);

        PrintLog.i( "xyz issuerCreateCredentialOffer start");
        startTime = DroidLibIndy.getCurrentTime();
        String issuer1XyzCredOffer = Anoncreds.issuerCreateCredentialOffer(wallet, issuer1xyzCredDefId).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "xyz issuerCreateCredentialOffer end : "+time);
        PrintLog.e( "issuer1XyzCredOffer = "+issuer1XyzCredOffer);
//        issuer2GvtCredOffer = Anoncreds.issuerCreateCredentialOffer(wallet, issuer2gvtCredDefId).get();
//
        PrintLog.i( "proverCreateMasterSecret start");
        startTime = DroidLibIndy.getCurrentTime();
        Anoncreds.proverCreateMasterSecret(wallet, masterSecretId).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "proverCreateMasterSecret end : "+time);
//
        PrintLog.i( "proverCreateCredentialReq start");
        startTime = DroidLibIndy.getCurrentTime();
        ProverCreateCredentialRequestResult createCredReqResult =
                Anoncreds.proverCreateCredentialReq(wallet, proverDid, issuer1GvtCredOffer, issuer1gvtCredDef, masterSecretId).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "proverCreateCredentialReq end : "+time);
//
        issuer1GvtCredReq = createCredReqResult.getCredentialRequestJson();
        PrintLog.e( "issuer1GvtCredReq = "+issuer1GvtCredReq);
        issuer1GvtCredReqMetadata = createCredReqResult.getCredentialRequestMetadataJson();
        PrintLog.e( "issuer1GvtCredReqMetadata = "+issuer1GvtCredReqMetadata);
//
        PrintLog.i( "issuerCreateCredential start");
        startTime = DroidLibIndy.getCurrentTime();
        AnoncredsResults.IssuerCreateCredentialResult createCredResult =
                Anoncreds.issuerCreateCredential(wallet, issuer1GvtCredOffer, issuer1GvtCredReq, gvtCredentialValuesJson, null, -1).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "issuerCreateCredential end : "+time);
        String issuer1GvtCredential = createCredResult.getCredentialJson();
        PrintLog.e( "issuer1GvtCredential = "+issuer1GvtCredential);
//
        PrintLog.i( "proverStoreCredential start");
        startTime = DroidLibIndy.getCurrentTime();
        Anoncreds.proverStoreCredential(wallet, credentialId1, issuer1GvtCredReqMetadata, issuer1GvtCredential, issuer1gvtCredDef, null).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "proverStoreCredential end : "+time);
//
        PrintLog.i( "proverCreateCredentialReq start");
        startTime = DroidLibIndy.getCurrentTime();
        createCredReqResult = Anoncreds.proverCreateCredentialReq(wallet, proverDid, issuer1XyzCredOffer, issuer1xyzCredDef, masterSecretId).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "proverCreateCredentialReq end : "+time);

        String issuer1XyzCredReq = createCredReqResult.getCredentialRequestJson();
        PrintLog.e( "issuer1XyzCredReq = "+issuer1XyzCredReq);
        String issuer1XyzCredReqMetadata = createCredReqResult.getCredentialRequestMetadataJson();
        PrintLog.e( "issuer1XyzCredReqMetadata = "+issuer1XyzCredReqMetadata);
//
        createCredResult = Anoncreds.issuerCreateCredential(wallet, issuer1XyzCredOffer, issuer1XyzCredReq, xyzCredentialValuesJson, null, -1).get();
        String issuer1XyzCredential = createCredResult.getCredentialJson();
//
//        Anoncreds.proverStoreCredential(wallet, credentialId2, issuer1XyzCredReqMetadata, issuer1XyzCredential, issuer1xyzCredDef, null).get();
//
//        createCredReqResult = Anoncreds.proverCreateCredentialReq(wallet, proverDid, issuer2GvtCredOffer, issuer2gvtCredDef, masterSecretId).get();
//        String issuer2GvtCredReq = createCredReqResult.getCredentialRequestJson();
//        String issuer2GvtCredReqMetadata = createCredReqResult.getCredentialRequestMetadataJson();
//
//        String gvt2CredValues = "{" +
//                "           \"sex\":{\"raw\":\"male\",\"encoded\":\"2142657394558967239210949258394838228692050081607692519917028371144233115103\"},\n" +
//                "           \"name\":{\"raw\":\"Alexander\",\"encoded\":\"21332817548165488690172217217278169335\"},\n" +
//                "           \"height\":{\"raw\":\"170\",\"encoded\":\"170\"},\n" +
//                "           \"age\":{\"raw\":\"28\",\"encoded\":\"28\"}\n" +
//                "   }";
//
//        createCredResult = Anoncreds.issuerCreateCredential(wallet, issuer2GvtCredOffer, issuer2GvtCredReq, gvt2CredValues, null, -1).get();
//        String issuer2GvtCredential = createCredResult.getCredentialJson();
//
//        String credentialId3 = "id3";
//        Anoncreds.proverStoreCredential(wallet, credentialId3, issuer2GvtCredReqMetadata, issuer2GvtCredential, issuer2gvtCredDef, null).get();
//
//        Anoncreds.proverStoreCredential(wallet, credentialIdX, issuer2GvtCredReqMetadata, issuer2GvtCredential, issuer2gvtCredDef, null).get();

        walletOpened = true;
    }
}
