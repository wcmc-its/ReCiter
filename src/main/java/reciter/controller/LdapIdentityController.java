package reciter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import reciter.Uids;
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
	
	@RequestMapping(value = "/reciter/ldap/get/identity/by/uid", method = RequestMethod.GET)
	@ResponseBody
	public Identity getIdentity(@RequestParam String uid) {
		Identity identity = ldapIdentityService.getIdentity(uid);
		if (identity != null) {
			identityService.save(identity);
		} else {
			slf4jLogger.info("uid doesn't exist: " + uid);
		}
		return identity;
	}
	
	/**
	 * Retrieve identity information for all uids in Uids.java
	 * @return
	 */
	@RequestMapping(value = "/reciter/ldap/retrieve/test", method = RequestMethod.GET)
	@ResponseBody
	public String retrieveTest() {
		long start = System.currentTimeMillis();
		slf4jLogger.info("Started retrieval at: " + start);
		for (String uid : Uids.uids) {
			slf4jLogger.info("Starting retrieval for : " + uid);
			Identity identity = ldapIdentityService.getIdentity(uid);
			if (identity == null) {
				slf4jLogger.info("uid doesn't exist: " + uid);
			} else {
				slf4jLogger.info("Finished retrieval for : " + identity.getUid());
			}
			identityService.save(identity);
		}
		long end = System.currentTimeMillis();
		slf4jLogger.info("End retrieval at: " + end + ". Time elapsed=[" + (end - start) + "] ms");
		return "OK";
	}
	
}
