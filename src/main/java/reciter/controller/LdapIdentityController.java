package reciter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.Cwids;
import reciter.model.identity.Identity;
import reciter.service.ldap.LdapIdentityService;
import reciter.service.mongo.IdentityService;

@Controller
public class LdapIdentityController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(LdapIdentityController.class);

	@Autowired
	private LdapIdentityService ldapIdentityService;
	
	@Autowired
	private IdentityService identityService;
	
	@RequestMapping(value = "/reciter/ldap/get/identity/by/cwid", method = RequestMethod.GET)
	@ResponseBody
	public Identity getIdentity(@RequestParam String cwid) {
		return ldapIdentityService.getIdentity(cwid);
	}
	
	@RequestMapping(value = "/reciter/ldap/retrieve/test", method = RequestMethod.GET)
	@ResponseBody
	public String retrieveTest() {
		long start = System.currentTimeMillis();
		slf4jLogger.info("Started retrieval at: " + start);
		for (String cwid : Cwids.cwids) {
			slf4jLogger.info("Starting retrieval for : " + cwid);
			Identity identity = ldapIdentityService.getIdentity(cwid);
			if (identity == null) {
				slf4jLogger.info("Cwid doesn't exist: " + cwid);
			} else {
				slf4jLogger.info("Finished retrieval for : " + identity.getCwid());
			}
			identityService.save(identity);
		}
		long end = System.currentTimeMillis();
		slf4jLogger.info("End retrieval at: " + end + ". Time elapsed=[" + (end - start) + "] ms");
		return "OK";
	}
	
}
