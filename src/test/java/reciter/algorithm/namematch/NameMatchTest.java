package reciter.algorithm.namematch;

import static org.junit.Assert.assertEquals;

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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import reciter.engine.Engine;
import reciter.engine.ReCiterEngineFactory;
import reciter.engine.ReCiterEngineProperty;
import reciter.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.service.impl.TargetAuthorServiceImpl;
import xmlparser.pubmed.PubmedEFetchHandler;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.translator.ArticleTranslator;

/**
 * https://github.com/wcmc-its/ReCiter/issues/133
 * 
 * Example is CWID wex2004 (note that this person is not among the 63 names in the gold standard, so to reproduce this 
 * error it is necessary to add a row to rc_identity based on data in the online directory). For seven of the false 
 * positives the first name is "Weifeng" which is in conflict with the first name that actually corresponds to this 
 * CWID. In cases where first name in publication metadata are in conflict with first name in rc_identity, articles 
 * should not be assigned to the target author.
 * Examples
 * 
 * wex2004 (Weizhen Xu) - 19648924, 19561614, 17570691, 17259987, 16960154, 16301649 (all have the name Weifeng Xu)
 * mil2041 (Minwei Liu) - 23965901 (Minetta C. Liu)
 * xul2005 (Xue Li) - 26628621 (Ying Liu)
 * All of these names are very common, so I would suggest troubleshooting this issue by putting into place a temporary 
 * hack such that it ONLY retrieves records with these PMIDs.
 * 
 * @author Jie
 *
 */
public class NameMatchTest {

	private String location = "src/test/resources/xml/";

	private ReCiterEngineFactory reCiterEngineFactory;
	private Engine engine;

	private static final String[] cwids = {"wex2004", "mil2041", "xul2005"};
	private static final int[] trueNegatives = {6, 1, 1};
	
	@Test
	public void nameMatchTest() throws SAXException, IOException, ParserConfigurationException {
		int i = 0;
		for (String cwid : cwids) {
			test(cwid);
			Analysis analysis = engine.getAnalysis();
			int numTrueNegatives = analysis.getTrueNeg();
			assertEquals(trueNegatives[i], numTrueNegatives); // should expect this number of false negatives.
			i++;
		}
	}
	
	public void test(String cwid) throws SAXException, IOException, ParserConfigurationException {
		TargetAuthorServiceImpl targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);

		File file = new File(location + cwid + ".xml");
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		SAXParserFactory.newInstance().newSAXParser().parse(is, pubmedXmlHandler);

		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
		for (PubmedArticle article : pubmedXmlHandler.getPubmedArticles()) {
			reCiterArticleList.add(ArticleTranslator.translate(article, null));
		}
		reCiterEngineFactory = new ReCiterEngineFactory();
		ReCiterEngineProperty property = new ReCiterEngineProperty();
		engine = reCiterEngineFactory.getReCiterEngine(property);

		engine.run(targetAuthor, reCiterArticleList);
	}
}
