package reciter.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.algorithm.cluster.targetauthor.ClusterSelector;
import reciter.algorithm.cluster.targetauthor.ReCiterClusterSelector;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.mesh.MeshMajorStrategyContext;
import reciter.algorithm.evidence.article.mesh.strategy.MeshMajorStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.AffiliationStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.strategy.WeillCornellAffiliationStrategy;
import reciter.algorithm.evidence.targetauthor.citizenship.CitizenshipStrategyContext;
import reciter.algorithm.evidence.targetauthor.citizenship.strategy.CitizenshipStrategy;
import reciter.algorithm.evidence.targetauthor.department.DepartmentStrategyContext;
import reciter.algorithm.evidence.targetauthor.department.strategy.DepartmentStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.email.EmailStrategyContext;
import reciter.algorithm.evidence.targetauthor.email.strategy.EmailStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.grant.GrantStrategyContext;
import reciter.algorithm.evidence.targetauthor.grant.strategy.KnownCoinvestigatorStrategy;
import reciter.algorithm.evidence.targetauthor.internship.InternshipAndResidenceStrategyContext;
import reciter.algorithm.evidence.targetauthor.internship.strategy.InternshipAndResidenceStrategy;
import reciter.algorithm.evidence.targetauthor.name.NameStrategyContext;
import reciter.algorithm.evidence.targetauthor.name.strategy.NameStrategy;
import reciter.algorithm.evidence.targetauthor.scopus.ScopusStrategyContext;
import reciter.algorithm.evidence.targetauthor.scopus.strategy.StringMatchingAffiliation;
import reciter.database.mongo.model.Feature;
import reciter.database.mongo.model.Identity;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;

@Component("reCiterEngine")
public class ReCiterEngine implements Engine {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterEngine.class);
	
	private Map<String, Long> meshTermCache;

	@Override
	public List<Feature> generateFeature(Identity identity, List<ReCiterArticle> reCiterArticleList) {
		List<Feature> features = new ArrayList<Feature>();
		for (ReCiterArticle reCiterArticle : reCiterArticleList) {
			Feature feature = new Feature();
			feature.setPmid(reCiterArticle.getArticleId());
			TargetAuthorStrategyContext emailStrategyContext = new EmailStrategyContext(new EmailStringMatchStrategy());
			emailStrategyContext.populateFeature(reCiterArticle, identity, feature);
			TargetAuthorStrategyContext departmentStringMatchStrategyContext = new DepartmentStrategyContext(new DepartmentStringMatchStrategy());
			departmentStringMatchStrategyContext.populateFeature(reCiterArticle, identity, feature);
			TargetAuthorStrategyContext grantCoauthorStrategyContext = new GrantStrategyContext(new KnownCoinvestigatorStrategy());
			grantCoauthorStrategyContext.populateFeature(reCiterArticle, identity, feature);
			TargetAuthorStrategyContext affiliationStrategyContext = new AffiliationStrategyContext(new WeillCornellAffiliationStrategy());
			affiliationStrategyContext.populateFeature(reCiterArticle, identity, feature);
			TargetAuthorStrategyContext scopusStrategyContext = new ScopusStrategyContext(new StringMatchingAffiliation());
			scopusStrategyContext.populateFeature(reCiterArticle, identity, feature);
			
			features.add(feature);
		}
		return features;
	}
	
	@Override
	public Analysis run(Identity identity, List<ReCiterArticle> reCiterArticleList) {

		Analysis.assignGoldStandard(reCiterArticleList, identity.getKnownPmids());

		// Perform Phase 1 clustering.
		Clusterer clusterer = new ReCiterClusterer(identity, reCiterArticleList);
		clusterer.cluster();
		slf4jLogger.info("Phase 1 Clustering result");
		slf4jLogger.info(clusterer.toString());

		// Perform Phase 2 clusters selection.
		ClusterSelector clusterSelector = new ReCiterClusterSelector(clusterer.getClusters(), identity);
		clusterSelector.runSelectionStrategy(clusterer.getClusters(), identity);

		// Perform Mesh Heading recall improvement.
		// Use MeSH major to improve recall after phase two (https://github.com/wcmc-its/ReCiter/issues/131)
		List<ReCiterArticle> selectedArticles = new ArrayList<ReCiterArticle>();

		for (long id : clusterSelector.getSelectedClusterIds()) {
			selectedArticles.addAll(clusterer.getClusters().get(id).getArticleCluster());
		}
		
		StrategyContext meshMajorStrategyContext = new MeshMajorStrategyContext(new MeshMajorStrategy(selectedArticles, meshTermCache));
		clusterSelector.handleNonSelectedClusters((MeshMajorStrategyContext) meshMajorStrategyContext, clusterer.getClusters(), identity);

		Analysis analysis = Analysis.performAnalysis(clusterer, clusterSelector, identity.getKnownPmids());
		slf4jLogger.info(clusterer.toString());
		slf4jLogger.info("Analysis for cwid=[" + identity.getCwid() + "]");
		slf4jLogger.info("Precision=" + analysis.getPrecision());
//		totalPrecision += analysis.getPrecision();
		slf4jLogger.info("Recall=" + analysis.getRecall());
//		totalRecall += analysis.getRecall();

		double accuracy = (analysis.getPrecision() + analysis.getRecall()) / 2;
		slf4jLogger.info("Accuracy=" + accuracy);
//		totalAccuracy += accuracy;

		slf4jLogger.info("True Positive List [" + analysis.getTruePositiveList().size() + "]: " + analysis.getTruePositiveList());
		slf4jLogger.info("True Negative List: [" + analysis.getTrueNegativeList().size() + "]: " + analysis.getTrueNegativeList());
		slf4jLogger.info("False Positive List: [" + analysis.getFalsePositiveList().size() + "]: " + analysis.getFalsePositiveList());
		slf4jLogger.info("False Negative List: [" + analysis.getFalseNegativeList().size() + "]: " + analysis.getFalseNegativeList());
		slf4jLogger.info("\n");

		for (ReCiterArticle reCiterArticle : reCiterArticleList) {
			slf4jLogger.info(reCiterArticle.getArticleId() + ": " + reCiterArticle.getClusterInfo());
		}
		
		return analysis;
	}

	public void executeTargetAuthorStrategy(List<StrategyContext> strategyContexts, 
			List<ReCiterArticle> reCiterArticles, Identity identity) {

		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			for (StrategyContext context : strategyContexts) {
				((TargetAuthorStrategyContext) context).executeStrategy(reCiterArticle, identity);
			}
		}
	}

	public List<StrategyContext> getStrategyContexts() {
		// Strategies that select clusters that are similar to the target author.
		StrategyContext emailStrategyContext = new EmailStrategyContext(new EmailStringMatchStrategy());
		StrategyContext departmentStringMatchStrategyContext = new DepartmentStrategyContext(new DepartmentStringMatchStrategy());
		StrategyContext grantCoauthorStrategyContext = new GrantStrategyContext(new KnownCoinvestigatorStrategy());
		StrategyContext affiliationStrategyContext = new AffiliationStrategyContext(new WeillCornellAffiliationStrategy());

		// Using the following strategy contexts in sequence to reassign individual articles
		// to selected clusters.
		StrategyContext scopusStrategyContext = new ScopusStrategyContext(new StringMatchingAffiliation());
		//		StrategyContext coauthorStrategyContext = new CoauthorStrategyContext(new CoauthorStrategy(targetAuthor));
		//		StrategyContext journalStrategyContext = new JournalStrategyContext(new JournalStrategy(targetAuthor));
		StrategyContext citizenshipStrategyContext = new CitizenshipStrategyContext(new CitizenshipStrategy());
		StrategyContext nameStrategyContext = new NameStrategyContext(new NameStrategy());
//		StrategyContext boardCertificationStrategyContext = new BoardCertificationStrategyContext(new CosineSimilarityStrategy());
		StrategyContext internshipsAndResidenceStrategyContext = new InternshipAndResidenceStrategyContext(new InternshipAndResidenceStrategy());

		// TODO: removeArticlesBasedOnYearDiscrepancy(map);
		//		StrategyContext bachelorsYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.BACHELORS));
		//		StrategyContext doctoralYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.DOCTORAL));

		List<StrategyContext> strategyContexts = new ArrayList<StrategyContext>();

		strategyContexts.add(emailStrategyContext);
		strategyContexts.add(departmentStringMatchStrategyContext);
		strategyContexts.add(grantCoauthorStrategyContext);
		strategyContexts.add(affiliationStrategyContext);

		strategyContexts.add(scopusStrategyContext);
		//		strategyContexts.add(coauthorStrategyContext);
		//		strategyContexts.add(journalStrategyContext);
		strategyContexts.add(citizenshipStrategyContext);
		strategyContexts.add(nameStrategyContext);
//		strategyContexts.add(boardCertificationStrategyContext);
		strategyContexts.add(internshipsAndResidenceStrategyContext);

		//		strategyContexts.add(bachelorsYearDiscrepancyStrategyContext);
		//		strategyContexts.add(doctoralYearDiscrepancyStrategyContext);

		return strategyContexts;
	}

	@Override
	public Map<String, Long> getMeshTermCache() {
		return meshTermCache;
	}

	@Override
	public void setMeshTermCache(Map<String, Long> meshTermCache) {
		this.meshTermCache = meshTermCache;
	}
}
