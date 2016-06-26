package reciter.xml.retriever.pubmed;

import reciter.model.author.TargetAuthor;

public class DepartmentRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	
	public DepartmentRetrievalStrategy(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}
	
	@Override
	protected String constructInitialQuery(TargetAuthor targetAuthor) {
		String department = targetAuthor.getDepartment();
		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		return lastName + " " + firstInitial + " AND " + department;
	}

	@Override
	protected String constructStrictQuery(TargetAuthor targetAuthor) {
		String department = targetAuthor.getDepartment();
		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstName = targetAuthor.getAuthorName().getFirstName();
		return lastName + " " + firstName + " AND " + department;
	}

	@Override
	public int getThreshold() {
		return threshold;
	}

	@Override
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
}
