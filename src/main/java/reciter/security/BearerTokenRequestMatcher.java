package reciter.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

public class BearerTokenRequestMatcher implements RequestMatcher {

	@Override
    public boolean matches(HttpServletRequest request) {
		System.out.println("Checking for Authorization in request");
		String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

}
