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

import reciter.model.scopus.ScopusArticle;
import reciter.service.mongo.ScopusService;

@Controller
public class ScopusController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ScopusController.class);

	@Autowired
	private ScopusService scopusService;
	
	@RequestMapping(value = "/reciter/save/scopus/articles/", method = RequestMethod.PUT)
	@ResponseBody
	public void savePubMedArticles(@RequestBody List<ScopusArticle> scopusArticles) {
		slf4jLogger.info("calling savePubMedArticles with number of Scopus articles=" + scopusArticles.size());
		scopusService.save(scopusArticles);
	}
	
	@RequestMapping(value = "/reciter/find/scopus/articles/pmids/", method = RequestMethod.GET)
	@ResponseBody
	public List<ScopusArticle> findByPmids(@RequestParam List<Long> pmids) {
		slf4jLogger.info("calling findByPmids with size of pmids=" + pmids);
		return scopusService.findByPmids(pmids);
	}
}
