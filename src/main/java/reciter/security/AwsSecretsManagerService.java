package reciter.security;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class AwsSecretsManagerService {

	private static final ObjectMapper objectMapper = new ObjectMapper();  
  
    @Value("${aws.secretsmanager.secretName}")
    private String secretName;

    public JsonNode getClientSecret(String clientId) {
        // Initialize the Secrets Manager client
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        System.out.println("secretName pulled from application.properties"+secretName);
        // Fetch the secret from AWS Secrets Manager
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
        
        System.out.println("valueResponse : "+valueResponse);
        
        // Assuming the secret is in JSON format, parse the client secrets from it
        JsonNode clientSecrets = parseJsonSecrets(valueResponse.secretString());
        
        //Printing Client Id, Secret and ClientName
        ObjectNode objectNode = (ObjectNode) clientSecrets;
        clientSecrets.fieldNames().forEachRemaining(fieldName -> {
            JsonNode value = objectNode.get(fieldName);
            System.out.println("fieldName" + fieldName + ": " + value);
        });

        return clientSecrets.get(clientId);  // Return the client secret based on the clientId
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
    
   
}

