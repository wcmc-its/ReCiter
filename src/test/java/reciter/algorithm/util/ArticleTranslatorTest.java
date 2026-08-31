package reciter.algorithm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reciter.model.article.ReCiterArticle;
import reciter.model.pubmed.MedlineCitationCommentsCorrections;
import reciter.model.pubmed.PubMedArticle;

@ExtendWith(MockitoExtension.class)
public class ArticleTranslatorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PubMedArticle pubmedArticle;

    @BeforeEach
    public void setUp() {
        when(pubmedArticle.getPubmeddata()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getArticle().getJournal()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getArticle().getPublicationtypelist()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getArticle().getPublicationAbstract()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getArticle().getAuthorlist()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getKeywordlist()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getMeshheadinglist()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getArticle().getArticledate()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getArticle().getGrantlist()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getArticle().getPagination()).thenReturn(null);
        when(pubmedArticle.getMedlinecitation().getArticle().getElocationid()).thenReturn(null);
    }

    @Test
    public void translateSkipsNonNumericCommentsCorrectionsAndKeepsNumeric() {
        MedlineCitationCommentsCorrections pmcEntry = new MedlineCitationCommentsCorrections();
        pmcEntry.setPmid("PMC7149824");
        pmcEntry.setReftype("ErratumIn");

        MedlineCitationCommentsCorrections textEntry = new MedlineCitationCommentsCorrections();
        textEntry.setPmid("Zhu X., Han J. (2020). A novel circular RNA. BMC Cancer 20, 1190.");

        MedlineCitationCommentsCorrections numericEntry = new MedlineCitationCommentsCorrections();
        numericEntry.setPmid("32648546");
        numericEntry.setReftype("ErratumFor");

        List<MedlineCitationCommentsCorrections> ccList = Arrays.asList(pmcEntry, textEntry, numericEntry);
        when(pubmedArticle.getMedlinecitation().getCommentscorrectionslist()).thenReturn(ccList);

        ReCiterArticle result = ArticleTranslator.translate(pubmedArticle, null, "", null);

        assertEquals(1, result.getCommentsCorrectionsPmids().size());
        assertTrue(result.getCommentsCorrectionsPmids().contains(32648546L));
        assertEquals("ErratumFor", result.getCommentsCorrectionsRefTypes().get(32648546L));
    }
}
