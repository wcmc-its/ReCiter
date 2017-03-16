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
//package reciter.algorithm.evidence.targetauthor.boardcertification.strategy;
//
//import java.util.List;
//
//import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
//import reciter.database.mongo.model.Identity;
//import reciter.model.article.ReCiterArticle;
//import reciter.model.boardcertifications.ReadBoardCertifications;
//
//public class CosineSimilarityStrategy extends AbstractTargetAuthorStrategy {
//
//	@Override
//	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
//		// Leverage data on board certifications to improve phase two matching #45 
//		//List<String> boardCertifications = targetAuthor.getBoardCertifications();
//		ReadBoardCertifications certifications = new ReadBoardCertifications(identity.getCwid());
//		identity.setBoardCertifications(certifications.getBoardCertifications());
//		
//		double score = certifications.getBoardCertificationScoreByClusterArticle(reCiterArticle);
//		reCiterArticle.setBoardCertificationStrategyScore(score);
//		
//		return score;
//	}
//
//	@Override
//	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
//		double sum = 0;
//		for (ReCiterArticle reCiterArticle : reCiterArticles) {
//			sum += executeStrategy(reCiterArticle, identity);
//		}
//		return sum;
//	}
//}
