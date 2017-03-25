package reciter.scopus.callable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import reciter.model.scopus.ScopusArticle;
import reciter.scopus.xmlparser.ScopusXmlHandler;

public class ScopusUriParserCallable implements Callable<List<ScopusArticle>> {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(ScopusUriParserCallable.class);
    
    // Scopus institutions and API key
    private static final String INST_TOKEN = "";
    private static final String API_KEY = "";
    
    private final ScopusXmlHandler xmlHandler;
    private final String uri;
    
    public ScopusUriParserCallable(ScopusXmlHandler xmlHandler, String uri) {
        this.xmlHandler = xmlHandler;
        this.uri = uri;
    }
    
    public List<ScopusArticle> parse(String uri) throws ParserConfigurationException, SAXException, IOException {
        URL url = new URL(uri);
        slf4jLogger.info(url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");
        conn.setRequestProperty("X-ELS-Insttoken", INST_TOKEN);
        conn.setRequestProperty("X-ELS-APIKey", API_KEY);
        
        InputSource source = new InputSource(conn.getInputStream());
        
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(source, xmlHandler);
        List<ScopusArticle> scopusArticles = xmlHandler.getScopusArticles();
        slf4jLogger.info("Number of Scopus article retrieved=[" + scopusArticles.size() + "] for query=[" + uri + "].");
        return scopusArticles;
    }
    
    @Override
    public List<ScopusArticle> call() throws Exception {
        return parse(uri);
    }

}