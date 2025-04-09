package reciter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.ApplicationUser;
import reciter.database.dynamodb.model.UserFeedback;
import reciter.service.ApplicationUserService;
import reciter.service.UserFeedbackService;

@Tag(name = "ReCiterPubManagerController", description = "Operations on ReCiter publication manager.")
@Slf4j
@Controller
public class ReCiterPubManagerController {
	
	@Autowired
	private ApplicationUserService applicationUserService;
	
	@Autowired
	private UserFeedbackService userFeedbackService;
	
	@Operation(summary= "Authenticate user for ReCiter publications manager", description  = "This api checks for credentials for access to reciter publication manager app.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @PostMapping(value = "/reciter/publication/manager/authenticate", produces = "application/json")
    @ResponseBody
    public boolean authenticate(@RequestParam(value = "username") String uid, @RequestParam(value = "password") String password) {
        StopWatch stopWatch = new StopWatch("Authenticate user for ReCiter publications manager");
        stopWatch.start("Authenticate user for ReCiter publications manager");

        log.info("Authenticating user with username: " + uid);
        
        ApplicationUser appUser = new ApplicationUser(uid, "", password);
        if(applicationUserService.authenticateUser(appUser)) {
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        	return true;
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return false;
    }
	
	@Operation(summary  = "Create user for ReCiter publications manager", description  = "This api create user for reciter publication manager app.")
    @Parameters({
    	@Parameter(name = "api-key", description =  "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @PostMapping(value = "/reciter/publication/manager/user/create", produces = "application/json")
    @ResponseBody
    public boolean createUser(@RequestParam(value = "username") String uid, @RequestParam(value = "name") String username, @RequestParam(value = "password") String password) {
        StopWatch stopWatch = new StopWatch("Create user for ReCiter publications manager");
        stopWatch.start("Create user for ReCiter publications manager");
        log.info("Creating user with username: " + uid);
        
        ApplicationUser appUser = new ApplicationUser(uid, username, password);
        if(applicationUserService.createUser(appUser)) {
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        	return true;
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return false;
    }
	
	@Operation(summary = "Save user feedback for ReCiter publications manager",description = "This api create saves user feedback for reciter publication manager app.")
    @Parameters({
    	@Parameter(name = "api-key", description  = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User feedback created"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @PostMapping(value = "/reciter/publication/manager/userfeedback/save", produces = "application/json")
    @ResponseBody
    public ResponseEntity saveUserFeedback(@RequestBody UserFeedback userFeedback) {
        StopWatch stopWatch = new StopWatch("Save user feedback for ReCiter publications manager");
        stopWatch.start("Save user feedback for ReCiter publications manager");
        log.info("Adding a user feedback for " + userFeedback.getUid());
        if(userFeedback == null) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The api requires a UserFeedback model");
    	} else if(userFeedback != null && userFeedback.getUid() == null) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The api requires a valid uid to be passed with UserFeedback model");
    	}
        userFeedbackService.save(userFeedback);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(userFeedback);
    }
	
	@Operation(summary  = "Delete user feedback for ReCiter publications manager", description  = "This api create deletes user feedback for reciter publication manager app.")
    @Parameters({
    	@Parameter(name = "api-key", description  = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/publication/manager/userfeedback/delete", produces = "application/json")
    @ResponseBody
    public ResponseEntity deleteUserFeedback(@RequestParam String uid) {
        StopWatch stopWatch = new StopWatch("Delete user feedback for ReCiter publications manager");
        stopWatch.start("Delete user feedback for ReCiter publications manager");
        if(userFeedbackService.findByUid(uid) == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Uid " + uid + " not found");
    	}
        userFeedbackService.delete(uid);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok("The user feedback of " + uid + " is deleted");
    }
	
	@Operation(summary = "Find user feedback for ReCiter publications manager",description  = "This api create find user feedback for reciter publication manager app.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/publication/manager/userfeedback/find", produces = "application/json")
    @ResponseBody
    public ResponseEntity findUserFeedback(@RequestParam String uid) {
        StopWatch stopWatch = new StopWatch("Find user feedback for ReCiter publications manager");
        stopWatch.start("Find user feedback for ReCiter publications manager");
        if(userFeedbackService.findByUid(uid) == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Uid " + uid + " not found");
    	}
        UserFeedback userFeedback = userFeedbackService.findByUid(uid);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(userFeedback);
    }
}
