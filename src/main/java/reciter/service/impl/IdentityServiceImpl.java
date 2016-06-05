package reciter.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityDao;
import reciter.database.model.Identity;
import reciter.service.IdentityService;
import reciter.service.bean.IdentityBean;
import reciter.service.converters.IdentityConverter;

@Service("identityService")
public class IdentityServiceImpl implements IdentityService {

	@Autowired
	private IdentityDao identityDao;
	
	@Override
	public IdentityBean getIdentityByCwid(String cwid) {
		Identity identity = identityDao.getIdentityByCwid(cwid);
		IdentityConverter identityConverter = new IdentityConverter();
		return identityConverter.convertToDTO(identity);
	}

	@Override
	public List<IdentityBean> getAssosiatedGrantIdentityList(String cwid) {
		List<Identity> identities = identityDao.getAssosiatedGrantIdentityList(cwid);
		List<IdentityBean> identityDTOs = new ArrayList<IdentityBean>();
		IdentityConverter identityConverter = new IdentityConverter();
		for (Identity identity : identities) {
			identityDTOs.add(identityConverter.convertToDTO(identity));
		}
		return identityDTOs;
	}
}
