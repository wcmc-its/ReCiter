package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.s3.AmazonS3;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.DynamoDbS3Operations;
import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.repository.AnalysisOutputRepository;
import reciter.service.AnalysisService;

@Slf4j
@Service("AnalysisOutputService")
public class AnalysisServiceImpl implements AnalysisService{
	
	@Autowired
	private AnalysisOutputRepository analysisOutputRepository;
	
	@Autowired
	private AmazonS3 s3;
	
	@Autowired
	private DynamoDbS3Operations ddbs3;
	
    @Value("${aws.s3.dynamodb.bucketName}")
    private String s3BucketName;

	@Override
	public void save(AnalysisOutput analysis) {
		try{
			analysisOutputRepository.save(analysis);
		} catch(AmazonDynamoDBException addbe) {
			log.info("Storing item in s3 since it item size exceeds more than 400kb");
			ddbs3.saveLargeItem(s3BucketName, analysis, analysis.getUid());
			analysis.setReCiterFeature(null);
			analysis.setUsingS3(true);
			analysisOutputRepository.save(analysis);
			
		}
	}

	@Override
	public AnalysisOutput findByUid(String uid) {
		return analysisOutputRepository.findById(uid).orElseGet(() -> null);
	}

	@Override
	public void deleteAll() {
		 analysisOutputRepository.deleteAll();
	}

	@Override
	public void delete(String uid) {
		analysisOutputRepository.deleteById(uid);
	}
	

}
