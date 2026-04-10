package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.database.dynamodb.model.GoldStandardAuditLog;
import reciter.database.dynamodb.model.PmidProvenance;
import reciter.database.dynamodb.model.PublicationFeedback;
import reciter.database.dynamodb.repository.DynamoDbGoldStandardRepository;
import reciter.service.ESearchResultService;
import reciter.service.PmidProvenanceService;

@Service("DynamoDbGoldStandardService")
public class DynamoDbGoldStandardService implements IDynamoDbGoldStandardService {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbGoldStandardService.class);
    private static final String PM_MANUAL_STRATEGY = "PublicationManagerManual";

    @Autowired
    private DynamoDbGoldStandardRepository dynamoDbGoldStandardRepository;

    @Autowired
    private ESearchResultService eSearchResultService;

    @Autowired
    private PmidProvenanceService pmidProvenanceService;

    @Override
    public void save(GoldStandard goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag, String provenanceSource) {
    	// Resolve provenance strategy: caller-supplied source, or default
    	String strategy = (provenanceSource != null && !provenanceSource.isBlank())
    			? provenanceSource : PM_MANUAL_STRATEGY;

    	// Capture incoming PMIDs before merge logic mutates them
    	List<Long> incomingAcceptedPmids = (goldStandard.getKnownPmids() != null)
    			? new ArrayList<>(goldStandard.getKnownPmids()) : Collections.emptyList();
    	List<Long> incomingRejectedPmids = (goldStandard.getRejectedPmids() != null)
    			? new ArrayList<>(goldStandard.getRejectedPmids()) : Collections.emptyList();

    	if(goldStandardUpdateFlag == GoldStandardUpdateFlag.REFRESH) {
    		dynamoDbGoldStandardRepository.save(goldStandard);
    	} else {
    		GoldStandard goldStandardDdb = findByUid(goldStandard.getUid());
    		if(goldStandardDdb == null) {
    			dynamoDbGoldStandardRepository.save(goldStandard);
    		} else {
    			List<Long> acceptedPmids = goldStandardDdb.getKnownPmids();
    			List<Long> rejectedPmids = goldStandardDdb.getRejectedPmids();
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
    			dynamoDbGoldStandardRepository.save(goldStandard);
    		}
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
	public void save(List<GoldStandard> goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag, String provenanceSource) {
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
					.dateTime(now)
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
					.dateTime(now)
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
					.dateTime(now)
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
					.dateTime(now)
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
	 * Write PmidProvenance records for accepted PMIDs. Uses saveIfNotExists
	 * so that PMIDs already discovered by automated retrieval strategies
	 * keep their original provenance. Only PMIDs with no existing provenance
	 * (e.g., manually added via Publication Manager) get a new record.
	 */
	private void writeProvenanceForAcceptedPmids(String uid, List<Long> pmids, String strategy) {
		List<PmidProvenance> provenanceRecords = new ArrayList<>();
		Date now = new Date();
		for (Long pmid : pmids) {
			PmidProvenance provenance = new PmidProvenance(uid, pmid, now, strategy);
			provenanceRecords.add(provenance);
		}
		pmidProvenanceService.saveAllIfNotExists(provenanceRecords);
		log.info("Tracked provenance ({}) for {} accepted PMIDs for uid={}", strategy, pmids.size(), uid);
	}
}

