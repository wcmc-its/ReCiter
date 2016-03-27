package reciter.service.converters;

import reciter.database.model.CoauthorAffiliations;
import reciter.service.dto.CoauthorAffiliationsDTO;

public class CoauthorAffiliationsConverter {

	public static CoauthorAffiliationsDTO convert(CoauthorAffiliations coauthorAffiliations) {
		CoauthorAffiliationsDTO coauthorAffiliationsDTO = new CoauthorAffiliationsDTO();
		coauthorAffiliationsDTO.setAffiliationId(coauthorAffiliations.getAffiliationId());
		coauthorAffiliationsDTO.setLabel(coauthorAffiliations.getLabel());
		coauthorAffiliationsDTO.setScore(coauthorAffiliations.getScore());
		return coauthorAffiliationsDTO;
	}
}
