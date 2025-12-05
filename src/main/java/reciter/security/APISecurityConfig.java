package reciter.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author mjangari This will intercept and request for consumer api JWT token
 * and admin api key and authenticate its JWT token or api-key
 */

@EnableWebSecurity
@Configuration
public class APISecurityConfig {
	
	private static final Logger log = LoggerFactory.getLogger(APISecurityConfig.class);
    
	@Autowired(required = false)
    private JwtTokenAuthenticationFilter jwtAuthenticationFilter;

    @Value("${spring.security.enabled}")
    private boolean securityEnabled;
    
    @Autowired(required = false)
    private CustomAuthenticationEntryPoint customEntryPoint;

	@Bean
	public JwtTokenAuthenticationFilter jwtAuthenticationFilter() {
	   log.info("JWT filter bean is being created!");
	   return new JwtTokenAuthenticationFilter();
	}
	
	 @Bean
	    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
	        log.info("*************Executing Configure method***************");
	        
	        return httpSecurity
	            .securityMatcher("/reciter/**")
	            .csrf(csrf -> csrf.disable())
	            .exceptionHandling(exception -> 
	                exception.authenticationEntryPoint(customEntryPoint))
	            .sessionManagement(session -> 
	                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	            .authorizeHttpRequests(auth -> 
	                auth.anyRequest().authenticated())
	            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
	            .build();
	    }
	 
	 @Bean
	    public WebSecurityCustomizer webSecurityCustomizer() {
	        return (web) -> {
	            if (!securityEnabled) {
	                web.ignoring().requestMatchers("/reciter/**");
	            }
	            // Added to whitelist ping controller and Access Token
	            web.ignoring().requestMatchers("/reciter/ping","/reciter/v3/api-docs/swagger-config","reciter/v3/api-docs/reciter-group");
	        };
	    }
}