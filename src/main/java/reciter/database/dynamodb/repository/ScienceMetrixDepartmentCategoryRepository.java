package reciter.database.dynamodb.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;

@Repository
public interface ScienceMetrixDepartmentCategoryRepository extends CrudRepository<ScienceMetrixDepartmentCategory, Long> {
	
	List<ScienceMetrixDepartmentCategory> findByScienceMetrixJournalSubfieldId(Long subfieldId);

}
