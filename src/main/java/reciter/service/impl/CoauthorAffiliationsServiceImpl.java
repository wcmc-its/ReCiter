package reciter.service.impl;

import database.dao.CoauthorAffiliationsDao;
import database.dao.impl.CoauthorAffiliationsDaoImpl;
import reciter.service.CoauthorAffiliationsService;
import reciter.service.converters.CoauthorAffiliationsConverter;
import reciter.service.dto.CoauthorAffiliationsDTO;

public class CoauthorAffiliationsServiceImpl implements CoauthorAffiliationsService {

	@Override
	public CoauthorAffiliationsDTO getCoauthorAffiliationsByLabel(String label) {
		CoauthorAffiliationsDao coauthorAffiliationsDao = new CoauthorAffiliationsDaoImpl();
		return CoauthorAffiliationsConverter.convert(coauthorAffiliationsDao.getCoathorAffiliationsByAffiliationLabel(label));
	}
}
