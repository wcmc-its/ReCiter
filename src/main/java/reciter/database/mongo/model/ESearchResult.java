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
package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import reciter.xml.retriever.pubmed.PubMedQueryResult;

@Document(collection = "esearchresult")
public class ESearchResult {

	@Id
	private String id;
	private String uid;
	private ESearchPmid eSearchPmid;
	private List<PubMedQueryResult> pubMedQueryResults;
	
	public ESearchResult(String uid, ESearchPmid eSearchPmid, List<PubMedQueryResult> pubMedQueryResults) {
		this.uid = uid;
		this.eSearchPmid = eSearchPmid;
		this.pubMedQueryResults = pubMedQueryResults;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public ESearchPmid getESearchPmid() {
		return eSearchPmid;
	}
	public void setESearchPmid(ESearchPmid eSearchPmid) {
		this.eSearchPmid = eSearchPmid;
	}
	public List<PubMedQueryResult> getPubMedQueryResults() {
		return pubMedQueryResults;
	}
	public void setPubMedQueryResults(List<PubMedQueryResult> pubMedQueryResults) {
		this.pubMedQueryResults = pubMedQueryResults;
	}

}
