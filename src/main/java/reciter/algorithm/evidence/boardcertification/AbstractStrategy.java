package reciter.algorithm.evidence.boardcertification;

import java.util.List;

public abstract class AbstractStrategy implements Strategy {

	private String[] removals = {"/", "and", "the", "medicine", "-", "with", "in", "med", "adult", "general"};

	/**
	 * <a href="https://github.com/wcmc-its/ReCiter/issues/45">Github issue #45 -
	 * Leverage data on board certifications to improve phase two matching</a>
	 * 
	 * <p>Pre-process the board certification data.
	 * 
	 * <p>1. Break up terms containing a slash into two distinct terms: 
	 * eg: "Obstetrics/Gynecology" >> "Obstetrics", "Gynecology".
	 * 
	 * <p>2. Remove any of the following terms in the {@code removals} array.
	 * 
	 * @param boardCertifications list of board certifications.
	 * 
	 * @return A concatenated string that has been processed.
	 */
	public String preprocess(List<String> boardCertifications) {

		StringBuilder sb = new StringBuilder();
		for(String certification : boardCertifications){
			for (String removal : removals) {
				certification = certification.replace(removal, " ");
			}
			certification = certification.replaceAll("\\s+", "").trim();
			sb.append(certification);
			sb.append(" ");
		}
		return sb.toString().trim();
	}
}
