package main.xml.scopus.model;

import java.util.Set;

/**
 * A class model representing a Scopus article author XML tag.
 * @author jil3004
 *
 */
public class Author {
	private final int seq; // <author seq="1"> tag.
	private final long authid; // <authid> tag.
	private final String authname; // <authname> tag.
	private final String surname; // <surname> tag.
	private final String givenName; // <given-name> tag.
	private final String initials; // <initials> tag.
	private final Set<Integer> afids; // <afid> tag. Using a set because duplicates are not allowed.
	
	public Author(int seq, long authid, String authname, String surname,
			String givenName, String initials, Set<Integer> afids) {
		this.seq = seq;
		this.authid = authid;
		this.authname = authname;
		this.surname = surname;
		this.givenName = givenName;
		this.initials = initials;
		this.afids = afids;
	}

	public int getSeq() {
		return seq;
	}

	public long getAuthid() {
		return authid;
	}

	public String getAuthname() {
		return authname;
	}

	public String getSurname() {
		return surname;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getInitials() {
		return initials;
	}

	public Set<Integer> getAfidSet() {
		return afids;
	}
}
