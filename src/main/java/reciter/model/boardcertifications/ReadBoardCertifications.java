package reciter.model.boardcertifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.dao.BoardCertificationDao;
import database.dao.impl.BoardCertificationDaoImpl;
//import reciter.lucene.DocumentVector;
//import reciter.lucene.DocumentVectorType;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleKeywords.Keyword;
import reciter.tfidf.Document;
import reciter.tfidf.TfIdf;

/**
 * @author htadimeti
 */

public class ReadBoardCertifications {
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReadBoardCertifications.class);
	private String cwid;



	public ReadBoardCertifications(String cwid){
		this.cwid=cwid;		
	}

	/*private String preProcessBoardCertifications(String certification){
		if(certification!=null && !certification.trim().equals("")){
			certification=certification.trim();
			certification=certification.replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim();
		}
		return certification;
	}*/

	public List<String> getBoardCertificationsMap(){
		List<String> boardCertifications = new ArrayList<String>();
		BoardCertificationDao dao = new BoardCertificationDaoImpl();
		boardCertifications = dao.getBoardCertificationsByCwid(cwid);
		return boardCertifications;
	}

	/**
	 * 
	 * @param cwid
	 * @return
	 */
	public String getBoardCertifications(Map<String, List<String>> map){
		List<String> list=null;	
		StringBuilder sb = new StringBuilder();
		if(map.containsKey(cwid)){
			list=map.get(cwid);
			List<String> keys = new ArrayList<String>();
			for(String certification: list){
				if(certification!=null && !certification.trim().equals("")){
					certification=certification.trim();
					certification=certification.replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim();
					if(!certification.equals("")){
						String[] s = certification.split(" ");
						for(int i=0;s!=null && i<s.length;i++){
							if(!keys.contains(s[i])){
								keys.add(s[i]);
								sb.append(s[i]).append(" ");
							}
						}
					}
				}
			}
		}
		return sb.toString().trim();
	}


	/**
	 * 
	 * @param cwid
	 * @param article
	 */

	public double getBoardCertifications(String cwid, Map<String, List<String>> map,List<ReCiterArticle> articles){
		double sim=0;
		String str=getBoardCertifications(map);
		if(str!=null && !str.trim().equals("")){
			Document doc = new Document(str);
			List<Document> documents = new ArrayList<Document>();		
			//SparseRealVector docVector = new OpenMapRealVector(vectorValues);

			int docSize=1;
			doc.setId(docSize);
			++docSize;
			documents.add(doc);
			for(ReCiterArticle article: articles){
				StringBuilder sb = new StringBuilder();
				for(Keyword k: article.getArticleKeywords().getKeywords()){
					sb.append(k.getKeyword()).append(" ");
				}
				Document articleDoc = new Document(sb.toString().trim().replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim());
				doc.setId(docSize);
				documents.add(articleDoc);
			}
			TfIdf tfidf = new TfIdf(documents);
			tfidf.computeTfIdf();
			double[] vectorValues = tfidf.createVector(doc);
			for(int i=1; i<documents.size();i++){			
				double[] articleVector=tfidf.createVector(documents.get(i));
				sim = sim + tfidf.cosineSimilarity(vectorValues, articleVector);

			}
		}
		slf4jLogger.info("Board Certifications For CWID("+cwid+") => "+str + " => SIM: "+sim);
		return sim;
	}
}
