package reciter.storage.s3;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.DynamoDbConfig;

@Slf4j
@Configuration
@ComponentScan(basePackages ="reciter.storage.s3")
public class AmazonS3Config {
	
	private String amazonAWSAccessKey = System.getenv("AMAZON_AWS_ACCESS_KEY");

    private String amazonAWSSecretKey = System.getenv("AMAZON_AWS_SECRET_KEY");
    
    @Value("${aws.s3.use}")
    private boolean isS3Use;
    
    @Value("${aws.s3.region}")
    private String awsS3Region;
    
    @Value("${aws.s3.dynamodb.bucketName}")
    private String s3BucketName;
    
    /**
     * @return AmazonS3 client object
     */
    @Bean
    @Scope("singleton")
    public AmazonS3 amazonS3() {
    	
    	if(isS3Use) {
	    	final AmazonS3 s3 = AmazonS3ClientBuilder
	    						.standard()
	    						.withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials() {
									
									@Override
									public String getAWSSecretKey() {
										return amazonAWSSecretKey;
									}
									
									@Override
									public String getAWSAccessKeyId() {
										return amazonAWSAccessKey;
									}
								}))
	    						.withRegion(awsS3Region)
	    						.build();
	    	createBucket(s3);
	    	return s3;
    	}
    						
    	return null;		
    }
    
    private void createBucket(AmazonS3 s3) {
    	
    	if(s3.doesBucketExistV2(s3BucketName.toLowerCase())) {
			log.info(s3BucketName.toLowerCase() + " Bucket Name already exists");
		} else {
			try {
				s3.createBucket(s3BucketName.toLowerCase());
			} catch(AmazonS3Exception e) {
				log.error(e.getErrorMessage());
			}
		}
    }
    
    /**
     * This function creates empty folder in a s3 bucket
     * @param bucketName
     * @param folderName
     * @param client
     */
    public static void createFolder(String bucketName, String folderName, AmazonS3 client) {
    	final String SUFFIX = "/";
    	
    	// create meta-data for your folder and set content-length to 0
    	ObjectMetadata metadata = new ObjectMetadata();
    	metadata.setContentLength(0);
    	// create empty content
    	InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
    	// create a PutObjectRequest passing the folder name suffixed by /
    	PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
    				folderName + SUFFIX, emptyContent, metadata);
    	// send request to S3 to create folder
    	client.putObject(putObjectRequest);
    }

}
