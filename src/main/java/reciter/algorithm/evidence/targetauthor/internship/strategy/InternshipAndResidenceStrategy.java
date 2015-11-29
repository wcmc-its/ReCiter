package reciter.algorithm.evidence.targetauthor.internship.strategy;

import java.util.ArrayList;
import java.util.List;

import database.dao.IdentityIntershipsResidenciesDao;
import database.dao.impl.IdentityIntershipsResidenciesDaoImpl;
import database.model.IdentityIntershipsResidencies;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleKeywords.Keyword;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.tfidf.Document;
import reciter.tfidf.TfIdf;
import reciter.utils.stemmer.PorterStemmer;
import reciter.utils.stemmer.SnowballStemmer;

public class InternshipAndResidenceStrategy extends AbstractTargetAuthorStrategy{

	// TODO: should use a service class to query db. not dao class.
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		IdentityIntershipsResidenciesDao dao = new IdentityIntershipsResidenciesDaoImpl();
		List<IdentityIntershipsResidencies> list = dao.getIdentityIntershipsResidenciesByCwid(targetAuthor.getCwid());
		double score=0;
		if(list!=null && list.size()>0){
			for(IdentityIntershipsResidencies internshipResidencies : list){
				//  Increase likelihood of a match based on clinical specialties of internships and residencies #105 
				StringBuilder clinicalSpecialities = new StringBuilder();
				//Match against institutions of target author's internships and residencies #106
				for(ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()){
					if(internshipResidencies.getInternship_name1()!=null && internshipResidencies.getInternship_name1().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
					if(internshipResidencies.getInternship_name2()!=null && internshipResidencies.getInternship_name2().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
					if(internshipResidencies.getInternship_name3()!=null && internshipResidencies.getInternship_name3().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
					if(internshipResidencies.getResidency_name1()!=null && internshipResidencies.getResidency_name1().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
					if(internshipResidencies.getResidency_name2()!=null && internshipResidencies.getResidency_name2().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
					if(internshipResidencies.getResidency_name3()!=null &&  internshipResidencies.getResidency_name3().equalsIgnoreCase(author.getAffiliation().getAffiliationName()))score+=1;
				//  Increase likelihood of a match based on clinical specialties of internships and residencies #105 
					if(internshipResidencies.getInternship_specialty1()!=null && !internshipResidencies.getInternship_specialty1().trim().equals("")){
						clinicalSpecialities.append(internshipResidencies.getInternship_specialty1()).append(" ");
						String stemWordStr = stemWord(internshipResidencies.getInternship_specialty1());
						if(!internshipResidencies.getInternship_specialty1().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
					}
					if(internshipResidencies.getInternship_specialty2()!=null && !internshipResidencies.getInternship_specialty2().trim().equals("")){
						clinicalSpecialities.append(internshipResidencies.getInternship_specialty2()).append(" ");
						String stemWordStr = stemWord(internshipResidencies.getInternship_specialty2());
						if(!internshipResidencies.getInternship_specialty2().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
					}
					if(internshipResidencies.getInternship_specialty3()!=null && !internshipResidencies.getInternship_specialty3().trim().equals("")){
						clinicalSpecialities.append(internshipResidencies.getInternship_specialty3()).append(" ");
						String stemWordStr = stemWord(internshipResidencies.getInternship_specialty3());
						if(!internshipResidencies.getInternship_specialty3().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
					}
					if(internshipResidencies.getResidency_specialty1()!=null && !internshipResidencies.getResidency_specialty1().trim().equals("")){
						clinicalSpecialities.append(internshipResidencies.getResidency_specialty1()).append(" ");
						String stemWordStr = stemWord(internshipResidencies.getResidency_specialty1());
						if(!internshipResidencies.getResidency_specialty1().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
					}
					if(internshipResidencies.getResidency_specialty2()!=null && !internshipResidencies.getResidency_specialty2().trim().equals("")){
						clinicalSpecialities.append(internshipResidencies.getResidency_specialty2()).append(" ");
						String stemWordStr = stemWord(internshipResidencies.getResidency_specialty2());
						if(!internshipResidencies.getResidency_specialty2().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
					}
					if(internshipResidencies.getResidency_specialty3()!=null && !internshipResidencies.getResidency_specialty3().trim().equals("")){
						clinicalSpecialities.append(internshipResidencies.getResidency_specialty3()).append(" ");
						String stemWordStr = stemWord(internshipResidencies.getResidency_specialty3());
						if(!internshipResidencies.getResidency_specialty3().equalsIgnoreCase(stemWordStr))clinicalSpecialities.append(stemWordStr).append(" ");
					}
					
				}
			//  Increase likelihood of a match based on clinical specialties of internships and residencies #105 
				if(!clinicalSpecialities.toString().trim().equals("")){
					String finalString = clinicalSpecialities.toString().trim();
					finalString=finalString.replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim();
					Document doc = new Document(finalString);
					List<Document> documents = new ArrayList<Document>();
					int docSize=1;
					doc.setId(docSize);
					++docSize;
					documents.add(doc);
					StringBuilder sb = new StringBuilder();
					for(Keyword k: reCiterArticle.getArticleKeywords().getKeywords()){
						sb.append(k.getKeyword()).append(" ");
					}
					Document articleDoc = new Document(sb.toString().trim().replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim());
					doc.setId(docSize);
					documents.add(articleDoc);
					TfIdf tfidf = new TfIdf(documents);
					tfidf.computeTfIdf();
					double[] vectorValues = tfidf.createVector(doc);
					for(int i=1; i<documents.size();i++){			
						double[] articleVector=tfidf.createVector(documents.get(i));
						score = score + tfidf.cosineSimilarity(vectorValues, articleVector);
					}
				}
			}
		}
		reCiterArticle.setInternshipAndResidenceStrategyScore(score);
		return score;
	}
	
	private String stemWord(String word){
		SnowballStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent();
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}

}
