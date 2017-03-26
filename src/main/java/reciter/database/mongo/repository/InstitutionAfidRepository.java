package reciter.database.mongo.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.InstitutionAfid;

public interface InstitutionAfidRepository extends MongoRepository<InstitutionAfid, ObjectId> {

	List<InstitutionAfid> findByInstitution(String institution);
}
