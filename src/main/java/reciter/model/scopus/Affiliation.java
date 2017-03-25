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

/**
 * A class model for a Scopus article affiliation XML tag.
 * @author jil3004
 *
 */
public class Affiliation {

	private int afid; // <afid> XML tag.
	private String affilname; // <affilname> XML tag.
	private String affiliationCity; // <affiliation-city> XML tag.
	private String affiliationCountry; // <affiliation-country> XML tag.

	public Affiliation() {}
	
	public Affiliation(int afid, String affilname, String affiliationCity, String affiliationCountry) {
		this.afid = afid;
		this.affilname = affilname;
		this.affiliationCity = affiliationCity;
		this.affiliationCountry = affiliationCountry;
	}

	public int getAfid() {
		return afid;
	}

	public void setAfid(int afid) {
		this.afid = afid;
	}

	public String getAffilname() {
		return affilname;
	}

	public void setAffilname(String affilname) {
		this.affilname = affilname;
	}

	public String getAffiliationCity() {
		return affiliationCity;
	}

	public void setAffiliationCity(String affiliationCity) {
		this.affiliationCity = affiliationCity;
	}

	public String getAffiliationCountry() {
		return affiliationCountry;
	}

	public void setAffiliationCountry(String affiliationCountry) {
		this.affiliationCountry = affiliationCountry;
	}
	
}
