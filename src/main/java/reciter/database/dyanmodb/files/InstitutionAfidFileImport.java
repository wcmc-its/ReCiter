package reciter.database.dyanmodb.files;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.dynamodb.model.InstitutionAfid;
import reciter.service.dynamo.IDynamoDbInstitutionAfidService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * This class deals with import of InstitutionAfid from files and import it into dynamodb
 * @author szd2013
 *
 */
@Component
public class InstitutionAfidFileImport {
	
	private static final Logger log = LoggerFactory.getLogger(InstitutionAfidFileImport.class);
	
	@Autowired
	private IDynamoDbInstitutionAfidService institutionAfIdService;
	
	
	@Autowired
	private ObjectMapper mapper;
	
	/**
	 * This function imports identity data to identity table
	 */
	public void importInstitutionAfids() {
		List<InstitutionAfid> institutionAfids = null;
		try (InputStream is = getClass().getResourceAsStream("/files/InstitutionAfid.json")) {
			institutionAfids = Arrays.asList(mapper.readValue(is, InstitutionAfid[].class));
		} catch (JacksonException e) {
			log.error("Failed to read institution afids from file", e);
		} catch (Exception e) {
			log.error("Unexpected error while reading institution afids from file", e);
		}
		if(institutionAfids != null 
				&&
				institutionAfids.size() == institutionAfIdService.getItemCount()) {
			log.info("The file InstitutionAfid.json and the InstitutionAfid table in DynamoDb is isomorphic and hence skipping import.");
		} else {
			if(institutionAfids != null 
					&&
					!institutionAfids.isEmpty()) {
				log.info("The file InstitutionAfid.json and the InstitutionAfid table in DynamoDb is not isomorphic and hence starting import.");
				
				institutionAfIdService.save(institutionAfids);
			}
		}
	}

}
