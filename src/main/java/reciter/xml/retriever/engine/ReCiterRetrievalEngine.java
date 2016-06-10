package reciter.xml.retriever.engine;

import java.io.IOException;
import java.util.List;

import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;

public interface ReCiterRetrievalEngine {

	List<PubMedArticle> retrieve(TargetAuthor targetAuthor) throws IOException;
}
