package reciter.service;

import reciter.database.dynamodb.model.ConsumerAPILogTracker;

public interface ConsumerAPILogTrackerService {
	
	void save(ConsumerAPILogTracker ConsumerAPILogTracker);
	
	ConsumerAPILogTracker findByUid(String uid);

	void delete(String uid);
}
