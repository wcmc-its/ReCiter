package reciter.xml.retriever.engine;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.mongo.model.ESearchResult;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.PubMedAlias;
import reciter.model.pubmed.MedlineCitationArticleAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.mongo.ESearchResultService;
import reciter.utils.PubMedConverter;
import reciter.xml.retriever.pubmed.AbstractRetrievalStrategy.RetrievalResult;

@Component("aliasReCiterRetrievalEngine")
public class AliasReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AliasReCiterRetrievalEngine.class);

	@Autowired
	private ESearchResultService eSearchResultService;
	
	private class AsyncRetrievalEngine extends Thread {

		private final Identity identity;
		private final LocalDate startDate;
		private final LocalDate endDate;
		
		public AsyncRetrievalEngine(Identity identity, LocalDate startDate, LocalDate endDate) {
			this.identity = identity;
			this.startDate = startDate;
			this.endDate = endDate;
		}
		
		@Override
		public void run() {
			try {
				List<ESearchResult> results = eSearchResultService.findByCwid(identity.getCwid());
				if (results.isEmpty()) {
					slf4jLogger.info("Starting full retrieval for cwid=[" + identity.getCwid() + "].");
					retrieveData(identity);
				} else {
					slf4jLogger.info("Starting date range retrieval for cwid=[" + identity.getCwid() + "] startDate=[" 
							+ startDate + " endDate=[" + endDate + "].");
					retrieveDataByDateRange(identity, startDate, endDate);
				}
			} catch (IOException e) {
				slf4jLogger.error("Unabled to retrieve. " + identity.getCwid(), e);
			}
		}
	}

	@Override
	public void retrieveArticlesByDateRange(List<Identity> identities, LocalDate startDate, LocalDate endDate) throws IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		for (Identity identity : identities) {
			executorService.execute(new AsyncRetrievalEngine(identity, startDate, endDate));
		}
		executorService.shutdown();
	}
	
	private Set<Long> retrieveData(Identity identity) throws IOException {
		Set<Long> uniquePmids = new HashSet<Long>();
		
		String cwid = identity.getCwid();
		
		// Retrieve by email.
		RetrievalResult retrievalResult = emailRetrievalStrategy.retrievePubMedArticles(identity);
		Map<Long, PubMedArticle> emailPubMedArticles = retrievalResult.getPubMedArticles();
		
		if (emailPubMedArticles.size() > 0) {
			Map<Long, AuthorName> aliasSet = calculatePotentialAlias(identity, emailPubMedArticles.values());

			slf4jLogger.info("Found " + aliasSet.size() + " new alias for cwid=[" + cwid + "]");
			 
			// Update alias.
			List<PubMedAlias> pubMedAliases = new ArrayList<PubMedAlias>();
			for (Map.Entry<Long, AuthorName> entry : aliasSet.entrySet()) {
				PubMedAlias pubMedAlias = new PubMedAlias();
				pubMedAlias.setAuthorName(entry.getValue());
				pubMedAlias.setPmid(entry.getKey());
				slf4jLogger.info("new alias for cwid=[" + identity.getCwid() + "], alias=[" + entry.getValue() + "] from pmid=[" + entry.getKey() + "]");
				pubMedAliases.add(pubMedAlias);
			}

			identity.setPubMedAlias(pubMedAliases);
			identity.setDateInitialRun(LocalDateTime.now(Clock.systemUTC()));
			identity.setDateLastRun(LocalDateTime.now(Clock.systemUTC()));
			identityService.save(identity);
			
			uniquePmids.addAll(emailPubMedArticles.keySet());
		}
		
		// TODO parallelize by putting save in a separate thread.
		savePubMedArticles(emailPubMedArticles.values(), cwid, emailRetrievalStrategy.getRetrievalStrategyName(), retrievalResult.getPubMedQueryResults());
		
		RetrievalResult r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity);
		if (r1.getPubMedArticles().size() > 0) {
			savePubMedArticles(r1.getPubMedArticles().values(), cwid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), r1.getPubMedQueryResults());
			uniquePmids.addAll(r1.getPubMedArticles().keySet());
		} else {
			RetrievalResult r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity);
			savePubMedArticles(r2.getPubMedArticles().values(), cwid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), r2.getPubMedQueryResults());
			uniquePmids.addAll(r2.getPubMedArticles().keySet());
			
			RetrievalResult r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity);
			savePubMedArticles(r3.getPubMedArticles().values(), cwid, affiliationRetrievalStrategy.getRetrievalStrategyName(), r3.getPubMedQueryResults());
			uniquePmids.addAll(r3.getPubMedArticles().keySet());
			
			RetrievalResult r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity);
			savePubMedArticles(r4.getPubMedArticles().values(), cwid, departmentRetrievalStrategy.getRetrievalStrategyName(), r4.getPubMedQueryResults());
			uniquePmids.addAll(r4.getPubMedArticles().keySet());
			
			RetrievalResult r5 = grantRetrievalStrategy.retrievePubMedArticles(identity);
			savePubMedArticles(r5.getPubMedArticles().values(), cwid, grantRetrievalStrategy.getRetrievalStrategyName(), r5.getPubMedQueryResults());
			uniquePmids.addAll(r5.getPubMedArticles().keySet());
		}
		
		List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);
		scopusService.save(scopusArticles);
		slf4jLogger.info("Finished retrieval for cwid: " + identity.getCwid());
		return uniquePmids;
	}
	
	public void retrieveDataByDateRange(Identity identity, LocalDate startDate, LocalDate endDate) throws IOException {
		Set<Long> uniquePmids = new HashSet<Long>();
		
		String cwid = identity.getCwid();
		
		// Retrieve by email.
		RetrievalResult retrievalResult = emailRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
		Map<Long, PubMedArticle> emailPubMedArticles = retrievalResult.getPubMedArticles();
		
		if (emailPubMedArticles.size() > 0) {
			Map<Long, AuthorName> aliasSet = calculatePotentialAlias(identity, emailPubMedArticles.values());

			slf4jLogger.info("Found " + aliasSet.size() + " new alias for cwid=[" + cwid + "]");
			 
			// Update alias.
			List<PubMedAlias> pubMedAliases = new ArrayList<PubMedAlias>();
			for (Map.Entry<Long, AuthorName> entry : aliasSet.entrySet()) {
				PubMedAlias pubMedAlias = new PubMedAlias();
				pubMedAlias.setAuthorName(entry.getValue());
				pubMedAlias.setPmid(entry.getKey());
				slf4jLogger.info("new alias for cwid=[" + identity.getCwid() + "], alias=[" + entry.getValue() + "] from pmid=[" + entry.getKey() + "]");
				pubMedAliases.add(pubMedAlias);
			}

			identity.setPubMedAlias(pubMedAliases);
			identity.setDateInitialRun(LocalDateTime.now(Clock.systemUTC()));
			identity.setDateLastRun(LocalDateTime.now(Clock.systemUTC()));
			identityService.save(identity);
			
			uniquePmids.addAll(emailPubMedArticles.keySet());
		}
		
		// TODO parallelize by putting save in a separate thread.
		savePubMedArticles(emailPubMedArticles.values(), cwid, emailRetrievalStrategy.getRetrievalStrategyName(), retrievalResult.getPubMedQueryResults());
		
		RetrievalResult r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
		if (r1.getPubMedArticles().size() > 0) {
			savePubMedArticles(r1.getPubMedArticles().values(), cwid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), r1.getPubMedQueryResults());
			uniquePmids.addAll(r1.getPubMedArticles().keySet());
		} else {
			RetrievalResult r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
			savePubMedArticles(r2.getPubMedArticles().values(), cwid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), r2.getPubMedQueryResults());
			uniquePmids.addAll(r2.getPubMedArticles().keySet());
			
			RetrievalResult r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
			savePubMedArticles(r3.getPubMedArticles().values(), cwid, affiliationRetrievalStrategy.getRetrievalStrategyName(), r3.getPubMedQueryResults());
			uniquePmids.addAll(r3.getPubMedArticles().keySet());
			
			RetrievalResult r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
			savePubMedArticles(r4.getPubMedArticles().values(), cwid, departmentRetrievalStrategy.getRetrievalStrategyName(), r4.getPubMedQueryResults());
			uniquePmids.addAll(r4.getPubMedArticles().keySet());
			
			RetrievalResult r5 = grantRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
			savePubMedArticles(r5.getPubMedArticles().values(), cwid, grantRetrievalStrategy.getRetrievalStrategyName(), r5.getPubMedQueryResults());
			uniquePmids.addAll(r5.getPubMedArticles().keySet());
		}
		
		List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);
		scopusService.save(scopusArticles);
		slf4jLogger.info("Finished retrieval for cwid: " + identity.getCwid());
	}
	
	private Map<Long, AuthorName> calculatePotentialAlias(Identity identity, Collection<PubMedArticle> emailPubMedArticles) {
		Map<Long, AuthorName> aliasSet = new HashMap<Long, AuthorName>();
		for (PubMedArticle pubMedArticle : emailPubMedArticles) {
			for (MedlineCitationArticleAuthor author : pubMedArticle.getMedlineCitation().getArticle().getAuthorList()) {
				String affiliation = author.getAffiliation();
				if (affiliation != null) {
					for (String email : identity.getEmails()) {
						if (affiliation.contains(email)) {
							// possibility of an alias:
							if (author.getLastName().equals(identity.getPrimaryName().getLastName())) {
								// sanity check: last name matches
								AuthorName alias = PubMedConverter.extractAuthorName(author);
								if (!alias.getFirstInitial().equals(identity.getPrimaryName().getFirstInitial())) {
									// check if the same first initial is already added to the set.
									if (aliasSet.isEmpty()) {
										aliasSet.put(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid(), alias);
										slf4jLogger.info(identity.getCwid() + ": " + identity.getPrimaryName() + ": (Empty set) Adding alias: " + alias);
									} else {
										for (AuthorName aliasAuthorName : aliasSet.values()) {
											if (!aliasAuthorName.getFirstInitial().equals(alias.getFirstInitial())) {
												aliasSet.put(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid(), alias);
												slf4jLogger.info(identity.getCwid() + ": " + identity.getPrimaryName() + ": (Different first initial) Adding alias: " + alias);
												break;
											} else {
												String firstNameInSet = aliasAuthorName.getFirstName();
												String currentFirstName = alias.getFirstName();
												// prefer the name with the longer first name: i.e., prefer 'Clay' over 'C.'
												// so remove the 'C.' and add the 'Clay'
												if (firstNameInSet.length() < currentFirstName.length()) {
													aliasSet.remove(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
													aliasSet.put(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid(), alias);
													slf4jLogger.info(identity.getCwid() + ": " + identity.getPrimaryName() + ": (Prefer longer first name) Adding alias: " + alias);
													break;
												}
											}
										}
									}
								}
							}
							break;
						}
					}
				}
			}
		}
		return aliasSet;
	}
}
