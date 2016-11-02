package reciter.database.mongo.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import reciter.database.mongo.model.Identity;
import reciter.database.mongo.model.PubMedAlias;
import reciter.database.mongo.repository.IdentityRepositoryCustom;

public class IdentityRepositoryImpl implements IdentityRepositoryCustom {

	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Override
	public void updatePubMedAlias(Identity identity) {
		String cwid = identity.getCwid();

		List<Object> pubmedAliasDbList = new BasicDBList();
		for (PubMedAlias pubMedAlias : identity.getPubMedAliases()) {
			DBObject pubmedAliasDbObject = new BasicDBObject();
			DBObject authorNameDbObject = new BasicDBObject();
			authorNameDbObject.put("firstName", pubMedAlias.getAuthorName().getFirstName());
			authorNameDbObject.put("firstInitial", pubMedAlias.getAuthorName().getFirstInitial());
			authorNameDbObject.put("middleName", pubMedAlias.getAuthorName().getMiddleName());
			authorNameDbObject.put("middleInitial", pubMedAlias.getAuthorName().getMiddleInitial());
			authorNameDbObject.put("lastName", pubMedAlias.getAuthorName().getLastName());
			
		    pubmedAliasDbObject.put("authorName", authorNameDbObject);
		    pubmedAliasDbObject.put("pmid", pubMedAlias.getPmid());
		    pubmedAliasDbList.add(pubmedAliasDbObject);
		}

		// add or modify field 'pubMedAlias'.
		BasicDBObject basicDbObject = new BasicDBObject("$set", new BasicDBObject("pubMedAlias", pubmedAliasDbList));
		mongoTemplate.getCollection("identity").update(new BasicDBObject("cwid", cwid), basicDbObject, true, false);
	}
}
