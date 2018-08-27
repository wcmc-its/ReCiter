package reciter.engine.analysis;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.engine.analysis.ReCiterArticleFeature.PublicationFeedback;
import reciter.engine.analysis.evidence.Evidence;
import reciter.engine.erroranalysis.Analysis;
import reciter.engine.erroranalysis.UseGoldStandard;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
                                          double totalStandardizedScore,
                                          Clusterer reCiterClusterer,
                                          List<Long> goldStandardPmids,
                                          List<Long> rejectedPmids,
                                          Analysis analysis) {
        Map<Long, ReCiterCluster> finalCluster = reCiterClusterer.getClusters();
        //Set<Long> selection = clusterSelector.getSelectedClusterIds();
        Identity identity = reCiterClusterer.getIdentity();

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

        // overall accuracy
        reCiterFeature.setOverallAccuracy((analysis.getPrecision() + analysis.getRecall()) / 2);

        // precision
        reCiterFeature.setPrecision(analysis.getPrecision());

        // recall
        reCiterFeature.setRecall(analysis.getRecall());

        // in gold standard but not retrieved TODO optimize
        List<Long> inGoldStandardButNotRetrieved = new ArrayList<>();
        for (long pmid : goldStandardPmids) {
            if (!pmidsRetrieved.contains(pmid)) {
                inGoldStandardButNotRetrieved.add(pmid);
            }
        }
        reCiterFeature.setInGoldStandardButNotRetrieved(inGoldStandardButNotRetrieved);

        List<ReCiterArticle> selectedArticles = reCiterClusterer.getReCiterArticles().stream().filter(reCiterArticle -> reCiterArticle.getTotalArticleScoreStandardized() >= totalStandardizedScore).collect(Collectors.toList());
        reCiterFeature.setCountSuggestedArticles(selectedArticles.size());

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
            reCiterArticleFeature.setPmcid(reCiterArticle.getPmcid());

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
                // affiliation

                ReCiterArticleAffiliationFeature reCiterArticleAffiliationFeature =
                        new ReCiterArticleAffiliationFeature();

                // affiliation from PubMed
                reCiterArticleAffiliationFeature.setAffiliationPubmed(reCiterArticleAuthor.getAffiliation());

                // affiliation from Scopus
                reCiterArticleAffiliationFeature.setAffiliationScopus(reCiterArticleAuthor.getAffiliation());

                // affiliation Scopus id

                // isTargetAuthor
                reCiterArticleAuthorFeature.setTargetAuthor(reCiterArticleAuthor.isTargetAuthor());

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
            // Clustering Evidence
            //positiveEvidence.setClusteringEvidence(reCiterArticle.getClusteringEvidence());

            log.info("reCiter {} hashcode {}", reCiterArticle.getArticleId(), reCiterArticle.hashCode());
            reCiterArticleFeature.setEvidence(evidence);

            reCiterArticleFeatures.add(reCiterArticleFeature);
        }
        // rak2007
        reCiterFeature.setReCiterArticleFeatures(reCiterArticleFeatures.stream().sorted(Comparator.comparing(ReCiterArticleFeature::getTotalArticleScoreNonStandardized).reversed()).collect(Collectors.toList()));

        return reCiterFeature;
    }
}
