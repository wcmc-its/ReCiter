package xmlparser.scopus.model;

import java.util.Map;

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
public class ScopusArticle {
	
	private final Map<Integer, Affiliation> affiliations;
	private final int pubmedId;
	private final Map<Long, Author> authors;
	
	public Map<Integer, Affiliation> getAffiliationMap() {
		return affiliations;
	}

	public int getPubmedId() {
		return pubmedId;
	}

	public Map<Long, Author> getAuthors() {
		return authors;
	}

	public ScopusArticle(Map<Integer, Affiliation> affiliations, int pubmedId, Map<Long, Author> authors) {
		this.affiliations = affiliations;
		this.pubmedId = pubmedId;
		this.authors = authors;
	}
}
