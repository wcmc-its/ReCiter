package reciter.engine.analysis;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.cluster.targetauthor.ClusterSelector;
import reciter.algorithm.evidence.targetauthor.name.strategy.RemoveByNameStrategy;
import reciter.engine.analysis.evidence.AuthorNameEvidence;
import reciter.engine.analysis.evidence.EducationYearEvidence;
import reciter.engine.analysis.evidence.PositiveEvidence;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public ReCiterFeature computeFeatures(String mode,
                                          Clusterer reCiterClusterer,
                                          ClusterSelector clusterSelector,
                                          List<Long> goldStandardPmids,
                                          List<Long> rejectedPmids,
                                          Analysis analysis) {
        Map<Long, ReCiterCluster> finalCluster = reCiterClusterer.getClusters();
        Set<Long> selection = clusterSelector.getSelectedClusterIds();
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

        // Combine all articles into a single list. TODO optimize
        int countSuggestedArticles = 0;
        List<ReCiterArticle> articleList = new ArrayList<>();
        for (long s : selection) {
            long clusterOriginator = finalCluster.get(s).getClusterOriginator();
            String journalTitle = null;
            for (ReCiterArticle reCiterArticle : finalCluster.get(s).getArticleCluster()) {
                articleList.add(reCiterArticle);
                if (reCiterArticle.getArticleId() == clusterOriginator) {
                    journalTitle = reCiterArticle.getJournal().getJournalTitle();
                }
                countSuggestedArticles++;
            }
            // Add journals
            for (ReCiterArticle reCiterArticle : finalCluster.get(s).getArticleCluster()) {
                reCiterArticle.getClusteringEvidence().setJournal(journalTitle);
            }
        }
        reCiterFeature.setCountSuggestedArticles(countSuggestedArticles);

        // "suggestedArticles"
        List<ReCiterArticleFeature> reCiterArticleFeatures = new ArrayList<>(articleList.size());
        for (ReCiterArticle reCiterArticle : articleList) {
            ReCiterArticleFeature reCiterArticleFeature = new ReCiterArticleFeature();
            reCiterArticleFeature.setPmid(reCiterArticle.getArticleId()); // pmid
            reCiterArticleFeature.setScore(
                    reCiterArticle.getAffiliationScore() +
                            reCiterArticle.getCitizenshipStrategyScore() +
                            reCiterArticle.getCoauthorStrategyScore() +
                            reCiterArticle.getDepartmentStrategyScore() +
                            reCiterArticle.getEducationStrategyScore() +
                            reCiterArticle.getJournalStrategyScore() +
                            reCiterArticle.getKnownCoinvestigatorScore() +
                            reCiterArticle.getScopusStrategyScore() +
                            reCiterArticle.getEmailStrategyScore() +
                            reCiterArticle.getMeshMajorStrategyScore() +
                            reCiterArticle.getBachelorsYearDiscrepancyScore() +
                            reCiterArticle.getBoardCertificationStrategyScore() +
                            reCiterArticle.getDoctoralYearDiscrepancyScore() +
                            reCiterArticle.getInternshipAndResidenceStrategyScore() +
                            reCiterArticle.getNameStrategyScore()); // score

            // true; false; null. make it Boolean
         // userAssertion TODO get from DB
            if(goldStandardPmids != null && goldStandardPmids.contains(reCiterArticle.getArticleId())) {
            	reCiterArticleFeature.setUserAssertion(true);
            }
            else if(rejectedPmids != null && rejectedPmids.contains(reCiterArticle.getArticleId())) {
            	reCiterArticleFeature.setUserAssertion(false);
            }
            else {
            	reCiterArticleFeature.setUserAssertion(null);
            }
            	
            	
            	
             

            // PubDate
            reCiterArticleFeature.setPubDate(reCiterArticle.getPubDate());

            // journal title
            reCiterArticleFeature.setJournalTitleVerbose(reCiterArticle.getJournal().getJournalTitle());

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

            PositiveEvidence positiveEvidence = new PositiveEvidence();
            // Affiliation Evidence
            positiveEvidence.setAffiliationEvidence(reCiterArticle.getAffiliationEvidence());

            // AuthorName Evidence (the most complete author name in the article)
            AuthorNameEvidence authorNameEvidence = new AuthorNameEvidence();
            ReCiterAuthor reCiterAuthor = RemoveByNameStrategy.getCorrectAuthor(reCiterArticle, identity);
            if (reCiterAuthor != null) {
                authorNameEvidence.setArticleAuthorName(reCiterAuthor.getAuthorName());
                authorNameEvidence.setInstitutionalAuthorName(identity.getPrimaryName());
            }
            positiveEvidence.setAuthorNameEvidence(authorNameEvidence);

            // Grant Evidence
            positiveEvidence.setGrantEvidence(reCiterArticle.getGrantEvidence());

            // Relationship Evidence
            positiveEvidence.setRelationshipEvidences(reCiterArticle.getRelationshipEvidence());

            // Education Year Evidence
            EducationYearEvidence educationYearEvidence = new EducationYearEvidence();
            educationYearEvidence.setDiscrepancyDegreeYearBachelor(reCiterArticle.getBachelorsYearDiscrepancy());
            educationYearEvidence.setDiscrepancyDegreeYearDoctoral(reCiterArticle.getDoctoralYearDiscrepancy());
            reCiterArticle.setEducationYearEvidence(educationYearEvidence);
            positiveEvidence.setEducationYearEvidence(reCiterArticle.getEducationYearEvidence());

            // Clustering Evidence
            positiveEvidence.setClusteringEvidence(reCiterArticle.getClusteringEvidence());
            log.info("reCiter {} hashcode {}", reCiterArticle.getArticleId(), reCiterArticle.hashCode());
            reCiterArticleFeature.setPositiveEvidence(positiveEvidence);

            reCiterArticleFeatures.add(reCiterArticleFeature);
        }
        // rak2007
        reCiterFeature.setReCiterArticleFeatures(reCiterArticleFeatures);

        return reCiterFeature;
    }
}
