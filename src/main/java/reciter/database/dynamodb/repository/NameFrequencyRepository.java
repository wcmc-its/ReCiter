package reciter.database.dynamodb.repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import reciter.database.dynamodb.model.NameFrequency;

@EnableScan
public interface NameFrequencyRepository extends CrudRepository<NameFrequency, String>{

}
