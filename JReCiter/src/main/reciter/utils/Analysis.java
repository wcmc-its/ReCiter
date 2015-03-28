package main.reciter.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Analysis {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(Analysis.class);	
	private List<Integer> truePositiveList;
	private Set<Integer> goldStandard;
	private int sizeOfSelected;
	private int truePos;
	private int trueNeg;
	private int falsePos;
	private int falseNeg;
	
	public Analysis(Set<Integer> goldStandard) {
		this.goldStandard = goldStandard;
	}

	public void getTruePositiveList(Set<Integer> goldStandard, List<Integer> pmidList) {
		truePositiveList = new ArrayList<Integer>();
		for (int pmid : pmidList) {
			if (goldStandard.contains(pmid)) {
				truePos++;
				truePositiveList.add(pmid);
			} else {
				falsePos++;
			}
		}
	}
	
	public double getPrecision() {
		double precision = (double) truePos / sizeOfSelected;
		slf4jLogger.info("Precision: " + precision);
		return precision;
	}
	
	public double getRecall() {
		double recall = (double) truePos / goldStandard.size();
		slf4jLogger.info("Recall: " + recall);
		return recall;
	}
	
	
}
