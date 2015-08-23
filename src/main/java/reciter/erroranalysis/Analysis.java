package reciter.erroranalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.dao.GoldStandardPmidsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.article.ReCiterArticle;

/**
 * Class that performs analysis such as calculating precision and recall.
 * @author jil3004
 *
 */
public class Analysis {

	private int truePos;
	private int goldStandardSize;
	private int selectedClusterSize;
	private List<Integer> falsePositiveList = new ArrayList<Integer>();
	private List<AnalysisObject> analysisObjectList = new ArrayList<AnalysisObject>();

	private static final Logger slf4jLogger = LoggerFactory.getLogger(Analysis.class);	

	public Analysis() {}

	/**
	 * Single Selection.
	 * @param finalCluster
	 * @param selection
	 * @param cwid
	 * @return
	 */
	public static Analysis performAnalysis(Map<Integer, ReCiterCluster> finalCluster, int selection, String cwid) {
		Analysis analysis = new Analysis();
		GoldStandardPmidsDao goldStandardPmidsDao = new GoldStandardPmidsDaoImpl();
		List<String> goldStandardPmids = goldStandardPmidsDao.getPmidsByCwid(cwid);

		slf4jLogger.info("Gold Standard: " + goldStandardPmids);

		analysis.setGoldStandardSize(goldStandardPmids.size());
		analysis.setSelectedClusterSize(finalCluster.get(selection).getArticleCluster().size());
		int numTruePos = 0;

		// get number of true positives.


		for (ReCiterArticle reCiterArticle : finalCluster.get(selection).getArticleCluster()) {
			int pmid = reCiterArticle.getArticleId();
			StatusEnum statusEnum;
			if (goldStandardPmids.contains(Integer.toString(pmid))) {
				numTruePos++;
				statusEnum = StatusEnum.TRUE_POSITIVE;
			} else {
				analysis.falsePositiveList.add(pmid);
				statusEnum = StatusEnum.FALSE_POSITIVE;
			}

			//			AnalysisObject analysisObject = AnalysisTranslator.translate(reCiterArticle, statusEnum, cwid, targetAuthor, isClusterOriginator)
		}
		analysis.setTruePos(numTruePos);


		return analysis;
	}

	/**
	 * List of selections.
	 * @param finalCluster
	 * @param selection
	 * @param cwid
	 * @return
	 */
	public static Analysis performAnalysis(ReCiterClusterer reCiterClusterer) {

		Map<Integer, ReCiterCluster> finalCluster = reCiterClusterer.getFinalCluster();
		Set<Integer> selection = reCiterClusterer.getSelectedClusterIdSet();
		String cwid = reCiterClusterer.getTargetAuthor().getCwid();

		Analysis analysis = new Analysis();
		GoldStandardPmidsDao goldStandardPmidsDao = new GoldStandardPmidsDaoImpl();
		List<String> goldStandardPmids = goldStandardPmidsDao.getPmidsByCwid(cwid);

		slf4jLogger.info("Gold Standard: " + goldStandardPmids);

		analysis.setGoldStandardSize(goldStandardPmids.size());

		// Combine all articles into a single list.
		List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
		for (int s : selection) {
			for (ReCiterArticle reCiterArticle : finalCluster.get(s).getArticleCluster()) {
				articleList.add(reCiterArticle);
			}
		}

		analysis.setSelectedClusterSize(articleList.size());
		int numTruePos = 0;

		// get number of true positives.
		for (ReCiterArticle reCiterArticle : articleList) {
			int pmid = reCiterArticle.getArticleId();
			if (goldStandardPmids.contains(Integer.toString(pmid))) {
				numTruePos++;
			} else {
				analysis.falsePositiveList.add(pmid);
			}
		}

		for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
			for (ReCiterArticle reCiterArticle : entry.getValue().getArticleCluster()) {
				int pmid = reCiterArticle.getArticleId();
				StatusEnum statusEnum;
				if (articleList.contains(reCiterArticle) && goldStandardPmids.contains(Integer.toString(pmid))) {
					statusEnum = StatusEnum.TRUE_POSITIVE;
				} else if (articleList.contains(reCiterArticle) && !goldStandardPmids.contains(Integer.toString(pmid))) {
					slf4jLogger.info("year diff: " + reCiterArticle.getArticleId() + ": " + reCiterClusterer.computeYearDiscrepancy(
							reCiterArticle, reCiterClusterer.getTargetAuthor()));
					statusEnum = StatusEnum.FALSE_POSITIVE;
				} else if (!articleList.contains(reCiterArticle) && goldStandardPmids.contains(Integer.toString(pmid))) {
					statusEnum = StatusEnum.FALSE_NEGATIVE;
				} else {
					statusEnum = StatusEnum.TRUE_NEGATIVE;
				}

				boolean isClusterOriginator = false;
				int clusterOriginator = entry.getValue().getClusterOriginator();
				if (pmid == clusterOriginator) {
					isClusterOriginator = true;
				}

				boolean isArticleSelected = false;
				if (articleList.contains(reCiterArticle)) {
					isArticleSelected = true;
				}
				AnalysisObject analysisObject = AnalysisTranslator.translate(
						reCiterArticle, statusEnum, cwid, reCiterClusterer.getTargetAuthor(), isClusterOriginator, 
						entry.getValue().getClusterID(), 
						entry.getValue().getArticleCluster().size(), 
						isArticleSelected);
				analysis.getAnalysisObjectList().add(analysisObject);
			}
		}


		analysis.setTruePos(numTruePos);
		return analysis;
	}

	public double getPrecision() {
		if (selectedClusterSize == 0) 
			return 0;
		return (double) truePos / selectedClusterSize;
	}

	public double getRecall() {
		if (goldStandardSize == 0)
			return 0;
		return (double) truePos / goldStandardSize;
	}

	public int getTruePos() {
		return truePos;
	}

	public void setTruePos(int truePos) {
		this.truePos = truePos;
	}

	public int getGoldStandardSize() {
		return goldStandardSize;
	}

	public void setGoldStandardSize(int goldStandardSize) {
		this.goldStandardSize = goldStandardSize;
	}

	public int getSelectedClusterSize() {
		return selectedClusterSize;
	}

	public void setSelectedClusterSize(int selectedClusterSize) {
		this.selectedClusterSize = selectedClusterSize;
	}

	public List<Integer> getFalsePositiveList() {
		return falsePositiveList;
	}

	public void setFalsePositiveList(List<Integer> falsePositiveList) {
		this.falsePositiveList = falsePositiveList;
	}

	public List<AnalysisObject> getAnalysisObjectList() {
		return analysisObjectList;
	}

	public void setAnalysisObjectList(List<AnalysisObject> analysisObjectList) {
		this.analysisObjectList = analysisObjectList;
	}
}
