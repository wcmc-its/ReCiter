package reciter.database.dao;

public interface MeshRawCount {

	/**
	 * Get count of this mesh term.
	 * @param mesh
	 * @return
	 */
	long getCount(String mesh);
}
