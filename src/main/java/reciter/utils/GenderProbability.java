package reciter.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import reciter.database.dynamodb.model.Gender;
import reciter.database.dynamodb.model.GenderEnum;
import reciter.engine.EngineParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

/**
 * @author szd2013
 * This class tries to find gender probability for identity user
 */
public class GenderProbability {
	
	/**
	 * @param identity
	 * Finds the Gender Name and the probability from Gender table and assigns to identity
	 */
	public static void getGenderIdentityProbability(Identity identity) {
		List<Gender> genders = new ArrayList<Gender>();
		for(Gender gender: EngineParameters.getGenders()) {
			genders.add(new Gender(gender.getUniqueId(), gender.getName(), gender.getGender(), gender.getProbability()));
		}
		List<Gender> matchingGenders = new ArrayList<Gender>();
		Set<String> identityNames = new HashSet<String>();
		if(identity.getPrimaryName() != null) {
			if(identity.getPrimaryName().getFirstName() != null
					&&
					(identity.getPrimaryName().getFirstName().contains(" ") || identity.getPrimaryName().getFirstName().contains("-"))) {
				String primaryName[] = identity.getPrimaryName().getFirstName().split("\\s+|-");
				for (int i = 0; i < primaryName.length; i++) {
					if(primaryName[i].length() >= 2) {
						identityNames.add(primaryName[i].toLowerCase());
					}
				}
			} else {
				if(identity.getPrimaryName().getFirstName().length() >= 2) {
					identityNames.add(identity.getPrimaryName().getFirstName().toLowerCase());
				}
			}
			if(identity.getPrimaryName().getMiddleName() != null && identity.getPrimaryName().getMiddleName().length() >= 2) {
				if(identity.getPrimaryName().getMiddleName().contains(" ") || identity.getPrimaryName().getMiddleName().contains("-")) {
					String primaryName[] = identity.getPrimaryName().getMiddleName().split("\\s+|-");
					for (int i = 0; i < primaryName.length; i++) {
						if(primaryName[i].length() >= 2) {
							identityNames.add(primaryName[i].toLowerCase());
						}
					}
				} else {
					identityNames.add(identity.getPrimaryName().getMiddleName().toLowerCase());
				}
			}
		}
		
		if(identity.getAlternateNames() != null && !identity.getAlternateNames().isEmpty()) {
			for (AuthorName alternateName : identity.getAlternateNames()) {
				if(alternateName.getFirstName() != null
						&&
						(alternateName.getFirstName().contains(" ") || alternateName.getFirstName().contains("-"))) {
					String alternateNameArray[] = alternateName.getFirstName().split("\\s+|-");
					for (int i = 0; i < alternateNameArray.length; i++) {
						if(alternateNameArray[i].length() >= 2) {
							identityNames.add(alternateNameArray[i].toLowerCase());
						}
					}
				} else {
					if(alternateName.getFirstName().length() >= 2) {
						identityNames.add(alternateName.getFirstName().toLowerCase());
					}
				}
				if(alternateName.getMiddleName() != null && alternateName.getMiddleName().length() >= 2) {
					if(alternateName.getMiddleName().contains(" ") || alternateName.getMiddleName().contains("-")) {
						String alternateNameArray[] = alternateName.getMiddleName().split("\\s+|-");
						for (int i = 0; i < alternateNameArray.length; i++) {
							if(alternateNameArray[i].length() >= 2) {
								identityNames.add(alternateNameArray[i].toLowerCase());
							}
						}
					} else {
						identityNames.add(alternateName.getMiddleName().toLowerCase());
					}
				}
			}
		}
		if(!identityNames.isEmpty()
				&&
				genders != null
				&&
				!genders.isEmpty()) {
			matchingGenders = genders
				.parallelStream()
				.filter(gender ->
				identityNames.contains(gender.getName().toLowerCase())
				).collect(Collectors.toList());
			if(!matchingGenders.isEmpty()) {
				if(matchingGenders.size() > 1) {
					matchingGenders.forEach(matchGender -> {
						if(matchGender.getGender() == GenderEnum.F) {
							matchGender.setProbability(1 - matchGender.getProbability());
						}
					});
					Double avgProbability = matchingGenders.stream().mapToDouble(Gender::getProbability).average().getAsDouble();
					identity.setGender(new Gender(null, null, null, avgProbability));
				} else {
					identity.setGender(matchingGenders.get(0));
				}
			}
		}
	}
	
	/**
	 * @param reCiterArticle
	 * @return Gender match for article
	 */
	public static Gender getGenderArticleProbability(ReCiterArticle reCiterArticle) {
		List<Gender> genders = new ArrayList<Gender>();
		for(Gender gender: EngineParameters.getGenders()) {
			genders.add(new Gender(gender.getUniqueId(), gender.getName(), gender.getGender(), gender.getProbability()));
		}
		List<Gender> matchingGenders = new ArrayList<Gender>();
		if(reCiterArticle.getArticleCoAuthors().getAuthors() != null 
				&& 
				!reCiterArticle.getArticleCoAuthors().getAuthors().isEmpty()
				&&
				genders != null
				&&
				!genders.isEmpty()) {
			List<ReCiterAuthor> targetAuthorList = reCiterArticle.getArticleCoAuthors().getAuthors()
			.stream()
			.filter(reCiterAuthor -> reCiterAuthor.isTargetAuthor())
			.collect(Collectors.toList());
			
			if(targetAuthorList.isEmpty()) {
				return null;
			} else {
				if(targetAuthorList.get(0) != null && targetAuthorList.get(0).getAuthorName().getFirstName() != null) {
					if(targetAuthorList.get(0).getAuthorName().getFirstName().contains(" ") || targetAuthorList.get(0).getAuthorName().getFirstName().contains("-")) {
					String targetAuthor[] = targetAuthorList.get(0).getAuthorName().getFirstName().split("\\s+|-");
					List<String> targetAuthorSplitList = Arrays.asList(targetAuthor);
					matchingGenders = genders
							.parallelStream()
							.filter(gender ->
							targetAuthorSplitList.stream().anyMatch(split -> split.length() >= 2 &&  split.equalsIgnoreCase(gender.getName().trim())
							))
							.collect(Collectors.toList());
					} else {
						matchingGenders = genders
								.parallelStream()
								.filter(gender ->
										targetAuthorList.get(0).getAuthorName().getFirstName() != null
										&&
										targetAuthorList.get(0).getAuthorName().getFirstName().length() >= 2
										&& 
										targetAuthorList.get(0).getAuthorName().getFirstName().equalsIgnoreCase(gender.getName())).collect(Collectors.toList());
					}
				}
			}
		}
		if(!matchingGenders.isEmpty()) {
			if(matchingGenders.size() > 1) {
				matchingGenders.forEach(matchGender -> {
					if(matchGender.getGender() == GenderEnum.F) {
						matchGender.setProbability(1 - matchGender.getProbability());
					}
				});
				Double avgProbability = matchingGenders.stream().mapToDouble(Gender::getProbability).average().getAsDouble();
				return new Gender(null, null, null, avgProbability);
			} else {
				return matchingGenders.get(0);
			}
		}
		return null;
	}
}
