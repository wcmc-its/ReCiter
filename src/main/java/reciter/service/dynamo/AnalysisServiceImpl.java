package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.DynamoDbS3Operations;
import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.repository.AnalysisOutputRepository;
import reciter.engine.analysis.ReCiterFeature;
import reciter.service.AnalysisService;
import reciter.storage.s3.AmazonS3Config;

@Slf4j
@Service("AnalysisOutputService")
public class AnalysisServiceImpl implements AnalysisService{
	
	@Autowired
	private AnalysisOutputRepository analysisOutputRepository;
	
	@Autowired(required=false)
	private DynamoDbS3Operations ddbs3;
	
    @Value("${aws.s3.use}")
    private boolean isS3Use;
    
    @Value("${aws.dynamoDb.local}")
    private boolean isDynamoDbLocal;

	@Override
	public void save(AnalysisOutput analysis) {
		try{
			analysisOutputRepository.save(analysis);
		} catch(AmazonDynamoDBException addbe) {
			if(isS3Use && !isDynamoDbLocal) {
				log.info("Storing item in s3 since it item size exceeds more than 400kb");
				ddbs3.saveLargeItem(AmazonS3Config.BUCKET_NAME, analysis.getReCiterFeature(), AnalysisOutput.class.getSimpleName() + "/" + analysis.getUid());
				analysis.setReCiterFeature(null);
				analysis.setUsingS3(true);
				analysisOutputRepository.save(analysis);
			} else if(isDynamoDbLocal){
				log.info("You are running dynamodb in local mode. Add AWS access key and secret key to environment variable to enable S3 storage.");
			} else {
				log.info("Enable s3 use in application properties file to store larger objects. Set aws.s3.use to true and set aws.s3.dynamodb.bucketName");
			}
			
		}
		catch(Exception e)
		{
			log.info("AnalysisOutput",analysis);
			log.info("AnalysisOutput reciterFeature",analysis.getReCiterFeature());
			e.printStackTrace();
		}
	}

	@Override
	public AnalysisOutput findByUid(String uid) {
		AnalysisOutput analysisOutput = analysisOutputRepository.findById(uid).orElseGet(() -> null);
		performResourceCleanup(analysisOutput);
		if(analysisOutput != null 
				&&
				analysisOutput.isUsingS3()) {
			log.info("Retreving analysis from s3 for " + uid);
			ReCiterFeature reCiterFeature = (ReCiterFeature) ddbs3.retrieveLargeItem(AmazonS3Config.BUCKET_NAME, AnalysisOutput.class.getSimpleName() + "/" + uid.trim(), ReCiterFeature.class);
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

	@Override
	public List<AnalysisOutput> findByUids(List<String> uids) {
		List<AnalysisOutput> analysisOutputs = null;
        Iterator<reciter.database.dynamodb.model.AnalysisOutput> iterator = analysisOutputRepository.findAllById(uids).iterator();
        analysisOutputs = new ArrayList<>(uids.size());
        while (iterator.hasNext()) {
        	AnalysisOutput anaOutput = iterator.next();
        	if(anaOutput != null 
    				&&
    				anaOutput.isUsingS3()) {
        		log.info("Retreving analysis from s3 for " + anaOutput.getUid());
        		ReCiterFeature reCiterFeature = (ReCiterFeature) ddbs3.retrieveLargeItem(AmazonS3Config.BUCKET_NAME, AnalysisOutput.class.getSimpleName() + "/" + anaOutput.getUid(), ReCiterFeature.class);
        		anaOutput.setReCiterFeature(reCiterFeature);
        	}
        	analysisOutputs.add(anaOutput);
        }
        return analysisOutputs;
	}
	
	private void performResourceCleanup(AnalysisOutput analysisOutput) {
		if(analysisOutput != null) {
			//Case where Size has increased 400kb and reciterFeature needs to be null in dynamoDB
			if(analysisOutput.isUsingS3() && analysisOutput.getReCiterFeature() != null) {
				analysisOutput.setReCiterFeature(null);
				log.debug("Performing cleanup for analysis size > 400 kb for " + analysisOutput.getUid());
				analysisOutputRepository.save(analysisOutput);
			}
			//case when size decreases < 400kb then remove object from S3
			//Might Have to change it when isUsingS3 == true - ToDo
			//Does increase 1 more api call to check - only will increase for objects stored in dynamodb.
			if((!analysisOutput.isUsingS3() || analysisOutput.isUsingS3()) && analysisOutput.getReCiterFeature() != null) {
				log.debug("Performing cleanup for analysis size < 400 kb for " + analysisOutput.getUid());
				ddbs3.deleteLargeItem(AmazonS3Config.BUCKET_NAME, AnalysisOutput.class.getSimpleName() + "/" + analysisOutput.getUid());
			}
		}
	}

}
