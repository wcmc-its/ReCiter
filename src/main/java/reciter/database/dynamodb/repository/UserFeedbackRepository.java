package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.UserFeedback;

@Repository
public interface UserFeedbackRepository extends CrudRepository<UserFeedback, String>{

}
