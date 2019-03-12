package reciter.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@EnableWebSecurity
public class APISecurityConfig {
    
	/**
	 * @author szd2013
	 * This will intercept and request for admin api and authenticate its api key
	 */
	@Configuration
    public static class AdminApiSecurityConfig extends WebSecurityConfigurerAdapter {
    	
	    private final String principalRequestHeader = "api-key";

	    private String principalRequestValue = System.getenv("ADMIN_API_KEY");
	    
	    @Value("${spring.security.enabled}")
	    private boolean securityEnabled;
    
	    @Override
	    protected void configure(HttpSecurity httpSecurity) throws Exception {
	        APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader);
	        filter.setAuthenticationManager(new AuthenticationManager() {
	
	            @Override
	            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
	                String principal = (String) authentication.getPrincipal();
	                if (!principalRequestValue.equals(principal))
	                {
	                    throw new BadCredentialsException("The API key was not found or not the expected value.");
	                }
	                authentication.setAuthenticated(true);
	                return authentication;
	            }
	        });
	        if(securityEnabled) {
		        httpSecurity.
		            antMatcher("/reciter/**").
		            csrf().disable().
		            sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
		            and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
	        }
	    }
	    
	    @Override
	    public void configure(WebSecurity web) throws Exception {
	    	if(!securityEnabled) {
		        web
		        .ignoring()
		        .antMatchers("/reciter/**");
	    	} 
	    }
	    
    }
    
	/**
	 * @author szd2013
	 * This will intercept and request for consumer api and authenticate its api key
	 */
	@Configuration
	@Order(1)
    public static class ConsumerApiSecurityConfig extends WebSecurityConfigurerAdapter {
    	
		private final String principalRequestHeader = "api-key";
		
        private String principalRequestValue = System.getenv("CONSUMER_API_KEY");
        
        @Value("${security.enabled:true}")
	    private boolean securityEnabled;
        
    	    @Override
    	    protected void configure(HttpSecurity httpSecurity) throws Exception {
    	        APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader);
    	        filter.setAuthenticationManager(new AuthenticationManager() {
    	
    	            @Override
    	            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	                String principal = (String) authentication.getPrincipal();
    	                if (!principalRequestValue.equals(principal))
    	                {
    	                    throw new BadCredentialsException("The API key was not found or not the expected value.");
    	                }
    	                authentication.setAuthenticated(true);
    	                return authentication;
    	            }
    	        });
    	        
    	        if(securityEnabled) {
	    	        httpSecurity.
	    	            antMatcher("/reciter/article-retrieval/**").
	    	            csrf().disable().
	    	            sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
	    	            and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
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
     }
}
