package reciter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.ApplicationUser;
import reciter.database.dynamodb.model.UserFeedback;
import reciter.service.ApplicationUserService;
import reciter.service.UserFeedbackService;

@Api(value = "ReCiterPubManagerController", description = "Operations on ReCiter publication manager.")
@Slf4j
@Controller
public class ReCiterPubManagerController {
	
	@Autowired
	private ApplicationUserService applicationUserService;
	
	@Autowired
	private UserFeedbackService userFeedbackService;
	
	@ApiOperation(value = "Authenticate user for ReCiter publications manager", response = Boolean.class, notes = "This api checks for credentials for access to reciter publication manager app.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User authenticated"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/publication/manager/authenticate", method = RequestMethod.POST, produces = "application/json")
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
	
	@ApiOperation(value = "Create user for ReCiter publications manager", response = Boolean.class, notes = "This api create user for reciter publication manager app.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User created"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/publication/manager/user/create", method = RequestMethod.POST, produces = "application/json")
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
	
	@ApiOperation(value = "Save user feedback for ReCiter publications manager", response = Boolean.class, notes = "This api create saves user feedback for reciter publication manager app.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User feedback created"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/publication/manager/userfeedback/save", method = RequestMethod.POST, produces = "application/json")
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
	
	@ApiOperation(value = "Delete user feedback for ReCiter publications manager", response = Boolean.class, notes = "This api create deletes user feedback for reciter publication manager app.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User deleted"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/publication/manager/userfeedback/delete", method = RequestMethod.GET, produces = "application/json")
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
	
	@ApiOperation(value = "Find user feedback for ReCiter publications manager", response = Boolean.class, notes = "This api create find user feedback for reciter publication manager app.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User deleted"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/publication/manager/userfeedback/find", method = RequestMethod.GET, produces = "application/json")
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
