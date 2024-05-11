package reciter.algorithm.evidence.targetauthor.feedback.orcid.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterAuthor;
import reciter.model.article.ReciterFeedbackOrcid;
import reciter.model.identity.Identity;

public class OrcidFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	
	Map<String, List<ReciterFeedbackOrcid>> feedbackOrcidMap =  new HashMap<>();
	List<ReCiterAuthor> listOfAuthors = null;
	
	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		
		return 0.0;
		
	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		
			System.out.println("reCiterArticles size: " + reCiterArticles.size());

			
			for(ReCiterArticle article : reCiterArticles)
				{
					ReCiterArticleAuthors coAuthors = article.getArticleCoAuthors();
					listOfAuthors = coAuthors.getAuthors(); 
					
						for(ReCiterAuthor author : listOfAuthors) 
						{
							
							int countAccepted = 0;
							int countRejected = 0;
							int countNull = 0;
							double scoreAll=0.0;
							double scoreWithout1Accepted=0.0;
							double scoreWithout1Rejected=0.0;
		
			 				if(feedbackOrcidMap!=null && !feedbackOrcidMap.containsKey(author.getOrcid()))
							{	
								for(ReCiterAuthor innerAuthor : listOfAuthors)
								{
									
									if(author.getOrcid()!=null && innerAuthor.getOrcid()!=null 	&& author.getOrcid().equalsIgnoreCase(innerAuthor.getOrcid())
											&& article.getGoldStandard() ==1)
									{
											countAccepted = countAccepted + 1;
									}
									else if(author.getOrcid()!=null && innerAuthor.getOrcid()!=null 	&& author.getOrcid().equalsIgnoreCase(innerAuthor.getOrcid())
											&& article.getGoldStandard() == -1)
									{
											countRejected = countRejected + 1;
									}
								}
								
								scoreAll = (1 / (1 + Math.exp(- (countAccepted - countRejected) / (Math.sqrt(countAccepted + countRejected) + 1)))) - 0.5;
						        scoreWithout1Accepted = 
						                (1 / (1 + Math.exp(- ((countAccepted > 0 ? countAccepted - 1 : countAccepted) - countRejected) / (Math.sqrt((countAccepted > 0 ? countAccepted - 1 : countAccepted) + countRejected) + 1)))) - 0.5;
						        scoreWithout1Rejected = 
						                (1 / (1 + Math.exp(- (countAccepted - (countRejected > 0 ? countRejected - 1 : countRejected)) / (Math.sqrt(countAccepted + (countRejected > 0 ? countRejected - 1 : countRejected)) + 1)))) - 0.5;
						        
								if(feedbackOrcidMap!=null && feedbackOrcidMap.containsKey(article.getJournal().getJournalTitle()))
								{
									List<ReciterFeedbackOrcid> listofSameOrcidAuthors = feedbackOrcidMap.get(author.getOrcid());
									ReciterFeedbackOrcid feedbackOrcid = new ReciterFeedbackOrcid();
									feedbackOrcid.setUid(identity.getUid());
									feedbackOrcid.setArticleId(article.getArticleId());
									feedbackOrcid.setOrcid(author.getOrcid());
									feedbackOrcid.setCountAccepted(countAccepted);
									feedbackOrcid.setCountRejected(countRejected);
									feedbackOrcid.setCountNull(countNull);
									feedbackOrcid.setScoreAll(scoreAll);
									feedbackOrcid.setScoreWithout1Accepted(scoreWithout1Accepted);
									feedbackOrcid.setScoreWithout1Rejected(scoreWithout1Rejected);
									feedbackOrcid.setGoldStandard(article.getGoldStandard());
									
									listofSameOrcidAuthors.add(feedbackOrcid);
								}
								else
								{
									List<ReciterFeedbackOrcid> listofJournals = new ArrayList<>();
									ReciterFeedbackOrcid feedbackOrcid = new ReciterFeedbackOrcid();
									feedbackOrcid.setUid(identity.getUid());
									feedbackOrcid.setArticleId(article.getArticleId());
									feedbackOrcid.setOrcid(author.getOrcid());
									feedbackOrcid.setCountAccepted(countAccepted);
									feedbackOrcid.setCountRejected(countRejected);
									feedbackOrcid.setCountNull(countNull);
									feedbackOrcid.setScoreAll(scoreAll);
									feedbackOrcid.setScoreWithout1Accepted(scoreWithout1Accepted);
									feedbackOrcid.setScoreWithout1Rejected(scoreWithout1Rejected);
									feedbackOrcid.setGoldStandard(article.getGoldStandard());
									listofJournals.add(feedbackOrcid);
									feedbackOrcidMap.put(author.getOrcid(),listofJournals);
									
								}
							}
						
						}
				}
			/*feedbackJournalsMap.forEach((key, listOfFeedbackJournals) -> {
	            System.out.println("Key: " + key);
	            // Iterate over each element in the list
	            Collections.sort(listOfFeedbackJournals, (jrt1, jrt2) -> jrt1.getJournalTitle().compareToIgnoreCase(jrt2.getJournalTitle()));
	            listOfFeedbackJournals.forEach(article -> System.out.println("journal Title:"+article.getJournalTitle() +"CountAccepted Score: " + article.getCountAccepted() + ", CountRejected: " + article.getCountRejected()
				 + ",CountNull: " + article.getCountNull() + ",scoreAll:" + article.getScoreAll() 
				 + ",scoreWithout1Accepted:" +article.getScoreWithout1Accepted() + ",ScoreWithout1Rejected:" +  article.getScoreWithout1Rejected()));
	        });*/
			
			//List<ReciterFeedbackJournal> listofJournals =  feedbackJournalsMap.get(identity.getUid());
			
			//Updating the score for all the PMID that has same value and USER Assertion
			//for(ReciterFeedbackJournal feedbackArticle : listofJournals)
			/*for (Map.Entry<String, List<ReciterFeedbackOrcid>> entry : feedbackOrcidMap.entrySet()) 
			{
				 String key = entry.getKey();
				
				 List<ReciterFeedbackOrcid> feedbacklistOfOrcid = entry.getValue();
				
				 for(ReciterFeedbackOrcid feedbackOrcid : feedbacklistOfOrcid)
				 {	
					for(ReCiterArticle article : reCiterArticles)
					{
						ReCiterArticleAuthors coAuthors = article.getArticleCoAuthors();
						List<ReCiterAuthor> listOfAuthors = coAuthors.getAuthors(); 
						for(ReCiterAuthor author : listOfAuthors)
						if(feedbackOrcid!=null && feedbackOrcid.getOrcid().equalsIgnoreCase(author.getOrcid()) 
							 && feedbackOrcid.getGoldStandard() == 1 && article.getGoldStandard() ==1)
						{
								 article.setFeedbackScoreOrcid(feedbackOrcid.getScoreWithout1Accepted());
						}
						else if(feedbackOrcid!=null && feedbackOrcid.getOrcid().equalsIgnoreCase(author.getOrcid()) 
								 && feedbackOrcid.getGoldStandard() == 1 && article.getGoldStandard() == -1)
						{
								article.setFeedbackScoreOrcid(feedbackOrcid.getScoreWithout1Rejected());
						}
						else if(feedbackOrcid!=null && feedbackOrcid.getOrcid().equalsIgnoreCase(author.getOrcid()) 
								 && feedbackOrcid.getGoldStandard() == 0 && article.getGoldStandard() == 0)
						{
							article.setFeedbackScoreJournal(feedbackOrcid.getScoreAll());
						}
					}
				 }
				
			}*/
			//Prior Java 8
			//printing reciter article PMIDs and User Assertion and score
			//Collections.sort(reCiterArticles, (jrt1, jrt2) -> jrt1.getJournal().getJournalTitle().compareToIgnoreCase(jrt2.getJournal().getJournalTitle()));
			//reCiterArticles.forEach(article -> System.out.println("Prior JAVA 8 : "+ "PMID: "+article.getArticleId() + "User Assertion: "+ article.getGoldStandard() +"journal Title:"+article.getJournal().getJournalTitle() +"Feedback Journal Score: " + article.getFeedbackScoreJournal()));			

		
			feedbackOrcidMap.forEach((key, feedbacklistOfOrcid) -> {
			    feedbacklistOfOrcid.forEach(feedbackOrcid -> {
			        reCiterArticles.forEach(article -> {
			            ReCiterArticleAuthors coAuthors = article.getArticleCoAuthors();
			            List<ReCiterAuthor> listOfAuthors = coAuthors.getAuthors();
			            listOfAuthors.forEach(author -> {
			                if (feedbackOrcid != null &&
			                        feedbackOrcid.getOrcid().equalsIgnoreCase(author.getOrcid()) &&
			                        feedbackOrcid.getGoldStandard() == 1 && article.getGoldStandard() == 1) {
			                    article.setFeedbackScoreOrcid(feedbackOrcid.getScoreWithout1Accepted());
			                } else if (feedbackOrcid != null &&
			                        feedbackOrcid.getOrcid().equalsIgnoreCase(author.getOrcid()) &&
			                        feedbackOrcid.getGoldStandard() == 1 && article.getGoldStandard() == -1) {
			                    article.setFeedbackScoreOrcid(feedbackOrcid.getScoreWithout1Rejected());
			                } else if (feedbackOrcid != null &&
			                        feedbackOrcid.getOrcid().equalsIgnoreCase(author.getOrcid()) &&
			                        feedbackOrcid.getGoldStandard() == 0 && article.getGoldStandard() == 0) {
			                    article.setFeedbackScoreJournal(feedbackOrcid.getScoreAll());
			                }
			            });
			        });
			    });
			});

	        // Sorting using Comparator.comparing
	        Collections.sort(listOfAuthors, (jrt1, jrt2) -> jrt1.getOrcid().compareToIgnoreCase(jrt2.getOrcid()));

	        // Printing using forEach
	        reCiterArticles.forEach(article -> System.out.println("PersonIdentifier: %s" + identity.getUid() + "PMID:%s " + article.getArticleId() + " User Assertion:%s " + article.getGoldStandard() + " Orcid : %s" + article.getArticleCoAuthors().getAuthors().get(0).getOrcid() + " Feedback Orcid Score: %d\n" + article.getFeedbackScoreOrcid()));
		
		return 0;
	}
}
