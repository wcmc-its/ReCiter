package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.Identity;

@Repository
public interface IdentityRepository extends CrudRepository<Identity, String> {

}