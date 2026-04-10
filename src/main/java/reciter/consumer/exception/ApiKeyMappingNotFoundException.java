package reciter.consumer.exception;

import org.springframework.security.core.AuthenticationException;

public class ApiKeyMappingNotFoundException extends RuntimeException  {

	public ApiKeyMappingNotFoundException(String message) {
	    super(message);
	}
	
}
