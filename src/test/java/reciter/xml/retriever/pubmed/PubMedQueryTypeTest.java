/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.xml.retriever.pubmed;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryBuilder;

public class PubMedQueryTypeTest {

	@Test
	public void testBuildPmidsSingle() {
		List<Long> pmids = new ArrayList<>(1);
		pmids.add(1L);
		PubMedQueryBuilder p = new PubMedQueryBuilder(pmids);
		Map<String, Integer> queries = p.buildPmids();
		assertEquals(new Integer(1), queries.get("1[uid]"));
	}
	
	@Test
	public void testBuildPmidsUnderThreshold() {
		List<Long> pmids = new ArrayList<>(3);
		pmids.add(1L);
		pmids.add(2L);
		pmids.add(3L);
		PubMedQueryBuilder p = new PubMedQueryBuilder(pmids);
		Map<String, Integer> queries = p.buildPmids();
		assertEquals(new Integer(3), queries.get("1[uid] OR 2[uid] OR 3[uid]"));
	}
}
