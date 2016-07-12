package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityEducationDao;
import reciter.database.model.IdentityEducation;
import reciter.model.author.AuthorEducation;
import reciter.service.IdentityEducationService;
import reciter.service.converter.IdentityEducationConverter;

@Service("identityEducationService")
public class IdentityEducationServiceImpl implements IdentityEducationService {

	@Autowired
	private IdentityEducationDao identityEducationDao;
	
	@Override
	public List<AuthorEducation> getEducations(String cwid) {
		List<IdentityEducation> identityEducations = identityEducationDao.getEducation(cwid);
		if (identityEducations != null) {
			return IdentityEducationConverter.convertToAuthorEducationList(identityEducations);
		} else {
			return null;
		}
	}
}
