package reciter.algorithm.feedback.article.scorer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StopWatch;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.acceptedrejected.AcceptedRejectedStrategyContext;
import reciter.algorithm.evidence.article.acceptedrejected.strategy.AcceptedRejectedStrategy;
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
import reciter.database.dynamodb.DynamoDbS3Operations;
import reciter.engine.EngineParameters;
import reciter.engine.StrategyParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public class ReciterFeedbackArticleScorer extends AbstractFeedbackArticleScorer {

	private static final Logger log = LoggerFactory.getLogger(ReciterFeedbackArticleScorer.class);
	private List<ReCiterArticle> reciterArticles;
	private Identity identity;
	public static StrategyParameters strategyParameters;
	/**
	 * Journal Category Score
	 */
	private StrategyContext journalStrategyContext;
	//private StrategyContext journalDomainStrategyContext;
	//private StrategyContext journalFieldStrategyContext;
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

	private Properties properties = new Properties();

	
	ExecutorService executorService = Executors.newFixedThreadPool(12);
	
	public ReciterFeedbackArticleScorer(List<ReCiterArticle> articles,Identity identity,EngineParameters parameters,StrategyParameters strategyParameters)
	{
		ReciterFeedbackArticleScorer.strategyParameters = strategyParameters;
		this.reciterArticles = articles;
		this.identity = identity;
		this.journalStrategyContext = new JournalFeedbackStrategyContext(new JournalFeedbackStrategy());
		//this.journalDomainStrategyContext = new JournalDomainFeedbackStrategyContext(new JournalDomainFeedbackStrategy());
		//this.journalFieldStrategyContext = new JournalFieldFeedbackStrategyContext(new JournalFieldFeedbackStrategy());
		this.journalSubFieldStrategyContext = new JournalSubFieldFeedbackStrategyContext(new JournalSubFieldFeedbackStrategy());
		this.orcidStrategyContext = new OrcidFeedbackStrategyContext(new OrcidFeedbackStrategy());
		this.yearStrategyContext = new YearFeedbackStrategyContext(new YearFeedbackStrategy());
		this.targetAuthorNameStrategyContext = new TargetAuthorNameFeedbackStrategyContext(new TargetAuthorNameFeedbackStrategy());
		this.organizationStrategyContext = new OrganizationFeedbackStrategyContext(new OrganizationFeedbackStrategy());
		this.orcidCoAuthorStrategyContext = new OrcidCoauthorFeedbackStrategyContext(new OrcidCoauthorFeedbackStrategy());
		this.keywordStrategyContext = new KeywordFeedbackStrategyContext(new KeywordFeedbackStrategy());
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
		/*if(strategyParameters.isFeedbackScoreJournalDomain()) {
			futures.add(submitAndLogTime("Journal Domain Category", executorService, journalDomainStrategyContext, reCiterArticles, identity));
		}*/
		/*if(strategyParameters.isFeedbackScoreJournalField()) {
			futures.add(submitAndLogTime("Journal Field Category", executorService, journalFieldStrategyContext, reCiterArticles, identity));

		}*/
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
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
		
	
		// Get current date and time
		LocalDateTime now = LocalDateTime.now();
		
		// Format the current date and time to a safe string for file names
		String timestamp = now.format(formatter);

		String[] csvHeaders = { "PersonIdentifier","Pmid","userAssertion","scoreCites","scoreCoAuthorName","scoreEmail",
    		 	"scoreInstitution","scoreJournal","scoreJournalSubField","scoreKeyword","scoreOrcid","scoreOrcidCoAuthor",
    		 	"scoreOrganization","scoreTargetAuthorName","scoreYear" };
		
		Path filePath = Paths.get(timestamp + "-" + personIdentifier + "-feedbackScoring-consolidated.csv");
		
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
			//	ddbs3.saveLargeItem((String bucketName, Object object, String keyName));;
				log.warn("Uploading CSV into S3 starts here******************",outputStream.toString(StandardCharsets.UTF_8.name()),filePath.toString());
				uploadCsvToS3(outputStream.toString(StandardCharsets.UTF_8.name()),filePath.toString());
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
		// Define a DateTimeFormatter for safe file name format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

		// Get current date and time
		LocalDateTime now = LocalDateTime.now();

		// Format the current date and time to a safe string for file names
		String timestamp = now.format(formatter);

		String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
				"subscoreType1", "subscoreValue", "subScoreIndividualScore","UserAssertion" };
		
		Path filePath = Paths.get(timestamp + "-" + personIdentifier + "-feedbackScoring-itemLevel.csv");
		log.warn("Flags**************************",isS3UploadRequired());

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
				uploadCsvToS3(outputStream.toString(StandardCharsets.UTF_8.name()),filePath.toString());
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	private void uploadCsvToS3(String csvContent,String fileName) {
       
	
		String FeedbackScoreBucketName = getProperty("aws.s3.feedback.score.bucketName");
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
        	
        	log.info("Uploading files to S3 bucket ",FeedbackScoreBucketName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(FeedbackScoreBucketName.toLowerCase(), fileName, inputStream, metadata);
            try{
				s3.putObject(putObjectRequest);
			    log.info("CSV file uploaded successfully to S3 bucket: " + FeedbackScoreBucketName);
			}
			catch(AmazonServiceException e) {
				// The call was transmitted successfully, but Amazon S3 couldn't process 
	            // it, so it returned an error response.
				log.error(e.getErrorMessage());
			}
        
        } catch (Exception e) {
            e.printStackTrace();
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
	
	private Properties PropertiesLoader(String fileName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + fileName);
                return null;
            }
            // Load the properties file
            properties.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return properties;
    }

    private String getProperty(String key) {
        return properties.getProperty(key);
    }

   
    
    private boolean isS3UploadRequired()
    {
    	  	  
  		 Properties properties = PropertiesLoader("application.properties");

         // Retrieve properties
         String awsS3Use = properties.getProperty("aws.s3.use");
         boolean isS3Use = Boolean.parseBoolean(awsS3Use);
         String dynamoDDLocal = properties.getProperty("aws.dynamoDb.local");
         boolean isDynamoDBLocal = Boolean.parseBoolean(dynamoDDLocal);
         if(isS3Use && !isDynamoDBLocal) 
        	 return true;
    	return false;
    }
    
    
    
}
