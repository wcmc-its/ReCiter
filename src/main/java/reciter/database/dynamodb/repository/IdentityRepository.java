package reciter.database.dynamodb.repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import reciter.database.dynamodb.model.Identity;

@EnableScan
public interface IdentityRepository extends CrudRepository<Identity, String> {

}