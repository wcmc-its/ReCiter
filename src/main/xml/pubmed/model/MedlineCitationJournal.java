package main.xml.pubmed.model;

public class MedlineCitationJournal {

	private MedlineCitationJournalISSN issn;
	private MedlineCitationJournalIssue journalIssue;
	private String title;
	private String isoAbbreviation;
	
	public String getJournalTitle() {
		return title;
	}
	public void setJournalTitle(String title) {
		this.title = title;
	}
	public MedlineCitationJournalIssue getJournalIssue() {
		return journalIssue;
	}
	public void setJournalIssue(MedlineCitationJournalIssue journalIssue) {
		this.journalIssue = journalIssue;
	}
	public String getIsoAbbreviation() {
		return isoAbbreviation;
	}
	public void setIsoAbbreviation(String isoAbbreviation) {
		this.isoAbbreviation = isoAbbreviation;
	}
}
