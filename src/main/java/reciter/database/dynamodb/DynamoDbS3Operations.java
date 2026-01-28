package reciter.database.dynamodb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reciter.engine.analysis.ReCiterFeature;
import reciter.model.identity.Identity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * This class allows you to store dynamodb items which exceeds dynamodb item limit of 400kb in s3.
 * Pre-requisite s3 properties should be set in application properties
 * @author Sarbajit Dutta(szd2013)
 *
 */
@Slf4j
@Component
public class DynamoDbS3Operations {
	
	@Lazy
	@Autowired
	private S3Client  s3;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private static final String CONTENT_TYPE = "application/json";
	
	/**
	 * This function stores large object which has size more than 400kb.
	 * @param bucketName
	 * @param object
	 * @param keyName
	 */
	public void saveLargeItem(String bucketName, Object object, String keyName) {
		
		if (s3 != null && bucketName != null && !s3ObjectExists(bucketName.toLowerCase(), keyName)) {

			
			//AmazonS3Config.createFolder(bucketName, AnalysisOutput.class.getName(), s3);
			String objectContentString = null;
			try {
				objectContentString = OBJECT_MAPPER.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
			byte[] objectContentBytes = objectContentString.getBytes(StandardCharsets.UTF_8);
			InputStream fileInputStream = new ByteArrayInputStream(objectContentBytes);
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			        .bucket(bucketName.toLowerCase())
			        .key(keyName)
			        .contentType(CONTENT_TYPE)
			        .contentLength((long) objectContentBytes.length)
			        .build();

			

			try{
				s3.putObject(putObjectRequest, RequestBody.fromInputStream(fileInputStream, objectContentBytes.length));
			} catch(AmazonServiceException e) {
				// The call was transmitted successfully, but Amazon S3 couldn't process 
	            // it, so it returned an error response.
				log.error(e.getErrorMessage());
			}
		} else {
			log.info("Deleting Object from bucket " + bucketName + " with keyName " + keyName);
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			        .bucket(bucketName.toLowerCase())
			        .key(keyName)
			        .build();

			s3.deleteObject(deleteObjectRequest);

			//s3.deleteObject(bucketName.toLowerCase(), keyName);
			//Delete the object and insert it again
			String objectContentString = null;
			try {
				objectContentString = OBJECT_MAPPER.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
			byte[] objectContentBytes;
			if(objectContentString!=null)
			{
				objectContentBytes = objectContentString.getBytes(StandardCharsets.UTF_8);
				InputStream fileInputStream = new ByteArrayInputStream(objectContentBytes);
				PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				        .bucket(bucketName.toLowerCase())
				        .key(keyName)
				        .contentType(CONTENT_TYPE)
				        .contentLength((long) objectContentBytes.length)
				        .build();
				try{
					s3.putObject(putObjectRequest, RequestBody.fromInputStream(fileInputStream, objectContentBytes.length));
				}
				catch(AmazonServiceException e) {
					// The call was transmitted successfully, but Amazon S3 couldn't process 
		            // it, so it returned an error response.
					log.error(e.getErrorMessage());
				}
			}
		}
	}
	
	private boolean s3ObjectExists(String bucketName, String keyName) {
	    try {
	        s3.headObject(HeadObjectRequest.builder()
	                .bucket(bucketName)
	                .key(keyName)
	                .build());
	        return true; // Object exists
	    } catch (NoSuchKeyException e) {
	        return false; // Object does not exist
	    } catch (S3Exception e) {
	        log.error("Error checking object existence in S3: {}", e.awsErrorDetails().errorMessage());
	        return false;
	    }
	}

	
	/**
	 * This function retrieves large object from S3
	 * @param bucketName
	 * @param keyName
	 * @param objectClass
	 * @return
	 */
	public <T> Object retrieveLargeItem(String bucketName, String keyName, Class<T> objectClass) {
		try {
			//S3Object s3Object = s3.getObject(new GetObjectRequest(bucketName.toLowerCase(), keyName));
			
			GetObjectRequest getObjectRequest  = GetObjectRequest.builder()
			        .bucket(bucketName.toLowerCase())
			        .key(keyName)
			        .build();
			try (ResponseInputStream<GetObjectResponse> s3Object = s3.getObject(getObjectRequest)) {
			    String objectContent = IOUtils.toString(s3Object, StandardCharsets.UTF_8);

			    if (objectClass == ReCiterFeature.class) {
			        return OBJECT_MAPPER.readValue(objectContent, ReCiterFeature.class);
			    }
			    if (objectClass == Identity.class) {
			        return Arrays.asList(OBJECT_MAPPER.readValue(objectContent, Identity[].class));
			    }
			} catch (IOException | S3Exception e) {
			    log.error("Error retrieving object from S3: {}", e.getMessage());
			}
		

		
	}
		catch ( AmazonServiceException e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	/**
	 * This function delete large objects from S3
	 * @param bucketName
	 * @param keyName
	 */
	/*public void deleteLargeItem(String bucketName, String keyName) {
		if(s3 != null && bucketName != null && s3.doesObjectExist(bucketName.toLowerCase(), keyName)) {
			log.info("Deleting Object from bucket " + bucketName + " with keyName " + keyName);
			s3.deleteObject(bucketName.toLowerCase(), keyName);
		}
	}
	*/
	public void deleteLargeItem(String bucketName, String keyName) {
	    if (s3 != null && bucketName != null && s3ObjectExists(bucketName.toLowerCase(), keyName)) {
	        log.info("Deleting Object from bucket " + bucketName + " with keyName " + keyName);

	        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
	                .bucket(bucketName.toLowerCase())
	                .key(keyName)
	                .build();

	        s3.deleteObject(deleteObjectRequest);
	    }
	}


	/**
	 * This function gets the timestamp of the object that was stored. It assumes versioning is turned off for bucket.
	 * @param bucketName
	 * @param keyName
	 * @return date of the object that was stored
	 */
	/*public Date getObjectSaveTimestamp(String bucketName, String keyName) {
		try {
			S3Object s3Object = s3.getObject(new GetObjectRequest(bucketName.toLowerCase(), keyName));
			Date lastModifedDate = s3Object.getObjectMetadata().getLastModified();
			return lastModifedDate;
		} catch (AmazonServiceException e) {
			log.error(e.getMessage());
		}
		return null;
	}*/
	public Date getObjectSaveTimestamp(String bucketName, String keyName) {
	    try {
	        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
	                .bucket(bucketName.toLowerCase())
	                .key(keyName)
	                .build();

	        HeadObjectResponse headObjectResponse = s3.headObject(headObjectRequest);

	        return Date.from(headObjectResponse.lastModified());
	    } catch (S3Exception e) {
	        log.error("Error retrieving object metadata from S3: {}", e.awsErrorDetails().errorMessage());
	    }
	    return null;
	}

}
