package reciter.database.dao;

import java.util.List;

import reciter.database.mongo.model.MeshTerm;

public interface MeshRawCount {

	/**
	 * Get count of this mesh term.
	 * @param mesh
	 * @return
	 */
	long getCount(String mesh);

	List<MeshTerm> getAllMeshTerms();
}
