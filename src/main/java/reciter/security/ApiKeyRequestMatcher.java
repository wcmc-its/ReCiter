package reciter.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiKeyRequestMatcher implements RequestMatcher {

	@Override
    public boolean matches(HttpServletRequest request) {
		log.info("Checking for api-key in request");
        return request.getHeader("api-key") != null;
    }

}
