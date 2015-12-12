package reciter.algorithm.cluster;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.engine.Engine;
import reciter.engine.ReCiterEngineFactory;
import reciter.engine.ReCiterEngineProperty;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.MedlineCitationArticleAuthor;
import xmlparser.pubmed.model.PubmedArticle;

public class ReCiterFetchArticles {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterFetchArticles.class);
	private static final String PROPERTIES_FILE_LOCATION = "src/main/resources/config/reciter.properties";
	private ReCiterEngineFactory reCiterEngineFactory;
	private Engine engine;
	
	public ReCiterFetchArticles() {
		reCiterEngineFactory = new ReCiterEngineFactory();
		ReCiterEngineProperty property = new ReCiterEngineProperty();
		engine = reCiterEngineFactory.getReCiterEngine(property);
	}
	
	public static void main(String[] args) {
		
		TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor("wcb2001");
		
		String targetAuthorEmailAddress = targetAuthor.getEmail();
		String cwid = targetAuthor.getCwid();
		
		String directory;
		Properties p = new Properties();
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(PROPERTIES_FILE_LOCATION);
			p.load(inputStream);
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage(), e);
		}

		directory = p.getProperty("pubmed_email_xml_folder");
		
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher(directory);
		List<PubmedArticle> pubmedArticles = pubmedXmlFetcher.getPubmedArticle(targetAuthorEmailAddress, cwid);
		
		Set<String> nameVariants = new HashSet<String>();
		
		nameVariants.add(targetAuthor.getAuthorName().pubmedFormat());
		
		List<AuthorName> aliasList = targetAuthor.getAliasList();
		for (AuthorName authorName : aliasList) {
			nameVariants.add(authorName.pubmedFormat());
		}
		
		for (PubmedArticle pubmedArticle : pubmedArticles) {
			for (MedlineCitationArticleAuthor author : pubmedArticle.getMedlineCitation().getArticle().getAuthorList())
				if (StringUtils.equalsIgnoreCase(targetAuthor.getAuthorName().getLastName(), 
						author.getLastName())) {
					
					String foreName = author.getForeName();
					String lastName = author.getLastName();
					nameVariants.add(lastName + " " + foreName.substring(0, 1));
			}
		}
		System.out.println(nameVariants);
		
		long startTime = System.currentTimeMillis();
		ReCiterFetchArticles example = new ReCiterFetchArticles();
		example.engine.run(cwid);
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");
	}
}
