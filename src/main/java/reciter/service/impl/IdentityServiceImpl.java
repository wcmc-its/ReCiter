package reciter.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityDao;
import reciter.database.model.Identity;
import reciter.database.mongo.repository.IdentityRepository;
import reciter.service.IdentityService;
import reciter.service.bean.IdentityBean;
import reciter.service.converter.IdentityConverter;

@Service("identityService")
public class IdentityServiceImpl implements IdentityService {

	@Autowired
	private IdentityDao identityDao;
	
	@Autowired
	private IdentityRepository identityRepository;
	
	@Override
	public IdentityBean getIdentityByCwid(String cwid) {
		Identity identity = identityDao.getIdentityByCwid(cwid);
		return IdentityConverter.convertToDTO(identity);
	}

	@Override
	public List<IdentityBean> getAssosiatedGrantIdentityList(String cwid) {
		List<Identity> identities = identityDao.getAssosiatedGrantIdentityList(cwid);
		List<IdentityBean> identityDTOs = new ArrayList<IdentityBean>();
		for (Identity identity : identities) {
			identityDTOs.add(IdentityConverter.convertToDTO(identity));
		}
		return identityDTOs;
	}
	
	@Override
	public void save(List<reciter.database.mongo.model.Identity> identities) {
		for (reciter.database.mongo.model.Identity identity : identities) {
			identityRepository.save(identity);
		}
	}
	
	@Override
	public List<IdentityBean> getTargetAuthorByNameOrCwid(String search) {
		List<Identity> identities = identityDao.getTargetAuthorByNameOrCwid(search);
		List<IdentityBean> identityBeans = new ArrayList<IdentityBean>();
		for (Identity identity : identities) {
			identityBeans.add(IdentityConverter.convertToDTO(identity));
		}
		return identityBeans;
	}
}
