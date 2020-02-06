package indy.hyperledger.org.droidlibindy;

import android.util.Log;

import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.json.JSONObject;

import indy.hyperledger.org.droidlibindy.util.PrintLog;


public class ProverCreateProof extends AnoncredsIntegration{
    private String TAG = "ProverCreateProof";
    private String requestedCredentialsJson = String.format("{" +
            "\"self_attested_attributes\":{}," +
            "\"requested_attributes\":{\"attr1_referent\":{\"cred_id\":\"%s\", \"revealed\":true}}," +
            "\"requested_predicates\":{\"predicate1_referent\":{\"cred_id\":\"%s\"}}" +
            "}", credentialId1, credentialId1);

    public ProverCreateProof() throws Exception{
        testProverCreateProofWorks();
//        testProverCreateProofWorksForInvalidMasterSecret();
    }

    public void testProverCreateProofWorks() throws Exception{

        String schemasJson = new JSONObject().put(gvtSchemaId, new JSONObject(gvtSchema)).toString();
        String credentialDefsJson = new JSONObject().put(issuer1gvtCredDefId, new JSONObject(issuer1gvtCredDef)).toString();
        String revocStatesJson = new JSONObject().toString();
        PrintLog.e( "schemasJson = "+schemasJson);
        PrintLog.e( "credentialDefsJson = "+credentialDefsJson);
        PrintLog.e( "revocStatesJson = "+revocStatesJson);
        PrintLog.e( "requestedCredentialsJson = "+requestedCredentialsJson);
        PrintLog.e( "proofRequest = "+proofRequest);
        PrintLog.e( "masterSecretId = "+masterSecretId);
        PrintLog.i( "proverCreateProof start");
        startTime = DroidLibIndy.getCurrentTime();
        String proofJson = Anoncreds.proverCreateProof(wallet, proofRequest, new JSONObject(requestedCredentialsJson).toString(),
                masterSecretId, schemasJson, credentialDefsJson, revocStatesJson).get();
        endTime = DroidLibIndy.getCurrentTime();
        time = endTime-startTime;
        PrintLog.i( "proverCreateProof end : "+time);
        PrintLog.e( "proofJson = "+proofJson);
    }

    public void testProverCreateProofWorksForInvalidMasterSecret() throws Exception{

        String schemasJson = new JSONObject().put(gvtSchemaId, new JSONObject(gvtSchema)).toString();
        String credentialDefsJson = new JSONObject().put(issuer1gvtCredDefId, new JSONObject(issuer1gvtCredDef)).toString();
        String revocStatesJson = new JSONObject().toString();
        PrintLog.e( "schemasJson = "+schemasJson);
        PrintLog.e( "credentialDefsJson = "+credentialDefsJson);
        PrintLog.e( "revocStatesJson = "+revocStatesJson);
//		thrown.expect(ExecutionException.class);
//		thrown.expectCause(isA(WalletItemNotFoundException.class));
        PrintLog.e( "requestedCredentialsJson = "+requestedCredentialsJson);
        PrintLog.e( "proofRequest = "+proofRequest);
        String proofJson =  Anoncreds.proverCreateProof(wallet, proofRequest, new JSONObject(requestedCredentialsJson).toString(),
                "wrong_master_secret", schemasJson, credentialDefsJson, revocStatesJson).get();
        PrintLog.e( "proofJson = "+proofJson);
    }
}
