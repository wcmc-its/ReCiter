package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.dao.AnalysisDao;
import database.model.Analysis;

/**
 * Implementation for AnalysisDao.
 * 
 * @author jil3004
 *
 */
public class AnalysisDaoImpl implements AnalysisDao {

	@Override
	public void emptyTable() {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		String query = "DELETE FROM rc_analysis";
		try {
			pst = con.prepareStatement(query);
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(pst);
			DbUtil.close(con);
		}
	}

	@Override
	public void insertAnalysisList(List<Analysis> analysisList) {

		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;

		String query = "INSERT INTO rc_analysis ("
				+ "cwid, "
				+ "analysis_status, "
				+ "gold_standard, "
				+ "target_name, "
				+ "pubmed_search_query, "
				+ "pmid, "
				+ "article_title, "
				+ "full_journal_title, "
				+ "publication_year, "
				+ "scopus_target_author_affiliation, "
				+ "scopus_coauthor_affiliation, "
				+ "pubmed_target_author_affiliation, "
				+ "pubmed_coauthor_affiliation, "
				+ "article_keywords, "
				+ "cluster_originator, "
				+ "cluster_article_assigned_to, "
				+ "count_articles_in_assigned_cluster, "
				+ "cluster_selected_in_phase_two_matching, "
				+ "email_score, "
				+ "department_score, "
				+ "known_coinvestigator_score, "
				+ "affiliation_score, "
				+ "scopus_score, "
				+ "coauthor_score, "
				+ "journal_score, "
				+ "citizenship_score,"
				+ "bachelors_year_discrepancy_score,"
				+ "doctoral_year_dicrepancy_score) "
				+ "VALUES ("
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?, "
				+ "?)";

		try {
			pst = con.prepareStatement(query);
			
			for (Analysis analysis : analysisList) {
				int i = 0;
				pst.setString(++i, analysis.getCwid());
				pst.setString(++i, analysis.getAnalysisStatus());
				pst.setInt(++i, analysis.getGoldStandard());
				pst.setString(++i, analysis.getTargetName());
				pst.setString(++i, analysis.getPubmedSearchQuery());
				pst.setString(++i, analysis.getPmid());
				pst.setString(++i, analysis.getArticleTitle());
				pst.setString(++i, analysis.getFullJournalTitle());
				pst.setString(++i, analysis.getPublicationYear());
				pst.setString(++i, analysis.getScopusTargetAuthorAffiliation());
				pst.setString(++i, analysis.getScopusCoauthorAffiliation());
				pst.setString(++i, analysis.getPubmedTargetAuthorAffiliation());
				pst.setString(++i, analysis.getPubmedCoauthorAffiliation());
				pst.setString(++i, analysis.getArticleKeywords());
				pst.setBoolean(++i, analysis.isClusterOriginator());
				pst.setInt(++i, analysis.getClusterArticleAssignedTo());
				pst.setInt(++i, analysis.getCountArticlesInAssignedClsuter());
				pst.setInt(++i, analysis.getClusterSelectedInPhaseTwoMatching());
				pst.setDouble(++i, analysis.getEmailStrategyScore());
				pst.setDouble(++i, analysis.getDepartmentStrategyScore());
				pst.setDouble(++i, analysis.getKnownCoinvestigatorScore());
				pst.setDouble(++i, analysis.getAffiliationScore());
				pst.setDouble(++i, analysis.getScopusStrategyScore());
				pst.setDouble(++i, analysis.getCoauthorStrategyScore());
				pst.setDouble(++i, analysis.getJournalStrategyScore());
				pst.setDouble(++i, analysis.getCitizenshipStrategyScore());
				pst.setDouble(++i, analysis.getBachelorsYearDiscrepancyScore());
				pst.setDouble(++i, analysis.getDoctoralYearDiscrepancyScore());
				pst.addBatch();
			}
			pst.executeBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(pst);
			DbUtil.close(con);
		}
	}

}
