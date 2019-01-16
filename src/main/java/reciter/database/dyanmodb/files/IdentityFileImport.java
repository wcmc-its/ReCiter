package reciter.database.dyanmodb.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.Identity;
import reciter.service.IdentityService;

@Component
@Slf4j
public class IdentityFileImport {
	
	
	@Autowired
	private IdentityService identityService;
	
	/**
	 * This function imports identity data to identity table
	 */
	public void importIdentity() {
		ObjectMapper mapper = new ObjectMapper();
		List<Identity> identities = null;
		try {
			identities = Arrays.asList(mapper.readValue(getClass().getResourceAsStream("/files/Identity.json"), Identity[].class));
		} catch (IOException e) {
			log.error("IOException", e);
		}
		if(identities != null 
				&&
				identities.size() == identityService.getItemCount()) {
			log.info("The file Identity.json and the Identity table in DynamoDb is isomorphic and hence skipping import.");
		} else {
			if(identities != null 
					&&
					!identities.isEmpty()) {
				log.info("The file Identity.csv and the Identity table in DynamoDb is not isomorphic and hence starting import.");
				
				identityService.save(identities.stream().map(identity->identity.getIdentity()).collect(Collectors.toList()));
			}
		}
	}
	
	
	//Code to read HTML document. 
	private static String readJsonFile(InputStream is){
		String str="";
	    StringBuilder sb = new StringBuilder();
	    try{
	        BufferedReader br=new BufferedReader(new InputStreamReader(is));

	        while((str=br.readLine())!=null){
	            sb.append(str);
	        }
	        br.close();
	    }catch(IOException ie){
	        ie.printStackTrace();
	    }
	    return sb.toString();
	}

}
