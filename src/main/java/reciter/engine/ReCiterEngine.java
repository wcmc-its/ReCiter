package reciter.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.dao.AnalysisDao;
import database.dao.GoldStandardPmidsDao;
import database.dao.impl.AnalysisDaoImpl;
import database.dao.impl.GoldStandardPmidsDaoImpl;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.algorithm.cluster.targetauthor.ClusterSelector;
import reciter.algorithm.cluster.targetauthor.ReCiterClusterSelector;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.AffiliationStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.strategy.WeillCornellAffiliationStrategy;
import reciter.algorithm.evidence.targetauthor.boardcertification.BoardCertificationStrategyContext;
import reciter.algorithm.evidence.targetauthor.boardcertification.strategy.CosineSimilarityStrategy;
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
import reciter.csv.CSVWriter;
import reciter.erroranalysis.Analysis;
import reciter.erroranalysis.AnalysisObject;
import reciter.model.ReCiterArticleFetcher;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import reciter.service.converters.AnalysisConverter;
import reciter.service.impl.TargetAuthorServiceImpl;

public class ReCiterEngine implements Engine {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterEngine.class);

	private ReCiterEngineProperty reCiterEngineProperty;
	private TargetAuthorService targetAuthorService;
	private List<AnalysisObject> analysisObjects;
	private AnalysisDao analysisDao;

	public double totalPrecision;
	public double totalRecall;

	public ReCiterEngine(ReCiterEngineProperty reCiterEngineProperty) {
		this.reCiterEngineProperty = reCiterEngineProperty;
		targetAuthorService = new TargetAuthorServiceImpl();
		analysisObjects = new ArrayList<AnalysisObject>();
		// TODO use service class.
		analysisDao = new AnalysisDaoImpl();
	}

	public ReCiterEngineProperty getReCiterEngineProperty() {
		return reCiterEngineProperty;
	}

	@Override
	public void run(TargetAuthor targetAuthor) {
		// Fetch the articles for this person.
		List<ReCiterArticle> reCiterArticleList = new ReCiterArticleFetcher().fetch(
				targetAuthor.getAuthorName().getLastName(), 
				targetAuthor.getAuthorName().getFirstInitial(),
				targetAuthor.getAuthorName().getMiddleName(),
				targetAuthor.getCwid(),
				targetAuthor.getEmail());

		// Perform Phase 1 clustering.
		Clusterer clusterer = new ReCiterClusterer(targetAuthor, reCiterArticleList);
		clusterer.cluster();

		// Perform Phase 2 clusters selection.
		ClusterSelector clusterSelector = new ReCiterClusterSelector(targetAuthor);
		clusterSelector.runSelectionStrategy(clusterer.getClusters(), targetAuthor);

		Analysis analysis = Analysis.performAnalysis(clusterer, clusterSelector);
		slf4jLogger.info(clusterer.toString());
		slf4jLogger.info("Precision=" + analysis.getPrecision());
		totalPrecision += analysis.getPrecision();
		slf4jLogger.info("Recall=" + analysis.getRecall());
		totalRecall += analysis.getRecall();
		slf4jLogger.info("True Positive List [" + analysis.getTruePositiveList().size() + "]: " + analysis.getTruePositiveList());
		slf4jLogger.info("True Negative List: [" + analysis.getTrueNegativeList().size() + "]: " + analysis.getTrueNegativeList());
		slf4jLogger.info("False Positive List: [" + analysis.getFalsePositiveList().size() + "]: " + analysis.getFalsePositiveList());
		slf4jLogger.info("False Negative List: [" + analysis.getFalseNegativeList().size() + "]: " + analysis.getFalseNegativeList());
		slf4jLogger.info("\n");

		for (ReCiterArticle reCiterArticle : reCiterArticleList) {
			slf4jLogger.info(reCiterArticle.getArticleId() + ": " + reCiterArticle.getClusterInfo());
		}
		analysisDao.insertAnalysisList(AnalysisConverter.convertToAnalysisList(analysis.getAnalysisObjectList()));
	}

	@Override
	public void run() {
		List<String> cwids = reCiterEngineProperty.getCwids();
		analysisDao.emptyTable(); // empty the analysis table.
		for (String cwid : cwids) {
			TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
			run(targetAuthor);
		}
		slf4jLogger.info("Average Precision: [" + totalPrecision / cwids.size() + "]");
		slf4jLogger.info("Average Recall: [" + totalRecall / cwids.size() + "]");
		slf4jLogger.info("\n");
	}

	public void executeTargetAuthorStrategy(List<StrategyContext> strategyContexts, 
			List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {

		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			for (StrategyContext context : strategyContexts) {
				((TargetAuthorStrategyContext) context).executeStrategy(reCiterArticle, targetAuthor);
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
		StrategyContext boardCertificationStrategyContext = new BoardCertificationStrategyContext(new CosineSimilarityStrategy());
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
		strategyContexts.add(boardCertificationStrategyContext);
		strategyContexts.add(internshipsAndResidenceStrategyContext);
		
//		strategyContexts.add(bachelorsYearDiscrepancyStrategyContext);
//		strategyContexts.add(doctoralYearDiscrepancyStrategyContext);
		
		return strategyContexts;
	}
	
	public void assignGoldStandard(List<ReCiterArticle> reCiterArticles, String cwid) {
		GoldStandardPmidsDao dao = new GoldStandardPmidsDaoImpl();
		List<String> pmids = dao.getPmidsByCwid(cwid);
		Set<Integer> pmidSet = new HashSet<Integer>();
		for (String pmid : pmids) {
			pmidSet.add(Integer.parseInt(pmid));
		}
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			if (pmidSet.contains(reCiterArticle.getArticleId())) {
				reCiterArticle.setGoldStandard(1);
			} else {
				reCiterArticle.setGoldStandard(0);
			}
		}
	}
	
	@Override
	public Analysis constructAnalysis() {
		List<String> cwids = reCiterEngineProperty.getCwids();
		analysisDao.emptyTable();
		List<StrategyContext> strategyContexts = getStrategyContexts();
		for (String cwid : cwids) {
			TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
			List<ReCiterArticle> reCiterArticleList = new ReCiterArticleFetcher().fetch(
					targetAuthor.getAuthorName().getLastName(), 
					targetAuthor.getAuthorName().getFirstInitial(),
					targetAuthor.getAuthorName().getMiddleName(),
					targetAuthor.getCwid(),
					targetAuthor.getEmail());
			
			assignGoldStandard(reCiterArticleList, cwid);
			executeTargetAuthorStrategy(strategyContexts, reCiterArticleList, targetAuthor);
			CSVWriter csvWriter = new CSVWriter("C:\\Users\\Jie\\Documents\\reciter_data\\" + cwid + ".csv");
			try {
				csvWriter.write(reCiterArticleList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
