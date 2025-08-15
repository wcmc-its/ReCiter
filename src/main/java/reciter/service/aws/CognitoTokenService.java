package reciter.service.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reciter.security.APISecurityConfig;
import reciter.security.AwsSecretsManagerService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Service
public class CognitoTokenService {

		private static final Logger log = LoggerFactory.getLogger(CognitoTokenService.class);
	 	private final RestTemplate restTemplate = new RestTemplate();
	 	private final String CLIENT_ID ="clientID";
	 	private final String CLIENT_NAME ="clientName";
	 	private final String USER_POOL_ID = "userPoolID";
	 	private final String SCOPE = "scope";
	 	private final String CLIENT_SECRET = "clientSecret";
	 	
	 	@Autowired
	    private AwsSecretsManagerService awsSecretsManagerService; // Inject the service to get the secret
	 	
	 	@Value("${aws.secretsmanager.consumer.secretName}")
		private String consumerSecretName;
	 	
	 	
	    public ResponseEntity<String> getCognitoAccessToken(String clientName) {
	    	
	    	
	    	JsonNode secretsJson = getClientSecretsFromSecretsManager();
	    	log.info("Secrets JSON Node :", secretsJson.fieldNames());
	    	
	    	Map<String, String> clientSecretMap = extractClientSecret(secretsJson, clientName);
	    	clientSecretMap.forEach((key,value) -> log.info("secretsName:",key));
			
			String clientID = clientSecretMap.get(CLIENT_ID)!=null?clientSecretMap.get(CLIENT_ID) :"";
			String userPoolID = clientSecretMap.get(USER_POOL_ID)!=null?clientSecretMap.get(USER_POOL_ID):"" ;
			String scope = clientSecretMap.get(SCOPE)!=null?clientSecretMap.get(SCOPE):"";
			String clientSecret = clientSecretMap.get(CLIENT_SECRET)!=null?clientSecretMap.get(CLIENT_SECRET):"";
	    	
	        String tokenEndpoint = "https://" + userPoolID + "/oauth2/token";

	        // Encode clientId:clientSecret in base64 for Basic Auth
	        String auth = clientID + ":" + clientSecret;
	        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	        String basicAuthHeader = "Basic " + encodedAuth;

	        // Set headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	        headers.set("Authorization", basicAuthHeader);

	        // Set request body
	        String body = "grant_type=client_credentials&scope=" + scope;

	        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

	        // Send POST request
	        ResponseEntity<String> response = restTemplate.exchange(
	                tokenEndpoint,
	                HttpMethod.POST,
	                requestEntity,
	                String.class
	        );

	        // Handle response
	        if (response.getStatusCode() == HttpStatus.OK) {
	            return response; // Contains JSON with access_token, etc.
	        } else {
	            throw new RuntimeException("Failed to get token: " + response.getStatusCode() + " - " + response.getBody());
	        }
	    }
	    
	 // Fetch the Issuer URL from Secrets Manager
	    private JsonNode getClientSecretsFromSecretsManager() {
	    	JsonNode secretValueJson = awsSecretsManagerService.getSecrets(consumerSecretName); 
	        return secretValueJson;
	    }
	    public Map<String, String> extractClientSecret(JsonNode secretsJson, String clientName) {
	        Map<String, String> extractedJson = null;

	        try {
	            ObjectMapper objectMapper = new ObjectMapper();

	            // Parse the outer secret value (if stored as JSON string)
	            secretsJson = objectMapper.readTree(secretsJson.asText());

	            // Convert fields iterator to stream
	            extractedJson = StreamSupport.stream(
	                    Spliterators.spliteratorUnknownSize(secretsJson.fields(), Spliterator.ORDERED), false)
	                .map(Entry::getValue)
	                .map(valueNode -> objectMapper.convertValue(valueNode, Map.class))
	                .filter(map -> map.containsKey(CLIENT_NAME)
	                        && clientName.equalsIgnoreCase(String.valueOf(map.get(CLIENT_NAME))))
	                .findFirst()
	                .orElse(null);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return extractedJson;
	    }
}
