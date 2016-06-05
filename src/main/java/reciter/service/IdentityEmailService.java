package reciter.service;

import java.util.List;

public interface IdentityEmailService {

	List<String> getEmailAddressesForCwid(String cwid);
}
