package reciter.xml.retriever.pubmed;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import reciter.xml.retriever.pubmed.PubMedQuery.PubMedQueryBuilder;

public class PubMedQueryTest {

	@Test
	public void testBuildPmidsSingle() {
		List<Long> pmids = new ArrayList<>(1);
		pmids.add(1L);
		PubMedQueryBuilder p = new PubMedQueryBuilder(pmids);
		List<String> queries = p.buildPmids();
		assertEquals("1[uid]", queries.get(0));
	}
	
	@Test
	public void testBuildPmidsUnderThreshold() {
		List<Long> pmids = new ArrayList<>(3);
		pmids.add(1L);
		pmids.add(2L);
		pmids.add(3L);
		PubMedQueryBuilder p = new PubMedQueryBuilder(pmids);
		List<String> queries = p.buildPmids();
		assertEquals("1[uid] OR 2[uid] OR 3[uid]", queries.get(0));
	}
}
