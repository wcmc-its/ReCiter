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
import reciter.engine.analysis.ReCiterFeature;
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
	
    @Value("${aws.s3.use}")
    private boolean isS3Use;
	
    @Value("${aws.s3.dynamodb.bucketName}")
    private String s3BucketName;

	@Override
	public void save(AnalysisOutput analysis) {
		try{
			analysisOutputRepository.save(analysis);
		} catch(AmazonDynamoDBException addbe) {
			if(isS3Use) {
				log.info("Storing item in s3 since it item size exceeds more than 400kb");
				ddbs3.saveLargeItem(s3BucketName, analysis.getReCiterFeature(), AnalysisOutput.class.getSimpleName() + "/" + analysis.getUid());
				analysis.setReCiterFeature(null);
				analysis.setUsingS3(true);
				analysisOutputRepository.save(analysis);
			} else {
				log.info("Enable s3 use in application properties file to store larger objects. Set aws.s3.use to true and set aws.s3.dynamodb.bucketName");
			}
		}
	}

	@Override
	public AnalysisOutput findByUid(String uid) {
		AnalysisOutput analysisOutput = analysisOutputRepository.findById(uid).orElseGet(() -> null);
		if(analysisOutput != null 
				&&
				analysisOutput.isUsingS3()) {
			log.info("Retreving analysis from s3 for " + uid);
			ReCiterFeature reCiterFeature = (ReCiterFeature) ddbs3.retrieveLargeItem(s3BucketName, AnalysisOutput.class.getSimpleName() + "/" + uid.trim(), ReCiterFeature.class);
			analysisOutput.setReCiterFeature(reCiterFeature);
		} 
		return analysisOutput;
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
