package reciter.service;

import java.util.List;

import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;

public interface ScienceMetrixDepartmentCategoryService {
	List<ScienceMetrixDepartmentCategory> findByScienceMetrixJournalSubfieldId(Long subfieldId);
}
