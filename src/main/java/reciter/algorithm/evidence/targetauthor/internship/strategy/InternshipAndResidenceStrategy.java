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
package reciter.algorithm.evidence.targetauthor.internship.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public class InternshipAndResidenceStrategy extends AbstractTargetAuthorStrategy{

	// TODO: should use a service class to query db. not dao class.
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
//		IdentityIntershipsResidenciesDao dao = new IdentityIntershipsResidenciesDaoImpl();
//		List<IdentityIntershipsResidencies> list = dao.getIdentityIntershipsResidenciesByCwid(identity.getCwid());
//		double score=0;
//		if(list!=null && list.size()>0){
//			for(IdentityIntershipsResidencies internshipResidencies : list){
//				//  Increase likelihood of a match based on clinical specialties of internships and residencies #105 
//				StringBuilder clinicalSpecialities = new StringBuilder();
//				//Match against institutions of target author's internships and residencies #106
//				for(ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()){
//					if(internshipResidencies.getInternship_name1()!=null && internshipResidencies.getInternship_name1().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
//					if(internshipResidencies.getInternship_name2()!=null && internshipResidencies.getInternship_name2().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
//					if(internshipResidencies.getInternship_name3()!=null && internshipResidencies.getInternship_name3().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
//					if(internshipResidencies.getResidency_name1()!=null && internshipResidencies.getResidency_name1().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
//					if(internshipResidencies.getResidency_name2()!=null && internshipResidencies.getResidency_name2().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
//					if(internshipResidencies.getResidency_name3()!=null &&  internshipResidencies.getResidency_name3().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
//				//  Increase likelihood of a match based on clinical specialties of internships and residencies #105 
//					if(internshipResidencies.getInternship_specialty1()!=null && !internshipResidencies.getInternship_specialty1().trim().equals("")){
//						clinicalSpecialities.append(internshipResidencies.getInternship_specialty1()).append(" ");
//						String stemWordStr = stemWord(internshipResidencies.getInternship_specialty1());
//						if(!internshipResidencies.getInternship_specialty1().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
//					}
//					if(internshipResidencies.getInternship_specialty2()!=null && !internshipResidencies.getInternship_specialty2().trim().equals("")){
//						clinicalSpecialities.append(internshipResidencies.getInternship_specialty2()).append(" ");
//						String stemWordStr = stemWord(internshipResidencies.getInternship_specialty2());
//						if(!internshipResidencies.getInternship_specialty2().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
//					}
//					if(internshipResidencies.getInternship_specialty3()!=null && !internshipResidencies.getInternship_specialty3().trim().equals("")){
//						clinicalSpecialities.append(internshipResidencies.getInternship_specialty3()).append(" ");
//						String stemWordStr = stemWord(internshipResidencies.getInternship_specialty3());
//						if(!internshipResidencies.getInternship_specialty3().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
//					}
//					if(internshipResidencies.getResidency_specialty1()!=null && !internshipResidencies.getResidency_specialty1().trim().equals("")){
//						clinicalSpecialities.append(internshipResidencies.getResidency_specialty1()).append(" ");
//						String stemWordStr = stemWord(internshipResidencies.getResidency_specialty1());
//						if(!internshipResidencies.getResidency_specialty1().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
//					}
//					if(internshipResidencies.getResidency_specialty2()!=null && !internshipResidencies.getResidency_specialty2().trim().equals("")){
//						clinicalSpecialities.append(internshipResidencies.getResidency_specialty2()).append(" ");
//						String stemWordStr = stemWord(internshipResidencies.getResidency_specialty2());
//						if(!internshipResidencies.getResidency_specialty2().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
//					}
//					if(internshipResidencies.getResidency_specialty3()!=null && !internshipResidencies.getResidency_specialty3().trim().equals("")){
//						clinicalSpecialities.append(internshipResidencies.getResidency_specialty3()).append(" ");
//						String stemWordStr = stemWord(internshipResidencies.getResidency_specialty3());
//						if(!internshipResidencies.getResidency_specialty3().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
//					}
//					
//				}
//			//  Increase likelihood of a match based on clinical specialties of internships and residencies #105 
//				if(!clinicalSpecialities.toString().trim().equals("")){
//					String finalString = clinicalSpecialities.toString().trim();
//					finalString=finalString.replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim();
//					Document doc = new Document(finalString);
//					List<Document> documents = new ArrayList<Document>();
//					int docSize=1;
//					doc.setId(docSize);
//					++docSize;
//					documents.add(doc);
//					StringBuilder sb = new StringBuilder();
//					for(Keyword k: reCiterArticle.getArticleKeywords().getKeywords()){
//						sb.append(k.getKeyword()).append(" ");
//					}
//					Document articleDoc = new Document(sb.toString().trim().replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim());
//					doc.setId(docSize);
//					documents.add(articleDoc);
//					TfIdf tfidf = new TfIdf(documents);
//					tfidf.computeTfIdf();
//					double[] vectorValues = tfidf.createVector(doc);
//					for(int i=1; i<documents.size();i++){			
//						double[] articleVector=tfidf.createVector(documents.get(i));
//						score = score + tfidf.cosineSimilarity(vectorValues, articleVector);
//					}
//				}
//			}
//		}
//		reCiterArticle.setInternshipAndResidenceStrategyScore(score);
//		return score;
		return 0;
	}
	
	private String stemWord(String word){
//		SnowballStemmer stemmer = new PorterStemmer();
//		stemmer.setCurrent(word);
//		stemmer.stem();
//		return stemmer.getCurrent();
		return word;
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
