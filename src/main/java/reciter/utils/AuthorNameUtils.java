package reciter.utils;

import org.apache.commons.lang3.StringUtils;

import reciter.model.author.AuthorName;

public class AuthorNameUtils {

	/**
	 * Check whether two author names match on first name, middle name and
	 * last name.
	 * 
	 * @param name
	 * @param other
	 * 
	 * @return {@code true} if the two author names match on first name, middle
	 * name and last name.
	 */
	public static boolean isFullNameMatch(AuthorName name, AuthorName other) {

		return StringUtils.equalsIgnoreCase(name.getFirstName(), name.getFirstName()) &&
				StringUtils.equalsIgnoreCase(name.getMiddleName(), name.getMiddleName()) &&
				StringUtils.equalsIgnoreCase(name.getLastName(), name.getLastName());
	}
	
	public static boolean isFirstNameMatch(AuthorName name, AuthorName other) {
		return StringUtils.equalsIgnoreCase(name.getFirstName(), other.getFirstName());
	}
	
	public static boolean isMiddleNameMatch(AuthorName name, AuthorName other) {
		return StringUtils.equalsIgnoreCase(name.getMiddleName(), name.getMiddleName());
	}
	
	public static boolean isLastNameMatch(AuthorName name, AuthorName other) {
		return StringUtils.equalsIgnoreCase(name.getLastName(), other.getLastName());
	}
	
}
