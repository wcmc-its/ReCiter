package reciter.service;

import reciter.database.mongo.model.InstitutionAfid;

import java.util.List;

public interface InstitutionAfidService {

	List<String> getAfidByInstitution(String institution);
	List<InstitutionAfid> findAll();
}
