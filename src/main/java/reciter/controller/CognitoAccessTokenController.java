package reciter.controller;

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

	@Autowired
    private AwsSecretsManagerService awsSecretsManagerService; 
	
	@Autowired
    private CognitoTokenService cognitoTokenService; 
	
	
    @GetMapping("/getAccessToken")
    @ResponseBody
    public ResponseEntity<String> generateAccessToken(@RequestParam(value = "clientName") String clientName) {
       
    	 ResponseEntity<String> tokenResponse = cognitoTokenService.getCognitoAccessToken(clientName);
		return tokenResponse;
    }
}
