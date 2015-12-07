package reciter.service.converters;

import java.util.ArrayList;
import java.util.List;

import database.model.IdentityEducation;
import reciter.model.author.AuthorEducation;

public class IdentityEducationConverter {

	public static AuthorEducation convertToAuthorEducation(IdentityEducation identityEducation) {
		AuthorEducation authorEducation = new AuthorEducation();
		authorEducation.setInstitution(identityEducation.getInstitution());
		authorEducation.setDegreeYear(identityEducation.getDegreeYear());
		authorEducation.setDegreeField(identityEducation.getDegreeField());
		authorEducation.setInstLoc(identityEducation.getInstLoc());
		authorEducation.setInstAbbr(identityEducation.getInstAbbr());
		return authorEducation;
	}
	
	public static List<AuthorEducation> convertToAuthorEducationList(List<IdentityEducation> identityEducations) {
		List<AuthorEducation> authorEducations = new ArrayList<AuthorEducation>();
		for (IdentityEducation identityEducation : identityEducations) {
			authorEducations.add(convertToAuthorEducation(identityEducation));
		}
		return authorEducations;
	}
}
