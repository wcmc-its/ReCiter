package reciter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import reciter.security.AwsSecretsManagerService;
import reciter.service.aws.CognitoTokenService;

@Controller
@RequestMapping("/reciter")
public class CognitoAccessTokenController {

	private static final Logger log = LoggerFactory.getLogger(CognitoAccessTokenController.class);
	
	@Autowired
    private AwsSecretsManagerService awsSecretsManagerService; 
	
	@Autowired
    private CognitoTokenService cognitoTokenService; 
	
	
    @GetMapping("/generate-access-token")
    @ResponseBody
    public ResponseEntity<String> generateAccessToken(@RequestParam(value = "clientName") String clientName) {
       
    	ResponseEntity<String> tokenResponse = cognitoTokenService.getCognitoAccessToken(clientName);
    	System.out.println("Token Response"+tokenResponse);
		return tokenResponse;
    }
}
