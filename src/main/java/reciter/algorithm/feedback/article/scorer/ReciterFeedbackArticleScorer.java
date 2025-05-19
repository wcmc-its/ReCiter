package reciter.algorithm.feedback.article.scorer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import reciter.algorithm.article.score.predictor.NeuralNetworkModelArticlesScorer;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.feedbackevidence.FeedbackEvidenceStrategyContext;
import reciter.algorithm.evidence.article.feedbackevidence.strategy.FeedbackEvidenceStrategy;
import reciter.algorithm.evidence.feedback.targetauthor.TargetAuthorFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.cites.CitesFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.cites.strategy.CitesFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.coauthorname.CoauthorNameFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.coauthorname.strategy.CoauthorNameFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.email.EmailFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.email.strategy.EmailFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.institution.InstitutionFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.institution.strategy.InstitutionFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.journal.JournalFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.journal.strategy.JournalFeedbackStrategy;
//import reciter.algorithm.evidence.targetauthor.feedback.journaldomain.JournalDomainFeedbackStrategyContext;
//import reciter.algorithm.evidence.targetauthor.feedback.journaldomain.strategy.JournalDomainFeedbackStrategy;
//import reciter.algorithm.evidence.targetauthor.feedback.journalfield.JournalFieldFeedbackStrategyContext;
//import reciter.algorithm.evidence.targetauthor.feedback.journalfield.strategy.JournalFieldFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.journalsubfield.JournalSubFieldFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.journalsubfield.strategy.JournalSubFieldFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.keyword.KeywordFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.keyword.strategy.KeywordFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.orcid.OrcidFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.orcid.strategy.OrcidFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.orcidcoauthor.OrcidCoauthorFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.orcidcoauthor.strategy.OrcidCoauthorFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.organization.OrganizationFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.organization.strategy.OrganizationFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.targetauthorname.TargetAuthorNameFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.targetauthorname.strategy.TargetAuthorNameFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.year.YearFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.year.strategy.YearFeedbackStrategy;
import reciter.engine.EngineParameters;
import reciter.engine.StrategyParameters;
import reciter.engine.analysis.evidence.AffiliationEvidence;
import reciter.engine.analysis.evidence.ArticleCountEvidence;
import reciter.engine.analysis.evidence.AuthorCountEvidence;
import reciter.engine.analysis.evidence.AuthorNameEvidence;
import reciter.engine.analysis.evidence.EducationYearEvidence;
import reciter.engine.analysis.evidence.EmailEvidence;
import reciter.engine.analysis.evidence.GenderEvidence;
import reciter.engine.analysis.evidence.JournalCategoryEvidence;
import reciter.engine.analysis.evidence.NonTargetAuthorScopusAffiliation;
import reciter.engine.analysis.evidence.TargetAuthorPubmedAffiliation;
import reciter.engine.analysis.evidence.TargetAuthorScopusAffiliation;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackIdentityScore;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;
import reciter.utils.PropertiesUtils;

public class ReciterFeedbackArticleScorer extends AbstractFeedbackArticleScorer {

	
	private static final Logger log = LoggerFactory.getLogger(ReciterFeedbackArticleScorer.class);
	private static final int ACCEPTED_ASSERTION =1;
	private static final int REJECTED_ASSERTION =-1;
	private List<ReCiterArticle> reciterArticles;
	private Identity identity;
	public static StrategyParameters strategyParameters;
	/**
	 * Journal Category Score
	 */
	private StrategyContext journalStrategyContext;
	private StrategyContext journalSubFieldStrategyContext;
	private StrategyContext orcidStrategyContext;
	private StrategyContext yearStrategyContext;
	private StrategyContext targetAuthorNameStrategyContext;
	private StrategyContext organizationStrategyContext;
	private StrategyContext orcidCoAuthorStrategyContext;
	private StrategyContext keywordStrategyContext;
	private StrategyContext institutionStrategyContext;
	private StrategyContext emailStrategyContext;
	private StrategyContext coAuthorNameStrategyContext;
	private StrategyContext citesStrategyContext;
	private StrategyContext feedbackEvidenceStrategyContext;
	
	ExecutorService executorService = Executors.newWorkStealingPool(13);
	
	public ReciterFeedbackArticleScorer(List<ReCiterArticle> articles,Identity identity,EngineParameters parameters,StrategyParameters strategyParameters)
	{
		ReciterFeedbackArticleScorer.strategyParameters = strategyParameters;
		this.reciterArticles = articles;
		this.identity = identity;
		this.journalStrategyContext = new JournalFeedbackStrategyContext(new JournalFeedbackStrategy());
		this.journalSubFieldStrategyContext = new JournalSubFieldFeedbackStrategyContext(new JournalSubFieldFeedbackStrategy());
		this.orcidStrategyContext = new OrcidFeedbackStrategyContext(new OrcidFeedbackStrategy());
		this.yearStrategyContext = new YearFeedbackStrategyContext(new YearFeedbackStrategy());
		this.targetAuthorNameStrategyContext = new TargetAuthorNameFeedbackStrategyContext(new TargetAuthorNameFeedbackStrategy());
		this.organizationStrategyContext = new OrganizationFeedbackStrategyContext(new OrganizationFeedbackStrategy());
		this.orcidCoAuthorStrategyContext = new OrcidCoauthorFeedbackStrategyContext(new OrcidCoauthorFeedbackStrategy());
		this.keywordStrategyContext = new KeywordFeedbackStrategyContext(new KeywordFeedbackStrategy(strategyParameters));
		this.institutionStrategyContext = new InstitutionFeedbackStrategyContext(new InstitutionFeedbackStrategy());
		this.emailStrategyContext = new EmailFeedbackStrategyContext(new EmailFeedbackStrategy());
		this.coAuthorNameStrategyContext = new CoauthorNameFeedbackStrategyContext(new CoauthorNameFeedbackStrategy());
		this.citesStrategyContext = new CitesFeedbackStrategyContext(new CitesFeedbackStrategy());
		this.feedbackEvidenceStrategyContext = new FeedbackEvidenceStrategyContext(new FeedbackEvidenceStrategy());
		
		
	}
	public void runFeedbackArticleScorer(List<ReCiterArticle> reCiterArticles, Identity identity) 
	{
		 List<Future<?>> futures = new ArrayList<>();

		if(strategyParameters.isFeedbackScoreJournal()) {
			futures.add(submitAndLogTime("Journal Category", executorService, journalStrategyContext, reCiterArticles, identity));
		}
		if(strategyParameters.isFeedbackScoreJournalSubField()) {
			futures.add(submitAndLogTime("Journal SubField Category", executorService, journalSubFieldStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreOrcid()) {
			futures.add(submitAndLogTime("Orcid Category", executorService, orcidStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreYear()) {
			futures.add(submitAndLogTime("Year Category", executorService, yearStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreEmail()) {
			futures.add(submitAndLogTime("Email Category", executorService, emailStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreOrganization()) {
			futures.add(submitAndLogTime("Organization Category", executorService, organizationStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreInstitution()) {
			futures.add(submitAndLogTime("Institution Category", executorService, institutionStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreTargetAuthorName()) {
			futures.add(submitAndLogTime("Target Author Name Category", executorService, targetAuthorNameStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreOrcidCoAuthor()) {
			futures.add(submitAndLogTime("Orcid coAuthor Category", executorService, orcidCoAuthorStrategyContext, reCiterArticles, identity));
		}
		if(strategyParameters.isFeedbackScoreKeyword()) {
			futures.add(submitAndLogTime("Keyword Category", executorService, keywordStrategyContext, reCiterArticles, identity));
		}
		if(strategyParameters.isFeedbackScoreCoauthorName()) {
			futures.add(submitAndLogTime("CoAuthorName Category", executorService, coAuthorNameStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreCites()) {
			futures.add(submitAndLogTime("Cites Category", executorService, citesStrategyContext, reCiterArticles, identity));

		}
		//futures.add(submitAndLogTime("authors Count Category", executorService, authorCountStrategyContext, reCiterArticles, identity));
		// Shutdown executorService after submitting all tasks
        executorService.shutdown();
        
        // Wait for all tasks to complete
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean allTasksCompleted = true;
        // Print execution times from futures
        for (Future<?> future : futures) {
            try {
                future.get(); // Ensure all tasks are completed
            } catch (InterruptedException | ExecutionException e) {
                log.error("Task execution interrupted or encountered an error", e);
                allTasksCompleted=false;
            }
        }
        if (allTasksCompleted) {
        	//Export item Level Data
        	exportArticleItemLevelReport(reCiterArticles, identity);
            // All tasks completed successfully
            exportArticlesConsolidateReport(reCiterArticles, identity);
         	((ReCiterArticleStrategyContext) feedbackEvidenceStrategyContext).executeStrategy(reCiterArticles);
         	executePythonScriptForArticleFeedbackTotal(reCiterArticles,identity);
	    } else {
            log.error("One or more tasks failed; report generation may be incomplete.");
        }


	}
	
	private Future<?> submitAndLogTime(String category, ExecutorService executorService,
			StrategyContext context,List<ReCiterArticle> reCiterArticles, Identity identity) 
	{

		return executorService.submit(() -> {
		StopWatch stopWatch = new StopWatch(category);
		stopWatch.start(category);
		((TargetAuthorFeedbackStrategyContext)context).executeFeedbackStrategy(reCiterArticles, identity);
		stopWatch.stop();
		log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
		});
	}
	private void exportArticlesConsolidateReport(List<ReCiterArticle> reCiterArticles, Identity identity )
	{
		// printing all the article lookup PMID and CoAuthors associated with other
		// PMIDs.
		Map<Long, ReCiterArticle> articlesMap = reCiterArticles.stream()
	            .collect(Collectors.toMap(ReCiterArticle::getArticleId, Function.identity()));
		exportConsolidatedFeedbackScores(identity.getUid(), articlesMap);
	}
	private void exportArticleItemLevelReport(List<ReCiterArticle> reCiterArticles, Identity identity )
	{
		// printing all the article lookup PMID and CoAuthors associated with other
		// PMIDs.
		exportArticleItemLevelFeedbackScores(identity.getUid(), reCiterArticles);
	}
	protected void exportConsolidatedFeedbackScores(String personIdentifier, Map<Long,ReCiterArticle> articleMap)
	{
		String[] csvHeaders = { "PersonIdentifier","Pmid","userAssertion","scoreCites","scoreCoAuthorName","scoreEmail",
    		 	"scoreInstitution","scoreJournal","scoreJournalSubField","scoreKeyword","scoreOrcid","scoreOrcidCoAuthor",
    		 	"scoreOrganization","scoreTargetAuthorName","scoreYear" };
		
		Path filePath = Paths.get(personIdentifier + "_consolidated.csv");
		
		if(isS3UploadRequired()) 
		{	
			try ( // Create BufferedWriter
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
					// Create CSVPrinter
					CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create(CSVFormat.DEFAULT)
			                             .setHeader(csvHeaders)
			                             .build())) {	
				
				mapConsolidatedCSVData(articleMap,csvPrinter,personIdentifier);

				csvPrinter.flush();
				NeuralNetworkModelArticlesScorer nnmodel = new NeuralNetworkModelArticlesScorer();																	  
				log.warn("Uploading CSV into S3 starts here******************",outputStream.toString(StandardCharsets.UTF_8.name()),filePath.toString());
				boolean uploadCsvToS3 = uploadCsvToS3(outputStream.toString(StandardCharsets.UTF_8.name()),filePath.toString());
				/*if(uploadCsvToS3) {
					 nnmodel. deleteFile(filePath);
					 log.info("File deleted successfully: " + filePath);
				}*/
				log.warn("Uploading CSV into S3 ends here******************");
				
			} catch (IOException e) {
				e.printStackTrace();
				
			}	
		}
		else
		{
			try ( // Create BufferedWriter
					BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
					// Create CSVPrinter
					CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create(CSVFormat.DEFAULT)
			                             .setHeader(csvHeaders)
			                             .build())) {	
				
				mapConsolidatedCSVData(articleMap,csvPrinter,personIdentifier);
				csvPrinter.flush();
				
			 
			} catch (IOException e) {
				e.printStackTrace();
				
			}
		}
	}
	protected void exportArticleItemLevelFeedbackScores(String personIdentifier, List<ReCiterArticle> reCiterArticles)
	{
		
		String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
				"subscoreType1", "subscoreValue", "subScoreIndividualScore","UserAssertion" };
		
		Path filePath = Paths.get(personIdentifier + "_item_level.csv");
		
		if(isS3UploadRequired()) 
		{
			try ( // Create BufferedWriter
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
					// Create CSVPrinter
					CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create(CSVFormat.DEFAULT)
	                        .setHeader(csvHeaders).build())) {
	
					mapItemLevelCSVData(reCiterArticles,csvPrinter,personIdentifier);
				
				csvPrinter.flush();
				log.warn("Uploading CSV into S3 starts here******************",outputStream.toString(StandardCharsets.UTF_8.name()),filePath.toString());
				boolean uploadCsvToS3 = uploadCsvToS3(outputStream.toString(StandardCharsets.UTF_8.name()),filePath.toString());
				/*NeuralNetworkModelArticlesScorer nnmodel = new NeuralNetworkModelArticlesScorer();
				if(uploadCsvToS3) {
					 nnmodel.deleteFile(filePath);
					 log.info("File deleted successfully: " + filePath);
				}*/
				log.warn("Uploading CSV into S3 ends here******************");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		{
			try ( // Create BufferedWriter
					BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
					// Create CSVPrinter
					CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create(CSVFormat.DEFAULT)
	                        .setHeader(csvHeaders).build())) {
	
					mapItemLevelCSVData(reCiterArticles,csvPrinter,personIdentifier);
				
				csvPrinter.flush();
				//NeuralNetworkModelArticlesScorer nnmodel = new NeuralNetworkModelArticlesScorer();
				//nnmodel.deleteFile(filePath);																	  
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	private boolean uploadCsvToS3(String csvContent,String fileName) {
       
		String FeedbackScoreBucketName = PropertiesUtils.get("aws.s3.feedback.score.bucketName");
        // Create InputStream from CSV content
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        
        // Set metadata
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(csvContent.length());
       	metadata.setContentType("text/csv");
     
        // Upload the CSV file
        try {
        	
        	final AmazonS3 s3 = AmazonS3ClientBuilder
					.standard()
					.withCredentials(new DefaultAWSCredentialsProviderChain())
					.withRegion(System.getenv("AWS_REGION"))
					.build();
			if(s3.doesBucketExistV2(FeedbackScoreBucketName)) {
        		log.info("Uploading files to S3 bucket ",FeedbackScoreBucketName);
                PutObjectRequest putObjectRequest = new PutObjectRequest(FeedbackScoreBucketName.toLowerCase(), fileName, inputStream, metadata);
                try{
    				s3.putObject(putObjectRequest);
    			    log.info("CSV file uploaded successfully to S3 bucket: " + FeedbackScoreBucketName);
    			    return true;
    			}
    			catch(AmazonServiceException e) {
    				// The call was transmitted successfully, but Amazon S3 couldn't process 
    	            // it, so it returned an error response.
    				log.error(e.getErrorMessage());
    				return false;
    			}
        	}
        	else {
        		log.error("S3 bucket does not exist: " + FeedbackScoreBucketName);
                return false;
        	}
        
        } catch (Exception e) {
            e.printStackTrace();
			return false;			 
        }
    }
	private void mapConsolidatedCSVData(Map<Long,ReCiterArticle> articleMap,CSVPrinter csvPrinter,String personIdentifier)
	{
		articleMap.forEach((articleId, article) -> {
			try {
				
						
					String citiesFeedbackScore = article.getExportedCitesFeedbackScore()!=null? article.getExportedCitesFeedbackScore() :"0" ;
					String coAuthorFeedbackScore = article.getExportedCoAuthorNameFeedbackScore()!= null?article.getExportedCoAuthorNameFeedbackScore() :"0";
					String emailFeedbackScore = article.getExportedEmailFeedbackScore()!=null?article.getExportedEmailFeedbackScore():"0";
					String institutionFeedbackScore = article.getExportedInstitutionFeedbackScore()!=null?article.getExportedInstitutionFeedbackScore():"0" ;
					String journalFeedbackScore = article.getExportedJournalFeedackScore()!=null?article.getExportedJournalFeedackScore():"0";
					String journalSubFieldFeedbackScore = article.getExportedJournalSubFieldFeedbackScore()!=null?article.getExportedJournalSubFieldFeedbackScore():"0";
					String keywordFeedbackScore = article.getExportedKeywordFeedackScore()!=null?article.getExportedKeywordFeedackScore():"0";
					String orcidFeedbackScore = article.getExportedOrcidFeedbackScore()!=null?article.getExportedOrcidFeedbackScore():"0";
					String orcidCoAuthorFeedbackScore = article.getExportedOrcidCoAuthorFeedbackScore()!=null?article.getExportedOrcidCoAuthorFeedbackScore():"0";
					String organizationFeedbackScore = article.getExportedOrganizationFeedbackScore()!=null?article.getExportedOrganizationFeedbackScore():"0";
					String targetAuthorNameFeedbackScore = article.getExportedTargetAuthorNameFeedbackScore()!=null?article.getExportedOrganizationFeedbackScore():"0";
					String yearFeedbackScore = article.getExportedYearFeedbackScore();
					
					
					csvPrinter.printRecord(personIdentifier, 
							articleId, 
							article.getGoldStandard(),
							citiesFeedbackScore,
							coAuthorFeedbackScore,
							emailFeedbackScore,
							institutionFeedbackScore,
							journalFeedbackScore,
							journalSubFieldFeedbackScore,
							keywordFeedbackScore,
							orcidFeedbackScore,
							orcidCoAuthorFeedbackScore,
							organizationFeedbackScore,
							targetAuthorNameFeedbackScore,
							yearFeedbackScore
						//	totalFeedbackScoreStr
							);
					
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}
	private void mapItemLevelCSVData(List<ReCiterArticle> reCiterArticles,CSVPrinter csvPrinter,String personIdentifier)
	{
		reCiterArticles.stream()
		 .filter(article -> article!=null && article.getArticleFeedbackScoresMap()!=null && article.getArticleFeedbackScoresMap().size() > 0)	
		.forEach(article -> {
				try {
					 
					
					article.getArticleFeedbackScoresMap().stream()
						  .flatMap(map -> map.entrySet().stream()
							 .flatMap(entry -> entry.getValue().stream()))
						 .forEach(feedbackScore -> { 
									if(feedbackScore.getFeedbackScore() != 0.0)
									{
										//String feedbackScore = article.getExportedFeedbackScore();
										try {
											csvPrinter.printRecord(personIdentifier, feedbackScore.getArticleId(),
													feedbackScore.getAcceptedCount(), feedbackScore.getRejectedCount(), feedbackScore.getFeedbackScoreType(),
													feedbackScore.getFeedbackScoreFieldValue(), feedbackScore.getFeedbackScore(),feedbackScore.getGoldStandard());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
							 });
							 
					} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});
	}
	
    private boolean isS3UploadRequired()
    {
    	  	  
  	     // Retrieve properties
         String awsS3Use = PropertiesUtils.get("aws.s3.use");
         boolean isS3Use = Boolean.parseBoolean(awsS3Use);
         String dynamoDDLocal = PropertiesUtils.get("aws.dynamoDb.local");
         boolean isDynamoDBLocal = Boolean.parseBoolean(dynamoDDLocal);
         if(isS3Use && !isDynamoDBLocal) 
        	 return true;
    	return false;
    }
      private List<ReCiterArticle> executePythonScriptForArticleFeedbackTotal(List<ReCiterArticle> reCiterArticles, Identity identity) {
    
    	//retrieving countAccepted, CountRejected and CountNull 
		
		Map<Integer, List<ReCiterArticle>> groupedByGoldStandard = reCiterArticles.stream()
	            .collect(Collectors.groupingBy(ReCiterArticle::getGoldStandard));
		
		//countAccepted
		int countAccepted = groupedByGoldStandard.getOrDefault(ACCEPTED_ASSERTION, Collections.emptyList()).size();
		
		//countRejected
		int countRejected = groupedByGoldStandard.getOrDefault(REJECTED_ASSERTION, Collections.emptyList()).size();
		
    	List<ReCiterArticleFeedbackIdentityScore> articleIdentityFeedbackScore = reCiterArticles.stream()
    																	.map( article -> mapToFeedbackScore(article, countAccepted, countRejected))
														    		    .collect(Collectors.toList());
	
    	ObjectMapper objectMapper = new ObjectMapper();
    	
		String fileName = StringUtils.join(identity.getUid(), "-feedbackIdentityScoringInput.json");
		boolean isS3UploadRequired = isS3UploadRequired();
		String feedbackIdentityS3BucketName = PropertiesUtils.get("aws.s3.feedback.score.bucketName");
        try {
			NeuralNetworkModelArticlesScorer nnmodel = new NeuralNetworkModelArticlesScorer();																			   
        	  if(isS3UploadRequired) 
        	  {
        		  File jsonFile = new File(fileName);
        		  
        		// Write the User object to the JSON file
                  objectMapper.writeValue(jsonFile, articleIdentityFeedbackScore);
                  log.info("JSON data written to file successfully: ", jsonFile.getAbsolutePath());
                  boolean uploadJsonFileIntoS3 = uploadJsonFileIntoS3(fileName, jsonFile);
                  
                  if(uploadJsonFileIntoS3) {
                	//  nnmodel.deleteFile(jsonFile.toPath());
 					 log.info("File deleted successfully: " + jsonFile);
 				}
        	  }
        	  else
        	  {	  
        		  File jsonFile = new File("src/main/resources/scripts/"+fileName);
	        	  objectMapper.writeValue(jsonFile,articleIdentityFeedbackScore);
				  log.info("JSON written to file successfully.", jsonFile.getAbsolutePath() +"-" + fileName);
        	  }
        	  String isS3UploadRequiredString = Boolean.toString(isS3UploadRequired);
			  
			  JSONArray articlesIdentityFeedbackScoreTotal = nnmodel.executeArticleScorePredictor("FeedbackIdentityScore", "feedbackIdentityScoreArticles.py",fileName,feedbackIdentityS3BucketName,isS3UploadRequiredString);
			  log.info("articlesIdentityFeedbaclScoreTotal length",articlesIdentityFeedbackScoreTotal!=null?articlesIdentityFeedbackScoreTotal.length():0);
			  if(articlesIdentityFeedbackScoreTotal!=null && articlesIdentityFeedbackScoreTotal.length() > 0)
			  {  
				  List<ReCiterArticle> articlesScores =  mapAuthorshipLikelihoodScore(reCiterArticles, articlesIdentityFeedbackScoreTotal);
			  		articlesScores.forEach(article -> log.info("articleId :", article.getArticleId(), "authorshipLikelihoodScore : ", article.getAuthorshipLikelihoodScore() ));
			  	 return articlesScores;	
			  }  	
				  
			  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    private static ReCiterArticleFeedbackIdentityScore mapToFeedbackScore(ReCiterArticle article,int countAccepted, int countRejected) {
   
        try {
        	
        	return new ReCiterArticleFeedbackIdentityScore(
														    article.getArticleId(),
														    getFeedbackScore(article.getCitesFeedbackScore()),
														    getFeedbackScore(article.getCoAuthorNameFeedbackScore()),
														    getFeedbackScore(article.getEmailFeedbackScore()),
														    getFeedbackScore(article.getInstitutionFeedbackScore()),
														    getFeedbackScore(article.getJournalFeedackScore()),
														    getFeedbackScore(article.getJournalSubFieldFeedbackScore()),
														    getFeedbackScore(article.getKeywordFeedackScore()),
														    getFeedbackScore(article.getOrcidFeedbackScore()),
														    getFeedbackScore(article.getOrcidCoAuthorFeedbackScore()),
														    getFeedbackScore(article.getOrganizationFeedbackScore()),
														    getFeedbackScore(article.getTargetAuthorNameFeedbackScore()),
														    getFeedbackScore(article.getYearFeedbackScore()),
														    getArticleCountScore(article.getArticleCountEvidence()),
														    getAuthorsCountScore(article.getAuthorCountEvidence()),													   
														    getEducationYearScore(article.getEducationYearEvidence()),
														    getEmailMatchScore(article.getEmailEvidence()),
														    getGenderScore(article.getGenderEvidence()),
														    article.getGrantEvidenceTotalScore(), 
														    getJournalSubfieldScore(article.getJournalCategoryEvidence()),
														    getNameMatchScore(article.getAuthorNameEvidence(), AuthorNameEvidence::getNameMatchFirstScore),
														    getNameMatchScore(article.getAuthorNameEvidence(), AuthorNameEvidence::getNameMatchLastScore),
														    getNameMatchScore(article.getAuthorNameEvidence(), AuthorNameEvidence::getNameMatchMiddleScore),
														    getNameMatchScore(article.getAuthorNameEvidence(), AuthorNameEvidence::getNameMatchModifierScore),
														    getFeedbackScore(article.getOrganizationalEvidencesTotalScore()),
														    article.getRelationshipEvidence().getRelationshipPositiveMatchScore(),
														    article.getRelationshipEvidence().getRelationshipNegativeMatchScore(),
														    article.getRelationshipEvidence().getRelationshipIdentityCount(),
														    getNonTargetAuthorInstitutionalAffiliationScore(article.getAffiliationEvidence()),
														    getTargetAuthorAffiliationScore(article.getAffiliationEvidence()),
														    getPubmedTargetAuthorAffiliationScore(article.getAffiliationEvidence()),
														    article.getGoldStandard()==1? countAccepted-1 : countAccepted,  
														    article.getGoldStandard()==-1? countRejected-1: countRejected,
														    ((article.getGoldStandard()==1)? "ACCEPTED" : (article.getGoldStandard()==-1)? "REJECTED" :"PENDING"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

    }
    
	// Helper methods
	private static double getFeedbackScore(Double score) {
	    return Optional.ofNullable(score).orElse(0.0);
	}

	private static double getArticleCountScore(ArticleCountEvidence evidence) {
	    return Optional.ofNullable(evidence)
	            .map(ArticleCountEvidence::getArticleCountScore)
	            .orElse(0.0);
	}
	
	private static double getAuthorsCountScore(AuthorCountEvidence evidence)
	{
		return Optional.ofNullable(evidence)
	            .map(AuthorCountEvidence::getAuthorCountScore)
	            .orElse(0.0);
	}
	
	private static double getEducationYearScore(EducationYearEvidence evidence) {
	    return Optional.ofNullable(evidence)
	            .map(EducationYearEvidence::getDiscrepancyDegreeYearDoctoralScore)
	            .filter(score -> score != 0.0)  
	            .orElseGet(() -> Optional.ofNullable(evidence)
	                                      .map(EducationYearEvidence::getDiscrepancyDegreeYearBachelorScore)
	                                      .orElse(0.0));   
	}

	private static double getEmailMatchScore(EmailEvidence evidence) {
	    return Optional.ofNullable(evidence)
	            .map(EmailEvidence::getEmailMatchScore)
	            .orElse(0.0);
	}

	private static double getGenderScore(GenderEvidence evidence) {
	    return Optional.ofNullable(evidence)
	            .map(GenderEvidence::getGenderScoreIdentityArticleDiscrepancy)
	            .orElse(0.0);
	}

	private static double getJournalSubfieldScore(JournalCategoryEvidence evidence) {
	    return Optional.ofNullable(evidence)
	            .map(JournalCategoryEvidence::getJournalSubfieldScore)
	            .orElse(0.0);
	}

	private static double getNameMatchScore(AuthorNameEvidence evidence, Function<AuthorNameEvidence, Double> scoreFunction) {
	    return Optional.ofNullable(evidence)
	            .map(scoreFunction)
	            .orElse(0.0);
	}

	private static double getNonTargetAuthorInstitutionalAffiliationScore(AffiliationEvidence evidence) {
	    return Optional.ofNullable(evidence)
	            .map(AffiliationEvidence::getScopusNonTargetAuthorAffiliation)
	            .map(NonTargetAuthorScopusAffiliation::getNonTargetAuthorInstitutionalAffiliationScore)
	            .orElse(0.0);
	}

	private static double getTargetAuthorAffiliationScore(AffiliationEvidence evidence) {
	    return Optional.ofNullable(evidence)
	            .map(AffiliationEvidence::getScopusTargetAuthorAffiliation)
	            .map(affiliations -> affiliations.stream()
	                    .mapToDouble(TargetAuthorScopusAffiliation::getTargetAuthorInstitutionalAffiliationMatchTypeScore)
	                    .sum())
	            .orElse(0.0);
	}

	private static double getPubmedTargetAuthorAffiliationScore(AffiliationEvidence evidence) {
	    return Optional.ofNullable(evidence)
	            .map(AffiliationEvidence::getPubmedTargetAuthorAffiliation)
	            .map(TargetAuthorPubmedAffiliation::getTargetAuthorInstitutionalAffiliationMatchTypeScore)
	            .orElse(0.0);
	}
	
	
	private static List<ReCiterArticle> mapAuthorshipLikelihoodScore(List<ReCiterArticle> reCiterArticles, JSONArray authorshipLikelihoodScoreArray)
	{
		 return reCiterArticles.stream()
				 				 .filter(Objects::nonNull)
				 				 .map(article -> {
				 					 // Find the JSON object that corresponds to this article's ID
				 		        	ReCiterArticle reCiterArticle = findJSONObjectById(authorshipLikelihoodScoreArray, article);
				 		        	log.info("After setting the score to article***",reCiterArticle.getAuthorshipLikelihoodScore());
				 		            // count the targetAuthors per article
				 		        	 	long targetAuthorCount = article.getArticleCoAuthors().getAuthors().stream()
				 		                     .filter(ReCiterAuthor::isTargetAuthor)  // Filter target authors
				 		                     .count();  // Count them
				 		        	 	log.info("Article: " + article.getArticleId() + ", Target Author Count: " + targetAuthorCount);
				 		                 //if the targetAuthorCount is zero then impose the penality in the article authorshipLikelyhood score.
				 		                 article.setTargetAuthorCount(targetAuthorCount);
				 		                 if(targetAuthorCount == 0)
				 		                 {
				 		                	 double authorshipLikelyhoodScore = (strategyParameters.getTargetAuthorMissingPenaltyPercent() * (article.getAuthorshipLikelihoodScore()/100));
				 		                	 article.setAuthorshipLikelihoodScore(authorshipLikelyhoodScore);
				 		                	 article.setTargetAuthorCountPenalty(authorshipLikelyhoodScore - article.getAuthorshipLikelihoodScore());
				 		                 }
				 		                 else if (reCiterArticle == null) {
					 		            	article.setAuthorshipLikelihoodScore(0.0);
					 		            }
				 		            return article; 
				 				 })
						         .filter(Objects::nonNull) // Filter out null values returned from findJSONObjectById
						         .collect(Collectors.toList()); // Collect the results if needed, or just perform the mapping
	}
	// Helper method to find JSONObject by article
	private static ReCiterArticle findJSONObjectById(JSONArray jsonArray, ReCiterArticle article) {
	    for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject jsonObject = jsonArray.getJSONObject(i);
	        if (jsonObject.getLong("id") == article.getArticleId()) {
	        	log.info("both articleIds are matching and Score is ***",jsonObject.getDouble("scoreTotal"));
	        	article.setAuthorshipLikelihoodScore(jsonObject.getDouble("scoreTotal")*100);
	        	log.info("After setting the score to article***",article.getAuthorshipLikelihoodScore());
	            return article; // Return the modified article
	        }
	    }
	    if(article!=null)
	    	article.setAuthorshipLikelihoodScore(0.0);
	    return article; // Return null if not found
	}
	private boolean uploadJsonFileIntoS3(String keyName,File file)
	{
		String FeedbackScoreBucketName = PropertiesUtils.get("aws.s3.feedback.score.bucketName");
        
		// Upload the python file
        try {
        	
        	final AmazonS3 s3 = AmazonS3ClientBuilder
					.standard()
					.withCredentials(new DefaultAWSCredentialsProviderChain())
					.withRegion(System.getenv("AWS_REGION"))
					.build();
        	
			if(s3.doesBucketExistV2(FeedbackScoreBucketName)) 
			{												
	        	log.info("Uploading files to S3 bucket ",FeedbackScoreBucketName);
	        	PutObjectRequest putObjectRequest = new PutObjectRequest(FeedbackScoreBucketName.toLowerCase(), keyName, file);
	       
	        	// Optionally, set metadata
	            ObjectMetadata metadata = new ObjectMetadata();
	            metadata.setContentType("application/json");
	            putObjectRequest.setMetadata(metadata);
	
	            
	            try{
					s3.putObject(putObjectRequest);
				    log.info("CSV file uploaded successfully to S3 bucket: " + FeedbackScoreBucketName);
					return true;   
				}
				catch(AmazonServiceException e) {
					// The call was transmitted successfully, but Amazon S3 couldn't process 
		            // it, so it returned an error response.
					log.error(e.getErrorMessage());
					 return false;
				}
			}
        	else {
        		log.error("S3 bucket does not exist: " + FeedbackScoreBucketName);
                return false;
        	}
        
        } catch (Exception e) {
            e.printStackTrace();
			return false;			 
        }
	}

}
