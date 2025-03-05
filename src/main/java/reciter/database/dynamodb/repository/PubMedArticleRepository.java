package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.PubMedArticle;

@Repository
public interface PubMedArticleRepository extends CrudRepository<PubMedArticle, Long> {
}
