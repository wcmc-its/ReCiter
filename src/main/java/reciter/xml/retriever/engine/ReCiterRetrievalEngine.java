package reciter.xml.retriever.engine;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import reciter.model.identity.Identity;

public interface ReCiterRetrievalEngine {

	void retrieve(Identity identity) throws IOException;

	void retrieveArticlesByDateRange(Identity identity, LocalDate startDate, LocalDate endDate) throws IOException;
	
	void retrieve(List<Identity> identities) throws IOException;
	
	void retrieveArticlesByDateRange(List<Identity> identities, LocalDate startDate, LocalDate endDate) throws IOException;
	
}
