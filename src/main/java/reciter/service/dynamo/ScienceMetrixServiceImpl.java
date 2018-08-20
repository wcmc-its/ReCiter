package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.database.dynamodb.repository.ScienceMetrixRepository;
import reciter.service.ScienceMetrixService;

import java.util.List;

@Service
public class ScienceMetrixServiceImpl implements ScienceMetrixService {

    @Autowired
    private ScienceMetrixRepository scienceMetrixRepository;

   /* @Override
    public ScienceMetrix findByEissn(String eissn) {
        return scienceMetrixRepository.findByEissn(eissn);
    }

    @Override
    public ScienceMetrix findByIssn(String issn) {
        return scienceMetrixRepository.findByIssn(issn);
    }*/

    @Override
    public ScienceMetrix findBySmsid(Long smsid) {
        //return scienceMetrixRepository.findById(smsid).orElseGet(() -> null);
    	return scienceMetrixRepository.findOne(smsid);
    }
}
