package reciter.algorithm.article.score.predictor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import reciter.storage.s3.AwsScoringClients;
import reciter.utils.EnvUtils;
import reciter.utils.PropertiesUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

public class NeuralNetworkModelArticlesScorer {
	
	private static final Logger log = LoggerFactory.getLogger(NeuralNetworkModelArticlesScorer.class);

	private static final String RETURN_CODE = "returnCode";

	private static final String PREDICTION_SCORES = "predictionScores";

	private static final String SCORING_DATA_FILE = "scoringDataFile";

	private static final String USE_S3_BUCKET = "useS3Bucket";

	private static final String BUCKET_NAME = "bucket_name";

	private static final String MODEL_NAME = "modelName";

	private static final String LAMBDA_FUNCTION_REGION = "aws.lambda.region";

	private static final String RECITERSCORING_SERVICE_URL = System.getenv("RECITERSCORING_SERVICE_URL");

	// ── Java 21: reusable ObjectMapper (thread-safe, expensive to construct) ──
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    
	public NeuralNetworkModelArticlesScorer() {
	}

	public JSONArray executeArticleScorePredictor(String goldStandardName, String dataFileName, String s3BucketName,
			String isS3UploadRequiredString){

		StopWatch stopWatch = new StopWatch(goldStandardName);
		stopWatch.start(goldStandardName);
		JSONArray authorshipLikelihoodScore;
		boolean useLocal = "false".equalsIgnoreCase(isS3UploadRequiredString);
		if (useLocal) {
			authorshipLikelihoodScore = callLocalLambda(goldStandardName, dataFileName, s3BucketName,
					                    isS3UploadRequiredString);
		} else {
			String lambdaFunction = EnvUtils.require("reciterScoringLambdaFunction", System.getenv("RECITER_SCORING_LAMBDA_FUNCTION"));
			log.info("lambdaFunction Name: {}" ,lambdaFunction);

			authorshipLikelihoodScore = callAwsLambda(goldStandardName, dataFileName, s3BucketName,
					                    isS3UploadRequiredString, lambdaFunction);
		}

		stopWatch.stop();
		log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
		return authorshipLikelihoodScore;
	}

	// Helper method to find JSONObject by article
	private Double findJSONObjectById(JSONArray jsonArray, long articleId) {
		if (jsonArray == null) return null;

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			if (jsonObject.getLong("id") == articleId) {
				return jsonObject.getDouble("scoreTotal") * 100;
			}
		}
		return null;
	}

	public static void main(String args[]){
		NeuralNetworkModelArticlesScorer nn = new NeuralNetworkModelArticlesScorer();
		JSONArray articlesIdentityFeedbackScoreTotal = nn.executeArticleScorePredictor("feedback",
				"ajg9004-feedbackIdentityScoringInput.json", "feedbackScore", "false");
		if (articlesIdentityFeedbackScoreTotal != null && articlesIdentityFeedbackScoreTotal.length() > 0) {
			Double score=nn.findJSONObjectById(articlesIdentityFeedbackScoreTotal, 9856924);
			log.info("Found score: {}",score);
	     }

	}


	private JSONArray callLocalLambda(String goldStandardModelName, String articleDataFilename, String s3BucketName,
			String isS3UploadRequiredString) {
		
		Map<String, Object> payloadMap = new HashMap<>();
		payloadMap.put(MODEL_NAME, goldStandardModelName);
		payloadMap.put(SCORING_DATA_FILE, articleDataFilename);
		payloadMap.put(USE_S3_BUCKET, isS3UploadRequiredString);
		payloadMap.put(BUCKET_NAME, s3BucketName);
		
		String payload;
		try {
			payload = OBJECT_MAPPER.writeValueAsString(payloadMap);
		} catch (JacksonException e) {
			log.error("Failed to serialize local-Lambda scoring payload for model={}, dataFile={}",
					goldStandardModelName, articleDataFilename, e);
			return null;
		}
		
		HttpURLConnection conn = null;
		try {
			URL url = URI.create(System.getenv("RECITERSCORING_SERVICE_URL") + PropertiesUtils.get("local.lambda.function.invocation.url")).toURL();
			conn = (HttpURLConnection) url.openConnection();
			if (conn != null) {
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setDoOutput(true);
			}
		} catch (Exception e) {
			log.error("Failed to open Lambda HTTP connection to {} for model={}, dataFile={}", RECITERSCORING_SERVICE_URL,
					goldStandardModelName, articleDataFilename, e);
			return null;
		}

		try {
			if (conn != null) {
				// Write payload to Lambda container
				try (OutputStream os = conn.getOutputStream()) {
					os.write(payload.getBytes(StandardCharsets.UTF_8));
				}
				StringBuilder response = new StringBuilder();
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
					String output;
					while ((output = reader.readLine()) != null) {
						response.append(output);
					}
				}
				// Parse the response
				JSONArray predictionScoresArray = null;
				JSONObject outer = new JSONObject(response.toString());
				if (outer.get(PREDICTION_SCORES) instanceof JSONArray) {
					predictionScoresArray = outer.getJSONArray(PREDICTION_SCORES);
				}
				log.info(" predictionScoresArray: {}", predictionScoresArray);

				// return Code
				int returnCode = outer.getInt(RETURN_CODE);
				log.info("returnCode: {} ", returnCode);

				if (returnCode == 0) {
					return predictionScoresArray;
				} else {
					// Log the error from the Python scoring script
					String error = outer.optString("error");
					log.error("Lambda scoring failed with returncode {}. error: {}", returnCode, error);
					return null;
				}
			}

		} catch (IOException | RuntimeException e) {
			log.error("Local Lambda scoring invocation/parse failed for model={}, dataFile={}; returning null",
					goldStandardModelName, articleDataFilename, e);
			return null;
		}
		finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
		return null;
	}

	/*
	 * Calls AWS Lambda function
	 */
	private JSONArray callAwsLambda(String goldStandardModelName, String articleDataFilename, String s3BucketName,
			String isS3UploadRequiredString, String lambdaFunctionName) {

		LambdaClient client = AwsScoringClients.lambda(PropertiesUtils.get(LAMBDA_FUNCTION_REGION));


		Map<String, Object> payloadMap = new HashMap<>();
		payloadMap.put(MODEL_NAME, goldStandardModelName);
		payloadMap.put(SCORING_DATA_FILE, articleDataFilename);
		payloadMap.put(USE_S3_BUCKET, isS3UploadRequiredString);
		payloadMap.put(BUCKET_NAME, s3BucketName);

		String payloadJson = null;
		try {
			payloadJson = OBJECT_MAPPER.writeValueAsString(payloadMap);
		} catch (JacksonException e) {
			log.error("Failed to serialize AWS Lambda scoring payload for model={}, dataFile={}", goldStandardModelName,
					articleDataFilename, e);
			return null;
		}

		// v2: InvokeRequest.builder() replaces new InvokeRequest().withFunctionName()
		// v2: SdkBytes.fromUtf8String() replaces plain String payload
		InvokeRequest request = InvokeRequest.builder()
				.functionName(lambdaFunctionName)
				.payload(SdkBytes.fromUtf8String(payloadJson))
				.build();

		try {
			// v2: LambdaClient.invoke() replaces AWSLambda.invoke()
			// v2: InvokeResponse replaces InvokeResult
			InvokeResponse result = client.invoke(request);

			// v2: result.payload().asUtf8String() replaces result.getPayload().array()
			String response = result.payload().asUtf8String();

			JSONObject outer = new JSONObject(response);
			JSONArray predictionScoresArray = null;
			if (outer.get(PREDICTION_SCORES) instanceof JSONArray) {
				predictionScoresArray = outer.getJSONArray(PREDICTION_SCORES);
			}
			log.info(" predictionScoresArray " + predictionScoresArray);

			// return Code
			int returnCode = outer.getInt(RETURN_CODE);
			log.info("returnCode: " + returnCode);

			if (returnCode == 0) {
				return predictionScoresArray;

			} else {
				// Log the error from the Python scoring script
				String error = outer.optString("error");
				log.error("Lambda scoring failed with returncode {}. error: {}", returnCode, error);
				return null;

			}

		} catch (Exception e) {
			log.error("AWS Lambda scoring invocation/parse failed for function={}, model={}, dataFile={}",
					lambdaFunctionName, goldStandardModelName, articleDataFilename, e);
		}
		return null;
	}

}