package reciter.service.aws;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reciter.security.AwsSecretsManagerService;

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
	    	
	    	JsonNode secretsJson = awsSecretsManagerService.getSecretValueFromSecretsManager(consumerSecretName,clientName);
	    	System.out.println("Secrets JSON Node :"+ secretsJson);
	    	System.out.println("clientName :"+ clientName);
	    	
	    	JsonNode clientIdSecrets = awsSecretsManagerService.getSecretValueFromSecretsManager(consumerSecretName,secretsJson.asText());
	    	
	    	String clientID = clientIdSecrets.get(CLIENT_ID)!=null?clientIdSecrets.get(CLIENT_ID).asText() :"";
	    	System.out.println("clientID :"+ clientID);
			String userPoolID = clientIdSecrets.get(USER_POOL_ID)!=null?clientIdSecrets.get(USER_POOL_ID).asText():"" ;
			System.out.println("userPoolID :"+ userPoolID);
			String scope = clientIdSecrets.get(SCOPE)!=null?clientIdSecrets.get(SCOPE).asText():"";
			System.out.println("scope :"+ scope);
			String clientSecret = clientIdSecrets.get(CLIENT_SECRET)!=null?clientIdSecrets.get(CLIENT_SECRET).asText():"";
			System.out.println("clientSecret :"+ clientSecret);
	    	
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
}
