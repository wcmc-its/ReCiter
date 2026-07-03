package reciter.service.dynamo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import reciter.database.dynamodb.model.ExternalArticle;
import reciter.engine.analysis.ReCiterArticleFeature;
import reciter.service.dynamo.ExternalArticleDupCheck.Level;
import reciter.service.dynamo.ExternalArticleDupCheck.Result;

public class ExternalArticleDupCheckTest {

    private ExternalArticle candidate(String articleId, Long pmid, String doi, String title, String pubDate) {
        ExternalArticle externalArticle = new ExternalArticle();
        externalArticle.setUid("test1234");
        externalArticle.setArticleId(articleId);
        externalArticle.setPmid(pmid);
        externalArticle.setDoi(doi);
        externalArticle.setTitle(title);
        externalArticle.setPubDate(pubDate);
        return externalArticle;
    }

    private ReCiterArticleFeature pubmedArticle(long pmid, String doi, String title, String dateStandardized) {
        ReCiterArticleFeature feature = new ReCiterArticleFeature();
        feature.setPmid(pmid);
        feature.setDoi(doi);
        feature.setArticleTitle(title);
        feature.setPublicationDateStandardized(dateStandardized);
        return feature;
    }

    @Test
    public void cleanAddIsOk() {
        Result result = ExternalArticleDupCheck.check(
                candidate("OPENALEX:W123", null, "10.1000/xyz", "A Novel Finding", "2025-06-01"),
                Arrays.asList(11111L),
                Arrays.asList(pubmedArticle(11111L, "10.1000/abc", "Something Else Entirely", "2020-01-01")),
                Collections.emptyList());
        assertEquals(Level.OK, result.getLevel());
    }

    @Test
    public void pmidInGoldStandardBlocks() {
        Result result = ExternalArticleDupCheck.check(
                candidate("SCOPUS:850001", 11111L, null, "Any Title", "2020"),
                Arrays.asList(11111L),
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(Level.BLOCKED, result.getLevel());
        assertEquals("PMID_IN_GOLD_STANDARD", result.getMatches().get(0).getType());
    }

    @Test
    public void pmidInCandidateSetBlocks() {
        Result result = ExternalArticleDupCheck.check(
                candidate("SCOPUS:850001", 22222L, null, "Any Title", "2020"),
                Collections.emptyList(),
                Arrays.asList(pubmedArticle(22222L, null, "Any Title", "2020-01-01")),
                Collections.emptyList());
        assertEquals(Level.BLOCKED, result.getLevel());
        assertEquals("PMID_IN_CANDIDATES", result.getMatches().get(0).getType());
    }

    @Test
    public void doiMatchAgainstCandidatesBlocksDespiteUrlPrefixAndCase() {
        Result result = ExternalArticleDupCheck.check(
                candidate("OPENALEX:W123", null, "https://doi.org/10.1000/ABC", "Different Title", "2021"),
                Collections.emptyList(),
                Arrays.asList(pubmedArticle(33333L, "10.1000/abc", "Original Title", "2021-05-01")),
                Collections.emptyList());
        assertEquals(Level.BLOCKED, result.getLevel());
        assertEquals("DOI_MATCH", result.getMatches().get(0).getType());
    }

    @Test
    public void doiMatchAgainstExistingExternalBlocks() {
        ExternalArticle existing = candidate("SCOPUS:850002", null, "10.1000/xyz", "Same Work From Scopus", "2022");
        Result result = ExternalArticleDupCheck.check(
                candidate("OPENALEX:W456", null, "10.1000/xyz", "Same Work From OpenAlex", "2022"),
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList(existing));
        assertEquals(Level.BLOCKED, result.getLevel());
        assertEquals("DOI_MATCH_EXTERNAL", result.getMatches().get(0).getType());
    }

    @Test
    public void sameArticleIdBlocks() {
        ExternalArticle existing = candidate("OPENALEX:W123", null, null, "A Title", "2020");
        Result result = ExternalArticleDupCheck.check(
                candidate("OPENALEX:W123", null, null, "A Title", "2020"),
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList(existing));
        assertEquals(Level.BLOCKED, result.getLevel());
        assertEquals("ALREADY_ADDED", result.getMatches().get(0).getType());
    }

    @Test
    public void titleAndYearCollisionWarns() {
        Result result = ExternalArticleDupCheck.check(
                candidate("OPENALEX:W123", null, null, "Deep Learning for Author Disambiguation!", "2023"),
                Collections.emptyList(),
                Arrays.asList(pubmedArticle(44444L, null, "Deep learning for author disambiguation", "2023-09-15")),
                Collections.emptyList());
        assertEquals(Level.WARNING, result.getLevel());
        assertEquals("TITLE_YEAR_MATCH", result.getMatches().get(0).getType());
    }

    @Test
    public void sameTitleDifferentYearIsOk() {
        Result result = ExternalArticleDupCheck.check(
                candidate("OPENALEX:W123", null, null, "Annual Report on Disambiguation", "2024"),
                Collections.emptyList(),
                Arrays.asList(pubmedArticle(55555L, null, "Annual Report on Disambiguation", "2023-01-01")),
                Collections.emptyList());
        assertEquals(Level.OK, result.getLevel());
    }

    @Test
    public void blockedOutranksWarning() {
        List<ReCiterArticleFeature> candidates = Arrays.asList(
                pubmedArticle(66666L, "10.1000/dup", "A Fuzzy Title Match", "2022-01-01"),
                pubmedArticle(77777L, null, "A Fuzzy Title Match", "2022-03-01"));
        Result result = ExternalArticleDupCheck.check(
                candidate("SCOPUS:850003", null, "10.1000/dup", "A Fuzzy Title Match", "2022"),
                Collections.emptyList(),
                candidates,
                Collections.emptyList());
        assertEquals(Level.BLOCKED, result.getLevel());
    }

    @Test
    public void normalizers() {
        assertEquals("10.1000/abc", ExternalArticleDupCheck.normalizeDoi(" https://dx.doi.org/10.1000/ABC "));
        assertNull(ExternalArticleDupCheck.normalizeDoi("  "));
        assertEquals("deep learning 2 0", ExternalArticleDupCheck.normalizeTitle("Deep-Learning: 2.0?"));
        assertEquals("2023", ExternalArticleDupCheck.year("2023-09-15"));
        assertNull(ExternalArticleDupCheck.year("n.d."));
    }
}
