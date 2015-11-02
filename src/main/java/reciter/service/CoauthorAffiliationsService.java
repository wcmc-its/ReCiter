package reciter.service;

import reciter.service.dto.CoauthorAffiliationsDTO;

public interface CoauthorAffiliationsService {

	CoauthorAffiliationsDTO getCoauthorAffiliationsByLabel(String label);
}
