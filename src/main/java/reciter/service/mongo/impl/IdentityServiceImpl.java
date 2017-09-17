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

import reciter.database.dynamodb.repository.IdentityRepository;
import reciter.database.mongo.model.IdentityMongo;
import reciter.model.identity.Identity;
import reciter.service.IdentityService;

@Service("identityService")
public class IdentityServiceImpl implements IdentityService {

	@Autowired
	private IdentityRepository identityRepository;

	@Override
	public void save(Collection<Identity> identities) {
		List<reciter.database.dynamodb.model.Identity> dbIdentities = new ArrayList<>();
		for (Identity identity : identities) {
            reciter.database.dynamodb.model.Identity dbIdentity =
                    new reciter.database.dynamodb.model.Identity(identity.getUid(), identity);
			dbIdentities.add(dbIdentity);
		}
		identityRepository.save(dbIdentities);
	}

	@Override
	public void save(Identity identity) {
        reciter.database.dynamodb.model.Identity dbIdentity =
                new reciter.database.dynamodb.model.Identity(identity.getUid(), identity);
		identityRepository.save(dbIdentity);
	}

	@Override
	public List<Identity> findByUids(List<String> uids) {
		Iterator<reciter.database.dynamodb.model.Identity> iterator = identityRepository.findAll(uids).iterator();
		List<Identity> identities = new ArrayList<>(uids.size());
		while (iterator.hasNext()) {
			identities.add(iterator.next().getIdentity());
		}
		return identities;
	}

	@Override
	public Identity findByUid(String uid) {
        reciter.database.dynamodb.model.Identity dbIdentity = identityRepository.findOne(uid);
		return dbIdentity.getIdentity();
	}
}
