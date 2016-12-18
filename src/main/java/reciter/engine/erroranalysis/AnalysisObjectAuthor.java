package reciter.engine.erroranalysis;

import reciter.model.article.ReCiterAuthor;

public class AnalysisObjectAuthor {
	
	private ReCiterAuthor author;
	private boolean lastNameMatchTargetAuthor;
	private boolean isFirstNameFullName; // first name in the article has length > 0 and target author's first name's length > 0
	private boolean isFirstNameMatch;
	private boolean isAliasNameMatch;
	private boolean isFirstNameDashRemovedMatch;
	private boolean isFirstInitialDashRemovedMatch;
	private boolean isFirstNameDashAddedMatch;
	private boolean isFirstNameDashInitialMatch;
	private boolean isFirstInitialMiddleInitialConcatenatedMatch;
	private boolean isLevenshteinDistanceMatch;
	private boolean isFirstThreeCharAndAffiliationScoreMatch;
	private boolean isTargetAuthorFirstAndMiddleNameConcatenatedMatch;
	private boolean isFirstPartOfNameMatch;
	private boolean isCheckMiddleNameMatch;
	private boolean isScopusFirstNameMatch;
	private boolean isMultipleAuthorMatchButMiddleNameDiffer;
	
	private boolean isInitialInCorrectOrder;
	private boolean isMiddleNameExistInArticleButNotInDb;
	private boolean isMiddleNameMatch;
	private boolean isRemovePeriodMatch;
	
	public ReCiterAuthor getAuthor() {
		return author;
	}
	public void setAuthor(ReCiterAuthor author) {
		this.author = author;
	}
	public boolean isLastNameMatchTargetAuthor() {
		return lastNameMatchTargetAuthor;
	}
	public void setLastNameMatchTargetAuthor(boolean lastNameMatchTargetAuthor) {
		this.lastNameMatchTargetAuthor = lastNameMatchTargetAuthor;
	}
	public boolean isFirstNameFullName() {
		return isFirstNameFullName;
	}
	public void setFirstNameFullName(boolean isFirstNameFullName) {
		this.isFirstNameFullName = isFirstNameFullName;
	}
	public boolean isFirstNameMatch() {
		return isFirstNameMatch;
	}
	public void setFirstNameMatch(boolean isFirstNameMatch) {
		this.isFirstNameMatch = isFirstNameMatch;
	}
	public boolean isAliasNameMatch() {
		return isAliasNameMatch;
	}
	public void setAliasNameMatch(boolean isAliasNameMatch) {
		this.isAliasNameMatch = isAliasNameMatch;
	}
	public boolean isFirstNameDashRemovedMatch() {
		return isFirstNameDashRemovedMatch;
	}
	public void setFirstNameDashRemovedMatch(boolean isFirstNameDashRemovedMatch) {
		this.isFirstNameDashRemovedMatch = isFirstNameDashRemovedMatch;
	}
	public boolean isFirstInitialDashRemovedMatch() {
		return isFirstInitialDashRemovedMatch;
	}
	public void setFirstInitialDashRemovedMatch(boolean isFirstInitialDashRemovedMatch) {
		this.isFirstInitialDashRemovedMatch = isFirstInitialDashRemovedMatch;
	}
	public boolean isFirstNameDashAddedMatch() {
		return isFirstNameDashAddedMatch;
	}
	public void setFirstNameDashAddedMatch(boolean isFirstNameDashAddedMatch) {
		this.isFirstNameDashAddedMatch = isFirstNameDashAddedMatch;
	}
	public boolean isFirstNameDashInitialMatch() {
		return isFirstNameDashInitialMatch;
	}
	public void setFirstNameDashInitialMatch(boolean isFirstNameDashInitialMatch) {
		this.isFirstNameDashInitialMatch = isFirstNameDashInitialMatch;
	}
	public boolean isFirstInitialMiddleInitialConcatenatedMatch() {
		return isFirstInitialMiddleInitialConcatenatedMatch;
	}
	public void setFirstInitialMiddleInitialConcatenatedMatch(boolean isFirstInitialMiddleInitialConcatenatedMatch) {
		this.isFirstInitialMiddleInitialConcatenatedMatch = isFirstInitialMiddleInitialConcatenatedMatch;
	}
	public boolean isLevenshteinDistanceMatch() {
		return isLevenshteinDistanceMatch;
	}
	public void setLevenshteinDistanceMatch(boolean isLevenshteinDistanceMatch) {
		this.isLevenshteinDistanceMatch = isLevenshteinDistanceMatch;
	}
	public boolean isFirstThreeCharAndAffiliationScoreMatch() {
		return isFirstThreeCharAndAffiliationScoreMatch;
	}
	public void setFirstThreeCharAndAffiliationScoreMatch(boolean isFirstThreeCharAndAffiliationScoreMatch) {
		this.isFirstThreeCharAndAffiliationScoreMatch = isFirstThreeCharAndAffiliationScoreMatch;
	}
	public boolean isTargetAuthorFirstAndMiddleNameConcatenatedMatch() {
		return isTargetAuthorFirstAndMiddleNameConcatenatedMatch;
	}
	public void setTargetAuthorFirstAndMiddleNameConcatenatedMatch(
			boolean isTargetAuthorFirstAndMiddleNameConcatenatedMatch) {
		this.isTargetAuthorFirstAndMiddleNameConcatenatedMatch = isTargetAuthorFirstAndMiddleNameConcatenatedMatch;
	}
	public boolean isFirstPartOfNameMatch() {
		return isFirstPartOfNameMatch;
	}
	public void setFirstPartOfNameMatch(boolean isFirstPartOfNameMatch) {
		this.isFirstPartOfNameMatch = isFirstPartOfNameMatch;
	}
	public boolean isCheckMiddleNameMatch() {
		return isCheckMiddleNameMatch;
	}
	public void setCheckMiddleNameMatch(boolean isCheckMiddleNameMatch) {
		this.isCheckMiddleNameMatch = isCheckMiddleNameMatch;
	}
	public boolean isScopusFirstNameMatch() {
		return isScopusFirstNameMatch;
	}
	public void setScopusFirstNameMatch(boolean isScopusFirstNameMatch) {
		this.isScopusFirstNameMatch = isScopusFirstNameMatch;
	}
	public boolean isMultipleAuthorMatchButMiddleNameDiffer() {
		return isMultipleAuthorMatchButMiddleNameDiffer;
	}
	public void setMultipleAuthorMatchButMiddleNameDiffer(boolean isMultipleAuthorMatchButMiddleNameDiffer) {
		this.isMultipleAuthorMatchButMiddleNameDiffer = isMultipleAuthorMatchButMiddleNameDiffer;
	}
	public boolean isInitialInCorrectOrder() {
		return isInitialInCorrectOrder;
	}
	public void setInitialInCorrectOrder(boolean isInitialInCorrectOrder) {
		this.isInitialInCorrectOrder = isInitialInCorrectOrder;
	}
	public boolean isMiddleNameExistInArticleButNotInDb() {
		return isMiddleNameExistInArticleButNotInDb;
	}
	public void setMiddleNameExistInArticleButNotInDb(boolean isMiddleNameExistInArticleButNotInDb) {
		this.isMiddleNameExistInArticleButNotInDb = isMiddleNameExistInArticleButNotInDb;
	}
	public boolean isMiddleNameMatch() {
		return isMiddleNameMatch;
	}
	public void setMiddleNameMatch(boolean isMiddleNameMatch) {
		this.isMiddleNameMatch = isMiddleNameMatch;
	}
	public boolean isRemovePeriodMatch() {
		return isRemovePeriodMatch;
	}
	public void setRemovePeriodMatch(boolean isRemovePeriodMatch) {
		this.isRemovePeriodMatch = isRemovePeriodMatch;
	}
}