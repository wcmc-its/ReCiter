package reciter.security;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;
public class BearerTokenRequestMatcher implements RequestMatcher {

	private static final Logger log = LoggerFactory.getLogger(BearerTokenRequestMatcher.class);	
	@Override
    public boolean matches(HttpServletRequest request) {
		log.info("Checking for Authorization in request");
		String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

}
