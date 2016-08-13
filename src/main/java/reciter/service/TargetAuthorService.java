package reciter.service;

import java.util.List;

import reciter.database.mongo.model.Identity;
import reciter.model.author.TargetAuthor;
import reciter.service.bean.IdentityBean;

public interface TargetAuthorService {

	TargetAuthor getTargetAuthor(String cwid);

	List<IdentityBean> getTargetAuthorByNameOrCwid(String search);

	TargetAuthor convertToTargetAuthor(Identity identity);
}
