package reciter.service.impl;

import database.dao.IdentityDao;
import database.dao.impl.IdentityDaoImpl;
import database.model.Identity;
import reciter.service.IdentityService;
import reciter.service.converters.IdentityConverter;
import reciter.service.dto.IdentityDTO;

public class IdentityServiceImpl implements IdentityService {

	@Override
	public IdentityDTO getIdentityByCwid(String cwid) {
		IdentityDao identityDao = new IdentityDaoImpl();
		Identity identity = identityDao.getIdentityByCwid(cwid);
		IdentityConverter identityConverter = new IdentityConverter();
		return identityConverter.convertToDTO(identity);
	}
}
