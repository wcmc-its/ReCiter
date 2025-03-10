package reciter.database.dynamodb.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.service.ScienceMetrixService;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@Repository
public class ScienceMetrixRepository implements ScienceMetrixService {
    
	 private final DynamoDbTable<ScienceMetrix> scienceMetrixTable;
	
	 public ScienceMetrixRepository(DynamoDbEnhancedClient enhancedClient) {
	        this.scienceMetrixTable = enhancedClient.table("ScienceMetrix", TableSchema.fromBean(ScienceMetrix.class));
	    }
	 
	
	
	 public Collection<ScienceMetrix> saveAll(Collection<ScienceMetrix> scienceMetrix) {
	        scienceMetrix.forEach(entity -> scienceMetrixTable.putItem(entity));
	        return scienceMetrix;
	    }
	    
	    public List<ScienceMetrix> findAll() {
	        return scienceMetrixTable.scan().items().stream().toList();
	    }


		@Override
		public ScienceMetrix findBySmsid(Long smsid) {
			  return scienceMetrixTable.getItem(r -> r.key(k -> k.partitionValue(smsid)));
		}

		
		@Override
		public void save(Collection<ScienceMetrix> scienceMetrix) {
			scienceMetrix.forEach(entity -> scienceMetrixTable.putItem(entity));
			
		}

		@Override
		public long getItemCount() {
			 return scienceMetrixTable.scan().items().spliterator().getExactSizeIfKnown();
		}

		@Override
		public ScienceMetrix findByEissn(String eissn) {
	        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
	                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(eissn))) // Correct key condition
	                .build();

	        return  scienceMetrixTable.query(queryRequest)
	                        .items()
	                        .stream()
	                        .findFirst()
	                        .orElse(null);
		}

		@Override
		public ScienceMetrix findByIssn(String issn) {
			 QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
			            .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(issn))) // Correct key condition
			            .build();

			    return scienceMetrixTable.query(queryRequest)
			            .items()
			            .stream()
			            .findFirst()
			            .orElse(null);
		}
		public long count() {
			return scienceMetrixTable.scan().items().spliterator().getExactSizeIfKnown();
		}
	    
		 public ScienceMetrix findById(Long id) {
		        return scienceMetrixTable.getItem(r -> r.key(k -> k.partitionValue(id)));
		    }
		
		/*
		 * ScienceMetrix findByEissn(String eissn);
		 * 
		 * ScienceMetrix findByIssn(String issn);
		 */
}
