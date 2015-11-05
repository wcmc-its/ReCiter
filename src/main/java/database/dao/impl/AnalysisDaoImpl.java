package database.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import database.DbConnectionFactory;
import database.DbUtil;
import database.NamedParameterStatement;
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
				+ "citizenship_score) "
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
				+ "?)";

		try {
			pst = con.prepareStatement(query);
			for (Analysis analysis : analysisList) {
				pst.setString(1, analysis.getCwid());
				pst.setString(2, analysis.getAnalysisStatus());
				pst.setString(3, analysis.getTargetName());
				pst.setString(4, analysis.getPubmedSearchQuery());
				pst.setString(5, analysis.getPmid());
				pst.setString(6, analysis.getArticleTitle());
				pst.setString(7, analysis.getFullJournalTitle());
				pst.setString(8, analysis.getPublicationYear());
				pst.setString(9, analysis.getScopusTargetAuthorAffiliation());
				pst.setString(10, analysis.getScopusCoauthorAffiliation());
				pst.setString(11, analysis.getPubmedTargetAuthorAffiliation());
				pst.setString(12, analysis.getPubmedCoauthorAffiliation());
				pst.setString(13, analysis.getArticleKeywords());
				pst.setBoolean(14, analysis.isClusterOriginator());
				pst.setInt(15, analysis.getClusterArticleAssignedTo());
				pst.setInt(16, analysis.getCountArticlesInAssignedClsuter());
				pst.setInt(17, analysis.getClusterSelectedInPhaseTwoMatching());
				pst.setDouble(18, analysis.getEmailStrategyScore());
				pst.setDouble(19, analysis.getDepartmentStrategyScore());
				pst.setDouble(20, analysis.getKnownCoinvestigatorScore());
				pst.setDouble(21, analysis.getAffiliationScore());
				pst.setDouble(22, analysis.getScopusStrategyScore());
				pst.setDouble(23, analysis.getCoauthorStrategyScore());
				pst.setDouble(24, analysis.getJournalStrategyScore());
				pst.setDouble(25, analysis.getCitizenshipStrategyScore());
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
