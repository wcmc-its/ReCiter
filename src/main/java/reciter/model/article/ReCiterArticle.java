package reciter.model.article;

import java.util.List;
import java.util.Map;

import reciter.erroranalysis.AnalysisObject;
import reciter.lucene.DocumentVector;
import reciter.lucene.DocumentVectorType;
import reciter.lucene.docsimilarity.DocumentSimilarity;
import reciter.lucene.docsimilarity.MaxCosineSimilarity;
import reciter.model.article.ReCiterArticleKeywords.Keyword;
import reciter.model.author.ReCiterAuthor;
import reciter.model.completeness.ArticleCompleteness;
import reciter.model.completeness.ReCiterCompleteness;
import database.model.IdentityDirectory;

public class ReCiterArticle implements Comparable<ReCiterArticle> {

	private final int articleID;
	private ReCiterArticleTitle articleTitle;
	private ReCiterArticleCoAuthors articleCoAuthors;
	private ReCiterJournal journal;
	private ReCiterArticleKeywords articleKeywords;
	private double completenessScore;
	private ArticleCompleteness articleCompleteness;
	private Map<DocumentVectorType, DocumentVector> documentVectors;
	private DocumentSimilarity documentSimmilarity;
	private String affiliationConcatenated;
	private int dateCreated;
	private String info;
	private boolean isClusterOriginator;
	private String scopusAffiliation;
	private AnalysisObject analysisObject;
	private List<IdentityDirectory> aliasesList;
	
	
	
	public List<IdentityDirectory> getAliasesList() {
		return aliasesList;
	}

	public void setAliasesList(
			List<IdentityDirectory> identityDirectoryList) {
		this.aliasesList = identityDirectoryList;
	}

	/**
	 * Default Completeness Score Calculation: ReCiterCompleteness
	 * @param articleID
	 */
	public ReCiterArticle(int articleID) {
		this.articleID = articleID;
		this.setArticleCompleteness(new ReCiterCompleteness());
		setDocumentSimmilarity(new MaxCosineSimilarity()); // default similarity is maximum similarity.
//		setDocumentSimmilarity(new HeuristicSimilarity());
	}
	
	public double getCompletenessScore() {
		return completenessScore;
	}

	public void setCompletenessScore(double completenessScore) {
		this.completenessScore = completenessScore;
	}

	public int getArticleID() {
		return articleID;
	}

	public ReCiterArticleTitle getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(ReCiterArticleTitle articleTitle) {
		this.articleTitle = articleTitle;
	}

	public ReCiterArticleCoAuthors getArticleCoAuthors() {
		return articleCoAuthors;
	}

	public void setArticleCoAuthors(ReCiterArticleCoAuthors articleCoAuthors) {
		this.articleCoAuthors = articleCoAuthors;
	}

	public ReCiterJournal getJournal() {
		return journal;
	}

	public void setJournal(ReCiterJournal journal) {
		this.journal = journal;
	}

	public ReCiterArticleKeywords getArticleKeywords() {
		return articleKeywords;
	}

	public void setArticleKeywords(ReCiterArticleKeywords articleKeywords) {
		this.articleKeywords = articleKeywords;
	}

	public ArticleCompleteness getArticleCompleteness() {
		return articleCompleteness;
	}

	public void setArticleCompleteness(ArticleCompleteness articleCompleteness) {
		this.articleCompleteness = articleCompleteness;
	}

	public Map<DocumentVectorType, DocumentVector> getDocumentVectors() {
		return documentVectors;
	}

	public void setDocumentVectors(Map<DocumentVectorType, DocumentVector> documentVectors) {
		this.documentVectors = documentVectors;
	}

	public DocumentSimilarity getDocumentSimmilarity() {
		return documentSimmilarity;
	}

	public void setDocumentSimmilarity(DocumentSimilarity documentSimmilarity) {
		this.documentSimmilarity = documentSimmilarity;
	}

	@Override
	public int compareTo(ReCiterArticle otherArticle) {
		double x = this.getCompletenessScore();
		double y = otherArticle.getCompletenessScore();
		if (x > y) {
			return -1;
		} else if (x < y) {
			return 1;
		} else {
			return 0;
		}
	}

	public String toCSV() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"");
		sb.append(articleID);
		sb.append("\",");
		
		sb.append("\"");
		sb.append(articleTitle.getTitle());
		sb.append("\",");
		
		sb.append("\"");
		sb.append(journal.getJournalTitle());
		sb.append("\",");
		
		sb.append("\"");
		for (ReCiterAuthor author : articleCoAuthors.getCoAuthors()) {
			sb.append(author.getAuthorName().getCSVFormat());
			sb.append(": ");
			sb.append(author.getAffiliation());
			sb.append(", ");
		}
		sb.append("\"");
		
		sb.append("\"");
		for (Keyword keyword : articleKeywords.getKeywords()) {
			sb.append(keyword.getKeyword());
			sb.append(", ");
		}
		sb.append("\"");
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "ReCiterArticle [articleID=" + articleID + ", articleTitle="
				+ articleTitle + ", articleCoAuthors=" + articleCoAuthors
				+ ", journal=" + journal + ", articleKeywords="
				+ articleKeywords + ", completenessScore=" + completenessScore
				+ "]";
	}

	public String getAffiliationConcatenated() {
		return affiliationConcatenated;
	}

	public void setAffiliationConcatenated(String affiliationConcatenated) {
		this.affiliationConcatenated = affiliationConcatenated;
	}

	public int getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(int dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public boolean isClusterOriginator() {
		return isClusterOriginator;
	}

	public void setClusterOriginator(boolean isClusterOriginator) {
		this.isClusterOriginator = isClusterOriginator;
	}

	public String getScopusAffiliation() {
		return scopusAffiliation;
	}

	public void setScopusAffiliation(String scopusAffiliation) {
		this.scopusAffiliation = scopusAffiliation;
	}

	public AnalysisObject getAnalysisObject() {
		return analysisObject;
	}

	public void setAnalysisObject(AnalysisObject analysisObject) {
		this.analysisObject = analysisObject;
	}

}
