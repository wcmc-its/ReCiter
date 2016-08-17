package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.Identity;
import reciter.database.mongo.repository.IdentityRepository;
import reciter.service.IdentityService;

@Service("identityService")
public class IdentityServiceImpl implements IdentityService {

//	@Autowired
//	private IdentityDao identityDao;

	@Autowired
	private IdentityRepository identityRepository;

//	@Override
//	public IdentityBean getIdentityByCwid(String cwid) {
//		Identity identity = identityDao.getIdentityByCwid(cwid);
//		return IdentityConverter.convertToDTO(identity);
//	}
//
//	@Override
//	public List<IdentityBean> getAssosiatedGrantIdentityList(String cwid) {
//		List<Identity> identities = identityDao.getAssosiatedGrantIdentityList(cwid);
//		List<IdentityBean> identityDTOs = new ArrayList<IdentityBean>();
//		for (Identity identity : identities) {
//			identityDTOs.add(IdentityConverter.convertToDTO(identity));
//		}
//		return identityDTOs;
//	}

	@Override
	public void save(List<reciter.database.mongo.model.Identity> identities) {
		for (reciter.database.mongo.model.Identity identity : identities) {
			identityRepository.save(identity);
		}
	}

	@Override
	public void save(reciter.database.mongo.model.Identity identity) {
		identityRepository.save(identity);
	}

//	@Override
//	public List<IdentityBean> getTargetAuthorByNameOrCwid(String search) {
//		List<Identity> identities = identityDao.getTargetAuthorByNameOrCwid(search);
//		List<IdentityBean> identityBeans = new ArrayList<IdentityBean>();
//		for (Identity identity : identities) {
//			identityBeans.add(IdentityConverter.convertToDTO(identity));
//		}
//		return identityBeans;
//	}
	
	@Override
	public Identity findByCwid(String cwid) {
		return identityRepository.findByCwid(cwid);
	}

	@Override
	public List<Identity> findByCwidRegex(String search) {
		return identityRepository.findByCwidRegex(search);
	}
}
