package reciter.database.mongo.repository;

import reciter.database.mongo.model.Identity;

public interface IdentityRepositoryCustom {

	void updatePubMedAlias(Identity identity);
}
