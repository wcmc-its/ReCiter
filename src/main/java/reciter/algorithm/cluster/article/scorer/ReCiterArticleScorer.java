package reciter.algorithm.cluster.article.scorer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import reciter.storage.s3.AwsScoringClients;
import reciter.utils.PropertiesUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * @author szd2013
 * This class will calculate scores based on https://docs.google.com/spreadsheets/d/1p-AIQOzFCFaGiIGsDR2ch7wJw1BFysIhLmsg7nGh-I0/
 */
public class ReCiterArticleScorer extends AbstractArticleScorer {
	
	private static final String AWS_DYNAMO_DB_LOCAL = "aws.dynamoDb.local";


	private static final String AWS_S3_USE = "aws.s3.use";


	private static final String APPLICATION_JSON = "application/json";


	private static final String AWS_S3_FEEDBACK_SCORE_BUCKET_NAME = "aws.s3.feedback.score.bucketName";


	private static final Logger log = LoggerFactory.getLogger(ReCiterArticleScorer.class);
	

	// ── Java 21: reusable ObjectMapper (thread-safe, expensive to construct) ──
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

	/**
	 * Year Discrepancy (Doctoral or bachelors).
	 */

	private StrategyContext educationYearDiscrepancyStrategyContext;

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
	 * Gender Strategy
	 */
	private GenderStrategyContext genderStrategyContext;

	/**
	 * Grant Strategy
	 */
	private StrategyContext grantStrategyContext;

	/**
	 * Author Count Strategy
	 */
	private StrategyContext authorCountStrategyContext;
	
	private List<StrategyContext> strategyContexts;

	public static StrategyParameters strategyParameters;
	
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
		this.grantStrategyContext = new GrantStrategyContext(new GrantStrategy());
		this.articleSizeStrategyContext = new ArticleSizeStrategyContext(new ArticleSizeStrategy(reCiterArticles.size()));
		this.personTypeStrategyContext = new PersonTypeStrategyContext(new PersonTypeStrategy());
	    this.educationYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy());
		this.authorCountStrategyContext = new AuthorCountStrategyContext(new AuthorCountStrategy(ReCiterArticleScorer.strategyParameters));
		
		this.strategyContexts = new ArrayList<StrategyContext>();
		
		if (strategyParameters.isGrant()) {
			this.strategyContexts.add(this.grantStrategyContext);
		}
		if (strategyParameters.isArticleSize()) {
			this.strategyContexts.add(this.articleSizeStrategyContext);
		}
		if (strategyParameters.isEducationYearDiscrepancy()) {
			this.strategyContexts.add(this.educationYearDiscrepancyStrategyContext);
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
		
		 try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			 
			     var futures = new ArrayList<CompletableFuture<Void>>();
				
				futures.add(runStrategy("name Category", nameStrategyContext, reCiterArticles, identity,executor));
				
				if (strategyParameters.isEmail()) 
					futures.add(runStrategy("Email Category", emailStrategyContext, reCiterArticles, identity, executor));
				if (strategyParameters.isGrant()) 
					futures.add(runStrategy("Grant Category", grantStrategyContext, reCiterArticles, identity,executor));
				if (strategyParameters.isKnownRelationship()) 
					futures.add(runStrategy("KnownRelationships Category", knownRelationshipsStrategyContext, reCiterArticles, identity,executor));
				if (strategyParameters.isEducationYearDiscrepancy()) 
					futures.add(runStrategy("educationYearDiscrepancy Category", educationYearDiscrepancyStrategyContext, reCiterArticles, identity,executor));
				if (strategyParameters.isDepartment()) 
					futures.add(runStrategy("departmentStringMatch Category", departmentStringMatchStrategyContext, reCiterArticles, identity,executor));
				if (strategyParameters.isJournalCategory()) 
					futures.add(runStrategy("journalCategory Category", journalCategoryStrategyContext, reCiterArticles, identity,executor));
				if (strategyParameters.isAffiliation()) 
					futures.add(runStrategy("affiliation Category", affiliationStrategyContext, reCiterArticles, identity,executor));
				if (strategyParameters.isArticleSize()) 
					futures.add(runStrategy("articleSize Category", articleSizeStrategyContext, reCiterArticles, identity,executor));
				if (strategyParameters.isPersonType()) 
					futures.add(runStrategy("personType Category", personTypeStrategyContext, reCiterArticles, identity,executor));
				futures.add(runStrategy("authorCount Category", authorCountStrategyContext, reCiterArticles, identity,executor));
				if(strategyParameters.isGender()) 
					futures.add(runStrategy("gender Category", genderStrategyContext, reCiterArticles, identity,executor));
				
				// Wait for ALL strategies to complete, then log overall result
		        CompletableFuture
		                .allOf(futures.toArray(new CompletableFuture[0]))
		                .whenComplete((result, ex) -> {
		                    if (ex != null) {
		                        log.error("One or more scoring tasks failed; report may be incomplete.", ex);
		                    } else {
		                        log.info("All Identity score strategy contexts completed successfully.");
		                    }
		                })
		                .join(); // blocks calling thread until all tasks finish

		    } // executor.close() called here automatically — no manual shutdown() needed
		}
		
		
	public List<ReCiterArticle> executePythonScriptForArticleIdentityTotalScore(List<ReCiterArticle> reCiterArticles, Identity identity) {
	    
		log.info("articles Size :", reCiterArticles.size());
   	
		List<ReCiterArticleFeedbackIdentityScore> articleIdentityScore = reCiterArticles.parallelStream()
				.map(ReCiterArticleScorer::mapToIdentityScore).filter(Objects::nonNull) // Optionally filter out nulls
				.collect(Collectors.toList());
    	
    	
    	ObjectMapper objectMapper = new ObjectMapper();

    	// Enrich scores with identity names and per-article name evidence for Python scoring
    	String identityFirstName = (identity.getPrimaryName() != null && identity.getPrimaryName().getFirstName() != null)
    	        ? identity.getPrimaryName().getFirstName() : "";
    	String identityMiddleName = (identity.getPrimaryName() != null && identity.getPrimaryName().getMiddleName() != null)
    	        ? identity.getPrimaryName().getMiddleName() : "";

    	// Build articleId → article map for per-article name evidence enrichment
    	Map<Long, ReCiterArticle> articleByIdMap = reCiterArticles.stream()
    	        .collect(Collectors.toMap(ReCiterArticle::getArticleId, Function.identity(), (a, b) -> a));

    	List<ObjectNode> enrichedScores = articleIdentityScore.stream()
    	        .map(score -> {
    	            ObjectNode node = objectMapper.convertValue(score, ObjectNode.class);
    	            node.put("identityFirstName", identityFirstName);
    	            node.put("identityMiddleName", identityMiddleName);

    	            // Per-article name evidence fields
    	            ReCiterArticle article = articleByIdMap.get(score.getArticleId());
    	            if (article != null && article.getAuthorNameEvidence() != null) {
    	                AuthorNameEvidence ev = article.getAuthorNameEvidence();
    	                node.put("articleAuthorFirstName",
    	                    ev.getArticleAuthorName() != null && ev.getArticleAuthorName().getFirstName() != null
    	                    ? ev.getArticleAuthorName().getFirstName() : "");
    	                node.put("nameMatchFirstType",
    	                    ev.getNameMatchFirstType() != null ? ev.getNameMatchFirstType() : "");
    	                node.put("nameMatchMiddleType",
    	                    ev.getNameMatchMiddleType() != null ? ev.getNameMatchMiddleType() : "");
    	            }
    	            return node;
    	        })
    	        .collect(Collectors.toList());


		String fileName = StringUtils.join(identity.getUid(), "-identityOnlyScoringInput.json");
		boolean isS3UploadRequired = isS3UploadRequired();
		String identityS3BucketName = PropertiesUtils.get(AWS_S3_FEEDBACK_SCORE_BUCKET_NAME);

        try {
			NeuralNetworkModelArticlesScorer nnmodel = new NeuralNetworkModelArticlesScorer();
        	  if(isS3UploadRequired)
        	  {
        		  File jsonFile = new File(fileName);

        		// Write the User object to the JSON file
        		  OBJECT_MAPPER.writeValue(jsonFile, enrichedScores);
                  uploadJsonFileIntoS3(fileName, jsonFile);
         	  }
        	  else
        	  {
        		  File jsonFile = new File("src/main/resources/scripts/"+fileName);
        		  OBJECT_MAPPER.writeValue(jsonFile, enrichedScores);
        	  }
              String isS3UploadRequiredString = Boolean.toString(isS3UploadRequired);
 			  JSONArray articlesIdentityScoreTotal = nnmodel.executeArticleScorePredictor("identity", fileName,identityS3BucketName,isS3UploadRequiredString);
 			  if(articlesIdentityScoreTotal!=null && articlesIdentityScoreTotal.length() > 0 )
					  return mapAuthorshipLikelihoodScore(reCiterArticles, articlesIdentityScoreTotal);
 		
       

		} catch (IOException e) {
			log.error("Failed to write/upload identity scoring input or invoke scorer for uid={}, file={}", identity.getUid(), fileName, e);
			throw new RuntimeException("Identity scoring failed for uid=" + identity.getUid(), e);
		}
       return null;
   }
    private static ReCiterArticleFeedbackIdentityScore mapToIdentityScore(ReCiterArticle article) {
    	
        try {
			String goldStandardLabel = switch (article.getGoldStandard()) {
			case 1 -> "ACCEPTED";
			case -1 -> "REJECTED";
			default -> "PENDING";
			};
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
															    (article.getRelationshipEvidence() == null ? 0.0 : article.getRelationshipEvidence().getRelationshipPositiveMatchScore()),
															    (article.getRelationshipEvidence() == null ? 0.0 : article.getRelationshipEvidence().getRelationshipNegativeMatchScore()),
															    (article.getRelationshipEvidence() == null ? 0L : article.getRelationshipEvidence().getRelationshipIdentityCount()),
															    getNonTargetAuthorInstitutionalAffiliationScore(article.getAffiliationEvidence()),
															    getTargetAuthorAffiliationScore(article.getAffiliationEvidence()),
															    getPubmedTargetAuthorAffiliationScore(article.getAffiliationEvidence()),
															    goldStandardLabel);
		} catch (Exception e) {
			log.error("Failed to map identity score for articleId={}; article will be dropped from scoring", article.getArticleId(), e);
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
        double y_baseline = strategyParameters.getLnCoefficient() * Math.log(strategyParameters.getAuthorCountThreshold()) + strategyParameters.getConstantCoefficient();
        // Likelihood for the given author count
        double y = authorCount > 0 ? strategyParameters.getLnCoefficient() * Math.log(authorCount) + strategyParameters.getConstantCoefficient() : y_baseline;
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
 		Map<Long, Double> scoreMap = buildScoreMap(authorshipLikelihoodScoreArray);
 		return reCiterArticles.parallelStream()
        .filter(Objects::nonNull)
        .map(article -> {
            // Look up score from pre-built map (O(1) instead of O(n))
            article.setAuthorshipLikelihoodScore(scoreMap.getOrDefault(article.getArticleId(), 0.0));
            // count the targetAuthors per article
        	 	long targetAuthorCount = article.getArticleCoAuthors().getAuthors().stream()
                     .filter(ReCiterAuthor::isTargetAuthor)  // Filter target authors
                     .count();  // Count them
        	 	//enable if requrired for debugging
        	 	//slf4jLogger.info("Article: " + article.getArticleId() + ", Target Author Count: " + targetAuthorCount);
                 //if the targetAuthorCount is zero then impose the penality in the article authorshipLikelyhood score.
                 article.setTargetAuthorCount(targetAuthorCount);
                 if(targetAuthorCount == 0)
                 {
                	// FIX (#640-C): capture the ORIGINAL score before overwriting it. Previously
                	 // the penalty was computed AFTER setAuthorshipLikelihoodScore, so it read back
                	 // the new value and the delta was always x-x=0.
                	 double originalAuthorshipLikelihoodScore = article.getAuthorshipLikelihoodScore();
                	 double authorshipLikelyhoodScore = (strategyParameters.getTargetAuthorMissingPenaltyPercent() * (originalAuthorshipLikelihoodScore/100));
                	 
                	 article.setAuthorshipLikelihoodScore(authorshipLikelyhoodScore);
                	 article.setTargetAuthorCountPenalty(authorshipLikelyhoodScore - originalAuthorshipLikelihoodScore);
                 }
            return article;
        })
        .collect(Collectors.toList());
	}
 	
 	
	

	// Build a lookup map from JSONArray for O(1) score access per article
	private static Map<Long, Double> buildScoreMap(JSONArray jsonArray) {
	    Map<Long, Double> scoreMap = new HashMap<>(jsonArray.length());
	    for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject obj = jsonArray.getJSONObject(i);
	        scoreMap.put(obj.getLong("id"), obj.optDouble("scoreTotal", 0.0));
	    }
	    return scoreMap;
	}

	private boolean isS3UploadRequired() {
		// Retrieve properties
		boolean isS3Use = Boolean.parseBoolean(PropertiesUtils.get(AWS_S3_USE));
		boolean isDynamoDBLocal = Boolean.parseBoolean(PropertiesUtils.get(AWS_DYNAMO_DB_LOCAL));
		return isS3Use && !isDynamoDBLocal;
	}
	
	private boolean uploadJsonFileIntoS3(String keyName,File fileName)
	{
		String feedbackScoreBucketName = PropertiesUtils.get(AWS_S3_FEEDBACK_SCORE_BUCKET_NAME);
        
		// Upload the python file
        try {
        	
        	final S3Client s3 = AwsScoringClients.s3();
        	try {
                HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                        .bucket(feedbackScoreBucketName)
                        .build();

                s3.headBucket(headBucketRequest); // If successful, the bucket exists
                log.info("Uploading files to S3 bucket {}",feedbackScoreBucketName);
                
                // Upload file
	            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
	                    .bucket(feedbackScoreBucketName.toLowerCase())
	                    .key(keyName)
	                    .contentType(APPLICATION_JSON)
	                    .build();
	            
	            PutObjectResponse putObjectResponse = s3.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(fileName.getAbsolutePath())));
	            if (putObjectResponse.sdkHttpResponse().isSuccessful()) {
	            	log.info("CSV file uploaded successfully to S3 bucket : {} ",feedbackScoreBucketName );
	                return true;
	            } else {
	            	log.error("Failed to upload JSON file to S3.");
	                return false;
	            }
            } catch (S3Exception e) {
                if (e.statusCode() == 404) {
                    log.error("S3 bucket does not exist: {} ",feedbackScoreBucketName);
                    return false;
                }else {
                	log.error(e.getMessage());
                	return false;
                }
            }
    
        
        } catch (Exception e) {
			log.error("Unexpected exception while uploading CSV file to S3. fileName={}, bucketName={}",fileName, feedbackScoreBucketName, e);
			return false;			 
        }
 	}
	
	/**
     * Submits a single strategy as a virtual-thread task.
     * .exceptionally() logs the failure per-task and returns null
     * so sibling strategies are NOT cancelled on one failure.
     */
    private CompletableFuture<Void> runStrategy(String category,
                                                 StrategyContext context,
                                                 List<ReCiterArticle> reCiterArticles,
                                                 Identity identity,
                                                 Executor executor) {
        return CompletableFuture.runAsync(() -> {
            var stopWatch = new StopWatch(category);
            stopWatch.start(category);

            // Java 21: pattern matching instanceof — no explicit cast needed
            if (context instanceof RemoveReCiterArticleStrategyContext removeCtx) {
                removeCtx.executeStrategy(reCiterArticles, identity);
            } else if (context instanceof TargetAuthorStrategyContext targetCtx) {
                targetCtx.executeStrategy(reCiterArticles, identity);
            }

            stopWatch.stop();
            log.info("{} took {} s", stopWatch.getId(), stopWatch.getTotalTimeSeconds());

        }, executor).exceptionally(ex -> {
            log.error("Strategy '{}' failed", category, ex);
            return null; // null = completed exceptionally but siblings continue
        });
    }
}