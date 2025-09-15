package reciter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reciter.security.AwsSecretsManagerService;
import reciter.service.aws.CognitoTokenService;

@RestController
@RequestMapping("/reciter")
public class CognitoAccessTokenController {

	private static final Logger log = LoggerFactory.getLogger(CognitoAccessTokenController.class);
	
	@Autowired
    private AwsSecretsManagerService awsSecretsManagerService; 
	
	@Autowired
    private CognitoTokenService cognitoTokenService; 
	
	@CrossOrigin(origins = "https://reciter-consumer.weill.cornell.edu",
				 methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    @GetMapping("/generate-access-token")
    public ResponseEntity<String> generateAccessToken(@RequestParam(value = "clientName") String clientName) {
       
    	if(clientName == null || clientName.equalsIgnoreCase(""))
    	{
    		 return ResponseEntity
                     .status(HttpStatus.BAD_REQUEST)  // Set status to 400 (Bad Request)
                     .body("The 'clientName' is required and cannot be empty.");
    	}
    	ResponseEntity<String> tokenResponse = cognitoTokenService.getCognitoAccessToken(clientName);
 		return tokenResponse;
    }
}
