package reciter.database.dynamodb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reciter.engine.analysis.ReCiterFeature;

/**
 * This class allows you to store dynamodb items which exceeds dynamodb item limit of 400kb in s3.
 * Pre-requisite s3 properties should be set in application properties
 * @author Sarbajit Dutta(szd2013)
 *
 */
@Slf4j
@Component
public class DynamoDbS3Operations {
	
	@Autowired
	private AmazonS3 s3;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private static final String CONTENT_TYPE = "application/json";
	
	public void saveLargeItem(String bucketName, Object object, String keyName) {
		
		if(!s3.doesObjectExist(bucketName.toLowerCase(), keyName)) {
			
			//AmazonS3Config.createFolder(bucketName, AnalysisOutput.class.getName(), s3);
			String objectContentString = null;
			try {
				objectContentString = OBJECT_MAPPER.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
			byte[] objectContentBytes = objectContentString.getBytes(StandardCharsets.UTF_8);
			InputStream fileInputStream = new ByteArrayInputStream(objectContentBytes);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(CONTENT_TYPE);
			metadata.setContentLength(objectContentBytes.length);
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					bucketName.toLowerCase(), keyName, fileInputStream, metadata);
			s3.putObject(putObjectRequest);
		} else {
			log.info("Deleting Object from bucket " + bucketName + " with keyName " + keyName);
			s3.deleteObject(bucketName.toLowerCase(), keyName);
			//Delete the object and insert it again
			String objectContentString = null;
			try {
				objectContentString = OBJECT_MAPPER.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
			byte[] objectContentBytes = objectContentString.getBytes(StandardCharsets.UTF_8);
			InputStream fileInputStream = new ByteArrayInputStream(objectContentBytes);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(CONTENT_TYPE);
			metadata.setContentLength(objectContentBytes.length);
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					bucketName.toLowerCase(), keyName, fileInputStream, metadata);
			s3.putObject(putObjectRequest);
		}
	}
	
	public <T> Object retrieveLargeItem(String bucketName, String keyName, Class<T> objectClass) {
		S3Object s3Object = s3.getObject(new GetObjectRequest(bucketName.toLowerCase(), keyName));
		try {
			String objectContent = IOUtils.toString(s3Object.getObjectContent());
			if(objectClass == ReCiterFeature.class) {
				ReCiterFeature reCiterFeature = OBJECT_MAPPER.readValue(objectContent, ReCiterFeature.class);
				return reCiterFeature;
			}
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return null;
		
	}
}
