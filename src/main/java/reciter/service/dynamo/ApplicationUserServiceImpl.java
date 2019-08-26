package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reciter.database.dynamodb.model.ApplicationUser;
import reciter.database.dynamodb.repository.ApplicationUserRepository;
import reciter.service.ApplicationUserService;

@Slf4j
@Service("ApplicationUserService")
public class ApplicationUserServiceImpl implements ApplicationUserService {
	
	@Autowired
	private ApplicationUserRepository applicationUserRepository;
	
	private String secretsalt = BCrypt.gensalt(10);

	@Override
	public boolean createUser(ApplicationUser appUser) {
		if(appUser.getId() != null
				&& !appUser.getId().isEmpty()
				&& appUser.getUsername() != null
				&& !appUser.getUsername().isEmpty()
				&& appUser.getPassword() != null
				&& !appUser.getPassword().isEmpty()) {
			String password = BCrypt.hashpw(appUser.getPassword(), secretsalt);
			appUser.setPassword(password);
			applicationUserRepository.save(appUser);
			return true;
		}
		return false;
	}

	@Override
	public boolean authenticateUser(ApplicationUser appUser) {
		if(appUser.getId() != null
				&& !appUser.getId().isEmpty()
				&& appUser.getPassword() != null
				&& !appUser.getPassword().isEmpty()) {
			ApplicationUser validUser = applicationUserRepository.findById(appUser.getId().trim()).orElseGet(null);
			if(validUser == null) {
				return false;
			}
			if(validUser != null) {
				if(BCrypt.checkpw(appUser.getPassword(), validUser.getPassword())) {
					return true;
				}
			}
		}
		return false;
	}
	
}
