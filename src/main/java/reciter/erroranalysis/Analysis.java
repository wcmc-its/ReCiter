package reciter.erroranalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that performs analysis such as calculating precision and recall.
 * @author jil3004
 *
 */
public class Analysis {

	private List<Integer> truePositiveList;
	private Set<Integer> goldStandard;
	private int sizeOfSelected;
	private int truePos;
	private int falsePos;

	public Analysis(Set<Integer> goldStandard) {
		this.goldStandard = goldStandard;
	}

	public Set<Integer> getGoldStandard() {
		return goldStandard;
	}
	
	public void setSizeOfSelected(int sizeOfSelected) {
		this.sizeOfSelected = sizeOfSelected;
	}
	
	public void setTruePositiveList(Set<Integer> pmidList) {
		truePos = 0;
		setFalsePos(0);
		truePositiveList = new ArrayList<Integer>();
		
		for (int pmid : pmidList) {
			if (goldStandard.contains(pmid)) {
				truePos++;
				truePositiveList.add(pmid);
			} else {
				setFalsePos(getFalsePos() + 1);
			}
		}
	}

	public double getPrecision() {
		double precision = (double) truePos / sizeOfSelected;
		return precision;
	}

	public double getRecall() {
		double recall = (double) truePos / goldStandard.size();
		return recall;
	}

	public int getFalsePos() {
		return falsePos;
	}

	public void setFalsePos(int falsePos) {
		this.falsePos = falsePos;
	}
}
