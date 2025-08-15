package reciter.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author mjangari 
 * This will intercept and request for consumer api JWT token
 * and admin api key and authenticate its JWT token or api-key
 */

@EnableWebSecurity
@Configuration
public class APISecurityConfig extends WebSecurityConfigurerAdapter {

	private JwtTokenAuthenticationFilter filter = null;
	private static final Logger log = LoggerFactory.getLogger(APISecurityConfig.class);

	@Value("${security.enabled:true}")
	private boolean securityEnabled;

	@Autowired
	public APISecurityConfig(JwtTokenAuthenticationFilter filter) {
		this.filter = filter;
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		log.info("*************Execuring Configure method***************");
		if (securityEnabled) {
			httpSecurity.antMatcher("/reciter/**").csrf().disable().sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
					.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).authorizeRequests()
					.antMatchers("/reciter/generate-access-token").permitAll()
					.anyRequest().authenticated();
		}

	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		if (!securityEnabled) {
			web.ignoring().antMatchers("/reciter/**");
		}
		// Added to whitelist ping controller and Access Token
		web.ignoring().antMatchers("/reciter/ping");
		
	}
}
