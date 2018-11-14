package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.database.dynamodb.repository.DynamoDbGoldStandardRepository;

@Service("DynamoDbGoldStandardService")
public class DynamoDbGoldStandardService implements IDynamoDbGoldStandardService {

    @Autowired
    private DynamoDbGoldStandardRepository dynamoDbGoldStandardRepository;

    @Override
    public void save(GoldStandard goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag) {
    	if(goldStandardUpdateFlag == GoldStandardUpdateFlag.REFRESH) {
    		dynamoDbGoldStandardRepository.save(goldStandard);
    	} else {
    		GoldStandard goldStandardDdb = findByUid(goldStandard.getUid());
    		if(goldStandardDdb == null) {
    			dynamoDbGoldStandardRepository.save(goldStandard);
    		} else {
    			List<Long> acceptedPmids = goldStandardDdb.getKnownPmids();
    			List<Long> rejectedPmids = goldStandardDdb.getRejectedPmids();
    			if(goldStandardUpdateFlag == GoldStandardUpdateFlag.DELETE) {
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
    			dynamoDbGoldStandardRepository.save(goldStandard);
    		}
    	}
        
    }

    @Override
    public GoldStandard findByUid(String uid) {
        return dynamoDbGoldStandardRepository.findById(uid).orElseGet(() -> null);
    }

	@Override
	public void save(List<GoldStandard> goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag) {
		
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
    			}
    			dynamoDbGoldStandardRepository.saveAll(goldStandard);
    		}
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
		
}