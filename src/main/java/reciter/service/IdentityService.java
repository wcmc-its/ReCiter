package reciter.service;

import reciter.service.dto.IdentityDTO;

public interface IdentityService {
	IdentityDTO getIdentityByCwid(String cwid);
}
