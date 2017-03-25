package reciter.model.article;

import java.util.ArrayList;
import java.util.List;

public class ReCiterArticleAuthors {

	/**
	 * Authors.
	 */
	private List<ReCiterAuthor> authors;

	/**
	 * Constructs a ReCiterArticleAuthors with an ArrayList.
	 */
	public ReCiterArticleAuthors() {
		authors = new ArrayList<ReCiterAuthor>();
	}

	/**
	 * Returns {@code true} if there are co-authors, {@code false} otherwise.
	 * 
	 * @return {@code true} if there are co-authors, {@code false} otherwise.
	 */
	public boolean exist() {
		return authors != null && authors.size() != 0;
	}

	/**
	 * Returns the authors as a list.
	 * 
	 * @return the authors as a list.
	 */
	public List<ReCiterAuthor> getAuthors() {
		return authors;
	}
	
	/**
	 * Set the authors.
	 * 
	 * @param coAuthors
	 */
	public void setAuthors(List<ReCiterAuthor> authors) {
		this.authors = authors;
	}

	/**
	 * Returns the number of authors.
	 * 
	 * @return the number of authors.
	 */
	public int getNumberOfAuthors() {
		if (authors == null || authors.size() == 0) {
			return 0;
		}
		return authors.size();
	}

	/**
	 * Add an author.
	 * 
	 * @param author ReCiterAuthor author
	 */
	public void addAuthor(ReCiterAuthor author) {
		authors.add(author);
	}
}
