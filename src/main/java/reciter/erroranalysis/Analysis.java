package reciter.erroranalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.article.ReCiterArticle;
import database.dao.GoldStandardPmidsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;

/**
 * Class that performs analysis such as calculating precision and recall.
 * @author jil3004
 *
 */
public class Analysis {

	private int truePos;
	private int goldStandardSize;
	private int selectedClusterSize;
	
	public Analysis() {}
	
	public static Analysis performAnalysis(Map<Integer, ReCiterCluster> finalCluster, int selection, String cwid) {
		Analysis analysis = new Analysis();
		GoldStandardPmidsDao goldStandardPmidsDao = new GoldStandardPmidsDaoImpl();
		List<String> goldStandardPmids = goldStandardPmidsDao.getPmidsByCwid(cwid);
		
		analysis.setGoldStandardSize(goldStandardPmids.size());
		analysis.setSelectedClusterSize(finalCluster.get(selection).getArticleCluster().size());
		int numTruePos = 0;

		// get number of true positives.
		for (ReCiterArticle reCiterArticle : finalCluster.get(selection).getArticleCluster()) {
			int pmid = reCiterArticle.getArticleId();
			if (goldStandardPmids.contains(Integer.toString(pmid))) {
				numTruePos++;
			}
		}
		analysis.setTruePos(numTruePos);
		return analysis;
	}

	public double getPrecision() {
		double precision = (double) truePos / selectedClusterSize;
		return precision;
	}

	public double getRecall() {
		double recall = (double) truePos / goldStandardSize;
		return recall;
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
}
