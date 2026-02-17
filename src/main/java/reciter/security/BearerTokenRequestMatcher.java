package reciter.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class BearerTokenRequestMatcher implements RequestMatcher {

	@Override
    public boolean matches(HttpServletRequest request) {
		log.info("Checking for Authorization in request");
		String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

}
