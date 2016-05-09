package reciter.service.converters;

import reciter.database.model.CoauthorAffiliations;
import reciter.service.bean.CoauthorAffiliationsBean;

public class CoauthorAffiliationsConverter {

	public static CoauthorAffiliationsBean convert(CoauthorAffiliations coauthorAffiliations) {
		CoauthorAffiliationsBean coauthorAffiliationsDTO = new CoauthorAffiliationsBean();
		coauthorAffiliationsDTO.setAffiliationId(coauthorAffiliations.getAffiliationId());
		coauthorAffiliationsDTO.setLabel(coauthorAffiliations.getLabel());
		coauthorAffiliationsDTO.setScore(coauthorAffiliations.getScore());
		return coauthorAffiliationsDTO;
	}
}
