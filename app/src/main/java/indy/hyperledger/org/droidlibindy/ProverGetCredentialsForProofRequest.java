package indy.hyperledger.org.droidlibindy;

import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.json.JSONArray;
import org.json.JSONObject;


public class ProverGetCredentialsForProofRequest extends AnoncredsIntegration {

	public ProverGetCredentialsForProofRequest() throws Exception{
		testProverGetCredentialsForProofRequestWorksForRevealedAttribute();
		testProverGetCredentialsForProofRequestWorksForNotFoundAttribute();
		testProverGetCredentialsForProofRequestWorksForPredicate();
		testProverGetCredentialsForProofRequestWorksForNotSatisfiedPredicate();
		testProverGetCredentialsForProofRequestWorksForMultiplyAttributesAndPredicates();
		testProverGetCredentialsForProofRequestWorksForRevealedAttributeBySpecificIssuer();



	}

	public void testProverGetCredentialsForProofRequestWorksForRevealedAttribute() throws Exception {

		String proofRequest = "{" +
				"              \"nonce\":\"123432421212\"," +
				"              \"name\":\"proof_req_1\"," +
				"              \"version\":\"0.1\"," +
				"              \"requested_attributes\":{" +
				"                   \"attr1_referent\":{\"name\":\"name\"}" +
				"               }," +
				"              \"requested_predicates\":{}" +
				"          }";

		String credentialsJson = Anoncreds.proverGetCredentialsForProofReq(wallet, new JSONObject(proofRequest).toString()).get();

		JSONObject credentials = new JSONObject(credentialsJson);

		JSONArray credentialsForAttribute1 = credentials.getJSONObject("attrs").getJSONArray("attr1_referent");

	}

	public void testProverGetCredentialsForProofRequestWorksForNotFoundAttribute() throws Exception {

		String proofRequest = "{" +
				"              \"nonce\":\"123432421212\"," +
				"              \"name\":\"proof_req_1\"," +
				"              \"version\":\"0.1\"," +
				"              \"requested_attributes\":{" +
				"                   \"attr1_referent\":{\"name\":\"attribute\"}" +
				"              }," +
				"              \"requested_predicates\":{}" +
				"         }";

		String credentialsJson = Anoncreds.proverGetCredentialsForProofReq(wallet, new JSONObject(proofRequest).toString()).get();

		JSONObject credentials = new JSONObject(credentialsJson);

		JSONArray credentialsForAttribute1 = credentials.getJSONObject("attrs").getJSONArray("attr1_referent");
	}

	public void testProverGetCredentialsForProofRequestWorksForPredicate() throws Exception {

		String proofRequest = "{" +
				"              \"nonce\":\"123432421212\"," +
				"              \"name\":\"proof_req_1\"," +
				"              \"version\":\"0.1\"," +
				"              \"requested_attributes\":{}," +
				"              \"requested_predicates\":{" +
				"                   \"predicate1_referent\":{" +
				"                       \"name\":\"age\",\"p_type\":\">=\",\"p_value\":18" +
				"                   }" +
				"              }" +
				"          }";

		String credentialsJson = Anoncreds.proverGetCredentialsForProofReq(wallet, new JSONObject(proofRequest).toString()).get();

		JSONObject credentials = new JSONObject(credentialsJson);

		JSONArray credentialsForPredicate = credentials.getJSONObject("predicates").getJSONArray("predicate1_referent");
	}

	public void testProverGetCredentialsForProofRequestWorksForNotSatisfiedPredicate() throws Exception {

		String proofRequest = "{" +
				"              \"nonce\":\"123432421212\"," +
				"              \"name\":\"proof_req_1\"," +
				"              \"version\":\"0.1\"," +
				"              \"requested_attributes\":{}," +
				"              \"requested_predicates\":{" +
				"                   \"predicate1_referent\":{" +
				"                       \"name\":\"age\",\"p_type\":\">=\",\"p_value\":58" +
				"                   }" +
				"               }" +
				"         }";

		String credentialsJson = Anoncreds.proverGetCredentialsForProofReq(wallet, new JSONObject(proofRequest).toString()).get();

		JSONObject credentials = new JSONObject(credentialsJson);

		JSONArray credentialsForPredicate = credentials.getJSONObject("predicates").getJSONArray("predicate1_referent");
	}

	public void testProverGetCredentialsForProofRequestWorksForMultiplyAttributesAndPredicates() throws Exception {

		String proofRequest = "{" +
				"               \"nonce\":\"123432421212\"," +
				"               \"name\":\"proof_req_1\"," +
				"               \"version\":\"0.1\"," +
				"               \"requested_attributes\":{" +
				"                     \"attr1_referent\":{ \"name\":\"name\"}," +
				"                     \"attr2_referent\":{\"name\":\"sex\"}" +
				"               }," +
				"               \"requested_predicates\":{" +
				"                     \"predicate1_referent\":{\"name\":\"age\",\"p_type\":\">=\",\"p_value\":18}," +
				"                     \"predicate2_referent\":{\"name\":\"height\",\"p_type\":\">=\",\"p_value\":160}" +
				"               }" +
				"            }";

		String credentialsJson = Anoncreds.proverGetCredentialsForProofReq(wallet, new JSONObject(proofRequest).toString()).get();

		JSONObject credentials = new JSONObject(credentialsJson);

		JSONArray credentialsForAttribute1 = credentials.getJSONObject("attrs").getJSONArray("attr1_referent");

		JSONArray credentialsForAttribute2 = credentials.getJSONObject("attrs").getJSONArray("attr2_referent");

		JSONArray credentialsForPredicate1 = credentials.getJSONObject("predicates").getJSONArray("predicate1_referent");

		JSONArray credentialsForPredicate2 = credentials.getJSONObject("predicates").getJSONArray("predicate2_referent");
	}

	public void testProverGetCredentialsForProofRequestWorksForRevealedAttributeBySpecificIssuer() throws Exception {

		String proofRequest = String.format("{" +
				"              \"nonce\":\"123432421212\"," +
				"              \"name\":\"proof_req_1\"," +
				"              \"version\":\"0.1\"," +
				"              \"requested_attributes\":{" +
				"                   \"attr1_referent\":{" +
				"                       \"name\":\"name\"," +
				"                       \"restrictions\":[{\"issuer_did\":\"%s\"}]" +
				"                   }" +
				"               }," +
				"              \"requested_predicates\":{}" +
				"          }", issuerDid);

		String credentialsJson = Anoncreds.proverGetCredentialsForProofReq(wallet, new JSONObject(proofRequest).toString()).get();

		JSONObject credentials = new JSONObject(credentialsJson);

		JSONArray credentialsForAttribute1 = credentials.getJSONObject("attrs").getJSONArray("attr1_referent");
	}
}
