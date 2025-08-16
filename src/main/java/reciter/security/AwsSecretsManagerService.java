package reciter.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.regions.Region;

@Service
public class AwsSecretsManagerService {

	private static final Logger log = LoggerFactory.getLogger(AwsSecretsManagerService.class);
	
	private static final ObjectMapper objectMapper = new ObjectMapper();  
	
	
	@Value("${aws.secretsmanager.region}")
    private String secretManagerRegion;
	
    public JsonNode getSecrets(String secretName) {
    	
    	System.out.println("Resolved AWS credentials: "+DefaultCredentialsProvider.create().resolveCredentials());
        // Initialize the Secrets Manager client
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
        		.region(Region.of(secretManagerRegion)) 
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        log.info("secretName pulled from application.properties" + secretName);
        // Fetch the secret from AWS Secrets Manager
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
        
        
        // Assuming the secret is in JSON format, parse the client secrets from it
        JsonNode clientSecrets = parseJsonSecrets(valueResponse.secretString());
     
        return clientSecrets;  // Return the client secret based on the clientId
     }

    private JsonNode parseJsonSecrets(String secretJson) {
 
         try {
        	 if(secretJson!=null && !secretJson.equalsIgnoreCase(""))
        	 {
        		 // Parse the secret string into a JsonNode (Jackson's tree model)
                 JsonNode jsonNode = objectMapper.readTree(secretJson);
                 return jsonNode;
        	 }
        	 else {
                 throw new Exception("Secret not found or is empty");
             }

         } catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null; 
    }
    
    public String getSecretKeyPairs(String secretName) {
        // Initialize the Secrets Manager client
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        log.info("secretName1 pulled from application.properties" + secretName);
        // Fetch the secret from AWS Secrets Manager
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
        
        
        // Assuming the secret is in JSON format, parse the client secrets from it
        String clientSecrets = valueResponse.secretString();
     
        return clientSecrets;  // Return the client secret based on the clientId
     }
}

