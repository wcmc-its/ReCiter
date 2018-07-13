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
package reciter.algorithm.evidence.targetauthor.department.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.OrganizationalUnitEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.OrganizationalUnit;

/**
 * 
 * @author jil3004
 *
 */
public class DepartmentStringMatchStrategy extends AbstractTargetAuthorStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(DepartmentStringMatchStrategy.class);

	private String extractedDept;
	private long pmid;
	private int isGoldStandard;
	//	private Set<String> departments = new HashSet<String>();

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		pmid = reCiterArticle.getArticleId();
		isGoldStandard = reCiterArticle.getGoldStandard();

		double score = 0;
		if (reCiterArticle.getArticleCoAuthors() != null && reCiterArticle.getArticleCoAuthors().getAuthors() != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {

				//				boolean isDepartmentMatch = departmentMatchStrict(author, targetAuthor);
				boolean isDepartmentMatch = departmentMatchStrictAndFillInAffiliationIfNotPresent(reCiterArticle.getArticleId(), reCiterArticle.getGoldStandard(),
						reCiterArticle.getArticleCoAuthors().getAuthors(), author, identity);

				boolean isFirstNameInitialMatch = 
						author.getAuthorName().getFirstInitial().equalsIgnoreCase(identity.getPrimaryName().getFirstInitial());

				boolean isFirstNameInitialMatchFromEmailFetched = false;
				if (identity.getAlternateNames() != null) {
					for (AuthorName authorName : identity.getAlternateNames()) {
						if (StringUtils.equalsIgnoreCase(authorName.getFirstInitial(), author.getAuthorName().getFirstInitial()) &&
								StringUtils.equalsIgnoreCase(authorName.getLastName(), author.getAuthorName().getLastName())) {
							isFirstNameInitialMatchFromEmailFetched = true;
							break;
						}
					}
				}

				if ((isDepartmentMatch && isFirstNameInitialMatch) || (isDepartmentMatch && isFirstNameInitialMatchFromEmailFetched)) {
					reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + 
							" [department and first name initial matches: " + extractedDept + 
							", first name initial: " + identity.getPrimaryName().getFirstInitial() + "]");
					slf4jLogger.info("Department and first name initial matches. "
							+ "PMID=[" + pmid + "] - Extracted Deptment From Article=[" + extractedDept + 
							"] Is Gold=[" + isGoldStandard + "]");
					score = 1;
					reCiterArticle.setMatchingDepartment(extractedDept);
					break;
				}
			}
		}
		reCiterArticle.setDepartmentStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			//sum += executeStrategy(reCiterArticle, identity);
			List<OrganizationalUnitEvidence> orgUnitEvidences = new ArrayList<OrganizationalUnitEvidence>(); 
			if (reCiterArticle.getArticleCoAuthors() != null && reCiterArticle.getArticleCoAuthors().getAuthors() != null) {
				for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
					if(author.isTargetAuthor() 
							&& 
							author.getAffiliation() != null) {
						OrganizationalUnitEvidence orgUnitEvidence = new OrganizationalUnitEvidence();
						String articleAffiliation = author.getAffiliation().replaceAll("&", "and").replaceAll("Tri-I", "Tri-Institutional").replaceAll("[-,]", "");
						if(identity.getOrganizationalUnits() != null 
								&& 
								identity.getOrganizationalUnits().size() > 0) {
							//This is for department
							for(OrganizationalUnit orgUnit: identity.getOrganizationalUnits()) {
								String identityDepartment = orgUnit.getOrganizationalUnitLabel().replaceAll("&", "and").replaceAll("Tri-I", "Tri-Institutional").replaceAll("[-,]", "");
								if(orgUnit.getOrganizationalUnitType().equals("department")) {
									if(orgUnit.getOrganizationalUnitLabel() != null 
											&& 
											(orgUnit.getOrganizationalUnitLabel().contains("Center")
													||
													orgUnit.getOrganizationalUnitLabel().contains("Program")
													||
													orgUnit.getOrganizationalUnitLabel().contains("Institute")) 
											&& 
											orgUnit.getOrganizationalUnitLabel().length() > 14) {
										if(articleAffiliation.contains(identityDepartment)) {
											//articleAffiliation: "Center for Integrative Medicine, Weill Cornell Medicine, New York, NY, USA."
											//identityDepartment: "Center for Integrative Medicine"
											//departmentMatchingScore: 2
											orgUnitEvidence.setIdentityOrganizationalUnit(orgUnit.getOrganizationalUnitLabel());
											orgUnitEvidence.setArticleAffiliation(author.getAffiliation());
											orgUnitEvidence.setOrganizationalUnitMatchingScore(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitDepartmentMatchingScore());
										}
									} else if(articleAffiliation.contains("Department of " + identityDepartment) 
											|| 
											articleAffiliation.contains("Division of " + identityDepartment)) {
										//articleAffiliation: "Department of Pharmacology, Weill Cornell Medical College. New York, NY 10021, USA. jobuck@med.cornell.edu"
										//identityDepartment: "Pharmacology"
										//departmentMatchingScore: 2
										orgUnitEvidence.setIdentityOrganizationalUnit(orgUnit.getOrganizationalUnitLabel());
										orgUnitEvidence.setArticleAffiliation(author.getAffiliation());
										orgUnitEvidence.setOrganizationalUnitMatchingScore(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitDepartmentMatchingScore());
									}  
									
									
									if(Arrays.asList(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitModifier().trim().split(",")).contains(identityDepartment)) {
										orgUnitEvidence.setOrganizationalUnitModifier(Arrays.asList(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitModifier().trim().split(",")).toString());
										orgUnitEvidence.setOrganizationalUnitModifierScore(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitModifierScore());
									}
								} else {
									if(articleAffiliation.contains("Program in " + identityDepartment) 
											||
											articleAffiliation.contains(identityDepartment + " Program")
											||
											articleAffiliation.contains(identityDepartment + " Graduate Program")) {
										orgUnitEvidence.setIdentityOrganizationalUnit(orgUnit.getOrganizationalUnitLabel());
										orgUnitEvidence.setArticleAffiliation(author.getAffiliation());
										orgUnitEvidence.setOrganizationalUnitMatchingScore(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitProgramMatchingScore());
									}
								}
							}
						}
						if(orgUnitEvidence != null && orgUnitEvidence.getIdentityOrganizationalUnit() != null && orgUnitEvidence.getArticleAffiliation() != null) {
							orgUnitEvidences.add(orgUnitEvidence);
						}
					}
				}
			}
			if(orgUnitEvidences.size() > 0) {
				slf4jLogger.info("Pmid: " + reCiterArticle.getArticleId() + " " + orgUnitEvidences.toString());
			}
		}
		return sum;
	}

	/**
	 * Leverage departmental affiliation string matching for phase two matching.
	 * 
	 * If reCiterAuthor has department information, extract the "department of ***" string and use string comparison
	 * to match to target author's primary department and other department fields. If both party's department match,
	 * return true, else return false.
	 * 
	 * (Github issue: https://github.com/wcmc-its/ReCiter/issues/79)
	 * @return True if the department of the ReCiterAuthor and TargetAuthor match.
	 */
	private boolean departmentMatch(ReCiterAuthor reCiterAuthor, Identity targetAuthor) {

		if (reCiterAuthor.getAffiliation() != null && reCiterAuthor.getAffiliation() != null) {
			String affiliation = reCiterAuthor.getAffiliation();
			String extractedDept = extractDepartment(affiliation);
			List<OrganizationalUnit> targetAuthorDepts = targetAuthor.getOrganizationalUnits();
			for (OrganizationalUnit dept : targetAuthorDepts) {
				if (StringUtils.containsIgnoreCase(extractedDept, dept.getOrganizationalUnitLabel())) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean departmentMatchStrictAndFillInAffiliationIfNotPresent(long pmid, int goldStandard, List<ReCiterAuthor> authors, 
			ReCiterAuthor reCiterAuthor, Identity identity) {

		if (reCiterAuthor.getAffiliation() != null && reCiterAuthor.getAffiliation() != null) {
			String affiliation = reCiterAuthor.getAffiliation();
			extractedDept = extractDepartment(affiliation);
			slf4jLogger.info("Extracted department=[" + extractedDept + "] for author=[" + identity.getUid() + "] in pmid=[" + pmid + "].");
			for (OrganizationalUnit department : identity.getOrganizationalUnits()) {
				if (StringUtils.equalsIgnoreCase(extractedDept, department.getOrganizationalUnitLabel())) {
					return true;
				} else if (StringUtils.containsIgnoreCase(extractedDept, department.getOrganizationalUnitLabel()) && !StringUtils.containsIgnoreCase(extractedDept, "medicine")) {
					// check for substring match - only when the extracted department is not "medicine" because
					// it is too common.
					if (reCiterAuthor.getAuthorName().firstInitialMiddleInitialLastNameMatch(identity.getPrimaryName())) {
						slf4jLogger.info("Extracted department=[" + extractedDept + "] contains identity's department=[" + department + "] "
								+ "for author=[" + identity.getUid() + "] in pmid=[" + pmid + "]. And first initial, middle initial and last names match. "
										+ "gold standard=[" + goldStandard + "]");
						return true;
					}
				}
			}

			//			if (identity.getAlternateDepartmentNames() != null) {
			//				for (String alternateDeptName : identity.getAlternateDepartmentNames()) {
			//					if (StringUtils.equalsIgnoreCase(alternateDeptName, extractedDept)) {
			//						slf4jLogger.info("PMID=[" + pmid + "] - Extracted Deptment From Article=[" + extractedDept + 
			//								"] Alternate Dept Name=[" + alternateDeptName + "] Is Gold=[" + isGoldStandard + "]");
			//						return true;
			//					}
			//				}
			//			}
		} else {
			// get affiliation from one of the other authors.
			for (ReCiterAuthor author : authors) {
				if (author.getAffiliation() != null && author.getAffiliation() != null 
						&& author.getAffiliation().length() > 0) {
					String affiliation = author.getAffiliation();
					extractedDept = extractDepartment(affiliation);

					for (OrganizationalUnit department : identity.getOrganizationalUnits()) {
						if (StringUtils.equalsIgnoreCase(extractedDept, department.getOrganizationalUnitLabel())) {
							return true;
						}
					}

					//					if (identity.getAlternateDepartmentNames() != null) {
					//						for (String alternateDeptName : identity.getAlternateDepartmentNames()) {
					//							if (StringUtils.equalsIgnoreCase(alternateDeptName, extractedDept)) {
					//								slf4jLogger.info("PMID=[" + pmid + "] - Extracted Deptment From Article=[" + extractedDept + 
					//										"] Alternate Dept Name=[" + alternateDeptName + "] Is Gold=[" + isGoldStandard + "]");
					//								return true;
					//							}
					//						}
					//					}
				}
			}
		}
		return false;
	}

	private boolean departmentMatchStrict(ReCiterAuthor reCiterAuthor, Identity identity) {

		// this causes precision to decrease, but increases recall.
		//		if (departments.contains(targetAuthorDept) || departments.contains(targetAuthorOtherDept)) {
		//			return true;
		//		}

		if (reCiterAuthor.getAffiliation() != null && reCiterAuthor.getAffiliation() != null) {
			String affiliation = reCiterAuthor.getAffiliation();
			extractedDept = extractDepartment(affiliation);
			//			if (extractedDept.length() > 0) {
			//				departments.add(extractedDept);
			//			}
			for (OrganizationalUnit department : identity.getOrganizationalUnits()) {
				if (StringUtils.equalsIgnoreCase(extractedDept, department.getOrganizationalUnitLabel())) {
					return true;
				}
			}

			//			if (identity.getAlternateDepartmentNames() != null) {
			//				for (String alternateDeptName : identity.getAlternateDepartmentNames()) {
			//					if (StringUtils.equalsIgnoreCase(alternateDeptName, extractedDept)) {
			//						//						slf4jLogger.info("PMID=[" + pmid + "] - Extracted Deptment From Article=[" + extractedDept + 
			//						//								"] Alternate Dept Name=[" + alternateDeptName + "] Is Gold=[" + isGoldStandard + "]");
			//						return true;
			//					}
			//				}
			//			}
		}
		return false;
	}

	/**
	 * Extract Department information from string of the form "Department of *," or "Department of *.".
	 * 
	 * @param department Department string
	 * @return Department name.
	 */
	private String extractDepartment(String department) {
		final Pattern pattern = Pattern.compile("Department of (.+?)[\\.,]");
		final Matcher matcher = pattern.matcher(department);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		if (reCiterArticle.getArticleCoAuthors() != null && reCiterArticle.getArticleCoAuthors().getAuthors() != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {

				boolean isDepartmentMatch = departmentMatchStrictAndFillInAffiliationIfNotPresent(reCiterArticle.getArticleId(), reCiterArticle.getGoldStandard(),
						reCiterArticle.getArticleCoAuthors().getAuthors(), author, identity);

				boolean isFirstNameInitialMatch = 
						author.getAuthorName().getFirstInitial().equalsIgnoreCase(identity.getPrimaryName().getFirstInitial());

				boolean isFirstNameInitialMatchFromEmailFetched = false;
				if (identity.getAlternateNames() != null) {
					for (AuthorName authorName : identity.getAlternateNames()) {
						if (StringUtils.equalsIgnoreCase(authorName.getFirstInitial(), author.getAuthorName().getFirstInitial()) &&
								StringUtils.equalsIgnoreCase(authorName.getLastName(), author.getAuthorName().getLastName())) {
							isFirstNameInitialMatchFromEmailFetched = true;
							break;
						}
					}
				}

				if ((isDepartmentMatch && isFirstNameInitialMatch) || (isDepartmentMatch && isFirstNameInitialMatchFromEmailFetched)) {
					feature.setDepartmentMatch(1);
					break;
				}
			}
		}
	}
}
