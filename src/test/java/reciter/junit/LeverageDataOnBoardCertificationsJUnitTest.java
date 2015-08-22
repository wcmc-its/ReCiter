package reciter.junit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.erroranalysis.Analysis;
import reciter.erroranalysis.AnalysisObject;
import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.erroranalysis.StatusEnum;
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
import reciter.utils.reader.YearDiscrepacyReader;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;
import database.dao.ArticleDao;
import database.dao.IdentityDegreeDao;
import database.model.IdentityDegree;

public class LeverageDataOnBoardCertificationsJUnitTest {
	private final static Logger slf4jLogger = LoggerFactory.getLogger(LeverageDataOnBoardCertificationsJUnitTest.class);
	private String cwid = "tac2001";
	private String lastName;
	private String middleName;
	private String firstName;
	private String affiliation;
	private String firstInitial;
	private String authorKeywords;
	private String coAuthors;
	private double similarityThreshold;
	private String department;
	private ReCiterArticle targetAuthorArticle;
	private List<ReCiterArticle> reCiterArticleList;
	private List<ReCiterArticle> filteredArticleList;
	private double totalPrecision = 0;
	private double totalRecall = 0;
	
	
	@Before
	public void init() throws IOException{
		ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();		
		reCiterConfigProperty.loadProperty(ReCiterConfigProperty.getDefaultLocation() + cwid + "/" + cwid + ".properties");
		YearDiscrepacyReader.init();
		lastName = reCiterConfigProperty.getLastName();
		middleName = reCiterConfigProperty.getMiddleName();
		firstName = reCiterConfigProperty.getFirstName();
		affiliation = reCiterConfigProperty.getAuthorAffiliation();
		firstInitial = firstName.substring(0, 1);
		authorKeywords = reCiterConfigProperty.getAuthorKeywords();
		coAuthors = reCiterConfigProperty.getCoAuthors();
		similarityThreshold = reCiterConfigProperty.getSimilarityThreshold();
		department = reCiterConfigProperty.getAuthorDepartment();
		DocumentIndexWriter.setUseStopWords(reCiterConfigProperty.isUseStemming()); 
		TargetAuthor.init(new AuthorName(firstName, middleName, lastName), new AuthorAffiliation(affiliation));
		targetAuthorArticle = new ReCiterArticle(-1);
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
		DocumentIndexReader documentIndexReader = new DocumentIndexReader();

		// Lucene Index doesn't contain this cwid's files.
		if (!documentIndexReader.isIndexed(cwid)) {

			// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
			PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
			List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstInitial, middleName,cwid);

			// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
			List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();

			for (PubmedArticle pubmedArticle : pubmedArticleList) {
				String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
				ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);

				reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
			}

			
			for (ReCiterArticle article : reCiterArticleList) {
				String [] articleKeywordList  = article.getArticleTitle().getTitle().split(" ");
				for (String articleKeyword : articleKeywordList ) {
					article.getArticleKeywords().addKeyword(articleKeyword);
				}
				
				// Get Journal Tile and Split for Keywords 
				String [] journalKeywordList = article.getJournal().getJournalTitle().split(" ");
				for (String journalKeyword : journalKeywordList ) {
					article.getArticleKeywords().addKeyword(journalKeyword);
				}
			}
			// Add TargetAuthor Article:
			reCiterArticleList.add(targetAuthorArticle);

			// Convert ReCiterArticle to Lucene Document
			List<Document> luceneDocumentList = DocumentTranslator.translateAll(reCiterArticleList);

			// If Lucene index already exist for this cwid, read from index. Else write to index.
			// Use Lucene to write to index.
			DocumentIndexWriter docIndexWriter = new DocumentIndexWriter(cwid);
			docIndexWriter.indexAll(luceneDocumentList);
		}
		reCiterArticleList = documentIndexReader.readIndex(cwid);
		ReCiterArticle targetAuthorArticleIndexed = null;
		filteredArticleList = new ArrayList<ReCiterArticle>();
		for (ReCiterArticle article : reCiterArticleList) {
			if (article.getArticleID() == -1) {
				targetAuthorArticleIndexed = article;
			} else {
				filteredArticleList.add(article);
			}
		}
		reCiterArticleList.clear();
		reCiterArticleList = null;
		IdentityDegreeDao identityDegreeDao = new IdentityDegreeDao();
		IdentityDegree identityDegree = identityDegreeDao.getIdentityDegreeByCwid(cwid);
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
		TargetAuthor.getInstance().setTargetAuthorArticleIndexed(targetAuthorArticleIndexed);
		Collections.sort(filteredArticleList);
	}
	
	@Test
	public void doTest(){
		ReCiterClusterer reCiterClusterer = new ReCiterClusterer();
		ArticleDao articleDao = new ArticleDao();
		Set<Integer> pmidSet = articleDao.getPmidList(cwid);
		Analysis analysis = new Analysis(pmidSet);
		Set<Integer> selectedArticleIdSet = new HashSet<Integer>();
		reCiterClusterer.cluster(filteredArticleList);
		int assignedClusterId = reCiterClusterer.assignTargetToCluster(TargetAuthor.getInstance().getTargetAuthorArticleIndexed());
		for (ReCiterCluster reCiterCluster : reCiterClusterer.getFinalCluster().values()) {
			for (ReCiterArticle article : reCiterCluster.getArticleCluster()) {
				article.setAnalysisObject(new AnalysisObject());
				if (reCiterCluster.getClusterID() == reCiterClusterer.getSelectedReCiterClusterId()) {
					if (pmidSet.contains(article.getArticleID())) {
						article.getAnalysisObject().setStatus(StatusEnum.TRUE_POSITIVE);
						selectedArticleIdSet.add(article.getArticleID());
					} else {
						article.getAnalysisObject().setStatus(StatusEnum.FALSE_POSITIVE);
					}
				} else {
					if (pmidSet.contains(article.getArticleID())) {
						article.getAnalysisObject().setStatus(StatusEnum.FALSE_NEGATIVE);
					} else {
						article.getAnalysisObject().setStatus(StatusEnum.TRUE_NEGATIVE);
					}
				}
			}
		}
		if (reCiterClusterer.getSelectedReCiterClusterId() != -1) {
			analysis.setTruePositiveList(selectedArticleIdSet);
			analysis.setSizeOfSelected(selectedArticleIdSet.size());
			slf4jLogger.debug("Precision=" + analysis.getPrecision());
			totalPrecision += analysis.getPrecision();
			slf4jLogger.debug("Recall=" + analysis.getRecall());
			totalRecall += analysis.getRecall();
		} else {
			slf4jLogger.info("No cluster selected in phase 2 matching.");
			slf4jLogger.debug("Precision=" + 0);
			slf4jLogger.debug("Recall=" + 0);
		}

		for (ReCiterArticle article : filteredArticleList) {
			// Information Retrieval.
			article.getAnalysisObject().setCwid(cwid);
			article.getAnalysisObject().setTargetName(TargetAuthor.getInstance().getAuthorName().toString());
			article.getAnalysisObject().setPubmedSearchQuery(AuthorName.getPubmedQueryFormat(TargetAuthor.getInstance().getAuthorName()));

			// Pre-Processing.
			article.getAnalysisObject().setPmid(String.valueOf(article.getArticleID()));
			article.getAnalysisObject().setArticleTitle(article.getArticleTitle().getTitle());
			article.getAnalysisObject().setFullJournalTitle(article.getJournal().getJournalTitle());
			article.getAnalysisObject().setPublicationYear(String.valueOf(article.getJournal().getJournalIssuePubDateYear()));
			article.getAnalysisObject().setScopusTargetAuthorAffiliation(article.getScopusAffiliation());
			article.getAnalysisObject().setScopusCoAuthorAffiliation(article.getScopusAffiliation());
			article.getAnalysisObject().setPubmedTargetAuthorAffiliation(article.getAffiliationConcatenated());
			article.getAnalysisObject().setPubmedCoAuthorAffiliation(article.getAffiliationConcatenated());
			article.getAnalysisObject().setArticleKeywords(article.getArticleKeywords().getCommaConcatForm());

			// Phase one clustering: clustering results and scores.
			//			article.getAnalysisObject().setNameMatchingScore(0);
			article.getAnalysisObject().setClusterOriginator(article.isClusterOriginator());
			article.getAnalysisObject().setTerminalDegreeScore(0);
			article.getAnalysisObject().setDefaultDepartmentJournalSimilarityScore(0);
			article.getAnalysisObject().setKeywordMatchingScore(0);

			// Phase two matching: scoring results.
			article.getAnalysisObject().setPhaseTwoSimilarityThreshold(0);
			article.getAnalysisObject().setClusterArticleAssignedTo(reCiterClusterer.getSelectedReCiterClusterId());

			if(assignedClusterId>=0)article.getAnalysisObject().setCountArticlesInAssignedCluster(reCiterClusterer.getFinalCluster().get(assignedClusterId).getArticleCluster().size());
			//			article.getAnalysisObject().setClusterSelectedInPhaseTwoMatching(true);
			article.getAnalysisObject().setAffiliationSimilarity(0);
			article.getAnalysisObject().setKeywordSimilarity(0);
			article.getAnalysisObject().setJournalSimilarityPhaseTwo(0);
		}
	}
	
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(LeverageDataOnBoardCertificationsJUnitTest.class);
		for (Failure failure : result.getFailures()) {
		      System.out.println(failure.toString());
		    }
	}
}
