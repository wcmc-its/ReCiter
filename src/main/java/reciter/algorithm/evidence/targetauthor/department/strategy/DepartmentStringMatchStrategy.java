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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.engine.EngineParameters;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.OrganizationalUnitEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.OrganizationalUnit;
import reciter.model.identity.OrganizationalUnit.OrganizationalUnitType;

/**
 * This strategy matches on Organizational Unit recorded in Identity to affiliation string of target author
 * @author Sarbajit Dutta(szd2013)
 * @see <a href ="https://github.com/wcmc-its/ReCiter/issues/229">Organizational Unit Strategy</a>
 *
 */
public class DepartmentStringMatchStrategy extends AbstractTargetAuthorStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(DepartmentStringMatchStrategy.class);
	private final List<String> orgUnitSynonym = Arrays.asList(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitSynonym().trim().split("\\s*,\\s*"));

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
    
        public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
            double sum = 0; // Initialize a sum to accumulate scores, if applicable.
            // Fetch sanitized versions of the identity's organizational units and synonyms.
            Set<OrganizationalUnit> sanitizedIdentityInstitutions = identity.getSanitizedIdentityInstitutions();
            Map<String, List<String>> identityOrgUnitToSynonymMap = identity.getIdentityOrgUnitToSynonymMap();
            // Retrieve the negative match score from the strategy parameters.
            final double negativeMatchScore = ReCiterArticleScorer.strategyParameters.getOrganizationalUnitDepartmentNegativeMatchingScore();
        
            // Iterate over each article associated with the identity.
            for (ReCiterArticle reCiterArticle : reCiterArticles) {
                List<OrganizationalUnitEvidence> orgUnitEvidences = new ArrayList<>();
        
                // Check if the article has co-authors and proceed if not null.
                if (reCiterArticle.getArticleCoAuthors() != null && reCiterArticle.getArticleCoAuthors().getAuthors() != null) {
                    // Loop through each author of the article.
                    for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
                        // Check if the author is the target author and has an affiliation.
                        if(author.isTargetAuthor() && author.getAffiliation() != null) {
                            String synonymOrgUnitLabel = null; // Will hold the synonym label for the organizational unit, if one exists.
                            // Clean the affiliation string for easier matching.
                            String articleAffiliation = author.getAffiliation().replaceAll("&", "and")
                                                                .replaceAll("Tri-I", "Tri-Institutional")
                                                                .replaceAll("[-]", "");
                            // Proceed if the identity has associated organizational units.
                            if(identity.getOrganizationalUnits() != null && identity.getOrganizationalUnits().size() > 0) {
                                // Loop through each of the identity's organizational units.
                                for(OrganizationalUnit orgUnit : sanitizedIdentityInstitutions) {
                                    boolean isOrgUnitMatch = false; // Flag to indicate if a match is found.
                                    OrganizationalUnitEvidence orgUnitEvidence = new OrganizationalUnitEvidence(); // Prepare evidence object.
                                    orgUnitEvidence.setOrganizationalUnitType(orgUnit.getOrganizationalUnitType()); // Set the type of organizational unit.
                                    // Clean the organizational unit label for easier matching.
                                    String identityDepartment = orgUnit.getOrganizationalUnitLabel()
                                                                       .replaceAll("&", "and")
                                                                       .replaceAll("Tri-I", "Tri-Institutional")
                                                                       .replaceAll("[-]", "");
                                    // Check if the organizational unit is of type DEPARTMENT or DIVISION.
                                    if(orgUnit.getOrganizationalUnitType() == OrganizationalUnitType.DEPARTMENT ||
                                       orgUnit.getOrganizationalUnitType() == OrganizationalUnitType.DIVISION) {
                                        // Check various conditions to ensure a valid match.
                                        if(orgUnit.getOrganizationalUnitLabel() != null &&
                                           (StringUtils.containsIgnoreCase(orgUnit.getOrganizationalUnitLabel(), "Center") ||
                                            StringUtils.containsIgnoreCase(orgUnit.getOrganizationalUnitLabel(), "Program") ||
                                            StringUtils.containsIgnoreCase(orgUnit.getOrganizationalUnitLabel(), "Institute")) &&
                                           orgUnit.getOrganizationalUnitLabel().length() > 14) {
                                            // Check if the article's affiliation contains the identity's department after cleaning stop words.
                                            if(StringUtils.containsIgnoreCase(articleAffiliation.replaceAll(EngineParameters.getRegexForStopWords(), ""),
                                                                             identityDepartment.replaceAll(EngineParameters.getRegexForStopWords(), ""))) {
                                                // Check if synonyms exist and find a match.
                                                if(identityOrgUnitToSynonymMap.size() > 0 && identityOrgUnitToSynonymMap.values().stream().anyMatch(synonymOrgUnit -> synonymOrgUnit.contains(identityDepartment))) {
                                                    synonymOrgUnitLabel = identityOrgUnitToSynonymMap.entrySet().stream()
                                                                         .filter(synonymOrgUnit -> synonymOrgUnit.getValue().contains(identityDepartment))
                                                                         .map(Map.Entry::getKey)
                                                                         .findFirst()
                                                                         .orElse(null);
                                                    // If a synonym label is found, set it in the evidence.
                                                    if(synonymOrgUnitLabel != null) {
                                                        orgUnitEvidence.setIdentityOrganizationalUnit(synonymOrgUnitLabel);
                                                    } 
                                                }
                                                // If no synonym is found, use the original organizational unit label.
                                                if(synonymOrgUnitLabel == null) {
                                                    orgUnitEvidence.setIdentityOrganizationalUnit(orgUnit.getOrganizationalUnitLabel());
                                                }
                                                // Set the article's affiliation and the matching score from strategy parameters.
                                                orgUnitEvidence.setArticleAffiliation(author.getAffiliation());
                                                orgUnitEvidence.setOrganizationalUnitMatchingScore(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitDepartmentMatchingScore());
                                                // Indicate that a match has been found.
                                                isOrgUnitMatch = true;
                                            }
                                        } 
                                        // Similar checks for other department-related phrases in the affiliation.
                                        else if(StringUtils.containsIgnoreCase(articleAffiliation, "Department of " + identityDepartment) ||
                                                StringUtils.containsIgnoreCase(articleAffiliation, "Division of " + identityDepartment) ||
                                                StringUtils.containsIgnoreCase(articleAffiliation, "Dept of " + identityDepartment) ||
                                                StringUtils.containsIgnoreCase(articleAffiliation, "Departments of " + identityDepartment) ||
                                                StringUtils.containsIgnoreCase(articleAffiliation, "Divisions of " + identityDepartment) ||
                                                StringUtils.containsIgnoreCase(articleAffiliation, "Depts of " + identityDepartment) ||
                                                StringUtils.containsIgnoreCase(articleAffiliation, identityDepartment + " Department") ||
                                                StringUtils.containsIgnoreCase(articleAffiliation, identityDepartment + " Division") ||
                                                StringUtils.containsIgnoreCase(articleAffiliation, identityDepartment + " Dept")) {
                                            // Similar synonym checking as above
                                            if(identityOrgUnitToSynonymMap.size() > 0 && identityOrgUnitToSynonymMap.entrySet().stream().anyMatch(synonymOrgUnit -> synonymOrgUnit.getValue().contains(identityDepartment))) {
                                                synonymOrgUnitLabel = identityOrgUnitToSynonymMap.entrySet().stream()
                                                                     .filter(synonymOrgUnit -> synonymOrgUnit.getValue().contains(identityDepartment))
                                                                     .map(Map.Entry::getKey)
                                                                     .findFirst()
                                                                     .orElse(null);
                                                if(synonymOrgUnitLabel != null) {
                                                    orgUnitEvidence.setIdentityOrganizationalUnit(synonymOrgUnitLabel);
                                                }
                                            }
                                            if(synonymOrgUnitLabel == null) {
                                                orgUnitEvidence.setIdentityOrganizationalUnit(orgUnit.getOrganizationalUnitLabel());
                                            }
                                            orgUnitEvidence.setArticleAffiliation(author.getAffiliation());
                                            orgUnitEvidence.setOrganizationalUnitMatchingScore(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitDepartmentMatchingScore());
                                            isOrgUnitMatch = true;
                                        }
                                        // Check if the organizational unit modifier matches the identity's department.
                                        if(isOrgUnitMatch && Arrays.asList(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitModifier().trim().split("\\s*\\s*")).contains(identityDepartment)) {
                                            // Synonym checking as above
                                            if(identityOrgUnitToSynonymMap.size() > 0 && identityOrgUnitToSynonymMap.entrySet().stream().anyMatch(synonymOrgUnit -> synonymOrgUnit.getValue().contains(identityDepartment))) {
                                                synonymOrgUnitLabel = identityOrgUnitToSynonymMap.entrySet().stream()
                                                                     .filter(synonymOrgUnit -> synonymOrgUnit.getValue().contains(identityDepartment))
                                                                     .map(Map.Entry::getKey)
                                                                     .findFirst()
                                                                     .orElse(null);
                                                if(synonymOrgUnitLabel != null) {
                                                    orgUnitEvidence.setIdentityOrganizationalUnit(synonymOrgUnitLabel);
                                                }
                                            }
                                            if(synonymOrgUnitLabel == null) {
                                                orgUnitEvidence.setIdentityOrganizationalUnit(orgUnit.getOrganizationalUnitLabel());
                                            }
                                            orgUnitEvidence.setOrganizationalUnitModifier(identityDepartment);
                                            orgUnitEvidence.setOrganizationalUnitModifierScore(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitModifierScore());
                                        }
                                    } 
                                    // Check for other organizational unit types, such as PROGRAM.
                                    else {
                                        if(articleAffiliation.contains("Program in " + identityDepartment) ||
                                           articleAffiliation.contains(identityDepartment + " Program") ||
                                           articleAffiliation.contains(identityDepartment + " Graduate Program")) {
                                            // Synonym checking as above
                                            if(identityOrgUnitToSynonymMap.size() > 0 && identityOrgUnitToSynonymMap.entrySet().stream().anyMatch(synonymOrgUnit -> synonymOrgUnit.getValue().contains(identityDepartment))) {
                                                synonymOrgUnitLabel = identityOrgUnitToSynonymMap.entrySet().stream()
                                                                     .filter(synonymOrgUnit -> synonymOrgUnit.getValue().contains(identityDepartment))
                                                                     .map(Map.Entry::getKey)
                                                                     .findFirst()
                                                                     .orElse(null);
                                                if(synonymOrgUnitLabel != null) {
                                                    orgUnitEvidence.setIdentityOrganizationalUnit(synonymOrgUnitLabel);
                                                }
                                            }
                                            if(synonymOrgUnitLabel == null) {
                                                orgUnitEvidence.setIdentityOrganizationalUnit(orgUnit.getOrganizationalUnitLabel());
                                            }
                                            orgUnitEvidence.setArticleAffiliation(author.getAffiliation());
                                            orgUnitEvidence.setOrganizationalUnitMatchingScore(ReCiterArticleScorer.strategyParameters.getOrganizationalUnitProgramMatchingScore());
                                        }                            }
                                    // If an organizational unit match or modifier is not found, check for a negative match.
                                    boolean hasDepartmentKeywords = articleAffiliation.matches("(?i).*(Department of |Division of |Dept of |Departments of |Divisions of |Depts of ).*");
                                    if(!isOrgUnitMatch && hasDepartmentKeywords && !StringUtils.containsIgnoreCase(articleAffiliation, identityDepartment)) {
                                        // A negative match is found; set the negative match score.
                                        orgUnitEvidence.setIdentityOrganizationalUnit(orgUnit.getOrganizationalUnitLabel());
                                        orgUnitEvidence.setArticleAffiliation(author.getAffiliation());
                                        orgUnitEvidence.setOrganizationalUnitMatchingScore(negativeMatchScore);
                                        isOrgUnitMatch = true;
                                    }
                                    // Add evidence to list if there's a match or a negative match.
                                    if(orgUnitEvidence.getIdentityOrganizationalUnit() != null && orgUnitEvidence.getArticleAffiliation() != null && orgUnitEvidence.getOrganizationalUnitMatchingScore() != 0) {
                                        // Check if the evidence is not already present to avoid duplicates.
                                        if(!orgUnitEvidences.stream().anyMatch(oue -> oue.getIdentityOrganizationalUnit().equals(orgUnit.getOrganizationalUnitLabel()) ||
                                                                                        oue.getIdentityOrganizationalUnit().equals(identityDepartment))) {
                                            orgUnitEvidences.add(orgUnitEvidence);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // After processing all authors, log and set organizational unit evidences for the article.
                if(!orgUnitEvidences.isEmpty()) {
                    slf4jLogger.info("Pmid: " + reCiterArticle.getArticleId() + " " + orgUnitEvidences.toString());
                    // Remove duplicate evidence based on organizational unit matches.
                    if(orgUnitEvidences.size() > 1) {
                        Set<Object> seen = new HashSet<>();
                        orgUnitEvidences.removeIf(orgUnitEvidence -> !seen.add(orgUnitEvidence.getIdentityOrganizationalUnit()));
                    }
                    // Attach the collected evidences to the article.
                    reCiterArticle.setOrganizationalUnitEvidences(orgUnitEvidences);
                }
            }
            // Return the accumulated sum, if it is used.
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
