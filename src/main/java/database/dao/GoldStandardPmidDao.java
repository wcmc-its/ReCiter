package database.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.model.GoldStandardPmid;

public class GoldStandardPmidDao {
	public List<GoldStandardPmid> getGoldStandardPmidsByCwid(String cwid){
		List<GoldStandardPmid> list = new ArrayList<GoldStandardPmid>();
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		try{
			String query = "select id,cwid,pmid,rejected from  rc_gold_standard_pmids where cwid='"+cwid+"' and rejected='0'";
			pst=con.createStatement();
			rs=pst.executeQuery(query);
			while(rs.next()){
				GoldStandardPmid gsp = new GoldStandardPmid();
				gsp.setId(rs.getInt(1));
				gsp.setCwid(rs.getString(2));
				gsp.setPmid(rs.getString(3));
				gsp.setRejected(rs.getString(4));
				list.add(gsp);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return list;
	}
}
