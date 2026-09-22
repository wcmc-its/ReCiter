package reciter.database.dyanmodb.files;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.service.ScienceMetrixService;
import tools.jackson.databind.ObjectMapper;

/**
 * This class deals with import of ScienceMetrix from files and import it into dynamodb
 * @author szd2013
 *
 */
@Component
public class ScienceMetrixFileImport {
	
	private static final Logger log = LoggerFactory.getLogger(ScienceMetrixFileImport.class);
	
	@Autowired
	private ScienceMetrixService scienceMetrixService;
	
	@Autowired
	private ObjectMapper mapper;
	
	public void importScienceMetrix() {
		List<ScienceMetrix> scienceMetrixBeans = null;
		try {
			scienceMetrixBeans = Arrays.asList(mapper.readValue(getClass().getResourceAsStream("/files/ScienceMetrix.json"), ScienceMetrix[].class));
		} catch (Exception e) {
			log.error("Failed to read science metrix beans from file", e);
		}
		if(scienceMetrixBeans != null 
				&&
				scienceMetrixBeans.size() == scienceMetrixService.getItemCount()) {
			log.info("The file ScienceMetrix.json and the ScienceMetrix table in DynamoDb is isomorphic and hence skipping import.");
		} else {
				if(scienceMetrixBeans != null
						&&
						scienceMetrixBeans.size() > 0) {
					log.info("The file ScienceMetrix.json and the ScienceMetrix table in DynamoDb is not isomorphic and hence starting import.");
					scienceMetrixService.save(scienceMetrixBeans);
			}
		}
	}
}
