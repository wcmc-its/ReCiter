package reciter.utils;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

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

	/**
	 * Source: https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java.
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static int levenshteinDistance (CharSequence lhs, CharSequence rhs) {                          
		int len0 = lhs.length() + 1;                                                     
		int len1 = rhs.length() + 1;                                                     

		// the array of distances                                                       
		int[] cost = new int[len0];                                                     
		int[] newcost = new int[len0];                                                  

		// initial cost of skipping prefix in String s0                                 
		for (int i = 0; i < len0; i++) cost[i] = i;                                     

		// dynamically computing the array of distances                                  

		// transformation cost for each letter in s1                                    
		for (int j = 1; j < len1; j++) {                                                
			// initial cost of skipping prefix in String s1                             
			newcost[0] = j;                                                             

			// transformation cost for each letter in s0                                
			for(int i = 1; i < len0; i++) {                                             
				// matching current letters in both strings                             
				int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             

				// computing cost for each transformation                               
				int cost_replace = cost[i - 1] + match;                                 
				int cost_insert  = cost[i] + 1;                                         
				int cost_delete  = newcost[i - 1] + 1;                                  

				// keep minimum cost                                                    
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
			}                                                                           

			// swap cost/newcost arrays                                                 
			int[] swap = cost; cost = newcost; newcost = swap;                          
		}                                                                               

		// the distance is the cost for transforming all letters in both strings        
		return cost[len0 - 1];                                                          
	}

	/**
	 * Source: http://stackoverflow.com/questions/1008802/converting-symbols-accent-letters-to-english-alphabet
	 * 
	 * @param str
	 * @return
	 */
	public static String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
}
