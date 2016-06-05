package reciter.database.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import reciter.database.DbConnectionFactory;
import reciter.database.DbUtil;
import reciter.database.dao.IdentityDao;
import reciter.database.model.Identity;

@Repository("identityDao")
public class IdentityDaoImpl implements IdentityDao {
	
	/**
	 * Retrieves identity information for cwid. Identity information includes first name, last name, middle name, etc.
	 * @param cwid
	 * @return Identity object for cwid.
	 */
	public Identity getIdentityByCwid(String cwid) {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT first_name, last_name, middle_name, title, primary_department, primary_affiliation,"
				+ "first_initial,middle_initial,full_published_name,prefix,suffix,other_departments,email,"
				+ "email_other FROM rc_identity WHERE cwid='" + cwid + "'";
		
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
				identity.setFirstInitial(rs.getString(7));
				identity.setMiddleInitial(rs.getString(8));
				identity.setFullPublishedName(rs.getString(9));
				identity.setPrefix(rs.getString(10));
				identity.setSuffix(rs.getString(11));
				identity.setOtherDepartment(rs.getString(12));
				identity.setEmail(rs.getString(13));
				identity.setEmailOther(rs.getString(14));
			}
		} catch (SQLException e) {
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);	
			DbUtil.close(con);
		}
		return identity;
	}

	public List<Identity> getAssosiatedGrantIdentityList(String cwid){
		List<Identity> identityList = new ArrayList<Identity>();
		List<String> cwids=new ArrayList<String>();
		String targetAuthorGrantIDQuery = "SELECT distinct b.cwid "
				+ "FROM rc_identity_grant a "
				+ "LEFT JOIN rc_identity_grant b ON b.grantid=a.grantid AND a.cwid!=b.cwid "
				+ "WHERE b.cwid is not null and a.cwid='" + cwid + "'";
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
				identityList.add(getIdentityByCwid(grantCwid));
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

	@Override
	public String getPubmedQuery(String cwid) {
		String pubmedQuery = null;
		ResultSet rs = null;
		CallableStatement callableStatement = null;
		Connection con = DbConnectionFactory.getConnection();
		
		try {
			String query = "{call create_pubmed_query (?)}";
			callableStatement = con.prepareCall(query);
			callableStatement.setString(1, cwid);
			rs = callableStatement.executeQuery();

			while (rs.next()) {
				pubmedQuery = rs.getString("pubmedQuery");
			}
			
		}  catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DbUtil.close(rs);
			DbUtil.close(callableStatement);
			DbUtil.close(con);
		}
		return pubmedQuery;
	}
}
