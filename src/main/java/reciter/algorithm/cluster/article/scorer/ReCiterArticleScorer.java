package reciter.algorithm.cluster.article.scorer;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.acceptedrejected.AcceptedRejectedStrategyContext;
import reciter.algorithm.evidence.article.acceptedrejected.strategy.AcceptedRejectedStrategy;
import reciter.algorithm.evidence.article.citation.CitationStrategyContext;
import reciter.algorithm.evidence.article.citation.strategy.CitationStrategy;
import reciter.algorithm.evidence.article.citation.strategy.InverseCoCitationStrategy;
import reciter.algorithm.evidence.article.coauthor.CoauthorStrategyContext;
import reciter.algorithm.evidence.article.coauthor.strategy.CoauthorStrategy;
import reciter.algorithm.evidence.article.journal.JournalStrategyContext;
import reciter.algorithm.evidence.article.journal.strategy.JournalStrategy;
import reciter.algorithm.evidence.article.standardizedscore.StandardScoreStrategyContext;
import reciter.algorithm.evidence.article.standardizedscore.strategy.StandardScoreStrategy;
import reciter.algorithm.evidence.cluster.ClusterStrategyContext;
import reciter.algorithm.evidence.cluster.averageclustering.AverageClusteringStrategyContext;
import reciter.algorithm.evidence.cluster.averageclustering.strategy.AverageClusteringStrategy;
import reciter.algorithm.evidence.cluster.clustersize.ClusterSizeStrategyContext;
import reciter.algorithm.evidence.cluster.clustersize.strategy.ClusterSizeStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.AffiliationStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.strategy.CommonAffiliationStrategy;
import reciter.algorithm.evidence.targetauthor.articlesize.ArticleSizeStrategyContext;
import reciter.algorithm.evidence.targetauthor.articlesize.strategy.ArticleSizeStrategy;
import reciter.algorithm.evidence.targetauthor.citizenship.CitizenshipStrategyContext;
import reciter.algorithm.evidence.targetauthor.citizenship.strategy.CitizenshipStrategy;
import reciter.algorithm.evidence.targetauthor.degree.DegreeStrategyContext;
import reciter.algorithm.evidence.targetauthor.degree.strategy.DegreeType;
import reciter.algorithm.evidence.targetauthor.degree.strategy.YearDiscrepancyStrategy;
import reciter.algorithm.evidence.targetauthor.department.DepartmentStrategyContext;
import reciter.algorithm.evidence.targetauthor.department.strategy.DepartmentStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.education.EducationStrategyContext;
import reciter.algorithm.evidence.targetauthor.education.strategy.EducationStrategy;
import reciter.algorithm.evidence.targetauthor.email.EmailStrategyContext;
import reciter.algorithm.evidence.targetauthor.email.strategy.EmailStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.grant.GrantStrategyContext;
import reciter.algorithm.evidence.targetauthor.grant.strategy.GrantStrategy;
import reciter.algorithm.evidence.targetauthor.knownrelationship.KnownRelationshipStrategyContext;
import reciter.algorithm.evidence.targetauthor.knownrelationship.strategy.KnownRelationshipStrategy;
import reciter.algorithm.evidence.targetauthor.name.RemoveByNameStrategyContext;
import reciter.algorithm.evidence.targetauthor.name.ScoreByNameStrategyContext;
import reciter.algorithm.evidence.targetauthor.name.strategy.RemoveByNameStrategy;
import reciter.algorithm.evidence.targetauthor.name.strategy.ScoreByNameStrategy;
import reciter.algorithm.evidence.targetauthor.persontype.PersonTypeStrategyContext;
import reciter.algorithm.evidence.targetauthor.persontype.strategy.PersonTypeStrategy;
import reciter.algorithm.evidence.targetauthor.scopus.ScopusStrategyContext;
import reciter.algorithm.evidence.targetauthor.scopus.strategy.ScopusCommonAffiliation;
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
	 * Article size.
	 */
	private StrategyContext articleSizeStrategyContext;
	
	/**
	 * Person Type.
	 */
	private StrategyContext personTypeStrategyContext;
	
	/**
	 * Accpeted Rejected .
	 */
	private StrategyContext acceptedRejectedStrategyContext;

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
		this.knownRelationshipsStrategyContext = new KnownRelationshipStrategyContext(new KnownRelationshipStrategy());
		this.affiliationStrategyContext = new AffiliationStrategyContext(new CommonAffiliationStrategy());

		// Using the following strategy contexts in sequence to reassign individual articles
		// to selected clusters.
		this.scopusCommonAffiliationStrategyContext = new ScopusStrategyContext(new ScopusCommonAffiliation());
		this.coauthorStrategyContext = new CoauthorStrategyContext(new CoauthorStrategy(identity));
		this.journalStrategyContext = new JournalStrategyContext(new JournalStrategy(identity));
		this.citizenshipStrategyContext = new CitizenshipStrategyContext(new CitizenshipStrategy());
		this.educationStrategyContext = new EducationStrategyContext(new EducationStrategy()); // check this one.
		this.grantStrategyContext = new GrantStrategyContext(new GrantStrategy());
		this.citationStrategyContext = new CitationStrategyContext(new CitationStrategy());
		this.coCitationStrategyContext = new CitationStrategyContext(new InverseCoCitationStrategy());
		this.acceptedRejectedStrategyContext = new AcceptedRejectedStrategyContext(new AcceptedRejectedStrategy());
		this.averageClusteringStrategyContext = new AverageClusteringStrategyContext(new AverageClusteringStrategy());
		this.standardScoreStrategyContext = new StandardScoreStrategyContext(new StandardScoreStrategy());
		
		int numArticles = 0;
		for (ReCiterCluster reCiterCluster : clusters.values()) {
			numArticles += reCiterCluster.getArticleCluster().size();
		}
		this.articleSizeStrategyContext = new ArticleSizeStrategyContext(new ArticleSizeStrategy(numArticles));
		this.personTypeStrategyContext = new PersonTypeStrategyContext(new PersonTypeStrategy());

		// TODO: getBoardCertificationScore(map);

		this.bachelorsYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.BACHELORS));
		this.doctoralYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.DOCTORAL));
		// articleTitleInEnglishStrategyContext = new ArticleTitleStrategyContext(new ArticleTitleInEnglish());
		this.removeByNameStrategyContext = new RemoveByNameStrategyContext(new RemoveByNameStrategy());

		this.clusterSizeStrategyContext = new ClusterSizeStrategyContext(new ClusterSizeStrategy());

		this.strategyContexts = new ArrayList<StrategyContext>();
		
		if (strategyParameters.isScopusCommonAffiliation()) {
			this.strategyContexts.add(this.scopusCommonAffiliationStrategyContext);
		}
		
		if (strategyParameters.isCoauthor()) {
			this.strategyContexts.add(this.coauthorStrategyContext);
		}
		
		if (strategyParameters.isJournal()) {
			this.strategyContexts.add(this.journalStrategyContext);
		}
		
		if (strategyParameters.isCitizenship()) {
			this.strategyContexts.add(this.citizenshipStrategyContext);
		}
		
		if (strategyParameters.isEducation()) {
			this.strategyContexts.add(this.educationStrategyContext);
		}	
		
		if (strategyParameters.isGrant()) {
			this.strategyContexts.add(this.grantStrategyContext);
		}
		
		if (strategyParameters.isCitation()) {
			this.strategyContexts.add(this.citationStrategyContext);
		}
		
		if (strategyParameters.isCoCitation()) {
			this.strategyContexts.add(this.coCitationStrategyContext);
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
		
		//		strategyContexts.add(articleTitleInEnglishStrategyContext);
		
		if (strategyParameters.isRemoveByName()) {
			this.strategyContexts.add(this.removeByNameStrategyContext);
		}
		
		if(strategyParameters.isPersonType()) {
			this.strategyContexts.add(this.personTypeStrategyContext);
		}
		
		if(strategyParameters.isAcceptedRejected()) {
			this.strategyContexts.add(this.acceptedRejectedStrategyContext);
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
	}
	

	@Override
	public void runArticleScorer(Map<Long, ReCiterCluster> clusters, Identity identity) {
		for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
			long clusterId = entry.getKey();
			slf4jLogger.info("******************** Cluster " + clusterId + " scoring starts **********************");
			List<ReCiterArticle> reCiterArticles = entry.getValue().getArticleCluster();
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
			
			if (strategyParameters.isArticleSize()) {
				((TargetAuthorStrategyContext) articleSizeStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isPersonType()) {
				((TargetAuthorStrategyContext) personTypeStrategyContext).executeStrategy(reCiterArticles, identity);
			}
			
			if (strategyParameters.isAcceptedRejected()) {
				((ReCiterArticleStrategyContext) acceptedRejectedStrategyContext).executeStrategy(reCiterArticles);
			}
			
			if (strategyParameters.isAverageClustering()) {
				((ClusterStrategyContext) averageClusteringStrategyContext).executeStrategy(entry.getValue());
			}
			
			((ReCiterArticleStrategyContext) standardScoreStrategyContext).executeStrategy(reCiterArticles);
			

			/*if (strategyParameters.isKnownRelationship()) {
				double knownRelationshipScore = ((TargetAuthorStrategyContext) knownRelationshipsStrategyContext).executeStrategy(reCiterArticles, identity);
				if (knownRelationshipScore > 0) {
					selectedClusterIds.add(clusterId);
				}
			}

			if (strategyParameters.isAffiliation()) {
				double affiliationScore = ((TargetAuthorStrategyContext)affiliationStrategyContext).executeStrategy(reCiterArticles, identity);
				if (affiliationScore > 0) {
					selectedClusterIds.add(clusterId);
				}
			}*/
			
			slf4jLogger.info("******************** Cluster " + clusterId + " scoring ends **********************");
		}
		
	}
}
