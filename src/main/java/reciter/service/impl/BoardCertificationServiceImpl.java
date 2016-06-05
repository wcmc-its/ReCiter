package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dao.BoardCertificationDao;
import reciter.service.BoardCertificationService;

@Service("boardCertificationService")
public class BoardCertificationServiceImpl implements BoardCertificationService {

	@Autowired
	private BoardCertificationDao boardCertificationDao;
	
	@Override
	public List<String> getBoardCertificationsByCwid(String cwid) {
		return boardCertificationDao.getBoardCertificationsByCwid(cwid);
	}
}
