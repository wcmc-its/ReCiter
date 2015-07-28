package reciter.service.converters;

import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import database.model.Identity;
import database.model.IdentityEducation;

public class TargetAuthorConverter {
	
	public static TargetAuthor convert(Identity identity, IdentityEducation identityEducation) {
		
		TargetAuthor targetAuthor = new TargetAuthor(
				new AuthorName(
						identity.getFirstName(), identity.getMiddleName(), identity.getLastName()),
				new AuthorAffiliation(identity.getPrimaryAffiliation()));
		
		targetAuthor.setCwid(identity.getCwid());
		targetAuthor.setDepartment(identity.getPrimaryDepartment());
		targetAuthor.setEducation();
		return targetAuthor;
	}

}
