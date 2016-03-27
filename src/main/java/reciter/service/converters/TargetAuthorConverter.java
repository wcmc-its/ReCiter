package reciter.service.converters;

import reciter.database.model.Identity;
import reciter.database.model.IdentityEducation;
import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;

public class TargetAuthorConverter {
	
	public static TargetAuthor convert(Identity identity, IdentityEducation identityEducation) {
		
		TargetAuthor targetAuthor = new TargetAuthor(
				new AuthorName(
						identity.getFirstName(), identity.getMiddleName(), identity.getLastName()),
				new AuthorAffiliation(identity.getPrimaryAffiliation()));
		
		targetAuthor.setCwid(identity.getCwid());
		targetAuthor.setDepartment(identity.getPrimaryDepartment());
//		targetAuthor.setEducation();
		return targetAuthor;
	}

}
