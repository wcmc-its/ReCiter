package reciter.xml.retriever.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reciter.database.mongo.model.Identity;
import reciter.database.mongo.model.PubMedAlias;
import reciter.model.author.AuthorName;
import reciter.model.converter.PubMedConverter;
import reciter.model.pubmed.MedlineCitationArticleAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;

@Component("aliasReCiterRetrievalEngine")
public class AliasReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AliasReCiterRetrievalEngine.class);

	@Override
	public Set<Long> retrieve(Identity identity) throws IOException {
		Set<Long> uniquePmids = new HashSet<Long>();
		
		String cwid = identity.getCwid();
		
		// Retrieve by email.
		Map<Long, PubMedArticle> emailPubMedArticles = emailRetrievalStrategy.retrievePubMedArticles(identity);
		
		if (emailPubMedArticles.size() > 0) {
			Map<Long, AuthorName> aliasSet = calculatePotentialAlias(identity, emailPubMedArticles.values());

			// Update alias.
			List<PubMedAlias> pubMedAliases = new ArrayList<PubMedAlias>();
			for (Map.Entry<Long, AuthorName> entry : aliasSet.entrySet()) {
				PubMedAlias pubMedAlias = new PubMedAlias();
				pubMedAlias.setAuthorName(entry.getValue());
				pubMedAlias.setPmid(entry.getKey());
				slf4jLogger.info("new alias for cwid=[" + identity.getCwid() + "], alias=[" + entry.getValue() + "] from pmid=[" + entry.getKey() + "]");
				pubMedAliases.add(pubMedAlias);
			}

			identity.setPubMedAliases(pubMedAliases);
			identityService.updatePubMedAlias(identity);
			
			uniquePmids.addAll(emailPubMedArticles.keySet());
		}
		
		// TODO parallelize by putting save in a separate thread.
		savePubMedArticles(emailPubMedArticles.values(), cwid, emailRetrievalStrategy.getRetrievalStrategyName());
		
		Map<Long, PubMedArticle> r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity);
		if (r1.size() > 0) {
			savePubMedArticles(r1.values(), cwid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName());
			uniquePmids.addAll(r1.keySet());
		} else {
			Map<Long, PubMedArticle> r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity);
			savePubMedArticles(r2.values(), cwid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName());
			uniquePmids.addAll(r2.keySet());
			
			Map<Long, PubMedArticle> r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity);
			savePubMedArticles(r3.values(), cwid, affiliationRetrievalStrategy.getRetrievalStrategyName());
			uniquePmids.addAll(r3.keySet());
			
			Map<Long, PubMedArticle> r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity);
			savePubMedArticles(r4.values(), cwid, departmentRetrievalStrategy.getRetrievalStrategyName());
			uniquePmids.addAll(r4.keySet());
			
			Map<Long, PubMedArticle> r5 = grantRetrievalStrategy.retrievePubMedArticles(identity);
			savePubMedArticles(r5.values(), cwid, grantRetrievalStrategy.getRetrievalStrategyName());
			uniquePmids.addAll(r5.keySet());
		}
		
		notifier.sendNotification(identity.getCwid());
		
		List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);
		scopusService.save(scopusArticles);
		
		return uniquePmids;
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
		return aliasSet;
	}
}
