package reciter.database.dynamodb.repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import reciter.database.dynamodb.model.ScopusArticle;

@EnableScan
public interface ScopusArticleRepository extends CrudRepository<ScopusArticle, Long> {
}
