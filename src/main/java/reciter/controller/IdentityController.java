/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
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
import reciter.service.IdentityService;

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
	
	@RequestMapping(value = "/reciter/find/identity/by/uids/", method = RequestMethod.GET)
	@ResponseBody
	public List<Identity> findByUids(@RequestParam List<String> uids) {
		slf4jLogger.info("calling findByUid with size of uids=" + uids);
		return identityService.findByUids(uids);
	}
}
