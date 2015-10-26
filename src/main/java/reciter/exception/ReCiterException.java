package reciter.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ReCiterException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2931955226989010018L;
	
	private String errorMessage;
	private String stackTraceMessage;
	
	//private ReCiterException(){}
	//private ReCiterException(String message){super(message);this.errorMessage=message;}
	public ReCiterException(Throwable t){super(t);this.stackTraceMessage=getStackTrace(t);}
	public ReCiterException(String message, Throwable t){super(message,t);this.errorMessage=message;this.stackTraceMessage=getStackTrace(t);}
	/*private ReCiterException(String message, Throwable t, boolean enableSuppression, boolean writableStackTrace){
		super(message, t, enableSuppression, writableStackTrace);
		this.errorMessage=message;
		this.stackTraceMessage=getStackTrace(t);
	}*/
	
	public static String getStackTrace(final Throwable throwable) {
	     final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     return sw.getBuffer().toString();
	}
	
	public String getErrorMessage() {
		return errorMessage;//==null?stackTraceMessage:errorMessage;
	}
	
	public String getStackTraceMessage() {
		return stackTraceMessage;
	}
	
}
