package reciter.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class S3UserLogHandler {

    private AmazonS3 s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.consumer.api.logs.bucketName}")
    private String apiLogsBucketName;
    
    @Value("${aws.congito.userpool.region}")
    private String apiLogsBucketRegion;
    
    public S3UserLogHandler() {
 		this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        if (apiLogsBucketRegion != null && !apiLogsBucketRegion.isEmpty()) {
            s3Client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .withRegion(apiLogsBucketRegion)
                    .build();
        } else {
            throw new IllegalStateException("AWS region is not configured correctly");
        }
    }

    // Path format: transactions/YYYY-MM-DD/logs.json
    private String getLogFilePath(String date) {
    	return String.format("%s.json", date);
    }

    // Method to create or append user log entry to the log file
    public void writeUserLog(UserLog userLog, String date) throws IOException {
        String logFilePath = getLogFilePath(date);
        List<UserLog> logs = new ArrayList<>();
        try
        {
        	boolean isObjectexists = s3Client.doesObjectExist(apiLogsBucketName, logFilePath);
        }
        catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                System.out.println("Object does not exist.");
            } else {
                System.out.println("Error: " + e.getMessage());
            }
        }
        // Check if file already exists
        if (s3Client.doesObjectExist(apiLogsBucketName, logFilePath)) {
            try {
                // If the file exists, download it and append the new log
                S3Object object = s3Client.getObject(apiLogsBucketName, logFilePath);
                try (InputStream inputStream = object.getObjectContent()) {
                    // Read existing logs
                    UserLog[] existingLogs = objectMapper.readValue(inputStream, UserLog[].class);
                    for (UserLog log : existingLogs) {
                        logs.add(log);
                    }
                }
            } catch (AmazonServiceException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
        // Add the new log entry
        logs.add(userLog);
        // Convert the list of logs to JSON
        String jsonLogs = objectMapper.writeValueAsString(logs);
        // Upload the updated logs back to S3
        InputStream updatedInputStream = new ByteArrayInputStream(jsonLogs.getBytes());
        PutObjectRequest request = new PutObjectRequest(apiLogsBucketName, logFilePath, updatedInputStream, new ObjectMetadata());
        s3Client.putObject(request);

    }
}


