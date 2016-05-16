package reciter.database.mongo.impl;

import org.bson.Document;

import com.mongodb.MongoClient;

import reciter.database.mongo.PubMedServiceDao;

public class PubMedServiceDaoImpl implements PubMedServiceDao {

	@Override
	public void insertPubMedArticle(String json) {
		MongoClient mongoClient = new MongoClient();
		Document doc = Document.parse(json);
		mongoClient.getDatabase("reciter").getCollection("pubmedarticles").insertOne(doc);
		mongoClient.close();
	}
}
