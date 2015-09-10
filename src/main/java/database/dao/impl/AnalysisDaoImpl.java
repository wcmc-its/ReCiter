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
	public void insertAnalysisList(List<Analysis> analysisList) {

		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		String query = "INSERT INTO rc_analysis (cwid, analysis_status, target_name, pubmed_search_query, pmid, "
				+ "article_title, full_journal_title, publication_year, scopus_target_author_affiliation, "
				+ "scopus_coauthor_affiliation, pubmed_target_author_affiliation, pubmed_coauthor_affiliation, "
				+ "article_keywords, name_matching_score, cluster_originator, journal_similarity_phase_one, "
				+ "coauthor_affiliation_score, target_author_affiliation_score, known_coinvestigator_score, "
				+ "funding_statement_score, terminal_degree_score, default_department_journal_similarity_score, "
				+ "department_of_affiliation_score, keyword_matching_score, phase_two_similarity_threshold, "
				+ "cluster_article_assigned_to, count_articles_in_assigned_cluster, cluster_selected_in_phase_two_matching, "
				+ "phase_two_affiliation_similarity, phase_two_keyword_similarity, phase_two_journal_similarity)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
				pst.setDouble(14, analysis.getNameMatchingScore());
				pst.setBoolean(15, analysis.isClusterOriginator());
				pst.setDouble(16, analysis.getJournalSimilarityPhaseOne());
				pst.setDouble(17, analysis.getCoauthorAffiliationScore());
				pst.setDouble(18, analysis.getTargetAuthorAffiliationScore());
				pst.setDouble(19, analysis.getKnownCoinvestigatorScore());
				pst.setDouble(20, analysis.getFundingStatementScore());
				pst.setDouble(21, analysis.getTerminalDegreeScore());
				pst.setDouble(22, analysis.getDefaultDepartmentJournalSimilarityScore());
				pst.setDouble(23, analysis.getDepartmentOfAffiliationScore());
				pst.setDouble(24, analysis.getKeywordMatchingScore());
				pst.setDouble(25, analysis.getPhaseTwoSimilarityThreshold());
				pst.setInt(26, analysis.getClusterArticleAssignedTo());
				pst.setInt(27, analysis.getCountArticlesInAssignedClsuter());
				pst.setDouble(28, analysis.getClusterSelectedInPhaseTwoMatching());
				pst.setDouble(29, analysis.getPhaseTwoAffiliationSimilarity());
				pst.setDouble(30, analysis.getPhaseTwoKeywordSimilarity());
				pst.setDouble(31, analysis.getPhaseTwoJournalSimilarity());
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
