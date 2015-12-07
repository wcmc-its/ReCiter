package reciter.tfidf;

import java.util.HashSet;
import java.util.Set;

public class ReCiterStringUtil {

	public static String[] tokenize(String s) {
		return s.replaceAll("[^A-Za-z0-9\\s+]", "").split("\\s+");
	}
	
	public static int computeNumberOfOverlapTokens(String s1, String s2) {
		String[] s1Arr = tokenize(s1);
		String[] s2Arr = tokenize(s2);
		Set<String> set1 = new HashSet<String>();
		for (String s : s1Arr)
			set1.add(s);
		
		int numOverlap = 0;
		Set<String> set2 = new HashSet<String>();
		for (String s : s2Arr)
			set2.add(s);
		
		for (String str1 : set1) {
			if (set2.contains(str1))
				numOverlap++;
		}
		return numOverlap;
	}
}
