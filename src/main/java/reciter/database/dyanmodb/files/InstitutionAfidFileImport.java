package reciter.database.dyanmodb.files;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.InstitutionAfid;
import reciter.service.dynamo.IDynamoDbInstitutionAfidService;

@Component
@Slf4j
public class InstitutionAfidFileImport {
	
	@Autowired
	private IDynamoDbInstitutionAfidService institutionAfIdService;
	
	/**
	 * This function imports identity data to identity table
	 */
	public void importInstitutionAfids() {
		ObjectMapper mapper = new ObjectMapper();
		List<InstitutionAfid> institutionAfids = null;
		try {
			institutionAfids = Arrays.asList(mapper.readValue(getClass().getResourceAsStream("/files/Identity.json"), InstitutionAfid[].class));
		} catch (IOException e) {
			log.error("IOException", e);
		}
		if(institutionAfids != null 
				&&
				institutionAfids.size() == institutionAfIdService.getItemCount()) {
			log.info("The file Identity.json and the Identity table in DynamoDb is isomorphic and hence skipping import.");
		} else {
			if(institutionAfids != null 
					&&
					!institutionAfids.isEmpty()) {
				log.info("The file Identity.csv and the Identity table in DynamoDb is not isomorphic and hence starting import.");
				
				institutionAfIdService.save(institutionAfids);
			}
		}
	}

}
