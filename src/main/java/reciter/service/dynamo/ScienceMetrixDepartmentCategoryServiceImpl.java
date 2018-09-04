package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.database.dynamodb.repository.ScienceMetrixDepartmentCategoryRepository;
import reciter.model.identity.Identity;
import reciter.service.ScienceMetrixDepartmentCategoryService;

@Service
public class ScienceMetrixDepartmentCategoryServiceImpl implements ScienceMetrixDepartmentCategoryService {
	
	@Autowired
	private ScienceMetrixDepartmentCategoryRepository scienceMetrixDepartmentCategoryRepository;

	@Override
	public List<ScienceMetrixDepartmentCategory> findByScienceMetrixJournalSubfieldId(Long subfieldId) {
		return scienceMetrixDepartmentCategoryRepository.findByScienceMetrixJournalSubfieldId(subfieldId);
	}
	
	@Override
    public void save(Collection<ScienceMetrixDepartmentCategory> scienceMetrixDepartmentCategories) {
        scienceMetrixDepartmentCategoryRepository.saveAll(scienceMetrixDepartmentCategories);
    }

	@Override
	public long getItemCount() {
		return scienceMetrixDepartmentCategoryRepository.count();
	}
	
	
}
