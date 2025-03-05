package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ESearchResult;

@Repository
public interface ESearchResultRepository extends CrudRepository<ESearchResult, String> {
}
