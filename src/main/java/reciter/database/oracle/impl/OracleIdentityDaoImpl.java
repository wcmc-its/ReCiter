package reciter.database.oracle.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import reciter.algorithm.evidence.targetauthor.grant.GrantStrategyContext;
import reciter.database.oracle.OracleConnectionFactory;
import reciter.database.oracle.OracleIdentityDao;
import reciter.model.identity.Grant;

@Repository("oracleIdentityDao")
public class OracleIdentityDaoImpl implements OracleIdentityDao {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(OracleIdentityDaoImpl.class);

	@Autowired
	private OracleConnectionFactory oracleConnectionFactory;

	@Override
	public int getBachelorDegreeYear(String cwid) {
		int year = 0;
		Connection connection = oracleConnectionFactory.createConnection();
		if (connection == null) {
			return year;
		}
		ResultSet rs = null;
		PreparedStatement pst = null;
		String sql = "select degree_year from OFA_DB.PERSON p1 "
				+ "inner join "
				+ "(select cwid, degree_year from ofa_db.degree d "
				+ "join OFA_DB.FACORE fac on fac.facore_pk = d.facore_fk "
				+ "join OFA_DB.PERSON p on p.PERSON_PK = fac.FACORE_PK "
				+ "join OFA_DB.INSTITUTE i on i.institute_PK = d.institute_FK "
				+ "left join OFA_DB.DEGREE_NAME n on n.DEGREE_NAME_PK = d.degree_name_fk "
				+ "where p.cwid is not NULL and terminal_degree <> 'Yes' and doctoral_degree is null "
				+ "and md = 'F' and mdphd ='F' and do_degree = 'F' and n.OTHER_PROFESSORIAL = 'F' "
				+ "and degree not like 'M%' and degree not like 'Pharm%' and degree not like 'Sc%' "
				+ "order by degree_year asc) p2 "
				+ "on p2.cwid = p1.cwid and p1.cwid = ?";
		try {
			pst = connection.prepareStatement(sql);
			pst.setString(1, cwid);
			rs = pst.executeQuery();
			while(rs.next()) {
				year = rs.getInt(1);
			}
		} catch(SQLException e) {
			slf4jLogger.error("Exception occured in query=" + sql, e);
		}
		finally {
			try {
				rs.close();
				pst.close();
				connection.close();;
			} catch(SQLException e) {
				slf4jLogger.error("Unabled to close connection to Oracle DB.", e);
			}
		}
		return year;
	}

	@Override
	public int getDoctoralYear(String cwid) {
		int year = 0;
		Connection connection = oracleConnectionFactory.createConnection();
		if (connection == null) {
			return year;
		}
		ResultSet rs = null;
		PreparedStatement pst = null;
		String sql = "select degree_year from ofa_db.degree d "
				+ "join OFA_DB.FACORE fac on fac.facore_pk = d.facore_fk "
				+ "join OFA_DB.PERSON p ON p.PERSON_PK = fac.FACORE_PK "
				+ "join OFA_DB.INSTITUTE i on i.institute_PK = d.institute_FK "
				+ "left join OFA_DB.DEGREE_NAME n on n.DEGREE_NAME_PK = d.degree_name_fk "
				+ "where p.cwid is not NULL and cwid <> '0' and terminal_degree = 'Yes' and p.cwid = ?";
		try {
			pst = connection.prepareStatement(sql);
			pst.setString(1, cwid);
			rs = pst.executeQuery();
			while(rs.next()) {
				year = rs.getInt(1);
			}
		} catch(SQLException e) {
			slf4jLogger.error("Exception occured in query=" + sql, e);
		}
		finally {
			try {
				rs.close();
				pst.close();
				connection.close();;
			} catch(SQLException e) {
				slf4jLogger.error("Unabled to close connection to Oracle DB.", e);
			}
		}
		return year;
	}

	@Override
	public List<String> getInstitutions(String cwid) {
		Connection connection = oracleConnectionFactory.createConnection();
		if (connection == null) {
			return Collections.emptyList();
		}
		ResultSet rs = null;
		PreparedStatement pst = null;
		String sql = "SELECT cwid, ai.INSTITUTION, 'Academic-PrimaryAffiliation' from OFA_DB.FACORE fac "
				+ "JOIN OFA_DB.AFFIL_INSTITUTE ai ON ai.affil_institute_pk = fac.affil_institute_FK "
                + "JOIN OFA_DB.PERSON p ON p.PERSON_PK = fac.FACORE_PK where cwid is not null and cwid <> '0' and cwid = ?"
                + "union "
                + "SELECT p.cwid, ai.INSTITUTION, 'Academic-AppointingInstitution' FROM OFA_DB.FACORE fac "
                + "JOIN OFA_DB.APPOINTMENT a ON fac.facore_PK = a.facore_fk "
                + "JOIN OFA_DB.AFFIL_INSTITUTE ai ON ai.affil_institute_pk = a.affil_institute_FK "
                + "JOIN OFA_DB.AFFIL_INSTITUTE ai ON ai.affil_institute_pk = fac.affil_institute_FK "
                + "JOIN OFA_DB.PERSON p ON p.PERSON_PK = fac.FACORE_PK "
                + "where cwid is not null and cwid <> '0' and cwid = ?"
                + "union "
                + "SELECT cwid, institution, 'Academic-Degree' from ofa_db.degree d "
                + "join OFA_DB.FACORE fac on fac.facore_pk = d.facore_fk "
                + "join OFA_DB.PERSON p ON p.PERSON_PK = fac.FACORE_PK " 
                + "join OFA_DB.INSTITUTE i on i.institute_PK = d.institute_FK "
                + "left join OFA_DB.DEGREE_NAME n on n.DEGREE_NAME_PK = d.degree_name_fk " 
                + "where cwid is not null and cwid <> '0' and cwid = ?";
		Set<String> distinctInstitutions = new HashSet<String>();
		try {
			pst = connection.prepareStatement(sql);
			pst.setString(1, cwid);
			pst.setString(2, cwid);
			pst.setString(3, cwid);
			rs = pst.executeQuery();
			while(rs.next()) {
				distinctInstitutions.add(rs.getString(2));
			}
		} catch(SQLException e) {
			slf4jLogger.error("Exception occured in query=" + sql, e);
		}
		finally {
			try {
				rs.close();
				pst.close();
				connection.close();;
			} catch(SQLException e) {
				slf4jLogger.error("Unabled to close connection to Oracle DB.", e);
			}
		}
		return new ArrayList<>(distinctInstitutions);
	}

	@Override
	public List<String> getPersonalEmailFromOfa(String cwid) {
		List<String> emails = new ArrayList<String>();
		Connection connection = oracleConnectionFactory.createConnection();
		if (connection == null) {
			return emails;
		}
		ResultSet rs = null;
		PreparedStatement pst = null;
		String sql = "select email from "
				+ "(select distinct p.cwid, substr(private_email,1,INSTR(private_email,',')-1) as email "
				+ "FROM OFA_DB.PERSON p where p.private_email like '%,%' "
				+ "UNION "
				+ "select distinct p.cwid, substr(email,1,INSTR(email,',')-1) AS email from OFA_DB.PERSON p "
				+ "where p.email like '%,%' "
				+ "UNION "
				+ "select distinct p.cwid, "
				+ "substr(replace(p.private_email,' ',''),1 + INSTR(replace(p.private_email,' ',''),',')) "
				+ "as email from OFA_DB.PERSON p where p.private_email like '%,%' "
				+ "UNION "
				+ "select distinct p.cwid, "
				+ "substr(replace(p.email,' ',''),1 + INSTR(replace(p.email,' ',''),',')) as email from OFA_DB.PERSON p "
				+ "where p.email like '%,%') where email like '%@%' and email like '%.%' and CWID = ?";
		try {
			pst = connection.prepareStatement(sql);
			pst.setString(1, cwid);
			rs = pst.executeQuery();
			while(rs.next()) {
				emails.add(rs.getString(1));
			}
		} catch(SQLException e) {
			slf4jLogger.error("Exception occured in query=" + sql, e);
		}
		finally {
			try {
				rs.close();
				pst.close();
				connection.close();;
			} catch(SQLException e) {
				slf4jLogger.error("Unabled to close connection to Oracle DB.", e);
			}
		}
		return emails;
	}

	@Override
	public List<String> getGrants(String cwid) {
		
		List<String> grants = new ArrayList<String>();
		Connection connection = oracleConnectionFactory.createConnection();
		if (connection == null) {
			return grants;
		}
		ResultSet rs = null;
		PreparedStatement pst = null;
		String sql = "select distinct cwid, " 
				   + "substr(substr(replace(sponsor_award_number,'  ',' '),1,INSTR(replace(sponsor_award_number,'  ',' '),'-')-1),7,8) AS awardNumber "
				   + "from coeus_reports_user_1.V_ALL_AWARD_CO_INV_VIVO "
				   + "where sponsor_type_code = '0' and "
				   + "substr(sponsor_award_number,2,1) = ' ' and cwid = ?";
		try {
			pst = connection.prepareStatement(sql);
			pst.setString(1, cwid);
			rs = pst.executeQuery();
			while(rs.next()) {
				grants.add(rs.getString(2));
			}
		} catch(SQLException e) {
			slf4jLogger.error("Exception occured in query=" + sql, e);
		}
		finally {
			try {
				rs.close();
				pst.close();
				connection.close();;
			} catch(SQLException e) {
				slf4jLogger.error("Unabled to close connection to Oracle DB.", e);
			}
		}
		return grants;
	}

	@Override
	public List<String> getRelationship(String cwid) {
		
		List<String> relationships = new ArrayList<String>();
		Connection connection = oracleConnectionFactory.createConnection();
		if (connection == null) {
			return relationships;
		}
		ResultSet rs = null;
		PreparedStatement pst = null;
		String sql = "select distinct cwid AS targetCWID, C2.relationshipCWID "
				   + "from coeus_reports_user_1.V_ALL_AWARD_CO_INV_VIVO C1 "
				   + "left join (select distinct " 
				   + "cwid AS relationshipCWID, " 
				   + "award_number AS awardNumber2 "
				   + "from coeus_reports_user_1.V_ALL_AWARD_CO_INV_VIVO "
				   + "where  award_number not in ('003152-001','005927-001') "
				   + ")  C2 "
				   + "on award_number = C2.awardNumber2 " 
				   + "where relationshipCWID is not null "
				   + "and relationshipCWID <> cwid " 
				   + "and award_number not in ('003152-001','005927-001') and cwid = ?";
		try {
			pst = connection.prepareStatement(sql);
			pst.setString(1, cwid);
			rs = pst.executeQuery();
			while(rs.next()) {
				relationships.add(rs.getString(2));
			}
		} catch(SQLException e) {
			slf4jLogger.error("Exception occured in query=" + sql, e);
		}
		finally {
			try {
				rs.close();
				pst.close();
				connection.close();;
			} catch(SQLException e) {
				slf4jLogger.error("Unabled to close connection to Oracle DB.", e);
			}
		}
		return relationships;
	}

}
