package reciter.service;

import java.util.List;

import reciter.database.model.IdentityDirectory;

public interface IdentityDirectoryService {

	List<IdentityDirectory> getIdentityDirectoriesByCwid(String cwid);
}
