package reciter.junit.testcases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

public class ReCiterTestData {
	private String cwid;
	public ReCiterTestData(String cwid){
		this.cwid=cwid;
	}
	
	public List<ReCiterArticle> prepareTestData(){
		//String xmlFileName = PubmedXmlFetcher.getDefaultLocation()+this.cwid+"/"+this.cwid+"_0.xml";
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
		
		YearDiscrepacyReader.init();
		String lastName = reCiterConfigProperty.getLastName();
		String middleName = reCiterConfigProperty.getMiddleName();
		String firstName = reCiterConfigProperty.getFirstName();
		String affiliation = reCiterConfigProperty.getAuthorAffiliation();
		String firstInitial = firstName.substring(0, 1);

		String authorKeywords = reCiterConfigProperty.getAuthorKeywords();
		String coAuthors = reCiterConfigProperty.getCoAuthors();

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
				List<String> articleKeywordList = Arrays.asList(article.getArticleTitle().split(" "));
				stemmer = new PorterStemmer();
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
			}
			List<ReCiterArticle> filteredArticleList = new ArrayList<ReCiterArticle>();	
			for (ReCiterArticle article : reCiterArticleList) {
				if (article.getArticleId() != -1) {
					filteredArticleList.add(article);
				}
			}
			reCiterArticleList.clear();
			reCiterArticleList = null;				
			IdentityDegreeDao identityDegreeDao = new IdentityDegreeDaoImpl();
			IdentityDegree identityDegree = identityDegreeDao.getIdentityDegreeByCwid(cwid);
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
			
			return filteredArticleList;
	}
}
