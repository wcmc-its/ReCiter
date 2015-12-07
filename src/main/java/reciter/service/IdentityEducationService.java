package reciter.service;

import java.util.List;

import reciter.model.author.AuthorEducation;

public interface IdentityEducationService {

	List<AuthorEducation> getEducations(String cwid);
	
}
