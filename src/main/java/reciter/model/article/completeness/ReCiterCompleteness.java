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
package reciter.model.article.completeness;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;

public class ReCiterCompleteness implements ArticleCompleteness {

	@Override
	public double computeCompleteness(ReCiterArticle article, ReCiterAuthor target) {
		double articleCompleteness = 0;
		int numAuthorFullNames = 0;
		int totalNumberOfAuthors = article.getArticleCoAuthors().getNumberOfAuthors();
		
		if (article.getArticleTitle() != null && article.getArticleTitle().length() != 0) {
			articleCompleteness += 1;
		}
		if (article.getJournal().exist()) {
			articleCompleteness += 1;
		}
		if (article.getArticleKeywords().exist()) {
			articleCompleteness += 1;
		}
		if (article.getArticleCoAuthors().exist()) {
			for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
//				articleCompleteness += author.getAuthorName().nameSimilarityScore(target.getAuthorName());
				if (author.getAuthorName().checkFirstNameAndMiddleNameNotEmpty()) { numAuthorFullNames += 1; }
			}
		}
		// the percentage of full names is greater than 50%.
		if (totalNumberOfAuthors != 0 && 
			(double) numAuthorFullNames / totalNumberOfAuthors > 0.5) {
			articleCompleteness += 20;
		}
		return articleCompleteness;
	}
}
