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
package reciter.engine;

public class Feature {

	private long pmid;
	private int medCornellEdu;
	private int mailMedCornellEdu;
	private int weillCornellEdu;
	private int nypOrg;
	private int departmentMatch;
	private int numKnownRelationships;
	private int weillCornellAffiliation;
	private int containsWeillCornellFromScopus;
	private int isGoldStandard;
	
	public long getPmid() {
		return pmid;
	}
	public void setPmid(long pmid) {
		this.pmid = pmid;
	}
	public int getMedCornellEdu() {
		return medCornellEdu;
	}
	public void setMedCornellEdu(int medCornellEdu) {
		this.medCornellEdu = medCornellEdu;
	}
	public int getMailMedCornellEdu() {
		return mailMedCornellEdu;
	}
	public void setMailMedCornellEdu(int mailMedCornellEdu) {
		this.mailMedCornellEdu = mailMedCornellEdu;
	}
	public int getWeillCornellEdu() {
		return weillCornellEdu;
	}
	public void setWeillCornellEdu(int weillCornellEdu) {
		this.weillCornellEdu = weillCornellEdu;
	}
	public int getNypOrg() {
		return nypOrg;
	}
	public void setNypOrg(int nypOrg) {
		this.nypOrg = nypOrg;
	}
	public int getDepartmentMatch() {
		return departmentMatch;
	}
	public void setDepartmentMatch(int departmentMatch) {
		this.departmentMatch = departmentMatch;
	}
	public int getNumKnownRelationships() {
		return numKnownRelationships;
	}
	public void setNumKnownRelationships(int numKnownRelationships) {
		this.numKnownRelationships = numKnownRelationships;
	}
	public int getWeillCornellAffiliation() {
		return weillCornellAffiliation;
	}
	public void setWeillCornellAffiliation(int weillCornellAffiliation) {
		this.weillCornellAffiliation = weillCornellAffiliation;
	}
	public int getContainsWeillCornellFromScopus() {
		return containsWeillCornellFromScopus;
	}
	public void setContainsWeillCornellFromScopus(int containsWeillCornellFromScopus) {
		this.containsWeillCornellFromScopus = containsWeillCornellFromScopus;
	}
	public int getIsGoldStandard() {
		return isGoldStandard;
	}
	public void setIsGoldStandard(int isGoldStandard) {
		this.isGoldStandard = isGoldStandard;
	}
	
	


}
