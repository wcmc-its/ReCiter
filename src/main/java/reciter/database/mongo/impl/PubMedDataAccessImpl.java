package reciter.database.mongo.impl;

import org.bson.Document;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoClient;
import com.mongodb.client.model.UpdateOptions;

import reciter.database.mongo.MongoConnectionFactory;
import reciter.database.mongo.PubMedDataAccess;

@Repository
public class PubMedDataAccessImpl implements PubMedDataAccess {

	@Override
	public void insertPubMedArticle(String json) {
		MongoClient mongoClient = MongoConnectionFactory.CONNECTION.getClient();
		Document doc = Document.parse(json);
		mongoClient.getDatabase("reciter").getCollection("pubmedarticles").insertOne(doc);
	}
	
	@Override
	public void upsertPubMedArticle(String json, String pmid) {
		MongoClient mongoClient = MongoConnectionFactory.CONNECTION.getClient();
		Document doc = Document.parse(json);
		UpdateOptions updateOptions = new UpdateOptions();
		updateOptions.upsert(true);
		mongoClient.getDatabase("reciter").getCollection("pubmedarticles").updateOne(
				new Document("pmid", pmid),
				new Document("$set", doc),
				updateOptions
		);
	}
}
