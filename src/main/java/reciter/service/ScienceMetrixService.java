package reciter.service;

import reciter.database.dynamodb.model.ScienceMetrix;

import java.util.List;

public interface ScienceMetrixService {
    ScienceMetrix findByEissn(String eissn);
    ScienceMetrix findByIssn(String issn);
    ScienceMetrix findBySmsid(Long smsid);
}
