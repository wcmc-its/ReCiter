package reciter.tfidf;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TfidfTest {

	private String docA;
	private String docB;
	
	@Test
	public void TestGetTermFrequency() {
		String e = extractDepartment("Department of General Surgery");
		System.out.println(e);
	}
	
	private String extractDepartment(String department) {
		final Pattern pattern = Pattern.compile("Department of (.+?)[\\.,]");
		final Matcher matcher = pattern.matcher(department);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}

}
