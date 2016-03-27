package reciter.database.dao;

import java.util.List;

public interface IdentityEmailDao {

	List<String> getEmailAddressesForCwid(String cwid);

}
