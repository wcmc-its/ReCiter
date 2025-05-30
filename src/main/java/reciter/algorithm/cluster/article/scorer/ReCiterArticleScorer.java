package reciter.algorithm.cluster.article.scorer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.algorithm.evidence.author.authorcount.AuthorCountStrategyContext;
import reciter.algorithm.evidence.author.authorcount.strategy.AuthorCountStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.AffiliationStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.strategy.CommonAffiliationStrategy;
import reciter.algorithm.evidence.targetauthor.articlesize.ArticleSizeStrategyContext;
import reciter.algorithm.evidence.targetauthor.articlesize.strategy.ArticleSizeStrategy;
import reciter.algorithm.evidence.targetauthor.degree.DegreeStrategyContext;
import reciter.algorithm.evidence.targetauthor.degree.strategy.DegreeType;
import reciter.algorithm.evidence.targetauthor.degree.strategy.YearDiscrepancyStrategy;
import reciter.algorithm.evidence.targetauthor.department.DepartmentStrategyContext;
import reciter.algorithm.evidence.targetauthor.department.strategy.DepartmentStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.email.EmailStrategyContext;
import reciter.algorithm.evidence.targetauthor.email.strategy.EmailStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.gender.GenderStrategyContext;
import reciter.algorithm.evidence.targetauthor.gender.strategy.GenderStrategy;
import reciter.algorithm.evidence.targetauthor.grant.GrantStrategyContext;
import reciter.algorithm.evidence.targetauthor.grant.strategy.GrantStrategy;
import reciter.algorithm.evidence.targetauthor.journalcategory.JournalCategoryStrategyContext;
import reciter.algorithm.evidence.targetauthor.journalcategory.strategy.JournalCategoryStrategy;
import reciter.algorithm.evidence.targetauthor.knownrelationship.KnownRelationshipStrategyContext;
import reciter.algorithm.evidence.targetauthor.knownrelationship.strategy.KnownRelationshipStrategy;
import reciter.algorithm.evidence.targetauthor.name.ScoreByNameStrategyContext;
import reciter.algorithm.evidence.targetauthor.name.strategy.ScoreByNameStrategy;
import reciter.algorithm.evidence.targetauthor.persontype.PersonTypeStrategyContext;
import reciter.algorithm.evidence.targetauthor.persontype.strategy.PersonTypeStrategy;
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

/**
 * @author szd2013
 * This class will calculate scores based on https://docs.google.com/spreadsheets/d/1p-AIQOzFCFaGiIGsDR2ch7wJw1BFysIhLmsg7nGh-I0/
 */
public class ReCiterArticleScorer extends AbstractArticleScorer {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterArticleScorer.class);

	
	/**
	 * Email Strategy.
	 */
	private StrategyContext emailStrategyContext;
	
	/**
	 * Name Strategy.
	 */
	private StrategyContext nameStrategyContext;

	/**
	 * Department Strategy.
	 */
	private StrategyContext departmentStringMatchStrategyContext;

	/**
	 * Known co-investigator strategy context.
	 */
	private StrategyContext knownRelationshipsStrategyContext;

	/**
	 * Affiliation strategy context.
	 */
	private StrategyContext affiliationStrategyContext;

	/** Individual article selection strategy contexts. */
	/**
	 * Scopus strategy context.
	 */
	private StrategyContext scopusCommonAffiliationStrategyContext;

	/**
	 * Coauthor strategy context.
	 */
	private StrategyContext coauthorStrategyContext;

	/**
	 * Journal strategy context.
	 */
	private StrategyContext journalStrategyContext;

	/**
	 * Citizenship strategy context.
	 */
	private StrategyContext citizenshipStrategyContext;

	/**
	 * Year Discrepancy (Bachelors).
	 */
	private StrategyContext bachelorsYearDiscrepancyStrategyContext;

	/**
	 * Year Discrepancy (Doctoral).
	 */
	private StrategyContext doctoralYearDiscrepancyStrategyContext;

	/**
	 * Discounts Articles not in English.
	 */
	private StrategyContext articleTitleInEnglishStrategyContext;
	
	/**
	 * Education.
	 */
	private StrategyContext educationStrategyContext;

	/**
	 * Remove article if the full first name doesn't match.
	 */
	private StrategyContext removeByNameStrategyContext;
	
	/**
	 * Journal Category Score
	 */
	private StrategyContext journalCategoryStrategyContext;

	/**
	 * Article size.
	 */
	private StrategyContext articleSizeStrategyContext;
	
	/**
	 * Person Type.
	 */
	private StrategyContext personTypeStrategyContext;
	
	/**
	 * Accepted Rejected .
	 */
	private StrategyContext acceptedRejectedStrategyContext;
	
	/**
	 * Gender Strategy
	 */
	private GenderStrategyContext genderStrategyContext;

	//	private StrategyContext boardCertificationStrategyContext;
	//
	//	private StrategyContext degreeStrategyContext;
	
	private StrategyContext grantStrategyContext;
	
	private StrategyContext citationStrategyContext;
	
	private StrategyContext coCitationStrategyContext;
	
	private StrategyContext authorCountStrategyContext;
	
	private List<StrategyContext> strategyContexts;

	public static StrategyParameters strategyParameters;
	
	ExecutorService executorService = Executors.newWorkStealingPool(13);
	
	public ReCiterArticleScorer(List<ReCiterArticle> reCiterArticles, Identity identity, StrategyParameters strategyParameters) {
		
		ReCiterArticleScorer.strategyParameters = strategyParameters;
		
		// Strategies that select clusters that are similar to the target author.
		this.emailStrategyContext = new EmailStrategyContext(new EmailStringMatchStrategy());
		this.nameStrategyContext = new ScoreByNameStrategyContext(new ScoreByNameStrategy());
		this.departmentStringMatchStrategyContext = new DepartmentStrategyContext(new DepartmentStringMatchStrategy());
		this.journalCategoryStrategyContext = new JournalCategoryStrategyContext(new JournalCategoryStrategy());
		this.knownRelationshipsStrategyContext = new KnownRelationshipStrategyContext(new KnownRelationshipStrategy());
		this.affiliationStrategyContext = new AffiliationStrategyContext(new CommonAffiliationStrategy());
		this.genderStrategyContext = new GenderStrategyContext(new GenderStrategy());

		// Using the following strategy contexts in sequence to reassign individual articles
		// to selected clusters.
		this.grantStrategyContext = new GrantStrategyContext(new GrantStrategy());
	
		//ArticleCountScore
		this.articleSizeStrategyContext = new ArticleSizeStrategyContext(new ArticleSizeStrategy(reCiterArticles.size()));//numArticles));
		this.personTypeStrategyContext = new PersonTypeStrategyContext(new PersonTypeStrategy());


		this.bachelorsYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.BACHELORS));
		this.doctoralYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.DOCTORAL));
		
		this.authorCountStrategyContext = new AuthorCountStrategyContext(new AuthorCountStrategy(ReCiterArticleScorer.strategyParameters));

		this.strategyContexts = new ArrayList<StrategyContext>();
		
		if (strategyParameters.isGrant()) {
			this.strategyContexts.add(this.grantStrategyContext);
		}
		
		if (strategyParameters.isArticleSize()) {
			this.strategyContexts.add(this.articleSizeStrategyContext);
		}

		if (strategyParameters.isBachelorsYearDiscrepancy()) {
			this.strategyContexts.add(this.bachelorsYearDiscrepancyStrategyContext);
		}
		
		if (strategyParameters.isDoctoralYearDiscrepancy()) {
			this.strategyContexts.add(this.doctoralYearDiscrepancyStrategyContext);
		}
		
		if(strategyParameters.isPersonType()) {
			this.strategyContexts.add(this.personTypeStrategyContext);
		}

		// Re-run these evidence types (could have been removed or not processed in sequence).
		this.strategyContexts.add(this.emailStrategyContext);
		this.strategyContexts.add(this.authorCountStrategyContext);
	}
	

	@Override
	public void runArticleScorer(List<ReCiterArticle> reCiterArticles, Identity identity) {
		
		List<Future<?>> futures = new ArrayList<>();
		
		
		//((TargetAuthorStrategyContext) nameStrategyContext).executeStrategy(reCiterArticles, identity);
		futures.add(submitAndLogTime("name Category", executorService, nameStrategyContext, reCiterArticles, identity));

		if (strategyParameters.isEmail()) {
			//((TargetAuthorStrategyContext) emailStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("Email Category", executorService, emailStrategyContext, reCiterArticles, identity));
		}
		
		if (strategyParameters.isGrant()) {
			//((TargetAuthorStrategyContext) grantStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("Grant Category", executorService, grantStrategyContext, reCiterArticles, identity));
		}
		
		if (strategyParameters.isKnownRelationship()) {
			//((TargetAuthorStrategyContext) knownRelationshipsStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("KnownRelationships Category", executorService, knownRelationshipsStrategyContext, reCiterArticles, identity));
		}
		
		if (strategyParameters.isBachelorsYearDiscrepancy()) {
			//((RemoveReCiterArticleStrategyContext) bachelorsYearDiscrepancyStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("bachelorsYearDiscrepancy Category", executorService, bachelorsYearDiscrepancyStrategyContext, reCiterArticles, identity));
		}
		
		if (strategyParameters.isDoctoralYearDiscrepancy()) {
			//((RemoveReCiterArticleStrategyContext) doctoralYearDiscrepancyStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("doctoralYearDiscrepancy Category", executorService, doctoralYearDiscrepancyStrategyContext, reCiterArticles, identity));
		}

		if (strategyParameters.isDepartment()) {
			//((TargetAuthorStrategyContext) departmentStringMatchStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("departmentStringMatch Category", executorService, departmentStringMatchStrategyContext, reCiterArticles, identity));
		}
		
		if(strategyParameters.isJournalCategory()) {
			//((TargetAuthorStrategyContext) journalCategoryStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("journalCategory Category", executorService, journalCategoryStrategyContext, reCiterArticles, identity));
		}
		
		if (strategyParameters.isAffiliation()) {
			//((TargetAuthorStrategyContext)affiliationStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("affiliation Category", executorService, affiliationStrategyContext, reCiterArticles, identity));
		}
		
		if (strategyParameters.isArticleSize()) {
			//((TargetAuthorStrategyContext) articleSizeStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("articleSize Category", executorService, articleSizeStrategyContext, reCiterArticles, identity));
		}
		
		if (strategyParameters.isPersonType()) {
			//((TargetAuthorStrategyContext) personTypeStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("personType Category", executorService, personTypeStrategyContext, reCiterArticles, identity));
		}
		futures.add(submitAndLogTime("authorCount Category", executorService, authorCountStrategyContext, reCiterArticles, identity));
		
		
		if(strategyParameters.isGender()) {
			//((TargetAuthorStrategyContext) genderStrategyContext).executeStrategy(reCiterArticles, identity);
			futures.add(submitAndLogTime("gender Category", executorService, genderStrategyContext, reCiterArticles, identity));
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
            	slf4jLogger.error("Task execution interrupted or encountered an error", e);
                allTasksCompleted=false;
            }
        }
        if (allTasksCompleted) {
        	slf4jLogger.info("All Idnetity score strategy contexts have been completed successfully.");
	    } else {
	    	slf4jLogger.error("One or more tasks failed; report generation may be incomplete.");
        }
		
	}
	public List<ReCiterArticle> executePythonScriptForArticleIdentityTotalScore(List<ReCiterArticle> reCiterArticles, Identity identity) {
	    
		slf4jLogger.info("articles Size :", reCiterArticles.size());
   	
    	List<ReCiterArticleFeedbackIdentityScore> articleIdentityScore = reCiterArticles.stream()
																		    		    .map(article -> {
																		    		        ReCiterArticleFeedbackIdentityScore score = mapToIdentityScore(article);
																		    		        return score;
																		    		    })
																		    		    .filter(Objects::nonNull) // Optionally filter out nulls
																		    		    .collect(Collectors.toList());
    	
    	
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	// Define a DateTimeFormatter for safe file name format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

		// Get current date and time
		LocalDateTime now = LocalDateTime.now();

		// Format the current date and time to a safe string for file names
		String timestamp = now.format(formatter);

		String fileName = StringUtils.join(identity.getUid(), "-identityOnlyScoringInput.json");
		boolean isS3UploadRequired = isS3UploadRequired();
		String identityS3BucketName = PropertiesUtils.get("aws.s3.feedback.score.bucketName");
		
        try {
			NeuralNetworkModelArticlesScorer nnmodel = new NeuralNetworkModelArticlesScorer();																			   
        	  if(isS3UploadRequired) 
        	  {
        		  File jsonFile = new File(fileName);

        		// Write the User object to the JSON file
                  objectMapper.writeValue(jsonFile, articleIdentityScore);
                  uploadJsonFileIntoS3(fileName, jsonFile);
         	  }
        	  else
        	  {	  
        		  File jsonFile = new File("src/main/resources/scripts/"+fileName);
	        	  objectMapper.writeValue(jsonFile,articleIdentityScore);
        	  }
              String isS3UploadRequiredString = Boolean.toString(isS3UploadRequired);
 			  JSONArray articlesIdentityScoreTotal = nnmodel.executeArticleScorePredictor("Identity Score", "identityOnlyScoreArticles.py",fileName,identityS3BucketName,isS3UploadRequiredString);
 			  if(articlesIdentityScoreTotal!=null && articlesIdentityScoreTotal.length() > 0 )
					  return mapAuthorshipLikelihoodScore(reCiterArticles, articlesIdentityScoreTotal);
 		
       

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       return null;
   }
    private static ReCiterArticleFeedbackIdentityScore mapToIdentityScore(ReCiterArticle article) {
    	
        try {
	    		return new ReCiterArticleFeedbackIdentityScore(
															    article.getArticleId(),
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

	 // Function to calculate likelihood adjustment
    private static Function<Double, Double> calculateLikelihoodAdjustment = authorCount -> {
        // Baseline likelihood (at authorCountThreshold)
        double y_baseline = strategyParameters.getInCoefficent() * Math.log(strategyParameters.getAuthorCountThreshold()) + strategyParameters.getConstantCoefficeint();
        // Likelihood for the given author count
        double y = authorCount > 0 ? strategyParameters.getInCoefficent() * Math.log(authorCount) + strategyParameters.getConstantCoefficeint() : y_baseline;
        // Adjustment is scaled by gamma
        return strategyParameters.getAuthorCountAdjustmentGamma() * (y - y_baseline);
    };

    // Function to calculate adjusted article count score
    private static Function<Double, Double> calculateAdjustedArticleCountScore = authorCount -> {
        // Apply the likelihood adjustment function
        return calculateLikelihoodAdjustment.apply(authorCount);
    };
	
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
        .filter(Objects::nonNull)  // Make sure the article is not null
        .map(article -> {
            // Find the JSON object that corresponds to this article's ID
        	ReCiterArticle reCiterArticle = findJSONObjectById(authorshipLikelihoodScoreArray, article);
            // count the targetAuthors per article
        	 	long targetAuthorCount = article.getArticleCoAuthors().getAuthors().stream()
                     .filter(ReCiterAuthor::isTargetAuthor)  // Filter target authors
                     .count();  // Count them
        	 	slf4jLogger.info("Article: " + article.getArticleId() + ", Target Author Count: " + targetAuthorCount);
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
            return article;  // Return the article with updated score
        })
        .collect(Collectors.toList());  // Collect updated articles into a list
 
	}
 	
 	
	// Helper method to find JSONObject by article
	private static ReCiterArticle findJSONObjectById(JSONArray jsonArray, ReCiterArticle article) {
	    for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject jsonObject = jsonArray.getJSONObject(i);
	        if (jsonObject.getLong("id") == article.getArticleId()) {
	        	article.setAuthorshipLikelihoodScore(jsonObject.optDouble("scoreTotal",0.0)*100);
	            return article; // Return the modified article
	        }

	    }
	    return article; // Return null if not found
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
        	
	        	slf4jLogger.info("Uploading files to S3 bucket ",FeedbackScoreBucketName);
	        	PutObjectRequest putObjectRequest = new PutObjectRequest(FeedbackScoreBucketName.toLowerCase(), keyName, file);
	       
	        	// Optionally, set metadata
	            ObjectMetadata metadata = new ObjectMetadata();
	            metadata.setContentType("application/json");
	            putObjectRequest.setMetadata(metadata);
	
	            
	            try{
					s3.putObject(putObjectRequest);
					slf4jLogger.info("CSV file uploaded successfully to S3 bucket: " + FeedbackScoreBucketName);
				}
				catch(AmazonServiceException e) {
					// The call was transmitted successfully, but Amazon S3 couldn't process 
		            // it, so it returned an error response.
					slf4jLogger.error(e.getErrorMessage());
					 return false;
				}
	            return true;
			}
        	else 
        	{
        		slf4jLogger.error("S3 bucket does not exist: " + FeedbackScoreBucketName);
                return false;
        	}
        
        } catch (Exception e) {
            e.printStackTrace();
			return false;			 
        }
 	}
	private Future<?> submitAndLogTime(String category, ExecutorService executorService,
			StrategyContext context,List<ReCiterArticle> reCiterArticles, Identity identity) 
	{

		return executorService.submit(() -> {
		StopWatch stopWatch = new StopWatch(category);
		stopWatch.start(category);
		if(context instanceof RemoveReCiterArticleStrategyContext)
			((RemoveReCiterArticleStrategyContext)context).executeStrategy(reCiterArticles, identity);
		else
			((TargetAuthorStrategyContext)context).executeStrategy(reCiterArticles, identity);
		stopWatch.stop();
		slf4jLogger.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
		});
	}
}