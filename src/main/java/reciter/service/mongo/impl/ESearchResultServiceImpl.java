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
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import reciter.database.dynamodb.model.ESearchResult;
//import reciter.database.dynamodb.repository.ESearchResultRepository;
//
//import reciter.service.ESearchResultService;
//
////@Service("eSearchResultService")
//public class ESearchResultServiceImpl implements ESearchResultService {
//
//	@Autowired
//	private ESearchResultRepository eSearchResultRepository;
//
//	@Override
//	public void save(ESearchResult eSearchResult) {
//		eSearchResultRepository.save(eSearchResult);
//	}
//
//	@Override
//	public void update(ESearchResult eSearchResult) {
////		eSearchResultRepository.update(eSearchResult.getUid(), eSearchResult.getESearchPmid());
//		throw new UnsupportedOperationException("implement...");
//	}
//
//	@Override
//	public List<ESearchResult> findByUid(String uid) {
////		return eSearchResultRepository.findByUid(uid);
//		throw new UnsupportedOperationException("implement...");
//	}
//
//	@Override
//	public boolean pushESearchResult(ESearchResult eSearchResult) {
////		return eSearchResultRepository.pushESearchResult(eSearchResult);
//		throw new UnsupportedOperationException("implement...");
//	}
//
//	@Override
//	public boolean existByUidAndRetrievalStrategyName(String uid,
//			String eSearchPmidretrievalStrategyName) {
////		return eSearchResultRepository.existByUidAndRetrievalStrategyName(uid, eSearchPmidretrievalStrategyName);
//		throw new UnsupportedOperationException("implement...");
//	}
//
////	@Override
////	public void insertESearchResult(String uid, List<String> pmids) {
////		ESearchResultRepository eSearchResultDao = new ESearchResultDataAccessImpl();
////		Document doc = new Document("uid", uid).append("pmids", pmids);
////		eSearchResultDao.insertESearchResult(doc);
////	}
//
//}
