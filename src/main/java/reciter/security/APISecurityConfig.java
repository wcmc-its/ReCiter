package reciter.security;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

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

@EnableWebSecurity(debug = true)
public class APISecurityConfig {
    
	/**
	 * @author szd2013
	 * This will intercept and request for admin api and authenticate its api key
	 */
	@Configuration
    public static class AdminApiSecurityConfig extends WebSecurityConfigurerAdapter {
    	
	    private final String principalRequestHeader = "api-key";
	    private final String authorizationHeader = "Authorization"; 

	    private String principalRequestValue = System.getenv("ADMIN_API_KEY");
	    private String principalConsumerRequestValue = System.getenv("CONSUMER_API_KEY");
	    
	    @Value("${spring.security.enabled}")
	    private boolean securityEnabled;
    
	    @Override
	    protected void configure(HttpSecurity httpSecurity) throws Exception {
	        APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader,authorizationHeader);
	        
	        String[] clientId = {};
	        String[] principal= {};
	        
	        filter.setAuthenticationManager(new AuthenticationManager() {
	
	            @Override
	            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
	                principal[0] = (String) authentication.getPrincipal();
	                HttpServletRequest request = (HttpServletRequest) authentication.getDetails();
	                String apiKey = request.getHeader("api-key");
	                String authHeader = Optional.ofNullable(request.getHeader("Authorization")).orElseGet(() -> request.getHeader("authorization"));
	                if (authHeader != null && authHeader.startsWith("Bearer ")) {
	                	
	                	String token = authHeader.substring(7);
	                	clientId[0] = JWT.decode(token).getClaim("client_id").asString();
	                	if(clientId[0] !=null && !clientId[0].equalsIgnoreCase(""))
	                		   authentication.setAuthenticated(true);
	                	else
	                		 throw new BadCredentialsException("Client_id was not found in JWT token or expired token ");
	                }
	                else if(apiKey!=null && !apiKey.equalsIgnoreCase(""))
	                {	
		                if (!principalRequestValue.equals(principal) && !principalConsumerRequestValue.equals(principal))
		                {
		                    throw new BadCredentialsException("The API key was not found or not the expected value.");
		                }
		                authentication.setAuthenticated(true);
	                } 
	                return authentication;
	            }
	        });
	        if(securityEnabled && principalRequestValue.equals(principal[0])) {
		        httpSecurity.
		            antMatcher("/reciter/**").
		            csrf().disable().
		            sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
		            and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
	        }
	        else if(clientId[0]!=null && clientId.length > 1 || principalConsumerRequestValue.equals(principal))
	        {
	        	 httpSecurity.
		            antMatcher("/reciter/article-retrieval/**").
		            csrf().disable().
		            sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
		            and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
	        }
	        	
	    }
	    
	    @Override
	    public void configure(WebSecurity web) throws Exception {
	    	System.out.println("Coming into this AdminApiSecurityConfig configure1******************");
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
	    
    }
    
	/**
	 * @author szd2013
	 * This will intercept and request for consumer api and authenticate its api key
	 */
/*	@Configuration
	@Order(1)
    public static class ConsumerApiSecurityConfig extends WebSecurityConfigurerAdapter {
    	
		//private final String principalRequestHeader = "Authorization";//"api-key";
		
        //private String principalRequestValue = System.getenv("CONSUMER_API_KEY");
        
        private final JwtTokenAuthenticationFilter filter;
        
        @Value("${security.enabled:true}")
	    private boolean securityEnabled;
        
        @Autowired
        public ConsumerApiSecurityConfig(JwtTokenAuthenticationFilter filter)
        {
        	this.filter = filter;
        }
        
    	    @Override
    	    protected void configure(HttpSecurity httpSecurity) throws Exception {
    	    	if(securityEnabled) {
    	    		System.out.println("coming inside if condition*****************"+securityEnabled);
    	          httpSecurity.
	    	            antMatcher("/reciter/article-retrieval/**").
	    	            csrf().disable().
	    	            sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
	    	            and().addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class).authorizeRequests().anyRequest().authenticated();
    	        }
    	    	
    	    }
    	    
    	    @Override
    	    public void configure(WebSecurity web) throws Exception {
    	    	if(!securityEnabled) {
    		        web
    		        .ignoring()
    		        .antMatchers("/reciter/article-retrieval/**");
    	    	} 
    	    }
     }*/
	
}
