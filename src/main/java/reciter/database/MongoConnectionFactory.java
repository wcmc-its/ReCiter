package reciter.database;

import com.mongodb.MongoClient;

public enum MongoConnectionFactory {
	CONNECTION;
	private MongoClient client = null;

	private MongoConnectionFactory() {
		client = new MongoClient();
	}

	public MongoClient getClient() {
		if (client == null)
			throw new RuntimeException();
		return client;
	}
}
