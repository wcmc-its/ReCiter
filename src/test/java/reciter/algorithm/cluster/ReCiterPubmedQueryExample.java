package reciter.algorithm.cluster;

import java.util.List;

import reciter.database.dao.impl.IdentityDaoImpl;
import reciter.engine.ReCiterEngineProperty;

public class ReCiterPubmedQueryExample {

	public static void main(String[] args) {
		ReCiterEngineProperty property = new ReCiterEngineProperty();
		List<String> cwids = property.getCwids();
		for (String cwid : cwids) {
			IdentityDaoImpl i = new IdentityDaoImpl();
			String query = i.getPubmedQuery(cwid);
			System.out.println(query);
		}
	}
}
