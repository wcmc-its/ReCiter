package reciter.algorithm.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import database.dao.ArticleDao;
import database.dao.IdentityDegree;
import database.dao.IdentityDegreeDao;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.lucene.DocumentIndexReader;
import reciter.lucene.DocumentIndexWriter;
import reciter.lucene.DocumentTranslator;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleCoAuthors;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.MedlineCitationArticleAuthor;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.Affiliation;
import xmlparser.scopus.model.Author;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.erroranalysis.Analysis;
import reciter.erroranalysis.AnalysisCSVWriter;
import reciter.erroranalysis.AnalysisObject;
import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.erroranalysis.YearDiscrepacyReader;

public class ReCiterExampleSingleCwid {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterExampleSingleCwid.class);

	public static void main(String[] args) throws IOException {
		// Read Property file from config.properties.
		ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
		reCiterConfigProperty.loadProperty("data/config.properties");

		// Keep track of exeuction time of ReCiter.
		long startTime = System.currentTimeMillis();

		// Run ReCiter.
		ReCiterExampleSingleCwid reCiterExample = new ReCiterExampleSingleCwid();
		reCiterExample.runExample(reCiterConfigProperty);

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
	public void runExample(ReCiterConfigProperty reCiterConfigProperty) {

		YearDiscrepacyReader.init();
		
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

			// Need to integrate the Scopus information into PubmedArticle. Add a fake author which contains the
			// Scopus Affiliation. The fake author has pmid as last name and first name.
			for (PubmedArticle pubmedArticle : pubmedArticleList) {
				String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
				ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
				
				if (scopusArticle != null) {
					for (MedlineCitationArticleAuthor author : pubmedArticle.getMedlineCitation().getArticle().getAuthorList()) {
						for (Author scopusAuthor : scopusArticle.getAuthors().values()) {
							if (scopusAuthor.getSurname().equals(author.getLastName())) {
								StringBuilder scopusAffiliationStringBuilder = new StringBuilder();
								for (Integer afid : scopusAuthor.getAfidSet()) {
									Affiliation scopusAffiliation = scopusArticle.getAffiliationMap().get(afid);
									scopusAffiliationStringBuilder.append(scopusAffiliation.getAffiliationCity() + " ");
									scopusAffiliationStringBuilder.append(scopusAffiliation.getAffiliationCountry() + " ");
									scopusAffiliationStringBuilder.append(scopusAffiliation.getAffilname() + " ");
									scopusAffiliationStringBuilder.append(scopusAffiliation.getNameVariant() + " ");
								}
								author.setAffiliation(author.getAffiliation() + scopusAffiliationStringBuilder.toString());
								break;
							}
						}
					}
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
//			analysisCSVWriter.writePythonCSV(AnalysisObject.getAnalysisObjectList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
