package main.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.database.DbConnectionFactory;
import main.database.DbUtil;
import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.article.ReCiterArticleCoAuthors;
import main.reciter.model.article.ReCiterArticleKeywords;
import main.reciter.model.article.ReCiterArticleTitle;
import main.reciter.model.article.ReCiterJournal;
import main.reciter.model.author.AuthorAffiliation;
import main.reciter.model.author.AuthorName;
import main.reciter.model.author.ReCiterAuthor;
import main.xml.scopus.model.ScopusEntry;

import org.apache.commons.lang3.StringUtils;

public class ArticleDao {
	
	
	/**
	 * Retrieves all pmid to affiliation info from table rc_scopus_affiliation.
	 * @return
	 */
	public static Map<Integer, String> getScopusAffiliation() {
		Map<Integer, String> pmidToAffiliation = new HashMap<Integer, String>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT pmid, affiliation from rc_scopus_affiliation";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				pmidToAffiliation.put(rs.getInt(1), rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return pmidToAffiliation;
	}
	
	/**
	 * Stores pmid to Scopus Affiliation info into table rc_scopus_affiliation
	 * @param pmidToAffiliation
	 */
	public static void storeScopusAffiliation(List<ScopusEntry> scopusEntryList) {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		int count = 0;
		final int batchSize = 500;
		String query = "INSERT IGNORE INTO rc_scopus_affiliation (pmid, affiliation) VALUES (?, ?)";
		try {
			con.setAutoCommit(false);
			pst = con.prepareStatement(query);
			for (ScopusEntry entry : scopusEntryList) {
				if (entry.getPubmedID() != null) {
					pst.setInt(1, Integer.parseInt(entry.getPubmedID()));
					pst.setString(2, entry.affiliationConcatForm());
					pst.addBatch();
					count += 1;
				}
				if (count % batchSize == 0) {
					pst.executeBatch();
				}
			}
			pst.executeBatch();
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(pst);
			DbUtil.close(con);
		}
	}
	
	public Map<String, String> getXml(String cwid) {
		Map<String, String> xmlMap = new HashMap<String, String>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT pmid, xml from rc_test_data_xml "
				+ "WHERE rc_test_data_xml.cwid = '" + cwid + "'";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				xmlMap.put(rs.getString(1), rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return xmlMap;
	}
	
	public void storeXml(String cwid, Map<String, String> xmlMap) {
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		int count = 0;
		final int batchSize = 500;
		String query = "INSERT INTO rc_test_data_xml (cwid, pmid, xml) VALUES (?, ?, ?)";
		try {
			con.setAutoCommit(false);
			pst = con.prepareStatement(query);
			pst.setString(1, cwid);
			for (Entry<String, String> entry : xmlMap.entrySet()) {
				pst.setString(2, entry.getKey());
				pst.setString(3, entry.getValue());
				pst.addBatch();
				count += 1;
				if (count % batchSize == 0) {
					pst.executeBatch();
				}
			}
			pst.executeBatch();
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(pst);
			DbUtil.close(con);
		}
	}
	public Set<Integer> getPmidList(String cwid) {
		Set<Integer> pmidSet = new HashSet<Integer>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "SELECT rc_gold_standard_pmids.pmid from rc_gold_standard_pmids "
				+ "WHERE rc_gold_standard_pmids.cwid = '" + cwid + "'";
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				String pmidStr = rs.getString(1);
				try {
					if (pmidStr.contains("/")) {
						pmidStr = pmidStr.substring(0, pmidStr.indexOf("/"));
					}
					int pmid = Integer.parseInt(pmidStr);
					pmidSet.add(pmid);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return pmidSet;
	}
	
	/**
	 * Searches the database for articles containing Person target.
	 * @param target author to be searched in the database.
	 * @return a list of articles authored by target.
	 */
	public List<ReCiterArticle> getData(ReCiterAuthor target) {
		ArticleDao p = new ArticleDao();
		List<ReCiterArticle> data = p.queryArticleListByAuthor(target);
		for (ReCiterArticle article : data) {
			int articleId = article.getArticleID();
			List<ReCiterAuthor> authorList = queryAuthorListByArticlePk(articleId);
			article.setArticleCoAuthors(new ReCiterArticleCoAuthors());
			article.getArticleCoAuthors().setCoAuthors(authorList);
		}
		return data;
	}

	/**
	 * Searches the database for authors who wrote this article given by articlePk.
	 * @param articlePk article id of the article to be searched in the
	 * database.
	 * @return Returns a list of authors for this article.
	 */
	protected List<ReCiterAuthor> queryAuthorListByArticlePk(int articlePk) {
		List<ReCiterAuthor> authorList = new ArrayList<ReCiterAuthor>();
		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = 
				"SELECT rc_person.surname, rc_person.given_name, rc_affiliation.affiliation_name "
						+ "FROM rc_article "
						+ "LEFT JOIN rc_article_journal ON rc_article.article_pk = rc_article_journal.article_fk "
						+ "LEFT JOIN rc_journal ON rc_journal.journal_pk = rc_article_journal.journal_fk "
						+ "LEFT JOIN rc_article_keyword on rc_article_keyword.article_fk = rc_article.article_pk "
						+ "LEFT JOIN rc_keyword on rc_keyword.keyword_pk = rc_article_keyword.keyword_fk "
						+ "LEFT JOIN rc_person_article on rc_person_article.article_fk = rc_article.article_pk "
						+ "LEFT JOIN rc_person ON rc_person.person_pk = rc_person_article.person_fk "
						+ "LEFT JOIN rc_person_affiliation on rc_person_affiliation.person_fk = rc_person.person_pk "
						+ "LEFT JOIN rc_affiliation on rc_affiliation.affiliation_pk = rc_person_affiliation.affiliation_fk "
						+ "WHERE rc_article.article_pk = " + articlePk;
		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				String lastName = rs.getString(1);
				String givenName = rs.getString(2);
				String affiliations = rs.getString(3);
				String[] givenNameArray = StringUtils.split(givenName, "\\s+"); // split given name by white space.
				String firstName = "";
				String middleName = "";
				if (givenNameArray != null && givenNameArray.length == 2) { // split into first and middle names.
					firstName = givenNameArray[0]; // first name.
					middleName = givenNameArray[1]; // middle name.
					firstName = firstName.replace(".", ""); // remove period from first name.
					middleName = middleName.replace(".", ""); // remove period from middle initial if exist.
				} else {
					firstName = givenName;
					firstName = firstName.replace(".", "");
				}
				String[] affiliationArray = StringUtils.split(affiliations, "\\|"); // split by "|"
				List<AuthorAffiliation> affiliationList = new ArrayList<AuthorAffiliation>();
				if (affiliationArray != null) {
					for (int i = 0; i < affiliationArray.length; i++) {
						AuthorAffiliation a = new AuthorAffiliation(affiliationArray[i]);
						affiliationList.add(a); // add affiliation
					}
				}
				ReCiterAuthor wcmcPerson = new ReCiterAuthor(new AuthorName(firstName, middleName, lastName), 
						new AuthorAffiliation(affiliationList.get(0).getAffiliation()));
				authorList.add(wcmcPerson);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return authorList;
	}

	/**
	 * Searches the database for articles written by authors who share the same
	 * first initial and last name as the target. This function
	 * doesn't fill in the co-author information for each retrieved articles.
	 * 
	 * <pre>Example: If target name is "Stephen B. Johnson," it returns a list
	 * of articles authored by "S Johnson."</pre>
	 * 
	 * @param target author to be searched in the database.
	 * @return a list of articles authored by target.
	 */
	protected List<ReCiterArticle> queryArticleListByAuthor(ReCiterAuthor target) {
		List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
		String lastName = target.getAuthorName().getLastName();
		String firstInitial = target.getAuthorName().getFirstInitial();

		Connection con = DbConnectionFactory.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = 
				"SELECT rc_article.article_pk, rc_article.title, rc_article.abstract, rc_journal.journal_name, rc_keyword.terms "
						+ "FROM rc_article "
						+ "LEFT JOIN rc_article_journal ON rc_article.article_pk = rc_article_journal.article_fk "
						+ "LEFT JOIN rc_journal ON rc_journal.journal_pk = rc_article_journal.journal_fk "
						+ "LEFT JOIN rc_article_keyword on rc_article_keyword.article_fk = rc_article.article_pk "
						+ "LEFT JOIN rc_keyword on rc_keyword.keyword_pk = rc_article_keyword.keyword_fk "
						+ "LEFT JOIN rc_person_article on rc_person_article.article_fk = rc_article.article_pk "
						+ "LEFT JOIN rc_person ON rc_person.person_pk = rc_person_article.person_fk "
						+ "LEFT JOIN rc_person_affiliation on rc_person_affiliation.person_fk = rc_person.person_pk "
						+ "LEFT JOIN rc_affiliation on rc_affiliation.affiliation_pk = rc_person_affiliation.affiliation_fk "
						+ "WHERE rc_person.surname = '" + lastName + "' AND rc_person.given_name LIKE '" + firstInitial + "%' "
						+ "GROUP BY rc_article.article_pk;";

		try {
			pst = con.prepareStatement(query);
			rs = pst.executeQuery();
			while (rs.next()) {
				ReCiterArticle a = new ReCiterArticle(rs.getInt(1));
				a.setArticleTitle(new ReCiterArticleTitle(rs.getString(2))); // title
//				a.setAbstractText(rs.getString(3)); // abstract text
				a.setJournal(new ReCiterJournal(rs.getString(4))); // journal title
				String keywordFromDb = rs.getString(5); // get keyword field of this article
				String[] keywordArray = StringUtils.split(keywordFromDb, "\\|"); // split by "|"
				if (keywordArray != null) {
					ReCiterArticleKeywords k = new ReCiterArticleKeywords();
					for (int i = 0; i < keywordArray.length; i++) {
						k.addKeyword(keywordArray[i]);
					}
					a.setArticleKeywords(k);
				}
				
				articleList.add(a);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs);
			DbUtil.close(pst);
			DbUtil.close(con);
		}
		return articleList;
	}
}
