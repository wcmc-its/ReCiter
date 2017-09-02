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
package reciter.service.mongo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.PubMedArticleMongo;
import reciter.database.mongo.repository.PubMedRepository;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.PubMedService;

@Service("pubMedService")
public class PubMedServiceImpl implements PubMedService {

	@Autowired
	private PubMedRepository pubMedRepository;

	@Override
	public void save(Collection<PubMedArticle> pubMedArticles) {
		List<PubMedArticleMongo> pubMedArticleMongos = new ArrayList<>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			PubMedArticleMongo pubMedArticleMongo = new PubMedArticleMongo();
			pubMedArticleMongo.setId(pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid());
			pubMedArticleMongo.setPubMedArticle(pubMedArticle);
			pubMedArticleMongos.add(pubMedArticleMongo);
		}
		pubMedRepository.save(pubMedArticleMongos);
	}

	@Override
	public List<PubMedArticle> findByPmids(List<Long> pmids) {
		Iterator<PubMedArticleMongo> iterator = pubMedRepository.findAll(pmids).iterator();
		List<PubMedArticle> pubMedArticles = new ArrayList<>(pmids.size());
		while (iterator.hasNext()) {
			pubMedArticles.add(iterator.next().getPubMedArticle());
		}
		return pubMedArticles;
	}

	@Override
	public PubMedArticle findByPmid(long pmid) {
		return pubMedRepository.findOne(pmid).getPubMedArticle();
	}
}
