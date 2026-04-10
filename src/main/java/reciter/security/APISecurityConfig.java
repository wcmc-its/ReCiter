package reciter.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author mjangari 
 * This will intercept and request for consumer api JWT token
 * and admin api key and authenticate its JWT token or api-key
 */

@EnableWebSecurity(debug = true)
@Configuration
public class APISecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired(required = false)
    private MultiApiKeyFilter multiApiKeyFilter;
	 
	@Autowired(required = false)
    private CustomAuthenticationEntryPoint customEntryPoint;

	private static final Logger log = LoggerFactory.getLogger(APISecurityConfig.class);

	@Value("${spring.security.enabled}")
	private boolean securityEnabled;
	
	@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;
	
	@Value("${aws.cognito.issuer-uri}")
	private String issuerUri;
	
	@Value("${aws.cognito.user-pool-id}")
    private String userPoolId;
	
	@Value("${aws.cognito.userpool.region}")
    private String awsRegion;
	
	@Value("${aws.cognito.cache.refresh.timeinterval}")
    private int cognitoCacheRefreshTimeInterval;
	
	@Autowired
	private S3UserLogHandler s3UserLogHandler;
	
	// Get the expected ID once from your environment
	private final String MASTER_CLIENT_ID = System.getenv("MASTER_CLIENT_ID");
	
	// This is the name of the data bucket in the cache for authorized consumer clients.
    /*private static final String CACHE_KEY = "AUTHORIZED_CONSUMER_CLIENTS";

    private final LoadingCache<String, Set<String>> cognitoClientCache = Caffeine.newBuilder()
        .refreshAfterWrite(15, TimeUnit.MINUTES)
        .build(key -> fetchAllUserPoolClients());*/
    
    @Bean
	public MultiApiKeyFilter MultiApiKeyAuthenticationFilter() {
	   log.info("JWT filter bean is being created!");
	   return new MultiApiKeyFilter();
	}
	
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyScanner() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() 
    {
        log.info("DEBUG: Issuer URI is " + issuerUri);
        log.info("DEBUG: awsRegion is " + awsRegion);
        log.info("DEBUG: u is " + userPoolId);
 
    	// 1. Mandatory Property Validation (The Guard Clause)
        boolean isConfigMissing = jwkSetUri == null || jwkSetUri.isEmpty() || jwkSetUri.contains("${") ||
                                 issuerUri == null || issuerUri.isEmpty() || issuerUri.contains("${") ||
                                 "NONE".equals(userPoolId);

        if (isConfigMissing) {
            log.warn(">>> JWT SECURITY IS DISABLED <<<");
            log.warn("Missing or unresolved Kubernetes ConfigMap values. JWK URI: {}, Pool ID: {}", jwkSetUri, userPoolId);
            
            // Return a fail-fast decoder so the Spring Context stays healthy locally
            return token -> {
                throw new JwtException("JWT Authentication is not configured in this environment.");
            };
        }

        // 2. Initialize the Base Decoder
        log.info("Initializing Cognito JwtDecoder for Region: {} with Refresh Interval: {} min", 
                 awsRegion, cognitoCacheRefreshTimeInterval);
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri.trim()).build();

        // 1. Correct Validator for ID Tokens (Handles ArrayList from Cognito)
        OAuth2TokenValidator<Jwt> audValidator = new JwtClaimValidator<List<String>>(
            JwtClaimNames.AUD, 
            audList -> audList != null && audList.contains(System.getenv("MASTER_CLIENT_ID"))
        );

        // 2. Issuer Validator
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri.trim());

        // 3. Combine
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
            issuerValidator, 
            audValidator
        );

        jwtDecoder.setJwtValidator(combinedValidator);
        return jwtDecoder;
        /*OAuth2TokenValidator<Jwt> clientIdValidator = new JwtClaimValidator<String>(
            "client_id", 
            clientId -> {
                if (clientId == null) return false;
                try {
                    Set<String> authorizedIds = cognitoClientCache.get(CACHE_KEY);
                    return authorizedIds != null && authorizedIds.contains(clientId);
                } catch (Exception e) {
                    log.error("Security Registry Error: Could not validate Client ID {}", clientId, e);
                    return false;
                }
            }
        );

        // 2. Explicit Issuer Validator (Avoids the 'Default' one which might look for 'aud')
        // Ensure issuerUri does NOT have a trailing slash unless your Cognito settings do
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri.trim());

        // 3. Combine them using DelegatingOAuth2TokenValidator
        // We explicitly combine your cache-logic with standard OIDC checks
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
            issuerValidator, 
            clientIdValidator
        );

        jwtDecoder.setJwtValidator(combinedValidator);
        return jwtDecoder;*/
    
    }

	// 4. The AWS SDK v1 Fetch Logic (Helper Method)
   /* private Set<String> fetchAllUserPoolClients() {
    	 log.info("Refreshing Cognito Client Registry for Pool: {}", userPoolId);
        AWSCognitoIdentityProvider client = AWSCognitoIdentityProviderClientBuilder.standard()
                .withRegion(awsRegion)
                .build();

        Set<String> freshIds = new HashSet<>();
        String nextToken = null;

        try {
            do {
                ListUserPoolClientsRequest request = new ListUserPoolClientsRequest()
                        .withUserPoolId(userPoolId)
                        .withMaxResults(60)
                        .withNextToken(nextToken);

                ListUserPoolClientsResult result = client.listUserPoolClients(request);
                log.info("Successfully fetched {} clients", result.getUserPoolClients().size());
                result.getUserPoolClients().forEach(c -> freshIds.add(c.getClientId()));
                nextToken = result.getNextToken();
                
            } while (nextToken != null);
            log.info("nextToken*********",nextToken);
            log.info("freshIds*********",freshIds);
            return Collections.unmodifiableSet(freshIds);
        } catch (Exception e) {
            log.error("CRITICAL FAILURE: Could not reach AWS Cognito API!", e);
            // If this fails, the filter chain will have an empty registry and reject all tokens
            throw e; 
        }
    }*/
	
    
    
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		
		log.info("Inside the Configure method of the APiSecurity",httpSecurity);
		httpSecurity.antMatcher("/reciter/**")
	    .csrf().disable()
	    .sessionManagement()
	        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	    .and()
	    .exceptionHandling()
	        .authenticationEntryPoint(customEntryPoint)
	    .and()

	    // 1. Setup JWT Decoding (Fires ONLY if Bearer header is present)
	    .oauth2ResourceServer()
	        .jwt()
	        .decoder(jwtDecoder()) // Uses your Caffeine-backed bean
	    .and()
	    .and()

	    // 2. Add API Key Filter
	    .addFilterBefore(multiApiKeyFilter, UsernamePasswordAuthenticationFilter.class)

	    // 3. Asynchronous S3 Logging Filter
	    .addFilterAfter(new S3LoggingFilter(s3UserLogHandler), 
	                    org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter.class)

	    .authorizeRequests()
	        // this path /reciter/generate-access-token is allowed to generate a JWT token
	    	.antMatchers(HttpMethod.POST, "/reciter/generate-access-token").permitAll() 
	        
	        // Everything else requires a valid JWT or API Key
	        .anyRequest().authenticated();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		log.info("Inside the Configure method taking WebSecurity param of the APiSecurity",web);
		if (!securityEnabled) {
			web.ignoring().antMatchers("/reciter/**");
		}
		// Added to whitelist ping controller and Access Token
		web.ignoring().antMatchers("/reciter/ping");
		
	}
}
