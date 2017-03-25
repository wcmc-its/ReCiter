package reciter.service.mongo;

import reciter.database.mongo.model.GoldStandard;

public interface GoldStandardService {

	void save(GoldStandard goldStandard);
	GoldStandard findByCwid(String cwid);
}
