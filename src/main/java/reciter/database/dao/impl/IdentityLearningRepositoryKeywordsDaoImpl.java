package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.IdentityLearningRepositoryKeywordsDao;
import reciter.database.model.IdentityLearningRepositoryKeywords;

public class IdentityLearningRepositoryKeywordsDaoImpl implements IdentityLearningRepositoryKeywordsDao {

	@Override
	public List<IdentityLearningRepositoryKeywords> getIdentityLearningRepositoryKeywordsByCwid(String cwid) {
		List<IdentityLearningRepositoryKeywords> list = null;
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "select cwid,ontology,keyword from rc_identity_learning_repository_keywords where cwid='"+cwid+"";
		try{
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				if(list==null)list=new ArrayList<IdentityLearningRepositoryKeywords>();
				IdentityLearningRepositoryKeywords keywords = new IdentityLearningRepositoryKeywords();
				keywords.setCwid(rs.getString(1));
				keywords.setOntology(rs.getString(2));
				keywords.setKeyword(rs.getString(3));
				list.add(keywords);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return list;
	}

}
