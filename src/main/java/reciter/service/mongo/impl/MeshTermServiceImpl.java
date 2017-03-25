package reciter.service.mongo.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.MeshTerm;
import reciter.database.mongo.repository.MeshTermRepository;
import reciter.service.mongo.MeshTermService;

@Service("meshTermService")
public class MeshTermServiceImpl implements MeshTermService {

	@Autowired
	private MeshTermRepository meshTermRepository;
	
	@Override
	public void save(List<MeshTerm> meshTerms) {
		meshTermRepository.save(meshTerms);
	}
	
	@Override
	public List<MeshTerm> findAll() {
		return meshTermRepository.findAll();
	}
}
