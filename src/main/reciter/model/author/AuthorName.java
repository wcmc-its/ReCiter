package main.reciter.model.author;

import java.util.HashSet;
import java.util.Set;

import net.sf.junidecode.Junidecode;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorName {

	private String firstName;
	private String firstInitial;
	private String middleName;
	private String middleInitial;
	private String lastName;
	private Set<AuthorName> nameVariants;

	private static final Logger slf4jLogger = LoggerFactory.getLogger(AuthorName.class);	

	public AuthorName(String firstName, String middleName, String lastName) {
		this.firstName = StringUtils.capitalize(StringUtils.lowerCase(firstName));
		this.middleName = StringUtils.capitalize(StringUtils.lowerCase(middleName));
		this.lastName = StringUtils.capitalize(StringUtils.lowerCase(lastName));
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

	public String getCSVFormat() {
		StringBuilder sb = new StringBuilder();
		if (firstName != null) {
			sb.append(firstName);
			sb.append(" ");
		}
		if (middleName != null) {
			sb.append(middleName);
			sb.append(" ");
		}
		if (lastName != null) {
			sb.append(lastName);
		}
		return sb.toString();
	}

	public String getLuceneIndexableFormat() {
		StringBuilder sb = new StringBuilder();
		sb.append("first_name_");
		sb.append(StringUtils.stripAccents(firstName));
		sb.append(" middle_name_");
		sb.append(StringUtils.stripAccents(middleName));
		sb.append(" last_name_");
		sb.append(lastName);
		return sb.toString();
	}

	public static AuthorName deFormatLucene(String luceneFormat) {
		int middleNameIdx = luceneFormat.indexOf(" middle_name_");
		int lastNameIdx = luceneFormat.indexOf(" last_name_");

		String firstName = luceneFormat.substring("first_name_".length(), middleNameIdx);		
		String middleName = luceneFormat.substring(" middle_name_".length() + middleNameIdx, lastNameIdx);
		String lastName = luceneFormat.substring(" last_name_".length() + lastNameIdx, luceneFormat.length() - 1);

		// Check if name is null indexed, assign to null.
		if (firstName.equals("null")) {
			firstName = null;
		} else {
			// else capitalize first letter because Lucene doesn't keep case.
			firstName = StringUtils.capitalize(firstName);
		}

		if (middleName.equals("null")) {
			middleName = null;
		} else {
			middleName = StringUtils.capitalize(middleName);
		}

		if (lastName.equals("null")) {
			lastName = null;
		} else {
			lastName = StringUtils.capitalize(lastName);
		}

		return new AuthorName(firstName, middleName, lastName); 
	}

	/**
	 * Constructs a String from last name and first initial into a format that
	 * can be used to search this name in PubMed. 
	 * <pre>
	 * eg: Mary Jane Smith becomes "Smith%20M"
	 * <pre>
	 * @return Last name concatenated with "%20" and first initial. If first
	 * initial of the name is not provided, return last name only.
	 * 
	 * @throws IllegalArgumentException Last name's length is zero.
	 */
	public String getPubmedQueryFormat() {
		if (StringUtils.length(getLastName()) == 0) {
			throw new IllegalArgumentException("Last name is null.");
		}
		if (StringUtils.length(getFirstInitial()) == 0) {
			slf4jLogger.info("Only last name provided to be searched.");
			return getLastName();
		} else {
			return getLastName() + "%20" + getFirstInitial();
		}
	}

	public boolean firstInitialLastNameMatch(AuthorName name) {
		return StringUtils.equals(Junidecode.unidecode(getFirstInitial()), Junidecode.unidecode(name.getFirstInitial())) &&
				StringUtils.equals(Junidecode.unidecode(getLastName()), Junidecode.unidecode(name.getLastName()));
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
	 * Match two names accounting for variants
	 * > 330: last and first match
	 * = 320: bad match on middle name
	 * > 320: last and first initial match
	 */
	private String match(AuthorName y) {
		return matchnamepart(getFirstName(), y.getFirstName()) + 
				matchnamepart(getMiddleName(), y.getMiddleName()) + 
				matchnamepart(getLastName(), y.getLastName());
	}

	/**
	 * Perform partial match on part of a name
	 * 0 = different.
	 * 1 = either null,
	 * 2 = either initial, (initial matches)
	 * 3 = same (multiple characters)
	 */
	private String matchnamepart(String x, String y) {
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

	public boolean isFullNameMatch(AuthorName name) {
		
		return StringUtils.equals(Junidecode.unidecode(firstName), Junidecode.unidecode(name.getFirstName())) &&
				StringUtils.equals(Junidecode.unidecode(middleName), Junidecode.unidecode(name.getMiddleName())) &&
				StringUtils.equals(Junidecode.unidecode(lastName), Junidecode.unidecode(name.getLastName()));
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result
				+ ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result
				+ ((middleName == null) ? 0 : middleName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthorName other = (AuthorName) obj;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (middleName == null) {
			if (other.middleName != null)
				return false;
		} else if (!middleName.equals(other.middleName))
			return false;
		return true;
	}

	public String getFirstName() {
		return firstName;
	}
	public String getFirstInitial() {
		return firstInitial;
	}
	public String getMiddleName() {
		return middleName;
	}
	public String getMiddleInitial() {
		return middleInitial;
	}
	public String getLastName() {
		return lastName;
	}
	public Set<AuthorName> getNameVariants() {
		return nameVariants;
	}
	public void setNameVariants() {
		this.nameVariants = variants("coauthor", 1);
	}

	@Override
	public String toString() {
		return firstName + " " + middleName + " " + lastName;
	}

}
