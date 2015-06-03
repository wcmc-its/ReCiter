package main.reciter.analysis;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReCiterConfigProperty {

	private String cwid;
	private String authorKeywords;
	private String coAuthors;
	private double similarityThreshold;
	private String firstName;
	private String middleName;
	private String lastName;
	private String authorAffiliation;
	private String authorDepartment;
	private boolean performRetrievePublication;
	private String cosineSimilarityType;
	private double titleSimilarityWeight;
	private double journalSimilarityWeight;
	private double keywordSimilarityWeight;
	private double authorSimilarityWeight;
	private boolean useStemming;

	public void loadProperty(String propertyFileName) throws IOException {
		Properties prop = new Properties();
		InputStream inputStream = new FileInputStream(propertyFileName);
		prop.load(inputStream);

		setCwid(prop.getProperty("cwid"));
		setAuthorKeywords(prop.getProperty("authorKeywords"));
		setCoAuthors(prop.getProperty("coAuthors"));
		setSimilarityThreshold(Double.parseDouble(prop.getProperty("similarityThreshold")));
		setFirstName(prop.getProperty("firstName"));
		setMiddleName(prop.getProperty("middleName"));
		setLastName(prop.getProperty("lastName"));
		setAuthorAffiliation(prop.getProperty("authorAffiliation"));
		setAuthorDepartment(prop.getProperty("authorDepartment"));
		setPerformRetrievePublication(Boolean.parseBoolean(prop.getProperty("performRetrievePublication")));
		setCosineSimilarityType(prop.getProperty("cosineSimilarityType"));
		setTitleSimilarityWeight(Double.parseDouble(prop.getProperty("titleSimilarityWeight")));
		setJournalSimilarityWeight(Double.parseDouble(prop.getProperty("journalSimilarityWeight")));
		setKeywordSimilarityWeight(Double.parseDouble(prop.getProperty("keywordSimilarityWeight")));
		setAuthorSimilarityWeight(Double.parseDouble(prop.getProperty("authorSimilarityWeight")));
		setUseStemming(Boolean.parseBoolean("useStemming"));
	}

	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getAuthorAffiliation() {
		return authorAffiliation;
	}
	public void setAuthorAffiliation(String authorAffiliation) {
		this.authorAffiliation = authorAffiliation;
	}
	public String getAuthorDepartment() {
		return authorDepartment;
	}
	public void setAuthorDepartment(String authorDepartment) {
		this.authorDepartment = authorDepartment;
	}
	public boolean isPerformRetrievePublication() {
		return performRetrievePublication;
	}
	public void setPerformRetrievePublication(boolean performRetrievePublication) {
		this.performRetrievePublication = performRetrievePublication;
	}
	public String getCosineSimilarityType() {
		return cosineSimilarityType;
	}
	public void setCosineSimilarityType(String cosineSimilarityType) {
		this.cosineSimilarityType = cosineSimilarityType;
	}
	public double getTitleSimilarityWeight() {
		return titleSimilarityWeight;
	}
	public void setTitleSimilarityWeight(double titleSimilarityWeight) {
		this.titleSimilarityWeight = titleSimilarityWeight;
	}
	public double getJournalSimilarityWeight() {
		return journalSimilarityWeight;
	}
	public void setJournalSimilarityWeight(double journalSimilarityWeight) {
		this.journalSimilarityWeight = journalSimilarityWeight;
	}
	public double getKeywordSimilarityWeight() {
		return keywordSimilarityWeight;
	}
	public void setKeywordSimilarityWeight(double keywordSimilarityWeight) {
		this.keywordSimilarityWeight = keywordSimilarityWeight;
	}
	public double getAuthorSimilarityWeight() {
		return authorSimilarityWeight;
	}
	public void setAuthorSimilarityWeight(double authorSimilarityWeight) {
		this.authorSimilarityWeight = authorSimilarityWeight;
	}

	public double getSimilarityThreshold() {
		return similarityThreshold;
	}

	public void setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
	}

	public String getCoAuthors() {
		return coAuthors;
	}

	public void setCoAuthors(String coAuthors) {
		this.coAuthors = coAuthors;
	}

	public String getAuthorKeywords() {
		return authorKeywords;
	}

	public void setAuthorKeywords(String authorKeywords) {
		this.authorKeywords = authorKeywords;
	}

	public boolean isUseStemming() {
		return useStemming;
	}

	public void setUseStemming(boolean useStemming) {
		this.useStemming = useStemming;
	}


}
