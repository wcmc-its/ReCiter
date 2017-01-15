package reciter.service.mongo.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.database.mongo.model.ReCiterClusterMongo;
import reciter.database.mongo.repository.ReCiterClusterRepository;
import reciter.service.mongo.ReCiterClusterService;

@Service("reCiterClusterService")
public class ReCiterClusterServiceImpl implements ReCiterClusterService {

	@Autowired
	private ReCiterClusterRepository reCiterClusterRepository;
	
	@Override
	public void save(List<ReCiterCluster> reCiterClusters, String uid) {
		ReCiterClusterMongo reCiterClusterMongo = new ReCiterClusterMongo();
		reCiterClusterMongo.setUid(uid);
		reCiterClusterMongo.setReCiterClusters(reCiterClusters);
		reCiterClusterRepository.save(reCiterClusterMongo);
	}

}
