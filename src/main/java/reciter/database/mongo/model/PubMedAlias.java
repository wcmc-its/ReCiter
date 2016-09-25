package reciter.database.mongo.model;

import reciter.model.author.AuthorName;

public class PubMedAlias {

	private AuthorName authorName;
	private long pmid;
	
	public AuthorName getAuthorName() {
		return authorName;
	}
	public void setAuthorName(AuthorName authorName) {
		this.authorName = authorName;
	}
	public long getPmid() {
		return pmid;
	}
	public void setPmid(long pmid) {
		this.pmid = pmid;
	}
}
