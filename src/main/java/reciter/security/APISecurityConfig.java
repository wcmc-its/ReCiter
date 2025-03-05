package reciter.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class APISecurityConfig {
    
	/**
	 * @author szd2013
	 * This will intercept and request for admin api and authenticate its api key
	 */
	
    	
	    private final String principalRequestHeader = "api-key";
	    private final APIKeyAuthProvider authProvider;

	    
	    @Value("${spring.security.enabled}")
	    private boolean securityEnabled;
	    
	    public APISecurityConfig(APIKeyAuthProvider authProvider) {
	        this.authProvider = authProvider;
	    }
	    
	    @Bean
	    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
	        APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader, authProvider);

	        if (securityEnabled) {
	            http
	                .securityMatcher("/reciter/**")
	                .csrf(csrf -> csrf.disable())
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .authorizeHttpRequests(auth -> auth
	                    .requestMatchers("/reciter/**").authenticated()
	                    .requestMatchers("/reciter/ping").permitAll()
	                )
	                .addFilterBefore(filter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
	        } else {
	            http
	                .securityMatcher("/reciter/**")
	                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
	        }

	        return http.build();
	    }
    
	    @Bean
	    public SecurityFilterChain consumerSecurityFilterChain(HttpSecurity http) throws Exception {
	        APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader, authProvider);

	        if (securityEnabled) {
	            http
	                .securityMatcher("/reciter/article-retrieval/**")
	                .csrf(csrf -> csrf.disable())
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .authorizeHttpRequests(auth -> auth
	                    .requestMatchers("/reciter/article-retrieval/**").authenticated()
	                )
	                .addFilterBefore(filter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
	        } else {
	            http
	                .securityMatcher("/reciter/article-retrieval/**")
	                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
	        }

	        return http.build();
	    }

	
}
