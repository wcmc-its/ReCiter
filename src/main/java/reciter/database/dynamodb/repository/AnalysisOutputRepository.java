package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;

import reciter.database.dynamodb.model.AnalysisOutput;

public interface AnalysisOutputRepository extends CrudRepository<AnalysisOutput, String> {

}
