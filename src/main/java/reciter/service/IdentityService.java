package reciter.service;

import java.util.List;

import reciter.service.dto.IdentityDTO;

public interface IdentityService {
	IdentityDTO getIdentityByCwid(String cwid);
	List<IdentityDTO> getAssosiatedGrantIdentityList(String cwid);
}
