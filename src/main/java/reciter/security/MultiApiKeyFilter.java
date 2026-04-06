package reciter.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author mjangari
 * Validates the api-key received from the reciter-consumer and reciter respectively. 
 */
@Component
public class MultiApiKeyFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(MultiApiKeyFilter.class);
	
	private String adminPrincipalRequestValue = System.getenv("ADMIN_API_KEY");
	    
	private String consumerPrincipalRequestValue = System.getenv("CONSUMER_API_KEY");
		
	@Value("${spring.security.enabled}")
	private boolean securityEnabled;

	@Autowired
    private CustomAuthenticationEntryPoint authenticationEntryPoint;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException 
	{

		
		if (!securityEnabled) {
            // Security off: just continue
            filterChain.doFilter(request, response);
            return;
        }
		
        String path = request.getRequestURI();
        try
		{
	        /*
	         * Fall back for consumer users until migration to JWT tokens is complete.
	         * TODO: Once all existing users have been migrated, this conditional code will be removed.
	         */
	        if(((path.startsWith("/reciter/article-retrieval/") || path.startsWith("/reciter/dev/article-retrieval/"))))
			{
				Optional.ofNullable(request.getHeader("api-key"))
		        .filter(key -> !key.isEmpty())
		        .ifPresent(apiKey -> {
		        	 if (!apiKey.equals(consumerPrincipalRequestValue)) {
		        		 throw new BadCredentialsException("Invalid API key");
		                }
		                UsernamePasswordAuthenticationToken auth =
		                        new UsernamePasswordAuthenticationToken("reciter-consumer-user", null, Collections.emptyList());
		                SecurityContextHolder.getContext().setAuthentication(auth);
	
		        });
			}
			else if(path.startsWith("/reciter/"))
			{
				Optional.ofNullable(request.getHeader("api-key"))
		        .filter(key -> !key.isEmpty())
		        .ifPresent(apiKey -> {
		        	 if (!apiKey.equals(adminPrincipalRequestValue)) {
		        		 throw new BadCredentialsException("Invalid API key");
		                }
		                UsernamePasswordAuthenticationToken auth =
		                        new UsernamePasswordAuthenticationToken("reciter-internal-user", null, Collections.emptyList());
		                SecurityContextHolder.getContext().setAuthentication(auth);
	
		        });
			}
		}
	    catch (AuthenticationException ex) {
			authenticationEntryPoint.commence(request, response, ex);
			return;
	    }

		filterChain.doFilter(request, response);
		
	}

}
