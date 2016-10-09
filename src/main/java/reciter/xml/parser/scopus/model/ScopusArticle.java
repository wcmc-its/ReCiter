package reciter.xml.parser.scopus.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A class model for representing a Scopus XML article.
 * 
 * <p>
 * A ScopusArticle consists of a map of afids to affiliations.
 * 
 * Note:
 * Sometimes scopus articles contains duplicate affiliation information.
 * However, this problem is resolved by using a map to store only distinct affiliation based on afid.
 * </p>
 * 
 * @author jil3004
 *
 */
@Document(collection = "scopusarticle")
public class ScopusArticle {
	
	private long pubmedId;
	private List<Affiliation> affiliations;
	private List<Author> authors;
	
	public ScopusArticle() {}
	
	public ScopusArticle(long pubmedId, List<Affiliation> affiliations, List<Author> authors) {
		this.pubmedId = pubmedId;
		this.affiliations = affiliations;
		this.authors = authors;
	}

	public long getPubmedId() {
		return pubmedId;
	}

	public void setPubmedId(long pubmedId) {
		this.pubmedId = pubmedId;
	}

	public List<Affiliation> getAffiliations() {
		return affiliations;
	}

	public void setAffiliations(List<Affiliation> affiliations) {
		this.affiliations = affiliations;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}
	
//	public Map<Integer, Affiliation> getAffiliationMap() {
//		return affiliations;
//	}
//
//	public int getPubmedId() {
//		return pubmedId;
//	}
//
//	public Map<Long, Author> getAuthors() {
//		return authors;
//	}

//	public ScopusArticle(Map<Integer, Affiliation> affiliations, int pubmedId, Map<Long, Author> authors) {
//		this.affiliations = affiliations;
//		this.pubmedId = pubmedId;
//		this.authors = authors;
//	}
}
