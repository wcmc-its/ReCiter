package reciter.algorithm.cluster;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.ReCiterArticleFetcher;
import reciter.model.article.ReCiterArticle;

public class ReCiterExample {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterExample.class);

	public static double totalPrecision = 0;
	public static double totalRecall = 0;
	public static int numCwids = 0;

	public static void main(String[] args) throws IOException {

		// Keep track of execution time of ReCiter .
		long startTime = System.currentTimeMillis();

		slf4jLogger.info("Number of cwids: " + numCwids);
		slf4jLogger.info("Average Precision: " + totalPrecision / numCwids);
		slf4jLogger.info("Average Recall: " + totalRecall / numCwids);

		runExample("Darwich", "A", "aad2004");
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");
	}

	/**
	 * Setup the data to run the ReCiter algorithm.
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 */
	public static void runExample(String lastName, String firstInitial, String cwid) {
		
		// Fetch the articles for this person.
		List<ReCiterArticle> reCiterArticleList = new ReCiterArticleFetcher().fetch(lastName, firstInitial, cwid);
		
		// Perform clustering.
		Clusterer clusterer = new ReCiterClusterer();
		clusterer.cluster(reCiterArticleList);
		
		// make the target author the cluster originator.
		
		// Iterate through the similarity once and assign the most similar one. (inner layer).
		
		// Iterate through second time to compare against others.
		
		// Multiple times.
	}


	// Github issue: https://github.com/wcmc-its/ReCiter/issues/84.	
	public static void applyVertorOfKeywords(ReCiterArticle article) {		
		//ArrayList<String> vectorOfKeywords = new ArrayList<String>();
		// Get Article Tile and Split for Keywords 
		//ReCiterArticle article = new ReCiterArticle(-1);
		String [] articleKeywordList  = article.getArticleTitle().split(" ");
		for (String articleKeyword : articleKeywordList ) {
			article.getArticleKeywords().addKeyword(articleKeyword);
		}

		// Get Journal Tile and Split for Keywords 
		String [] journalKeywordList = article.getJournal().getJournalTitle().split(" ");
		for (String journalKeyword : journalKeywordList ) {
			article.getArticleKeywords().addKeyword(journalKeyword);
		}

		// TO DO, Not clear for getting the MeshKeywords 

		/*MedlineCitationMeshHeadingDescriptorName meshName = new MedlineCitationMeshHeadingDescriptorName();   
		String [] meshKeywordList = meshName.getDescriptorNameString().split(" "); 
		for (String meshKeyword : meshKeywordList ) {
			article.getArticleKeywords().addKeyword(meshKeyword);
		}*/
	}

	// Add primary and/or other department name(s) to list of topic keywords #46 
	// https://github.com/wcmc-its/ReCiter/issues/46
	/*public ArrayList<String> getListOfTopicKeywords() {			
		ArrayList<String> topicOfKeywords = new ArrayList<String>();
		// Get Article Tile and Split for Keywords 
		MatchingDepartmentsJournalsDao translatedDepartmentList = new MatchingDepartmentsJournalsDao();
		List<String> departmentList = translatedDepartmentList.getDepartmentalAffiliationStringList();
		for (String departmentKeyword : departmentList ) {
			//article.getArticleKeywords().addKeyword(articleKeyword);
			if (departmentKeyword!=null && (departmentKeyword.equals("and") || departmentKeyword.equals("or") || departmentKeyword.equals("of") || departmentKeyword.equals("for") || departmentKeyword.equals(" ") || departmentKeyword.equals("null"))) { continue;  }
			topicOfKeywords.add(departmentKeyword);
		}
		return topicOfKeywords; 
	}*/
}
