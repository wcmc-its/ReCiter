package main.xml.pubmed.model;

public class MedlineCitationJournal {

	private MedlineCitationJournalISSN issn;
	private MedlineCitationJournalIssue issue;
	private String title;
	private String isoAbbreviation;
	
	public String getJournalTitle() {
		return title;
	}
	public void setJournalTitle(String title) {
		this.title = title;
	}
}
