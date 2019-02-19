package reciter.database.dyanmodb.files;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.service.ScienceMetrixDepartmentCategoryService;

@Component
@Slf4j
public class ScienceMetrixDepartmentCategoryFileImport {
	
	@Autowired
	private ScienceMetrixDepartmentCategoryService scienceMetrixDepartmentCategoryService;
	
	public void importScienceMetrixDepartmentCategory() {
		List<ScienceMetrixDepartmentCategory> sciMetrixDeptCatgeoryBeans = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			sciMetrixDeptCatgeoryBeans = Arrays.asList(mapper.readValue(getClass().getResourceAsStream("/files/ScienceMetrixDepartmentCategory.json"), ScienceMetrixDepartmentCategory[].class));
		} catch (IOException e) {
			log.error("IOException", e);
		}
		if(sciMetrixDeptCatgeoryBeans != null 
				&&
				sciMetrixDeptCatgeoryBeans.size() == scienceMetrixDepartmentCategoryService.getItemCount()) {
			log.info("The file ScienceMetrixDepartmentCategory.json and the ScienceMetrixDepartmentCategory table in DynamoDb is isomorphic and hence skipping import.");
		} else {
			if(sciMetrixDeptCatgeoryBeans != null 
					&&
					sciMetrixDeptCatgeoryBeans.size() > 0) {
				log.info("The file ScienceMetrixDepartmentCategory.json and the ScienceMetrixDepartmentCategory table in DynamoDb is not isomorphic and hence starting import.");
				scienceMetrixDepartmentCategoryService.save(sciMetrixDeptCatgeoryBeans);
			}
		}
	}
}
