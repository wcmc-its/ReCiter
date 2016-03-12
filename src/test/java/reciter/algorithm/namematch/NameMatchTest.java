package reciter.algorithm.namematch;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import reciter.algorithm.cluster.ReCiterExampleRunSingle;
import reciter.engine.Engine;
import reciter.engine.ReCiterEngineFactory;
import reciter.engine.ReCiterEngineProperty;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.service.impl.TargetAuthorServiceImpl;
import xmlparser.pubmed.PubmedEFetchHandler;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.translator.ArticleTranslator;

public class NameMatchTest {

	private String location = "src/test/resources/xml/";
	private String fileName = "19648924.xml";

	private ReCiterEngineFactory reCiterEngineFactory;
	private Engine engine;
	
	@Test
	public void test() throws SAXException, IOException, ParserConfigurationException {
		TargetAuthorServiceImpl targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor("wex2004");
		
		File file = new File(location + fileName);
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(is, pubmedXmlHandler);
		
		PubmedArticle article = pubmedXmlHandler.getPubmedArticles().get(0);
		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
		reCiterArticleList.add(ArticleTranslator.translate(article, null));
		
		reCiterEngineFactory = new ReCiterEngineFactory();
		ReCiterEngineProperty property = new ReCiterEngineProperty();
		engine = reCiterEngineFactory.getReCiterEngine(property);
		
		engine.run(targetAuthor, reCiterArticleList);
	}
}
