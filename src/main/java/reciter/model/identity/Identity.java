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

import java.util.Collections;
import java.util.Date;
import java.util.List;

@DynamoDBDocument
public class Identity {

	private String uid; // uid of the user
	private AuthorName primaryName; // primary name of the user
	private List<AuthorName> alternateNames; // aliases
	private List<String> emails; // list of emails
	private List<KnownRelationship> knownRelationships; // known relationships
	private List<OrganizationalUnit> organizationalUnits; // list of department
	private String title; // title of the person
	private List<String> institutions; // institutions
	private Education degreeYear; // degreeYear
	private List<String> personTypes; // type of person: i.e., academic, academic-faculty, etc...
	private String program; // program the person is in.
	private List<String> boardCertifications; // board certifications
	private String citizenship; // citizenship
	private List<String> grants; // grants
	private List<String> keywords; // keywords
	private List<PubMedAlias> pubMedAlias; // name alias from PubMed
	private Date dateInitialRun; // the date of the first time that ReCiter perform the retrieval
	private Date dateLastRun; // the date of the most recent retrieval
	
	public Identity() {}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public AuthorName getPrimaryName() {
		return primaryName;
	}

	public void setPrimaryName(AuthorName primaryName) {
		this.primaryName = primaryName;
	}

	public List<AuthorName> getAlternateNames() {
		return alternateNames;
	}

	public void setAlternateNames(List<AuthorName> alternateNames) {
		this.alternateNames = alternateNames;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public List<KnownRelationship> getKnownRelationships() {
		return knownRelationships;
	}

	public void setKnownRelationships(List<KnownRelationship> knownRelationships) {
		this.knownRelationships = knownRelationships;
	}

	public List<OrganizationalUnit> getOrganizationalUnits() {
		return organizationalUnits;
	}

	public void setOrganizationalUnits(List<OrganizationalUnit> organizationalUnits) {
		this.organizationalUnits = organizationalUnits;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getInstitutions() {
		return institutions;
	}

	public void setInstitutions(List<String> institutions) {
		this.institutions = institutions;
	}

	public Education getDegreeYear() {
		return degreeYear;
	}

	public void setDegreeYear(Education degreeYear) {
		this.degreeYear = degreeYear;
	}

	public List<String> getPersonTypes() {
		return personTypes;
	}

	public void setPersonTypes(List<String> personTypes) {
		this.personTypes = personTypes;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public List<String> getBoardCertifications() {
		return boardCertifications;
	}

	public void setBoardCertifications(List<String> boardCertifications) {
		this.boardCertifications = boardCertifications;
	}

	public String getCitizenship() {
		return citizenship;
	}

	public void setCitizenship(String citizenship) {
		this.citizenship = citizenship;
	}

	public List<String> getGrants() {
		return grants;
	}

	public void setGrants(List<String> grants) {
		this.grants = grants;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<PubMedAlias> getPubMedAlias() {
		if (pubMedAlias != null) {
			return pubMedAlias;
		} else {
			return Collections.emptyList();
		}
	}

	public void setPubMedAlias(List<PubMedAlias> pubMedAlias) {
		this.pubMedAlias = pubMedAlias;
	}

	public Date getDateInitialRun() {
		return dateInitialRun;
	}

	public void setDateInitialRun(Date dateInitialRun) {
		this.dateInitialRun = dateInitialRun;
	}

	public Date getDateLastRun() {
		return dateLastRun;
	}

	public void setDateLastRun(Date dateLastRun) {
		this.dateLastRun = dateLastRun;
	}
}
