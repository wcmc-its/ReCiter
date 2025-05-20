package reciter.algorithm.article.score.predictor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reciter.security.AwsSecretsManagerService;
import reciter.utils.PropertiesUtils;


public class NeuralNetworkModelArticlesScorer {


	private static final Logger log = LoggerFactory.getLogger(NeuralNetworkModelArticlesScorer.class);
	
	private static final String LAMBDA_NAME = "lambdaFunctionName";
	
	private static final String LAMBDA_FUNCTION_INVOCATION_URL = "local.lambda.function.invocation.url";
	
	private static final String LAMBDA_FUNCTION_REGION = "aws.lambda.region";
	
    private AwsSecretsManagerService awsSecretsManagerService; // Inject the service to get the secret
	
    private String RECITER_SCORING_SECRET_NAME = "aws.secretsmanager.reciterscoring.secretName";
	
	private String reciterScoringServiceUrl = System.getenv("RECITERSCORING_SERVICE_URL");
	
	public NeuralNetworkModelArticlesScorer()
	{
		this.awsSecretsManagerService = new AwsSecretsManagerService();
	}
	
	public JSONArray executeArticleScorePredictor(String category, String articleScoreModelFileName,String articleDataFilename,String s3BucketName,String isS3UploadRequiredString) throws JsonMappingException, JsonProcessingException
	{
	
		StopWatch stopWatch = new StopWatch(category);
		stopWatch.start(category);
		JSONArray authorshipLikelihoodScore;
		
		 if (isS3UploadRequiredString!=null && !isS3UploadRequiredString.equalsIgnoreCase("") && (isS3UploadRequiredString == "false" || isS3UploadRequiredString.equalsIgnoreCase("false"))) 
			{  
			 	authorshipLikelihoodScore = callLocalLambda(category,articleScoreModelFileName,articleDataFilename,s3BucketName,isS3UploadRequiredString);
	        } else {
	        	log.info("Getting Secret Name from the Properties: {}", PropertiesUtils.get(RECITER_SCORING_SECRET_NAME));
	        	String secretValueJson = this.awsSecretsManagerService.getSecretKeyPairs(PropertiesUtils.get(RECITER_SCORING_SECRET_NAME)); 
	        	ObjectMapper mapper = new ObjectMapper();
	        	Map<String, String> secretMap = mapper.readValue(secretValueJson, Map.class);
	        	authorshipLikelihoodScore = callAwsLambda(category,articleScoreModelFileName,articleDataFilename,s3BucketName,isS3UploadRequiredString,secretMap.get(LAMBDA_NAME));
	        }
		
		
	   /* try {
	    	    String pythonCommandName ="";
	    	 	if(isS3UploadRequiredString!=null && isS3UploadRequiredString.equalsIgnoreCase("true"))
	            {
	            	pythonCommandName = "python3"; 
	            }
	            else
	            	pythonCommandName = "python";	
	           
	    		
			 	log.info("category and fileName "+ category + " - " + articleScoreModelFileName +" - " + articleDataFilename + " - " + s3BucketName +" -" + isS3UploadRequiredString);

			 	//Prepare to call the Python script
	            ProcessBuilder processBuilder = new ProcessBuilder(pythonCommandName, articleScoreModelFileName,articleDataFilename,s3BucketName,isS3UploadRequiredString);
	            processBuilder.redirectErrorStream(true); 
	            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
	            processBuilder.environment().put("TF_CPP_MIN_LOG_LEVEL", "2");
	            processBuilder.environment().put("TF_ENABLE_ONEDNN_OPTS", "0"); // Optional, to disable oneDNN ops
	            if(isS3UploadRequiredString!=null && isS3UploadRequiredString.equalsIgnoreCase("true"))
	            {
	            	processBuilder.directory(new File("/app/scripts")); 
	            }
	            else
	            	processBuilder.directory(new File("src/main/resources/scripts")); // Set the directory where the script is located
	            
	            // Start the process
	            Process process = processBuilder.start();
	            log.info("processes started");
	            
	         // Capture error stream
	            StringBuilder errorOutput = new StringBuilder();
	            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
	                String line;
	                while ((line = errorReader.readLine()) != null) {
	                    errorOutput.append(line).append(System.lineSeparator());
	                }
	            }
	           
            	// Capture the output
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                // Wait for the process to complete
                int exitCode = process.waitFor();
                log.info("Exited with code: " + exitCode);
                if (exitCode == 0) {
                    // Process output
                    String jsonOutput = output.toString();
                    log.info("jsonOutput : ->" + jsonOutput);
                    return new JSONArray(jsonOutput);
                }
                else
                	 log.info("subprocess error message:",errorOutput);
	
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    
		stopWatch.stop();
		log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
		return authorshipLikelihoodScore;
	}
	
	// Helper method to find JSONObject by article
		private static void findJSONObjectById(JSONArray jsonArray, long articleId) {
		    for (int i = 0; i < jsonArray.length(); i++) {
		        JSONObject jsonObject = jsonArray.getJSONObject(i);
		        if (jsonObject.getLong("id") == articleId) {
		        	double score = jsonObject.getDouble("scoreTotal")*100;
		        }
		    }
		    }
		public static void main(String args[]) throws JsonMappingException, JsonProcessingException
		{
			NeuralNetworkModelArticlesScorer nn = new NeuralNetworkModelArticlesScorer();
			JSONArray articlesIdentityFeedbackScoreTotal = nn.executeArticleScorePredictor("FeedbackIdentityScore", "feedbackIdentityScoreArticles.py","dwf2001-feedbackIdentityScoringInput.json","feedbackScore","false");
			if(articlesIdentityFeedbackScoreTotal!=null && articlesIdentityFeedbackScoreTotal.length() > 0)
				findJSONObjectById(articlesIdentityFeedbackScoreTotal,9856924);
				  
		}
		
		@SuppressWarnings("unused")
		private JSONArray callLocalLambda(String category, String articleScoreModelFileName,String articleDataFilename,String s3BucketName,String isS3UploadRequiredString)
		{
			URL url=null;
			HttpURLConnection conn=null;
			try {
				url = new URL(reciterScoringServiceUrl + PropertiesUtils.get(LAMBDA_FUNCTION_INVOCATION_URL));
				conn = (HttpURLConnection) url.openConnection();
				if(conn!=null)
				{	
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");
				    conn.setDoOutput(true);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      
	        
	        ObjectMapper mapper = new ObjectMapper();
	        
	        Map<String, Object> payloadMap = new HashMap<>();
	        payloadMap.put("scriptFile", articleScoreModelFileName);
	        payloadMap.put("inputDataFile", articleDataFilename);
	        payloadMap.put("useS3Bucket", isS3UploadRequiredString);
	        payloadMap.put("bucket_name", s3BucketName);

	        String payload=null;
			try {
				payload = mapper.writeValueAsString(payloadMap);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
			try {
				 if(conn!=null)
				 {	 
				    // Write payload to Lambda container
				    try (OutputStream os = conn.getOutputStream()) {
				        os.write(payload.getBytes(StandardCharsets.UTF_8));
				    }
				    // Read response from Lambda container
				 
				    // Now it's safe to read the response
			        int responseCode = conn.getResponseCode();
			     // Read response from Lambda
			        StringBuilder response = new StringBuilder();
			        
			        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				        String output;
				        while ((output = reader.readLine()) != null) {
				            response.append(output);
				           
				        }
				    }
			     // Parse the response
			        JSONObject outer = new JSONObject(response.toString());
			        String authorshipLikelihoodScore = outer.getString("authorshiplikelihoodScores");
			        int returnCode = outer.getInt("returncode");
			        JSONArray scoringArray = new JSONArray(authorshipLikelihoodScore);
			        
			        if(returnCode==0)
			        	return scoringArray;
				}

			} catch (IOException | RuntimeException e) {
			    e.printStackTrace(); // You may want to log this properly
			}
			return null;
	    }
		/*
		 * Calls AWS Lambda function
		 */
	    private JSONArray callAwsLambda(String category, String articleScoreModelFileName,String articleDataFilename,String s3BucketName,String isS3UploadRequiredString,String lambdaFunctionName) {
	       
	    	log.info("LambdaFunctionName: {}",lambdaFunctionName);
	    	log.info("LambdaFunctionRegion: {}",PropertiesUtils.get(LAMBDA_FUNCTION_REGION));
			log.info("category: {}",category);
			log.info("articleScoreModelFileName: {}",articleScoreModelFileName);
			log.info("articleDataFilename: {}",articleDataFilename);
			log.info("s3BucketName:{}",s3BucketName);
			log.info("isS3UploadRequiredString:{}",isS3UploadRequiredString);
	    	AWSLambda client = AWSLambdaClientBuilder.standard()
	                .withRegion(PropertiesUtils.get(LAMBDA_FUNCTION_REGION)) 
	                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
	                .build();

	        ObjectMapper mapper = new ObjectMapper();

	        Map<String, Object> payloadMap = new HashMap<>();
	        payloadMap.put("scriptFile", articleScoreModelFileName);
	        payloadMap.put("inputDataFile", articleDataFilename);
	        payloadMap.put("useS3Bucket", isS3UploadRequiredString);
	        payloadMap.put("bucket_name", s3BucketName);

	        
	        
	        String payloadJson=null;
			try {
				payloadJson = mapper.writeValueAsString(payloadMap);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
	        
	        InvokeRequest request = new InvokeRequest()
	                .withFunctionName(lambdaFunctionName)
	                .withPayload(payloadJson);

	        try {
	            InvokeResult result = client.invoke(request);
	            String response = new String(result.getPayload().array(), StandardCharsets.UTF_8);
	            log.info("AWS Lambda Response: {}" , response);
	            JSONObject outer = new JSONObject(response);
	            String authorshipLikelihoodScore = outer.getString("authorshiplikelihoodScores");
		        log.info("AWS Lambda authorshipLikelihoodScore Response: {}",authorshipLikelihoodScore);
		        int returnCode = outer.getInt("returncode");
		        log.info("returnCode: ",returnCode);
		        if(returnCode==0)
		        	return new JSONArray(authorshipLikelihoodScore);;
	          
	        } catch (Exception e) {
	            log.error("Lambda invocation failed: {}" , e.getMessage());
	            e.printStackTrace();
	        }
	        return null;
	    }
	   
}
