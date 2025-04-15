package reciter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
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
import reciter.service.ApplicationUserService;


@Api(value = "ReCiterPubManagerController", description = "Operations on ReCiter publication manager.")
@Slf4j
@Controller
public class ReCiterPubManagerController {
	
	@Autowired
	private ApplicationUserService applicationUserService;
	
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
}
