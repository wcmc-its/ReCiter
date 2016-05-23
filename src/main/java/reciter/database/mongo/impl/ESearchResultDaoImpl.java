package reciter.database.mongo.impl;

import org.bson.Document;

import com.mongodb.MongoClient;

import reciter.database.mongo.ESearchResultDao;

public class ESearchResultDaoImpl implements ESearchResultDao {

	@Override
	public void insertESearchResult(Document eSearchResult) {
		MongoClient mongoClient = new MongoClient();
		mongoClient.getDatabase("reciter").getCollection("esearchresult").insertOne(eSearchResult);
		mongoClient.close();
	}
}
