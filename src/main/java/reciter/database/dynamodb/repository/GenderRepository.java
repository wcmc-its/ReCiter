package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.Gender;

@Repository
public interface GenderRepository extends CrudRepository<Gender, String>{

}
