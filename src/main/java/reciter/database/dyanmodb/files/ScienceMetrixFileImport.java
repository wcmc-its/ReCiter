package reciter.database.dyanmodb.files;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opencsv.bean.CsvToBeanBuilder;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.service.ScienceMetrixService;

@Component
@Slf4j
public class ScienceMetrixFileImport {
	
	@Autowired
	private ScienceMetrixService scienceMetrixService;
	
	public void importScienceMetrix() {
		List<ScienceMetrix> scienceMetrixBeans = null;
		try {
			scienceMetrixBeans = new CsvToBeanBuilder(new FileReader("src/main/resources/files/ScienceMetrix.csv"))
				       .withType(ScienceMetrix.class).withSeparator(',').build().parse();
		} catch (IllegalStateException e) {
			log.info(e.getMessage());
		} catch (FileNotFoundException e) {
			log.info(e.getMessage());
		}
		if(scienceMetrixBeans != null 
				&&
				scienceMetrixBeans.size() == scienceMetrixService.getItemCount()) {
			log.info("The file ScienceMetrix.csv and the ScienceMetrix table in DynamoDb is isomorphic and hence skipping import.");
		} else {
				if(scienceMetrixBeans != null
						&&
						scienceMetrixBeans.size() > 0) {
					log.info("The file ScienceMetrix.csv and the ScienceMetrix table in DynamoDb is not isomorphic and hence starting import.");
					scienceMetrixService.save(scienceMetrixBeans);
			}
		}
	}
}
