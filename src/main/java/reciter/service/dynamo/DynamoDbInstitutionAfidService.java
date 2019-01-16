package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.model.InstitutionAfid;
import reciter.database.dynamodb.model.MeshTerm;
import reciter.database.dynamodb.repository.DynamoDbInstitutionAfidRepository;
import reciter.model.identity.Identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service("dynamoDbInstitutionAfidService")
public class DynamoDbInstitutionAfidService implements IDynamoDbInstitutionAfidService {

    @Autowired
    private DynamoDbInstitutionAfidRepository dynamoDbInstitutionAfidRepository;

    @Override
    public void save(Collection<InstitutionAfid> institutionAfids) {
        dynamoDbInstitutionAfidRepository.saveAll(institutionAfids);
    }

    @Override
    public InstitutionAfid findByInstitution(String institution) {
        return dynamoDbInstitutionAfidRepository.findById(institution).orElseGet(() -> null);
    }
    
    @Override
    public List<InstitutionAfid> findAll() {
    	Iterable<InstitutionAfid> institutionsAfidIterable = dynamoDbInstitutionAfidRepository.findAll();
        List<InstitutionAfid> institutionAfids = new ArrayList<>();
        Iterator<InstitutionAfid> iterator = institutionsAfidIterable.iterator();
        while (iterator.hasNext()) {
        	InstitutionAfid institutionAfid = iterator.next();
        	institutionAfids.add(institutionAfid);
        }
        return institutionAfids;
    }
    
    @Override
	public long getItemCount() {
		return dynamoDbInstitutionAfidRepository.count();
	}
}
