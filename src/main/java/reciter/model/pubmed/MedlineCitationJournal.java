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
package reciter.model.pubmed;

public class MedlineCitationJournal {

	private MedlineCitationJournalISSN issn;
	private MedlineCitationJournalIssue journalIssue;
	private String title;
	private String isoAbbreviation;
	
	public MedlineCitationJournal() {}
	
	public String getJournalTitle() {
		return title;
	}
	public void setJournalTitle(String title) {
		this.title = title;
	}
	public MedlineCitationJournalIssue getJournalIssue() {
		return journalIssue;
	}
	public void setJournalIssue(MedlineCitationJournalIssue journalIssue) {
		this.journalIssue = journalIssue;
	}
	public String getIsoAbbreviation() {
		return isoAbbreviation;
	}
	public void setIsoAbbreviation(String isoAbbreviation) {
		this.isoAbbreviation = isoAbbreviation;
	}
}
