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
package reciter.model.scopus;

import java.util.Set;

/**
 * A class model representing a Scopus article author XML tag.
 * @author jil3004
 *
 */
public class Author {
	private int seq; // <author seq="1"> tag.
	private long authid; // <authid> tag.
	private String authname; // <authname> tag.
	private String surname; // <surname> tag.
	private String givenName; // <given-name> tag.
	private String initials; // <initials> tag.
	private Set<Integer> afids; // <afid> tag. Using a set because duplicates are not allowed.
	
	public Author() {}
	
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

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public long getAuthid() {
		return authid;
	}

	public void setAuthid(long authid) {
		this.authid = authid;
	}

	public String getAuthname() {
		return authname;
	}

	public void setAuthname(String authname) {
		this.authname = authname;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public Set<Integer> getAfids() {
		return afids;
	}

	public void setAfids(Set<Integer> afids) {
		this.afids = afids;
	}

}
