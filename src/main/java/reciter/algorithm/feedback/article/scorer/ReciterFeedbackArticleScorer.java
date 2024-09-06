package reciter.algorithm.feedback.article.scorer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.springframework.util.StopWatch;

import reciter.algorithm.evidence.StrategyContext;
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
import reciter.algorithm.evidence.targetauthor.feedback.journaldomain.JournalDomainFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.journaldomain.strategy.JournalDomainFeedbackStrategy;
import reciter.algorithm.evidence.targetauthor.feedback.journalfield.JournalFieldFeedbackStrategyContext;
import reciter.algorithm.evidence.targetauthor.feedback.journalfield.strategy.JournalFieldFeedbackStrategy;
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
	private StrategyContext journalDomainStrategyContext;
	private StrategyContext journalFieldStrategyContext;
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
	
	ExecutorService executorService = Executors.newFixedThreadPool(14);
	
	public ReciterFeedbackArticleScorer(List<ReCiterArticle> articles,Identity identity,EngineParameters parameters,StrategyParameters strategyParameters)
	{
		ReciterFeedbackArticleScorer.strategyParameters = strategyParameters;
		this.reciterArticles = articles;
		this.identity = identity;
		this.journalStrategyContext = new JournalFeedbackStrategyContext(new JournalFeedbackStrategy());
		this.journalDomainStrategyContext = new JournalDomainFeedbackStrategyContext(new JournalDomainFeedbackStrategy());
		this.journalFieldStrategyContext = new JournalFieldFeedbackStrategyContext(new JournalFieldFeedbackStrategy());
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
	}
	public void runFeedbackArticleScorer(List<ReCiterArticle> reCiterArticles, Identity identity) 
	{
		 List<Future<?>> futures = new ArrayList<>();

		if(strategyParameters.isFeedbackScoreJournal()) {
			/*StopWatch stopWatch = new StopWatch("Journal Category");
	        stopWatch.start("Journal Category");
				executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) journalStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch.stop();
			log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Journal Category", executorService, journalStrategyContext, reCiterArticles, identity));
		}
		if(strategyParameters.isFeedbackScoreJournalDomain()) {
			/*StopWatch stopWatch1 = new StopWatch("Journal Domain Category");
	        stopWatch1.start("Journal Domain Category");
				executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) journalDomainStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch1.stop();
			log.info(stopWatch1.getId() + " took " + stopWatch1.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Journal Domain Category", executorService, journalDomainStrategyContext, reCiterArticles, identity));
		}
		if(strategyParameters.isFeedbackScoreJournalField()) {
			/*StopWatch stopWatch2 = new StopWatch("Journal Field Category");
	        stopWatch2.start("Journal Field Category");
				executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) journalFieldStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch2.stop();
			log.info(stopWatch2.getId() + " took " + stopWatch2.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Journal Field Category", executorService, journalFieldStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreJournalSubField()) {
			/*StopWatch stopWatch3 = new StopWatch("Journal SubField Category");
	        stopWatch3.start("Journal SubField Category");
				executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) journalSubFieldStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch3.stop();
			log.info(stopWatch3.getId() + " took " + stopWatch3.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Journal SubField Category", executorService, journalSubFieldStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreOrcid()) {
			/*StopWatch stopWatch4 = new StopWatch("Orcid Category");
	        stopWatch4.start("Orcid Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) orcidStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch4.stop();
			log.info(stopWatch4.getId() + " took " + stopWatch4.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Orcid Category", executorService, orcidStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreYear()) {
			/*StopWatch stopWatch5 = new StopWatch("Year Category");
	        stopWatch5.start("Year Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) yearStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch5.stop();
			log.info(stopWatch5.getId() + " took " + stopWatch5.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Year Category", executorService, yearStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreEmail()) {
			/*StopWatch stopWatch6 = new StopWatch("Email Category");
	        stopWatch6.start("Email Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) emailStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch6.stop();
			log.info(stopWatch6.getId() + " took " + stopWatch6.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Email Category", executorService, emailStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreOrganization()) {
			/*StopWatch stopWatch7 = new StopWatch("Organization Category");
	        stopWatch7.start("Organization Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) organizationStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch7.stop();
			log.info(stopWatch7.getId() + " took " + stopWatch7.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Organization Category", executorService, organizationStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreInstitution()) {
			/*StopWatch stopWatch8 = new StopWatch("Institution Category");
	        stopWatch8.start("Institution Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) institutionStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch8.stop();
			log.info(stopWatch8.getId() + " took " + stopWatch8.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Institution Category", executorService, institutionStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreTargetAuthorName()) {
			/*StopWatch stopWatch9 = new StopWatch("Target Author Name Category");
	        stopWatch9.start("Target Author Name Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) targetAuthorNameStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch9.stop();
			log.info(stopWatch9.getId() + " took " + stopWatch9.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Target Author Name Category", executorService, targetAuthorNameStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreOrcidCoAuthor()) {
			/*StopWatch stopWatch10 = new StopWatch("Orcid coAuthor Category");
	        stopWatch10.start("Orcid coAuthor Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) orcidCoAuthorStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch10.stop();
			log.info(stopWatch10.getId() + " took " + stopWatch10.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Orcid coAuthor Category", executorService, orcidCoAuthorStrategyContext, reCiterArticles, identity));
		}
		if(strategyParameters.isFeedbackScoreKeyword()) {
			/*StopWatch stopWatch11 = new StopWatch("Keyword Category");
	        stopWatch11.start("Keyword Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) keywordStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch11.stop();
			log.info(stopWatch11.getId() + " took " + stopWatch11.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("Keyword Category", executorService, keywordStrategyContext, reCiterArticles, identity));
		}
		if(strategyParameters.isFeedbackScoreCoauthorName()) {
			/*StopWatch stopWatch12 = new StopWatch("CoAuthorName Category");
	        stopWatch12.start("CoAuthorName Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) coAuthorNameStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch12.stop();
			log.info(stopWatch12.getId() + " took " + stopWatch12.getTotalTimeSeconds() + "s");*/
			futures.add(submitAndLogTime("CoAuthorName Category", executorService, coAuthorNameStrategyContext, reCiterArticles, identity));

		}
		if(strategyParameters.isFeedbackScoreCites()) {
			/*StopWatch stopWatch13 = new StopWatch("Cites Category");
	        stopWatch13.start("Cites Category");
			executorService.execute(() ->((TargetAuthorFeedbackStrategyContext) citesStrategyContext).executeFeedbackStrategy(reCiterArticles, identity));
			stopWatch13.stop();
			log.info(stopWatch13.getId() + " took " + stopWatch13.getTotalTimeSeconds() + "s");*/
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
            // All tasks completed successfully
            exportArticlesConsolidateReport(reCiterArticles, identity);
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
	protected void exportConsolidatedFeedbackScores(String personIdentifier, Map<Long,ReCiterArticle> articleMap)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
		
		 DecimalFormat decimalFormat = new DecimalFormat("#.######"); 
		// Get current date and time
		LocalDateTime now = LocalDateTime.now();
		
		// Format the current date and time to a safe string for file names
		String timestamp = now.format(formatter);

		Path filePath = Paths.get(timestamp + "-" + personIdentifier + "-feedbackScoring-consolidated.csv");
		
		try ( // Create BufferedWriter
				BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
				// Create CSVPrinter
				CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create(CSVFormat.DEFAULT)
		                             .setHeader("PersonIdentifier",
		                            		 	"Pmid", 
		                            		 	"userAssertion",
		                            		 	"scoreCites",
		                            		 	"scoreCoAuthorName",
		                            		 	"scoreEmail",
		                            		 	"scoreInstitution",
		                            		 	"scoreJournal",
		                            		 	"scoreJournalSubField",
		                            		 	"scoreKeyword",
		                            		 	"scoreOrcid",
		                            		 	"scoreOrcidCoAuthor",
		                            		 	"scoreOrganization",
		                            		 	"scoreTargetAuthorName",
		                            		 	"scoreYear",
		                            		 	"scoreFeedbackTotal")
		                             .build())) {	
			articleMap.forEach((articleId, article) -> {
				// System.out.println(" Inner Key: " + articleId + "Score:" + score);
				try {
					
						double totalFeedbackScore = Double.parseDouble(article.getExportedEmailFeedbackScore()!=null?article.getExportedEmailFeedbackScore():"0.0") +
								Double.parseDouble(article.getExportedInstitutionFeedbackScore()!=null?article.getExportedInstitutionFeedbackScore():"0.0") +
								Double.parseDouble(article.getExportedJournalFeedackScore()!=null?article.getExportedJournalFeedackScore():"0.0") +
								Double.parseDouble(article.getExportedJournalSubFieldFeedbackScore()!=null?article.getExportedJournalSubFieldFeedbackScore():"0.0") +
								Double.parseDouble(article.getExportedKeywordFeedackScore()!=null?article.getExportedKeywordFeedackScore():"0.0" ) +
								Double.parseDouble(article.getExportedOrcidFeedbackScore()!=null?article.getExportedOrcidFeedbackScore():"0.0")  +
								Double.parseDouble(article.getExportedOrcidCoAuthorFeedbackScore()!=null?article.getExportedOrcidCoAuthorFeedbackScore():"0.0") +
								Double.parseDouble(article.getExportedOrganizationFeedbackScore()!=null?article.getExportedOrganizationFeedbackScore():"0.0")  +
								Double.parseDouble(article.getExportedTargetAuthorNameFeedbackScore()!=null?article.getExportedTargetAuthorNameFeedbackScore():"0.0") +
								Double.parseDouble(article.getExportedYearFeedbackScore()!=null?article.getExportedYearFeedbackScore():"0.0")+
								Double.parseDouble(article.getExportedCoAuthorNameFeedbackScore()!=null?article.getExportedCoAuthorNameFeedbackScore():"0.0") +
								Double.parseDouble(article.getExportedCitesFeedbackScore()!=null?article.getExportedCitesFeedbackScore():"0.0");
					
						
						/* BigDecimal totalFeedbackScore = new BigDecimal(totalFeedbackScoreDouble);
						 BigDecimal formattedTotalFeedbackValue = totalFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						//if(log.isInfoEnabled())*/
							System.out.println("article total Feedback score :" + articleId + "-" + decimalFormat.format(totalFeedbackScore));
							System.out.println("article CoAuthorName score : " + articleId +"-" + decimalFormat.format(article.getCoAuthorNameFeedbackScore()));
						
						/*BigDecimal citesFeedbackScore = new BigDecimal(article.getCitesFeedbackScore());
						BigDecimal formattedCitesScore = citesFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						 
						BigDecimal coAuthorNameFeedbackScore = new BigDecimal(article.getCoAuthorNameFeedbackScore());
						BigDecimal formattedCoAuthorNameFeedbackScor = coAuthorNameFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal emailFeedbackScore = new BigDecimal(article.getEmailFeedbackScore());
						BigDecimal formattedEmailFeedbackScore = emailFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal institutionFeedbackScore = new BigDecimal(article.getInstitutionFeedbackScore());
						BigDecimal formattedInstitutionFeedbackScore = institutionFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal journalFeedackScore = new BigDecimal(article.getJournalFeedackScore());
						BigDecimal formattedJournalFeedackScore = journalFeedackScore.setScale(6, RoundingMode.HALF_UP);
							
						BigDecimal journalSubFieldFeedbackScore = new BigDecimal(article.getJournalSubFieldFeedbackScore());
						BigDecimal formattedJournalSubFieldFeedbackScore = journalSubFieldFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal keywordFeedackScore = new BigDecimal(article.getKeywordFeedackScore());
						BigDecimal formattedKeywordFeedackScore = keywordFeedackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal orcidFeedbackScore = new BigDecimal(article.getOrcidFeedbackScore());
						BigDecimal formattedOrcidFeedbackScore = orcidFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal coAuthorFeedbackScore = new BigDecimal(article.getOrcidCoAuthorFeedbackScore());
						BigDecimal formattedCoAuthorFeedbackScore = coAuthorFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal organizationFeedbackScore = new BigDecimal(article.getOrganizationFeedbackScore());
						BigDecimal formattedOrganizationFeedbackScore = organizationFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal targetAuthorNameFeedbackScore = new BigDecimal(article.getTargetAuthorNameFeedbackScore());
						BigDecimal formattedTargetAuthorNameFeedbackScore = targetAuthorNameFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						BigDecimal yearFeedbackScore = new BigDecimal(article.getYearFeedbackScore());
						BigDecimal formattedYearFeedbackScore = yearFeedbackScore.setScale(6, RoundingMode.HALF_UP);
						
						csvPrinter.printRecord(personIdentifier, 
												articleId, 
												article.getGoldStandard(),
												decimalFormat.format(formattedCitesScore),
												decimalFormat.format(formattedCoAuthorNameFeedbackScor),
												decimalFormat.format(formattedEmailFeedbackScore),
												decimalFormat.format(formattedInstitutionFeedbackScore),
												decimalFormat.format(formattedJournalFeedackScore),
												decimalFormat.format(formattedJournalSubFieldFeedbackScore),
												decimalFormat.format(formattedKeywordFeedackScore),
												decimalFormat.format(formattedOrcidFeedbackScore),
												decimalFormat.format(formattedCoAuthorFeedbackScore),
												decimalFormat.format(formattedOrganizationFeedbackScore),
												decimalFormat.format(formattedTargetAuthorNameFeedbackScore),
												decimalFormat.format(formattedYearFeedbackScore),
												decimalFormat.format(formattedTotalFeedbackValue)
												);*/
						
						
						String citiesFeedbackScore = article.getExportedCitesFeedbackScore();//decimalFormat.format(article.getCitesFeedbackScore());
						String coAuthorFeedbackScore = article.getExportedCoAuthorNameFeedbackScore();//decimalFormat.format(article.getCoAuthorNameFeedbackScore());
						String emailFeedbackScore = article.getExportedEmailFeedbackScore();//decimalFormat.format(article.getEmailFeedbackScore());
						String institutionFeedbackScore = article.getExportedInstitutionFeedbackScore();//decimalFormat.format(article.getInstitutionFeedbackScore());
						String journalFeedbackScore = article.getExportedJournalFeedackScore();//decimalFormat.format(article.getJournalFeedackScore());
						String journalSubFieldFeedbackScore = article.getExportedJournalSubFieldFeedbackScore();//decimalFormat.format(article.getJournalSubFieldFeedbackScore());
						String keywordFeedbackScore = article.getExportedKeywordFeedackScore();//decimalFormat.format(article.getKeywordFeedackScore());
						String orcidFeedbackScore = article.getExportedOrcidFeedbackScore();//decimalFormat.format(article.getOrcidFeedbackScore());
						String orcidCoAuthorFeedbackScore = article.getExportedOrcidCoAuthorFeedbackScore();//decimalFormat.format(article.getOrcidCoAuthorFeedbackScore());
						String organizationFeedbackScore = article.getExportedOrganizationFeedbackScore();//decimalFormat.format(article.getOrganizationFeedbackScore());
						String targetAuthorNameFeedbackScore = article.getExportedTargetAuthorNameFeedbackScore();//decimalFormat.format(article.getTargetAuthorNameFeedbackScore());
						String yearFeedbackScore = article.getExportedYearFeedbackScore();//decimalFormat.format(article.getYearFeedbackScore());
						String totalFeedbackScoreStr = decimalFormat.format(totalFeedbackScore);
						
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
								yearFeedbackScore,
								totalFeedbackScoreStr
								);
						
						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});
			csvPrinter.flush();
			// byte[] csvData = baos.toByteArray();

			// Upload CSV to S3
			/*
			 * String bucketName = "your-bucket-name"; String keyName =
			 * "data/total_scores.csv"; try (ByteArrayInputStream bais = new
			 * ByteArrayInputStream(csvData)) { PutObjectRequest putObjectRequest = new
			 * PutObjectRequest(bucketName, keyName, bais, null);
			 * s3Client.putObject(putObjectRequest); }
			 * 
			 * System.out.println("CSV file uploaded successfully to S3 bucket.");
			 */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
