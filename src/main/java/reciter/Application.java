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
package reciter;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dyanmodb.files.IdentityFileImport;
import reciter.database.dyanmodb.files.InstitutionAfidFileImport;
import reciter.database.dyanmodb.files.MeshTermFileImport;
import reciter.database.dyanmodb.files.ScienceMetrixDepartmentCategoryFileImport;
import reciter.database.dyanmodb.files.ScienceMetrixFileImport;
import reciter.database.dynamodb.model.InstitutionAfid;
import reciter.database.dynamodb.model.MeshTerm;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.engine.EngineParameters;
import reciter.service.ScienceMetrixDepartmentCategoryService;
import reciter.service.ScienceMetrixService;
import reciter.service.dynamo.DynamoDbInstitutionAfidService;
import reciter.service.dynamo.DynamoDbMeshTermService;
import reciter.storage.s3.AmazonS3Config;

@Slf4j
@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EnableAsync
@EnableDynamoDBRepositories("reciter.database.dynamodb")
@ComponentScan("reciter")
public class Application {

//	@Bean
//	public BCryptPasswordEncoder bCryptPasswordEncoder() {
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//		String password = "reciter";
//		String hashedPassword = encoder.encode(password);
//		System.out.println("password:" + hashedPassword);
//		return new BCryptPasswordEncoder();
//	}
	
    @Autowired
    private DynamoDbMeshTermService dynamoDbMeshTermService;
	
    @Autowired
    private ScienceMetrixService scienceMetrixService;
    
    @Autowired
    private ScienceMetrixDepartmentCategoryService scienceMetrixDepartmentCategoryService;
    
    @Autowired
    private DynamoDbInstitutionAfidService dynamoDbInstitutionAfidService;
    
    @Value("${use.scopus.articles}")
    private boolean useScopusArticles;
    
    @Value("${aws.dynamodb.settings.file.import}")
    private boolean isFileImport;
    
    private String scopusService = System.getenv("SCOPUS_SERVICE");
    
    private String pubmedService = System.getenv("PUBMED_SERVICE");
    
    

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	
	/**
	 * This function will check for pubmed and scopus service url provided
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void checkScopusPubmedService() {
		if(scopusService != null && !scopusService.isEmpty()) {
			try {
				URL siteURL = new URL(scopusService + "/swagger-ui.html");
				HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(10000);
				connection.connect();
	 
				int code = connection.getResponseCode();
				if (code == 200) {
					log.info("The Scopus Service endpoint " + scopusService + " provided is valid and reachable");
				} else {
					log.info("The Scopus Service endpoint " + scopusService + " provided is not valid and not reachable");
				}
			} catch (Exception e) {
				log.error("Wrong domain - Exception: " + e.getMessage());
	 
			}
		}
		
		if(pubmedService != null && !pubmedService.isEmpty()) {
			try {
				URL siteURL = new URL(pubmedService  + "/swagger-ui.html");
				HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(10000);
				connection.connect();
	 
				int code = connection.getResponseCode();
				if (code == 200) {
					log.info("The Pubmed Service endpoint " + pubmedService + " provided is valid and reachable");
				} else {
					log.info("The Pubmed Service endpoint " + pubmedService + " provided is not valid and not reachable");
				}
			} catch (Exception e) {
				log.error("Wrong domain - Exception: " + e.getMessage());
	 
			}
		}
		
		if(pubmedService == null) {
			log.warn("ReCiter Application will not run without a pubmed service. Please download from https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool.git and setup Scopus Service.");
		}
		
		if(scopusService == null) {
			log.warn("ReCiter Application will not run without a scopus service. Please download from https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool.git and setup Pubmed Service.");
		}
	}
	
	
	/**
	 * This function will load all the necessary data from files folder to dynamodb
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void loadDynamoDbTablesAfterStartUp() {
		if(isFileImport) {
			ScienceMetrixDepartmentCategoryFileImport scienceMetrixDepartmentCategoryFileImport = ApplicationContextHolder.getContext().getBean(ScienceMetrixDepartmentCategoryFileImport.class);
			scienceMetrixDepartmentCategoryFileImport.importScienceMetrixDepartmentCategory();
			
			ScienceMetrixFileImport scienceMetrixFileImport = ApplicationContextHolder.getContext().getBean(ScienceMetrixFileImport.class);
			scienceMetrixFileImport.importScienceMetrix();
			
			MeshTermFileImport meshTermFileImport = ApplicationContextHolder.getContext().getBean(MeshTermFileImport.class);
			meshTermFileImport.importMeshTerms();
			
			IdentityFileImport identityFileImport = ApplicationContextHolder.getContext().getBean(IdentityFileImport.class);
			identityFileImport.importIdentity();
			
			if(useScopusArticles) {
				InstitutionAfidFileImport institutionAfidFileImport = ApplicationContextHolder.getContext().getBean(InstitutionAfidFileImport.class);
				institutionAfidFileImport.importInstitutionAfids();
			}
		}
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void populateStaticEngineParameters() {
		
		log.info("Loading ScienceMetrixJournals to Engine Parameters");
		List<ScienceMetrix> scienceMetrixJournals = scienceMetrixService.findAll();
        if(scienceMetrixJournals != null && scienceMetrixJournals.size() > 0) {
        	EngineParameters.setScienceMetrixJournals(scienceMetrixJournals);
        }
        
        log.info("Loading ScienceMetrixDepartmentCategories to Engine Parameters");
		List<ScienceMetrixDepartmentCategory> scienceMetrixDeptCategories = scienceMetrixDepartmentCategoryService.findAll();
        if(scienceMetrixDeptCategories != null && scienceMetrixDeptCategories.size() > 0) {
        	EngineParameters.setScienceMetrixDepartmentCategories(scienceMetrixDeptCategories);
        }
        
        log.info("Loading MeshTermCounts to Engine Parameters");
        if (EngineParameters.getMeshCountMap() == null) {
            List<MeshTerm> meshTerms = dynamoDbMeshTermService.findAll();
            Map<String, Long> meshCountMap = new HashMap<>();
            for (MeshTerm meshTerm : meshTerms) {
                meshCountMap.put(meshTerm.getMesh(), meshTerm.getCount());
            }
            EngineParameters.setMeshCountMap(meshCountMap);
        }
        if(useScopusArticles) {
	        log.info("Loading ScopusInstitutionalAfids to Engine Parameters");
	        List<InstitutionAfid> instAfids = dynamoDbInstitutionAfidService.findAll();
	        if(instAfids != null && instAfids.size() > 0) {
	        	ObjectMapper mapper = new ObjectMapper();
	            try {
					mapper.writeValue(new File("/Users/szd2013/git/ReCiter/src/main/resources/files/InstitutionAfid.json"), instAfids);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					log.error("JSON parsing error occurred while processing InstitutionAfid.json file", e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("IO Error occurred while processing InstitutionAfid.json file", e);
				}
	        	Map<String, List<String>> institutionAfids = instAfids.stream().collect(Collectors.toMap(InstitutionAfid::getInstitution, InstitutionAfid::getAfids));
	        	EngineParameters.setAfiliationNameToAfidMap(institutionAfids);
	        }
        }
	}
}