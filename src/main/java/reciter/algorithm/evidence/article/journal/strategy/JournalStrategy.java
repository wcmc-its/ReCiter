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
package reciter.algorithm.evidence.article.journal.strategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class JournalStrategy extends AbstractReCiterArticleStrategy {

	private final Identity identity;

	public JournalStrategy(Identity identity) {
		this.identity = identity;
	}

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		double score = 0;
		boolean isJournalMatch = isJournalMatch(reCiterArticle, otherReCiterArticle);
		if (isJournalMatch) {
			// If the two articles' target author share the same first name, then it's likely that
			// these two articles are written by the same target author.
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				boolean isFirstNameMatch = StringUtils.equalsIgnoreCase(
						author.getAuthorName().getFirstName(), identity.getPrimaryName().getFirstName());

				if (isFirstNameMatch) {
					for (ReCiterAuthor otherAuthor : otherReCiterArticle.getArticleCoAuthors().getAuthors()) {
						boolean isOtherFirstNameMatch = StringUtils.equalsIgnoreCase(
								otherAuthor.getAuthorName().getFirstName(), identity.getPrimaryName().getFirstName());
						
						if (isOtherFirstNameMatch) {
							reCiterArticle.getJournalTitleInfo().append("This article is published in the same journal, \"" + 
									reCiterArticle.getJournal().getJournalTitle() + "\" as the clustered article " +
									otherReCiterArticle.getArticleId());
							score += 1;
						}
					}
				}
			}
		}
		reCiterArticle.setJournalStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, otherReCiterArticle);
		}
		return sum;
	}

	/**
	 * If a candidate article is published in a journal and another article contains that journal, return true. False
	 * otherwise.
	 * 
	 * Github issue: https://github.com/wcmc-its/ReCiter/issues/83
	 */
	private  boolean isJournalMatch(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {

		if (reCiterArticle.getJournal() != null && 
				otherReCiterArticle.getJournal() != null && 
				reCiterArticle.getJournal().getJournalTitle() != null &&
				otherReCiterArticle.getJournal() != null) {
			return reCiterArticle.getJournal().getJournalTitle().equalsIgnoreCase(otherReCiterArticle.getJournal().getJournalTitle());
		}
		return false;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles) {
		// TODO Auto-generated method stub
		return 0;
	}
}
