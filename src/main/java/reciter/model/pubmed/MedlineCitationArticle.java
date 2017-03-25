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

import java.util.List;

public class MedlineCitationArticle {

	private enum PubModel {
		PRINT,
		PRINT_ELECTRONIC,
		ELECTRONIC,
		ELECTRONIC_PRINT,
		ELECTRONIC_ECOLLECTION
	};
	
	private PubModel pubModel;
	private MedlineCitationJournal journal;
	private String articleTitle;
	private String pagination;
	private MedlineCitationArticleELocationID eLocationID;
	private MedlineCitationYNEnum authorListCompleteYN;
	private List<MedlineCitationArticleAuthor> authorList;
	private MedlineCitationYNEnum grantListCompleteYN;
	private List<MedlineCitationPublicationType> publicationTypeList;
	private MedlineCitationDate articleDate;
	private MedlineCitationJournalInfo journalInfo;
	private List<MedlineCitationChemical> chemicalList;
	private List<MedlineCitationMeshHeading> meshHeadingList;
	private	MedlineCitationSubset citationSubset;
	private List<MedlineCitationGrant> grantList;
	
	public MedlineCitationArticle() {}
	
	public String getArticleTitle() {
		return articleTitle;
	}
	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	public List<MedlineCitationArticleAuthor> getAuthorList() {
		return authorList;
	}
	public void setAuthorList(List<MedlineCitationArticleAuthor> authorList) {
		this.authorList = authorList;
	}
	public MedlineCitationJournal getJournal() {
		return journal;
	}
	public void setJournal(MedlineCitationJournal journal) {
		this.journal = journal;
	}
	public List<MedlineCitationGrant> getGrantList() {
		return grantList;
	}
	public void setGrantList(List<MedlineCitationGrant> grantList) {
		this.grantList = grantList;
	}
	public MedlineCitationArticleELocationID geteLocationID() {
		return eLocationID;
	}
	public void seteLocationID(MedlineCitationArticleELocationID eLocationID) {
		this.eLocationID = eLocationID;
	}
}
