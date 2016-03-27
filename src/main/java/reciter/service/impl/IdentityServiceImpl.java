package reciter.service.impl;

import java.util.ArrayList;
import java.util.List;

import reciter.database.dao.IdentityDao;
import reciter.database.dao.impl.IdentityDaoImpl;
import reciter.database.model.Identity;
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

	@Override
	public List<IdentityDTO> getAssosiatedGrantIdentityList(String cwid) {
		IdentityDao identityDao = new IdentityDaoImpl();
		List<Identity> identities = identityDao.getAssosiatedGrantIdentityList(cwid);
		List<IdentityDTO> identityDTOs = new ArrayList<IdentityDTO>();
		IdentityConverter identityConverter = new IdentityConverter();
		for (Identity identity : identities) {
			identityDTOs.add(identityConverter.convertToDTO(identity));
		}
		return identityDTOs;
	}
}
