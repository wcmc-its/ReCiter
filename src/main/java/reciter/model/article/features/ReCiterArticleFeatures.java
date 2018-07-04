package reciter.model.article.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import reciter.algorithm.evidence.article.mesh.strategy.MeshMajorStrategy;
import reciter.engine.EngineParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleMeshHeading;
import reciter.model.article.ReCiterAuthor;
import reciter.model.scopus.Author;

/**
 * @author szd2013
 * This class checks for all the features of a articles
 */
@Data
public class ReCiterArticleFeatures {
	
	private String journalName;
	private List<String> coAuthors = new ArrayList<String>();
	private List<String> meshMajor = new ArrayList<String>();
	private Set<Integer> affiliationIds = new HashSet<Integer>();
	private int featureCount = 0;
	

	/**
	 * @param reCiterArticle
	 */
	public ReCiterArticleFeatures(ReCiterArticle reCiterArticle) {
		populateFeatures(reCiterArticle);
	}
	
	private void populateFeatures(ReCiterArticle reCiterArticle) {
		//Journal Feature Name
		if (reCiterArticle.getJournal().exist()) {
			this.journalName = reCiterArticle.getJournal().getJournalTitle();
			this.featureCount++;
		}
		
		//Co-Author Feature Name
		if (reCiterArticle.getArticleCoAuthors().exist()) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				if(author != null && author.getAuthorName() != null &&
						author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getLastName() != null && !author.isTargetAuthor() &&
					(
					(!author.getAuthorName().getFirstInitial().equals("Y") && !author.getAuthorName().getLastName().equals("Wang"))
					||
					(!author.getAuthorName().getFirstInitial().equals("J") && !author.getAuthorName().getLastName().equals("Wang"))	
					||
					(!author.getAuthorName().getFirstInitial().equals("J") && !author.getAuthorName().getLastName().equals("Smith"))
					||
					(!author.getAuthorName().getFirstInitial().equals("S") && !author.getAuthorName().getLastName().equals("Kim"))
					||
					(!author.getAuthorName().getFirstInitial().equals("S") && !author.getAuthorName().getLastName().equals("Lee"))
					||
					(!author.getAuthorName().getFirstInitial().equals("J") && !author.getAuthorName().getLastName().equals("Lee"))
					)
					) {
					this.coAuthors.add(author.getAuthorName().getFirstInitial() + "." + author.getAuthorName().getLastName());
				}
			}
			if(this.coAuthors.size() > 0) {
				this.featureCount = this.featureCount + this.coAuthors.size();
			}
		}
		
		//MeshMajor Feature
		if(reCiterArticle.getMeshHeadings() != null && !reCiterArticle.getMeshHeadings().isEmpty()) {
			for(ReCiterArticleMeshHeading meshHeading: reCiterArticle.getMeshHeadings()) {
				if(MeshMajorStrategy.isMeshMajor(meshHeading) && EngineParameters.getMeshCountMap() != null && EngineParameters.getMeshCountMap().containsKey(meshHeading.getDescriptorName().getDescriptorName()) &&
						EngineParameters.getMeshCountMap().get(meshHeading.getDescriptorName().getDescriptorName()) < 100000L) {
					this.meshMajor.add(meshHeading.getDescriptorName().getDescriptorName());
				}
			}
			if(this.meshMajor.size() > 0) {
				this.featureCount = this.featureCount + this.meshMajor.size();
			}
		}
		
		if(reCiterArticle.getScopusArticle() != null && reCiterArticle.getScopusArticle().getAuthors().size() == reCiterArticle.getArticleCoAuthors().getNumberOfAuthors()) {
			int i = 0;
			for(ReCiterAuthor author: reCiterArticle.getArticleCoAuthors().getAuthors()) {
				/*if(author.isTargetAuthor()) {
					break;
				}*/
				Author scopusAuthor = reCiterArticle.getScopusArticle().getAuthors().get(i);
				if(scopusAuthor != null && scopusAuthor.getAfids() != null) {
					this.affiliationIds.addAll(scopusAuthor.getAfids());
				}
				i++;			
			}
			if(this.affiliationIds.size() > 0) {
				this.featureCount = this.featureCount + 1;
			}
			
		}
		
		
	}

}
