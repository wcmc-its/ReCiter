package reciter.model.author;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reciter.model.article.ReCiterArticle;

/**
 * Singleton TargetAuthor
 * @author jil3004
 *
 */
public class TargetAuthor extends ReCiterAuthor {

	private static TargetAuthor instance;
	
	private String cwid;
	private String department;
	private ReCiterArticle targetAuthorArticleIndexed;
	private Map<Integer, List<TypeScore>> map;
	private int terminalDegreeYear;
	
	public static class TypeScore {
		private String type;
		private double score;
		
		public TypeScore() {}
		public TypeScore(String type, double score) {
			this.type = type;
			this.score = score;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public double getScore() {
			return score;
		}
		public void setScore(double score) {
			this.score = score;
		}
	}
	
	private TargetAuthor(AuthorName name, AuthorAffiliation affiliation) {
		super(name, affiliation);
		map = new HashMap<Integer, List<TypeScore>>();
	}
	
	public static TargetAuthor getInstance() {
		return instance;
	}
	
	public static void init(AuthorName name, AuthorAffiliation affiliation) {
		instance = new TargetAuthor(name, affiliation);
	}

	public ReCiterArticle getTargetAuthorArticleIndexed() {
		return targetAuthorArticleIndexed;
	}

	public void setTargetAuthorArticleIndexed(ReCiterArticle targetAuthorArticleIndexed) {
		this.targetAuthorArticleIndexed = targetAuthorArticleIndexed;
	}

	public Map<Integer, List<TypeScore>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, List<TypeScore>> map) {
		this.map = map;
	}

	public int getTerminalDegreeYear() {
		return terminalDegreeYear;
	}

	public void setTerminalDegreeYear(int terminalDegreeYear) {
		this.terminalDegreeYear = terminalDegreeYear;
	}

	public String getCwid() {
		return cwid;
	}

	public void setCwid(String cwid) {
		this.cwid = cwid;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
}
