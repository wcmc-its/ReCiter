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
package reciter.algorithm.evidence.targetauthor.affiliation.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public class AffiliationStrategy extends AbstractTargetAuthorStrategy {

	// what issue is this?
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
//		boolean containsAffiliation = containsWeillCornell(reCiterArticle);
//		if (containsAffiliation) {
//			//  Decrease likelihood of institution match if paper was published before target author's start date #104
//			IdentityEarliestStartDateDao dao = new IdentityEarliestStartDateDaoImpl() ;
//			IdentityEarliestStartDate date = dao.getIdentityEarliestStartDateByCwid(targetAuthor.getCwid()); 
//			if(date!=null){
//				DateFormat format = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
//				Date startDate;
//				try {
//					startDate = format.parse(date.getStartDate());
//					if(reCiterArticle.getJournal().getJournalIssuePubDateYear()>=startDate.getYear())return 1;
//					else return 0;
//				} catch (ParseException e) {					
//					e.printStackTrace();
//					return 1;
//				}
//			}else return 1;
//		} else {
//			return 0;
//		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}
}
