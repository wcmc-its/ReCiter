package xmlparser.scopus.model;

import java.util.Map;

/**
 * A class model for representing a Scopus XML article.
 * 
 * <p>
 * A map consisting of afid to affiliation objects. A map is used instead of a list because it allows faster
 * retrieval based on afid. And sometimes, a Scopus article contains duplicate affiliation information inside
 * XML, so using a map will allow us to store only distinct affiliation based on afid.
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
