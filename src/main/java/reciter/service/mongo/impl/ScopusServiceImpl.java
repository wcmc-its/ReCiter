///*******************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *******************************************************************************/
//package reciter.service.mongo.impl;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import reciter.database.dynamodb.repository.ScopusArticleRepository;
//import reciter.model.scopus.ScopusArticle;
//import reciter.service.ScopusService;
//
////@Service("scopusService")
//public class ScopusServiceImpl implements ScopusService {
//
//	@Autowired
//	private ScopusArticleRepository scopusRepository;
//
//	@Override
//	public void save(Collection<ScopusArticle> scopusArticles) {
//		List<reciter.database.dynamodb.model.ScopusArticle> dbScopusArticles = new ArrayList<>();
//		for (ScopusArticle scopusArticle : scopusArticles) {
//			reciter.database.dynamodb.model.ScopusArticle dbScopusArticle = new reciter.database.dynamodb.model.ScopusArticle(
//					scopusArticle.getPubmedId(),
//					scopusArticle
//			);
//			dbScopusArticles.add(dbScopusArticle);
//		}
//		scopusRepository.save(dbScopusArticles);
//	}
//
//	@Override
//	public List<ScopusArticle> findByPmids(List<Long> pmids) {
//		Iterator<reciter.database.dynamodb.model.ScopusArticle> iterator = scopusRepository.findAll(pmids).iterator();
//		List<ScopusArticle> scopusArticles = new ArrayList<>(pmids.size());
//		while (iterator.hasNext()) {
//			scopusArticles.add(iterator.next().getScopusArticle());
//		}
//		return scopusArticles;
//	}
//}
