package reciter.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

public class ApiKeyRequestMatcher implements RequestMatcher {

	@Override
    public boolean matches(HttpServletRequest request) {
		System.out.println("Checking for api-key in request");
        return request.getHeader("api-key") != null;
    }

}
