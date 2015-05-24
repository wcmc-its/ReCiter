package test.examples.pubmed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.database.dao.ArticleDao;
import main.database.dao.IdentityDegree;
import main.database.dao.IdentityDegreeDao;
import main.reciter.algorithm.cluster.ReCiterClusterer;
import main.reciter.lucene.DocumentIndexReader;
import main.reciter.lucene.DocumentIndexWriter;
import main.reciter.lucene.DocumentTranslator;
import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.article.ReCiterArticleCoAuthors;
import main.reciter.model.article.ReCiterArticleKeywords;
import main.reciter.model.author.AuthorAffiliation;
import main.reciter.model.author.AuthorName;
import main.reciter.model.author.ReCiterAuthor;
import main.reciter.model.author.TargetAuthor;
import main.reciter.utils.Analysis;
import main.reciter.utils.AnalysisCSVWriter;
import main.reciter.utils.AnalysisObject;
import main.reciter.utils.ReCiterConfigProperty;
import main.reciter.utils.YearDiscrepacyReader;
import main.xml.pubmed.PubmedXmlFetcher;
import main.xml.pubmed.model.MedlineCitationArticleAuthor;
import main.xml.pubmed.model.PubmedArticle;
import main.xml.scopus.ScopusXmlFetcher;
import main.xml.scopus.model.ScopusEntry;
import main.xml.translator.ArticleTranslator;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReCiterExampleTest {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterExampleTest.class);

	public static double totalPrecision = 0;
	public static double totalRecall = 0;
	public static int numCwids = 0;
	public static void main(String[] args) throws IOException {

		// Keep track of execution time of ReCiter .
		long startTime = System.currentTimeMillis();
		Files.walk(Paths.get("data/test_xml")).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				String cwid = filePath.getFileName().toString().replace(".xml", "");
				ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
				try {
					reCiterConfigProperty.loadProperty("data/properties/" + cwid + "/" + cwid + ".properties");
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

		AnalysisCSVWriter analysisCSVWriter = new AnalysisCSVWriter();
		analysisCSVWriter.write(AnalysisObject.getAllAnalysisObjectList(), "all_target");
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
	public static void runExample(ReCiterConfigProperty reCiterConfigProperty) {

		YearDiscrepacyReader.init();
		//		reCiterConfigProperty.setPerformRetrievePublication(true);
		String lastName = reCiterConfigProperty.getLastName();
		String middleName = reCiterConfigProperty.getMiddleName();
		String firstName = reCiterConfigProperty.getFirstName();
		String affiliation = reCiterConfigProperty.getAuthorAffiliation();
		String firstInitial = firstName.substring(0, 1);
		String cwid = reCiterConfigProperty.getCwid();

		String authorKeywords = reCiterConfigProperty.getAuthorKeywords();
		String coAuthors = reCiterConfigProperty.getCoAuthors();
		double similarityThreshold = reCiterConfigProperty.getSimilarityThreshold();

		String department = reCiterConfigProperty.getAuthorDepartment();

		DocumentIndexWriter.setUseStopWords(reCiterConfigProperty.isUseStemming()); // revise this logic.

		// Define Singleton target author.
		TargetAuthor.init(new AuthorName(firstName, middleName, lastName), new AuthorAffiliation(affiliation));
		ReCiterArticle targetAuthorArticle = new ReCiterArticle(-1);
		targetAuthorArticle.setArticleCoAuthors(new ReCiterArticleCoAuthors());
		targetAuthorArticle.getArticleCoAuthors().addCoAuthor(new ReCiterAuthor(new AuthorName(firstName, middleName, lastName), new AuthorAffiliation(affiliation + " " + department)));		
		TargetAuthor.getInstance().setCwid(cwid);
		targetAuthorArticle.setArticleKeywords(new ReCiterArticleKeywords());

		for (String keyword : authorKeywords.split(",")) {
			targetAuthorArticle.getArticleKeywords().addKeyword(keyword);
		}

		for (String author : coAuthors.split(",")) {
			String[] authorArray = author.split(" ");

			if (authorArray.length == 2) {
				String coAuthorFirstName = authorArray[0];
				String coAuthorLastName = authorArray[1];
				targetAuthorArticle.getArticleCoAuthors().addCoAuthor(new ReCiterAuthor(new AuthorName(coAuthorFirstName, "", coAuthorLastName), new AuthorAffiliation("")));
			} else if (authorArray.length == 3) {
				String coAuthorFirstName = authorArray[0];
				String coAuthorMiddleName = authorArray[1];
				String coAuthorLastName = authorArray[2];
				targetAuthorArticle.getArticleCoAuthors().addCoAuthor(new ReCiterAuthor(new AuthorName(coAuthorFirstName, coAuthorMiddleName, coAuthorLastName), new AuthorAffiliation("")));
			}
		}


		// Try reading from Lucene Index:
		DocumentIndexReader documentIndexReader = new DocumentIndexReader();

		// Lucene Index doesn't contain this cwid's files.
		if (!documentIndexReader.isIndexed(cwid)) {

			slf4jLogger.debug("Indexing...");

			// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
			PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
			pubmedXmlFetcher.setPerformRetrievePublication(reCiterConfigProperty.isPerformRetrievePublication());
			List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstInitial, cwid);

			// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
			List<ScopusEntry> scopusEntryList = scopusXmlFetcher.getScopusEntryList(lastName, firstInitial, cwid);

			// Map the pmid to a ScopusEntry.
			Map<String, ScopusEntry> pmidToScopusEntry = new HashMap<String, ScopusEntry>();
			for (ScopusEntry entry : scopusEntryList) {
				pmidToScopusEntry.put(entry.getPubmedID(), entry);
			}

			// Need to integrate the Scopus information into PubmedArticle. Add a fake author which contains the
			// Scopus Affiliation. The fake author has pmid as last name and first name.
			for (PubmedArticle pubmedArticle : pubmedArticleList) {
				String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();

				if (pmidToScopusEntry.containsKey(pmid)) {
					String scopusAffiliation = pmidToScopusEntry.get(pmid).affiliationConcatForm();
					MedlineCitationArticleAuthor fakeAuthor = new MedlineCitationArticleAuthor();
					fakeAuthor.setLastName(pmid);
					fakeAuthor.setForeName(pmid);
					fakeAuthor.setAffiliation(scopusAffiliation);
					if (pubmedArticle.getMedlineCitation().getArticle().getAuthorList() == null) {
						pubmedArticle.getMedlineCitation().getArticle().setAuthorList(new ArrayList<MedlineCitationArticleAuthor>());
					}
					pubmedArticle.getMedlineCitation().getArticle().getAuthorList().add(fakeAuthor);
				}
			}

			// Convert PubmedArticle to ReCiterArticle.
			List<ReCiterArticle> reCiterArticleList = ArticleTranslator.translateAll(pubmedArticleList);

			// Add TargetAuthor Article:
			reCiterArticleList.add(targetAuthorArticle);

			// Convert ReCiterArticle to Lucene Document
			List<Document> luceneDocumentList = DocumentTranslator.translateAll(reCiterArticleList);

			// If Lucene index already exist for this cwid, read from index. Else write to index.
			// Use Lucene to write to index.
			DocumentIndexWriter docIndexWriter = new DocumentIndexWriter(cwid);
			docIndexWriter.indexAll(luceneDocumentList);
		}

		// Read the index from directory data/lucene and convert the indexed files to a list of ReCiterArticle.
		List<ReCiterArticle> reCiterArticleList = documentIndexReader.readIndex(cwid);

		// Run the Clustering algorithm.

		// Filter the targetAuthorArticle (find by article id = -1).
		ReCiterArticle targetAuthorArticleIndexed = null;
		List<ReCiterArticle> filteredArticleList = new ArrayList<ReCiterArticle>();

		for (ReCiterArticle article : reCiterArticleList) {
			if (article.getArticleID() == -1) {
				targetAuthorArticleIndexed = article;
			} else {
				filteredArticleList.add(article);
			}
		}

		// clear unfiltered list.
		reCiterArticleList.clear();
		reCiterArticleList = null;

		IdentityDegreeDao identityDegreeDao = new IdentityDegreeDao();
		IdentityDegree identityDegree = identityDegreeDao.getIdentityDegreeByCwid(cwid);
		// assign the highest terminal year to TargetAuthor.
		if (identityDegree.getDoctoral() == 0) {
			if (identityDegree.getMasters() == 0) {
				if (identityDegree.getBachelor() == 0) {
					TargetAuthor.getInstance().setTerminalDegreeYear(-1); // setting -1 to terminal year if no terminal degree present.
				} else {
					TargetAuthor.getInstance().setTerminalDegreeYear(identityDegree.getBachelor());
				}
			} else {
				TargetAuthor.getInstance().setTerminalDegreeYear(identityDegree.getMasters());
			}
		} else {
			TargetAuthor.getInstance().setTerminalDegreeYear(identityDegree.getDoctoral());
		}

		// Set the indexed article for target author.
		TargetAuthor.getInstance().setTargetAuthorArticleIndexed(targetAuthorArticleIndexed);

		// Sort articles on completeness score.
		Collections.sort(filteredArticleList);

		// Cluster.
		ReCiterClusterer reCiterClusterer = new ReCiterClusterer();
		reCiterClusterer.setArticleList(filteredArticleList);

		// Report results.
		ArticleDao articleDao = new ArticleDao();
		Set<Integer> pmidSet = articleDao.getPmidList(cwid);

		Analysis analysis = new Analysis(pmidSet);
		reCiterClusterer.cluster(similarityThreshold, 0.1, analysis);

		// Write to CSV.
		AnalysisCSVWriter analysisCSVWriter = new AnalysisCSVWriter();
		try {
			analysisCSVWriter.write(AnalysisObject.getAnalysisObjectList(), cwid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
