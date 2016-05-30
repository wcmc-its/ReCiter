package reciter.database.mongo.impl;

import org.bson.Document;

import com.mongodb.MongoClient;

import reciter.database.mongo.ESearchResultDataAccess;
import reciter.database.mongo.MongoConnectionFactory;

public class ESearchResultDataAccessImpl implements ESearchResultDataAccess {

	@Override
	public void insertESearchResult(Document eSearchResult) {
		MongoClient mongoClient = MongoConnectionFactory.CONNECTION.getClient();
		mongoClient.getDatabase("reciter").getCollection("esearchresult").insertOne(eSearchResult);
		mongoClient.close();
	}
}
