package reciter.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReCiterExceptionTransactioner {
	private static Map<ReCiterTransactionerName,ReCiterExceptionTransactioner> transactioners = new HashMap<ReCiterTransactionerName, ReCiterExceptionTransactioner>();
	private static Object lock=new Object();
	private ReCiterTransactionerName transactionerName;
	
	private ReCiterPriority previousPriority=ReCiterPriority.HIGH_RISK;
	private Map<ReCiterPriority, List<ReCiterExceptionMessage>> exceptionMap = new HashMap<ReCiterPriority, List<ReCiterExceptionMessage>>();
	
	private ReCiterExceptionTransactioner(ReCiterTransactionerName transactionerName){
		this.transactionerName=transactionerName;
	}
	
	/*
	 * 
	 */
	public static synchronized ReCiterExceptionTransactioner getTransactioner(ReCiterTransactionerName name){
		synchronized (lock) {
			if(!transactioners.containsKey(name)){
				ReCiterExceptionTransactioner transactioner = new ReCiterExceptionTransactioner(name);
				transactioners.put(name, transactioner);
			}
			return transactioners.get(name);
		}
	}
	
	private void setPriorityMessage(ReCiterPriority priority, String className, String message, String errMessage){
		if(!exceptionMap.containsKey(priority))exceptionMap.put(priority, new ArrayList<ReCiterExceptionMessage>());
		List<ReCiterExceptionMessage> list = exceptionMap.get(priority);
		ReCiterExceptionMessage exceptionMessage = new ReCiterExceptionMessage();
		exceptionMessage.setClassName(className);
		exceptionMessage.setErrorMessage(errMessage);
		exceptionMessage.setMessage(message);
		exceptionMessage.setPriority(priority);
		list.add(exceptionMessage);
	}
	
	/*
	 * 
	 */	
	public void raiseReCiterExcption(ReCiterPriority priority, Object classInstance, Throwable cause, String message){
		Class c = classInstance.getClass();
		ReCiterException reCiterException = message==null?new ReCiterException(cause):new ReCiterException(message,cause);
		String errorMessage = reCiterException.getStackTraceMessage();
		setPriorityMessage(priority, c.getName(), message, errorMessage);
		if(priority==ReCiterPriority.HIGH_RISK){
			generateErrorLog();
			System.exit(0); // TODO - needs to analyze
		}
		this.previousPriority=priority;
	}
	
	/*
	 *The below method can be called before exiting the process or if any error raised 
	 */
	public void generateErrorLog(){
		for(Map.Entry<ReCiterPriority, List<ReCiterExceptionMessage>> entry: exceptionMap.entrySet()){
			List<ReCiterExceptionMessage> list = entry.getValue();
			for(ReCiterExceptionMessage msg: entry.getValue()){
				ReCiterExceptionLogger logger = new ReCiterExceptionLogger(this.transactionerName.getFileName(),msg);
				logger=null;
			}
			list.clear();
		}
	}
	
	/*
	 * 
	 */
	public void raiseReCiterExcption(ReCiterPriority priority, Object classInstance, Throwable cause){
		raiseReCiterExcption(priority,classInstance,cause, null);
	}

	/**
	 * @return the transactionerName
	 */
	public ReCiterTransactionerName getTransactionerName() {
		return transactionerName;
	}

	/**
	 * @return the previousPriority
	 */
	public ReCiterPriority getPreviousPriority() {
		return previousPriority;
	}
}
