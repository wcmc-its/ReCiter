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
package reciter.algorithm.evidence.targetauthor.education.strategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.algorithm.util.ReCiterStringUtil;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;
import reciter.model.scopus.ScopusArticle;

public class EducationStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		int numWordsOverlap = 0;
		ScopusArticle scopusArticle = reCiterArticle.getScopusArticle();
		if (scopusArticle != null) {
			numWordsOverlap += numWordsOverlapByEducationFromScopus(scopusArticle, identity);
		}
		if (reCiterArticle.getArticleCoAuthors() != null) {
			numWordsOverlap += numWordsOverlapByEducationFromPubmed(reCiterArticle.getArticleCoAuthors().getAuthors(), identity);
		}
		reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[Education number of overlapping words=" + numWordsOverlap + "]");
		reCiterArticle.setEducationScore(numWordsOverlap);
		return numWordsOverlap;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		int numWordsOverlap = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles)
			numWordsOverlap += executeStrategy(reCiterArticle, identity);
		return numWordsOverlap;
	}
	
	private int numWordsOverlapByEducationFromPubmed(List<ReCiterAuthor> authors, Identity identity) {
		int numWordsOverlap = 0;
		for (ReCiterAuthor author : authors) {
			String lastName = author.getAuthorName().getLastName();
			if (lastName != null) {
				if (StringUtils.equalsIgnoreCase(identity.getPrimaryName().getLastName(), lastName)) {
					String authorEducationConcatenation = null;
//					if (identity.getEducations() != null) {
//						authorEducationConcatenation = concatenateAuthorEducationFields(identity.getEducations());
//					}
					String affiliation = author.getAffiliation();
					if (affiliation != null && authorEducationConcatenation != null) {
						numWordsOverlap += ReCiterStringUtil.computeNumberOfOverlapTokens(authorEducationConcatenation, affiliation);
					}
				}
			}
		}
		return numWordsOverlap;
	}
	
	private int numWordsOverlapByEducationFromScopus(ScopusArticle scopusArticle, Identity identity) {
		int numWordsOverlap = 0;
//		if (scopusArticle != null) {
//			for (Entry<Long, Author> entry : scopusArticle.getAuthors().entrySet()) {
//				boolean isNameMatch = entry.getValue().getSurname().equals(targetAuthor.getAuthorName().getLastName());
//				if (isNameMatch) {
//					if (scopusArticle.getAffiliationMap() != null && 
//						scopusArticle.getAffiliationMap().get(entry.getKey()) != null) {
//						
//						String scopusAffiliation = scopusArticle.getAffiliationMap().get(entry.getKey()).getAffilname();
////						String scopusAffiliationNameVariant = scopusArticle.getAffiliationMap().get(entry.getKey()).getNameVariant();
//						String affiliationCity = scopusArticle.getAffiliationMap().get(entry.getKey()).getAffiliationCity();
//						String affiliationCountry = scopusArticle.getAffiliationMap().get(entry.getKey()).getAffiliationCountry();
//						
//						StringBuilder scopusSb = new StringBuilder();
//						if (scopusAffiliation != null) {
//							scopusSb.append(scopusAffiliation);
//							scopusSb.append(" ");
//						}
//						
////						if (scopusAffiliationNameVariant != null) {
////							scopusSb.append(scopusAffiliationNameVariant);
////							scopusSb.append(" ");
////						}
//						
//						if (affiliationCity != null) {
//							scopusSb.append(affiliationCity);
//							scopusSb.append(" ");
//						}
//						
//						if (affiliationCountry != null) {
//							scopusSb.append(affiliationCountry);
//							scopusSb.append(" ");
//						}
//						
//						String scopusString = scopusSb.toString();
//						
//						String authorEducationConcatenation = null;
//						if (targetAuthor.getEducations() != null) {
//							authorEducationConcatenation = concatenateAuthorEducationFields(targetAuthor.getEducations());
//						}
//						
//						if (scopusString != null && authorEducationConcatenation != null) {
//							numWordsOverlap += ReCiterStringUtil.computeNumberOfOverlapTokens(scopusString, authorEducationConcatenation);
//						}
//					}
//				}
//			}
//		}
		return numWordsOverlap;
	}
	
//	private String concatenateAuthorEducationFields(List<Education> authorEducations) {
//		String authorEducationConcatenation = null;
//		if (authorEducations != null) {
//			for (Education authorEducation : authorEducations) {
//				String institution = authorEducation.getInstitution();
//				String degreeYear = Integer.toString(authorEducation.getDegreeYear());
//				String degreeField = authorEducation.getDegreeField();
//				String instLoc = authorEducation.getInstLoc();
//				String instAbbr = authorEducation.getInstAbbr();
//				
//				
//				StringBuilder sb = new StringBuilder();
//				if (institution != null) {
//					sb.append(institution);
//					sb.append(" ");
//				}
//				
//				if (degreeYear != null && !"0".equals(degreeYear)) {
//					sb.append(degreeYear);
//					sb.append(" ");
//				}
//				
//				if (degreeField != null) {
//					sb.append(degreeField);
//					sb.append(" ");
//				}
//				
//				if (instLoc != null) {
//					sb.append(instLoc);
//					sb.append(" ");
//				}
//				
//				if (instAbbr != null) {
//					sb.append(instAbbr);
//				}
//				authorEducationConcatenation = sb.toString();
//			}
//		}
//		return authorEducationConcatenation;
//	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}
}