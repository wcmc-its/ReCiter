package reciter.algorithm.evidence.targetauthor.feedback.journal.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReciterFeedbackJournal;
import reciter.model.identity.Identity;

public class JournalFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	
	Map<String, List<ReciterFeedbackJournal>> feedbackJournalsMap =  new HashMap<>();
	
	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		
		return 0.0;
		
	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		
			System.out.println("reCiterArticles size: " + reCiterArticles.size());
			
			for(ReCiterArticle article : reCiterArticles)
			{
				int countAccepted = 0;
				int countRejected = 0;
				int countNull = 0;
				double scoreAll=0.0;
				double scoreWithout1Accepted=0.0;
				double scoreWithout1Rejected=0.0;
				if(feedbackJournalsMap!=null && !feedbackJournalsMap.containsKey(article.getJournal().getJournalTitle()))
				{		
					for(ReCiterArticle innerArticle : reCiterArticles)
					{
						
						if(article.getJournal()!=null && article.getJournal().getJournalTitle()!=null
								&& innerArticle.getJournal()!=null && innerArticle.getJournal().getJournalTitle()!=null
								&& article.getJournal().getJournalTitle().equalsIgnoreCase(innerArticle.getJournal().getJournalTitle())
											&&	article.getGoldStandard() ==1 && innerArticle.getGoldStandard() == 1)
						{
								countAccepted = countAccepted + 1;
						}
						else if(article.getJournal()!=null && article.getJournal().getJournalTitle()!=null
								&& innerArticle.getJournal()!=null && innerArticle.getJournal().getJournalTitle()!=null
								&& article.getJournal().getJournalTitle().equalsIgnoreCase(innerArticle.getJournal().getJournalTitle())
											&&	article.getGoldStandard() ==-1 && innerArticle.getGoldStandard() == -1)
						{
								countRejected = countRejected + 1;
						}
					}
					
					scoreAll = (1 / (1 + Math.exp(- (countAccepted - countRejected) / (Math.sqrt(countAccepted + countRejected) + 1)))) - 0.5;
			        scoreWithout1Accepted = 
			                (1 / (1 + Math.exp(- ((countAccepted > 0 ? countAccepted - 1 : countAccepted) - countRejected) / (Math.sqrt((countAccepted > 0 ? countAccepted - 1 : countAccepted) + countRejected) + 1)))) - 0.5;
			        scoreWithout1Rejected = 
			                (1 / (1 + Math.exp(- (countAccepted - (countRejected > 0 ? countRejected - 1 : countRejected)) / (Math.sqrt(countAccepted + (countRejected > 0 ? countRejected - 1 : countRejected)) + 1)))) - 0.5;
			        
					if(feedbackJournalsMap!=null && feedbackJournalsMap.containsKey(article.getJournal().getJournalTitle()))
					{
						List<ReciterFeedbackJournal> listofJournals = feedbackJournalsMap.get(article.getJournal().getJournalTitle());
						ReciterFeedbackJournal feedbackJournal = new ReciterFeedbackJournal();
						feedbackJournal.setPersonIdentifier(identity.getUid());
						feedbackJournal.setArticleId(article.getArticleId());
						feedbackJournal.setJournalTitle(article.getJournal().getJournalTitle());
						feedbackJournal.setCountAccepted(countAccepted);
						feedbackJournal.setCountRejected(countRejected);
						feedbackJournal.setCountNull(countNull);
						feedbackJournal.setScoreAll(scoreAll);
						feedbackJournal.setScoreWithout1Accepted(scoreWithout1Accepted);
						feedbackJournal.setScoreWithout1Rejected(scoreWithout1Rejected);
						feedbackJournal.setGoldStandard(article.getGoldStandard());
						
						listofJournals.add(feedbackJournal);
					}
					else
					{
						List<ReciterFeedbackJournal> listofJournals = new ArrayList<>();
						ReciterFeedbackJournal feedbackJournal = new ReciterFeedbackJournal();
						feedbackJournal.setPersonIdentifier(identity.getUid());
						feedbackJournal.setArticleId(article.getArticleId());
						feedbackJournal.setJournalTitle(article.getJournal().getJournalTitle());
						feedbackJournal.setCountAccepted(countAccepted);
						feedbackJournal.setCountRejected(countRejected);
						feedbackJournal.setCountNull(countNull);
						feedbackJournal.setScoreAll(scoreAll);
						feedbackJournal.setScoreWithout1Accepted(scoreWithout1Accepted);
						feedbackJournal.setScoreWithout1Rejected(scoreWithout1Rejected);
						feedbackJournal.setGoldStandard(article.getGoldStandard());
						listofJournals.add(feedbackJournal);
						feedbackJournalsMap.put(article.getJournal().getJournalTitle(),listofJournals);
						
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
			/*for (Map.Entry<String, List<ReciterFeedbackJournal>> entry : feedbackJournalsMap.entrySet()) 
			{
				 String key = entry.getKey();
				
				 List<ReciterFeedbackJournal> feedbacklistOfArticles = entry.getValue();
				
				 for(ReciterFeedbackJournal feedbackArticle : feedbacklistOfArticles)
				 {	
					for(ReCiterArticle article : reCiterArticles)
					{
						if(feedbackArticle!=null && feedbackArticle.getJournalTitle().equalsIgnoreCase(article.getJournal().getJournalTitle()) 
							 && feedbackArticle.getGoldStandard() == article.getGoldStandard() && feedbackArticle.getGoldStandard()==1 && article.getGoldStandard() ==1)
						{
								 article.setFeedbackScoreJournal(feedbackArticle.getScoreWithout1Accepted());
						}
						else if(feedbackArticle!=null && feedbackArticle.getJournalTitle().equalsIgnoreCase(article.getJournal().getJournalTitle()) 
								 && feedbackArticle.getGoldStandard() == article.getGoldStandard() && feedbackArticle.getGoldStandard()==-1 && article.getGoldStandard() ==-1)
						{
								article.setFeedbackScoreJournal(feedbackArticle.getScoreWithout1Rejected());
						}
						else if(feedbackArticle!=null && feedbackArticle.getJournalTitle().equalsIgnoreCase(article.getJournal().getJournalTitle()) 
								 && feedbackArticle.getGoldStandard() == article.getGoldStandard() && feedbackArticle.getGoldStandard()==0 && article.getGoldStandard() ==0)
						{
							article.setFeedbackScoreJournal(feedbackArticle.getScoreAll());
						}
					}
				 }
				
			}*/
			//Prior Java 8
			//printing reciter article PMIDs and User Assertion and score
			//Collections.sort(reCiterArticles, (jrt1, jrt2) -> jrt1.getJournal().getJournalTitle().compareToIgnoreCase(jrt2.getJournal().getJournalTitle()));
			//reCiterArticles.forEach(article -> System.out.println("Prior JAVA 8 : "+ "PMID: "+article.getArticleId() + "User Assertion: "+ article.getGoldStandard() +"journal Title:"+article.getJournal().getJournalTitle() +"Feedback Journal Score: " + article.getFeedbackScoreJournal()));			

			
			
			
			feedbackJournalsMap.forEach((key, feedbacklistOfArticles) -> {
	            feedbacklistOfArticles.forEach(feedbackArticle -> {
	                reCiterArticles.stream()
	                        .filter(article -> feedbackArticle != null &&
	                                feedbackArticle.getJournalTitle().equalsIgnoreCase(article.getJournal().getJournalTitle()) &&
	                                feedbackArticle.getGoldStandard() == article.getGoldStandard() &&
	                                feedbackArticle.getGoldStandard() == 1 && article.getGoldStandard() == 1)
	                        .forEach(article -> article.setFeedbackScoreJournal(feedbackArticle.getScoreWithout1Accepted()));

	                reCiterArticles.stream()
	                        .filter(article -> feedbackArticle != null &&
	                                feedbackArticle.getJournalTitle().equalsIgnoreCase(article.getJournal().getJournalTitle()) &&
	                                feedbackArticle.getGoldStandard() == article.getGoldStandard() &&
	                                feedbackArticle.getGoldStandard() == -1 && article.getGoldStandard() == -1)
	                        .forEach(article -> article.setFeedbackScoreJournal(feedbackArticle.getScoreWithout1Rejected()));

	                reCiterArticles.stream()
	                        .filter(article -> feedbackArticle != null &&
	                                feedbackArticle.getJournalTitle().equalsIgnoreCase(article.getJournal().getJournalTitle()) &&
	                                feedbackArticle.getGoldStandard() == article.getGoldStandard() &&
	                                feedbackArticle.getGoldStandard() == 0 && article.getGoldStandard() == 0)
	                        .forEach(article -> article.setFeedbackScoreJournal(feedbackArticle.getScoreAll()));
	            });
	        });

	        // Sorting using Comparator.comparing
	        Collections.sort(reCiterArticles, (jrt1, jrt2) -> jrt1.getJournal().getJournalTitle().compareToIgnoreCase(jrt2.getJournal().getJournalTitle()));

	        // Printing using forEach
	        reCiterArticles.forEach(article -> System.out.println("PersonIdentifier: %s" + identity.getUid() + "PMID:%s " + article.getArticleId() + " User Assertion:%s " + article.getGoldStandard() + " Journal Title: %s" + article.getJournal().getJournalTitle() + " Feedback Journal Score: %d\n" + article.getFeedbackScoreJournal()));
	  
		return 0;
	}
}
