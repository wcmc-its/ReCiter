package reciter.xml.retriever.engine;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import reciter.model.identity.Identity;

public interface ReCiterRetrievalEngine {

	void retrieveArticlesByDateRange(List<Identity> identities, LocalDate startDate, LocalDate endDate) throws IOException;
	
	void retrieveByPmids(String cwid, List<Long> pmids) throws IOException;
}
