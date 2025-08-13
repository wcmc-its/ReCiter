package reciter.security;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
public class APISecurityConfig {

    @Configuration
    @Order(1)
    public static class ConsumerApiSecurityConfig extends WebSecurityConfigurerAdapter {

        private final JwtTokenAuthenticationFilter filter;

        @Value("${security.enabled:true}")
        private boolean securityEnabled;

        @Autowired
        public ConsumerApiSecurityConfig(JwtTokenAuthenticationFilter filter) {
            this.filter = filter;
        }

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            if (securityEnabled) {
                System.out.println("Consumer JWT security enabled");

                httpSecurity
                    .antMatcher("/reciter/article-retrieval/**")
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                    .authorizeRequests()
                        .anyRequest().authenticated();
            }
        }
    }

    @Configuration
    @Order(2)
    public static class AdminApiSecurityConfig extends WebSecurityConfigurerAdapter {

        private final String principalRequestHeader = "api-key";
        private final String principalRequestValue = System.getenv("ADMIN_API_KEY");

        @Value("${spring.security.enabled}")
        private boolean securityEnabled;

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader);
            filter.setAuthenticationManager(authentication -> {
                String principal = (String) authentication.getPrincipal();
                if (!principalRequestValue.equals(principal)) {
                    throw new BadCredentialsException("API key is invalid.");
                }
                authentication.setAuthenticated(true);
                return authentication;
            });

            if (securityEnabled) {
                System.out.println("Admin API key security enabled");

                httpSecurity
                    .antMatcher("/reciter/**")
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                    .authorizeRequests()
                        .anyRequest().authenticated();
            }
        }
    }


	
	    @PostConstruct
	    public void init() {
	        System.out.println(">>>>> APISecurityConfig initialized <<<<<");
	    }
	
}
