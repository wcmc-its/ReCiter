package reciter.database.dyanmodb.files;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.NameFrequency;
import reciter.service.NameFrequencyService;

@Slf4j
@Component
public class NameFrequencyFileImport {

	@Autowired
	private NameFrequencyService nameFrequencyService;

	public void importNameFrequency() {
		ObjectMapper mapper = new ObjectMapper();
		List<NameFrequency> nameFrequencies = null;
		try {
			nameFrequencies = Arrays.asList(mapper.readValue(getClass().getResourceAsStream("/files/NameFrequency.json"), NameFrequency[].class));
		} catch (IOException e) {
			log.error("IOException", e);
		}
		if(nameFrequencies != null
				&&
				nameFrequencies.size() == nameFrequencyService.getItemCount()) {
			log.info("The file NameFrequency.json and the NameFrequency table in DynamoDb is isomorphic and hence skipping import.");
		} else {
			if(nameFrequencies != null
					&&
					!nameFrequencies.isEmpty()) {
				log.info("The file NameFrequency.json and the NameFrequency table in DynamoDb is not isomorphic and hence starting import.");
				nameFrequencyService.save(nameFrequencies);
			}
		}
	}

}
