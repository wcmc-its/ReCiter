package reciter.algorithm.cluster.article.scorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.acceptedrejected.AcceptedRejectedStrategyContext;
import reciter.algorithm.evidence.article.acceptedrejected.strategy.AcceptedRejectedStrategy;
import reciter.algorithm.evidence.article.standardizedscore.StandardScoreStrategyContext;
import reciter.algorithm.evidence.article.standardizedscore.strategy.StandardScoreStrategy;
import reciter.algorithm.evidence.cluster.ClusterStrategyContext;
import reciter.algorithm.evidence.cluster.averageclustering.AverageClusteringStrategyContext;
import reciter.algorithm.evidence.cluster.averageclustering.strategy.AverageClusteringStrategy;
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
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

/**
 * @author szd2013
 * This class will calculate scores based on https://docs.google.com/spreadsheets/d/1p-AIQOzFCFaGiIGsDR2ch7wJw1BFysIhLmsg7nGh-I0/
 */
public class ReCiterArticleScorer extends AbstractArticleScorer {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterArticleScorer.class);

	/** Cluster selection strategy contexts. */

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
	
	private StrategyContext averageClusteringStrategyContext;
	
	private StrategyContext standardScoreStrategyContext;

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

	/**
	 * Remove clusters based on cluster information.
	 */
	private StrategyContext clusterSizeStrategyContext;

	//	private StrategyContext boardCertificationStrategyContext;
	//
	//	private StrategyContext degreeStrategyContext;
	
	private StrategyContext grantStrategyContext;
	
	private StrategyContext citationStrategyContext;
	
	private StrategyContext coCitationStrategyContext;
	
	private List<StrategyContext> strategyContexts;

	private Set<Long> selectedClusterIds; // List of currently selected cluster ids.
	
	public static StrategyParameters strategyParameters;
	
	public ReCiterArticleScorer(Map<Long, ReCiterCluster> clusters, Identity identity, StrategyParameters strategyParameters) {
		
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
		this.acceptedRejectedStrategyContext = new AcceptedRejectedStrategyContext(new AcceptedRejectedStrategy());
		this.averageClusteringStrategyContext = new AverageClusteringStrategyContext(new AverageClusteringStrategy());
		this.standardScoreStrategyContext = new StandardScoreStrategyContext(new StandardScoreStrategy());
		
		int numArticles = 0;
		for (ReCiterCluster reCiterCluster : clusters.values()) {
			numArticles += reCiterCluster.getArticleCluster().size();
		}
		this.articleSizeStrategyContext = new ArticleSizeStrategyContext(new ArticleSizeStrategy(numArticles));
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

		// https://github.com/wcmc-its/ReCiter/issues/136
		if (strategyParameters.isClusterSize()) {
			this.strategyContexts.add(this.clusterSizeStrategyContext);
		}
		
		if(strategyParameters.isAverageClustering()) {
			this.strategyContexts.add(this.averageClusteringStrategyContext);
		}
		
		if(strategyParameters.isGender()) {
			this.strategyContexts.add(this.genderStrategyContext);
		}
	}
	

	@Override
	public void runArticleScorer(Map<Long, ReCiterCluster> clusters, Identity identity) {
		for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
			long clusterId = entry.getKey();
			slf4jLogger.info("******************** Cluster " + clusterId + " scoring starts **********************");
			//Start a executor service
			ExecutorService executorService = Executors.newWorkStealingPool();
			List<Callable<Double>> articleScorerCallables = new ArrayList<>();
			

			List<ReCiterArticle> reCiterArticles = entry.getValue().getArticleCluster();
			articleScorerCallables.add(new Callable<Double>() {
				public Double call() {
					return ((TargetAuthorStrategyContext) nameStrategyContext).executeStrategy(reCiterArticles, identity);
				}
			});
			//((TargetAuthorStrategyContext) nameStrategyContext).executeStrategy(reCiterArticles, identity);

			if (strategyParameters.isEmail()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext) emailStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext) emailStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isGrant()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext) grantStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext) grantStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isKnownRelationship()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext) knownRelationshipsStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext) knownRelationshipsStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isBachelorsYearDiscrepancy()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((RemoveReCiterArticleStrategyContext) bachelorsYearDiscrepancyStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((RemoveReCiterArticleStrategyContext) bachelorsYearDiscrepancyStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isDoctoralYearDiscrepancy()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((RemoveReCiterArticleStrategyContext) doctoralYearDiscrepancyStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((RemoveReCiterArticleStrategyContext) doctoralYearDiscrepancyStrategyContext).executeStrategy(reCiterArticles, identity);
			}

			if (strategyParameters.isDepartment()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext) departmentStringMatchStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext) departmentStringMatchStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if(strategyParameters.isJournalCategory()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext) journalCategoryStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext) journalCategoryStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isAffiliation()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext)affiliationStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext)affiliationStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isArticleSize()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext) articleSizeStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext) articleSizeStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isPersonType()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext) personTypeStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext) personTypeStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isUseGoldStandardEvidence()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((ReCiterArticleStrategyContext) acceptedRejectedStrategyContext).executeStrategy(reCiterArticles);
					}
				});
				//((ReCiterArticleStrategyContext) acceptedRejectedStrategyContext).executeStrategy(reCiterArticles);
			}
			
			if(strategyParameters.isGender()) {
				articleScorerCallables.add(new Callable<Double>() {
					public Double call() {
						return ((TargetAuthorStrategyContext) genderStrategyContext).executeStrategy(reCiterArticles, identity);
					}
				});
				//((TargetAuthorStrategyContext) genderStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			articleScorerCallables.add(new Callable<Double>() {
				public Double call() {
					return ((ReCiterArticleStrategyContext) standardScoreStrategyContext).executeStrategy(reCiterArticles);
				}
			});
			//((ReCiterArticleStrategyContext) standardScoreStrategyContext).executeStrategy(reCiterArticles);
			
			
			try {
				executorService.invokeAll(articleScorerCallables)
				.stream()
				.map(future -> {
					try {
						return future.get();
					}
					catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}).forEach(System.out::println);
			} catch (InterruptedException e) {
				slf4jLogger.error("Unable to invoke callable.", e);
			}
			slf4jLogger.info("Shutting down article scorer Executor service");
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
					executorService.shutdownNow();
				} 
			} catch (InterruptedException e) {
				executorService.shutdownNow();
			}

			if (strategyParameters.isAverageClustering()) {
				((ClusterStrategyContext) averageClusteringStrategyContext).executeStrategy(entry.getValue());
			}
			
			slf4jLogger.info("******************** Cluster " + clusterId + " scoring ends **********************");
		}
		
	}
}
