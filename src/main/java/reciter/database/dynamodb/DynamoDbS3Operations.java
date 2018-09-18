package reciter.database.dynamodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.service.dynamo.AnalysisServiceImpl;
import reciter.storage.s3.AmazonS3Config;

@Slf4j
@Component
public class DynamoDbS3Operations {
	
	@Autowired
	private AmazonS3 s3;
	
	public void saveLargeItem(String bucketName, Object object, String keyName) {
		
		if(!s3.doesObjectExist(bucketName.toLowerCase(), AnalysisOutput.class.getName() + "/" + keyName)) {
			
			//AmazonS3Config.createFolder(bucketName, AnalysisOutput.class.getName(), s3);
			
			PutObjectResult result = s3.putObject(bucketName.toLowerCase(), AnalysisOutput.class.getName() + "/" + keyName, object.toString());
			log.info("here");
		}
	}
}
