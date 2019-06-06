package reciter.database.dyanmodb.files;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.Gender;
import reciter.service.GenderService;

@Slf4j
@Component
public class GenderFileImport {
	
	@Autowired
	private GenderService genderService;
	
	/**
	 * This function imports gender data to Gender table
	 */
	public void importGender() {
		ObjectMapper mapper = new ObjectMapper();
		List<Gender> genders = null;
		try {
			genders = Arrays.asList(mapper.readValue(getClass().getResourceAsStream("/files/Gender.json"), Gender[].class));
		} catch (IOException e) {
			log.error("IOException", e);
		}
		if(genders != null 
				&&
				genders.size() == genderService.getItemCount()) {
			log.info("The file Gender.json and the Gender table in DynamoDb is isomorphic and hence skipping import.");
		} else {
			if(genders != null 
					&&
					!genders.isEmpty()) {
				log.info("The file Gender.json and the Gender table in DynamoDb is not isomorphic and hence starting import.");
				genderService.save(genders);
			}
		}
	}
	
}
