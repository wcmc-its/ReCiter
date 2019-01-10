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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reciter.model.scopus.ScopusArticle;
import reciter.scopus.retriever.ScopusArticleRetriever;
import reciter.service.ScopusService;

@Controller
public class ScopusController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ScopusController.class);

	@Autowired
	private ScopusService scopusService;
	
	/*@ApiOperation(value = "Add list of scopus articles to ScopusArticle table in DynamoDb", notes = "This api saves list of scopusArticles in the ScopusArticle table in dynamoDb.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ScopusArticle List creation successful"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
			})
	@RequestMapping(value = "/reciter/save/scopus/articles/", method = RequestMethod.PUT)
	@ResponseBody
	public void savePubMedArticles(@RequestBody List<ScopusArticle> scopusArticles) {
		slf4jLogger.info("calling savePubMedArticles with number of Scopus articles=" + scopusArticles.size());
		scopusService.save(scopusArticles);
	}
	
	@ApiOperation(value = "Find the relevant scopus article for a supplied pubmedId", notes = "This api finds the relevant scopus article for a supplied pubmedId.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ScopusArticle List retrieval successful"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
			})
	@RequestMapping(value = "/reciter/scopus/id", method = RequestMethod.GET)
	@ResponseBody
	public ScopusArticle scopusArticle(@RequestParam(value="id") String id) {
		return scopusService.findByPmid(id);
	}
	
	@ApiOperation(value = "Find the relevant scopus article for a supplied list of pubmedId's", notes = "This api finds the relevant scopus articles for a supplied list of pubmedId's.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ScopusArticle List retrieval successful"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
			})
	@RequestMapping(value = "/reciter/find/scopus/articles/pmids/", method = RequestMethod.GET)
	@ResponseBody
	public List<ScopusArticle> findByPmids(@RequestParam List<String> pmids) {
		slf4jLogger.info("calling findByPmids with size of pmids=" + pmids);
		return scopusService.findByPmids(pmids);
	}
	
	@ApiOperation(value = "Find the relevant scopus article for a supplied pubmedId directly from Scopus", notes = "This api finds the relevant scopus article for a supplied pubmedId directly from scopus.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ScopusArticle retrieval successful from Scopus"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
			})
	@RequestMapping(value = "/reciter/scopusarticle/{pmid}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ScopusArticle>> retrieveScopusArticleByPmid(@PathVariable String pmid) {
		List<Long> pmids = new ArrayList<>();
		pmids.add(Long.parseLong(pmid));
		ScopusArticleRetriever<Long> scopusArticleRetriever = new ScopusArticleRetriever<>();
		List<ScopusArticle> scopusArticles =
				scopusArticleRetriever.retrieveScopus(ScopusArticleRetriever.PMID_MODIFIER, new ArrayList<>(pmids));
		return ResponseEntity.ok(scopusArticles);
	}*/
}
