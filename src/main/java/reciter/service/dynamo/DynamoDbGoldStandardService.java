package reciter.service.dynamo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.model.FeedbackLog;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.database.dynamodb.model.GoldStandardAuditLog;
import reciter.database.dynamodb.model.PmidProvenance;
import reciter.database.dynamodb.model.PublicationFeedback;
import reciter.database.dynamodb.repository.DynamoDbGoldStandardRepository;
import reciter.feedback.EntryPath;
import reciter.service.ArticleProvenanceService;
import reciter.service.ESearchResultService;
import reciter.service.FeedbackLogService;
import reciter.service.PmidProvenanceService;

@Service("DynamoDbGoldStandardService")
@RequiredArgsConstructor
public class DynamoDbGoldStandardService implements IDynamoDbGoldStandardService {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbGoldStandardService.class);
    private static final String PM_MANUAL_STRATEGY = "PublicationManagerManual";
    // Bounded optimistic-concurrency retries for the contended single-accept path.
    private static final int MAX_ATTEMPTS = 8;

    private final DynamoDbGoldStandardRepository dynamoDbGoldStandardRepository;
    private final ESearchResultService eSearchResultService;
    private final PmidProvenanceService pmidProvenanceService;
    private final FeedbackLogService feedbackLogService;
    private final ArticleProvenanceService articleProvenanceService;

    // ---- Phase 33-02 4-arg overloads -----------------------------------------------------------
    // Existing 3-arg save() callers default entryPath to CANDIDATE_LIST. Controllers that want to
    // distinguish PUBMED_SEARCH actions invoke the 4-arg overload directly.

    @Override
    public void save(GoldStandard goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag, String provenanceSource) {
        save(goldStandard, goldStandardUpdateFlag, provenanceSource, EntryPath.CANDIDATE_LIST);
    }

    @Override
    public void save(List<GoldStandard> goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag, String provenanceSource) {
        save(goldStandard, goldStandardUpdateFlag, provenanceSource, EntryPath.CANDIDATE_LIST);
    }

    @Override
    public void save(GoldStandard goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag,
                     String provenanceSource, EntryPath entryPath) {
        saveInternal(goldStandard, goldStandardUpdateFlag, provenanceSource, entryPath,0);
    }

    @Override
    public void save(GoldStandard goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag,
                     String provenanceSource, EntryPath entryPath, int curatedBy) {
        saveInternal(goldStandard, goldStandardUpdateFlag, provenanceSource, entryPath, curatedBy);
    }

    private void saveInternal(GoldStandard goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag, String provenanceSource, EntryPath entryPath, int curatedBy) {
    	// Resolve provenance strategy: caller-supplied source, or default
    	String strategy = (provenanceSource != null && !provenanceSource.isBlank())
    			? provenanceSource : PM_MANUAL_STRATEGY;

    	// Capture incoming request state before merge logic mutates it. Retries re-merge
    	// from this clean slate so the read-merge-write is idempotent across attempts.
    	List<Long> incomingAcceptedPmids = (goldStandard.getKnownPmids() != null)
    			? new ArrayList<>(goldStandard.getKnownPmids()) : Collections.emptyList();
    	List<Long> incomingRejectedPmids = (goldStandard.getRejectedPmids() != null)
    			? new ArrayList<>(goldStandard.getRejectedPmids()) : Collections.emptyList();
    	List<GoldStandardAuditLog> incomingAudit = (goldStandard.getAuditLog() == null)
    			? null : new ArrayList<>(goldStandard.getAuditLog());

    	if(goldStandardUpdateFlag == GoldStandardUpdateFlag.REFRESH) {
    		dynamoDbGoldStandardRepository.save(goldStandard);
    		return;
    	}

    	// Optimistic-concurrency retry loop: read -> merge -> conditional write. On a
    	// ConditionalCheckFailedException another replica committed first; re-read, re-merge
    	// and retry. Side-effect writes (FeedbackLog / ArticleProvenance / PmidProvenance) are
    	// deferred until AFTER the item durably commits so retries never duplicate them.
    	boolean committed = false;
    	List<Long> committedExistingAccepted = Collections.emptyList();
    	List<Long> committedExistingRejected = Collections.emptyList();
    	for (int attempt = 1; attempt <= MAX_ATTEMPTS && !committed; attempt++) {
    		// Reset the request object to the incoming state so each attempt merges idempotently.
    		goldStandard.setKnownPmids(new ArrayList<>(incomingAcceptedPmids));
    		goldStandard.setRejectedPmids(new ArrayList<>(incomingRejectedPmids));
    		goldStandard.setAuditLog(incomingAudit == null ? null : new ArrayList<>(incomingAudit));

    		GoldStandard goldStandardDdb = findByUid(goldStandard.getUid());
    		if(goldStandardDdb == null) {
    			// Create path: conditional on absence so a concurrent create is not clobbered.
    			// If another writer created it first, loop back to find + merge.
    			if(dynamoDbGoldStandardRepository.saveIfAbsent(goldStandard)) {
    				committed = true;
    				committedExistingAccepted = Collections.emptyList();
    				committedExistingRejected = Collections.emptyList();
    			}
    			continue;
    		}
    		List<Long> acceptedPmids = goldStandardDdb.getKnownPmids();
    		List<Long> rejectedPmids = goldStandardDdb.getRejectedPmids();
    		// Pre-image of the stored lists for the conditional write; null => attribute absent
    		// (handled by the attribute_not_exists branch of the condition expression).
    		List<Long> preKnown = (acceptedPmids != null) ? new ArrayList<>(acceptedPmids) : null;
    		List<Long> preRejected = (rejectedPmids != null) ? new ArrayList<>(rejectedPmids) : null;
    		// Snapshot existing lists before merge mutates them (for audit log diff)
    		List<Long> existingAccepted = (acceptedPmids != null) ? new ArrayList<>(acceptedPmids) : Collections.emptyList();
    		List<Long> existingRejected = (rejectedPmids != null) ? new ArrayList<>(rejectedPmids) : Collections.emptyList();
    			if(goldStandardUpdateFlag == GoldStandardUpdateFlag.DELETE) {
    				//This portion deals with cases when deleting a pmid from GoldStandard it will delete it from eSearchResult as well if it exists
    				ESearchResult eSearchResult = eSearchResultService.findByUid(goldStandard.getUid());
    				if(eSearchResult != null && eSearchResult.getESearchPmids() != null && eSearchResult.getESearchPmids().size() > 0) {
    					List<ESearchPmid> eSearchPmidGS = eSearchResult.getESearchPmids().stream().filter(eSearchPmid -> eSearchPmid.getRetrievalStrategyName().equalsIgnoreCase("GoldStandardRetrievalStrategy")).collect(Collectors.toList());
    					if(eSearchPmidGS != null && !eSearchPmidGS.isEmpty()) {
    						for(ESearchPmid eSearchPmid: eSearchPmidGS) {
		    					if(goldStandard.getKnownPmids() != null && goldStandard.getKnownPmids().size() > 0) {
		    						eSearchPmid.getPmids().removeAll(goldStandard.getKnownPmids());
		    					}
		    					if(goldStandard.getRejectedPmids() != null && goldStandard.getRejectedPmids().size() > 0) {
		    						eSearchPmid.getPmids().removeAll(goldStandard.getRejectedPmids());
		    					}
    						}
    					}
    					
    					eSearchResultService.save(eSearchResult);
    				}
    				
    				if(acceptedPmids != null && acceptedPmids.size() > 0) {
        				if(goldStandard.getKnownPmids() != null && goldStandard.getKnownPmids().size() > 0) {
        					for(Long acceptedPmid: goldStandard.getKnownPmids()) {
        						if(acceptedPmids.contains(acceptedPmid)) {
    	    						acceptedPmids.remove(acceptedPmid);
    	    					}
        					}
        				}
        				
        			}
    				
    				if(rejectedPmids != null && rejectedPmids.size() > 0) {
        				if(goldStandard.getRejectedPmids() != null && goldStandard.getRejectedPmids().size() > 0) {
        					for(Long rejectedPmid: goldStandard.getRejectedPmids()) {
        						if(rejectedPmids.contains(rejectedPmid)) {
        							rejectedPmids.remove(rejectedPmid);
    	    					}
        					}
        				}
        			}
    				if(acceptedPmids == null) {
    					goldStandard.setKnownPmids(new ArrayList<Long>());
    				} else {
    					goldStandard.setKnownPmids(acceptedPmids);
    				}
    				if(rejectedPmids == null) {
    					goldStandard.setRejectedPmids(new ArrayList<Long>());
    				} else {
    					goldStandard.setRejectedPmids(rejectedPmids);
    				}
    			} else if(goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE) {
    			
	     			if(acceptedPmids != null && acceptedPmids.size() > 0) {
	    				if(goldStandard.getKnownPmids() != null && goldStandard.getKnownPmids().size() > 0) {
		    				for(Long acceptedPmid: goldStandard.getKnownPmids()) {
		    					if(!acceptedPmids.contains(acceptedPmid)) {
		    						acceptedPmids.add(acceptedPmid);
		    					}
		    					if(rejectedPmids != null && rejectedPmids.size() > 0) {
		    						if(rejectedPmids.contains(acceptedPmid)) {
		    							rejectedPmids.remove(acceptedPmid);
		    						}
		    					}
		    				}
	    				}
	    				goldStandard.setKnownPmids(acceptedPmids);
	    			} else {
	    				if(goldStandard.getKnownPmids() != null && goldStandard.getKnownPmids().size() > 0) {
	    					for(Long acceptedPmid: goldStandard.getKnownPmids()) {
	    						if(goldStandard.getRejectedPmids() != null) {
		    						if(goldStandard.getRejectedPmids().contains(acceptedPmid)) {
		    							goldStandard.getRejectedPmids().remove(acceptedPmid);
		    						}
	    						}
	    					}
	    				}
	    			}
	     			
	    			if(rejectedPmids != null && rejectedPmids.size() > 0) {
	    				if(goldStandard.getRejectedPmids() != null && goldStandard.getRejectedPmids().size() > 0) {
		    				for(Long rejectedPmid: goldStandard.getRejectedPmids()) {
		    					if(!rejectedPmids.contains(rejectedPmid)) {
		    						rejectedPmids.add(rejectedPmid);
		    					}
		    					if(acceptedPmids != null && acceptedPmids.size() > 0) {
		    						if(acceptedPmids.contains(rejectedPmid)) {
		    							acceptedPmids.remove(rejectedPmid);
		    						}
		    					}
		    				}
	    				}
	    				goldStandard.setRejectedPmids(rejectedPmids);
	    			} else {
	    				if(goldStandard.getRejectedPmids() != null && goldStandard.getRejectedPmids().size() > 0) {
	    					for(Long rejectedPmid: goldStandard.getRejectedPmids()) {
	    						if(goldStandard.getKnownPmids() != null) {
		    						if(goldStandard.getKnownPmids().contains(rejectedPmid)) {
		    							goldStandard.getKnownPmids().remove(rejectedPmid);
		    						}
	    						}
	    					}
	    				}
	    			}
    			}
    			
    			if(goldStandardDdb.getAuditLog() != null
						&&
						goldStandardDdb.getAuditLog().size() > 0) {
					if(goldStandard.getAuditLog() != null
							&&
							goldStandard.getAuditLog().size() > 0) {
						goldStandard.getAuditLog().addAll(goldStandardDdb.getAuditLog());
					} else {
						goldStandard.setAuditLog(goldStandardDdb.getAuditLog());
					}
				}
    			// Create audit log entries for changes in this update
    			if (goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE) {
    				List<GoldStandardAuditLog> newEntries = buildAuditEntries(
    						goldStandard.getUid(), incomingAcceptedPmids, incomingRejectedPmids,
    						existingAccepted, existingRejected, strategy);
    				if (!newEntries.isEmpty()) {
    					List<GoldStandardAuditLog> auditLog = goldStandard.getAuditLog();
    					if (auditLog == null) {
    						auditLog = new ArrayList<>();
    					}
    					auditLog.addAll(newEntries);
    					goldStandard.setAuditLog(auditLog);
    				}
    			}
    			// Conditional persist guarded on the pre-image of knownpmids/rejectedpmids.
    			// false => another replica committed first: back off briefly and retry.
    			if(dynamoDbGoldStandardRepository.saveIfUnchanged(goldStandard, preKnown, preRejected)) {
    				committed = true;
    				committedExistingAccepted = existingAccepted;
    				committedExistingRejected = existingRejected;
    			} else {
    				try {
    					Thread.sleep(20 + ThreadLocalRandom.current().nextInt(60));
    				} catch (InterruptedException ie) {
    					Thread.currentThread().interrupt();
    					throw new RuntimeException("Interrupted while retrying GoldStandard update for uid=" + goldStandard.getUid(), ie);
    				}
    			}
    		}

    	if (!committed) {
    		throw new RuntimeException("GoldStandard update contended after " + MAX_ATTEMPTS + " attempts for uid=" + goldStandard.getUid());
    	}

    	// Side-effect writes run once, AFTER the item is durably committed, using the
    	// existing-state snapshot from the attempt that actually committed so the diff
    	// reflects the true transition. Retries never reach here => no duplicate FeedbackLog.
    	if (goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE) {
    		recordFeedbackLogAndArticleProvenance(
    				goldStandard.getUid(),
    				incomingAcceptedPmids, incomingRejectedPmids,
    				committedExistingAccepted, committedExistingRejected,
    				entryPath, curatedBy);
    	}

    	// Track provenance for accepted PMIDs. saveIfNotExists ensures we
    	// don't overwrite existing automated-retrieval provenance — only
    	// truly new PMIDs (e.g., manually added via Publication Manager)
    	// get a provenance record.
    	if (goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE
    			&& !incomingAcceptedPmids.isEmpty()) {
    		writeProvenanceForAcceptedPmids(goldStandard.getUid(), incomingAcceptedPmids, strategy);
    	}
    }

    @Override
    public GoldStandard findByUid(String uid) {
        return dynamoDbGoldStandardRepository.findById(uid).orElseGet(() -> null);
    }

	@Override
	public void save(List<GoldStandard> goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag,
	                 String provenanceSource, EntryPath entryPath) {
		saveListInternal(goldStandard, goldStandardUpdateFlag, provenanceSource, entryPath);
	}

	// TODO(goldstandard-race): saveListInternal has the same non-atomic read-modify-write; apply saveIfUnchanged+retry here too (lower priority — interactive single-accept path is saveInternal).
	private void saveListInternal(List<GoldStandard> goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag, String provenanceSource, EntryPath entryPath) {
		// Resolve provenance strategy: caller-supplied source, or default
		String strategy = (provenanceSource != null && !provenanceSource.isBlank())
				? provenanceSource : PM_MANUAL_STRATEGY;
		// Capture incoming PMIDs before merge logic mutates them
		Map<String, List<Long>> incomingAcceptedPmidsMap = new HashMap<>();
		Map<String, List<Long>> incomingRejectedPmidsMap = new HashMap<>();
		if (goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE) {
			for (GoldStandard gs : goldStandard) {
				if (gs.getKnownPmids() != null && !gs.getKnownPmids().isEmpty()) {
					incomingAcceptedPmidsMap.put(gs.getUid(), new ArrayList<>(gs.getKnownPmids()));
				}
				if (gs.getRejectedPmids() != null && !gs.getRejectedPmids().isEmpty()) {
					incomingRejectedPmidsMap.put(gs.getUid(), new ArrayList<>(gs.getRejectedPmids()));
				}
			}
		}

		if(goldStandardUpdateFlag == GoldStandardUpdateFlag.REFRESH) {
    		dynamoDbGoldStandardRepository.saveAll(goldStandard);
    	} else {
    		List<String> goldStandardUids = goldStandard.stream().map(GoldStandard::getUid).collect(Collectors.toList());
    		
    		List<GoldStandard> goldStandardDdbList = findByUids(goldStandardUids);
    		if(goldStandardDdbList == null|| (goldStandardDdbList != null && goldStandardDdbList.size() == 0)) {
    			dynamoDbGoldStandardRepository.saveAll(goldStandard);
    		} else {
    			for(GoldStandard goldStandardDdb: goldStandardDdbList) {
    				List<Long> acceptedPmids = goldStandardDdb.getKnownPmids();
    				List<Long> existingAccepted = (acceptedPmids != null) ? new ArrayList<>(acceptedPmids) : Collections.emptyList();
    				List<Long> existingRejected = (goldStandardDdb.getRejectedPmids() != null) ? new ArrayList<>(goldStandardDdb.getRejectedPmids()) : Collections.emptyList();
    				GoldStandard goldStandardNew = goldStandard.stream().filter(gs -> gs.getUid().equalsIgnoreCase(goldStandardDdb.getUid())).findFirst().get();
        			if(acceptedPmids != null && acceptedPmids.size() > 0) {
        				if(goldStandardNew != null && goldStandardNew.getKnownPmids() != null && goldStandardNew.getKnownPmids().size() > 0) {
	        				for(Long acceptedPmidNew: goldStandardNew.getKnownPmids()) {
	        					if(!acceptedPmids.contains(acceptedPmidNew)) {
	        						acceptedPmids.add(acceptedPmidNew);
	        					}
	        				}
        				}
        				goldStandardNew.setKnownPmids(acceptedPmids);
        			}
        			
        			List<Long> rejectedPmids = goldStandardDdb.getRejectedPmids();
        			if(rejectedPmids != null && rejectedPmids.size() > 0) {
        				if(goldStandardNew != null && goldStandardNew.getRejectedPmids() != null && goldStandardNew.getRejectedPmids().size() > 0) {
	        				for(Long rejectedPmidNew: goldStandardNew.getRejectedPmids()) {
	        					if(!rejectedPmids.contains(rejectedPmidNew)) {
	        						rejectedPmids.add(rejectedPmidNew);
	        					}
	        				}
        				}
        				goldStandardNew.setRejectedPmids(rejectedPmids);
        			}
        			
        			if(goldStandardDdb.getAuditLog() != null
    						&&
    						goldStandardDdb.getAuditLog().size() > 0) {
    					if(goldStandardNew.getAuditLog() != null
    							&&
    							goldStandardNew.getAuditLog().size() > 0) {
    						goldStandardNew.getAuditLog().addAll(goldStandardDdb.getAuditLog());
    					} else {
    						goldStandardNew.setAuditLog(goldStandardDdb.getAuditLog());
    					}
    				}
    				// Create audit log entries for changes in this batch update
    				if (goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE) {
    					String uid = goldStandardNew.getUid();
    					List<Long> batchIncomingAccepted = incomingAcceptedPmidsMap.getOrDefault(uid, Collections.emptyList());
    					List<Long> batchIncomingRejected = incomingRejectedPmidsMap.getOrDefault(uid, Collections.emptyList());
    					List<GoldStandardAuditLog> newEntries = buildAuditEntries(
    							uid, batchIncomingAccepted, batchIncomingRejected,
    							existingAccepted, existingRejected, strategy);
    					if (!newEntries.isEmpty()) {
    						List<GoldStandardAuditLog> auditLog = goldStandardNew.getAuditLog();
    						if (auditLog == null) {
    							auditLog = new ArrayList<>();
    						}
    						auditLog.addAll(newEntries);
    						goldStandardNew.setAuditLog(auditLog);
    					}
    					// Phase 33-02: per-uid FeedbackLog + ArticleProvenance writes for the diff.
    					// Bulk/list (PUT) path carries no interactive curator id -> curatedBy = 0.
    					recordFeedbackLogAndArticleProvenance(uid,
    							batchIncomingAccepted, batchIncomingRejected,
    							existingAccepted, existingRejected, entryPath,0);
    				}
    			}
    			dynamoDbGoldStandardRepository.saveAll(goldStandard);
    		}
    	}

    	// Track provenance for accepted PMIDs in batch
    	for (Map.Entry<String, List<Long>> entry : incomingAcceptedPmidsMap.entrySet()) {
    		writeProvenanceForAcceptedPmids(entry.getKey(), entry.getValue(), strategy);
    	}
	}


	@Override
	public List<GoldStandard> findByUids(List<String> uids) {
		List<GoldStandard> goldStanards = null;
		Iterator<GoldStandard> iterator = dynamoDbGoldStandardRepository.findAllById(uids).iterator();
		goldStanards = new ArrayList<>(uids.size());
		while(iterator.hasNext()) {
			goldStanards.add(iterator.next());
		}
		return goldStanards;
	}

	@Override
	public void delete(String uid) {
		dynamoDbGoldStandardRepository.deleteById(uid);
	}

	/**
	 * Build audit log entries by diffing incoming PMIDs against existing.
	 * Only creates entries for genuinely new acceptances/rejections.
	 */
	private List<GoldStandardAuditLog> buildAuditEntries(
			String uid, List<Long> incomingAccepted, List<Long> incomingRejected,
			List<Long> existingAccepted, List<Long> existingRejected, String source) {
		List<GoldStandardAuditLog> entries = new ArrayList<>();
		Date now = new Date();

		List<Long> newlyAccepted = new ArrayList<>(incomingAccepted);
		newlyAccepted.removeAll(existingAccepted);
		if (!newlyAccepted.isEmpty()) {
			entries.add(GoldStandardAuditLog.builder()
					.userVerbose(source)
					.uid(uid)
					.dateTime(Instant.now())
					.pmids(newlyAccepted)
					.action(PublicationFeedback.ACCEPTED)
					.build());
		}

		List<Long> newlyRejected = new ArrayList<>(incomingRejected);
		newlyRejected.removeAll(existingRejected);
		if (!newlyRejected.isEmpty()) {
			entries.add(GoldStandardAuditLog.builder()
					.userVerbose(source)
					.uid(uid)
					.dateTime(Instant.now())
					.pmids(newlyRejected)
					.action(PublicationFeedback.REJECTED)
					.build());
		}

		if (!entries.isEmpty()) {
			log.info("Audit log: {} new accepted, {} new rejected for uid={}",
					newlyAccepted.size(), newlyRejected.size(), uid);
		}
		return entries;
	}

	/**
	 * Phase 33-02: emit FeedbackLog rows + ArticleProvenance D-11/D-13 transitions for
	 * every curator action implied by the diff between incoming GS state and existing
	 * GS state. Three buckets:
	 *
	 * <ul>
	 *   <li><b>newly ACCEPTED</b> — pmids in {@code incomingAccepted} not in {@code existingAccepted}</li>
	 *   <li><b>newly REJECTED</b> — pmids in {@code incomingRejected} not in {@code existingRejected}</li>
	 *   <li><b>newly PENDING</b> — pmids in {@code (existingAccepted ∪ existingRejected)} that are
	 *       NOT in {@code (incomingAccepted ∪ incomingRejected)} (state transition to pending)</li>
	 * </ul>
	 *
	 * <p>Each bucket emits one FeedbackLog row per pmid + one ArticleProvenance D-11
	 * transition. PUBMED_SEARCH entry path additionally writes the rs='PM_UI_SEARCH' record
	 * before the D-11 transition (D-13). Both services log and continue on failure;
	 * curator request never blocks.
	 *
	 * <p>{@code curatedBy} is the curating user's id (admin_users.userID) supplied by
	 * Publication Manager via the {@code curatedBy} request param; it is 0 when the
	 * caller does not provide one (e.g., bulk/list updates or non-PM callers).
	 */
	private void recordFeedbackLogAndArticleProvenance(String uid,
			List<Long> incomingAccepted, List<Long> incomingRejected,
			List<Long> existingAccepted, List<Long> existingRejected,
			EntryPath entryPath,int curatedBy) {
		long actionEpoch = System.currentTimeMillis() / 1000L;
		Set<Long> incomingAccSet = new HashSet<>(incomingAccepted);
		Set<Long> incomingRejSet = new HashSet<>(incomingRejected);
		Set<Long> existingAccSet = new HashSet<>(existingAccepted);
		Set<Long> existingRejSet = new HashSet<>(existingRejected);

		Set<Long> newlyAccepted = new HashSet<>(incomingAccSet);
		newlyAccepted.removeAll(existingAccSet);

		Set<Long> newlyRejected = new HashSet<>(incomingRejSet);
		newlyRejected.removeAll(existingRejSet);

		Set<Long> previouslyClassified = new HashSet<>();
		previouslyClassified.addAll(existingAccSet);
		previouslyClassified.addAll(existingRejSet);
		Set<Long> stillClassified = new HashSet<>();
		stillClassified.addAll(incomingAccSet);
		stillClassified.addAll(incomingRejSet);
		Set<Long> newlyPending = new HashSet<>(previouslyClassified);
		newlyPending.removeAll(stillClassified);

		int total = newlyAccepted.size() + newlyRejected.size() + newlyPending.size();
		if (total == 0) {
			return;
		}
		log.info("Phase 33-02: feedback diff for uid={} entryPath={}: +{} accepted, +{} rejected, +{} pending",
				uid, entryPath, newlyAccepted.size(), newlyRejected.size(), newlyPending.size());

		for (Long pmid : newlyAccepted) {
			FeedbackLog logEntry = new FeedbackLog();
			logEntry.setUid(uid);
		    logEntry.setArticleId(String.valueOf(pmid));
		    logEntry.setFeedback(FeedbackLogService.Feedback.ACCEPTED.name());
		    logEntry.setCuratedBy(curatedBy);
		    logEntry.setCreateTimestamp(actionEpoch);
		    logEntry.setModifyTimestamp(actionEpoch);
			
			feedbackLogService.recordAction(logEntry);
			articleProvenanceService.upsertCuratorAction(uid, pmid, entryPath, actionEpoch);
		}
		for (Long pmid : newlyRejected) {
			FeedbackLog logEntry = new FeedbackLog();
		    logEntry.setUid(uid);
		    logEntry.setArticleId(String.valueOf(pmid));
		    logEntry.setFeedback(FeedbackLogService.Feedback.REJECTED.name());
		    logEntry.setCuratedBy(curatedBy);
		    logEntry.setCreateTimestamp(actionEpoch);
		    logEntry.setModifyTimestamp(actionEpoch);
			feedbackLogService.recordAction(logEntry);
			articleProvenanceService.upsertCuratorAction(uid, pmid, entryPath, actionEpoch);
		}
		for (Long pmid : newlyPending) {
			FeedbackLog logEntry = new FeedbackLog();
		    logEntry.setUid(uid);
		    logEntry.setArticleId(String.valueOf(pmid));
		    logEntry.setFeedback(FeedbackLogService.Feedback.PENDING.name());
		    logEntry.setCuratedBy(curatedBy);
		    logEntry.setCreateTimestamp(actionEpoch);
		    logEntry.setModifyTimestamp(actionEpoch);
			feedbackLogService.recordAction(logEntry);
			articleProvenanceService.upsertCuratorAction(uid, pmid, entryPath, actionEpoch);
		}
	}

	/**
	 * Write PmidProvenance records for accepted PMIDs. Uses saveIfNotExists
	 * so that PMIDs already discovered by automated retrieval strategies
	 * keep their original provenance. Only PMIDs with no existing provenance
	 * (e.g., manually added via Publication Manager) get a new record.
	 */
	private void writeProvenanceForAcceptedPmids(String uid, List<Long> pmids, String strategy) {
		List<PmidProvenance> provenanceRecords = new ArrayList<>();
		Date now = new Date();
		for (Long pmid : pmids) {
			PmidProvenance provenance = new PmidProvenance(uid, pmid, Instant.now(), strategy);
			provenanceRecords.add(provenance);
		}
		pmidProvenanceService.saveAllIfNotExists(provenanceRecords);
		log.info("Tracked provenance ({}) for {} accepted PMIDs for uid={}", strategy, pmids.size(), uid);
	}
}

