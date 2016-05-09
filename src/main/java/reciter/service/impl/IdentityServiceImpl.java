package reciter.service.impl;

import java.util.ArrayList;
import java.util.List;

import reciter.database.dao.IdentityDao;
import reciter.database.dao.impl.IdentityDaoImpl;
import reciter.database.model.Identity;
import reciter.service.IdentityService;
import reciter.service.bean.IdentityBean;
import reciter.service.converters.IdentityConverter;

public class IdentityServiceImpl implements IdentityService {

	@Override
	public IdentityBean getIdentityByCwid(String cwid) {
		IdentityDao identityDao = new IdentityDaoImpl();
		Identity identity = identityDao.getIdentityByCwid(cwid);
		IdentityConverter identityConverter = new IdentityConverter();
		return identityConverter.convertToDTO(identity);
	}

	@Override
	public List<IdentityBean> getAssosiatedGrantIdentityList(String cwid) {
		IdentityDao identityDao = new IdentityDaoImpl();
		List<Identity> identities = identityDao.getAssosiatedGrantIdentityList(cwid);
		List<IdentityBean> identityDTOs = new ArrayList<IdentityBean>();
		IdentityConverter identityConverter = new IdentityConverter();
		for (Identity identity : identities) {
			identityDTOs.add(identityConverter.convertToDTO(identity));
		}
		return identityDTOs;
	}
}
