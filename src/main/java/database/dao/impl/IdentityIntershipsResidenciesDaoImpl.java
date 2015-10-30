package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.IdentityIntershipsResidenciesDao;
import database.model.IdentityIntershipsResidencies;


public class IdentityIntershipsResidenciesDaoImpl implements IdentityIntershipsResidenciesDao{
	@Override
	public List<IdentityIntershipsResidencies> getIdentityIntershipsResidenciesByCwid(String cwid){
		List<IdentityIntershipsResidencies> list = null;
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String whereClause="";
		if(cwid==null || cwid.trim().equals(""))whereClause=" is null";
		else whereClause="='"+cwid+"'";
		String query ="select prac_id,first_name,last_name,middle_name,cwid,npi,Internship_name1,internship_specialty1,internship_date_from1,internship_date_to1,internship_name2,internship_specialty2,internship_date_from2,internship_date_to2,internship_name3,internship_specialty3,internship_date_from3,internship_date_to3,residency_name1,residency_date_from1,residency_date_to1,residency_specialty1,residency_name2,residency_date_from2,residency_date_to2,residency_specialty2,residency_name3,residency_date_from3,residency_date_to3,residency_specialty3,fellowship_name1,fellowship_date_from1,fellowship_date_to1,fellowship_type1,fellowship_specialty1,fellowship_name2,fellowship_date_from2,fellowship_date_to2,fellowship_type2,fellowship_specialty2,fellowship_name3,fellowship_date_from3,fellowship_date_to3,fellowship_type3,fellowship_specialty3 from rc_identity_internships_residencies where cwid"+whereClause;
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				if(list==null)list=new ArrayList<IdentityIntershipsResidencies>();
				IdentityIntershipsResidencies residencies = new IdentityIntershipsResidencies();
				residencies.setPrac_id(rs.getString(1));
				residencies.setFirst_name(rs.getString(2));
				residencies.setLast_name(rs.getString(3));
				residencies.setMiddle_name(rs.getString(4));
				residencies.setCwid(rs.getString(5));
				residencies.setNpi(rs.getString(6));
				residencies.setInternship_name1(rs.getString(7));
				residencies.setInternship_specialty1(rs.getString(8));
				residencies.setInternship_date_from1(rs.getString(9));
				residencies.setInternship_date_to1(rs.getString(10));
				residencies.setInternship_name2(rs.getString(11));
				residencies.setInternship_specialty2(rs.getString(12));
				residencies.setInternship_date_from2(rs.getString(13));
				residencies.setInternship_date_to2(rs.getString(14));
				residencies.setInternship_name3(rs.getString(15));
				residencies.setInternship_specialty3(rs.getString(16));
				residencies.setInternship_date_from3(rs.getString(17));
				residencies.setInternship_date_to3(rs.getString(18));
				residencies.setResidency_name1(rs.getString(19));
				residencies.setResidency_date_from1(rs.getString(20));
				residencies.setResidency_date_to1(rs.getString(21));
				residencies.setResidency_specialty1(rs.getString(22));
				residencies.setResidency_name2(rs.getString(23));
				residencies.setResidency_date_from2(rs.getString(24));
				residencies.setResidency_date_to2(rs.getString(25));
				residencies.setResidency_specialty2(rs.getString(26));
				residencies.setResidency_name3(rs.getString(27));
				residencies.setResidency_date_from3(rs.getString(28));
				residencies.setResidency_date_to3(rs.getString(29));
				residencies.setResidency_specialty3(rs.getString(30));
				residencies.setFellowship_name1(rs.getString(31));
				residencies.setFellowship_date_from1(rs.getString(32));
				residencies.setFellowship_date_to1(rs.getString(33));
				residencies.setFellowship_type1(rs.getString(34));
				residencies.setFellowship_specialty1(rs.getString(35));
				residencies.setFellowship_name2(rs.getString(36));
				residencies.setFellowship_date_from2(rs.getString(37));
				residencies.setFellowship_date_to2(rs.getString(38));
				residencies.setFellowship_type2(rs.getString(39));
				residencies.setFellowship_specialty2(rs.getString(40));
				residencies.setFellowship_name3(rs.getString(41));
				residencies.setFellowship_date_from3(rs.getString(42));
				residencies.setFellowship_date_to3(rs.getString(43));
				residencies.setFellowship_type3(rs.getString(44));
				residencies.setFellowship_specialty3(rs.getString(45));
				list.add(residencies);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return list;
	}
}
