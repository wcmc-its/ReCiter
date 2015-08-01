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
import database.model.Identity;

public class IdentityDao {

	/**
	 * Retrieves identity information for cwid. Identity information includes first name, last name, middle name, etc.
	 * @param cwid
	 * @return Identity object for cwid.
	 */
	public Identity getIdentityByCwid(String cwid) {
		Connection con = DbConnectionFactory.getConnection();
		Identity identity = null;
		try{
			identity=getIdentityByCwid(con,cwid);
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			DbUtil.close(con);
		}
		return identity;
	}
	
	private Identity getIdentityByCwid(Connection con, String cwid) throws SQLException {
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT first_name, last_name, middle_name, title, primary_department, primary_affiliation,email FROM rc_identity WHERE cwid='" + cwid + "'";
		Identity identity = new Identity();
		identity.setCwid(cwid);
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				identity.setFirstName(rs.getString(1));
				identity.setLastName(rs.getString(2));
				identity.setMiddleName(rs.getString(3));
				identity.setTitle(rs.getString(4));
				identity.setPrimaryDepartment(rs.getString(5));
				identity.setPrimaryAffiliation(rs.getString(6));
				identity.setEmail(rs.getString(7));
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);			
		}
		return identity;
	}
	
	public List<Identity> getAssosiatedGrantIdentityList(String cwid){
		List<Identity> identityList = new ArrayList<Identity>();
		List<String> cwids=new ArrayList<String>();
		String targetAuthorGrantIDQuery = "SELECT distinct b.cwid FROM rc_identity_grant a left join rc_identity_grant b on b.grantid=a.grantid and a.cwid!=b.cwid where b.cwid is not null and a.cwid='" + cwid + "'";
		Connection con = DbConnectionFactory.getConnection();
		Statement pst = null;
		ResultSet rs = null;
		try {
			pst = con.createStatement();
			rs = pst.executeQuery(targetAuthorGrantIDQuery);
			while (rs.next()) {
				try {
					cwids.add(rs.getString(1));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			for(String grantCwid: cwids){
				identityList.add(getIdentityByCwid(con,grantCwid));
			}
		}  catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return identityList;
	}
}
