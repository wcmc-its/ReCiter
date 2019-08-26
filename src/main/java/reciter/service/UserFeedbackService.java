package reciter.service;

import reciter.database.dynamodb.model.UserFeedback;

public interface UserFeedbackService {
	
	void save(UserFeedback userFeedback);
	
	UserFeedback findByUid(String uid);

	void delete(String uid);
}
