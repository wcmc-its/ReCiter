package reciter.database.dynamodb.repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import reciter.database.dynamodb.model.AnalysisOutput;

@EnableScan
public interface AnalysisOutputRepository extends CrudRepository<AnalysisOutput, String> {

}
