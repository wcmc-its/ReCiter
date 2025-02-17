package reciter.security;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

@Component
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

	private static final String STR_USER_POOL_ID = "userPoolID";	
	
	@Autowired
    private AwsSecretsManagerService awsSecretsManagerService; // Inject the service to get the secret

	@Autowired
	private S3UserLogHandler s3UserLogHandler;

	@Value("${aws.congito.userpool.region}")
    private String cogintoRegion;
	    
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		System.out.println("MyCustomFilter is called************************");
		// Example: Log the request method and URI
		String method = request.getMethod();
		String uri = request.getRequestURI();
		System.out.println("Request Method: " + method + " | Request URI: " + uri);

		String token = extractToken(request);
		System.out.println("token**************" + token);

		if (StringUtils.hasText(token)) {
			try {
				
				// Verify the token
				DecodedJWT decodedJWT = verifyJWT(token);

				
				String clientId = decodedJWT.getClaim("client_id").asString();
				
				System.out.println("Client Id : " + clientId);
				
				JsonNode secretsJson = getClientSecretsFromSecretsManager(clientId);
				
				System.out.println("secretsJson pretty : " + secretsJson.toPrettyString());
				
				System.out.println("cogintoRegion : " + cogintoRegion);
				
				System.out.println("Node Type: " + secretsJson.getNodeType());
				
				System.out.println("Issuer: " + JWT.decode(token).getIssuer());
				
				try
				{
					// Step 1: Convert the string to JsonNode using Jackson's ObjectMapper
		            ObjectMapper objectMapper = new ObjectMapper();
		            secretsJson = objectMapper.readTree(secretsJson.asText());
		
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				String clientName = secretsJson.get("clientName").asText();
				
				// Create an authentication object
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						decodedJWT.getSubject(), null, null);

				// Set authentication details
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// Set the authentication in the security context
				SecurityContextHolder.getContext().setAuthentication(authentication);
				
				System.out.println("Write the userlog details to S3 bucket here*************************" + request.getRequestURI() +" - "+ request.getParameter("uid"));
				
				
				// Create a new UserLog entry
                UserLog userLog = new UserLog(clientId, clientName,request.getRequestURI(),request.getParameter("uid"),LocalDateTime.now().toString());
                System.out.println("Details of the userLog*******************"+userLog.toString());
                // Get the current date as a string in yyyy-MM-dd format
                String date = Instant.now().toString().split("T")[0];
                
                // Write the user log entry to the S3 bucket
                s3UserLogHandler.writeUserLog(userLog, date);

			} catch (JWTVerificationException e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				String jsonResponse = "{"
				        + "\"status\": 401,"
				        + "\"error\": \"Unauthorized\","
				        + "\"message\": \"Invalid token. Please regenerate the token again.\""
				        + "}";
				response.getWriter().write(jsonResponse);
				return;
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				String jsonResponse = "{"
				        + "\"status\": 401,"
				        + "\"error\": \"Unauthorized\","
				        + "\"message\": " + e.getMessage()
				        + "}";
				response.getWriter().write(jsonResponse);
				return;
			}
		}

		filterChain.doFilter(request, response);
		// Custom logic after the response has been processed by all filters
		// (post-processing)
		System.out.println("Response Status: " + response.getStatus());
	}

	// Extract token from Authorization header
	private String extractToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		System.out.println("header received from client**************" + header);
		if (header != null && header.startsWith("Bearer ")) {
			System.out.println("header received from client**************" + header);
			return header.substring(7); // Remove "Bearer " prefix
		}
		return null;
	}

	// Verify JWT token using the public key from JWK set
	private DecodedJWT verifyJWT(String token) throws Exception {
		
		try
		{
			String clientId = JWT.decode(token).getClaim("client_id").asString();
			
			System.out.println("Client Id : " + clientId);
			
			JsonNode secretsJson = getClientSecretsFromSecretsManager(clientId);
			
			System.out.println("secretsJson pretty : " + secretsJson.toPrettyString());
			
			System.out.println("cogintoRegion : " + cogintoRegion);
			
			System.out.println("Node Type: " + secretsJson.getNodeType());
			
			System.out.println("Issuer: " + JWT.decode(token).getIssuer());
			
			try
			{
				// Step 1: Convert the string to JsonNode using Jackson's ObjectMapper
	            ObjectMapper objectMapper = new ObjectMapper();
	            secretsJson = objectMapper.readTree(secretsJson.asText());
	
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			 // Get an iterator for the field names (keys) in the JsonNode
	        Iterator<String> fieldNames = secretsJson.fieldNames();
	
	        // Print all field names (keys)
	        while (fieldNames.hasNext()) {
	            String fieldName = fieldNames.next();
	            System.out.println("Key: " + fieldName);
	        }
	        
	        System.out.println("STR_USER_POOL_ID1tr : " + secretsJson.get(STR_USER_POOL_ID).getNodeType());
			
	        
			System.out.println("STR_USER_POOL_ID2 : " + ((JsonNode)secretsJson.get(STR_USER_POOL_ID)).asText());
			
			String tokenSignInUrl = getTokenSigningKeyUrl(cogintoRegion,secretsJson.get(STR_USER_POOL_ID).asText());
			
			String issuer = getIssuerFromToken(cogintoRegion,secretsJson.get(STR_USER_POOL_ID).asText());
						
			System.out.println("tokenSignInUrl : " + tokenSignInUrl);
			
			RSAPublicKey publicKey = getPublicKeyFromJWKSet(token,tokenSignInUrl);
	
			JWTVerifier verifier = JWT.require(Algorithm.RSA256(publicKey, null))
					.withIssuer(issuer).build();
			System.out.println("Verifier : " +  verifier);
			
			return verifier.verify(token);
		}
		catch (JWTVerificationException jwtve) {
            System.out.println("Invalid JWT Token: " + jwtve.getMessage());
            throw jwtve;
        }
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	// Fetch the public key from Cognito's JWK set
	private RSAPublicKey getPublicKeyFromJWKSet(String token,String tokenSignInUrl) throws Exception {
		String jwks = fetchJWKSet(tokenSignInUrl);
		System.out.println("jwks are**********************"+jwks);
		JWKSet jwkSet = JWKSet.parse(jwks);

		String kid = JWT.decode(token).getKeyId(); // Get the key ID from the JWT header
		System.out.println("kid**********************"+kid);
		JWK jwk = jwkSet.getKeyByKeyId(kid);
		
		if (jwk != null && jwk instanceof RSAKey) {
			RSAKey rsaKey = (RSAKey) jwk;
			System.out.println("RSAKey**********************"+rsaKey.toRSAPublicKey());
			return rsaKey.toRSAPublicKey(); // Return the public RSA key
		} else {
			throw new Exception("Unable to find matching key ID in JWK Set");
		}
	}

	// Fetch the JWKS (JSON Web Key Set) from the Cognito endpoint
	private String fetchJWKSet(String tokenSignInUrl) {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.getForObject(tokenSignInUrl, String.class); // Fetch JWKS
	}

	// Fetch the Issuer URL from Secrets Manager
    private JsonNode getClientSecretsFromSecretsManager(String clientId) {
    	JsonNode secretValueJson = awsSecretsManagerService.getClientSecret(clientId); 
        if (secretValueJson != null) {
            return secretValueJson; 
        }
        return null;
    }
    public static String getTokenSigningKeyUrl(String region, String userPoolId) {
        // Construct the JWKS URL for the given region and user pool ID
        return "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId + "/.well-known/jwks.json";
    }
    public static String getIssuerFromToken(String region, String userPoolId) {
        // Construct the JWKS URL for the given region and user pool ID
        return "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId;
    }
}
