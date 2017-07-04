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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import reciter.model.pubmed.PubMedArticle;
import reciter.service.mongo.PubMedService;

import javax.xml.ws.WebServiceException;

@Slf4j
@RestController
@RequestMapping("/pubmed")
public class PubMedController {

    @Autowired
    private PubMedService pubMedService;

    @RequestMapping(value = "/save/pubmed/articles/", method = RequestMethod.PUT)
    @ResponseBody
    public void savePubMedArticles(@RequestBody List<PubMedArticle> pubMedArticles) {
        log.info("calling savePubMedArticles with numberOfPubmedArticles=" + pubMedArticles.size());
        pubMedService.save(pubMedArticles);
    }

    @RequestMapping(value = "/find/pubmed/articles/pmids/", method = RequestMethod.GET)
    @ResponseBody
    public List<PubMedArticle> findByPmids(@RequestParam List<Long> pmids) {
        log.info("calling findByPmids with size of pmids=" + pmids);
        return pubMedService.findByPmids(pmids);
    }

    @RequestMapping(value = "/pmid/{pmid}", method = RequestMethod.GET)
    @ResponseBody
    public String pubMedArticle(@PathVariable long pmid, @RequestParam(value="fields", required=false) String fields) {
        log.info("pmid=" + pmid);
        PubMedArticle pubMedArticle = pubMedService.findByPmid(pmid);
        if (pubMedArticle == null) {
            log.error("Unable to find.");
            throw new WebServiceException("Unable to find PubMedArticle with pmid=" + pmid);
        }
        log.info("Id: " + pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid());
//        return pubMedArticle;
        StringTokenizer st = new StringTokenizer(fields, ",");
        Set<String> filterProps = new HashSet<String>();
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            log.info("token: " + token);
            filterProps.add(token);
        }
//        SquigglyPropertyFilter propertyFilter = new SquigglyPropertyFilter("medlineCitation[medlineCitationPMID]");
////        ObjectMapper mapper = new ObjectMapper();
//        FilterProvider filterProvider = new SimpleFilterProvider().addFilter("squigglyFilter",
//                SimpleBeanPropertyFilter.filterOutAllExcept(filterProps));
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setFilterProvider(filterProvider);
//        objectMapper.addMixIn(Object.class, SquigglyPropertyFilterMixin.class);
//        System.out.println(SquigglyUtils.stringify(objectMapper, pubMedArticle));
//        return SquigglyUtils.stringify(objectMapper, pubMedArticle);
        ObjectMapper objectMapper = Squiggly.init(new ObjectMapper(), fields);
//        Issue object = new Issue();         // replace this with your object/collection/map here
        System.out.println(SquigglyUtils.stringify(objectMapper, pubMedArticle));
        return SquigglyUtils.stringify(objectMapper, pubMedArticle);
//        try
//        {
//            String json = mapper.writer(filters).writeValueAsString(pubMedArticle);
//            return json;
//        }
//        catch(IOException are)
//        {
//            are.printStackTrace();
//            return are.getMessage();
//        }
//        return new PartialResponse(pubMedArticle, "pubMedData");
    }
}
