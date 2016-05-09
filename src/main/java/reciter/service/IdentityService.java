package reciter.service;

import java.util.List;

import reciter.service.bean.IdentityBean;

public interface IdentityService {
	IdentityBean getIdentityByCwid(String cwid);
	List<IdentityBean> getAssosiatedGrantIdentityList(String cwid);
}
