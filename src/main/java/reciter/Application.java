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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.web.RequestSquigglyContextProvider;
import com.github.bohnman.squiggly.web.SquigglyRequestFilter;
import com.google.common.collect.Iterables;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dyanmodb.files.GenderFileImport;
import reciter.database.dyanmodb.files.IdentityFileImport;
import reciter.database.dyanmodb.files.InstitutionAfidFileImport;
import reciter.database.dyanmodb.files.MeshTermFileImport;
import reciter.database.dyanmodb.files.ScienceMetrixDepartmentCategoryFileImport;
import reciter.database.dyanmodb.files.ScienceMetrixFileImport;
import reciter.database.dynamodb.model.Gender;
import reciter.database.dynamodb.model.InstitutionAfid;
import reciter.database.dynamodb.model.MeshTerm;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.engine.EngineParameters;
import reciter.security.APIKey;
import reciter.service.GenderService;
import reciter.service.ScienceMetrixDepartmentCategoryService;
import reciter.service.ScienceMetrixService;
import reciter.service.dynamo.DynamoDbInstitutionAfidService;
import reciter.service.dynamo.DynamoDbMeshTermService;
import reciter.utils.AffiliationStrategyUtils;
import reciter.utils.DegreeYearStrategyUtils;

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
    
    @Autowired
    private GenderService genderService;
    
    @Value("${use.scopus.articles}")
    private boolean useScopusArticles;
    
    @Value("${spring.security.enabled}")
    private boolean useAPISecurity;
    
    @Value("${aws.dynamodb.settings.file.import}")
    private boolean isFileImport;
    
    @Value("${strategy.gender}")
	private boolean useGenderStrategy;
	
	@Value("${strategy.discrepancyDegreeYear.degreeYearDiscrepancyScore}")
	private String degreeYearDiscrepancyScore;
	
	@Value("${strategy.authorAffiliationScoringStrategy.institutionStopwords}")
    private String instAfflInstitutionStopwords;
    
    private String scopusService = System.getenv("SCOPUS_SERVICE");
    
    private String pubmedService = System.getenv("PUBMED_SERVICE");
    
    private String reciterScoringService = System.getenv("RECITERSCORING_SERVICE_URL");
    
	@Autowired 
	private Environment env;
	
	@Value("${aws.reciterscoring.service.portNo}")
	private String reciterScoringPortNumber;
	
	@Bean
    public FilterRegistrationBean squigglyRequestFilter() {
        FilterRegistrationBean filter = new FilterRegistrationBean();
        filter.setFilter(new SquigglyRequestFilter());
        filter.setOrder(1);
        return filter;
    }
    
    

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
		
		Iterable<ObjectMapper> objectMappers = context.getBeansOfType(ObjectMapper.class)
	            .values();
		
		Squiggly.init(objectMappers, new RequestSquigglyContextProvider() {
            @Override
            protected String customizeFilter(String filter, HttpServletRequest request, Class beanClass) {

                /*// OPTIONAL: automatically wrap filter expressions in items{} when the object is a ListResponse
                if (filter != null && ListResponse.class.isAssignableFrom(beanClass)) {
                    filter = "items[" + filter + "]";
                }*/

                return filter;
            }
        });

        ObjectMapper objectMapper = Iterables.getFirst(objectMappers, null);

        // Enable Squiggly for Jackson message converter
        if (objectMapper != null) {
            for (MappingJackson2HttpMessageConverter converter : context.getBeansOfType(MappingJackson2HttpMessageConverter.class).values()) {
                converter.setObjectMapper(objectMapper);
            }
        }
	}
	
	
	/**
	 * This function will check for pubmed and scopus service url provided
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void checkScopusPubmedService() {
		if(useScopusArticles && scopusService != null && !scopusService.isEmpty()) {
			try {
				URL siteURL = new URL(scopusService + "/scopus/ping");
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
				URL siteURL = new URL(pubmedService  + "/pubmed/ping");
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
		
		if(reciterScoringService != null && !reciterScoringService.isEmpty()) 
		{
			String urlString = "http://localhost:"+ reciterScoringPortNumber +"/2015-03-31/functions/function/invocations";
	        String payload = "{}";  // Empty payload, as Lambda doesn't require specific input for health check
	        
	        ObjectMapper mapper = new ObjectMapper();
	        
	        Map<String, Object> payloadMap = new HashMap<>();
	        payloadMap.put("category", "test");
	        payloadMap.put("file_Name", "test");
	        payloadMap.put("useS3Bucket", "feedbackScore");
	        payloadMap.put("bucket_name", false);

	        
	        
	        String payloadJson=null;
			try {
				payloadJson = mapper.writeValueAsString(payloadMap);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        try {
		            // Create a URL object
		            URL url = new URL(urlString);
	
		            // Open the connection and configure it
		            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		            connection.setRequestMethod("POST");
		            connection.setRequestProperty("Content-Type", "application/json");
		            connection.setDoOutput(true);
	
		            // Send the payload (empty JSON in this case)
		            connection.getOutputStream().write(payloadJson.getBytes("UTF-8"));
	
		            // Get the HTTP response code
		            int responseCode = connection.getResponseCode();
		            if (responseCode == 200) {
						log.info("The reciterScoring Service endpoint " + reciterScoringService + " provided is valid and reachable");
					} else {
						log.info("The ReciterScoring Service endpoint " + reciterScoringService + " provided is not valid and not reachable");
					}
	        }catch (Exception e) {
				log.error("ReCiter---Scoring Container is not running. Please start the service: " + e.getMessage());
	 
			}
	          
		}
		
		if(pubmedService == null) {
			log.warn("ReCiter Application will not run without a pubmed service. Please download from https://github.com/wcmc-its/ReCiter-PubMed-Retrieval-Tool.git and setup Pubmed Service.");
		}
		
		if(scopusService == null) {
			if(useScopusArticles) {
				log.warn("The property `use.scopus.articles` is set to `true` in the application.properties file. If you haven't done so already, please be sure to install the ReCiter Scopus Retrieval Tool, located at https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool. If you wish to run ReCiter without the Scopus service, set `use.scopus.articles=false`.");
			} else {
				log.warn("The property `use.scopus.articles` is set to `false` in the application.properties file, so it will not be using the ReCiter Scopus Retrieval Tool. To install the ReCiter Scopus Retrieval Tool, you would need to go to https://github.com/wcmc-its/ReCiter-Scopus-Retrieval-Tool and set property `use.scopus.articles=true`.");
			}
		}
		if(reciterScoringService == null || reciterScoringService.isEmpty()) {
			log.warn("ReCiter Application will not run without a ReCiterScoring service. Please download from https://github.com/wcmc-its/ReCiter---Scoring.git and setup ReCiter---Scoring Service.");
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
			
			if(useGenderStrategy) {
				GenderFileImport genderFileImport = ApplicationContextHolder.getContext().getBean(GenderFileImport.class);
				genderFileImport.importGender();
			} else {
				log.info("Gender strategy use is set to false. Please update strategy.gender to true in application.properties file to use it.\n"
			+ "Its recommened to use this strategy to get better scores.");
			}
			
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
        if(scienceMetrixJournals != null) {
        		EngineParameters.setScienceMetrixJournals(scienceMetrixJournals);
        }
        
        log.info("Loading ScienceMetrixDepartmentCategories to Engine Parameters");
		List<ScienceMetrixDepartmentCategory> scienceMetrixDeptCategories = scienceMetrixDepartmentCategoryService.findAll();
        if(scienceMetrixDeptCategories != null) {
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
        if(useGenderStrategy) {
	        log.info("Loading GenderProbability to Engine Parameters");
	        List<Gender> genders = genderService.findAll();
	        if(genders != null && !genders.isEmpty()) {
	        	EngineParameters.setGenders(genders);
	        }
        }
        
        if(useScopusArticles) {
	        log.info("Loading ScopusInstitutionalAfids to Engine Parameters");
	        List<InstitutionAfid> instAfids = dynamoDbInstitutionAfidService.findAll();
	        if(instAfids != null && instAfids.size() > 0) {
		        	Map<String, List<String>> institutionAfids = instAfids.stream().collect(Collectors.toMap(InstitutionAfid::getInstitution, InstitutionAfid::getAfids));
		        	EngineParameters.setAfiliationNameToAfidMap(institutionAfids);
	        }
		}
		DegreeYearStrategyUtils degreeYearStrategyUtils = new DegreeYearStrategyUtils();
		EngineParameters.setDegreeYearDiscrepancyScoreMap(degreeYearStrategyUtils.getDegreeYearDiscrepancyScoreMap(this.degreeYearDiscrepancyScore));

		AffiliationStrategyUtils affiliationStrategyUtils = new AffiliationStrategyUtils();
		EngineParameters.setRegexForStopWords(affiliationStrategyUtils.constructRegexForStopWords(this.instAfflInstitutionStopwords));
		
        log.info("ReCiter is up and ready to use. Please make sure its other components such as Pubmed-Retrieval-Tool is also setup if you wish to do retrieval.");
	}
	


    /**
     * @return APIKey
     */
    @Bean
    public APIKey apiKeySetter() {
    	APIKey apiKey = new APIKey();
    	apiKey.setAdminApiKey(env.getProperty("ADMIN_API_KEY"));
    	apiKey.setConsumerApiKey(env.getProperty("CONSUMER_API_KEY"));
    	if(useAPISecurity && (apiKey.getAdminApiKey() == null || apiKey.getConsumerApiKey() == null)) {
    		throw new BeanCreationException("The ADMIN_API_KEY and CONSUMER_API_KEY should be set in environment variable. Or mark spring.security.enabled as false in application.propeties file.");
    	}
        return apiKey;
    }
}
