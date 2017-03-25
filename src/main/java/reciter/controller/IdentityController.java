package reciter.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.model.identity.Identity;
import reciter.service.mongo.IdentityService;

@Controller
public class IdentityController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(IdentityController.class);

	@Autowired
	private IdentityService identityService;
	
	@RequestMapping(value = "/reciter/save/identities/", method = RequestMethod.PUT)
	@ResponseBody
	public void saveIdentities(@RequestBody List<Identity> identities) {
		slf4jLogger.info("calling saveIdentities with number of identities=" + identities.size());
		identityService.save(identities);
	}
	
	@RequestMapping(value = "/reciter/find/identity/by/cwids/", method = RequestMethod.GET)
	@ResponseBody
	public List<Identity> findByCwids(@RequestParam List<String> cwids) {
		slf4jLogger.info("calling findByCwids with size of cwids=" + cwids);
		return identityService.findByCwids(cwids);
	}
}
