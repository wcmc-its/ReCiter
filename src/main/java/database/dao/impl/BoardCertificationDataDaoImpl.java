package database.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.BoardCertificationDataDao;
/**
 * 
 * @author htadimeti
 *
 */
public class BoardCertificationDataDaoImpl implements BoardCertificationDataDao{
	/**
	 * 
	 * @param cwid
	 * @return
	 * @throws SQLException
	 */
	public Map<String, List<String>> getBoardCertificationsByCwid(String cwid) throws SQLException{
		Connection con = DbConnectionFactory.getConnection();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		Statement st = null;
		ResultSet rs = null;
		try{
			st=con.createStatement();
			rs=st.executeQuery("select cwid,specialty from rc_identity_board_certifications where cwid='"+cwid+"'");
			
			while (rs.next()) {
				List<String> list = null;
				String key = rs.getString(1);
				if(map.containsKey(key))list=map.get(key);
				else list=new ArrayList<String>();
				list.add(rs.getString(2));
				if(!map.containsKey(key))map.put(key, list);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			DbUtil.close(rs);
			DbUtil.close(st);
			DbUtil.close(con);
		}
		
		return map;
	}
}
