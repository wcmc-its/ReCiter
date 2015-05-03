package main.reciter.model.affiliation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.reciter.model.article.ReCiterArticle;

public class DepartmentExtractImpl implements DepartmentExtractor {

//	public static void main(String[] args) {
//		final Pattern pattern = Pattern.compile("Department of (.+?),");
//		final Matcher matcher = pattern.matcher(
//				"Department of Pathology and Laboratory Medicine, Rutgers Biomedical and Health Sciences-New Jersey Medical School, Newark, New Jersey., Department of Pathology and Laboratory Medicine, Rutgers ");
//		
//		matcher.find();
//		System.out.println(matcher.group(1));
//	}
	
	@Override
	public String extractDepartment(ReCiterArticle reCiterArticle) {
		if (reCiterArticle.getAffiliationConcatenated() != null) {
			final Pattern pattern = Pattern.compile("Department of (.+?),");
			final Matcher matcher = pattern.matcher(reCiterArticle.getAffiliationConcatenated());
			if (matcher.find()) {
				return matcher.group(1);
			} else {
				return "none";
			}
		}
		return "none";
	}
}
