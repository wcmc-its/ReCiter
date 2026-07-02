package reciter.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

/**
 * @author mjangari This will intercept and request for consumer api JWT token
 *         and admin api key and authenticate its JWT token or api-key
 */

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class APISecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(APISecurityConfig.class);

	@Value("${spring.security.enabled}")
	private boolean securityEnabled;

	private final CustomAuthenticationEntryPoint customEntryPoint;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		log.info("*************Executing Configure method***************");

		if (!securityEnabled) {
			// SECURITY DISABLED MODE
			return httpSecurity.csrf(csrf -> csrf.disable())
					.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
		}

		return httpSecurity.securityMatcher("/reciter/**").csrf(csrf -> csrf.disable())
				.exceptionHandling(exception -> exception.authenticationEntryPoint(customEntryPoint))
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/reciter/ping", "/reciter/v3/api-docs/swagger-config",
								"/reciter/v3/api-docs/reciter-group").permitAll().anyRequest().authenticated())
				.build();
	}
}