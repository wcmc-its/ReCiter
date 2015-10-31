package reciter.exception;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum ReCiterTransactionerName {
	RECITER(1,"ReCiter");
	// We can create more transactioner names with comma seperated values.
	
	private final int nameCode;
	private String fileName;
	private static final String filePath="~/reciter/"; 
	
	// if the directory does not exist, the directories will create by the following code
	static{
		File file = new File(filePath);
		file.mkdirs();
	}
	
	private ReCiterTransactionerName(int code, String fileName){
		DateFormat df = new SimpleDateFormat("_yyyy_MM_dd");
		this.nameCode=code;
		this.fileName=fileName+df.format(new Date())+".log";
	}
	
	public String getFileName(){
		return filePath+fileName;
	}
	
	public int getNameCode(){return this.nameCode;}
}
