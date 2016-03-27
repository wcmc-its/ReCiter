package reciter.database.dao;

import java.util.List;

import reciter.database.model.IdentityEarliestStartDate;

public interface IdentityEarliestStartDateDao {
	public List<IdentityEarliestStartDate> getAllIdentityEarliestStartDates();
	public IdentityEarliestStartDate getIdentityEarliestStartDateByCwid(String cwid);
}
