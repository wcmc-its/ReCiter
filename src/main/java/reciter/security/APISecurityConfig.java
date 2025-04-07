package reciter.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class APISecurityConfig {

    /**
     * This will intercept and request for admin api and authenticate its api key
     */
    @Configuration
    public static class AdminApiSecurityConfig {
        
        private final String principalRequestHeader = "api-key";
        private String principalRequestValue = System.getenv("ADMIN_API_KEY");
        
        @Value("${spring.security.enabled}")
        private boolean securityEnabled;
        
        @Bean
        @Order(2)
        public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
            if (!securityEnabled) {
                http.csrf(csrf -> csrf.disable())
                    .securityMatcher("/reciter/**")
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
                return http.build();
            }
            
            APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader);
            filter.setAuthenticationManager(authentication -> {
                String principal = (String) authentication.getPrincipal();
                if (principal == null || !principalRequestValue.equals(principal)) {
                    throw new BadCredentialsException("The API key was not found or not the expected value.");
                }
                authentication.setAuthenticated(true);
                return authentication;
            });
            
            http.csrf(csrf -> csrf.disable())
                .securityMatcher("/reciter/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/reciter/ping").permitAll()
                    .anyRequest().authenticated()
                );
                
            return http.build();
        }
    }
    
    /**
     * This will intercept and request for consumer api and authenticate its api key
     */
    @Configuration
    public static class ConsumerApiSecurityConfig {
        
        private final String principalRequestHeader = "api-key";
        private String principalRequestValue = System.getenv("CONSUMER_API_KEY");
        
        @Value("${security.enabled:true}")
        private boolean securityEnabled;
        
        @Bean
        @Order(1)
        public SecurityFilterChain consumerFilterChain(HttpSecurity http) throws Exception {
            if (!securityEnabled) {
                http.csrf(csrf -> csrf.disable())
                    .securityMatcher("/reciter/article-retrieval/**")
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
                return http.build();
            }
            
            APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader);
            filter.setAuthenticationManager(authentication -> {
                String principal = (String) authentication.getPrincipal();
                if (principal == null || !principalRequestValue.equals(principal)) {
                    throw new BadCredentialsException("The API key was not found or not the expected value.");
                }
                authentication.setAuthenticated(true);
                return authentication;
            });
            
            http.csrf(csrf -> csrf.disable())
                .securityMatcher("/reciter/article-retrieval/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
                
            return http.build();
        }
    }
}