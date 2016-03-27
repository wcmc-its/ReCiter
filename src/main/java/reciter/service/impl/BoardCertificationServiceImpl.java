package reciter.service.impl;

import java.util.List;

import reciter.database.dao.BoardCertificationDao;
import reciter.database.dao.impl.BoardCertificationDaoImpl;
import reciter.service.BoardCertificationService;

public class BoardCertificationServiceImpl implements BoardCertificationService {

	@Override
	public List<String> getBoardCertificationsByCwid(String cwid) {
		BoardCertificationDao boardCertificationDao = new BoardCertificationDaoImpl();
		return boardCertificationDao.getBoardCertificationsByCwid(cwid);
	}
}
