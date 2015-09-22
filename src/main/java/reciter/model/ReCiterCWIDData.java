package reciter.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import database.dao.GoldStandardPmidsDao;
import database.dao.IdentityDegreeDao;
import database.dao.IdentityDirectoryDao;
import database.dao.MatchingDepartmentsJournalsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;
import database.dao.impl.IdentityDegreeDaoImpl;
import database.dao.impl.IdentityDirectoryDaoImpl;
import database.dao.impl.MatchingDepartmentsJournalsDaoImpl;
import database.model.IdentityDegree;
import database.model.IdentityDirectory;
import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.article.ReCiterArticleKeywords.Keyword;
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

/**
 * 
 * @author htadimeti
 *
 */

public class ReCiterCWIDData {
	private ReCiterConfigProperty reCiterConfigProperty;
	private String cwid;
	private String lastName;
	private String middleName;
	private String firstName;
	private String affiliation;
	private String firstInitial;
	private String authorKeywords;
	private String coAuthors;
	private double similarityThreshold;
	private String department;
	private TargetAuthor targetAuthor;
	private List<ReCiterArticle> filteredArticleList;
	
	public ReCiterCWIDData(ReCiterConfigProperty reCiterConfigProperty){
		this.reCiterConfigProperty=reCiterConfigProperty;
		this.doPreOperation();
	}
	
	private void doPreOperation(){
		YearDiscrepacyReader.init();
		lastName = reCiterConfigProperty.getLastName();
		middleName = reCiterConfigProperty.getMiddleName();
		firstName = reCiterConfigProperty.getFirstName();
		affiliation = reCiterConfigProperty.getAuthorAffiliation();
		firstInitial = firstName.substring(0, 1);
		cwid = reCiterConfigProperty.getCwid();
		authorKeywords = reCiterConfigProperty.getAuthorKeywords();
		coAuthors = reCiterConfigProperty.getCoAuthors();
		similarityThreshold = reCiterConfigProperty.getSimilarityThreshold();
		department = reCiterConfigProperty.getAuthorDepartment();
		targetAuthor = new TargetAuthor(new AuthorName(firstName,middleName, lastName), new AuthorAffiliation(affiliation));
		ReCiterArticle targetAuthorArticle = new ReCiterArticle(-1);
		targetAuthorArticle.setArticleCoAuthors(new ReCiterArticleAuthors());
		targetAuthorArticle.getArticleCoAuthors().addAuthor(new ReCiterAuthor(new AuthorName(firstName, middleName,lastName), new AuthorAffiliation(affiliation + " "+ department)));
		targetAuthor.setCwid(cwid);
		targetAuthorArticle.setArticleKeywords(new ReCiterArticleKeywords());
		
		// Add primary and/or other department name(s) to list of topic keywords #46  
		MatchingDepartmentsJournalsDao translatedDepartmentList = new MatchingDepartmentsJournalsDaoImpl();
		List<String> departmentList = translatedDepartmentList.getDepartmentalAffiliationStringList();
		for (String departmentKeyword : departmentList ) {
			if (departmentKeyword!=null && (departmentKeyword.equals("and") || departmentKeyword.equals("or") || departmentKeyword.equals("of") || departmentKeyword.equals("for") || departmentKeyword.equals(" ") || departmentKeyword.equals("null"))) { continue;  }
			if(!targetAuthorArticle.getArticleKeywords().isKeywordExist(departmentKeyword))targetAuthorArticle.getArticleKeywords().addKeyword(departmentKeyword);
		}
		//
		
		for (String keyword : authorKeywords.split(",")) {
			if(targetAuthorArticle.getArticleKeywords().isKeywordExist(keyword))
				targetAuthorArticle.getArticleKeywords().addKeyword(keyword);
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
		
		IdentityDirectoryDao dao = new IdentityDirectoryDaoImpl();
		List<IdentityDirectory> identityDirectoryList = dao.getIdentityDirectoriesByCwid(cwid);
		targetAuthor.setAliasList(identityDirectoryList);
		
		
		//  For each of an author’s aliases, modify initial query based on lexical rules #100 
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		if (identityDirectoryList != null && identityDirectoryList.size() > 0) {
			for (IdentityDirectory dir : identityDirectoryList) {
				if (cwid.equals(dir.getCwid()))
					pubmedXmlFetcher.preparePubMedQueries(dir.getSurname(), dir.getGivenName(), dir.getMiddleName());
			}
		}
		
		
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstInitial, middleName, cwid);
		// Retrieve the scopus affiliation information for this cwid if the
		// affiliations have not been retrieve yet.
		ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
		
		// Use target author's known publications to populate first cluster
		// Git Issue #22
		GoldStandardPmidsDao gspDao = new GoldStandardPmidsDaoImpl();
		List<String> gspPmidList = gspDao.getPmidsByCwid(cwid);
		
		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
			if(gspPmidList.contains(pmid))gspPmidList.remove(pmid);
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
		}
		
		// Github issue: https://github.com/wcmc-its/ReCiter/issues/84.
		for (ReCiterArticle article : reCiterArticleList) {
			applyVertorOfKeywords(article);
		}
		
		filteredArticleList = new ArrayList<ReCiterArticle>();
		for (ReCiterArticle article : reCiterArticleList) {
			if (article.getArticleId() != -1) {
				filteredArticleList.add(article);
			}
		}
		
		reCiterArticleList.clear();
		reCiterArticleList = null;
		
		// Sort articles on completeness score.
		Collections.sort(filteredArticleList);
	}
	
	/**
	 * 
	 * @param word
	 * @return
	 */
	private String stemWord(String word){
		SnowballStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent();
	}
	
	
	// Github issue: https://github.com/wcmc-its/ReCiter/issues/84.
	private void applyVertorOfKeywords(ReCiterArticle article) {
		List<String> articleKeywordList = Arrays.asList(article.getArticleTitle().split(" "));
		for (String articleKeyword : articleKeywordList) {
			if(!article.getArticleKeywords().isKeywordExist(articleKeyword))article.getArticleKeywords().addKeyword(articleKeyword);
		}

		// Get Journal Tile and Split for Keywords
		List<String> journalKeywordList = Arrays.asList(article.getJournal().getJournalTitle().split(" "));
		for (String journalKeyword : journalKeywordList) {
			if(!article.getArticleKeywords().isKeywordExist(journalKeyword))article.getArticleKeywords().addKeyword(journalKeyword);
		}
		
		// Apply stemming on Title / Journal / Mesh Major and other keywords
		ReCiterArticleKeywords articleKeywords = article.getArticleKeywords();
		for(Keyword keyword: articleKeywords.getKeywords()){
			String origKeyword = keyword.getKeyword();
			String stemKeyword = stemWord(origKeyword);
			if(!origKeyword.equalsIgnoreCase(stemKeyword) && !articleKeywords.isKeywordExist(stemKeyword))articleKeywords.addKeyword(stemKeyword);
		}
	}

	/**
	 * @return the cwid
	 */
	public String getCwid() {
		return cwid;
	}

	/**
	 * @param cwid the cwid to set
	 */
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the middleName
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @param middleName the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the affiliation
	 */
	public String getAffiliation() {
		return affiliation;
	}

	/**
	 * @param affiliation the affiliation to set
	 */
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	/**
	 * @return the firstInitial
	 */
	public String getFirstInitial() {
		return firstInitial;
	}

	/**
	 * @param firstInitial the firstInitial to set
	 */
	public void setFirstInitial(String firstInitial) {
		this.firstInitial = firstInitial;
	}

	/**
	 * @return the authorKeywords
	 */
	public String getAuthorKeywords() {
		return authorKeywords;
	}

	/**
	 * @param authorKeywords the authorKeywords to set
	 */
	public void setAuthorKeywords(String authorKeywords) {
		this.authorKeywords = authorKeywords;
	}

	/**
	 * @return the coAuthors
	 */
	public String getCoAuthors() {
		return coAuthors;
	}

	/**
	 * @param coAuthors the coAuthors to set
	 */
	public void setCoAuthors(String coAuthors) {
		this.coAuthors = coAuthors;
	}

	/**
	 * @return the similarityThreshold
	 */
	public double getSimilarityThreshold() {
		return similarityThreshold;
	}

	/**
	 * @param similarityThreshold the similarityThreshold to set
	 */
	public void setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
	}

	/**
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}

	/**
	 * @param department the department to set
	 */
	public void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * @return the targetAuthor
	 */
	public TargetAuthor getTargetAuthor() {
		return targetAuthor;
	}

	/**
	 * @param targetAuthor the targetAuthor to set
	 */
	public void setTargetAuthor(TargetAuthor targetAuthor) {
		this.targetAuthor = targetAuthor;
	}

	/**
	 * @return the filteredArticleList
	 */
	public List<ReCiterArticle> getFilteredArticleList() {
		return filteredArticleList;
	}

	/**
	 * @param filteredArticleList the filteredArticleList to set
	 */
	public void setFilteredArticleList(List<ReCiterArticle> filteredArticleList) {
		this.filteredArticleList = filteredArticleList;
	}	
}
