package reciter.database.dynamodb.repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import reciter.database.dynamodb.model.ESearchCount;

@EnableScan
public interface ESearchCountRepository extends CrudRepository<ESearchCount, String> {
}
