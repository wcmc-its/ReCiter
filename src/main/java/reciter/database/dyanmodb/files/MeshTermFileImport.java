package reciter.database.dyanmodb.files;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.MeshTerm;
import reciter.service.IDynamoDbMeshTermService;

/**
 * This class deals with import of MeshTerm from files and import it into dynamodb
 * @author szd2013
 *
 */
@Component
@Slf4j
public class MeshTermFileImport {
	
	@Autowired
	private IDynamoDbMeshTermService meshTermService;
	
	/**
	 * This function imports identity data to identity table
	 */
	public void importMeshTerms() {
		ObjectMapper mapper = new ObjectMapper();
		List<MeshTerm> meshTerms = null;
		try {
			meshTerms = Arrays.asList(mapper.readValue(getClass().getResourceAsStream("/files/MeshTerm.json"), MeshTerm[].class));
		} catch (IOException e) {
			log.error("IOException", e);
		}
		if(meshTerms != null 
				&&
				meshTerms.size() == meshTermService.getItemCount()) {
			log.info("The file MeshTerm.json and the MeshTerm table in DynamoDb is isomorphic and hence skipping import.");
		} else {
			if(meshTerms != null 
					&&
					!meshTerms.isEmpty()) {
				log.info("The file MeshTerm.json and the MeshTerm table in DynamoDb is not isomorphic and hence starting import.");
				meshTermService.save(meshTerms);
			}
		}
	}
}
