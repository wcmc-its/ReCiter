package reciter.model.article;

import java.util.ArrayList;
import java.util.List;

import reciter.model.completeness.ArticleCompleteness;
import reciter.model.completeness.ReCiterCompleteness;
import xmlparser.scopus.model.ScopusArticle;

public class ReCiterArticle implements Comparable<ReCiterArticle> {

	/**
	 * Article id: (usually PMID).
	 */
	private final int articleId;
	
	/**
	 * Article title.
	 */
	private String articleTitle;
	
	/**
	 * Co-authors of this article.
	 */
	private ReCiterArticleAuthors articleCoAuthors;
	
	/**
	 * Journal that this article belongs to.
	 */
	private ReCiterJournal journal;
	
	/**
	 * Keywords associated with this article.
	 */
	private ReCiterArticleKeywords articleKeywords;
	
	/**
	 * How "complete" this article is. (Please refer to the ReCiter paper).
	 */
	private double completenessScore;
	
	/**
	 * Complete score strategy.
	 */
	private ArticleCompleteness articleCompleteness;
	
	/**
	 * Scopus Article.
	 */
	private ScopusArticle scopusArticle;
	
	/**
	 * MeSH Terms.
	 */
	private List<String> meshList;
	
	/**
	 * Text containing how it's clustered.
	 */
	private String clusterInfo;
	
	/**
	 * Default Completeness Score Calculation: ReCiterCompleteness
	 * @param articleID
	 */
	public ReCiterArticle(int articleId) {
		this.articleId = articleId;
		this.setArticleCompleteness(new ReCiterCompleteness());
		setMeshList(new ArrayList<String>());
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
	
	public int getArticleId() {
		return articleId;
	}
	
	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	public ReCiterArticleAuthors getArticleCoAuthors() {
		return articleCoAuthors;
	}

	public void setArticleCoAuthors(ReCiterArticleAuthors articleCoAuthors) {
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

	public double getCompletenessScore() {
		return completenessScore;
	}

	public void setCompletenessScore(double completenessScore) {
		this.completenessScore = completenessScore;
	}

	public ArticleCompleteness getArticleCompleteness() {
		return articleCompleteness;
	}

	public void setArticleCompleteness(ArticleCompleteness articleCompleteness) {
		this.articleCompleteness = articleCompleteness;
	}

	public ScopusArticle getScopusArticle() {
		return scopusArticle;
	}

	public void setScopusArticle(ScopusArticle scopusArticle) {
		this.scopusArticle = scopusArticle;
	}

	public List<String> getMeshList() {
		return meshList;
	}

	public void setMeshList(List<String> meshList) {
		this.meshList = meshList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + articleId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReCiterArticle other = (ReCiterArticle) obj;
		if (articleId != other.articleId)
			return false;
		return true;
	}

	public String getClusterInfo() {
		return clusterInfo;
	}

	public void setClusterInfo(String clusterInfo) {
		this.clusterInfo = clusterInfo;
	}
	
	public void appendClusterInfo(String clusterInfo) {
		this.clusterInfo += " | " + clusterInfo;
	}
}
