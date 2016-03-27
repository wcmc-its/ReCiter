package reciter.service.converters;

import reciter.database.model.IdentityDegree;
import reciter.model.author.AuthorDegree;

/**
 * Converter class to convert between AuthorDegree and IdentityDegree.
 * @author jil3004
 *
 */
public class IdentityDegreeConverter {

	public static AuthorDegree convert(IdentityDegree identityDegree) {
		AuthorDegree authorDegree = new AuthorDegree();
		authorDegree.setCwid(identityDegree.getCwid());
		authorDegree.setBachelor(identityDegree.getBachelor());
		authorDegree.setDoctoral(identityDegree.getDoctoral());
		authorDegree.setMasters(identityDegree.getMasters());
		authorDegree.setId(identityDegree.getId());
		return authorDegree;
	}
}
