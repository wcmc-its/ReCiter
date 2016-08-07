package reciter.service;

import java.util.List;

import reciter.database.mongo.model.Identity;
import reciter.service.bean.IdentityBean;

public interface IdentityService {
	IdentityBean getIdentityByCwid(String cwid);
	List<IdentityBean> getAssosiatedGrantIdentityList(String cwid);
	void save(List<Identity> identities);
	List<IdentityBean> getTargetAuthorByNameOrCwid(String search);
	void save(Identity identity);
}
