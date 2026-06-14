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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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
import java.time.Duration;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

/**
 * @author mjangari 
 * This will intercept and request for consumer api JWT token
 * and admin api key and authenticate its JWT token or api-key
 */

@EnableWebSecurity
@Configuration
public class APISecurityConfig {

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

     // 2. Timestamp Validator (Checks 'exp' and 'nbf')
        // This ensures the token is not expired and is currently valid
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(30));

        // 3. Issuer Validator
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri.trim());

        // 4. Combine all validators
        // Note: Order matters; usually check issuer and timestamp before custom claims
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
            timestampValidator,
            issuerValidator, 
            audValidator
        );

        jwtDecoder.setJwtValidator(combinedValidator);
        return jwtDecoder;
    }
    
	@Bean
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity httpSecurity, JwtDecoder jwtDecoder) throws Exception {

		log.info("Inside apiSecurityFilterChain of the APISecurity");
		// HttpSecurity.antMatcher(String) is the 5.7 matcher; it is renamed to securityMatcher
		// only at Spring Security 5.8/6.0, so we keep antMatcher here for the 2.7 / 5.7.11 line.
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
	        .decoder(jwtDecoder) // injected singleton bean, not self-invoked (no second decoder)
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

		return httpSecurity.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> {
			// /reciter/ping always bypassed (whitelisted ping controller)
			web.ignoring().antMatchers("/reciter/ping");
			// entire /reciter/** bypassed only when security is disabled (local/dev/test)
			if (!securityEnabled) {
				web.ignoring().antMatchers("/reciter/**");
			}
		};
	}
}
