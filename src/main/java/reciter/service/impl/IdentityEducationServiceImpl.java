package reciter.service.impl;

import java.util.List;

import reciter.database.dao.IdentityEducationDao;
import reciter.database.dao.impl.IdentityEducationDaoImpl;
import reciter.database.model.IdentityEducation;
import reciter.model.author.AuthorEducation;
import reciter.service.IdentityEducationService;
import reciter.service.converters.IdentityEducationConverter;

public class IdentityEducationServiceImpl implements IdentityEducationService {

	@Override
	public List<AuthorEducation> getEducations(String cwid) {
		IdentityEducationDao identityEducationDao = new IdentityEducationDaoImpl();
		List<IdentityEducation> identityEducations = identityEducationDao.getEducation(cwid);
		if (identityEducations != null) {
			return IdentityEducationConverter.convertToAuthorEducationList(identityEducations);
		} else {
			return null;
		}
	}
}
