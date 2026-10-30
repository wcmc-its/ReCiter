package reciter.database.dyanmodb.files;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.service.ScienceMetrixDepartmentCategoryService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * This class deals with import of ScienceMetrixDepartmentCategory from files and import it into dynamodb
 * @author szd2013
 *
 */
@Component
public class ScienceMetrixDepartmentCategoryFileImport {
	
	private static final Logger log = LoggerFactory.getLogger(ScienceMetrixDepartmentCategoryFileImport.class);
	
	@Autowired
	private ScienceMetrixDepartmentCategoryService scienceMetrixDepartmentCategoryService;
	
	@Autowired
	private ObjectMapper mapper;

	
	public void importScienceMetrixDepartmentCategory() {
		List<ScienceMetrixDepartmentCategory> sciMetrixDeptCatgeoryBeans = null;
		try (InputStream is = getClass().getResourceAsStream("/files/ScienceMetrixDepartmentCategory.json")) {
			sciMetrixDeptCatgeoryBeans = Arrays.asList(mapper.readValue(is, ScienceMetrixDepartmentCategory[].class));
		} catch (JacksonException e) {
			log.error("Failed to read science metrix department categories from file", e);
		} catch (Exception e) {
			log.error("Unexpected error while reading science metrix department categories from file", e);
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
