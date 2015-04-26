package test.database;

import main.database.dao.Identity;
import main.database.dao.IdentityDao;

public class IdentityDaoTest {


	public static void main(String[] args) {
		IdentityDao identityDao = new IdentityDao();
		Identity identity = identityDao.getIdentityByCwid("aaledo");
		System.out.println(identity.getCwid());
		System.out.println(identity.getFirstName());
		System.out.println(identity.getLastName());
		System.out.println(identity.getMiddleName());
		System.out.println(identity.getTitle());
		System.out.println(identity.getPrimaryDepartment());
		System.out.println(identity.getPrimaryAffiliation());
	}
}
