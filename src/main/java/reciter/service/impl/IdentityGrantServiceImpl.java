package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.IdentityGrantDao;
import reciter.database.model.IdentityGrant;
import reciter.service.IdentityGrantService;

@Service("identityGrantService")
public class IdentityGrantServiceImpl implements IdentityGrantService {

	@Autowired
	private IdentityGrantDao identityGrantDao;

	@Override
	public List<IdentityGrant> getIdentityGrantListByCwid(String cwid) {
		return identityGrantDao.getIdentityGrantListByCwid(cwid);
	}

	@Override
	public List<String> getSponsorAwardIdListByCwid(String cwid) {
		return identityGrantDao.getSponsorAwardIdListByCwid(cwid);
	}
}
