package main.reciter.model.article;

import java.util.ArrayList;
import java.util.List;

import main.reciter.model.author.ReCiterAuthor;

public class ReCiterArticleCoAuthors {

	private List<ReCiterAuthor> coAuthors;

	public ReCiterArticleCoAuthors() {
		coAuthors = new ArrayList<ReCiterAuthor>();
	}

	public boolean exist() {
		return coAuthors != null;
	}

	public List<ReCiterAuthor> getCoAuthors() {
		return coAuthors;
	}
	public void setCoAuthors(List<ReCiterAuthor> coAuthors) {
		this.coAuthors = coAuthors;
	}

	public int getNumberCoAuthors() {
		return coAuthors.size();
	}

	public void addCoAuthor(ReCiterAuthor author) {
		coAuthors.add(author);
	}

	public String getAffiliationConcatForm() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < coAuthors.size(); i++) {
			if (coAuthors.get(i).getAffiliation().getAffiliation() != null) {
				if (i == coAuthors.size() - 1) {
					sb.append(coAuthors.get(i).getAffiliation().getAffiliation());
				} else {
					sb.append(coAuthors.get(i).getAffiliation().getAffiliation());
					sb.append(" ");
				}
			}
		}
		return sb.toString();
	}
}
