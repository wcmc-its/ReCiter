package reciter.engine.analysis;

import lombok.Data;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.cluster.targetauthor.ClusterSelector;
import reciter.engine.analysis.evidence.PositiveEvidence;
import reciter.engine.erroranalysis.ReCiterAnalysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
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
                                          List<Long> goldStandardPmids) {
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

        // precision

        // recall

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
            for (ReCiterArticle reCiterArticle : finalCluster.get(s).getArticleCluster()) {
                articleList.add(reCiterArticle);
                countSuggestedArticles++;
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
            reCiterArticleFeature.setUserAssertion(false); // userAssertion TODO get from DB

            // PubDate
            reCiterArticleFeature.setPubDate(reCiterArticle.getPubDate());

            // journal title
            reCiterArticleFeature.setJournalTitleVerbose(reCiterArticle.getJournal().getJournalTitle());

            // journal title ISO Abbreviation
            reCiterArticleFeature.setJournalTitleISOabbreviation(reCiterArticle.getJournal().getIsoAbbreviation());

            // article title
            reCiterArticleFeature.setArticleTitle(reCiterArticle.getArticleTitle());

            // author list
            List<ReCiterArticleAuthorFeature> reCiterArticleAuthorFeatures = new ArrayList<>();
            for (ReCiterAuthor reCiterArticleAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {
                ReCiterArticleAuthorFeature reCiterArticleAuthorFeature = new ReCiterArticleAuthorFeature();
                // rank

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

                reCiterArticleAuthorFeatures.add(reCiterArticleAuthorFeature);
            }
            reCiterArticleFeature.setReCiterArticleAuthorFeatures(reCiterArticleAuthorFeatures);

            PositiveEvidence positiveEvidence = new PositiveEvidence();
            reCiterArticleFeature.setPositiveEvidence(positiveEvidence);
            // Affiliation Evidence
            positiveEvidence.setAffiliationEvidence(reCiterArticle.getAffiliationEvidence());

            // AuthorName Evidence (the most complete author name in the article)

            // Grant Evidence
            positiveEvidence.setGrantEvidence(reCiterArticle.getGrantEvidence());

            // Relationship Evidence
            positiveEvidence.setRelationshipEvidences(reCiterArticle.getRelationshipEvidence());

            // Education Year Evidence
            positiveEvidence.setEducationYearEvidence(reCiterArticle.getEducationYearEvidence());

            // Clustering Evidence
        }
        reCiterFeature.setReCiterArticleFeatures(reCiterArticleFeatures);

        return reCiterFeature;
    }
}
