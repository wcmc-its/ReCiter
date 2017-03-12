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

public class MedlineCitationCommentsCorrections {

	private String refType;
	private String refSource;
	private String pmidVersion;
	private String pmid;
	
	public MedlineCitationCommentsCorrections() {}
	
	public String getRefType() {
		return refType;
	}
	public void setRefType(String refType) {
		this.refType = refType;
	}
	public String getRefSource() {
		return refSource;
	}
	public void setRefSource(String refSource) {
		this.refSource = refSource;
	}
	public String getPmidVersion() {
		return pmidVersion;
	}
	public void setPmidVersion(String pmidVersion) {
		this.pmidVersion = pmidVersion;
	}
	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
}
