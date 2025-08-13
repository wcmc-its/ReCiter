package reciter.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.JsonNode;

@EnableWebSecurity
public class APISecurityConfig {
    
	/**
	 * @author szd2013
	 * This will intercept and request for admin api and authenticate its api key
	 */
	@Configuration
    public static class AdminApiSecurityConfig extends WebSecurityConfigurerAdapter {
    	
		 private static final Logger log = LoggerFactory.getLogger(AdminApiSecurityConfig.class);
		 
		private final String principalRequestHeader = "api-key";
	    private final String authorizationHeader = "Authorization"; 

	    private String principalRequestValue = System.getenv("ADMIN_API_KEY");
	    
	    @Value("${spring.security.enabled}")
	    private boolean securityEnabled;
	    
	    @Autowired
	    private AwsSecretsManagerService awsSecretsManagerService; // Inject the service to get the secret
    
	    @Autowired
		private S3UserLogHandler s3UserLogHandler;

	    @Value("${aws.secretsmanager.consumer.secretName}")
		private String consumerSecretName;
	    
	    
	    @Override
	    protected void configure(HttpSecurity httpSecurity) throws Exception {
	        APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader,authorizationHeader);
	        
	        final String[] clientId = new String[1];
	        final String[] principal= new String[1];
	        final HttpServletRequest[] request = {};
	        filter.setAuthenticationManager(new AuthenticationManager() {
	
	            @Override
	            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
	            	log.info("Coming into authenticate method in APISecurityConfig.");
	                principal[0] = (String) authentication.getPrincipal();
	                request[0] = (HttpServletRequest) authentication.getDetails();
	                String apiKey = request.length > 0 ? request[0].getHeader("api-key") : "";
	                String authHeader = request.length > 0? Optional.ofNullable(request[0].getHeader("Authorization")).orElseGet(() -> request[0].getHeader("authorization")):"";
	                if (authHeader != null && authHeader.startsWith("Bearer ")) {
	                	log.info("Authorization token received from the Client.");
	                	String token = authHeader.substring(7);
	                	clientId[0] = JWT.decode(token).getClaim("client_id").asString();
	                	if(clientId.length > 0 && clientId[0] !=null && !clientId[0].equalsIgnoreCase(""))
	                		   authentication.setAuthenticated(true);
	                	else
	                		 throw new BadCredentialsException("Client_id was not found in JWT token or expired token ");
	                }
	                else if(apiKey!=null && !apiKey.equalsIgnoreCase(""))
	                {	
	                	log.info("api-key token received from the Client.");
		                if (principal.length >0 && principalRequestValue!=null && !principalRequestValue.equals(principal[0]))
		                {
		                    throw new BadCredentialsException("The API key was not found or not the expected value.");
		                }
		                authentication.setAuthenticated(true);
	                } 
	                return authentication;
	            }
	        });
	        JsonNode secretsJson = getClientSecretsFromSecretsManager(clientId[0]);
       	 	String clientName =null;
       	 	String clientIdInToken=null;
	        if(secretsJson!=null && secretsJson.has("clientName"))
	        	clientName = secretsJson.get("clientName").asText();
	        if(secretsJson!=null && secretsJson.has("client_id"))
       	 	 	clientIdInToken = secretsJson.get("client_id").asText();
	        if(securityEnabled && principal.length > 0 && principalRequestValue!=null && principalRequestValue.equals(principal[0])) {
	        	log.info("api-key matched.");
		        httpSecurity.
		            antMatcher("/reciter/**").
		            csrf().disable().
		            sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
		            and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
	        }
	        else if(clientId.length > 0 && clientId[0]!=null && clientId[0].equalsIgnoreCase(clientIdInToken))
	        {
	        	log.info("Authorization token matched *");
	        	 httpSecurity.
		            antMatcher("/reciter/article-retrieval/**").
		            csrf().disable().
		            sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
		            and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
	        	 
		        	 // Create a new UserLog entry
	                UserLog userLog = new UserLog(clientId.length >0 ? clientId[0]:"", clientName,request.length >0? request[0].getRequestURI():"",request.length >0 ? request[0].getParameter("uid"):"",LocalDateTime.now().toString());
	                log.info("S3 bucket entries***"+userLog.toString());
	                // Get the current date as a string in yyyy-MM-dd format
	                String date = Instant.now().toString().split("T")[0];
	                
	                // Write the user log entry to the S3 bucket
	                s3UserLogHandler.writeUserLog(userLog, date);
		        }
	        	
	    }
	    
	    @Override
	    public void configure(WebSecurity web) throws Exception {
	    	if(!securityEnabled) {
		        web
		        .ignoring()
		        .antMatchers("/reciter/**");
			}
			//Added to whitelist ping controller
			web
			.ignoring()
			.antMatchers("/reciter/ping"); 
			
			 web
		        .ignoring()
		        .antMatchers("/reciter/article-retrieval/**");
	    }
	    
	 // Fetch the Issuer URL from Secrets Manager
	    private JsonNode getClientSecretsFromSecretsManager(String clientId) {
	    	JsonNode secretValueJson = awsSecretsManagerService.getSecrets(consumerSecretName); 
	        if (secretValueJson != null && clientId!=null && !clientId.equalsIgnoreCase("")) {
	            return secretValueJson.get(clientId); 
	        }
	        return null;
	    }
	    
    }
	
	
	
}
