package database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.model.CoauthorAffiliations;

/**
 * DAO class for table rc_identity_education.
 * @author jil3004
 *
 */
public class CoauthorAffiliationsDao {

	// TODO: @Balu and @Hanumantha: Please create functions to get rows from the database table "rc_coauthor_affiliations".
	/**
	 * 
	 * @param affiliation
	 * @return
	 */
	public CoauthorAffiliations getCoathorAffiliationsByAffiliationLabel(String affiliation){
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		CoauthorAffiliations affiliations = null;
		try{
			String query = "select affiliation_id,label,score from  rc_coauthor_affiliations where label='"+affiliation+"'";
			pst=con.createStatement();
			rs=pst.executeQuery(query);
			while(rs.next()){
				affiliations=new CoauthorAffiliations();
				affiliations.setAffiliationId(rs.getInt(1));
				affiliations.setLabel(rs.getString(2));
				affiliations.setScore(rs.getFloat(3));
			}			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return affiliations;
	}
	
	/**
	 * 
	 * @param affiliations
	 * @return
	 */
	public List<CoauthorAffiliations> getCoathorAffiliationsByAffiliationLabel(List<String> affiliations){
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		List<CoauthorAffiliations> list=new ArrayList<CoauthorAffiliations>();
		try{
			String query = "select affiliation_id,label,score from  rc_coauthor_affiliations where label=?";
			pst=con.prepareStatement(query);
			for(String affiliation:affiliations){
				try{
					pst.setString(1, affiliation);
					rs=pst.executeQuery();
					while(rs.next()){
						CoauthorAffiliations aff = new CoauthorAffiliations();
						aff.setAffiliationId(rs.getInt(1));
						aff.setLabel(rs.getString(2));
						aff.setScore(rs.getFloat(3));
						list.add(aff);
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}finally{
					DbUtil.close(rs);
					rs=null;
					pst.clearParameters();
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return list;
	}
}
