package main.reciter.analysis;

import java.util.ArrayList;
import java.util.List;

import main.reciter.model.article.ReCiterArticle;

public class AnalysisObject {

	private static List<AnalysisObject> analysisObjectList = new ArrayList<AnalysisObject>();
	private static List<AnalysisObject> allAnalysisObjectList = new ArrayList<AnalysisObject>();
	
	private double similarityMeasure;
	private ReCiterArticle reCiterArticle;
	private int clusterId; // Cluster to which the article was assigned
	private int numArticlesInCluster; // Number of articles in the selected cluster
	private String status; // Status of ReCiter's determination for this article with respect to reference standard
	private boolean selected; // Cluster ultimately selected in Phase 2 matching
	private String info;
	private int yearOfPublication;
	
	public double getSimilarityMeasure() {
		return similarityMeasure;
	}
	public void setSimilarityMeasure(double similarityMeasure) {
		this.similarityMeasure = similarityMeasure;
	}
	public int getClusterId() {
		return clusterId;
	}
	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	public int getNumArticlesInCluster() {
		return numArticlesInCluster;
	}
	public void setNumArticlesInCluster(int numArticlesInCluster) {
		this.numArticlesInCluster = numArticlesInCluster;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public ReCiterArticle getReCiterArticle() {
		return reCiterArticle;
	}
	public void setReCiterArticle(ReCiterArticle reCiterArticle) {
		this.reCiterArticle = reCiterArticle;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public static List<AnalysisObject> getAnalysisObjectList() {
		return analysisObjectList;
	}

	public static void setAnalysisObjectList(List<AnalysisObject> analysisObjectList) {
		AnalysisObject.analysisObjectList = analysisObjectList;
	}
	public int getYearOfPublication() {
		return yearOfPublication;
	}
	public void setYearOfPublication(int yearOfPublication) {
		this.yearOfPublication = yearOfPublication;
	}
	public static List<AnalysisObject> getAllAnalysisObjectList() {
		return allAnalysisObjectList;
	}
	public static void setAllAnalysisObjectList(List<AnalysisObject> allAnalysisObjectList) {
		AnalysisObject.allAnalysisObjectList = allAnalysisObjectList;
	}
	
}
