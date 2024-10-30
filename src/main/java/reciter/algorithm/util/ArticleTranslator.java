

/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.algorithm.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import reciter.algorithm.cluster.similarity.clusteringstrategy.article.MeshMajorClusteringStrategy;
import reciter.engine.EngineParameters;
import reciter.engine.StrategyParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterArticleFeatures;
import reciter.model.article.ReCiterArticleGrant;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.article.ReCiterArticleMeshHeading;
import reciter.model.article.ReCiterAuthor;
import reciter.model.article.ReCiterCitationYNEnum;
import reciter.model.article.ReCiterJournal;
import reciter.model.article.ReCiterMeshHeadingDescriptorName;
import reciter.model.article.ReCiterMeshHeadingQualifierName;
import reciter.model.article.ReCiterPublicationTypeScopus;
import reciter.model.identity.AuthorName;
import reciter.model.pubmed.MedlineCitationArticleAuthor;
import reciter.model.pubmed.MedlineCitationCommentsCorrections;
import reciter.model.pubmed.MedlineCitationDate;
import reciter.model.pubmed.MedlineCitationGrant;
import reciter.model.pubmed.MedlineCitationJournalISSN;
import reciter.model.pubmed.MedlineCitationKeyword;
import reciter.model.pubmed.MedlineCitationKeywordList;
import reciter.model.pubmed.MedlineCitationMeshHeading;
import reciter.model.pubmed.MedlineCitationMeshHeadingQualifierName;
import reciter.model.pubmed.MedlineCitationYNEnum;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.pubmed.PubMedPubDate;
import reciter.model.scopus.Author;
import reciter.model.scopus.ScopusArticle;
import reciter.utils.AuthorNameSanitizationUtils;

/**
 * Translator that translates a PubmedArticle to ReCiterArticle.
 *
 * @author jil3004
 */
@Component
public class ArticleTranslator {

    /**
     * Translates a PubmedArticle into a ReCiterArticle.
     *
     * @param pubmedArticle
     * @return
     */
    public static ReCiterArticle translate(PubMedArticle pubmedArticle, ScopusArticle scopusArticle, String nameIgnoredCoAuthors, StrategyParameters strategyParameters) {

        // PMID
        long pmid = pubmedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid();
        ReCiterArticle reCiterArticle = new ReCiterArticle(pmid);
        
        //PMCID
        
        if(pubmedArticle.getPubmeddata() != null && 
        		pubmedArticle.getPubmeddata().getArticleIdList() != null &&
        		pubmedArticle.getPubmeddata().getArticleIdList().getPmc() != null) {
        	reCiterArticle.setPmcid(pubmedArticle.getPubmeddata().getArticleIdList().getPmc());
        }

        // Article title
        String articleTitle = pubmedArticle.getMedlinecitation().getArticle().getArticletitle();

        // Journal Title
        String journalTitle = pubmedArticle.getMedlinecitation().getArticle().getJournal().getTitle();
        
        //Pubmed Publication Type
        if(pubmedArticle.getMedlinecitation().getArticle().getPublicationtypelist() != null && !pubmedArticle.getMedlinecitation().getArticle().getPublicationtypelist().isEmpty()) {
	        List<String> publicationTypePubmed = pubmedArticle.getMedlinecitation().getArticle().getPublicationtypelist().stream().map(pubType -> pubType.getPublicationtype()).collect(Collectors.toList());
	        
	        reCiterArticle.setPublicationTypePubmed(publicationTypePubmed);
        }
        
        if(pubmedArticle.getMedlinecitation().getArticle().getPublicationAbstract() != null && 
        		pubmedArticle.getMedlinecitation().getArticle().getPublicationAbstract().getAbstractTexts() != null && !pubmedArticle.getMedlinecitation().getArticle().getPublicationAbstract().getAbstractTexts().isEmpty()) {
	        String pubmedPublicationAbstract = pubmedArticle.getMedlinecitation().getArticle().getPublicationAbstract().getAbstractTexts()
	        		.stream()
	        		.map(pubAbstract -> ((pubAbstract.getAbstractTextLabel() != null)?pubAbstract.getAbstractTextLabel() + ": ":"") + pubAbstract.getAbstractText()).collect(Collectors.joining(" "));
	        reCiterArticle.setPublicationAbstract(pubmedPublicationAbstract);
        }
        
        
        
        

        List<MedlineCitationJournalISSN> journalIssn = pubmedArticle.getMedlinecitation().getArticle().getJournal().getIssn();

        // Translating Journal Issue PubDate Year.
        int journalIssuePubDateYear = Integer.parseInt(pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getPubdate().getYear());

        // Co-authors
        List<MedlineCitationArticleAuthor> coAuthors = pubmedArticle.getMedlinecitation().getArticle().getAuthorlist();

        // Translating Co-Authors
        int i = 1;
        ReCiterArticleAuthors reCiterCoAuthors = new ReCiterArticleAuthors();
        if (coAuthors != null) {
            for (MedlineCitationArticleAuthor author : coAuthors) {
                String lastName = author.getLastname();
                String foreName = author.getForename();
                String initials = author.getInitials();
                String firstName = null;
                String middleName = null;

                // PubMed sometimes concatenates the first name and middle initial into <ForeName> xml tag.
                // This extracts the first name and middle initial.

                // Sometimes forename doesn't exist in XML (ie: 8661541). So initials are used instead.
                // Forename take precedence. If foreName doesn't exist, use initials. If initials doesn't exist, use null.
                // TODO: Deal with collective names in XML.
                if (lastName != null) {
                    if (foreName != null) {
                        //String[] foreNameArray = foreName.split("\\s+");
                        //if (foreNameArray.length == 2) {
                        //	firstName = foreNameArray[0];
                        //	middleName = foreNameArray[1];
                        //} else {
                        //	firstName = foreName;
                        //}
                        firstName = foreName;
                    } else if (initials != null) {
                        firstName = initials;
                    }
                    String affiliation = author.getAffiliation();
                    AuthorName authorName = new AuthorName(firstName, middleName, lastName);

                    ReCiterAuthor reCiterAuthor = new ReCiterAuthor(authorName, affiliation);
                    reCiterAuthor.setRank(i++);
                    reCiterAuthor.setOrcid(author.getOrcid());
                    reCiterAuthor.setEqualContrib(author.getEqualContrib());
                    reCiterCoAuthors.addAuthor(reCiterAuthor);
                }
            }
        }

        MedlineCitationKeywordList keywordList = pubmedArticle.getMedlinecitation().getKeywordlist();

        // Translating Keywords.
        ReCiterArticleKeywords articleKeywords = new ReCiterArticleKeywords();
        if (keywordList != null) {
            for (MedlineCitationKeyword keyword : keywordList.getKeywordlist()) {
                articleKeywords.addKeyword(keyword.getKeyword());
            }
        }

        List<MedlineCitationMeshHeading> meshList = pubmedArticle.getMedlinecitation().getMeshheadinglist();
        List<ReCiterArticleMeshHeading> reCiterArticleMeshHeadings = new ArrayList<>();
        if (meshList != null) {
            // Translating Mesh
            for (MedlineCitationMeshHeading medlineCitationMeshHeading : meshList) {
                String descriptorNameString = medlineCitationMeshHeading.getDescriptorname().getDescriptorname();
                MedlineCitationYNEnum meshMajorTopicYN = medlineCitationMeshHeading.getDescriptorname().getMajortopicyn();
                List<MedlineCitationMeshHeadingQualifierName> medlineCitationMeshHeadingQualifierNames =
                        medlineCitationMeshHeading.getQualifiernamelist();

                List<ReCiterMeshHeadingQualifierName> reCiterMeshHeadingQualifierNames =
                        new ArrayList<>(medlineCitationMeshHeadingQualifierNames.size());

                // Set descriptor name and major topic.
                ReCiterArticleMeshHeading reCiterArticleMeshHeading = new ReCiterArticleMeshHeading();
                ReCiterMeshHeadingDescriptorName reCiterMeshHeadingDescriptorName = new ReCiterMeshHeadingDescriptorName();
                reCiterMeshHeadingDescriptorName.setDescriptorName(descriptorNameString);
                reCiterMeshHeadingDescriptorName.setMajorTopicYN(meshMajorTopicYN.getVal());
                reCiterArticleMeshHeading.setDescriptorName(reCiterMeshHeadingDescriptorName);

                // For each qualifier, set name and major topic.
                for (MedlineCitationMeshHeadingQualifierName medlineCitationMeshHeadingQualifierName : medlineCitationMeshHeadingQualifierNames) {
                    ReCiterMeshHeadingQualifierName reCiterMeshHeadingQualifierName = new ReCiterMeshHeadingQualifierName();
                    reCiterMeshHeadingQualifierName.setQualifierName(medlineCitationMeshHeadingQualifierName.getQualifiername());

                    ReCiterCitationYNEnum e;
                    if ("Y".equals(medlineCitationMeshHeadingQualifierName.getMajortopicyn().getVal())) {
                        e = ReCiterCitationYNEnum.Y;
                    } else {
                        e = ReCiterCitationYNEnum.N;
                    }
                    reCiterMeshHeadingQualifierName.setMajorTopicYN(e);
                    reCiterMeshHeadingQualifierNames.add(reCiterMeshHeadingQualifierName);
                }

                reCiterArticleMeshHeading.setQualifierNameList(reCiterMeshHeadingQualifierNames);
                reCiterArticleMeshHeadings.add(reCiterArticleMeshHeading);
            }
        }

        reCiterArticle.setArticleTitle(articleTitle);
        reCiterArticle.setJournal(new ReCiterJournal(journalTitle));
        reCiterArticle.setArticleCoAuthors(reCiterCoAuthors);
        reCiterArticle.setArticleKeywords(articleKeywords);
        reCiterArticle.getJournal().setJournalIssuePubDateYear(journalIssuePubDateYear);
        reCiterArticle.getJournal().setJournalIssn(journalIssn);
        reCiterArticle.getJournal().setIsoAbbreviation(pubmedArticle.getMedlinecitation().getArticle().getJournal().getIsoAbbreviation());
        reCiterArticle.setMeshHeadings(reCiterArticleMeshHeadings);

        // Use PubMed Article date as the publication date (pubdate)
        // TODO optimize date (single date across ReCiter files) (Tech Debt)
        MedlineCitationDate medlineCitationDate = pubmedArticle.getMedlinecitation().getArticle().getArticledate();
        if (medlineCitationDate != null && medlineCitationDate.getYear() != null) {
        	String articleDateMonth = null;
        	String articleDateDay = null;
        	if(medlineCitationDate.getMonth()== null) {
        		articleDateMonth = "01";
        	} else {
        		articleDateMonth = medlineCitationDate.getMonth();
        	}
        	
        	if(medlineCitationDate.getDay()== null) {
        		articleDateDay = "01";
        	} else {
        		articleDateDay = medlineCitationDate.getDay();
        	}
            LocalDate localDate = new LocalDate(Integer.parseInt(medlineCitationDate.getYear()),
                    Integer.parseInt(articleDateMonth), // convert JUL to 7
                    Integer.parseInt(articleDateDay));
            Date date = localDate.toDate();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            reCiterArticle.setPublicationDateStandardized(df.format(date));
            df = new SimpleDateFormat("yyyy MMM dd");
            reCiterArticle.setPublicationDateDisplay(df.format(date));
        }
        
        //Case when ArticleDate does not exist use PubDate as date standardized date
        if (reCiterArticle.getPublicationDateStandardized() == null
        		&&
        		pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getPubdate() != null) {
        	medlineCitationDate = pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getPubdate();
        	String articleDateMonth = null;
        	String articleDateDay = null;
        	if(medlineCitationDate.getMonth()== null) {
        		articleDateMonth = "01";
        	} else {
        		articleDateMonth = medlineCitationDate.getMonth();
        	}
        	
        	if(medlineCitationDate.getDay()== null) {
        		articleDateDay = "01";
        	} else {
        		articleDateDay = medlineCitationDate.getDay();
        	}
        	LocalDate localDate = new LocalDate(Integer.parseInt(medlineCitationDate.getYear()),
                    Integer.parseInt(articleDateMonth), // convert JUL to 7
                    Integer.parseInt(articleDateDay));
            Date date = localDate.toDate();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            reCiterArticle.setPublicationDateStandardized(df.format(date));
            
            if(reCiterArticle.getPublicationDateDisplay()== null) {
            	df = new SimpleDateFormat("yyyy MMM dd");
                reCiterArticle.setPublicationDateDisplay(df.format(date));
            }
        }
        
        //Populate datePublicationAddedToEntrez if it exits in pubmed
        if(pubmedArticle.getPubmeddata() != null
        		&&
        		pubmedArticle.getPubmeddata().getHistory() != null
        		&&
        		pubmedArticle.getPubmeddata().getHistory().getPubmedPubDate() != null
        		&& pubmedArticle.getPubmeddata().getHistory().getPubmedPubDate().size() > 0) {
        	PubMedPubDate pubmedPubDateEntrez =  pubmedArticle.getPubmeddata().getHistory().getPubmedPubDate().
        			stream().filter(pubmedPubDate -> pubmedPubDate.getPubStatus().equalsIgnoreCase("entrez")).
        			findAny().orElse(null);
        	if(pubmedPubDateEntrez != null) {
        		String entrezDateMonth = null;
            	String entrezDateDay = null;
            	if(pubmedPubDateEntrez.getPubMedPubDate().getMonth() == null) {
            		entrezDateMonth = "01";
            	} else {
            		entrezDateMonth = pubmedPubDateEntrez.getPubMedPubDate().getMonth();
            	}
            	
            	if(pubmedPubDateEntrez.getPubMedPubDate().getDay() == null) {
            		entrezDateDay = "01";
            	} else {
            		entrezDateDay = pubmedPubDateEntrez.getPubMedPubDate().getDay();
            	}
        		LocalDate localDate = new LocalDate(Integer.parseInt(pubmedPubDateEntrez.getPubMedPubDate().getYear()),
                        Integer.parseInt(entrezDateMonth), // convert JUL to 7
                        Integer.parseInt(entrezDateDay));
                Date date = localDate.toDate();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        		reCiterArticle.setDatePublicationAddedToEntrez(df.format(date));
        	}
        }
        		
        

        // Update PubMed's authors' first name from Scopus Article. Logic is as follows:
        // 1. First compare last name if match:
        // 2. Check scopus's first name has length > 1, so no initials (b/c PubMed already contains this info.)
        // 3. Check first initial is same.
        // 4. Check that scopus author's first name is more "complete" than Pubmed's author name.
        // 5. Only update if PubMed's author name is length = 1.
        // 6. Sanitization: remove periods and whitespaces. Grab only the first name (Scopus also provides middle initial).

        if (scopusArticle != null) {
            for (Author scopusAuthor : scopusArticle.getAuthors()) {
                String scopusAuthorFirstName = scopusAuthor.getGivenName();
                String scopusAuthorLastName = scopusAuthor.getSurname();
                for (ReCiterAuthor reCiterAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {
                    String reCiterAuthorLastName = reCiterAuthor.getAuthorName().getLastName();
                    if (StringUtils.equalsIgnoreCase(scopusAuthorLastName, reCiterAuthorLastName)) {
                        String reCiterAuthorFirstName = reCiterAuthor.getAuthorName().getFirstName();
                        String reCiterAuthorFirstInitial = reCiterAuthor.getAuthorName().getFirstInitial();
                        if (scopusAuthorFirstName != null && scopusAuthorFirstName.length() > 1) {
                            if (scopusAuthorFirstName.substring(0, 1).equals(reCiterAuthorFirstInitial)) {
                                if (scopusAuthorFirstName.length() > reCiterAuthorFirstName.length()) {

                                    if (reCiterAuthorFirstName.length() == 1) {

                                        scopusAuthorFirstName = scopusAuthorFirstName.replaceAll("[\\.]", "");
                                        int indexOfWhiteSpace = scopusAuthorFirstName.indexOf(" "); // index should be calculated here because scopusFirstName is updated.
                                        // i.e. If scopusAuthorFirstName = "A. J.", indexOfWhiteSpace would be 2, but it should be 1 after the scopusAuthorFirstName is trimmed.
                                        if (indexOfWhiteSpace == -1) {
                                            reCiterAuthor.getAuthorName().setFirstName(scopusAuthorFirstName);
                                        } else {
                                            reCiterAuthor.getAuthorName().setFirstName(scopusAuthorFirstName.substring(0, indexOfWhiteSpace));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            //Scopus Publication Type
            if(scopusArticle.getSubTypeDescription() != null && scopusArticle.getSubType() != null) {
            	reCiterArticle.setPublicationTypeScopus(ReCiterPublicationTypeScopus.builder().publicationTypeScopusAbbreviation(scopusArticle.getSubType()).publicationTypeScopusLabel(scopusArticle.getSubTypeDescription()).build());
            }
            //Times Cited
            if(scopusArticle.getCitedByCount() > 0) {
            	reCiterArticle.setTimesCited(scopusArticle.getCitedByCount());
            }
            if(scopusArticle.getScopusDocId() != null) {
            	reCiterArticle.setScopusDocId(scopusArticle.getScopusDocId());
            }
        }
        reCiterArticle.setScopusArticle(scopusArticle);

        // Grant lists
        List<MedlineCitationGrant> medlineCitationGrants = pubmedArticle.getMedlinecitation().getArticle().getGrantlist();
        List<ReCiterArticleGrant> reCiterArticleGrants = new ArrayList<>();
        if (medlineCitationGrants != null) {
            for (MedlineCitationGrant medlineCitationGrant : medlineCitationGrants) {
                ReCiterArticleGrant reCiterArticleGrant = new ReCiterArticleGrant();
                reCiterArticleGrant.setAcronym(medlineCitationGrant.getAcronym());
                reCiterArticleGrant.setAgency(medlineCitationGrant.getAgency());
                reCiterArticleGrant.setCountry(medlineCitationGrant.getCountry());
                reCiterArticleGrant.setGrantID(medlineCitationGrant.getGrantid());
                reCiterArticleGrants.add(reCiterArticleGrant);
            }
        }
        reCiterArticle.setGrantList(reCiterArticleGrants);
        
        determinePublicationTypeCanonical(reCiterArticle, scopusArticle);

        // translate the CommentsCorrections.

        if (pubmedArticle.getMedlinecitation().getCommentscorrectionslist() != null) {
            Set<Long> commentsCorrectionsPmids = new HashSet<>();
            List<MedlineCitationCommentsCorrections> commentsCorrectionsList = pubmedArticle.getMedlinecitation().getCommentscorrectionslist();
            for (MedlineCitationCommentsCorrections medlineCitationCommentsCorrections : commentsCorrectionsList) {
                if(medlineCitationCommentsCorrections.getPmid() != null)
                    commentsCorrectionsPmids.add(Long.parseLong(medlineCitationCommentsCorrections.getPmid()));
            }
            reCiterArticle.setCommentsCorrectionsPmids(commentsCorrectionsPmids);
        }

        // Volume
        if (pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getVolume() != null &&
                !pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getVolume().isEmpty()) {
            reCiterArticle.setVolume(pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getVolume());
        }
        // issue
        if (pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getIssue() != null &&
                !pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getIssue().isEmpty()) {
            reCiterArticle.setIssue(pubmedArticle.getMedlinecitation().getArticle().getJournal().getJournalissue().getIssue());
        }

        // pages
        if (pubmedArticle.getMedlinecitation().getArticle().getPagination() != null &&
                pubmedArticle.getMedlinecitation().getArticle().getPagination().getMedlinepgns() != null &&
                !pubmedArticle.getMedlinecitation().getArticle().getPagination().getMedlinepgns().isEmpty()) {
            reCiterArticle.setPages(pubmedArticle.getMedlinecitation().getArticle().getPagination().getMedlinepgns().get(0));
        }

        // doi
        if (pubmedArticle.getMedlinecitation().getArticle().getElocationid() != null) {
            reCiterArticle.setDoi(pubmedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid());
        }
        
        populateFeatures(reCiterArticle, nameIgnoredCoAuthors);
        
      //Add sanitized Author
	  AuthorNameSanitizationUtils authorNameSanitizationUtils= new AuthorNameSanitizationUtils(strategyParameters);
	  Map<ReCiterAuthor, ReCiterAuthor> sanitizedAuthorMap = authorNameSanitizationUtils.sanitizeArticleAuthorNames(reCiterArticle);
	  reCiterArticle.getArticleCoAuthors().setSanitizedAuthorMap(sanitizedAuthorMap);

        return reCiterArticle;
    }
    
    private static void determinePublicationTypeCanonical(ReCiterArticle reCiterArticle, ScopusArticle scopusArticle) {
    	String publicationTypeCanonical = null;

        if(publicationTypeCanonical == null && reCiterArticle.getPublicationTypePubmed() != null) {
            if(reCiterArticle.getPublicationTypePubmed().contains("Editorial")) {
                publicationTypeCanonical = "Editorial Article";
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Letter")) {
                publicationTypeCanonical = "Letter";
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Comment")) {
                publicationTypeCanonical = "Comment";
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Preprint")) {
                  publicationTypeCanonical = "Preprint";
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Published Erratum")) {
                  publicationTypeCanonical = "Erratum";                
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Consensus Development Conference")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Consensus Development Conference, NIH")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Address")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Clinical Conference")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Congress")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Lecture")) {
                publicationTypeCanonical = "Conference Paper";
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Guideline")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Practice Guideline")) {
                publicationTypeCanonical = "Guideline";
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Meta-Analysis")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Review")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Classical Article")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Scientific Integrity Review")) {
                publicationTypeCanonical = "Review";
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Case Reports")) {
                publicationTypeCanonical = "Case Report";
            } else if(reCiterArticle.getPublicationTypePubmed().contains("Journal Article")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Clinical Trial, Phase I")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Clinical Trial, Phase II")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Clinical Trial, Phase III")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Clinical Trial, Phase IV")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Controlled Clinical Trial")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Randomized Controlled Trial")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Multicenter Study")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Twin Study")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Validation Study")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Controlled Clinical Trial")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Pragmatic Clinical Trial")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Clinical Study")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Clinical Trial Protocol")
                    ||
                    reCiterArticle.getPublicationTypePubmed().contains("Comparative Study")
                    ||
					reCiterArticle.getPublicationTypePubmed().contains("Clinical Trial")                    
            		||
                    reCiterArticle.getPublicationTypePubmed().contains("Technical Report")) {
                publicationTypeCanonical = "Academic Article";
            } else {
                publicationTypeCanonical = "Article";
            }
        }

/*

## The below code uses publication type data from Scopus to infer publication type. Scopus has its virtues but it is commented out 
## for now so that we rely on PubMed exclusively. The reasoning is:
## 1. PubMed is freely available
## 2. More commonly used, and 
## 3. Less aggressive about mapping publications to what they call "articles" and what we would map to "academic articles."

    	if(reCiterArticle.getPublicationTypeScopus() != null && scopusArticle.getSubType() != null) {
    		if(scopusArticle.getSubType().equalsIgnoreCase("cp")
    				||
    				(reCiterArticle.getArticleTitle() != null && reCiterArticle.getArticleTitle().contains("Consensus Development Conference"))
    				||
    				(reCiterArticle.getArticleTitle() != null && reCiterArticle.getArticleTitle().contains("Consensus Development Conference, NIH"))
    				||
    				(reCiterArticle.getArticleTitle() != null && reCiterArticle.getArticleTitle().contains("Address"))
    				||
    				(reCiterArticle.getArticleTitle() != null && reCiterArticle.getArticleTitle().contains("Clinical Conference"))
    				||
    				(reCiterArticle.getArticleTitle() != null && reCiterArticle.getArticleTitle().contains("Congress"))
    				||
    				(reCiterArticle.getArticleTitle() != null && reCiterArticle.getArticleTitle().contains("Lecture"))) {
    			publicationTypeCanonical = "Conference Paper";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("re")) {
    			publicationTypeCanonical = "Review";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("rp")) {
    			publicationTypeCanonical = "Report";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("ch")) {
    			publicationTypeCanonical = "Chapter";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("ed")) {
    			publicationTypeCanonical = "Editorial Article";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("ip")) {
    			publicationTypeCanonical = "In Process";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("bk")) {
    			publicationTypeCanonical = "Book";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("le")) {
    			publicationTypeCanonical = "Letter";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("no")) {
    			publicationTypeCanonical = "Comment";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("ar")) {
    			publicationTypeCanonical = "Academic Article";
    		} else if(scopusArticle.getSubType().equalsIgnoreCase("ab") || scopusArticle.getSubType().equalsIgnoreCase("bz") || scopusArticle.getSubType().equalsIgnoreCase("cr") || scopusArticle.getSubType().equalsIgnoreCase("sh")) {
    			publicationTypeCanonical = "Article";
    		}
    	}
*/        
    	reCiterArticle.setPublicationTypeCanonical(publicationTypeCanonical);
    }
	
    private static void populateFeatures(ReCiterArticle reCiterArticle, String nameIgnoredCoAuthors) {
    	List<String> ignoredCoAuthorNames = Arrays.asList(nameIgnoredCoAuthors.trim().split("\\s*,\\s*"));
    	ReCiterArticleFeatures reCiterArticleFeatures = new ReCiterArticleFeatures();
    	int featureCount = reCiterArticleFeatures.getFeatureCount();
    	
    	//Journal Feature Name
		if (reCiterArticle.getJournal().exist()) {
			reCiterArticleFeatures.setJournalName(reCiterArticle.getJournal().getJournalTitle());
			featureCount++;
		}
		
		//Co-Author Feature Name
		if (reCiterArticle.getArticleCoAuthors().exist()) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				if(author != null && author.getAuthorName() != null &&
						author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getLastName() != null && !author.isTargetAuthor() 
						/*&&
					(
					(!author.getAuthorName().getFirstInitial().equals("Y") && !author.getAuthorName().getLastName().equals("Wang"))
					||
					(!author.getAuthorName().getFirstInitial().equals("J") && !author.getAuthorName().getLastName().equals("Wang"))	
					||
					(!author.getAuthorName().getFirstInitial().equals("J") && !author.getAuthorName().getLastName().equals("Smith"))
					||
					(!author.getAuthorName().getFirstInitial().equals("S") && !author.getAuthorName().getLastName().equals("Kim"))
					||
					(!author.getAuthorName().getFirstInitial().equals("S") && !author.getAuthorName().getLastName().equals("Lee"))
					||
					(!author.getAuthorName().getFirstInitial().equals("J") && !author.getAuthorName().getLastName().equals("Lee"))
					)*/
					) {
					boolean addCoAuthor = true;
					for(String ignoredCoAuthorName: ignoredCoAuthorNames) {
						String[] nameArray = ignoredCoAuthorName.split(" ");
						if(nameArray[1] != null && !nameArray[1].isEmpty() 
								&&
								author.getAuthorName().getFirstInitial().equalsIgnoreCase(nameArray[1])
								&&
								nameArray[0] != null && !nameArray[0].isEmpty()
								&&
								author.getAuthorName().getLastName().equalsIgnoreCase(nameArray[0])) { 
							addCoAuthor = false;
							break;
						}
					}
					if(addCoAuthor) {
						reCiterArticleFeatures.getCoAuthors().add(author.getAuthorName().getFirstInitial() + "." + author.getAuthorName().getLastName());
					}
				}
			}
			if(!reCiterArticleFeatures.getCoAuthors().isEmpty()) {
				featureCount = featureCount + reCiterArticleFeatures.getCoAuthors().size();
			}
		}
		
		//MeshMajor Feature
		if(reCiterArticle.getMeshHeadings() != null && !reCiterArticle.getMeshHeadings().isEmpty()) {
			for(ReCiterArticleMeshHeading meshHeading: reCiterArticle.getMeshHeadings()) {
				if(MeshMajorClusteringStrategy.isMeshMajor(meshHeading) && EngineParameters.getMeshCountMap() != null && EngineParameters.getMeshCountMap().containsKey(meshHeading.getDescriptorName().getDescriptorName()) &&
						EngineParameters.getMeshCountMap().get(meshHeading.getDescriptorName().getDescriptorName()) < 100000L) {
					reCiterArticleFeatures.getMeshMajor().add(meshHeading.getDescriptorName().getDescriptorName());
				}
			}
			if(!reCiterArticleFeatures.getMeshMajor().isEmpty()) {
				featureCount = featureCount + reCiterArticleFeatures.getMeshMajor().size();
			}
		}
		
		if(reCiterArticle.getScopusArticle() != null && reCiterArticle.getScopusArticle().getAuthors().size() == reCiterArticle.getArticleCoAuthors().getNumberOfAuthors()) {
			int i = 0;
			for(ReCiterAuthor author: reCiterArticle.getArticleCoAuthors().getAuthors()) {
				/*if(author.isTargetAuthor()) {
					break;
				}*/
				Author scopusAuthor = reCiterArticle.getScopusArticle().getAuthors().get(i);
				if(scopusAuthor != null && scopusAuthor.getAfids() != null) {
					reCiterArticleFeatures.getAffiliationIds().addAll(scopusAuthor.getAfids());
				}
				i++;			
			}
			if(!reCiterArticleFeatures.getAffiliationIds().isEmpty()) {
				featureCount = featureCount + reCiterArticleFeatures.getAffiliationIds().size();
			}
			
		}
		reCiterArticleFeatures.setFeatureCount(featureCount);
		reCiterArticle.setReCiterArticleFeatures(reCiterArticleFeatures);
    			
    }
}
