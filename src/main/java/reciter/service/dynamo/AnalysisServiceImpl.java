package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import reciter.model.identity.Identity;
import reciter.model.scopus.ScopusArticle;
import reciter.service.AnalysisService;

@Slf4j
@Service("AnalysisOutputService")
public class AnalysisServiceImpl implements AnalysisService{
	
	@Autowired
	private AnalysisOutputRepository analysisOutputRepository;
	
	@Autowired(required=false)
	private AmazonS3 s3;
	
	@Autowired(required=false)
	private DynamoDbS3Operations ddbs3;
	
    @Value("${aws.s3.use}")
    private boolean isS3Use;
	
    @Value("${aws.s3.dynamodb.bucketName}")
    private String s3BucketName;
    
    @Value("${aws.dynamoDb.local}")
    private boolean isDynamoDbLocal;

	@Override
	public void save(AnalysisOutput analysis) {
		try{
			analysisOutputRepository.save(analysis);
		} catch(AmazonDynamoDBException addbe) {
			if(isS3Use && !isDynamoDbLocal) {
				log.info("Storing item in s3 since it item size exceeds more than 400kb");
				ddbs3.saveLargeItem(s3BucketName, analysis.getReCiterFeature(), AnalysisOutput.class.getSimpleName() + "/" + analysis.getUid());
				analysis.setReCiterFeature(null);
				analysis.setUsingS3(true);
				analysisOutputRepository.save(analysis);
			} else if(isDynamoDbLocal){
				log.info("You are running dynamodb in local mode. Add AWS access key and secret key to environment variable to enable S3 storage.");
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
        		ReCiterFeature reCiterFeature = (ReCiterFeature) ddbs3.retrieveLargeItem(s3BucketName, AnalysisOutput.class.getSimpleName() + "/" + anaOutput.getUid(), ReCiterFeature.class);
        		anaOutput.setReCiterFeature(reCiterFeature);
        	}
        	analysisOutputs.add(anaOutput);
        }
        return analysisOutputs;
	}
	

}
