package reciter.engine.analysis;

import lombok.Data;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.cluster.targetauthor.ClusterSelector;
import reciter.engine.erroranalysis.AnalysisObject;
import reciter.engine.erroranalysis.AnalysisTranslator;
import reciter.engine.erroranalysis.StatusEnum;
import reciter.model.article.ReCiterArticle;
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

            // Affiliation Evidence


            // AuthorName Evidence

            // Grant Evidence

            // Relationship Evidence

            // Education Year Evidence

            // Clustering Evidence
        }
        reCiterFeature.setReCiterArticleFeatures(reCiterArticleFeatures);

        return reCiterFeature;
    }
}
