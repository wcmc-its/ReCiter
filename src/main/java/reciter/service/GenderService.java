package reciter.service;

import java.util.Collection;
import java.util.List;

import reciter.database.dynamodb.model.Gender;

public interface GenderService {
	
	void save(Gender gender);
	
	void save(Collection<Gender> genders);
	
	Gender findByUid(String uid);

	List<Gender> findAll();
	
	void deleteAll();

	void delete(String uid);
	
	long getItemCount();

}
