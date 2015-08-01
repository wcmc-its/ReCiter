package reciter.model.boardcertifications;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.SparseRealVector;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.lucene.DocumentVector;
import reciter.lucene.DocumentVectorType;
import reciter.model.article.ReCiterArticle;
import reciter.tfidf.Document;
import reciter.tfidf.TfIdf;
import database.dao.BoardCertificationDataDao;

/**
 * @author htadimeti
 */

public class ReadBoardCertifications {
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReadBoardCertifications.class);
	private String excelFileName;
	private Workbook workbook;
	private boolean isExcel;
	private String excelType;
	private static final String XLS="XLS";
	private static final String XLSX="XLSX";
	//private static final String CSV="CSV"; // TODO - will enable once the functionality implementation starts 

	public ReadBoardCertifications(){
		this.excelFileName="src/main/resources/docs/BoardCertificationsWCMC.xls";
		//		fileSearch(new File(ReadBoardCertifications.class.getProtectionDomain().getCodeSource().getLocation().getFile()), "BoardCertificationsWCMC.xls");
	}

	private String getBoardCertificationFilePath(){
		//ReadBoardCertifications.class.getProtectionDomain().getCodeSource().getLocation()
		return null;
	}

	public String getExcelFileName() {
		return excelFileName;
	}

	public void setExcelFileName(String excelFileName) {
		this.excelFileName = excelFileName;
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	public boolean isExcel() {
		isExcel=false;
		excelType=null;
		InputStream inputStream=null;
		try {
			inputStream = new FileInputStream(excelFileName);
			if(! inputStream.markSupported()) {
				inputStream = new PushbackInputStream(inputStream, 8);
			}
			isExcel=POIFSFileSystem.hasPOIFSHeader(inputStream) || POIXMLDocument.hasOOXMLHeader(inputStream);
			if(isExcel)excelType=POIFSFileSystem.hasPOIFSHeader(inputStream)?XLS:XLSX;
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(inputStream!=null)
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					inputStream=null;
				}
		}
		return isExcel;
	}

	public void setExcel(boolean isExcel) {
		this.isExcel = isExcel;
	}

	public String getExcelType() {
		return excelType;
	}

	public void setExcelType(String excelType) {
		this.excelType = excelType;
	}

	private String preProcessBoardCertifications(String certification){
		if(certification!=null && !certification.trim().equals("")){
			certification=certification.trim();
			certification=certification.replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim();
		}
		return certification;
	}


	/**
	 * 
	 * @param sheetNum - Sheet Number to read - 0 for first sheet, 1 for second sheet , etc.,
	 * @param numRows - number of rows, if it's -1 then all the valid rows
	 * @param columnNumber1 - column values as key
	 * @param columnNumber2 - column values as value
	 * @return Map object
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Map<String, List<String>> getMapFromExcelSheet(int sheetNum,int numRows,int columnNumber1,int columnNumber2,String searchKey) throws FileNotFoundException, IOException{
		Map<String, List<String>> excelContent=null;
		isExcel();
		if(this.excelType==ReadBoardCertifications.XLS)this.workbook=new HSSFWorkbook(new FileInputStream(excelFileName));
		else if(this.excelType==ReadBoardCertifications.XLSX)this.workbook=new XSSFWorkbook(this.excelFileName);

		if(this.workbook!=null){
			Sheet sheet = workbook.getSheetAt(sheetNum);
			Iterator<Row> rowIterator = sheet.iterator();
			int i = 0;
			boolean valid=true;
			excelContent=new HashMap<String, List<String>>();
			while (valid && rowIterator.hasNext()) {
				++i;				
				Row row = rowIterator.next();
				if(numRows==-1 || (numRows > 0 && i <= numRows))valid=true;
				else valid=false;
				if (i == 1) continue;
				String col1Value=row.getCell(columnNumber1)!=null?row.getCell(columnNumber1).getStringCellValue():"";
				String col2Value=row.getCell(columnNumber2)!=null?row.getCell(columnNumber2).getStringCellValue():"";

				if(!col1Value.trim().equals("") && !col2Value.trim().equals("")){
					if(searchKey!=null && !searchKey.toLowerCase().trim().equals(col1Value.toLowerCase().trim()))continue;
					col2Value=preProcessBoardCertifications(col2Value);
					List<String> list=null;
					if(excelContent.containsKey(col1Value))list=excelContent.get(col1Value);
					else list=new ArrayList<String>();
					String[] keywords = col2Value.trim().split(" ");
					for(String k : keywords)list.add(k);
					excelContent.put(col1Value, list);
				}
			}
		}
		return excelContent;
	}


	/**
	 * 
	 * @param cwid
	 * @return
	 */
	public String getBoardCertifications(String cwid){
		List<String> list=null;	
		StringBuilder sb = new StringBuilder();
		try {
			BoardCertificationDataDao dao = new BoardCertificationDataDao();
			Map<String, List<String>> map=dao.getBoardCertificationsByCwid(cwid);//getMapFromExcelSheet(0, -1, 1, 0,cwid);
			System.out.println(map);
			if(map.containsKey(cwid)){
				list=map.get(cwid);
				List<String> keys = new ArrayList<String>();
				for(String certification: list){
					if(certification!=null && !certification.trim().equals("")){
						certification=certification.trim();
						certification=certification.replace('/', ' ').replace("and", " ").replace("the", " ").replace("medicine", " ").replace("-", " ").replace("with", " ").replace("in", " ").replace("med", " ").replace("adult", " ").replace("general", " ").replaceAll("\\s+", " ").trim();
						if(!certification.equals("")){
							String[] s = certification.split(" ");
							for(int i=0;s!=null && i<s.length;i++){
								if(!keys.contains(s[i])){
									keys.add(s[i]);
									sb.append(s[i]).append(" ");
								}
							}
						}
					}
					//sb.append(preProcessBoardCertifications(str)).append(" ");
				}
			}
			dao=null;
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return sb.toString().trim();
	}


	/**
	 * 
	 * @param cwid
	 * @param article
	 */
	public double getBoardCertifications(String cwid, List<ReCiterArticle> articles){
		String str=getBoardCertifications(cwid);		
		Document doc = new Document(str);
		List<Document> documents = new ArrayList<Document>();
		TfIdf idf = new TfIdf(documents);
		double[] vectorValues = idf.createVector(doc);
		//double docNorm = idf.norm(vectorValues);
		SparseRealVector docVector = new OpenMapRealVector(vectorValues);
		double sim=0;
		documents.add(doc);
		for(ReCiterArticle article: articles){
			Map<DocumentVectorType, DocumentVector> vectors = article.getDocumentVectors();
			if(vectors!=null){
				for(Map.Entry<DocumentVectorType, DocumentVector> entry:vectors.entrySet()){
					DocumentVector vector = entry.getValue();
					DocumentVectorType vtype=entry.getKey();				
					double[] articleVectors = vector.getVector().toArray();
					sim=sim+idf.cosineSimilarity(vectorValues, articleVectors);
				}
			}
		}
		slf4jLogger.info("Board Certifications For CWID("+cwid+") => "+str + " => SIM: "+sim);
		return sim;
	}
	
	
	
	private Document getDocument(String str){
		return new  Document(str);
	}

	public void fileSearch(File path,String fileName){
		if(excelFileName!=null)return;
		FileFilter fileFilter = new FileFilter() {			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept (File dir, String name) {
				return name.equalsIgnoreCase(fileName);
			}
		};
		String[] children = path.list(filter);
		if(children!=null && children.length>0){
			excelFileName=path.getAbsolutePath();
			excelFileName=excelFileName+(excelFileName.endsWith(File.separator)?"":File.separator)+children[0];
		}else{
			File[] folders = path.listFiles(fileFilter);
			for(int i=0;folders!=null && i<folders.length;i++)fileSearch(folders[i],fileName);
		}
	}
}
