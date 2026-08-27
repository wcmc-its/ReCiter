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
package reciter.service;

import java.time.Instant;
import java.util.List;

import reciter.database.dynamodb.model.ESearchResult;

public interface ESearchResultService {

	void save(ESearchResult eSearchResult);

	ESearchResult findByUid(String uid);

	/**
	 * Batch lookup: one ESearchResult per requested uid, in request order, with
	 * {@code null} in place of any uid that has no ESearchResult record.
	 */
	List<ESearchResult> findByUids(List<String> uids);

	void deleteAll();

	void delete(String uid);

	/**
	 * Record that a clean ALL_PUBLICATIONS sweep completed for this uid (#696). The
	 * write is conditional and non-regressing: an existing newer stamp is never moved
	 * backward, so a Publication Manager sweep racing the batch cannot regress it.
	 */
	void stampLastFullSweepIfNewer(String uid, Instant sweepTime);

	/**
	 * The uid's persisted lastFullSweep stamp, or null if the uid has never had a
	 * clean full sweep recorded (callers then fall back to the lookupType inference).
	 */
	Instant findLastFullSweep(String uid);
}
