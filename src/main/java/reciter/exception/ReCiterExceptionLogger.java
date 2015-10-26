package reciter.exception;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReCiterExceptionLogger {
	
	public ReCiterExceptionLogger(String logFileName, ReCiterExceptionMessage exceptionMessage){
		applyHandler(logFileName,exceptionMessage);
	}
	
	private void applyHandler(String logFileName, ReCiterExceptionMessage exceptionMessage){
		Logger logger=Logger.getLogger(exceptionMessage.getClassName());
		logger.setUseParentHandlers(false);
		try {
			FileHandler logFile = new FileHandler(logFileName);
			logFile.setFormatter(new ReCiterExceptionLogFormatter());
			logger.addHandler(logFile);
			if(exceptionMessage.getMessage()!=null)logger.info(exceptionMessage.getMessage());
			logger.log(exceptionMessage.getPriority()==ReCiterPriority.HIGH_RISK?Level.SEVERE:Level.ALL, exceptionMessage.getErrorMessage());
			logFile.close();			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger=null;
	}
}
