package reciter.queue.sqs;

import java.util.UUID;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import lombok.extern.slf4j.Slf4j;
import reciter.ApplicationContextHolder;

@Slf4j
@Configuration
@ComponentScan(basePackages ="reciter.queue.sqs")
public class AmazonSQSExtendedClientConfig {
	
	private String amazonAWSAccessKey = System.getenv("AMAZON_AWS_ACCESS_KEY");

    private String amazonAWSSecretKey = System.getenv("AMAZON_AWS_SECRET_KEY");
    
    @Value("${aws.sqs.use}")
    private boolean isAwsSqs;
    
    @Value("${aws.sqs.extenedClient}")
    private boolean isAwsSqsExtenedClient;
    
    @Value("${aws.sqs.s3.bucketName}")
    private String s3BucketName;
    
    @Value("${aws.s3.region}")
    private String awsS3Region;
    
    @Autowired
    private AmazonS3 s3;
    
    @Bean
    @Scope("singleton")
    public AmazonSQS amazonSQSExtendedClient() {
    	
    	if(isAwsSqs && isAwsSqsExtenedClient) {
    		//AmazonS3 s3 = ApplicationContextHolder.getContext().getBean(AmazonS3.class);
    		
    		/*
             * Set the Amazon S3 bucket name, and then set a lifecycle rule on the
             * bucket to permanently delete objects 14 days after each object's
             * creation date.
             */
    		final BucketLifecycleConfiguration.Rule expirationRule =
                    new BucketLifecycleConfiguration.Rule();
            expirationRule.withExpirationInDays(7).withStatus("Enabled");
            
            final BucketLifecycleConfiguration lifecycleConfig =
                    new BucketLifecycleConfiguration().withRules(expirationRule);
    		
    		if(s3.doesBucketExistV2(s3BucketName.toLowerCase())) {
    			log.info(s3BucketName.toLowerCase() + " Bucket Name already exists");
    		} else {
    			try {
    				s3.createBucket(s3BucketName.toLowerCase());
    				s3.setBucketLifecycleConfiguration(s3BucketName.toLowerCase(), lifecycleConfig);
    			} catch(AmazonS3Exception e) {
    				log.error(e.getErrorMessage());
    			}
    		}
    		
    		final ExtendedClientConfiguration extendedClientConfig = new ExtendedClientConfiguration().withLargePayloadSupportEnabled(s3, s3BucketName)
    				.withMessageSizeThreshold(256);
    		
    		final AmazonSQS sqs = new AmazonSQSExtendedClient(AmazonSQSClientBuilder
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
					.build(), extendedClientConfig);
    				
    		return sqs;
    	} else if(isAwsSqs && !isAwsSqsExtenedClient) {
    		//regular client
	    		final AmazonSQS sqs = AmazonSQSClientBuilder
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
	  				
	  		return sqs;
    	}
    	
    	return null;
    }

}
