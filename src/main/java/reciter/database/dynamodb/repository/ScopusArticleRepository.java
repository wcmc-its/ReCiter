package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import reciter.database.dynamodb.model.ScopusArticle;

public interface ScopusArticleRepository extends CrudRepository<ScopusArticle, Long> {
}
