package reciter.service.impl;

import reciter.database.dao.CoauthorAffiliationsDao;
import reciter.database.dao.impl.CoauthorAffiliationsDaoImpl;
import reciter.service.CoauthorAffiliationsService;
import reciter.service.bean.CoauthorAffiliationsBean;
import reciter.service.converters.CoauthorAffiliationsConverter;

public class CoauthorAffiliationsServiceImpl implements CoauthorAffiliationsService {

	@Override
	public CoauthorAffiliationsBean getCoauthorAffiliationsByLabel(String label) {
		CoauthorAffiliationsDao coauthorAffiliationsDao = new CoauthorAffiliationsDaoImpl();
		return CoauthorAffiliationsConverter.convert(coauthorAffiliationsDao.getCoathorAffiliationsByAffiliationLabel(label));
	}
}
