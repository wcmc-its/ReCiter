package reciter.string;

import java.util.HashSet;
import java.util.Set;

public class PubmedSearchQueryGenerator {

	/**
	 * 1. If there are dashes in the lastName, add a string with dashes replaced by single white space. Add a string
	 * with dashes replaced by empty string.
	 * 2. 
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @return
	 */
	public Set<String> generate(String firstName, String middleName, String lastName) {
		
		Set<String> nameVariants = new HashSet<String>();
		
		firstName = ReCiterStringUtil.deAccent(firstName);
		middleName = ReCiterStringUtil.deAccent(middleName);
		lastName = ReCiterStringUtil.deAccent(lastName);
		
		firstName = removeNonUsefulChars(firstName);
		middleName = removeNonUsefulChars(middleName);
		lastName = removeNonUsefulChars(lastName);
		
		// search verbosely by concatenating first name, middle name, and last name.
		if (middleName.length() > 0) {
			nameVariants.add(firstName + " " + middleName + " " + lastName);
		} else {
			nameVariants.add(firstName + " " + lastName);
		}
		
		String firstNameInitial;
		if (firstName.length() > 1) {
			firstNameInitial = firstName.substring(0, 1);
		} else {
			firstNameInitial = firstName;
		}
		
		nameVariants.add(lastName + " " + firstNameInitial);
		
		// if last name contains dashes.
		if (lastName.contains("-")) {
			String lastNameWithDashReplacedByWhiteSpace = lastName.replace("-", " ");
			nameVariants.add(lastNameWithDashReplacedByWhiteSpace + " " + firstNameInitial);
			
			String lastNameWithDashReplacedByEmptyString = lastName.replace("-", "");
			nameVariants.add(lastNameWithDashReplacedByEmptyString + " " + firstNameInitial);
		}
		
		// if last name contains white spaces.
		if (lastName.contains(" ")) {
			String lastNameWithSpaceReplacedByDash = lastName.replaceAll("\\s+", "-");
			nameVariants.add(lastNameWithSpaceReplacedByDash + " " + firstNameInitial);
			
			String lastNameWithSpaceReplacedByEmptyString = lastName.replaceAll("\\s+", "");
			nameVariants.add(lastNameWithSpaceReplacedByEmptyString + " " + firstNameInitial);
			
			// source: http://stackoverflow.com/questions/20653976/remove-short-words-and-characters-from-a-string-java
			String lastNameWithCharsLessThanSpecified = lastName.replaceAll("\\b\\w{1,3}\\b\\s?", "");
			nameVariants.add(lastNameWithCharsLessThanSpecified + " " + firstNameInitial);
		}
		return nameVariants;
	}
	
	public String removeNonUsefulChars(String s) {
		// source: http://stackoverflow.com/questions/267399/how-do-you-match-only-valid-roman-numerals-with-a-regular-expression
		return s.replaceAll("(IX|IV|V?I{0,3})", "").replaceAll("(JR)", "").replaceAll("[^A-Za-z0-9'\\-\\s+]", "")
				.replaceAll("\\s+", " ").trim();
	}
}
