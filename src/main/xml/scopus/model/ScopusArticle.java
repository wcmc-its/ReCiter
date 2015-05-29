package main.xml.scopus.model;

import java.util.List;
import java.util.Map;

/**
 * A class model for representing a Scopus XML article.
 * @author jil3004
 *
 */
public class ScopusArticle {

	// A map consisting of afid to affiliation objects. A map is used instead of a list because it allows faster
	// retrieval based on afid. And sometimes, a Scopus article contains duplicate affiliation information inside
	// XML, so using a map will allow us to store only distinct affiliation based on afid.
	private final Map<Integer, Affiliation> affiliationMap;
	private final int pubmedId; // <pubmed-id> XML tag.
	private final List<Author> authorList;
	
	public Map<Integer, Affiliation> getAffiliationMap() {
		return affiliationMap;
	}

	public int getPubmedId() {
		return pubmedId;
	}

	public List<Author> getAuthorList() {
		return authorList;
	}

	public ScopusArticle(Map<Integer, Affiliation> affiliationMap,
			int pubmedId, List<Author> authorList) {
		this.affiliationMap = affiliationMap;
		this.pubmedId = pubmedId;
		this.authorList = authorList;
	}
	
}
