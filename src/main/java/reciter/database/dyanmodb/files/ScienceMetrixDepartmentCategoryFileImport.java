package reciter.database.dyanmodb.files;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opencsv.bean.CsvToBeanBuilder;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.service.ScienceMetrixDepartmentCategoryService;

@Component
@Slf4j
public class ScienceMetrixDepartmentCategoryFileImport {
	
	@Autowired
	private ScienceMetrixDepartmentCategoryService scienceMetrixDepartmentCategoryService;
	
	public void importScienceMetrixDepartmentCategory() {
		List<ScienceMetrixDepartmentCategory> sciMetrixDeptCatgeoryBeans = null;
		try {
			 sciMetrixDeptCatgeoryBeans = new CsvToBeanBuilder(new FileReader("src/main/resources/files/SciMetrixDepartmentCategory.csv"))
				       .withType(ScienceMetrixDepartmentCategory.class).withSeparator(',').build().parse();
		} catch (IllegalStateException e) {
			log.info(e.getMessage());
		} catch (FileNotFoundException e) {
			log.info(e.getMessage());
		}
		if(sciMetrixDeptCatgeoryBeans != null 
				&&
				sciMetrixDeptCatgeoryBeans.size() == scienceMetrixDepartmentCategoryService.getItemCount()) {
			log.info("The file SciMetrixDepartmentCategory.csv and the ScienceMetrixDepartmentCategory table in DynamoDb is isomorphic and hence skipping import.");
		} else {
			if(sciMetrixDeptCatgeoryBeans != null 
					&&
					sciMetrixDeptCatgeoryBeans.size() > 0) {
				log.info("The file SciMetrixDepartmentCategory.csv and the ScienceMetrixDepartmentCategory table in DynamoDb is not isomorphic and hence starting import.");
				scienceMetrixDepartmentCategoryService.save(sciMetrixDeptCatgeoryBeans);
			}
		}
	}
}
