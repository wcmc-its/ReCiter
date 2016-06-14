package reciter.xml.retriever.pubmed;

import reciter.model.author.TargetAuthor;

public class DepartmentRetrievalStrategy extends AbstractRetrievalStrategy {

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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setThreshold(int threshold) {
		// TODO Auto-generated method stub
		
	}
}
