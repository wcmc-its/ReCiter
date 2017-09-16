package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import reciter.database.dynamodb.model.PubMedArticle;

public interface PubMedArticleRepository extends CrudRepository<PubMedArticle, Long> {
}
