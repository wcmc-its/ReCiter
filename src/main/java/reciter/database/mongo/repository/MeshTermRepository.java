package reciter.database.mongo.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.MeshTerm;

public interface MeshTermRepository extends MongoRepository<MeshTerm, ObjectId> {

}
