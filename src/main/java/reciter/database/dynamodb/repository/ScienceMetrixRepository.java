package reciter.database.dynamodb.repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import reciter.database.dynamodb.model.ScienceMetrix;

@EnableScan
public interface ScienceMetrixRepository extends CrudRepository<ScienceMetrix, Long> {
    ScienceMetrix findByEissn(String eissn);

    ScienceMetrix findByIssn(String issn);
}
