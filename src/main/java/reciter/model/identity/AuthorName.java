/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.model.identity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamoDBDocument
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

	public AuthorName() {}

	/**
	 * Constructs an author provided a first name, middle name, and last name.
	 * 
	 * @param firstName First name.
	 * @param middleName Middle name.
	 * @param lastName Last name.
	 */
	public AuthorName(String firstName, String middleName, String lastName) {

		if (firstName == null) {
			this.firstName = "";
			this.firstInitial = "";
		} else {
			//this.firstName = capitalize(firstName.trim().toLowerCase());
			this.firstName = firstName.trim();
			this.firstInitial = this.firstName.substring(0, 1);
		}

		if (middleName == null) {
			this.middleName = "";
			this.middleInitial = "";
		} else {
			//this.middleName = capitalize(middleName.trim().toLowerCase());
			this.middleName = middleName.trim();
			this.middleInitial = this.middleName.substring(0, 1);
		}

		if (lastName == null) {
			this.lastName = "";
		} else {
			//this.lastName = capitalize(lastName.trim().toLowerCase());
			this.lastName = lastName.trim();
		}
	}

	/**
	 * Capitalize the first character of name (cannot be null).
	 * 
	 * @param name
	 * @return name with first character capitalized.
	 * 
	 */
	private String capitalize(String name) {
		if (name.length() == 0) {
			return name;
		}

		final char firstChar = name.charAt(0);
		final char newChar = Character.toTitleCase(firstChar);
		if (firstChar == newChar) {
			return name;
		}

		char[] newChars = new char[name.length()];
		newChars[0] = newChar;
		name.getChars(1, name.length(), newChars, 1);
		return String.valueOf(newChars);
	}

	public boolean firstInitialLastNameMatch(AuthorName name) {
		return firstInitial.equals(name.getFirstInitial()) && lastName.equals(name.getLastName());
	}

	public boolean firstInitialMiddleInitialLastNameMatch(AuthorName name) {
		return firstInitial.equals(name.getFirstInitial()) && middleInitial.equals(name.getMiddleInitial()) && lastName.equals(name.getLastName());
	}

//	public double nameSimilarityScore(AuthorName name) {
//		NameMatchHeuristic[] pattern = match(name);
//		if (pattern[0] == NameMatchHeuristic) {
//			return 100;
//		} else if (StringUtils.equals("332", pattern) || StringUtils.equals("323", pattern)) {
//			return 80;
//		} else if (StringUtils.equals("331", pattern)) {
//			return 60;
//		} else if (StringUtils.equals("330", pattern)) {
//			return 40;
//		} else if (StringUtils.equals("322", pattern)) {
//			return 10;
//		} else {
//			return 0;
//		}
//	}

	public enum NameMatchHeuristic {
		NONE, // two strings do not match
		EMPTY, // both strings have length zero or either one has length zero
		INITIAL, // both strings have length one and they are equal
		WHOLE // both strings are equal
	}

	/**
	 * Perform partial match on part of a name
	 * 0 = different.
	 * 1 = either null,
	 * 2 = either initial, (initial matches)
	 * 3 = same (multiple characters)
	 */
	private NameMatchHeuristic matchNameParts(String x, String y) {
		if (x.length() == 0 || y.length() == 0) {
			return NameMatchHeuristic.EMPTY;
		}
		if ((x.length() == 1 || y.length() == 1) && (x.substring(0, 1).equals(y.substring(0, 1)))) {
			return NameMatchHeuristic.INITIAL;
		}
		if (x.equals(y)) {
			return NameMatchHeuristic.WHOLE;
		}
		return NameMatchHeuristic.NONE;
	}

	/**
	 * Match two names accounting for variants
	 * > 330: last and first match
	 * = 320: bad match on middle name
	 * > 320: last and first initial match
	 */
	private NameMatchHeuristic[] match(AuthorName y) {
		return new NameMatchHeuristic[]
				{
						matchNameParts(getFirstName(), y.getFirstName()),
						matchNameParts(getMiddleName(), y.getMiddleName()),
						matchNameParts(getLastName(), y.getLastName())
				};
	}

	public boolean isFullNameMatch(AuthorName name) {
		if (lastName != null && firstName != null && middleName != null) {
			return firstName.equals(name.getFirstName()) &&
					middleName.equals(name.getMiddleName()) &&
					lastName.equals(name.getLastName());
		} else {
			return false;
		}
	}
	
	public boolean isNameMatch(AuthorName name) {
		if (lastName != null && firstName != null && middleName != null) {
			return firstName.equals(name.getFirstName()) &&
					middleName.equals(name.getMiddleName()) &&
					lastName.equals(name.getLastName());
		} 
		else if (lastName != null && firstName != null && middleName == null) {
			return firstName.equals(name.getFirstName()) &&
					lastName.equals(name.getLastName());
		}
		else {
			return false;
		}
	}

	public boolean checkFirstNameAndMiddleNameNotEmpty() {
		return firstName.length() > 1 || middleName.length() > 1;
	}

	//	public boolean isNameVariant(AuthorName name) {
	//		return nameVariants.contains(name);
	//	}

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
		if (firstName.length() > 1) {
			String firstInitial = firstName.substring(0, 1);
			if (!"target".equals(type)) {
				v.add(new AuthorName(firstInitial, "", last)); // add variant (F, "", Last).
			}
			// Check whether middle name exists. If it does, add the variants with full
			// middle name, (eg: Rita Mary Kukafka), and middle initial (eg:
			// (Rita M Kukafka). Also, add (R Mary Kukafka) and (R M Kukafka).
			String middle = getMiddleName();
			if (middleName.length() > 1) {
				v.add(new AuthorName(firstInitial, middle, last)); // add variant (F, middle, last).
				String middleInitial = middleName.substring(0, 1);
				if (!("target".equals(type) && initials == 2)) { // option "target" and initials 2 not specified.
					v.add(new AuthorName(firstInitial, middleInitial, last)); // add variant (F, M, last).
				}
				v.add(new AuthorName(first, "", last)); // add variant. (first, "", Last).
				v.add(new AuthorName(first, middleInitial, last)); // add variant (first, M, last).
			}
		}
		return v;
	}


	
	public void setFirstName(String firstName) {
		if (firstName == null) throw new IllegalArgumentException("first name should not be null.");
		//this.firstName = capitalize(firstName.trim().toLowerCase());
		this.firstName = firstName.trim();
		this.firstInitial = firstName.length() > 0 ? firstName.substring(0, 1) : "";
	}
	
	public void setMiddleName(String middleName) {
		if (middleName == null) throw new IllegalArgumentException("middle name should not be null.");
		//this.middleName = capitalize(middleName.trim().toLowerCase());
		this.middleName = middleName.trim();
		this.middleInitial = middleName.length() > 0 ? middleName.substring(0, 1) : "";
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

	public void setFirstInitial(String firstInitial) {
		this.firstInitial = firstInitial;
	}

	public void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
	}

	public void setLastName(String lastName) {
		if (lastName == null) throw new IllegalArgumentException("last name should not be null.");
		//this.lastName = capitalize(lastName.trim().toLowerCase());
		this.lastName = lastName.trim();
	}

	@Override
	public String toString() {
		if (middleName != null && middleName.length() > 0) {
			return "{firstName=[" + firstName + "], middleName=[" + middleName + "], lastName=[" + lastName + "]}";
		} else {
			return "{firstName=[" + firstName + "], lastName=[" + lastName + "]}";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstInitial == null) ? 0 : firstInitial.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((middleInitial == null) ? 0 : middleInitial.hashCode());
		result = prime * result + ((middleName == null) ? 0 : middleName.hashCode());
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
		if (firstInitial == null) {
			if (other.firstInitial != null)
				return false;
		} else if (!firstInitial.equals(other.firstInitial))
			return false;
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
		if (middleInitial == null) {
			if (other.middleInitial != null)
				return false;
		} else if (!middleInitial.equals(other.middleInitial))
			return false;
		if (middleName == null) {
			if (other.middleName != null)
				return false;
		} else if (!middleName.equals(other.middleName))
			return false;
		return true;
	}
}
