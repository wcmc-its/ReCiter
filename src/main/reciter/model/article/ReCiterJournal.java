package main.reciter.model.article;

/**
 * ReCiterArticle journal field.
 * @author jil3004
 *
 */
public class ReCiterJournal {
	private final String journalTitle;
	private int journalIssuePubDateYear;
	private String isoAbbreviation;
	
	public ReCiterJournal(String journalTitle) {
		this.journalTitle = journalTitle;
	}
	public boolean exist() {
		return journalTitle != null;
	}
	public String getJournalTitle() {
		return journalTitle;
	}
	public int getJournalIssuePubDateYear() {
		return journalIssuePubDateYear;
	}
	public void setJournalIssuePubDateYear(int journalIssuePubDateYear) {
		this.journalIssuePubDateYear = journalIssuePubDateYear;
	}
	public String getIsoAbbreviation() {
		return isoAbbreviation;
	}
	public void setIsoAbbreviation(String isoAbbreviation) {
		this.isoAbbreviation = isoAbbreviation;
	}
}
