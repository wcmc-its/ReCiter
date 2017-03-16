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

import java.util.List;

/**
 * A class model for representing a Scopus XML article.
 * 
 * <p>
 * A ScopusArticle consists of a map of afids to affiliations.
 * 
 * Note:
 * Sometimes scopus articles contains duplicate affiliation information.
 * However, this problem is resolved by using a map to store only distinct affiliation based on afid.
 * </p>
 * 
 * @author jil3004
 *
 */
public class ScopusArticle {
	
	private long pubmedId;
	private String doi;
	private List<Affiliation> affiliations;
	private List<Author> authors;
	
	public ScopusArticle() {}
	
	public ScopusArticle(long pubmedId, List<Affiliation> affiliations, List<Author> authors) {
		this.pubmedId = pubmedId;
		this.affiliations = affiliations;
		this.authors = authors;
	}

	public long getPubmedId() {
		return pubmedId;
	}

	public void setPubmedId(long pubmedId) {
		this.pubmedId = pubmedId;
	}
	
	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public List<Affiliation> getAffiliations() {
		return affiliations;
	}

	public void setAffiliations(List<Affiliation> affiliations) {
		this.affiliations = affiliations;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

}
