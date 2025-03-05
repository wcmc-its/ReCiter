package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ScienceMetrix;

@Repository
public interface ScienceMetrixRepository extends CrudRepository<ScienceMetrix, Long> {
    ScienceMetrix findByEissn(String eissn);

    ScienceMetrix findByIssn(String issn);
}
