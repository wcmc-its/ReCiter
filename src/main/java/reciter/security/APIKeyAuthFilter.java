package reciter.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class APIKeyAuthFilter extends OncePerRequestFilter {

    private final String principalRequestHeader;
    private final APIKeyAuthProvider authProvider;

    public APIKeyAuthFilter(String principalRequestHeader, APIKeyAuthProvider authProvider) {
        this.principalRequestHeader = principalRequestHeader;
        this.authProvider = authProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(principalRequestHeader);

        if (apiKey != null) {
            //  Wrap API Key in an Authentication Object
            Authentication authRequest = new UsernamePasswordAuthenticationToken(apiKey, null);
            
            //  Pass Authentication Object to Provider
            Authentication authentication = authProvider.authenticate(authRequest);

            //  Set Authentication in Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
