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
//package reciter.algorithm.evidence.targetauthor.affiliation.strategy;
//
//import java.util.List;
//
//import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
//import reciter.database.mongo.model.Identity;
//import reciter.model.article.ReCiterArticle;
//import reciter.model.author.ReCiterAuthor;
//import reciter.service.CoauthorAffiliationsService;
//import reciter.service.bean.CoauthorAffiliationsBean;
//
///**
// * Assign Phase Two score to reflect the extent to which candidate articles 
// * have authors with affiliations that occur frequently with WCMC authors.
// * 
// * https://github.com/wcmc-its/ReCiter/issues/74.
// * @author jil3004
// *
// */
//public class CoauthorAffiliationsStrategy extends AbstractTargetAuthorStrategy {
//
//	private CoauthorAffiliationsService coauthorAffiliationsService;
//	
//	public CoauthorAffiliationsStrategy(CoauthorAffiliationsService coauthorAffiliationsService) {
//		this.coauthorAffiliationsService = coauthorAffiliationsService;
//	}
//	
//	@Override
//	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
//		double averageScore = 0;
//		if (reCiterArticle.getArticleCoAuthors() != null) {
//			List<ReCiterAuthor> authors = reCiterArticle.getArticleCoAuthors().getAuthors();
//			for (ReCiterAuthor author : authors) {
//				if (author.getAffiliation() != null) {
//					String affiliation = author.getAffiliation().getAffiliationName();
//					if (affiliation != null && affiliation.length() > 0) {
//						CoauthorAffiliationsBean coauthorAffiliations = coauthorAffiliationsService.getCoauthorAffiliationsByLabel(affiliation);
//						if (coauthorAffiliations != null && coauthorAffiliations.getScore() != 0) {
//							averageScore += coauthorAffiliations.getScore();
//						}
//					}
//				}
//			}
//		}
//		return averageScore;
//	}
//
//	@Override
//	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//}
