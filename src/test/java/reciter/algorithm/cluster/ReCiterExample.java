package reciter.algorithm.cluster;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.erroranalysis.Analysis;
import reciter.erroranalysis.AnalysisCSVWriter;
import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorEducation;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.utils.reader.YearDiscrepacyReader;
import reciter.utils.stemmer.PorterStemmer;
import reciter.utils.stemmer.SnowballStemmer;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;
import database.dao.GoldStandardPmidsDao;
import database.dao.IdentityDegreeDao;
import database.dao.IdentityDirectoryDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;
import database.dao.impl.IdentityDegreeDaoImpl;
import database.dao.impl.IdentityDirectoryDaoImpl;
import database.model.IdentityDegree;
import database.model.IdentityDirectory;

public class ReCiterExample {

	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);

	public static double totalPrecision = 0;
	public static double totalRecall = 0;
	public static int numCwids = 0;

	public static void main(String[] args) throws IOException {

		// Keep track of execution time of ReCiter .
		long startTime = System.currentTimeMillis();

		Files.walk(Paths.get(PubmedXmlFetcher.getDefaultLocation()))
				.forEach(
						filePath -> {
							if (Files.isRegularFile(filePath)) {
								String cwid = filePath.getFileName().toString()
										.replace("_0.xml", "");
								ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
								try {
									reCiterConfigProperty
											.loadProperty(ReCiterConfigProperty
													.getDefaultLocation()
													+ cwid
													+ "/"
													+ cwid
													+ ".properties");
								} catch (Exception e) {
									e.printStackTrace();
								}
								runExample(reCiterConfigProperty);
								numCwids++;
							}
						});

		slf4jLogger.info("Number of cwids: " + numCwids);
		slf4jLogger.info("Average Precision: " + totalPrecision / numCwids);
		slf4jLogger.info("Average Recall: " + totalRecall / numCwids);

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");

		/* Output the ReCiter performance summary as .csv file #75 */
		CSVFormat format;
		String CSV_OUTPUT = "src/main/resources/data/csv_output/";
		format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		PrintWriter writer = new PrintWriter(CSV_OUTPUT + ".csv", "UTF-8");
		CSVPrinter printer = new CSVPrinter(writer, format);

		String header = "Count 		CWID	Precision	Recall		Average of precision and recall";

		printer.print(numCwids);
		printer.print(totalPrecision);
		printer.print(totalRecall);

		printer.print(header);

		String summary = "Overall precision" + totalPrecision + "\n"
				+ "Overall recall" + totalRecall + "\n" + "Overall average"
				+ (totalRecall / numCwids);

		printer.print(summary);
		printer.close();
		writer.close();
	}

	/**
	 * Setup the data to run the ReCiter algorithm.
	 * 
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 */
	public static void runExample(ReCiterConfigProperty reCiterConfigProperty) {

		YearDiscrepacyReader.init();
		String lastName = reCiterConfigProperty.getLastName();
		String middleName = reCiterConfigProperty.getMiddleName();
		String firstName = reCiterConfigProperty.getFirstName();
		String affiliation = reCiterConfigProperty.getAuthorAffiliation();
		String firstInitial = firstName.substring(0, 1);
		String cwid = reCiterConfigProperty.getCwid();

		String authorKeywords = reCiterConfigProperty.getAuthorKeywords();
		String coAuthors = reCiterConfigProperty.getCoAuthors();
		double similarityThreshold = reCiterConfigProperty
				.getSimilarityThreshold();

		String department = reCiterConfigProperty.getAuthorDepartment();

		// Define Singleton target author.
		TargetAuthor targetAuthor = new TargetAuthor(new AuthorName(firstName,
				middleName, lastName), new AuthorAffiliation(affiliation));
		ReCiterArticle targetAuthorArticle = new ReCiterArticle(-1);
		targetAuthorArticle.setArticleCoAuthors(new ReCiterArticleAuthors());
		targetAuthorArticle.getArticleCoAuthors().addAuthor(
				new ReCiterAuthor(new AuthorName(firstName, middleName,
						lastName), new AuthorAffiliation(affiliation + " "
						+ department)));
		targetAuthor.setCwid(cwid);
		targetAuthorArticle.setArticleKeywords(new ReCiterArticleKeywords());
		IdentityDirectoryDao dao = new IdentityDirectoryDaoImpl();
		List<IdentityDirectory> identityDirectoryList = dao
				.getIdentityDirectoriesByCwid(cwid.toLowerCase());
		targetAuthor.setAliasList(identityDirectoryList);
		SnowballStemmer stemmer = new PorterStemmer();
		for (String keyword : authorKeywords.split(",")) {
			stemmer.setCurrent(keyword);
			stemmer.stem();
			String newKeyword = stemmer.getCurrent();
			targetAuthorArticle.getArticleKeywords().addKeyword(keyword);
			if(!keyword.equalsIgnoreCase(newKeyword))targetAuthorArticle.getArticleKeywords().addKeyword(newKeyword);
		}

		for (String author : coAuthors.split(",")) {
			String[] authorArray = author.split(" ");

			if (authorArray.length == 2) {
				String coAuthorFirstName = authorArray[0];
				String coAuthorLastName = authorArray[1];
				targetAuthorArticle.getArticleCoAuthors().addAuthor(
						new ReCiterAuthor(new AuthorName(coAuthorFirstName, "",
								coAuthorLastName), new AuthorAffiliation("")));
			} else if (authorArray.length == 3) {
				String coAuthorFirstName = authorArray[0];
				String coAuthorMiddleName = authorArray[1];
				String coAuthorLastName = authorArray[2];
				targetAuthorArticle.getArticleCoAuthors().addAuthor(
						new ReCiterAuthor(new AuthorName(coAuthorFirstName,
								coAuthorMiddleName, coAuthorLastName),
								new AuthorAffiliation("")));
			}
		}
			// Retrieve the PubMed articles for this cwid if the articles have
			// not been retrieved yet.
			PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
			if (identityDirectoryList != null
					&& identityDirectoryList.size() > 0) {
				for (IdentityDirectory dir : identityDirectoryList) {
					if (cwid.equals(dir.getCwid()))
						pubmedXmlFetcher.preparePubMedQueries(dir.getSurname(),
								dir.getGivenName(), dir.getMiddleName());
				}
			}
			List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher
					.getPubmedArticle(lastName, firstInitial, middleName, cwid);

			// Retrieve the scopus affiliation information for this cwid if the
			// affiliations have not been retrieve yet.
			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
			List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
			// Use target author's known publications to populate first cluster
			// Git Issue #22
			GoldStandardPmidsDao gspDao = new GoldStandardPmidsDaoImpl();
			List<String> gspPmidList = gspDao.getPmidsByCwid(cwid);
			//
			for (PubmedArticle pubmedArticle : pubmedArticleList) {
				String pmid = pubmedArticle.getMedlineCitation().getPmid()
						.getPmidString();
				if(gspPmidList.contains(pmid))gspPmidList.remove(pmid);
				ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(
						cwid, pmid);
				reCiterArticleList.add(ArticleTranslator.translate(
						pubmedArticle, scopusArticle));
			}

			// Github issue: https://github.com/wcmc-its/ReCiter/issues/84.
			for (ReCiterArticle article : reCiterArticleList) {
				/*
				 * For Phase One clustering, create a vector of topic keywords
				 * which includes individual words from: 1. journal title 2.
				 * MeSH major keywords (not the whole word itself - just
				 * individual words that are part of it; for matching entire
				 * MesH major terms, see issue #82) 3. article title
				 */
				// article.getArticleKeywords().addKeyword(keyword); // add the
				// keyword retrieved from above.
				applyVertorOfKeywords(article);
			}

			slf4jLogger.info("finished getting Scopus Xml");

			// Run the Clustering algorithm.

			// Filter the targetAuthorArticle (find by article id = -1).
			ReCiterArticle targetAuthorArticleIndexed = null;
			List<ReCiterArticle> filteredArticleList = new ArrayList<ReCiterArticle>();
	
			for (ReCiterArticle article : reCiterArticleList) {
				if (article.getArticleId() == -1) {
					targetAuthorArticleIndexed = article;
				} else {
					filteredArticleList.add(article);
				}
			}
	
			// clear unfiltered list.
			reCiterArticleList.clear();
			reCiterArticleList = null;

		
				
			IdentityDegreeDao identityDegreeDao = new IdentityDegreeDaoImpl();
			IdentityDegree identityDegree = identityDegreeDao.getIdentityDegreeByCwid(cwid);
			// assign the highest terminal year to TargetAuthor.
			AuthorEducation authorEducation = new AuthorEducation();
			
			if (identityDegree.getDoctoral() == 0) {
				if (identityDegree.getMasters() == 0) {
					if (identityDegree.getBachelor() == 0) {
						authorEducation.setDegreeYear(-1); // setting -1 to terminal year if no terminal degree present.
					} else {
						authorEducation.setDegreeYear(identityDegree.getBachelor());
					}
				} else {
					authorEducation.setDegreeYear(identityDegree.getMasters());
				}
			} else {
				authorEducation.setDegreeYear(identityDegree.getDoctoral());
			}

			// Set the indexed article for target author.
			targetAuthor.setEducation(authorEducation);

			// Sort articles on completeness score.
			Collections.sort(filteredArticleList);

			// Cluster.
			ReCiterClusterer reCiterClusterer = new ReCiterClusterer(cwid);

			// Report results.
			Set<Integer> pmidSet = new HashSet<Integer>();
			for(String pmidStr: gspPmidList){
				try{
					pmidSet.add(Integer.parseInt(pmidStr));
				}catch(Exception ex){
					
				}
			}

			Analysis analysis = reCiterClusterer.cluster(filteredArticleList);
			//int assignedClusterId = reCiterClusterer.getSelectedReCiterClusterId();
			slf4jLogger.info(reCiterClusterer.getClusterInfo());
			slf4jLogger.info("Precision=" + analysis.getPrecision());
			totalPrecision += analysis.getPrecision();
			slf4jLogger.info("Recall=" + analysis.getRecall());
			totalRecall += analysis.getRecall();
			slf4jLogger.info("False Positive List: " + analysis.getFalsePositiveList());
			slf4jLogger.info("\n");
			// Write analysis to CSV.
			AnalysisCSVWriter analysisCSVWriter = new AnalysisCSVWriter();
			try {
				analysisCSVWriter.write(
						analysis.getAnalysisObjectList(), cwid, analysis.getPrecision(), analysis.getRecall());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	// Github issue: https://github.com/wcmc-its/ReCiter/issues/84.
	public static void applyVertorOfKeywords(ReCiterArticle article) {
		List<String> articleKeywordList = Arrays.asList(article.getArticleTitle().split(" "));
		SnowballStemmer stemmer = new PorterStemmer();
		 int repeat=1;
		for (String articleKeyword : articleKeywordList) {
			if(!article.getArticleKeywords().isKeywordExist(articleKeyword))article.getArticleKeywords().addKeyword(articleKeyword);
			stemmer.setCurrent(articleKeyword);
			 for (int i = repeat; i != 0; i--) {
				 	stemmer.stem();
			 }
			 String kwd = stemmer.getCurrent();
			 if(!articleKeywordList.contains(kwd) && !article.getArticleKeywords().isKeywordExist(kwd))
				 article.getArticleKeywords().addKeyword(kwd);
		}

		// Get Journal Tile and Split for Keywords
		List<String> journalKeywordList = Arrays.asList(article.getJournal().getJournalTitle().split(" "));
		for (String journalKeyword : journalKeywordList) {
			if(!article.getArticleKeywords().isKeywordExist(journalKeyword))article.getArticleKeywords().addKeyword(journalKeyword);
			stemmer.setCurrent(journalKeyword);
			 for (int i = repeat; i != 0; i--) {
				 	stemmer.stem();
			 }
			 String kwd = stemmer.getCurrent();
			 if(!journalKeywordList.contains(kwd) && !article.getArticleKeywords().isKeywordExist(kwd))
				 article.getArticleKeywords().addKeyword(kwd);
		}

		// TO DO, Not clear for getting the MeshKeywords

		/*
		 * MedlineCitationMeshHeadingDescriptorName meshName = new
		 * MedlineCitationMeshHeadingDescriptorName(); String [] meshKeywordList
		 * = meshName.getDescriptorNameString().split(" "); for (String
		 * meshKeyword : meshKeywordList ) {
		 * article.getArticleKeywords().addKeyword(meshKeyword); }
		 */
	}

	// Add primary and/or other department name(s) to list of topic keywords #46
	// https://github.com/wcmc-its/ReCiter/issues/46
	/*
	 * public ArrayList<String> getListOfTopicKeywords() { ArrayList<String>
	 * topicOfKeywords = new ArrayList<String>(); // Get Article Tile and Split
	 * for Keywords MatchingDepartmentsJournalsDao translatedDepartmentList =
	 * new MatchingDepartmentsJournalsDao(); List<String> departmentList =
	 * translatedDepartmentList.getDepartmentalAffiliationStringList(); for
	 * (String departmentKeyword : departmentList ) {
	 * //article.getArticleKeywords().addKeyword(articleKeyword); if
	 * (departmentKeyword!=null && (departmentKeyword.equals("and") ||
	 * departmentKeyword.equals("or") || departmentKeyword.equals("of") ||
	 * departmentKeyword.equals("for") || departmentKeyword.equals(" ") ||
	 * departmentKeyword.equals("null"))) { continue; }
	 * topicOfKeywords.add(departmentKeyword); } return topicOfKeywords; }
	 */
}