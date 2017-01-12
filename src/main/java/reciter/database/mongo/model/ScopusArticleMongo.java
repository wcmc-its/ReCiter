package reciter.database.mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import reciter.model.scopus.ScopusArticle;

@Document(collection="scopusarticle")
public class ScopusArticleMongo {

	@Id
	private long id;

	private ScopusArticle scopusArticle;

	public long getObjectId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ScopusArticle getScopusArticle() {
		return scopusArticle;
	}

	public void setScopusArticle(ScopusArticle scopusArticle) {
		this.scopusArticle = scopusArticle;
	}

}
