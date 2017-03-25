package reciter.database.mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import reciter.model.pubmed.PubMedArticle;

@Document(collection="pubmedarticle")
public class PubMedArticleMongo {

	@Id
	private long id;

	private PubMedArticle pubMedArticle;

	public long getObjectId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public PubMedArticle getPubMedArticle() {
		return pubMedArticle;
	}

	public void setPubMedArticle(PubMedArticle pubMedArticle) {
		this.pubMedArticle = pubMedArticle;
	}
}
