//package reciter.service.dynamo;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import reciter.database.dynamodb.model.UserFeedback;
//import reciter.database.dynamodb.repository.UserFeedbackRepository;
//import reciter.service.UserFeedbackService;
//
//@Service("UserFeedbackService")
//public class UserFeedbackServiceImpl implements UserFeedbackService {
//	
//	@Autowired
//	private UserFeedbackRepository userFeedbackRepository;
//
//	@Override
//	public void save(UserFeedback userFeedback) {
//		UserFeedback userFeedbackDdb = findByUid(userFeedback.getUid());
//		if(userFeedbackDdb == null) {
//			userFeedbackRepository.save(userFeedback);
//		} else {
//			if(userFeedback.getAcceptedPmids() != null && !userFeedback.getAcceptedPmids().isEmpty() && userFeedbackDdb.getAcceptedPmids() != null) {
//				userFeedbackDdb.getAcceptedPmids().addAll(userFeedback.getAcceptedPmids());
//			}
//			if(userFeedback.getRejectedPmids() != null && !userFeedback.getRejectedPmids().isEmpty() && userFeedbackDdb.getRejectedPmids() != null) {
//				userFeedbackDdb.getRejectedPmids().addAll(userFeedback.getRejectedPmids());
//			}
//			userFeedbackDdb.setFeedbackDate(userFeedback.getFeedbackDate());
//			userFeedbackRepository.save(userFeedbackDdb);
//		}
//	}
//
//	@Override
//	public UserFeedback findByUid(String uid) {
//		UserFeedback userFeedback = userFeedbackRepository.findById(uid).orElseGet(() -> null);
//        return userFeedback;
//	}
//
//	@Override
//	public void delete(String uid) {
//		userFeedbackRepository.deleteById(uid);
//	}
//
//}
