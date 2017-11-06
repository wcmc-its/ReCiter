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
package reciter.scopus.xmlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import reciter.model.scopus.Affiliation;
import reciter.model.scopus.Author;
import reciter.model.scopus.ScopusArticle;

/**
 * The {@code ScopusXmlHandler} class parses Scopus XML.
 * 
 * @author jil3004
 *
 */
public class ScopusXmlHandler extends DefaultHandler {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlHandler.class);

	private ScopusArticle scopusArticle;

	private boolean bAffiliation;
	private boolean bAfid;
	private boolean bAffilname;
//	private boolean bNameVariant;
	private boolean bAffiliationCity;
	private boolean bAffiliationCountry;

	private boolean bPubmedId;
	private boolean bDoi;

	private boolean bAuthor;
	private boolean bAuthid;
	private boolean bAuthname;
	private boolean bSurname;
	private boolean bGivenName;
	private boolean bInitials;
	private boolean bAfids;
	private boolean bError;

	private int afid;
	private String affilname;
//	private String nameVariant;
	private String affiliationCity;
	private String affiliationCountry;
	private Map<Integer, Affiliation> affiliations = new HashMap<>();

	private long pubmedId;
	private String doi;
	
	private int seq;
	private long authid;
	private String authname;
	private String surname;
	private String givenName;
	private String initials;
	private List<Integer> afids;
	private Map<Long, Author> authors = new HashMap<>();
	
	private List<ScopusArticle> scopusArticles = new ArrayList<>();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("prism:doi")) {
			bDoi = true;
		}
		// <affiliation>
		if (qName.equalsIgnoreCase("affiliation")) {
			bAffiliation = true;
		}
		if (bAffiliation) {
			if (qName.equalsIgnoreCase("afid")) {
				bAfid = true;
			}
			if (qName.equalsIgnoreCase("affilname")) {
				bAffilname = true;
			}
//			if (qName.equalsIgnoreCase("name-variant")) {
//				bNameVariant = true;
//			}
			if (qName.equalsIgnoreCase("affiliation-city")) {
				bAffiliationCity = true;
			}
			if (qName.equalsIgnoreCase("affiliation-country")) {
				bAffiliationCountry = true;
			}
		}
		// end </affiliation> tag.

		// <pubmed-id>
		if (qName.equalsIgnoreCase("pubmed-id")) {
			bPubmedId = true;
		}
		// end </pubmed-id> tag.

		// <author>
		if (qName.equalsIgnoreCase("author")) {
			bAuthor = true;
			seq = Integer.parseInt(attributes.getValue("seq"));
			afids = new ArrayList<>();
		}
		if (bAuthor) {
			if (qName.equalsIgnoreCase("authid")) {
				bAuthid = true;
			}
			if (qName.equalsIgnoreCase("authname")) {
				bAuthname = true;
			}
			if (qName.equalsIgnoreCase("surname")) {
				bSurname = true;
			}
			if (qName.equalsIgnoreCase("given-name")) {
				bGivenName = true;
			}
			if (qName.equalsIgnoreCase("initials")) {
				bInitials = true;
			}
			if (qName.equalsIgnoreCase("afid")) {
				bAfids = true;
			}
		}
		// end </author> tag.

		// <error>
		if (qName.equalsIgnoreCase("error")) {
			bError = true;
		}
		// end </error> tag.
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (bDoi) {
			doi = new String(ch, start, length);
		}
		if (bAffiliation) {
			if (bAfid) {
				afid = Integer.parseInt(new String(ch, start, length));
			}
			if (bAffilname) {
				affilname = new String(ch, start, length);
			}
//			if (bNameVariant) {
//				nameVariant = new String(ch, start, length);
//			}
			if (bAffiliationCity) {
				affiliationCity = new String(ch, start, length);
			}
			if (bAffiliationCountry) {
				affiliationCountry = new String(ch, start, length);
			}
		}

		if (bPubmedId) {
			pubmedId = Long.parseLong(new String(ch, start, length));
		}

		if (bAuthor) {
			if (bAuthid) {
				authid = Long.parseLong(new String(ch, start, length));
			}
			if (bAuthname) {
				authname = new String(ch, start, length);
			}
			if (bSurname) {
				surname = new String(ch, start, length);
			}
			if (bGivenName) {
				givenName = new String(ch, start, length);
			}
			if (bInitials) {
				initials = new String(ch, start, length);
			}
			if (bAfids) {
				int afid = Integer.parseInt(new String(ch, start, length));
				if (afid != 0) {
					afids.add(afid);
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("prism:doi")) {
			bDoi = false;
		}
		if (qName.equalsIgnoreCase("affiliation") && bAffiliation) {
			if (afid != 0) {
				affiliations.put(afid,
						Affiliation.builder()
								.affiliationCity(affiliationCity)
								.afid(afid)
								.affilname(affilname)
								.affiliationCountry(affiliationCountry)
								.build());
			}
			bAffiliation = false;
		}

		// <affiliation> child tags need to be checked for empty contents.
		// Check for empty XML tags: ie: <afid />
		if (bAffiliation) {
			if (qName.equalsIgnoreCase("afid")) {
				bAfid = false;
			}
			if (qName.equalsIgnoreCase("affilname")) {
				bAffilname = false;
			}
//			if (qName.equalsIgnoreCase("name-variant")) {
//				bNameVariant = false;
//			}
			if (qName.equalsIgnoreCase("affiliation-city")) {
				bAffiliationCity = false;
			}
			if (qName.equalsIgnoreCase("affiliation-country")) {
				bAffiliationCountry = false;
			}
		}

		if (qName.equalsIgnoreCase("pubmed-id")) {
			bPubmedId = false;
		}

		if (qName.equalsIgnoreCase("author") && bAuthor) {
			if (authid != 0) {
				authors.put(authid,
						Author.builder()
								.seq(seq)
								.afids(afids)
								.authid(authid)
								.authname(authname)
								.surname(surname)
								.givenName(givenName)
								.initials(initials).build());
			}
			bAuthor = false;
		}

		if (bAuthor) {
			if (qName.equalsIgnoreCase("authid")) {
				bAuthid = false;
			}
			if (qName.equalsIgnoreCase("authname")) {
				bAuthname = false;
			}
			if (qName.equalsIgnoreCase("surname")) {
				bSurname = false;
			}
			if (qName.equalsIgnoreCase("given-name")) {
				bGivenName = false;
			}
			if (qName.equalsIgnoreCase("initials")) {
				bInitials = false;
			}
			if (qName.equalsIgnoreCase("afid")) {
				bAfids = false;
			}
		}

		// Check for error entry. Return null.
		if (qName.equalsIgnoreCase("entry")) {
			if (bError) {
				scopusArticle = null;
			} else {
				List<Affiliation> affiliationList = new ArrayList<Affiliation>();
				List<Author> authorList = new ArrayList<Author>();
				for (Affiliation affiliation : affiliations.values()) {
					affiliationList.add(affiliation);
				}
				for (Author author : authors.values()) {
					authorList.add(author);
				}
				scopusArticle = ScopusArticle.builder()
						.pubmedId(pubmedId)
						.affiliations(affiliationList)
						.authors(authorList).build();
				if (doi != null) {
					scopusArticle.setDoi(doi);
					doi = null;
				}
				scopusArticles.add(scopusArticle);
				// TODO refactor
				scopusArticle = null;
				affiliations.clear();
				authors.clear();
			}
		}
	}

	public ScopusArticle getScopusArticle() {
		return scopusArticle;
	}

	public List<ScopusArticle> getScopusArticles() {
		return scopusArticles;
	}
}
