package reciter.consumer.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import com.github.benmanes.caffeine.cache.*;
import org.springframework.http.ResponseEntity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import reciter.consumer.dto.TokenInfo;
import reciter.consumer.exception.ApiKeyMappingNotFoundException;
import reciter.security.AwsSecretsManagerService;
/**
 * Service responsible for managing authentication tokens retrieved from AWS Cognito.
 *
 * <p>This service provides methods to fetch and cache Cognito access tokens based on API keys.
 * It integrates with AWS Secrets Manager to dynamically resolve client credentials, and uses the
 * Caffeine cache to reduce token fetch overhead by caching valid tokens until their expiration.</p>
 *
 * <p>Main responsibilities of this service include:</p>
 * <ul>
 *   <li>Resolving API keys to client credentials via AWS Secrets Manager</li>
 *   <li>Fetching OAuth2 tokens from Cognito using resolved credentials</li>
 *   <li>Caching token responses using Caffeine until token expiry</li>
 *   <li>Handling errors such as missing secrets, invalid API keys, or Cognito failures</li>
 * </ul>
 *
 * <p>This service is designed to be thread-safe and performant under high load,
 * ensuring that tokens are fetched only once per API key within the validity period.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * TokenInfo token = cognitoTokenService.getToken("your-api-key");
 * String accessToken = token.getAccessToken();
 * }</pre>
 *
 * @author mjangari
 * @version 1.0
 * @since 2025-09-15
 */
@Service
public class CognitoTokenService {

		private static final Logger log = LoggerFactory.getLogger(CognitoTokenService.class);
	 	private final RestTemplate restTemplate = new RestTemplate();
	 	private final String CLIENT_ID ="clientID";
	 	private final String USER_POOL_ID = "userPoolID";
	 	private final String SCOPE = "scope";
	 	private final String CLIENT_SECRET = "clientSecret";
	 	
	 	@Autowired
	    private AwsSecretsManagerService awsSecretsManagerService; // Inject the service to get the secret
	 	
	 	@Value("${aws.secretsmanager.consumer.secretName}")
		private String consumerSecretName;
	 	
	 	@Value("${aws.congito.userpool.region}")
		private String cognitoPoolRegion;
	 	
	 // Cache with a single key (since it's one token)
	    private final LoadingCache<String, TokenInfo> tokenCache;

	    public TokenInfo getJWTAccessToken(String apiKey) {
	            return tokenCache.get(apiKey);
	    }
	 	 
	 	 public CognitoTokenService() {
	 		this.tokenCache = Caffeine.newBuilder()
	 	            .expireAfter(new Expiry<String, TokenInfo>() {
	 	                @Override
	 	                public long expireAfterCreate(String apiKey, TokenInfo token, long currentTime) {
	 	                    long secondsUntilExpiry = Instant.now().until(token.getExpiresAt(), java.time.temporal.ChronoUnit.SECONDS);
	 	                    return TimeUnit.SECONDS.toNanos(Math.max(secondsUntilExpiry - 60, 0)); // Subtract 60s buffer
	 	                }

	 	                @Override
	 	                public long expireAfterUpdate(String apiKey, TokenInfo token, long currentTime, long currentDuration) {
	 	                    return currentDuration;
	 	                }

	 	                @Override
	 	                public long expireAfterRead(String apiKey, TokenInfo token, long currentTime, long currentDuration) {
	 	                    return currentDuration;
	 	                }
	 	            })
	 	            .build(this::getCognitoAccessToken);
	     }
	 	 
	    public TokenInfo getCognitoAccessToken(String apiKey) {
	    	
	    	JsonNode clientIdSecretValues =null;
	    	//Map<String, String> secretMap =null;
	    	JsonNode clientIdSecrets = this.awsSecretsManagerService.getSecretValueFromSecretsManager(consumerSecretName,apiKey); 
	    	if(clientIdSecrets == null)
	    	{
	    	    throw new ApiKeyMappingNotFoundException("No client mapping found for API key: " + apiKey);
	    	}
	    	/*String clientIdSecrets =null;
	    	try
	    	{
	    		ObjectMapper objectMapper = new ObjectMapper();
	            secretMap = objectMapper.readValue(secretsJson, Map.class);
	            clientIdSecretValues = secretMap.get(apiKey);
	    	}
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}*/
	    	/*String clientIdSecrets =  secretMap.get(clientId);
	    	if(clientIdSecrets == null || clientIdSecrets.equalsIgnoreCase(""))
	    		throw new BadCredentialsException("Invalid API Key");*/
	    	ObjectMapper objectMapper = new ObjectMapper();
	    	try {
				 clientIdSecretValues = objectMapper.readTree(clientIdSecrets.asText());
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	
	    	String clientID = clientIdSecretValues.get(CLIENT_ID)!=null?clientIdSecretValues.get(CLIENT_ID).asText() :"";
	    	String userPoolID = clientIdSecretValues.get(USER_POOL_ID)!=null?clientIdSecretValues.get(USER_POOL_ID).asText():"" ;
			String scope = clientIdSecretValues.get(SCOPE)!=null?clientIdSecretValues.get(SCOPE).asText():"";
			String clientSecret = clientIdSecretValues.get(CLIENT_SECRET)!=null?clientIdSecretValues.get(CLIENT_SECRET).asText():"";
				
	        String tokenEndpoint = "https://" + cognitoPoolRegion +  userPoolID + ".auth.us-east-1.amazoncognito.com/oauth2/token";

	        // Encode clientId:clientSecret in base64 for Basic Auth
	        String auth = clientID + ":" + clientSecret;
	        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
	        String basicAuthHeader = "Basic " + encodedAuth;

	        // Set headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	        headers.set("Authorization", basicAuthHeader);

	        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
	        formData.add("grant_type", "client_credentials");
	        formData.add("scope", scope);

	      
	        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

	        // Send POST request
	        ResponseEntity<TokenInfo> response = restTemplate.exchange(
	        		tokenEndpoint,
	                HttpMethod.POST,
	                request,
	                TokenInfo.class
	            );
	        
	        TokenInfo token = response.getBody();
	        if (response.getStatusCode() == HttpStatus.OK && token != null) {
	            token.computeExpiry();  // compute expiresAt based on expiresIn
	        } else {
	        	throw new RuntimeException("Failed to get token: " + response.getStatusCode() + " - " + response.getBody());
	        }

	        return token;
	    }
}
