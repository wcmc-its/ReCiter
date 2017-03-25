package reciter.database.mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import reciter.model.identity.Identity;

@Document(collection="identity")
public class IdentityMongo {

	@Id
	private String id;

	private Identity identity;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
}
