package reciter.xml.retriever.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import reciter.engine.ReCiterEngine;
import reciter.model.author.TargetAuthor;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

public class DefaultRetrievalSession extends AbstractRetrievalSession{

	private static final Logger slf4jLogger = LoggerFactory.getLogger(DefaultRetrievalSession.class);
	
	private List<RetrievalStrategy> retrievalStrategies;
	
	public DefaultRetrievalSession() {
		retrievalStrategies = new ArrayList<RetrievalStrategy>();
		
	}
	
	@Override
	public void setLevel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void retrieve(TargetAuthor targetAuthor) {
		for (RetrievalStrategy retrievalStrategy : retrievalStrategies) {
			try {
				retrievalStrategy.retrieve(targetAuthor);
			} catch (IOException | SAXException | ParserConfigurationException e) {
				slf4jLogger.error(e.toString());
			}
		}
	}
}
