package reciter.database.dyanmodb.files;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.dynamodb.model.MeshTerm;
import reciter.service.IDynamoDbMeshTermService;
import tools.jackson.databind.ObjectMapper;

/**
 * This class deals with import of MeshTerm from files and import it into dynamodb
 * @author szd2013
 *
 */
@Component
public class MeshTermFileImport {
	private static final Logger log = LoggerFactory.getLogger(MeshTermFileImport.class);
	
	@Autowired
	private IDynamoDbMeshTermService meshTermService;
	
	@Autowired
	private ObjectMapper mapper;
	
	/**
	 * This function imports identity data to identity table
	 */
	public void importMeshTerms() {
		List<MeshTerm> meshTerms = null;
		try {
			meshTerms = Arrays.asList(mapper.readValue(getClass().getResourceAsStream("/files/MeshTerm.json"), MeshTerm[].class));
		} catch (Exception e) {
			log.error("Failed to read mesh terms from file", e);
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
