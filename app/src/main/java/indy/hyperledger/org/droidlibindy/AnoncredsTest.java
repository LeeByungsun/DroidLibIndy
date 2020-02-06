package indy.hyperledger.org.droidlibindy;

import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearchForProofReq;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;


import indy.hyperledger.org.droidlibindy.util.PoolUtils;
import indy.hyperledger.org.droidlibindy.util.PrintLog;

import static indy.hyperledger.org.droidlibindy.util.PoolUtils.PROTOCOL_VERSION;
import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.*;


class AnoncredsTest{

    static void demo() throws Exception{
        PrintLog.e("Anoncreds sample -> started");

        String issuerDid = "NcYxiDXkpYi6ov5FcYDi1e";
        String proverDid = "VsKV7grR1BUE29mG2Fm2kX";

        long startTime = 0;
        long endTime = 0;
        long time = 0;

        // Set protocol version 2 to work with Indy Node 1.4
        Pool.setProtocolVersion(PROTOCOL_VERSION).get();

        //1. Create and Open Pool
		/*PrintLog.e("1.Create and Open Pool");
		String poolName = PoolUtils.createPoolLedgerConfig();
		Pool pool = Pool.openPoolLedger(poolName, "{}").get();*/

        //2. Issuer Create and Open Wallet
        PrintLog.e("2. Issuer Create and Open Wallet");
        startTime = DroidLibIndy.getCurrentTime();
        String issuerWalletConfig = new JSONObject().put("id", "issuerWallet").toString();
        String issuerWalletCredentials = new JSONObject().put("key", "issuer_wallet_key").toString();
        Wallet.createWallet(issuerWalletConfig, issuerWalletCredentials).get();
        Wallet issuerWallet = Wallet.openWallet(issuerWalletConfig, issuerWalletCredentials).get();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("issuerWalletConfig : " + issuerWalletConfig);
        PrintLog.i("issuerWalletCredentials : " + issuerWalletCredentials);
        PrintLog.i("process time : " + (endTime - startTime));

        //3. Prover Create and Open Wallet
        PrintLog.e("3. Prover Create and Open Wallet");
        startTime = DroidLibIndy.getCurrentTime();
        String proverWalletConfig = new JSONObject().put("id", "trusteeWallet").toString();
        String proverWalletCredentials = new JSONObject().put("key", "prover_wallet_key").toString();
        Wallet.createWallet(proverWalletConfig, proverWalletCredentials).get();
        Wallet proverWallet = Wallet.openWallet(proverWalletConfig, proverWalletCredentials).get();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("proverWalletConfig : " + proverWalletConfig);
        PrintLog.i("proverWalletCredentials : " + proverWalletCredentials);
        PrintLog.i("process time : " + (endTime - startTime));

        //4. Issuer Creates Credential Schema
        PrintLog.e("4. Issuer Creates Credential Schema");
        startTime = DroidLibIndy.getCurrentTime();
        String schemaName = "gvt";
        String schemaVersion = "1.0";
        String schemaAttributes = new JSONArray().put("name").put("age").put("sex").put("height").toString();
        AnoncredsResults.IssuerCreateSchemaResult createSchemaResult =
                issuerCreateSchema(issuerDid, schemaName, schemaVersion, schemaAttributes).get();
        String schemaId = createSchemaResult.getSchemaId();
        String schemaJson = createSchemaResult.getSchemaJson();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("schemaId : " + schemaId);
        PrintLog.i("schemaJson : " + schemaJson);
        PrintLog.i("process time : " + (endTime - startTime));

        //5. Issuer create Credential Definition
        PrintLog.e("5. Issuer create Credential Definition");
        startTime = DroidLibIndy.getCurrentTime();
        String credDefTag = "Tag1";
        String credDefConfigJson = new JSONObject().put("support_revocation", false).toString();
        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult createCredDefResult =
                issuerCreateAndStoreCredentialDef(issuerWallet, issuerDid, schemaJson, credDefTag, null, credDefConfigJson).get();
        String credDefId = createCredDefResult.getCredDefId();
        String credDefJson = createCredDefResult.getCredDefJson();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("credDefId : " + credDefId);
        PrintLog.i("credDefJson : " + credDefJson);
        PrintLog.i("process time : " + (endTime - startTime));

        //6. Prover create Master Secret
        PrintLog.e("6. Prover create Master Secret");
        startTime = DroidLibIndy.getCurrentTime();
        String masterSecretId = proverCreateMasterSecret(proverWallet, null).get();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("masterSecretId : " + masterSecretId);
        PrintLog.i("process time : " + (endTime - startTime));

        //7. Issuer Creates Credential Offer
        PrintLog.e("7. Issuer Creates Credential Offer");
        startTime = DroidLibIndy.getCurrentTime();
        String credOffer = issuerCreateCredentialOffer(issuerWallet, credDefId).get();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("credOffer : " + credOffer);
        PrintLog.i("process time : " + (endTime - startTime));

        //8. Prover Creates Credential Request
        PrintLog.e("8. Prover Creates Credential Request");
        startTime = DroidLibIndy.getCurrentTime();
        AnoncredsResults.ProverCreateCredentialRequestResult createCredReqResult =
                proverCreateCredentialReq(proverWallet, proverDid, credOffer, credDefJson, masterSecretId).get();
        String credReqJson = createCredReqResult.getCredentialRequestJson();
        String credReqMetadataJson = createCredReqResult.getCredentialRequestMetadataJson();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("credReqJson : " + credReqJson);
        PrintLog.i("credReqMetadataJson : " + credReqMetadataJson);
        PrintLog.i("process time : " + (endTime - startTime));

        //9. Issuer create Credential
        //   note that encoding is not standardized by Indy except that 32-bit integers are encoded as themselves. IS-786
        PrintLog.e("9. Issuer create Credential");
        startTime = DroidLibIndy.getCurrentTime();
        String credValuesJson = new JSONObject()
                .put("sex", new JSONObject().put("raw", "male").put("encoded", "594465709955896723921094925839488742869205008160769251991705001"))
                .put("name", new JSONObject().put("raw", "Alex").put("encoded", "1139481716457488690172217916278103335"))
                .put("height", new JSONObject().put("raw", "175").put("encoded", "175"))
                .put("age", new JSONObject().put("raw", "28").put("encoded", "28"))
                .toString();

        AnoncredsResults.IssuerCreateCredentialResult createCredentialResult =
                issuerCreateCredential(issuerWallet, credOffer, credReqJson, credValuesJson, null, -1).get();
        String credential = createCredentialResult.getCredentialJson();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("credential : " + credential);
        PrintLog.i("process time : " + (endTime - startTime));

        //10. Prover Stores Credential
        PrintLog.e("10. Prover Stores Credential");
        startTime = DroidLibIndy.getCurrentTime();
        String proverCredentail = proverStoreCredential(proverWallet, null, credReqMetadataJson, credential, credDefJson, null).get();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("proverCredentail : " + proverCredentail);
        PrintLog.i("process time : " + (endTime - startTime));

        //11. Prover Gets Credentials for Proof Request
        PrintLog.e("11. Prover Gets Credentials for Proof Request");
        startTime = DroidLibIndy.getCurrentTime();
        String nonce = generateNonce().get();
        String proofRequestJson = new JSONObject()
                .put("nonce", nonce)
                .put("name", "proof_req_1")
                .put("version", "0.1")
                .put("requested_attributes", new JSONObject()
                        .put("attr1_referent", new JSONObject().put("name", "name"))
                        .put("attr2_referent", new JSONObject().put("name", "sex"))
                        .put("attr3_referent", new JSONObject().put("name", "phone"))
                )
                .put("requested_predicates", new JSONObject()
                        .put("predicate1_referent", new JSONObject()
                                .put("name", "age")
                                .put("p_type", ">=")
                                .put("p_value", 18)
                        )
                )
                .toString();

        CredentialsSearchForProofReq credentialsSearch = CredentialsSearchForProofReq.open(proverWallet, proofRequestJson, null).get();

        JSONArray credentialsForAttribute1 = new JSONArray(credentialsSearch.fetchNextCredentials("attr1_referent", 100).get());
        String credentialIdForAttribute1 = credentialsForAttribute1.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute2 = new JSONArray(credentialsSearch.fetchNextCredentials("attr2_referent", 100).get());
        String credentialIdForAttribute2 = credentialsForAttribute2.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute3 = new JSONArray(credentialsSearch.fetchNextCredentials("attr3_referent", 100).get());
//		assertEquals(0, credentialsForAttribute3.length());

        JSONArray credentialsForPredicate = new JSONArray(credentialsSearch.fetchNextCredentials("predicate1_referent", 100).get());
        String credentialIdForPredicate = credentialsForPredicate.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        credentialsSearch.close();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("nonce : " + nonce);
        PrintLog.i("proofRequestJson : " + proofRequestJson);
        PrintLog.i("credentialIdForAttribute1 : " + credentialIdForAttribute1);
        PrintLog.i("credentialIdForAttribute2 : " + credentialIdForAttribute2);
        PrintLog.i("credentialsForAttribute3 : " + credentialsForAttribute3);
        PrintLog.i("credentialIdForPredicate : " + credentialIdForPredicate);
        PrintLog.i("process time : " + (endTime - startTime));

        //12. Prover Creates Proof
        PrintLog.e("12. Prover Creates Proof");
        startTime = DroidLibIndy.getCurrentTime();
        String selfAttestedValue = "8-800-300";
        String requestedCredentialsJson = new JSONObject()
                .put("self_attested_attributes", new JSONObject().put("attr3_referent", selfAttestedValue))
                .put("requested_attributes", new JSONObject()
                        .put("attr1_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute1)
                                .put("revealed", true)
                        )
                        .put("attr2_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute2)
                                .put("revealed", false)
                        )
                )
                .put("requested_predicates", new JSONObject()
                        .put("predicate1_referent", new JSONObject()
                                .put("cred_id", credentialIdForPredicate)
                        )
                )
                .toString();

        String schemas = new JSONObject().put(schemaId, new JSONObject(schemaJson)).toString();
        String credentialDefs = new JSONObject().put(credDefId, new JSONObject(credDefJson)).toString();
        String revocStates = new JSONObject().toString();

        String proofJson = "";
        try{
            proofJson = proverCreateProof(proverWallet, proofRequestJson, requestedCredentialsJson,
                    masterSecretId, schemas, credentialDefs, revocStates).get();
        }catch(Exception e){
            PrintLog.e("");
        }

        JSONObject proof = new JSONObject(proofJson);
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("schemas : " + schemas);
        PrintLog.i("credentialDefs : " + proofJson);
        PrintLog.i("revocStates : " + revocStates);
        PrintLog.i("proofJson : " + proofJson);
        PrintLog.i("process time : " + (endTime - startTime));

        //13. Verifier verify Proof
        PrintLog.e("13. Verifier verify Proof");
        startTime = DroidLibIndy.getCurrentTime();
        JSONObject revealedAttr1 = proof.getJSONObject("requested_proof").getJSONObject("revealed_attrs").getJSONObject("attr1_referent");
//		assertEquals("Alex", revealedAttr1.getString("raw"));

//		assertNotNull(proof.getJSONObject("requested_proof").getJSONObject("unrevealed_attrs").getJSONObject("attr2_referent").getInt("sub_proof_index"));

//		assertEquals(selfAttestedValue, proof.getJSONObject("requested_proof").getJSONObject("self_attested_attrs").getString("attr3_referent"));

        String revocRegDefs = new JSONObject().toString();
        String revocRegs = new JSONObject().toString();

        Boolean valid = verifierVerifyProof(proofRequestJson, proofJson, schemas, credentialDefs, revocRegDefs, revocRegs).get();
//		assertTrue(valid);
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("revealedAttr1 : " + revealedAttr1.toString());
        PrintLog.i("revocRegDefs : " + revocRegDefs);
        PrintLog.i("revocRegs : " + revocRegs);
        PrintLog.i("revealedAttr1 : " + revealedAttr1.toString());
        PrintLog.i("valid : " + valid);


        PrintLog.i("process time : " + (endTime - startTime));

        //14. Close and Delete issuer wallet
        PrintLog.e("14. Close and Delete issuer wallet");
        startTime = DroidLibIndy.getCurrentTime();
        issuerWallet.closeWallet().get();
        Wallet.deleteWallet(issuerWalletConfig, issuerWalletCredentials).get();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("process time = " + (endTime - startTime));

        //15. Close and Delete prover wallet
        PrintLog.e("15. Close and Delete prover wallet");
        startTime = DroidLibIndy.getCurrentTime();
        proverWallet.closeWallet().get();
        Wallet.deleteWallet(proverWalletConfig, proverWalletCredentials).get();
        endTime = DroidLibIndy.getCurrentTime();
        PrintLog.i("process time = " + (endTime - startTime));

        //16. Close pool
//		pool.closePoolLedger().get();

        //17. Delete Pool ledger config
//		Pool.deletePoolLedgerConfig(poolName).get();

        PrintLog.e("Anoncreds sample -> completed");
    }
}
