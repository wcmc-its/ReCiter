package reciter.consumer.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CognitoAuthService {

    // These should be injected from Kubernetes Secrets/Env Vars
    private final String USER_POOL_ID = System.getenv("AWS_COGNITO_USER_POOL_ID");
    private final String CLIENT_ID = System.getenv("MASTER_CLIENT_ID");
    private final String CLIENT_SECRET = System.getenv("MASTER_CLIENT_SECRET");
    private final String AWS_REGION = System.getenv("AWS_REGION");
    
 // Initialize Caffeine Cache
    // Key: Department Username, Value: JWT Token
    private final Cache<String, String> tokenCache = Caffeine.newBuilder()
            .expireAfterWrite(55, TimeUnit.MINUTES) // Cognito tokens usually last 60 mins; cache for 55 to be safe
            .maximumSize(2000) // One slot per department machine
            .build();

    private final AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard()
            .withRegion(AWS_REGION)
            .build();

    public String authenticateConsumer(String username, String password) {
        
        // 1. Check Cache First
        String cachedToken = tokenCache.getIfPresent(username);
        if (cachedToken != null) {
            return cachedToken;
        }

        // 2. If not in cache, calculate Hash and call Cognito
        String secretHash = calculateSecretHash(CLIENT_ID, CLIENT_SECRET, username);

        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", username);
        authParams.put("PASSWORD", password);
        authParams.put("SECRET_HASH", secretHash);

        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .withUserPoolId(USER_POOL_ID)
                .withClientId(CLIENT_ID)
                .withAuthParameters(authParams);

        try {
            AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(authRequest);
            String idToken = result.getAuthenticationResult().getIdToken();
            
            // 3. Store in Cache before returning
            tokenCache.put(username, idToken);
            
            return idToken;
            
        } catch (Exception e) {
            System.err.println("Login failed for: " + username + " Error: " + e.getMessage());
            throw e;
        }
    }

    private String calculateSecretHash(String clientId, String clientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
        SecretKeySpec signingKey = new SecretKeySpec(
                clientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(clientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating secret hash", e);
        }
    }
}