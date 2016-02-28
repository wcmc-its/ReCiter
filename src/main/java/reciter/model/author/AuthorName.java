package reciter.model.author;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorName {

	/**
	 * First Name.
	 */
	private String firstName;

	/**
	 * First Initial.
	 */
	private String firstInitial;

	/**
	 * Middle Name.
	 */
	private String middleName;

	/**
	 * Middle Initial.
	 */
	private String middleInitial;

	/**
	 * Last Name.
	 */
	private String lastName;

	/**
	 * Name variants.
	 */
	private Set<AuthorName> nameVariants;

	/**
	 * Constructs an author provided a first name, middle name, and last name.
	 * 
	 * @param firstName First name.
	 * @param middleName Middle name.
	 * @param lastName Last name.
	 */
	public AuthorName(String firstName, String middleName, String lastName) {
		this.firstName = StringUtils.capitalize(StringUtils.lowerCase(StringUtils.trim(firstName)));
		this.middleName = StringUtils.capitalize(StringUtils.lowerCase(StringUtils.trim(middleName)));
		this.lastName = StringUtils.capitalize(StringUtils.lowerCase(StringUtils.trim(lastName)));
		firstInitial = StringUtils.substring(this.firstName, 0, 1);
		middleInitial = StringUtils.substring(this.middleName, 0, 1);

		if (firstName == null) {
			this.firstName = "";
		}
		if (middleName == null) {
			this.middleName = "";
		}
		if (lastName == null) {
			this.lastName = "";
		}
		if (firstInitial == null) {
			this.firstInitial = "";
		}
		if (middleInitial == null ) {
			middleInitial = "";
		}
	}

	public boolean firstInitialLastNameMatch(AuthorName name) {
		return StringUtils.equals(getFirstInitial(), name.getFirstInitial()) &&
				StringUtils.equals(getLastName(), name.getLastName());
	}

	public double nameSimilarityScore(AuthorName name) {
		String pattern = match(name);
		if (StringUtils.equals("333", pattern)) {
			return 100;
		} else if (StringUtils.equals("332", pattern) || StringUtils.equals("323", pattern)) {
			return 80;
		} else if (StringUtils.equals("331", pattern)) {
			return 60;
		} else if (StringUtils.equals("330", pattern)) {
			return 40;
		} else if (StringUtils.equals("322", pattern)) {
			return 10;
		} else {
			return 0;
		}
	}

	/**
	 * Perform partial match on part of a name
	 * 0 = different.
	 * 1 = either null,
	 * 2 = either initial, (initial matches)
	 * 3 = same (multiple characters)
	 */
	private String matchNameParts(String x, String y) {
		if ((StringUtils.length(x) == 1 || StringUtils.length(y) == 1) &&
				(StringUtils.equals(StringUtils.substring(x, 0, 1), StringUtils.substring(y, 0, 1)))) { // either is initial
			return "2";
		} else if (StringUtils.length(x) == 0 || StringUtils.length(y) == 0) { // either is null
			return "1";
		} else if (StringUtils.equals(x, y)) {
			return "3";
		} else {
			return "0";
		}
	}

	/**
	 * Match two names accounting for variants
	 * > 330: last and first match
	 * = 320: bad match on middle name
	 * > 320: last and first initial match
	 */
	private String match(AuthorName y) {
		return matchNameParts(getFirstName(), y.getFirstName()) + 
				matchNameParts(getMiddleName(), y.getMiddleName()) + 
				matchNameParts(getLastName(), y.getLastName());
	}

	public boolean isFullNameMatch(AuthorName name) {

		return StringUtils.equalsIgnoreCase(firstName, name.getFirstName()) &&
				StringUtils.equalsIgnoreCase(middleName, name.getMiddleName()) &&
				StringUtils.equalsIgnoreCase(lastName, name.getLastName());
	}


	public boolean isFullName() {
		return (StringUtils.length(firstName) > 1 || StringUtils.length(middleName) > 1);
	}

	public boolean isNameVariant(AuthorName name) {
		return nameVariants.contains(name);
	}

	/**
	 * Return indexable variants of a name.
	 * type=target: restrict initials,
	 * type=coauthor: no restrictions
	 * initials=1: Last F
	 * initials=2: Last FM
	 * 
	 * @param type target or coauthor. If target, restrict initials. If 
	 * coauthor, no restrictions.
	 * This means that if initials is 1, variants of type "R Kukafka" is omitted. 
	 * (F, "", Last). This results in only 5 options. (assuming the name has
	 * full first, middle, and last names).
	 * 
	 * If initials is 2, variants of type above and type "R M Kukafka" is omitted.
	 * (F, M, Last). This results in only 4 options. (assuming the name has full
	 * first, middle, and last names).
	 * 
	 * @param initials If 1, Last F. If 2, Last FM
	 * @return A list of variants of a name.
	 * 
	 * Invariant: First Name and Last Name must exist, else IllegalArgumentException is thrown.
	 * 
	 * Eg: The name "Rita Mary Kukafka" has the following 6 variants:
	 * 
	 * 1. "Rita Mary Kukafka"
	 * 2. "R Kukafka"
	 * 3. "R Mary Kukafka"
	 * 4. "R M Kukafka"
	 * 5. "Rita Kukafka"
	 * 6. "Rita M Kukafka"
	 */
	public Set<AuthorName> variants(String type, int initials) {

		Set<AuthorName> v = new HashSet<AuthorName>();
		v.add(this);

		String first = getFirstName();
		String last = getLastName();

		// Check if first name can be initialized (ie: "Rita" becomes "R").
		if (StringUtils.length(firstName) > 1) {
			String firstInitial = StringUtils.substring(firstName, 0, 1);
			if (!"target".equals(type)) {
				v.add(new AuthorName(firstInitial, "", last)); // add variant (F, "", Last).
			}
			// Check whether middle name exists. If it does, add the variants with full
			// middle name, (eg: Rita Mary Kukafka), and middle initial (eg:
			// (Rita M Kukafka). Also, add (R Mary Kukafka) and (R M Kukafka).
			String middle = getMiddleName();
			if (StringUtils.length(middleName) > 1) {
				v.add(new AuthorName(firstInitial, middle, last)); // add variant (F, middle, last).
				String middleInitial = StringUtils.substring(middleName, 0, 1);
				if (!("target".equals(type) && initials == 2)) { // option "target" and initials 2 not specified.
					v.add(new AuthorName(firstInitial, middleInitial, last)); // add variant (F, M, last).
				}
				v.add(new AuthorName(first, "", last)); // add variant. (first, "", Last).
				v.add(new AuthorName(first, middleInitial, last)); // add variant (first, M, last).
			}
		}
		return v;
	}

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		if (firstName == null) throw new IllegalArgumentException("first name should not be null.");
		this.firstName = StringUtils.capitalize(StringUtils.lowerCase(StringUtils.trim(firstName)));
		this.firstInitial = firstName.length() > 0 ? firstName.substring(0, 1) : "";
	}
	public String getFirstInitial() {
		return firstInitial;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		if (middleName == null) throw new IllegalArgumentException("middle name should not be null.");
		this.middleName = StringUtils.capitalize(StringUtils.lowerCase(StringUtils.trim(middleName)));
		this.middleInitial = middleName.length() > 0 ? middleName.substring(0, 1) : "";
	}
	public String getMiddleInitial() {
		return middleInitial;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		if (lastName == null) throw new IllegalArgumentException("last name should not be null.");
		this.lastName = StringUtils.capitalize(StringUtils.lowerCase(StringUtils.trim(lastName)));
	}
	public Set<AuthorName> getNameVariants() {
		return nameVariants;
	}
	public void setNameVariants() {
		this.nameVariants = variants("coauthor", 1);
	}

	public String pubmedFormat() {
		return lastName + " " + firstInitial;
	}
	
	@Override
	public String toString() {
		if (middleName.length() > 0) {
			return firstName + " " + middleName + " " + lastName;
		} else {
			return firstName + " " + lastName;
		}
	}
}
