package reciter.xml.retriever.engine;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import reciter.model.identity.Identity;

public interface ReCiterRetrievalEngine {

	Set<Long> retrieve(Identity identity) throws IOException;

	Set<Long> retrieveArticlesByDateRange(Identity identity, LocalDate startDate, LocalDate endDate) throws IOException;
}
