package reciter.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
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

	@Autowired(required = false)
    private JwtTokenAuthenticationFilter jwtAuthenticationFilter;
	 
	@Autowired(required = false)
    private CustomAuthenticationEntryPoint customEntryPoint;

	private static final Logger log = LoggerFactory.getLogger(APISecurityConfig.class);

	@Value("${spring.security.enabled}")
	private boolean securityEnabled;

	@Bean
	public JwtTokenAuthenticationFilter jwtAuthenticationFilter() {
	   log.info("JWT filter bean is being created!");
	   return new JwtTokenAuthenticationFilter();
	}
	
	/*@Bean
    	@ConditionalOnProperty(name = "spring.security.enabled", havingValue = "true")
    	public FilterRegistrationBean<JwtTokenAuthenticationFilter> jwtFilterRegistration(JwtTokenAuthenticationFilter filter) 
	{
        	FilterRegistrationBean<JwtTokenAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        	registration.setEnabled(false);
       		 return registration;
    	}*/
	

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		log.info("*************Execuring Configure method***************");
		/*if (!securityEnabled) { // allow when security is off
			httpSecurity.antMatcher("/reciter/**").csrf().disable()
	            .authorizeRequests()
	            .anyRequest().permitAll();
	        return;
	    }*/
		/*httpSecurity.antMatcher("/reciter/**").csrf().disable().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		
		// Only set exceptionHandling if the entry point is available
       // if (customEntryPoint != null) {
        	httpSecurity.exceptionHandling().authenticationEntryPoint(customEntryPoint)
                .and();
        //}
	        httpSecurity.authorizeRequests()
					.anyRequest().authenticated();
		
		 //if (jwtAuthenticationFilter != null) {
			// httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	      //  }*/
		
		httpSecurity.antMatcher("/reciter/**")
         .csrf().disable()
         .exceptionHandling()
             .authenticationEntryPoint(customEntryPoint)
         .and()
         .sessionManagement()
             .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
         .and()
         .authorizeRequests()
             .anyRequest().authenticated();

		httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
