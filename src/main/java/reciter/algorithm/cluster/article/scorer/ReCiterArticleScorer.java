package reciter.algorithm.cluster.article.scorer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import reciter.engine.analysis.evidence.AuthorNameEvidence;
import reciter.engine.analysis.evidence.EducationYearEvidence;
import reciter.engine.analysis.evidence.EmailEvidence;
import reciter.engine.analysis.evidence.GenderEvidence;
import reciter.engine.analysis.evidence.JournalCategoryEvidence;
import reciter.engine.analysis.evidence.NonTargetAuthorScopusAffiliation;
import reciter.engine.analysis.evidence.RelationshipEvidence;
import reciter.engine.analysis.evidence.RelationshipNegativeMatch;
import reciter.engine.analysis.evidence.TargetAuthorPubmedAffiliation;
import reciter.engine.analysis.evidence.TargetAuthorScopusAffiliation;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackIdentityScore;
import reciter.model.identity.Identity;

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
	
	private List<StrategyContext> strategyContexts;

	public static StrategyParameters strategyParameters;
	
	private Properties properties = new Properties();
	
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

	}
	

	@Override
	public void runArticleScorer(List<ReCiterArticle> reCiterArticles, Identity identity) {
		
			((TargetAuthorStrategyContext) nameStrategyContext).executeStrategy(reCiterArticles, identity);

			if (strategyParameters.isEmail()) {
				((TargetAuthorStrategyContext) emailStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isGrant()) {
				((TargetAuthorStrategyContext) grantStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isKnownRelationship()) {
				((TargetAuthorStrategyContext) knownRelationshipsStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isBachelorsYearDiscrepancy()) {
				((RemoveReCiterArticleStrategyContext) bachelorsYearDiscrepancyStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isDoctoralYearDiscrepancy()) {
				((RemoveReCiterArticleStrategyContext) doctoralYearDiscrepancyStrategyContext).executeStrategy(reCiterArticles, identity);
			}

			if (strategyParameters.isDepartment()) {
				((TargetAuthorStrategyContext) departmentStringMatchStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if(strategyParameters.isJournalCategory()) {
				((TargetAuthorStrategyContext) journalCategoryStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isAffiliation()) {
				((TargetAuthorStrategyContext)affiliationStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isArticleSize()) {
				((TargetAuthorStrategyContext) articleSizeStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isPersonType()) {
				((TargetAuthorStrategyContext) personTypeStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			
			
			if(strategyParameters.isGender()) {
				((TargetAuthorStrategyContext) genderStrategyContext).executeStrategy(reCiterArticles, identity);
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

		String fileName = StringUtils.join(timestamp, "-" , identity.getUid(), "-identityOnlyScoringInput.json");
		//PropertiesLoader("application.properties");// loading application.properties before retrieving specific property;
		boolean isS3UploadRequired = isS3UploadRequired();
		String identityS3BucketName = getProperty("aws.s3.feedback.score.bucketName");
		
        try {
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
        	  NeuralNetworkModelArticlesScorer nnmodel = new NeuralNetworkModelArticlesScorer();
			  JSONArray articlesIdentityScoreTotal = nnmodel.executeArticleScorePredictor("Identity Score", "identityOnlyScoreArticles.py",fileName,identityS3BucketName,isS3UploadRequiredString);
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
														    getRelationshipEvidenceTotalScore(article.getRelationshipEvidence()),
														    getNegativeMatchScore(article.getRelationshipEvidence()),
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

 	private static double getEducationYearScore(EducationYearEvidence evidence) {
 	    return Optional.ofNullable(evidence)
 	            .map(EducationYearEvidence::getDiscrepancyDegreeYearDoctoralScore)
 	            .orElse(0.0);
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

 	private static double getRelationshipEvidenceTotalScore(RelationshipEvidence evidence) {
 	    return Optional.ofNullable(evidence)
 	            .map(RelationshipEvidence::getRelationshipEvidenceTotalScore)
 	            .orElse(0.0);
 	}

 	private static double getNegativeMatchScore(RelationshipEvidence evidence) {
 	    return Optional.ofNullable(evidence)
 	            .map(RelationshipEvidence::getRelationshipNegativeMatch)
 	            .map(RelationshipNegativeMatch::getRelationshipNonMatchScore)
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
						         .map(article -> findJSONObjectById(authorshipLikelihoodScoreArray, article))
						         .filter(Objects::nonNull) // Filter out null values returned from findJSONObjectById
						         .collect(Collectors.toList()); // Collect the results if needed, or just perform the mapping
	}
	// Helper method to find JSONObject by article
	private static ReCiterArticle findJSONObjectById(JSONArray jsonArray, ReCiterArticle article) {
	    for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject jsonObject = jsonArray.getJSONObject(i);
	        if (jsonObject.getLong("id") == article.getArticleId()) {
	            /*article.setAuthorshipLikelihoodScore(BigDecimal.valueOf(jsonObject.getDouble("scoreTotal")*100)
	                    .setScale(3, RoundingMode.DOWN)
	                    .doubleValue());*/
	        	article.setAuthorshipLikelihoodScore(jsonObject.getDouble("scoreTotal")*100);
	            return article; // Return the modified article
	        }
	    }
	    if(article!=null)
	    	article.setAuthorshipLikelihoodScore(0.0);
	    return article; // Return null if not found
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
	private Properties PropertiesLoader(String fileName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
            	slf4jLogger.error("Sorry, unable to find " , fileName);
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
	private void uploadJsonFileIntoS3(String keyName,File file)
	{
		String FeedbackScoreBucketName = getProperty("aws.s3.feedback.score.bucketName");
        
		// Upload the python file
        try {
        	
        	final AmazonS3 s3 = AmazonS3ClientBuilder
					.standard()
					.withCredentials(new DefaultAWSCredentialsProviderChain())
					.withRegion(System.getenv("AWS_REGION"))
					.build();
        	
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
			}
        
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
