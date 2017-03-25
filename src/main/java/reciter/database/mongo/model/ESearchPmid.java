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

import java.time.LocalDateTime;
import java.util.List;

public class ESearchPmid {

	private List<Long> pmids;
	private String retrievalStrategyName;
	private LocalDateTime retrievalDate;

	public ESearchPmid(List<Long> pmids, String retrievalStrategyName, LocalDateTime retrievalDate) {
		this.pmids = pmids;
		this.retrievalStrategyName = retrievalStrategyName;
		this.retrievalDate = retrievalDate;
	}
	
	public List<Long> getPmids() {
		return pmids;
	}
	public void setPmids(List<Long> pmids) {
		this.pmids = pmids;
	}
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}
	public void setRetrievalStrategyName(String retrievalStrategyName) {
		this.retrievalStrategyName = retrievalStrategyName;
	}
	public LocalDateTime getRetrievalDate() {
		return retrievalDate;
	}
	public void setRetrievalDate(LocalDateTime retrievalDate) {
		this.retrievalDate = retrievalDate;
	}
}
