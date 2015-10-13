package reciter.algorithm.cluster;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.dao.IdentityDao;
import database.dao.impl.IdentityDaoImpl;
import database.model.Identity;
import reciter.erroranalysis.Analysis;
import reciter.model.ReCiterArticleFetcher;
import reciter.model.article.ReCiterArticle;

public class ReCiterExample {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterExample.class);

	public static double totalPrecision = 0;
	public static double totalRecall = 0;

	public static void main(String[] args) throws IOException {
//		runExample("Falk", "A", "", "avf2003");
		 runTestCwids(); // run for 63 cwids.
	}

	/**
	 * Run reciter for cwids in file test_data_cwid_list.txt.
	 */
	public static void runTestCwids() {
		long startTime = System.currentTimeMillis();

		List<String> cwids = getListOfCwids();
		IdentityDao identityDao = new IdentityDaoImpl();

		for (String cwid : cwids) {
			slf4jLogger.info("cwid=" + cwid);
			Identity identity = identityDao.getIdentityByCwid(cwid);
			String firstInitial = identity.getFirstInitial();
			String lastName = identity.getLastName();
			String middleName = identity.getMiddleName();
			runExample(lastName, firstInitial, middleName, cwid);
		}

		slf4jLogger.info("Number of cwids: " + cwids.size());
		slf4jLogger.info("Average Precision: " + totalPrecision / cwids.size());
		slf4jLogger.info("Average Recall: " + totalRecall / cwids.size());

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");
	}
	
	public static List<String> getListOfCwids() {
		List<String> cwids = new ArrayList<String>();
		try (Stream<String> stream = Files.lines(Paths.get("src/main/resources/data/test_data_cwid_list.txt"),Charset.defaultCharset())) {
			stream.forEach(e -> cwids.add(e));
		} catch (IOException ex) {
			// do something with exception
		} 
		return cwids;
	}

	/**
	 * Setup the data to run the ReCiter algorithm.
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 */
	public static void runExample(String lastName, String firstInitial, String middleName, String cwid) {

		// Fetch the articles for this person.
		List<ReCiterArticle> reCiterArticleList = new ReCiterArticleFetcher().fetch(lastName, firstInitial, middleName, cwid);

		// Perform clustering.
		ReCiterClusterer clusterer = new ReCiterClusterer(cwid);
		Analysis analysis = clusterer.cluster(reCiterArticleList);

		slf4jLogger.info(clusterer.getClusterInfo());
		slf4jLogger.info("Precision=" + analysis.getPrecision());
		totalPrecision += analysis.getPrecision();
		slf4jLogger.info("Recall=" + analysis.getRecall());
		totalRecall += analysis.getRecall();
		slf4jLogger.info("True Positive List [" + analysis.getTruePositiveList().size() + "]: " + analysis.getTruePositiveList());
		slf4jLogger.info("True Negative List: [" + analysis.getTrueNegativeList().size() + "]: " + analysis.getTrueNegativeList());
		slf4jLogger.info("False Positive List: [" + analysis.getFalsePositiveList().size() + "]: " + analysis.getFalsePositiveList());
		slf4jLogger.info("False Negative List: [" + analysis.getFalseNegativeList().size() + "]: " + analysis.getFalseNegativeList());
//		
//		slf4jLogger.info("# of True Positives: " + analysis.getTruePos());
//		slf4jLogger.info("# of True Negatives: " + analysis.getTrueNeg());
//		slf4jLogger.info("# of False Positives: " + analysis.getFalsePos());
//		slf4jLogger.info("# of False Negatives: " + analysis.getFalseNeg());
//		slf4jLogger.info("# of Selected cluster size: " + analysis.getSelectedClusterSize());
//		slf4jLogger.info("# of Gold standard size: " + analysis.getGoldStandardSize());
//
//		slf4jLogger.info("True Positive Journal Count: " + analysis.getTruePositiveJournalCount());
//		slf4jLogger.info("True Negative Journal Count: " + analysis.getTrueNegativeJournalCount());
//		slf4jLogger.info("False Positive Journal Count: " + analysis.getFalsePositiveJournalCount());
//		slf4jLogger.info("False Negative Journal Count: " + analysis.getFalseNegativeJournalCount());
		
		Set<String> s1 = analysis.getTruePositiveJournalCount().keySet();
		s1.retainAll(analysis.getFalseNegativeJournalCount().keySet());
		
//		slf4jLogger.info("Intersection between True Positive and False Negative Journal Count: " + s1);
		slf4jLogger.info("\n");
		// Write analysis to CSV.
//		AnalysisCSVWriter analysisCSVWriter = new AnalysisCSVWriter();
//		try {
//			analysisCSVWriter.write(
//					analysis.getAnalysisObjectList(), cwid, analysis.getPrecision(), analysis.getRecall());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		// write to database.
//		AnalysisDao analysisDao = new AnalysisDaoImpl();
//		analysisDao.emptyTable();
//		analysisDao.insertAnalysisList(AnalysisConverter.convertToAnalysisList(analysis.getAnalysisObjectList()));
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