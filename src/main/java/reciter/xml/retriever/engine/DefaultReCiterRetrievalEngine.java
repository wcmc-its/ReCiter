package reciter.xml.retriever.engine;

import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.mongo.model.ESearchPmid;
import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.model.Identity;
import reciter.database.mongo.model.PubMedAlias;
import reciter.engine.notification.Notifier;
import reciter.model.author.AuthorName;
import reciter.model.converter.PubMedConverter;
import reciter.model.pubmed.MedlineCitationArticleAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.xml.parser.scopus.model.ScopusArticle;
import reciter.xml.retriever.pubmed.AffiliationInDbRetrievalStrategy;
import reciter.xml.retriever.pubmed.DepartmentRetrievalStrategy;
import reciter.xml.retriever.pubmed.EmailRetrievalStrategy;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.GrantRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

@Component("defaultReCiterRetrievalEngine")
public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(DefaultReCiterRetrievalEngine.class);

	@Autowired
	private PubMedService pubMedService;

	@Autowired
	private ESearchResultService eSearchResultService;

	@Autowired
	private ScopusService scopusService;

	@Autowired
	private IdentityService identityService;

	@Autowired
	private Notifier notifier;
	
	

	@Override
	public List<Long> retrieve(Identity identity) {

		List<RetrievalStrategy> retrievalStrategies = new  ArrayList<RetrievalStrategy>();

		// Retrieve by email.
		//		RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy(false);
		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy(false);
		//		RetrievalStrategy departmentRetrievalStrategy = new DepartmentRetrievalStrategy(false);
		//		RetrievalStrategy affiliationInDbRetrievalStrategy = new AffiliationInDbRetrievalStrategy(false);
		//		RetrievalStrategy grantRetrievalStrategy = new GrantRetrievalStrategy(false);

		//		retrievalStrategies.add(emailRetrievalStrategy);
		retrievalStrategies.add(firstNameInitialRetrievalStrategy);
		//		retrievalStrategies.add(departmentRetrievalStrategy);
		//		retrievalStrategies.add(affiliationInDbRetrievalStrategy);
		//		retrievalStrategies.add(grantRetrievalStrategy);

		return retrieve(retrievalStrategies, identity);
	}

	@Override
	public List<Long> retrieveWithMultipleStrategies(Identity identity) {
		List<RetrievalStrategy> retrievalStrategies = new  ArrayList<RetrievalStrategy>();

		// Retrieve by email.
		RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy(false);
		List<PubMedArticle> emailPubMedArticles = retrieve(emailRetrievalStrategy, identity);
		
		// TODO Send it to a separate thread to calculate the potential alias for a given author.
		
		Map<Long, AuthorName> aliasSet = new HashMap<Long, AuthorName>();
		for (PubMedArticle pubMedArticle : emailPubMedArticles) {
			for (MedlineCitationArticleAuthor author : pubMedArticle.getMedlineCitation().getArticle().getAuthorList()) {
				String affiliation = author.getAffiliation();
				if (affiliation != null) {
					for (String email : identity.getEmails()) {
						if (affiliation.contains(email)) {
							// possibility of an alias:
							if (author.getLastName().equals(identity.getAuthorName().getLastName())) {
								// sanity check: last name matches
								AuthorName alias = PubMedConverter.extractAuthorName(author);
								if (!alias.getFirstInitial().equals(identity.getAuthorName().getFirstInitial())) {
									// check if the same first initial is already added to the set.
									if (aliasSet.isEmpty()) {
										aliasSet.put(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid(), alias);
										slf4jLogger.info(identity.getCwid() + ": " + identity.getAuthorName() + ": (Empty set) Adding alias: " + alias);
									} else {
										for (AuthorName aliasAuthorName : aliasSet.values()) {
											if (!aliasAuthorName.getFirstInitial().equals(alias.getFirstInitial())) {
												aliasSet.put(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid(), alias);
												slf4jLogger.info(identity.getCwid() + ": " + identity.getAuthorName() + ": (Different first initial) Adding alias: " + alias);
												break;
											} else {
												String firstNameInSet = aliasAuthorName.getFirstName();
												String currentFirstName = alias.getFirstName();
												// prefer the name with the longer first name: i.e., prefer 'Clay' over 'C.'
												// so remove the 'C.' and add the 'Clay'
												if (firstNameInSet.length() < currentFirstName.length()) {
													aliasSet.remove(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
													aliasSet.put(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid(), alias);
													slf4jLogger.info(identity.getCwid() + ": " + identity.getAuthorName() + ": (Prefer longer first name) Adding alias: " + alias);
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

		List<PubMedAlias> pubMedAliases = new ArrayList<PubMedAlias>();
		for (Map.Entry<Long, AuthorName> entry : aliasSet.entrySet()) {
			PubMedAlias pubMedAlias = new PubMedAlias();
			pubMedAlias.setAuthorName(entry.getValue());
			pubMedAlias.setPmid(entry.getKey());
		}
		identity.setPubMedAliases(pubMedAliases);
		identity = identityService.save(identity);

//		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy(false);
//		RetrievalStrategy departmentRetrievalStrategy = new DepartmentRetrievalStrategy(false);
//		RetrievalStrategy affiliationInDbRetrievalStrategy = new AffiliationInDbRetrievalStrategy(false);
//		RetrievalStrategy grantRetrievalStrategy = new GrantRetrievalStrategy(false);
//
//		retrievalStrategies.add(emailRetrievalStrategy);
//		retrievalStrategies.add(firstNameInitialRetrievalStrategy);
//		retrievalStrategies.add(departmentRetrievalStrategy);
//		retrievalStrategies.add(affiliationInDbRetrievalStrategy);
//		retrievalStrategies.add(grantRetrievalStrategy);
		notifier.sendNotification(identity.getCwid());
		return retrieve(retrievalStrategies, identity);
	}

	/**
	 * Retrieve PubMed and Scopus articles for a single strategy.
	 * 
	 * @param retrievalStrategy
	 * @param identity
	 * 
	 * @return List of pmids that were retrieved
	 */
	private List<PubMedArticle> retrieve(RetrievalStrategy retrievalStrategy, Identity identity) {
		String cwid = identity.getCwid();
		List<Long> pmids = new ArrayList<Long>();
		List<PubMedArticle> pubMedArticles = new ArrayList<PubMedArticle>();
		try {
			retrievalStrategy.constructPubMedQuery(identity);
			slf4jLogger.info("cwid=[" + cwid + "], retrievalStrategy=[" + retrievalStrategy.getRetrievalStrategyName() 
			+ "], pubmedQuery=[" + retrievalStrategy.getPubMedQuery() + "]");

			List<Long> strategyPmids = new ArrayList<Long>();
			int numberOfPubmedArticles = retrievalStrategy.getNumberOfPubmedArticles();
			if (numberOfPubmedArticles > 0) {
				pubMedArticles = retrievalStrategy.retrieve();
				for (PubMedArticle pubMedArticle : pubMedArticles) {
					strategyPmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
				}
//				savePubMedArticles(pubMedArticles, cwid, retrievalStrategy.getRetrievalStrategyName());
			}
			List<ScopusArticle> scopusArticles = retrievalStrategy.retrieveScopus(strategyPmids);
//			scopusService.save(scopusArticles);

			pmids.addAll(strategyPmids);
		} catch (IOException e) {
			slf4jLogger.error("RetrievalStrategy " + retrievalStrategy + "encountered an IO Exception", e);
		}
		return pubMedArticles;
	}

	/**
	 * Retrieve articles.
	 * @param retrievalStrategies
	 * @param targetAuthor
	 * @return
	 */
	private List<Long> retrieve(List<RetrievalStrategy> retrievalStrategies, Identity identity) {
		String cwid = identity.getCwid();
		List<Long> pmids = new ArrayList<Long>();
		for (RetrievalStrategy retrievalStrategy : retrievalStrategies) {
			try {
				retrievalStrategy.constructPubMedQuery(identity);
				slf4jLogger.error("cwid=[" + cwid + "], retrievalStrategy=[" + retrievalStrategy.getRetrievalStrategyName() 
				+ "], pubmedQuery=[" + retrievalStrategy.getPubMedQuery() + "]");

				List<Long> strategyPmids = new ArrayList<Long>();
				int numberOfPubmedArticles = retrievalStrategy.getNumberOfPubmedArticles();
				if (numberOfPubmedArticles > 0) {
					List<PubMedArticle> pubMedArticles = retrievalStrategy.retrieve();
					for (PubMedArticle pubMedArticle : pubMedArticles) {
						strategyPmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
					}
					savePubMedArticles(pubMedArticles, cwid, retrievalStrategy.getRetrievalStrategyName());
				}
				List<ScopusArticle> scopusArticles = retrievalStrategy.retrieveScopus(strategyPmids);
				scopusService.save(scopusArticles);

				pmids.addAll(strategyPmids);
			} catch (IOException e) {
				slf4jLogger.error("RetrievalStrategy " + retrievalStrategy + "encountered an IO Exception", e);
			}
		}
		return pmids;
	}

	/**
	 * Save the PubMed articles and the ESearch results.
	 * @param pubMedArticles
	 * @param cwid
	 */
	private void savePubMedArticles(List<PubMedArticle> pubMedArticles, String cwid, String retrievalStrategyName) {
		// Save the articles.
		pubMedService.save(pubMedArticles);

		// Save the search result.
		List<Long> pmids = new ArrayList<Long>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			pmids.add(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid());
		}
		ESearchPmid eSearchPmid = new ESearchPmid(pmids, retrievalStrategyName, LocalDateTime.now());
		eSearchResultService.save(new ESearchResult(cwid, eSearchPmid));
	}

	public void updatePubMedQuery(RetrievalStrategy retrievalStrategy) throws IOException {

		String pubMedQuery = retrievalStrategy.getPubMedQuery();
		int numberOfPubmedArticles = retrievalStrategy.getNumberOfPubmedArticles();

		String[] pmidStrings = retrievalStrategy.retrievePmids(pubMedQuery);
		List<Long> pmids = new ArrayList<Long>();
		for (String pmidString : pmidStrings) {
			pmids.add(Long.valueOf(pmidString));
		}
		List<PubMedArticle> pubMedArticles = pubMedService.findByMedlineCitationMedlineCitationPMIDPmid(pmids);

		if (pubMedArticles != null && !pubMedArticles.isEmpty()) {
			String decodedeUrl = URLDecoder.decode(pubMedQuery, "UTF-8");
			if (pubMedArticles.size() == 1) {
				retrievalStrategy.setPubMedQuery(decodedeUrl + " NOT " + pubMedArticles.get(0).getMedlineCitation().getMedlineCitationPMID().getPmid() + "[pmid]");
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append("(");
				int index = 0;
				for (PubMedArticle pubMedArticle : pubMedArticles) {
					if (index != pubMedArticles.size() - 1) {
						sb.append(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid() + "[pmid] OR ");
					} else {
						sb.append(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid() + "[pmid]");
					}
					index++;
				}
				sb.append(")");
				retrievalStrategy.setPubMedQuery(decodedeUrl + " NOT " + sb.toString());
			}
			retrievalStrategy.setNumberOfPubmedArticles(numberOfPubmedArticles - pubMedArticles.size());
		}

		slf4jLogger.info("Updated PubMed query=[" + retrievalStrategy.getPubMedQuery() 
		+ "], number of articles = [" + retrievalStrategy.getNumberOfPubmedArticles() + "]");
	}
}
