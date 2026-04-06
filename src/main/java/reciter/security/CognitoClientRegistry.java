
package reciter.security;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.ListUserPoolClientsRequest;
import com.amazonaws.services.cognitoidp.model.ListUserPoolClientsResult;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CognitoClientRegistry {

    private AWSCognitoIdentityProvider cognitoClient;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    private LoadingCache<String, Set<String>> clientCache;

    @PostConstruct
    public void init() {
        // Initialize AWS Client
        this.cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard()
                .withRegion(awsRegion)
                .build();

        // Initialize Caffeine Cache
        this.clientCache = Caffeine.newBuilder()
                .refreshAfterWrite(15, TimeUnit.MINUTES) // Background refresh every 15m
                .expireAfterWrite(24, TimeUnit.HOURS)    // Cleanup if AWS stays down
                .build(key -> fetchAllClientsFromAws());
    }

    public boolean isAllowed(String clientId) {
        try {
            // Get the set from cache (Key name doesn't matter, we use a constant)
            Set<String> authorizedIds = clientCache.get("AUTHORIZED_CLIENTS");
            return authorizedIds != null && authorizedIds.contains(clientId);
        } catch (Exception e) {
            log.error("Security Bypass Prevention: Error checking Client ID {}", clientId, e);
            return false;
        }
    }

    private Set<String> fetchAllClientsFromAws() {
        Set<String> freshIds = new HashSet<>();
        String nextToken = null;

        try {
            do {
                // Use ListUserPoolClientsRequest instead of ListAppClientsRequest
                ListUserPoolClientsRequest request = new ListUserPoolClientsRequest()
                        .withUserPoolId(userPoolId)
                        .withMaxResults(60)
                        .withNextToken(nextToken);

                ListUserPoolClientsResult result = cognitoClient.listUserPoolClients(request);
                
                // Collect the Client IDs
                result.getUserPoolClients().forEach(client -> freshIds.add(client.getClientId()));
                
                nextToken = result.getNextToken();
            } while (nextToken != null);

            return Collections.unmodifiableSet(freshIds);
        } catch (Exception e) {
            log.error("Failed to sync with Cognito. Check IAM permissions for 'ListUserPoolClients'.", e);
            throw e; 
        }
    }
}