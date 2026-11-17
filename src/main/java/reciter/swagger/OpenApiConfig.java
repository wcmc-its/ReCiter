package reciter.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    servers = {
        // This is the fix: Setting the URL to the root context path forces 
        // the browser to resolve the full URL using the current page's scheme (HTTPS).
        @Server(url = "/", description = "Relative context path (Forces HTTPS)") 
    },
    // Either credential satisfies MultiApiKeyFilter / the JWT resource server.
    security = { @SecurityRequirement(name = "api-key"), @SecurityRequirement(name = "bearer-jwt") }
)
@SecurityScheme(name = "api-key", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "api-key")
@SecurityScheme(name = "bearer-jwt", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {}
