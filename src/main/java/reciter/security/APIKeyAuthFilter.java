package reciter.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class APIKeyAuthFilter  extends AbstractPreAuthenticatedProcessingFilter {
	
	private String principalRequestHeader;
	private String authorizationHeader;

    public APIKeyAuthFilter(String principalRequestHeader, String authorizationHeader) {
        this.principalRequestHeader = principalRequestHeader;
        this.authorizationHeader = authorizationHeader;
    }

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		//return request.getHeader(principalRequestHeader);
		String authHeader = request.getHeader(authorizationHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // You can choose to return the token or a decoded user ID (e.g., from gateway)
            return token;// Or extract user info if needed
        }

        // Fallback to API Key
        String apiKey = request.getHeader(principalRequestHeader);
        if (apiKey != null) {
            return apiKey;
        }

        return null;
	}

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return "N/A";
	}

}
