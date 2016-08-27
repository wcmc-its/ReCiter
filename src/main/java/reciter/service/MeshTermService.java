package reciter.service;

import java.util.List;

import reciter.database.mongo.model.MeshTerm;

public interface MeshTermService {

	void save(List<MeshTerm> meshTerms);

	List<MeshTerm> findAll();
}
