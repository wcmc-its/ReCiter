package reciter.database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.GoldStandardPmidsDao;
import reciter.database.model.GoldStandardPmid;

public class GoldStandardPmidsDaoImpl implements GoldStandardPmidsDao {

	/**
	 * Retrieves the gold standard PMIDs for cwid.
	 * @param cwid
	 * @return set of PMIDs.
	 */
	@Override
	public List<String> getPmidsByCwid(String cwid) {
		List<String> pmids = new ArrayList<String>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT pmid from rc_gold_standard_pmids WHERE cwid = '" + cwid + "' and rejected='0'";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				pmids.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return pmids;
	}
	
	/**
	 * 
	 */
	@Override
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