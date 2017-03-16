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
package reciter.engine.erroranalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class AnalysisReCiterCluster {

	public Map<String, Integer> getTargetAuthorNameCounts(List<ReCiterArticle> list, Identity targetAuthor) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (ReCiterArticle reCiterArticle : list) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				if (author.getAuthorName().getLastName().equalsIgnoreCase(targetAuthor.getPrimaryName().getLastName())) {
					String fullName = author.getAuthorName().getLastName() + " " + 
							author.getAuthorName().getMiddleName() + " " + 
							author.getAuthorName().getFirstName();
					if (map.containsKey(fullName)) {
						int count = map.get(fullName);
						map.put(fullName, ++count);
					} else {
						map.put(fullName, 1);
					}
				}
			}
		}
		return map;
	}
}
