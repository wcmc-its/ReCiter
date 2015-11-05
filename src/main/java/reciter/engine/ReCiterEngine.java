package reciter.engine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.dao.AnalysisDao;
import database.dao.impl.AnalysisDaoImpl;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.algorithm.cluster.targetauthor.ClusterSelector;
import reciter.algorithm.cluster.targetauthor.ReCiterClusterSelector;
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
	
	public double totalPrecision;
	public double totalRecall;
	
	public ReCiterEngine(ReCiterEngineProperty reCiterEngineProperty) {
		this.reCiterEngineProperty = reCiterEngineProperty;
		targetAuthorService = new TargetAuthorServiceImpl();
		analysisObjects = new ArrayList<AnalysisObject>();
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
//		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		
		// Perform Phase 1 clustering.
		Clusterer clusterer = new ReCiterClusterer(targetAuthor, reCiterArticleList);
		clusterer.cluster();

		// Perform Phase 2 clusters selection.
		AnalysisObject analysisObject = new AnalysisObject();
		analysisObjects.add(analysisObject);
		ClusterSelector clusterSelector = new ReCiterClusterSelector(analysisObject, targetAuthor);
		clusterSelector.runSelectionStrategy(clusterer.getClusters(), targetAuthor);
		
		Analysis analysis = Analysis.performAnalysis(clusterer, clusterSelector, analysisObject);
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
		
		// TODO use service class.
		AnalysisDao analysisDao = new AnalysisDaoImpl();
		analysisDao.emptyTable();
		analysisDao.insertAnalysisList(AnalysisConverter.convertToAnalysisList(analysis.getAnalysisObjectList()));
	}

	@Override
	public void run() {
		List<String> cwids = reCiterEngineProperty.getCwids();
		for (String cwid : cwids) {
			TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
			run(targetAuthor);
		}
		slf4jLogger.info("Average Precision: [" + totalPrecision / cwids.size() + "]");
		slf4jLogger.info("Average Recall: [" + totalRecall / cwids.size() + "]");
		slf4jLogger.info("\n");
	}
}
