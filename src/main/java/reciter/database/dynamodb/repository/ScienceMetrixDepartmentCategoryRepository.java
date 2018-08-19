package reciter.database.dynamodb.repository;

import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;

@EnableScan
public interface ScienceMetrixDepartmentCategoryRepository extends CrudRepository<ScienceMetrixDepartmentCategory, Long> {
	
	List<ScienceMetrixDepartmentCategory> findByScienceMetrixJournalSubfieldId(Long subfieldId);

}
