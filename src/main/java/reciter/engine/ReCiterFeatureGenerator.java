package reciter.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.cluster.similarity.clusteringstrategy.article.MeshMajorClusteringStrategy;
import reciter.api.parameters.UseGoldStandard;
import reciter.engine.analysis.ReCiterArticleAffiliationFeature;
import reciter.engine.analysis.ReCiterArticleAuthorFeature;
import reciter.engine.analysis.ReCiterArticleFeature;
import reciter.engine.analysis.ReCiterArticleFeature.ArticleKeyword;
import reciter.engine.analysis.ReCiterArticleFeature.ArticleKeyword.KeywordType;
import reciter.engine.analysis.ReCiterArticleFeature.PublicationFeedback;
import reciter.engine.analysis.ReCiterArticlePublicationType;
import reciter.engine.analysis.ReCiterFeature;
import reciter.engine.analysis.ReCiterFeatureCluster;
import reciter.engine.analysis.ReCiterArticleAffiliationFeature.ReCiterArticleAffiliationInstitution;
import reciter.engine.analysis.evidence.Evidence;
import reciter.engine.analysis.evidence.AffiliationEvidence.InstitutionalAffiliationSource;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleGrant;
import reciter.model.article.ReCiterArticleMeshHeading;
import reciter.model.article.ReCiterAuthor;
import reciter.model.article.ReCiterCitedBy;
import reciter.model.article.ReCiterCites;
import reciter.model.identity.Identity;
import reciter.model.scopus.Affiliation;
import reciter.model.scopus.Author;

@Data
@Slf4j
public class ReCiterFeatureGenerator {

    private double precision;
    private double recall;

    private int truePos;
    private int trueNeg;
    private int falseNeg;
    private int falsePos;
    private int goldStandardSize;
    private int selectedClusterSize;
    private List<Long> truePositiveList = new ArrayList<>();
    private List<Long> trueNegativeList = new ArrayList<>();
    private List<Long> falsePositiveList = new ArrayList<>();
    private List<Long> falseNegativeList = new ArrayList<>();

    public ReCiterFeature computeFeatures(UseGoldStandard mode,
                                          final double filterScore,
                                          final double keywordsMax,
                                          Clusterer reCiterClusterer,
                                          List<Long> goldStandardPmids,
                                          List<Long> rejectedPmids) {
        Map<Long, ReCiterCluster> finalCluster = reCiterClusterer.getClusters();
        //Select Filter to filter by total score
        
        //Set<Long> selection = clusterSelector.getSelectedClusterIds();
        Identity identity = reCiterClusterer.getIdentity();
        List<Long> finalArticles = reCiterClusterer.getReCiterArticles().stream().map(article -> article.getArticleId()).collect(Collectors.toList());

        ReCiterFeature reCiterFeature = new ReCiterFeature();
        reCiterFeature.setPersonIdentifier(identity.getUid());
        reCiterFeature.setDateAdded(new Date()); // TODO Add 'date_added' to identity.
        reCiterFeature.setDateUpdated(new Date()); // TODO add 'date_updated' to identity.
        reCiterFeature.setMode(mode);

        Set<Long> pmidsRetrieved = new HashSet<>();
        for (ReCiterCluster reCiterCluster : finalCluster.values()) {
            for (ReCiterArticle reCiterArticle : reCiterCluster.getArticleCluster()) {
                pmidsRetrieved.add(reCiterArticle.getArticleId());
            }
        }

       

        // in gold standard but not retrieved TODO optimize
        List<Long> inGoldStandardButNotRetrieved = new ArrayList<>();
        if(goldStandardPmids != null && goldStandardPmids.size() > 0) {
	        for (long pmid : goldStandardPmids) {
	            if (!pmidsRetrieved.contains(pmid)) {
	                inGoldStandardButNotRetrieved.add(pmid);
	            }
	        }
        }
        if(rejectedPmids != null && rejectedPmids.size() > 0) {
	        for (long pmid : rejectedPmids) {
	            if (!pmidsRetrieved.contains(pmid)) {
	                inGoldStandardButNotRetrieved.add(pmid);
	            }
	        }
        }
        reCiterFeature.setInGoldStandardButNotRetrieved(inGoldStandardButNotRetrieved);
        List<ReCiterArticle> selectedArticles = new ArrayList<>();
        if(mode == UseGoldStandard.AS_EVIDENCE) {
        	selectedArticles = reCiterClusterer.getReCiterArticles()
        			.stream()
        			.filter(reCiterArticle -> reCiterArticle.getTotalArticleScoreStandardized() >= filterScore || reCiterArticle.getGoldStandard() == 1 || reCiterArticle.getGoldStandard() == -1)
        			.collect(Collectors.toList());
        } else {
        	selectedArticles = reCiterClusterer.getReCiterArticles()
        			.stream()
        			.filter(reCiterArticle -> reCiterArticle.getTotalArticleScoreStandardized() >= filterScore)
        			.collect(Collectors.toList());
        }

        reCiterFeature.setCountSuggestedArticles(selectedArticles.size());

        // Count of pending publications
        List<ReCiterArticle> pendingArticles = selectedArticles
                .stream()
                .filter(reCiterArticle -> reCiterArticle.getTotalArticleScoreStandardized() >= filterScore && reCiterArticle.getGoldStandard() == 0)
                .collect(Collectors.toList());
        reCiterFeature.setCountPendingArticles(pendingArticles.size());
        
        List<Long> filteredArticles = selectedArticles.stream().map(article -> article.getArticleId()).collect(Collectors.toList());
        
        Analysis analysis = Analysis.performAnalysis(finalArticles, filteredArticles, goldStandardPmids);
        
        log.info("Analysis for uid=[" + identity.getUid() + "]");
        log.info("Precision=" + analysis.getPrecision());
        log.info("Recall=" + analysis.getRecall());
        
        // Calculate accuracy using the new formula
        double accuracy = (double)(analysis.getTruePositiveList().size() + analysis.getTrueNegativeList().size()) / (double)(analysis.getTruePositiveList().size() + analysis.getTrueNegativeList().size() + analysis.getFalsePositiveList().size() + analysis.getFalseNegativeList().size());
        
        log.info("Accuracy=" + accuracy);
        
        log.info("True Positive List [" + analysis.getTruePositiveList().size() + "]: " + analysis.getTruePositiveList());
        log.info("True Negative List: [" + analysis.getTrueNegativeList().size() + "]: " + analysis.getTrueNegativeList());
        log.info("False Positive List: [" + analysis.getFalsePositiveList().size() + "]: " + analysis.getFalsePositiveList());
        log.info("False Negative List: [" + analysis.getFalseNegativeList().size() + "]: " + analysis.getFalseNegativeList());
        log.info("\n");
        
        // Set overall accuracy using the new formula
        reCiterFeature.setOverallAccuracy(accuracy);

        // precision
        reCiterFeature.setPrecision(analysis.getPrecision());

        // recall
        reCiterFeature.setRecall(analysis.getRecall());

        // "suggestedArticles"
        List<ReCiterArticleFeature> reCiterArticleFeatures = new ArrayList<>(selectedArticles.size());
        for (ReCiterArticle reCiterArticle : selectedArticles) {
            ReCiterArticleFeature reCiterArticleFeature = new ReCiterArticleFeature();
            reCiterArticleFeature.setPmid(reCiterArticle.getArticleId());
            reCiterArticleFeature.setTotalArticleScoreNonStandardized(reCiterArticle.getTotalArticleScoreNonStandardized());
            reCiterArticleFeature.setTotalArticleScoreStandardized(reCiterArticle.getTotalArticleScoreStandardized());
            // true; false; null. make it Boolean
            // userAssertion TODO get from DB
            //if(goldStandardPmids != null && goldStandardPmids.contains(reCiterArticle.getArticleId())) {
            if (reCiterArticle.getGoldStandard() == 1) {
                reCiterArticleFeature.setUserAssertion(PublicationFeedback.ACCEPTED);
            } //else if(rejectedPmids != null && rejectedPmids.contains(reCiterArticle.getArticleId())) {
            else if (reCiterArticle.getGoldStandard() == -1) {
                reCiterArticleFeature.setUserAssertion(PublicationFeedback.REJECTED);
            } else if (reCiterArticle.getGoldStandard() == 0) {
                reCiterArticleFeature.setUserAssertion(PublicationFeedback.NULL);
            }

            //publicationDateDisplay
            reCiterArticleFeature.setPublicationDateDisplay(reCiterArticle.getPublicationDateDisplay());
            
            //publicationDateStandardized
            reCiterArticleFeature.setPublicationDateStandardized(reCiterArticle.getPublicationDateStandardized());
            
            //datePublicationAddedToEntrez
            if(reCiterArticle.getDatePublicationAddedToEntrez() != null) {
            	reCiterArticleFeature.setDatePublicationAddedToEntrez(reCiterArticle.getDatePublicationAddedToEntrez());
            }
            
            //Publication type
            ReCiterArticlePublicationType reCiterPublicationType = ReCiterArticlePublicationType.builder().build();
            
            if(reCiterArticle.getPublicationTypeCanonical() != null) {
            	reCiterPublicationType.setPublicationTypeCanonical(reCiterArticle.getPublicationTypeCanonical());
            }
            if(reCiterArticle.getPublicationTypePubmed() != null) {
            	reCiterPublicationType.setPublicationTypePubMed(reCiterArticle.getPublicationTypePubmed());
            }
            if(reCiterArticle.getPublicationTypeScopus() != null) {
            	reCiterPublicationType.setPublicationTypeScopus(reCiterArticle.getPublicationTypeScopus());
            }
            
            reCiterArticleFeature.setPublicationType(reCiterPublicationType);
            
            //times cited
            if(reCiterArticle.getTimesCited() != null) {
            	reCiterArticleFeature.setTimesCited(reCiterArticle.getTimesCited());
            }
            
            //abstract
            if(reCiterArticle.getPublicationAbstract() != null) {
            	reCiterArticleFeature.setPublicationAbstract(reCiterArticle.getPublicationAbstract());
            }
            
            //article keywords
            if(reCiterArticle.getMeshHeadings() != null && !reCiterArticle.getMeshHeadings().isEmpty()) {
                List<ReCiterArticleFeature.ArticleKeyword> articleKeywords = new ArrayList<>();
                for (ReCiterArticleMeshHeading reCiterArticleMeshHeading : reCiterArticle.getMeshHeadings()) {
                    if(MeshMajorClusteringStrategy.isMeshMajor(reCiterArticleMeshHeading)) {
                        ReCiterArticleFeature.ArticleKeyword articleKeyword = new ArticleKeyword(reCiterArticleMeshHeading.getDescriptorName().getDescriptorName(), KeywordType.MESH_MAJOR, EngineParameters.getMeshCountMap().get(reCiterArticleMeshHeading.getDescriptorName().getDescriptorName()));
                        articleKeywords.add(articleKeyword);
                    }
                }
                if(!articleKeywords.isEmpty()) {
                    reCiterArticleFeature.setArticleKeywords(articleKeywords);
                }
                
            }
            //scopus doc id
            if(reCiterArticle.getScopusDocId() != null) {
            	reCiterArticleFeature.setScopusDocID(reCiterArticle.getScopusDocId());
            }
            
            //ReCiter Cluster - Commenting out as not necessary at the moment
            /*ReCiterFeatureCluster reCiterFeatureCluster = new ReCiterFeatureCluster();
            //Cites
            if(reCiterArticle.getCommentsCorrectionsPmids() != null && !reCiterArticle.getCommentsCorrectionsPmids().isEmpty()) { 
                List<Long> cites = new ArrayList<>();
                ReCiterCites reCiterCites = new ReCiterCites(cites, "pmid");
                for(Long pmid: reCiterArticle.getCommentsCorrectionsPmids()) {
                    cites.add(pmid);
                }
                reCiterFeatureCluster.setReCiterCites(reCiterCites);
            }
            //CitedBy
            if(reCiterClusterer.getReCiterArticles() != null) {
                List<Long> citedBy = reCiterClusterer.getReCiterArticles()
                    .stream()
                    .filter(article -> article.getCommentsCorrectionsPmids().contains(reCiterArticle.getArticleId()))
                    .map(ReCiterArticle::getArticleId)
                    .collect(Collectors.toList());
                if(citedBy != null && !citedBy.isEmpty()) {
                    ReCiterCitedBy reCiterCitedBy = new ReCiterCitedBy(citedBy, "pmid");
                    reCiterFeatureCluster.setReCiterCitedBy(reCiterCitedBy);
                }
            }
            //Grant
            if(reCiterArticle.getGrantList() != null) {
                reCiterFeatureCluster.setGrantIdentifiers(reCiterArticle.getGrantList()
                    .stream()
                    .map(ReCiterArticleGrant::getGrantID)
                    .collect(Collectors.toList()));
            }
            //Journal Category
            if(reCiterArticle.getJournalCategory() != null) {
                reCiterFeatureCluster.setJournalCategory(reCiterArticle.getJournalCategory()); 
            }
            reCiterArticleFeature.setReCiterCluster(reCiterFeatureCluster);*/
            // journal title
            reCiterArticleFeature.setJournalTitleVerbose(reCiterArticle.getJournal().getJournalTitle());
            
            //journal issn
            reCiterArticleFeature.setIssn(reCiterArticle.getJournal().getJournalIssn());

            // journal title ISO Abbreviation
            reCiterArticleFeature.setJournalTitleISOabbreviation(reCiterArticle.getJournal().getIsoAbbreviation());

            // article title
            reCiterArticleFeature.setArticleTitle(reCiterArticle.getArticleTitle());

            // volume
            reCiterArticleFeature.setVolume(reCiterArticle.getVolume());

            // issue
            reCiterArticleFeature.setIssue(reCiterArticle.getIssue());

            /**
             * <Pagination>
             <MedlinePgn>1083-95</MedlinePgn> get the first one.
             </Pagination>
             */
            // pages
            reCiterArticleFeature.setPages(reCiterArticle.getPages());

            // pmcid (https://www.ncbi.nlm.nih.gov/pubmed/26174865?report=xml&format=text)
            // Need to add parsing for <OtherID Source="NLM">PMC5009940 [Available on 01/01/17]</OtherID>
            // Or check <ArticleId IdType="pmc">PMC2907408</ArticleId>
            if(reCiterArticle.getPmcid() != null) {
            	reCiterArticleFeature.setPmcid(reCiterArticle.getPmcid());
            }

            // doi
            // <ArticleId IdType="doi">10.1093/jnci/djq238</ArticleId>
            reCiterArticleFeature.setDoi(reCiterArticle.getDoi());

            // author list
            List<ReCiterArticleAuthorFeature> reCiterArticleAuthorFeatures = new ArrayList<>();
            int i = 1;

            for (ReCiterAuthor reCiterArticleAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {
                ReCiterArticleAuthorFeature reCiterArticleAuthorFeature = new ReCiterArticleAuthorFeature();
                // rank
                reCiterArticleAuthorFeature.setRank(i++);

                // lastname
                reCiterArticleAuthorFeature.setLastName(reCiterArticleAuthor.getAuthorName().getLastName());
                // first name
                reCiterArticleAuthorFeature.setFirstName(reCiterArticleAuthor.getAuthorName().getFirstName());
                // initials
                reCiterArticleAuthorFeature.setInitials(reCiterArticleAuthor.getAuthorName().getFirstInitial());
                // affiliation scopus
                //Commenting out as not necessary at the moment
                /*ReCiterArticleAffiliationFeature reCiterArticleAffiliationFeature =
                        new ReCiterArticleAffiliationFeature();
                if(reCiterArticleAuthor.getAffiliation() != null) {
                    reCiterArticleAffiliationFeature.setAffiliationStatementLabel(reCiterArticleAuthor.getAffiliation());
                    reCiterArticleAffiliationFeature.setAffiliationStatementLabelSource(InstitutionalAffiliationSource.PUBMED);
                }

                // affiliation Scopus 
                if(reCiterArticle.getScopusArticle() != null) {
                    Author scopusAuthor = reCiterArticle.getScopusArticle().getAuthors().stream().filter(author -> reCiterArticleAuthor.getRank() == author.getSeq()).findFirst().orElse(null);
                    if(scopusAuthor != null && scopusAuthor.getAfids() != null && !scopusAuthor.getAfids().isEmpty()) {
                        List<ReCiterArticleAffiliationFeature.ReCiterArticleAffiliationInstitution> affiliationInstitutions = new ArrayList<>(scopusAuthor.getAfids().size());
                        for(Integer afid: scopusAuthor.getAfids()) {
                            Affiliation affiliationScopus = reCiterArticle.getScopusArticle().getAffiliations()
                                    .stream()
                                    .filter(affiliation -> affiliation.getAfid() == afid)
                                    .findFirst()
                                    .orElse(null);
                            ReCiterArticleAffiliationFeature.ReCiterArticleAffiliationInstitution articleAffiliationInstitution = new ReCiterArticleAffiliationInstitution();
                            articleAffiliationInstitution.setAffiliationInstitutionSource(InstitutionalAffiliationSource.SCOPUS);
                            articleAffiliationInstitution.setAffiliationInstitutionId(afid);
                            if(affiliationScopus != null && affiliationScopus.getAffilname() != null)
                                articleAffiliationInstitution.setAffiliationInstitutionLabel(affiliationScopus.getAffilname());
                            affiliationInstitutions.add(articleAffiliationInstitution);
                        }
                        reCiterArticleAffiliationFeature.setAffiliationInstitutions(affiliationInstitutions);
                    }
                }
                reCiterArticleAuthorFeature.setAffiliations(reCiterArticleAffiliationFeature);*/
                //email
                if(reCiterArticleAuthor.getAffiliation() != null) {
                    Pattern pattern = Pattern.compile("([a-z0-9_.-]+)@([a-z0-9_.-]+[a-z])", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(reCiterArticleAuthor.getAffiliation());
                    while(matcher.find()) {
                        reCiterArticleAuthorFeature.setEmail(matcher.group());
                    }
                }
                // isTargetAuthor
                reCiterArticleAuthorFeature.setTargetAuthor(reCiterArticleAuthor.isTargetAuthor());

                // Orcid
                if(reCiterArticleAuthor.getOrcid() != null && !reCiterArticleAuthor.getOrcid().isEmpty()) {
                    Pattern pattern = Pattern.compile("[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}");
                    Matcher matcher = pattern.matcher(reCiterArticleAuthor.getOrcid());
                    if(matcher.find()) {
                        reCiterArticleAuthorFeature.setOrcid(matcher.group());
                    }
                }
                //EqualContrib
                if(reCiterArticleAuthor.getEqualContrib()!=null && !reCiterArticleAuthor.getEqualContrib().isEmpty())
                {
                	reCiterArticleAuthorFeature.setEqualContrib(reCiterArticleAuthor.getEqualContrib());
                }
                

                reCiterArticleAuthorFeatures.add(reCiterArticleAuthorFeature);
            }
            reCiterArticleFeature.setReCiterArticleAuthorFeatures(reCiterArticleAuthorFeatures);

            Evidence evidence = new Evidence();
            // Affiliation Evidence
            if (reCiterArticle.getAffiliationEvidence() != null) {
                evidence.setAffiliationEvidence(reCiterArticle.getAffiliationEvidence());
            }

            // AuthorName Evidence (the most complete author name in the article)
            /*AuthorNameEvidence authorNameEvidence = new AuthorNameEvidence();
            ReCiterAuthor reCiterAuthor = RemoveByNameStrategy.getCorrectAuthor(reCiterArticle, identity);
            if (reCiterAuthor != null) {
                authorNameEvidence.setArticleAuthorName(reCiterAuthor.getAuthorName());
                authorNameEvidence.setInstitutionalAuthorName(identity.getPrimaryName());
            }
            positiveEvidence.setAuthorNameEvidence(authorNameEvidence);*/
            if (reCiterArticle.getAuthorNameEvidence() != null) {
                evidence.setAuthorNameEvidence(reCiterArticle.getAuthorNameEvidence());
            }

            // Grant Evidence
            if (reCiterArticle.getGrantEvidence() != null) {
                evidence.setGrantEvidence(reCiterArticle.getGrantEvidence());
            }
            
            //Journal Category Evidence
            if(reCiterArticle.getJournalCategoryEvidence() != null) {
            	evidence.setJournalCategoryEvidence(reCiterArticle.getJournalCategoryEvidence());
            }
            // Relationship Evidence
            if (reCiterArticle.getRelationshipEvidence() != null) {
                evidence.setRelationshipEvidence(reCiterArticle.getRelationshipEvidence());
            }

            // Education Year Evidence
            /*EducationYearEvidence educationYearEvidence = new EducationYearEvidence();
            educationYearEvidence.setDiscrepancyDegreeYearBachelor(reCiterArticle.getBachelorsYearDiscrepancy());
            educationYearEvidence.setDiscrepancyDegreeYearDoctoral(reCiterArticle.getDoctoralYearDiscrepancy());
            reCiterArticle.setEducationYearEvidence(educationYearEvidence);*/
            if (reCiterArticle.getEducationYearEvidence() != null) {
                evidence.setEducationYearEvidence(reCiterArticle.getEducationYearEvidence());
            }

            if (reCiterArticle.getAcceptedRejectedEvidence() != null) {
                evidence.setAcceptedRejectedEvidence(reCiterArticle.getAcceptedRejectedEvidence());
            }

            if (reCiterArticle.getOrganizationalUnitEvidences() != null) {
                evidence.setOrganizationalUnitEvidence(reCiterArticle.getOrganizationalUnitEvidences());
            }

            if (reCiterArticle.getPersonTypeEvidence() != null) {
                evidence.setPersonTypeEvidence(reCiterArticle.getPersonTypeEvidence());
            }

            if (reCiterArticle.getEmailEvidence() != null) {
                evidence.setEmailEvidence(reCiterArticle.getEmailEvidence());
            }

            if (reCiterArticle.getArticleCountEvidence() != null) {
                evidence.setArticleCountEvidence(reCiterArticle.getArticleCountEvidence());
            }

            if (reCiterArticle.getAverageClusteringEvidence() != null) {
                evidence.setAverageClusteringEvidence(reCiterArticle.getAverageClusteringEvidence());
            }
            
            if(reCiterArticle.getGenderEvidence() != null && reCiterArticle.getGenderEvidence().getGenderScoreIdentityArticleDiscrepancy() != null) {
            	evidence.setGenderEvidence(reCiterArticle.getGenderEvidence());
            }
            // Clustering Evidence
            //positiveEvidence.setClusteringEvidence(reCiterArticle.getClusteringEvidence());

            log.info("reCiter {} hashcode {}", reCiterArticle.getArticleId(), reCiterArticle.hashCode());
            reCiterArticleFeature.setEvidence(evidence);

            reCiterArticleFeatures.add(reCiterArticleFeature);
        }
        calculateTopKeywords(reCiterArticleFeatures, reCiterFeature, keywordsMax);
        //Sorting the List in descending order based on TotalScoreNonStandardized
        reCiterFeature.setReCiterArticleFeatures(reCiterArticleFeatures.stream().sorted(Comparator.comparing(ReCiterArticleFeature::getTotalArticleScoreNonStandardized).reversed()).collect(Collectors.toList()));

        return reCiterFeature;
    }

    private void calculateTopKeywords(List<ReCiterArticleFeature> reCiterArticleFeatures, ReCiterFeature reCiterFeature, double keywordsMax) {
        
        Map<String, Long> acceptedArticleKeywords = reCiterArticleFeatures
            .stream()
            .filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() != null 
                && reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
                && reCiterArticleFeature.getArticleKeywords() != null)
            .flatMap(keyword -> keyword.getArticleKeywords().stream())
            .map(ReCiterArticleFeature.ArticleKeyword::getKeyword)
            .collect(Collectors.groupingBy(acceptedKeyword -> acceptedKeyword , Collectors.counting()));

        if(acceptedArticleKeywords != null && !acceptedArticleKeywords.isEmpty()) {
            //Sort descending by Count and ascending by Keyword
            LinkedHashMap<String, Long> sortedAcceptedKeywordCount = acceptedArticleKeywords.entrySet().stream()
                        .sorted(Map.Entry
                        .<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                        .limit((long)keywordsMax)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            
            List<ReCiterArticleFeature.ArticleKeyword> acceptedKeywords = 
                sortedAcceptedKeywordCount.entrySet()
                    .stream()
                    .map(keyword -> 
                        new ArticleKeyword(keyword.getKey(), KeywordType.MESH_MAJOR, keyword.getValue()))
                    .collect(Collectors.toList());
            
            reCiterFeature.setArticleKeywordsAcceptedArticles(acceptedKeywords);
        }
    }
}
