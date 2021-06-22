package reciter.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.NoArgsConstructor;
import reciter.engine.StrategyParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

/**
 * @author szd2013
 * <p>This class sanitizes author names for both Identity and Article.
 *  Preprocess identity.firstName, identity.middleName, and article.firstName
	If any names are in quotes or parentheses in identity.firstName or identity.middleName, pull them out so they can be matched against.
	Wing Tak "Jack" --> add "Jack" to identity.firstName, add "Wing Tak" to identity.firstName
	Qihui (Jim) --> add Jim to identity.firstName, add Qihui to identity.firstName
	Remove any periods, spaces, or dashes from both identity.firstName, identity.middleName, and article.firstName. For example:
	"Chi-chao" --> "Chichao"
	"Minh-Nhut Yvonne" --> "MinhNhutYvonne"
	"Eliot A." --> "EliotA"
	Disregard any cases where one variant of identity.firstName is a substring of another case of identity.firstName. Disregard any cases where one variant of identity.middleName is a substring of another case of identity.middleName. For example:
	"Cary" (keep) vs "C" (disregard)
	Null for middle name should only be included as possible value if none of the names or aliases contain a middle name
	A given target author might have N different first names and M different middle names.
	Go to D
	Retrieve article.lastName where targetAuthor=TRUE and all distinct cases of identity.lastName for our target author from identity. Preprocess identity.lastName and article.lastName.
	
	Remove any periods from both identity.lastName and article.lastName
	Remove any of the following endings from identity.lastName:
	", Jr"
	", MD PhD"
	", MD-PhD"
	", PhD"
	", MD"
	", III"
	", II"
	", Sr"
	" Jr"
	" MD PhD"
	" MD-PhD"
	" PhD"
	" MD"
	" III"
	" II"
	" Sr"
	Remove any periods, spaces, dashes, or quotes.
	For example:
	"Del Cole" --> "Delcole"
	"Garcia-Marquez" --> ""GarciaMarquez"
	"Capetillo Gonzalez de Zarate" --> "CapetilloGonzalezdeZarate"<p>
 *
 */
@NoArgsConstructor
public class AuthorNameSanitizationUtils {
	
	private StrategyParameters strategyParameters; 
	
	private List<String> nameExcludedSuffixes;
	
	public AuthorNameSanitizationUtils(StrategyParameters strategyParameters) {
		this.strategyParameters = strategyParameters;
	}
	
	/**
	 * @author szd2013
	 * @param reCiterArticle
	 * @return 
	 * Sanitize Article Authors
	 */
	public Map<ReCiterAuthor, ReCiterAuthor> sanitizeArticleAuthorNames(ReCiterArticle reCiterArticle) {
		Map<ReCiterAuthor, ReCiterAuthor> sanitizeArticleAuthors = new LinkedHashMap<ReCiterAuthor, ReCiterAuthor>();
		if(reCiterArticle.getArticleCoAuthors() != null && reCiterArticle.getArticleCoAuthors().getAuthors() != null) {
			for(ReCiterAuthor authorName: reCiterArticle.getArticleCoAuthors().getAuthors()) {
				AuthorName articleAuthor = new AuthorName();
				if(authorName.getAuthorName().getFirstName() != null) {
					articleAuthor.setFirstName(ReCiterStringUtil.deAccent(authorName.getAuthorName().getFirstName().replaceAll("[-.\"() ]", "")));
				}
				if(authorName.getAuthorName().getLastName() != null) {
					articleAuthor.setLastName(ReCiterStringUtil.deAccent(authorName.getAuthorName().getLastName().replaceAll("[-.\",()\\s]|(" + generateSuffixRegex() + ")$", "")));
				}
				ReCiterAuthor sanitizedReCiterAuthor = new ReCiterAuthor(articleAuthor, authorName.getAffiliation());
				sanitizedReCiterAuthor.setRank(authorName.getRank());
				sanitizedReCiterAuthor.setTargetAuthor(authorName.isTargetAuthor());
				sanitizedReCiterAuthor.setValidEmail(authorName.getValidEmail());
				sanitizeArticleAuthors.put(authorName, sanitizedReCiterAuthor);
			}
		}
		return sanitizeArticleAuthors;
	}
	
	/**
	 * Sanitize Identity Author Names and also derive author names if there is double code (") in the first name
	 * @param identity
	 * @return Sanitized Map of Identity primary names, alternate names and derived names
	 */
	public Map<AuthorName, AuthorName> sanitizeIdentityAuthorNames(Identity identity) {
		
		Map<AuthorName, AuthorName> sanitizedIdentityAuthorName = null;
		if(identity != null) {
			sanitizedIdentityAuthorName = new HashMap<AuthorName, AuthorName>();
			AuthorName identityPrimaryName = new AuthorName();
			AuthorName additionalName = new AuthorName();
			String firstName = null;
			String lastName = null;
			String middleName = null;
			//Sanitize Identity Names
			if(identity.getPrimaryName() != null) {
				
				if(identity.getPrimaryName().getFirstName() != null) {
					if(identity.getPrimaryName().getFirstName().contains("\"") || (identity.getPrimaryName().getFirstName().contains("(") && identity.getPrimaryName().getFirstName().contains(")"))) {
						firstName = identity.getPrimaryName().getFirstName().replaceAll("[-.,()\\s]", "").replaceAll("\"([^\"]*)\"|(\"([^\"]*)\")|(([a-z]*))/i/g", "");
						if(firstName !=null) {
							additionalName.setFirstName(ReCiterStringUtil.deAccent(firstName));
						}
						Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\"([^\"]*)\")|(([a-z]*))/i/g");
						Matcher matcher = pattern.matcher(identity.getPrimaryName().getFirstName());
						while(matcher.find()) {
							identityPrimaryName.setFirstName(ReCiterStringUtil.deAccent(matcher.group().replaceAll("\"", "")));
						}
					} else {
						identityPrimaryName.setFirstName(ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().replaceAll("[-.,()\\s]", "")));
					}
				}
				if(identity.getPrimaryName().getMiddleName() != null) {
					if(identity.getPrimaryName().getMiddleName().contains("\"") || (identity.getPrimaryName().getMiddleName().contains("(") && identity.getPrimaryName().getMiddleName().contains(")"))) {
						middleName = identity.getPrimaryName().getMiddleName().replaceAll("[-.,()\\s]", "").replaceAll("\"([^\"]*)\"|(\"([^\"]*)\")|(([a-z]*))/i/g", "");
						if(middleName !=null) {
							additionalName.setMiddleName(ReCiterStringUtil.deAccent(middleName));
						}
						Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\"([^\"]*)\")|(([a-z]*))/i/g");
						Matcher matcher = pattern.matcher(identity.getPrimaryName().getMiddleName());
						while(matcher.find()) {
							identityPrimaryName.setMiddleName(ReCiterStringUtil.deAccent(matcher.group().replaceAll("\"", "")));
						}
					} else {
						identityPrimaryName.setMiddleName(ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleName().replaceAll("[-.,()\\s]", "")));
					}
				}
				if(identity.getPrimaryName().getLastName() != null) {
					//lastName = identity.getPrimaryName().getLastName().replaceAll("[-.,,()\\s]|(,Jr|, Jr|, MD PhD|,MD PhD|, MD-PhD|,MD-PhD|, PhD|,PhD|, MD|,MD|, III|,III|, II|,II|, Sr|,Sr|Jr|MD PhD|MD-PhD|PhD|MD|III|II|Sr)$", "");
					lastName = identity.getPrimaryName().getLastName().replaceAll("[-.,,()\\s]|(" + generateSuffixRegex() + ")$", "");
					identityPrimaryName.setLastName(ReCiterStringUtil.deAccent(lastName));
					if(additionalName.getFirstName() != null) {
						additionalName.setLastName(ReCiterStringUtil.deAccent(lastName));
					}
				}
				if(identityPrimaryName.getLastName() != null) {
					sanitizedIdentityAuthorName.put(identity.getPrimaryName(), identityPrimaryName);
				}
			}
			
			if(identity.getAlternateNames() != null && identity.getAlternateNames().size() > 0) {
				for(AuthorName aliasAuthorName: identity.getAlternateNames()) {
					AuthorName identityAliasAuthorName = new AuthorName();
					if(aliasAuthorName.getFirstName() != null) {
						identityAliasAuthorName.setFirstName(ReCiterStringUtil.deAccent(aliasAuthorName.getFirstName().replaceAll("[-.\",()\\s]", "")));
					}
					if(aliasAuthorName.getMiddleName() != null) {
						identityAliasAuthorName.setMiddleName(ReCiterStringUtil.deAccent(aliasAuthorName.getMiddleName().replaceAll("[-.\",()\\s]", "")));
					}
					if(aliasAuthorName.getLastName() != null) {
						identityAliasAuthorName.setLastName(ReCiterStringUtil.deAccent(aliasAuthorName.getLastName().replaceAll("[-.\",()\\s]|(" + generateSuffixRegex() + ")$", "")));
					}
					
					if(identityAliasAuthorName.getLastName() != null) {
						sanitizedIdentityAuthorName.put(aliasAuthorName, identityAliasAuthorName);
					}
				}
			}
			if(additionalName != null && additionalName.getLastName() != null) {
				sanitizedIdentityAuthorName.put(additionalName, additionalName);
			}
			if(sanitizedIdentityAuthorName.size() > 1) {
				checkToIgnoreNameVariants(sanitizedIdentityAuthorName);
			}
		}
		return sanitizedIdentityAuthorName;
	}
	
	/**
	 * This function checks for if there is other name variant if another complete name exists in Identity
	 * @param idenityAuthorNames
	 */
	public void checkToIgnoreNameVariants(Map<AuthorName, AuthorName> idenityAuthorNames) {
		Set<Entry<AuthorName, AuthorName>> sanitizedIdentityAuthorNames =  idenityAuthorNames.entrySet();
		Set<Entry<AuthorName, AuthorName>> sanitizedIdentityAuthorNamesCopy = new HashSet<>(sanitizedIdentityAuthorNames);
		Iterator<Map.Entry<AuthorName,AuthorName>> copyIterator = sanitizedIdentityAuthorNamesCopy.iterator();
		while(copyIterator.hasNext()) {
			Map.Entry<AuthorName,AuthorName> i = copyIterator.next();
			Iterator<Map.Entry<AuthorName,AuthorName>> iterator = idenityAuthorNames.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<AuthorName,AuthorName> j = iterator.next();
			//for(Map.Entry<AuthorName,AuthorName> j : sanitizedIdentityAuthorNames) {
				if(!i.getValue().equals(j.getValue())) {
					if(sanitizedIdentityAuthorNames.size() > 1 
							&&
							i.getValue() != null 
							&& 
							j.getValue() != null
							&&
							i.getValue().getFirstName() != null
							&&
							j.getValue() != null
							&&
							j.getValue().getFirstName() != null
							&&
							j.getValue().getLastName() != null) {
						if(StringUtils.equalsIgnoreCase(i.getValue().getLastName(), j.getValue().getLastName()) 
								&& 
								i.getValue().getFirstName().toLowerCase().startsWith(j.getValue().getFirstName().toLowerCase())) {
							if(j.getValue().getMiddleName() == null && i.getValue().getMiddleName() == null) {
								iterator.remove();
								copyIterator.remove();
								if(iterator.hasNext()) {
									j = iterator.next(); //Avoid IllegalStateException thrown by iterator when using remove method
								}
							}
							if(j.getValue().getMiddleName() != null && j.getValue().getMiddleName().trim().isEmpty()) {								
								iterator.remove();
								copyIterator.remove();
								if(iterator.hasNext()) {
									j = iterator.next(); //Avoid IllegalStateException thrown by iterator when using remove method
								}
							}
						}
						//Case - ajdannen - Throw away Andrew J Dannenberg because Andrew Jess Dannenberg exists
						if(sanitizedIdentityAuthorNames.size() > 1) {
							if(i.getValue() != null
									&&
									j.getValue() != null
									&&
									i.getValue().getLastName() != null 
									&&
									j.getValue().getLastName() != null 
									&&
									i.getValue().getFirstName() != null 
									&&
									j.getValue().getFirstName() != null
									&&
									i.getValue().getMiddleName() != null 
									&&
									j.getValue().getMiddleName() != null
									&&
									StringUtils.equalsIgnoreCase(i.getValue().getLastName(), j.getValue().getLastName()) 
									&& 
									StringUtils.equalsIgnoreCase(i.getValue().getFirstName(), j.getValue().getFirstName()) 
									&&
									i.getValue().getMiddleName().toLowerCase().startsWith(j.getValue().getMiddleName().toLowerCase())) {
								iterator.remove();
								copyIterator.remove();
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * This function generates regex of suffix from application.properties file
	 * @return regex string
	 */
	public String generateSuffixRegex() {
		nameExcludedSuffixes = Arrays.asList(strategyParameters.getNameExcludedSuffixes().trim().split("\\s*,\\s*"));
		String suffixRegex = "";
		String suffixTogether = "";
		for(String suffix: nameExcludedSuffixes) {
			suffixRegex = suffixRegex + "," + suffix + "|, " + suffix + "|";
			suffixTogether = suffixTogether + suffix + "|";
		}
		if(suffixTogether.endsWith("|")) {
			suffixTogether = suffixTogether.substring(0, suffixTogether.length() - 1);
		}
		return suffixRegex + "," + suffixTogether;
	}
	

}
