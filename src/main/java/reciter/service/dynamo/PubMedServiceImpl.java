package reciter.service.dynamo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.DynamoDbS3Operations;
import reciter.database.dynamodb.repository.PubMedArticleRepository;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;
import reciter.storage.s3.AmazonS3Config;

@Slf4j
@Service("pubMedService")
public class PubMedServiceImpl implements PubMedService {

    @Autowired
    private PubMedArticleRepository pubMedRepository;
    
    @Autowired(required=false)
	private DynamoDbS3Operations ddbs3;
	
    @Value("${aws.s3.use}")
    private boolean isS3Use;
    
    @Value("${aws.dynamoDb.local}")
    private boolean isDynamoDbLocal;

    @Override
    public void save(Collection<PubMedArticle> pubMedArticles) {
        List<reciter.database.dynamodb.model.PubMedArticle> pubmedArticlesDb = new ArrayList<>();
        for (PubMedArticle pubMedArticle : pubMedArticles) {
            reciter.database.dynamodb.model.PubMedArticle pubMedArticleDb = new reciter.database.dynamodb.model.PubMedArticle(
                    pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid(),
                    pubMedArticle
            );
            pubmedArticlesDb.add(pubMedArticleDb);
        }
        try{
		//modifying the large articles whose size in > 400KB. Storing those to S3 and meta data into DynamoDB.
        	 for (reciter.database.dynamodb.model.PubMedArticle article : pubmedArticlesDb) {
        	        offloadLargeFields(article,  AmazonS3Config.BUCKET_NAME);
        	    }
        	pubMedRepository.saveAll(pubmedArticlesDb);
        } catch(Exception e) { //This is to skip over articles with huge list of authors e.g. yiwang - 29547300
        	log.info(e.getMessage());
        }
    }

    @Override
    public List<PubMedArticle> findByPmids(List<Long> pmids) {
        List<PubMedArticle> pubMedArticles = null;
        Iterator<reciter.database.dynamodb.model.PubMedArticle> iterator = pubMedRepository.findAllById(pmids).iterator();
        pubMedArticles = new ArrayList<>(pmids.size());
        while (iterator.hasNext()) {
        	reciter.database.dynamodb.model.PubMedArticle pubMedArticle = iterator.next();
        	if(pubMedArticle!=null && pubMedArticle.isUsingS3())
        	{
        		PubMedArticle pubMedArticleOutput = (PubMedArticle) ddbs3.retrieveLargeItem(AmazonS3Config.BUCKET_NAME, PubMedArticle.class.getSimpleName() + "/" + pubMedArticle.getPmid(), PubMedArticle.class);
    			log.info("PubMed Article retrieved from the S3 is : "+pubMedArticleOutput);
				pubMedArticle.setPubMedArticle(pubMedArticleOutput);
        	}
        	PubMedArticle pubarticle = pubMedArticle.getPubMedArticle();
        	
            pubMedArticles.add(pubarticle);
        }
        return pubMedArticles;
    }

    @Override
    public PubMedArticle findByPmid(Long pmid) {
        reciter.database.dynamodb.model.PubMedArticle pubMedArticle = pubMedRepository.findById(pmid).orElseGet(() -> null);
		performResourceCleanup(pubMedArticle);
        if (pubMedArticle != null && pubMedArticle.isUsingS3()) {
    			log.info("Retreving pubmed article from s3 for " + pmid);
    			PubMedArticle pubMedArticleOutput = (PubMedArticle) ddbs3.retrieveLargeItem(AmazonS3Config.BUCKET_NAME, PubMedArticle.class.getSimpleName() + "/" + pmid, PubMedArticle.class);
    			log.info("PubMed Article retrieved from the S3 is : "+pubMedArticleOutput);
				pubMedArticle.setPubMedArticle(pubMedArticleOutput);
    		} 
            return pubMedArticle.getPubMedArticle();
        
    }
    private void offloadLargeFields(reciter.database.dynamodb.model.PubMedArticle article, String bucketName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
        	 
	            // Estimate item size as JSON
	            String json = mapper.writeValueAsString(article);
	            int sizeInBytes = json.getBytes(StandardCharsets.UTF_8).length;
	
	            if (sizeInBytes > 400 * 1024 && isS3Use && !isDynamoDbLocal) 
	            {
	         		log.info("Storing item in s3 since it item size exceeds more than 400kb PMID: "+article.getPmid() + " and Size :" + sizeInBytes/1024 +" KB"");
					ddbs3.saveLargeItem(AmazonS3Config.BUCKET_NAME, article.getPubMedArticle(), PubMedArticle.class.getSimpleName() + "/" + article.getPmid());
					article.setPubMedArticle(null);
					article.setUsingS3(true);
	            } else if(isDynamoDbLocal){
	    			log.info("You are running dynamodb in local mode. Add AWS access key and secret key to environment variable to enable S3 storage.");
	    		} else {
	    			log.info("Enable s3 use in application properties file to store larger objects. Set aws.s3.use to true and set aws.s3.dynamodb.bucketName");
	    		}
                
            }
        	catch (Exception e) {
        		throw new RuntimeException("Failed to offload large field to S3", e);
        	}
    }
	private void performResourceCleanup(reciter.database.dynamodb.model.PubMedArticle pubMedArticle) {
		if(pubMedArticle != null) {
			//Case where Size has increased 400kb and reciterFeature needs to be null in dynamoDB
			if(pubMedArticle.isUsingS3() && pubMedArticle.getPubMedArticle() != null) {
				pubMedArticle.setPubMedArticle(null);
				log.debug("Performing cleanup for pubmed article size > 400 kb for " + pubMedArticle.getPmid());
				pubMedRepository.save(pubMedArticle);
			}
			//case when size decreases < 400kb then remove object from S3
			//Might Have to change it when isUsingS3 == true - ToDo
			//Does increase 1 more api call to check - only will increase for objects stored in dynamodb.
			if((!pubMedArticle.isUsingS3() || pubMedArticle.isUsingS3()) && pubMedArticle.getPubMedArticle() != null) {
				log.debug("Performing cleanup for analysis size < 400 kb for " + pubMedArticle.getPmid());
				ddbs3.deleteLargeItem(AmazonS3Config.BUCKET_NAME, PubMedArticle.class.getSimpleName() + "/" + pubMedArticle.getPmid());
			}
		}
	}
   
}
