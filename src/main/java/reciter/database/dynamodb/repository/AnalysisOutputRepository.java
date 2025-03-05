package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.AnalysisOutput;

@Repository
public interface AnalysisOutputRepository extends CrudRepository<AnalysisOutput, String> {

}
