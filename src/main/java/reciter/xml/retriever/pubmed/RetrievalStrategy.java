package reciter.xml.retriever.pubmed;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import reciter.model.author.TargetAuthor;

public interface RetrievalStrategy {

	/**
	 * Retrieve the articles for this author.
	 * 
	 * @param targetAuthor
	 * 
	 * @return <code>True</code> if the retrieval is successful. <code>False</code> otherwise.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	boolean retrieve(TargetAuthor targetAuthor) throws IOException;
	
	/**
	 * Set to <code>true</code> if the retrieval strategy should retrieve the articles even if the number of articles
	 * exceeds the threshold.
	 * @param isRetrieveExceedThreshold
	 */
	void setRetrieveExceedThreshold(boolean isRetrieveExceedThreshold);
}
