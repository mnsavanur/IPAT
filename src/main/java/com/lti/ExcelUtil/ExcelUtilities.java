package com.lti.ExcelUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lti.TestUtil.Report;
import com.lti.base.Config;
import com.lti.dataProviders.DataProviders;
import org.apache.poi.openxml4j.opc.OPCPackage;

public class ExcelUtilities {
	static Logger log = Logger.getLogger(ExcelUtilities.class.getName());
	public static String fileExtn = null;
	public static FileInputStream fis = null;
	public static File file = null;
	public static Workbook workbook;
	public static org.apache.poi.ss.usermodel.Sheet worksheet;
	
	/*
	 * Date - 11/08/2020 Author - Vipin Phogat 
	 * Description - The below method is
	 * responsible for returning the sheet object of the excel file kept at file
	 * path passed as an argument
	 */

	public static Sheet getWorkSheet(String filePath, String sheetName) throws IOException {
		fis = new FileInputStream(filePath);
		fileExtn = FilenameUtils.getExtension(filePath);
		if (fileExtn.equalsIgnoreCase("xls")) {
			workbook = new HSSFWorkbook(fis);
		} else if (fileExtn.equalsIgnoreCase("xlsx")) {
			workbook = new XSSFWorkbook(fis);
		} else {
			throw new RuntimeException("Invalid file extension: " + fileExtn);
		}

		worksheet = workbook.getSheet(sheetName);
		fis.close();
		return worksheet;

	}
	public static Workbook getWorkbook(String filePath) throws IOException {

		fis = new FileInputStream(filePath);
		fileExtn = FilenameUtils.getExtension(filePath);
		if (fileExtn.equalsIgnoreCase("xls")) {
			workbook = new HSSFWorkbook(fis);
			//HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
		} else if (fileExtn.equalsIgnoreCase("xlsx")) {
			workbook = new XSSFWorkbook(fis);
			//XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
		} else {
			throw new RuntimeException("Invalid file extension: " + fileExtn);
		}

		fis.close();
		return workbook;
	}
	
	public static Sheet getWorkSheetFromWorkbook(Workbook workbook, String sheetName) throws IOException {
		worksheet = workbook.getSheet(sheetName);
		return worksheet;
	}
	
	/*
	 * Date - 11/09/2020 Author - Vipin Phogat Description - this method is
	 * responsible for reading and storing the data sheet headers. Updates - 11/09 -
	 * Base Function
	 */
	public static HashMap<String, Integer> getColumnHeader(Sheet sheetName) {
		HashMap<String, Integer> dataHeader = new HashMap<String, Integer>();
		Row headerRow = sheetName.getRow(0);
		int colCount = headerRow.getPhysicalNumberOfCells();
		for (int index = 0; index < colCount; index++) {
			Cell tempCell = null;
			try {
				tempCell = headerRow.getCell(index);
			}catch(Exception e) {
				continue;
			}
			
			if (tempCell != null && !StringUtils.equals(tempCell.toString().trim(),"")) {
				String key = tempCell.toString().trim();
				key=authenticateString(key);
				int value = index;
				dataHeader.put(key, value);
			}

		}

		return dataHeader;

	}

	/*
	 * Date - 11/09/2020 Author - Vipin Phogat Description - this method is
	 * responsible for this method is responsible for returning total number of rows
	 * present in a sheet. Updates - 11/09 - Base Function
	 */

	public static int getAllRowCount(Sheet sheetName) {
		int totalRows = sheetName.getPhysicalNumberOfRows();
		return totalRows;
	}
	
	/*
	 * Date - 11/09/2020 Author - Vipin Phogat Description - this method is
	 * responsible for this method is responsible for returning total number of rows
	 * present in a sheet. Updates - 11/09 - Base Function
	 */

	public static int getColumnsCount(Sheet sheetName, int rowIndex) {
		int actualcolumn = 0;
		Row row = sheetName.getRow(rowIndex);
		int totalColumns = row.getPhysicalNumberOfCells();
		for (int index = 0; index < totalColumns; index++) {
			Cell tempCell=null;
			try {
				tempCell = row.getCell(index);
			}catch(Exception e) {
				break;
			}
			if(tempCell != null) {
				if (authenticateString(tempCell.toString().trim()) != "") {
					actualcolumn++;
				}
			}
		} 
		return actualcolumn;
	}
	
	public static HashMap<Integer,Object> getRowData(Sheet sheetName, int rowIndex){
		int colIndex;
		String value;
		HashMap<Integer,Object> rowData = new HashMap<Integer,Object>();
		Row row = sheetName.getRow(rowIndex);
		int totalColumns = row.getPhysicalNumberOfCells();
		for (int index = 0; index < totalColumns; index++) {
			Cell tempCell = row.getCell(index);
			if(tempCell!=null) {
				if (authenticateString(tempCell.toString().trim()) != "") {
					colIndex = index;
					Cell valueCell=row.getCell(index);
					if(valueCell!=null) {
						value=authenticateString(valueCell.toString().trim());
						rowData.put(colIndex, value);
					}
				}
			}
		}
		return rowData;
	}

	public static HashMap<String,Integer> getColumnData(Sheet sheetName, int colIndex){
		int rowCount = 0;
		String keyWord;
		Row row;
		HashMap<String,Integer> colData = new HashMap<String,Integer>();
		rowCount = sheetName.getLastRowNum();
		for(int rowIndex=0;rowIndex<=rowCount;rowIndex++) {
			row = sheetName.getRow(rowIndex);
			Cell keyWordCell=row.getCell(colIndex);
			if(keyWordCell!=null) {
				keyWord = authenticateString(keyWordCell.toString());
				colData.put(keyWord, rowIndex);
				log.info("Adding data to hashmap: Key: "+keyWord+" Value: "+rowIndex);
			}
		}
		
		return colData;
	}
	
	/*
	 * Author - Vipin Phogat
	 * Date - 11/18/2020
	 * Description - Function to read test data of the supplied tcid
	 
	*/
	
	public static HashMap<Integer, String> getTestData(int fieldNameIndex, Sheet SheetName){
		
		int totalRow = SheetName.getLastRowNum()+1;
		int key=0;
		String value;
		Row row;
		HashMap<Integer,String> testData = new HashMap<Integer,String>();
		for(int i=1;i<totalRow;i++) {
			row = SheetName.getRow(i);
			key = i;
			try {
				value = row.getCell(fieldNameIndex).toString();
			}catch(Exception e){
				value="";
			}
			value=authenticateString(value);
			if(!StringUtils.equals(value, null) && !StringUtils.equals(value, "")) {
			//if(!value.toString().equals("") || !value.toString().equals(null)){
				testData.put(key, value);
			}
		}
		
		return testData;
		
	}
	
public static HashMap<String, String> getTestData(int fieldNameIndex, int tcIdIndex, Sheet SheetName){
		int totalRow = SheetName.getLastRowNum()+1;
		String key;
		String value;
		Row row;
		HashMap<String,String> testData = new HashMap<String,String>();
		for(int i=1;i<totalRow;i++) {
			key="";
			value="";
			row = SheetName.getRow(i);
			if(row!=null) {
				Cell keyCell=row.getCell(fieldNameIndex);
				if(keyCell!=null) {
					key = authenticateString(keyCell.toString());
					//value = row.getCell(tcIdIndex).toString();
					try {
						Cell curCell=row.getCell(tcIdIndex);
						if(curCell!=null) {
							FormulaEvaluator evaluator = SheetName.getWorkbook().getCreationHelper().createFormulaEvaluator();
							evaluator.evaluateFormulaCell(curCell);
							value = curCell.getStringCellValue();
						}else {
							value="";
						}
					}catch(Exception e) {
						value = row.getCell(tcIdIndex).toString();
					}
					value=authenticateString(value);
					if(!value.toString().equals("") || !value.toString().equals(null)){
						testData.put(key, value);
					}
				}
			}
		}
		return testData;
	}

public static HashMap<String, String> getObjectData(int fieldNameIndex,Sheet SheetName){
	int totalRow = SheetName.getLastRowNum()+1;
	int fieldTypeIndex;
	int actionIndex;
	int xpathIndex;
	int idIndex;
	
	String key;
	String value;
	Row row;
	HashMap<String, Integer> colHeader = new HashMap<String, Integer>();
	HashMap<String,String> objectData = new HashMap<String,String>();
	
	colHeader = getColumnHeader(SheetName);
	fieldTypeIndex = colHeader.get("FieldType");
	actionIndex = colHeader.get("Action");
	xpathIndex = colHeader.get("xpath");
	idIndex = colHeader.get("id");
	String fieldNameValue;
	String fieldTypeValue;
	String actionValue;
	String xpathValue;
	String idValue;
	
	
	for(int i = 1;i<totalRow;i++) {
		row = SheetName.getRow(i);
		Cell keyCell=row.getCell(fieldNameIndex);
		if(keyCell !=null) {
			key = authenticateString(keyCell.toString());
			fieldNameValue = authenticateString(row.getCell(fieldNameIndex).toString().trim());
			fieldTypeValue = authenticateString(row.getCell(fieldTypeIndex).toString().trim());
			actionValue = authenticateString(row.getCell(actionIndex).toString().trim());
			if(row.getCell(xpathIndex) != null) {
				xpathValue=authenticateString(row.getCell(xpathIndex).toString().trim());
			}else {
				xpathValue = "";
			}
			idValue = authenticateString(row.getCell(idIndex).toString().trim());
			value = fieldNameValue +"|"+fieldTypeValue + "|" +  actionValue + "|" +  xpathValue+ "|" +  idValue + "|";
			objectData.put(key, value);
		}
	}
	
	return objectData;
}
public static String[][] getObjectDataArray(int fieldNameIndex,Sheet SheetName){
	int totalRow = SheetName.getLastRowNum()+1;
	int fieldTypeIndex;
	int actionIndex;
	int xpathIndex;
	int idIndex;
	
	String key;
	String value;
	Row row;
	HashMap<String, Integer> colHeader = new HashMap<String, Integer>();
	HashMap<String,String> objectData = new HashMap<String,String>();
	
	colHeader = getColumnHeader(SheetName);
	fieldTypeIndex = colHeader.get("FieldType");
	actionIndex = colHeader.get("Action");
	xpathIndex = colHeader.get("xpath");
	idIndex = colHeader.get("id");
	String fieldNameValue;
	String fieldTypeValue;
	String actionValue;
	String xpathValue;
	String idValue;
	
	String[][] objectDataArray=new String[totalRow-1][2];
	for(int i = 1;i<totalRow;i++) {
		row = SheetName.getRow(i);
		Cell keyCell=row.getCell(fieldNameIndex);
		if(keyCell != null) {
			key = authenticateString(keyCell.toString());
			if(!StringUtils.equals(key, null) && !StringUtils.equals(key, "")) {
				fieldNameValue = authenticateString(row.getCell(fieldNameIndex).toString().trim());
				fieldTypeValue = authenticateString(row.getCell(fieldTypeIndex).toString().trim());
				actionValue = authenticateString(row.getCell(actionIndex).toString().trim());
				if(row.getCell(xpathIndex) != null) {
					xpathValue=authenticateString(row.getCell(xpathIndex).toString().trim());
				}else {
					xpathValue = "";
				}
				if(row.getCell(idIndex) != null) {
					idValue = authenticateString(row.getCell(idIndex).toString().trim());
				}else {
					idValue = "";
				}
				
				if(StringUtils.equals(idValue, ""))
					idValue="NP";
				
				if(StringUtils.equals(xpathValue, ""))
					xpathValue="NP";
				
				value = fieldNameValue +"|"+fieldTypeValue + "|" +  actionValue + "|" +  xpathValue+ "|" +  idValue + "|";
				//objectData.put(key, value);
				objectDataArray[i-1][0]=key;
				objectDataArray[i-1][1]=value;
			}
		}
	}
	
	return objectDataArray;
}	
	
	
	
	public static String getCellData(Sheet sheetName, int colIndex, int rowIndex) throws Exception {
		String value=null;
		Row dataRow = sheetName.getRow(rowIndex);
		if(dataRow.getCell(colIndex) != null) {
			value = authenticateString(dataRow.getCell(colIndex).toString().trim());
		}else {
			value="";
		}
		
		return value;

	}
	
	public static boolean validateFile(String filePath) {
		File file = new File(filePath);
		boolean fileFlag;
		if(file.exists()) {
			fileFlag = true;
		}else {
			fileFlag = false;
		}
		
		return fileFlag;
	}
	
	public static int getTransactionIndexFromTestData(Sheet dataSheet,String tcId, String transactionName)
	{
		int transColIndex=-1;
		
		Row headerRow = dataSheet.getRow(0);
		Row transactionRow=dataSheet.getRow(1);
		int colCount = headerRow.getPhysicalNumberOfCells();
		for (int index = 0; index < colCount; index++) {
			Cell tempCellTC = null;
			Cell tempCellTransaction= null;
			try {
				tempCellTC = headerRow.getCell(index);
				tempCellTransaction=transactionRow.getCell(index);
				if(StringUtils.contains(transactionName, "@gbl")) {
					if(StringUtils.equalsIgnoreCase(authenticateString(tempCellTransaction.toString()), transactionName)){
						transColIndex =index;
						break;
					}
				}else {
					if(StringUtils.equalsIgnoreCase(authenticateString(tempCellTC.toString()), tcId) && StringUtils.equalsIgnoreCase(authenticateString(tempCellTransaction.toString()), transactionName)){
						transColIndex =index;
						break;
					}
				}
			}catch(Exception e) {
				continue;
			}
		}
		
		return transColIndex;
	}
	public static void writeToDataCarrier(String tcToWriteInDC, String columnNameInDC, String valueToWriteInDC) throws Exception
	{
		
			File carrierFile=new File(Config.dataCarrier);
			if(!carrierFile.exists()) {
				XSSFWorkbook wb=new XSSFWorkbook();
				Sheet sheet=wb.createSheet("DataCarrier");
				Row headerRow=sheet.createRow(0);
				headerRow.createCell(0).setCellValue("TestCaseID");
				FileOutputStream fileOut=new FileOutputStream(Config.dataCarrier);
				wb.write(fileOut);
				fileOut.close();
				wb.close();
			}
			Workbook wb=getWorkbook(Config.dataCarrier);
			Sheet dataCarrierSheet=getWorkSheetFromWorkbook(wb,"DataCarrier");
			int rowCountDC=dataCarrierSheet.getPhysicalNumberOfRows();
			int colCountDC=dataCarrierSheet.getRow(0).getPhysicalNumberOfCells();
			
			int colToWrite=-1, rowToWrite=-1;
			Row headerRow = dataCarrierSheet.getRow(0);
			
			//locate or create header column
			for(int i=1;i<colCountDC;i++) {
				if(headerRow.getCell(i).getStringCellValue().equalsIgnoreCase(columnNameInDC)){
					colToWrite=i;
					break;
				}
			}
			if(colToWrite==-1){
				headerRow.createCell(colCountDC).setCellValue(columnNameInDC);
				colToWrite=colCountDC;
			}
			
			//locate or create test case id row
			for(int i=1;i<rowCountDC;i++) {
				if(dataCarrierSheet.getRow(i).getCell(0).getStringCellValue().equalsIgnoreCase(tcToWriteInDC)) {
					rowToWrite=i;
					break;
				}
			}
			if(rowToWrite==-1){
				dataCarrierSheet.createRow(rowCountDC).createCell(0).setCellValue(tcToWriteInDC);
				rowToWrite=rowCountDC;
			}
			
			//Write value to Data Carrier
			dataCarrierSheet.getRow(rowToWrite).createCell(colToWrite).setCellValue(valueToWriteInDC);
			FileOutputStream fileOut=new FileOutputStream(Config.dataCarrier);
			wb.write(fileOut);
			fileOut.close();
			wb.close();
	}
	public static String readFromDataCarrier(String tcToReadFromInDC, String columnNameInDC) throws Exception
	{
		
			File carrierFile=new File(Config.dataCarrier);
			if(!carrierFile.exists()) {
				return "Value not found for '"+ columnNameInDC + "' as Data Carrier file does not exist.";
			}
			Workbook wb=getWorkbook(Config.dataCarrier);
			Sheet dataCarrierSheet=getWorkSheetFromWorkbook(wb,"DataCarrier");
			int rowCountDC=dataCarrierSheet.getPhysicalNumberOfRows();
			int colCountDC=dataCarrierSheet.getRow(0).getPhysicalNumberOfCells();
			
			int colToRead=-1, rowToRead=-1;
			Row headerRow = dataCarrierSheet.getRow(0);
			
			//locate header column
			for(int i=1;i<colCountDC;i++) {
				if(headerRow.getCell(i).getStringCellValue().equalsIgnoreCase(columnNameInDC)){
					colToRead=i;
					break;
				}
			}
			if(colToRead==-1){
				return "Column with name '"+ columnNameInDC+"' not found in Data Carrier file.";
			}
			
			//locate test case id row
			for(int i=1;i<rowCountDC;i++) {
				if(dataCarrierSheet.getRow(i).getCell(0).getStringCellValue().equalsIgnoreCase(tcToReadFromInDC)) {
					rowToRead=i;
					break;
				}
			}
			if(rowToRead==-1){
				return "Test Case '"+ tcToReadFromInDC+"' not found in Data Carrier file.";
			}
			
			//Retrieve value from Data Carrier
			Cell dataCell=dataCarrierSheet.getRow(rowToRead).getCell(colToRead);
			String retrievedValue="";
			if(dataCell != null)
			 retrievedValue=dataCell.getStringCellValue();
			
			wb.close();
			return retrievedValue;
	}
	
	public static HashMap<String,String> retrieveFileAndTabList(String multiPath)throws Exception {
		HashMap<String, String> tempFileTabMapping = new HashMap<String, String>();
		String[] testInputFileArray=multiPath.trim().split(",");
		for(int i=0;i<testInputFileArray.length;i++) {
			String filePath=testInputFileArray[i].trim();
			OPCPackage pkg = OPCPackage.open(new FileInputStream(filePath));
			XSSFReader r = new XSSFReader(pkg);
			Iterator<InputStream> sheets = r.getSheetsData();
	
			if (sheets instanceof XSSFReader.SheetIterator) {
			   XSSFReader.SheetIterator sheetiterator = (XSSFReader.SheetIterator)sheets;
			   while (sheetiterator.hasNext()) {
					InputStream dummy = sheetiterator.next();
					tempFileTabMapping.put(sheetiterator.getSheetName(), filePath);
					dummy.close();
			   }
			}
		  pkg.close();
		}
		return tempFileTabMapping;
	}
	public static HashMap<String,Workbook> retrieveWorkbookAndTabList(String multiPath)throws Exception {
		HashMap<String, Workbook> tempWorkbookTabMapping = new HashMap<String, Workbook>();
		String[] testInputFileArray=multiPath.trim().split(",");
		for(int i=0;i<testInputFileArray.length;i++) {
			String filePath=testInputFileArray[i].trim();
			OPCPackage pkg = OPCPackage.open(new FileInputStream(filePath));
			XSSFReader r = new XSSFReader(pkg);
			Iterator<InputStream> sheets = r.getSheetsData();
			
			Workbook tempWorkbook = new XSSFWorkbook(new FileInputStream(filePath));
			if (sheets instanceof XSSFReader.SheetIterator) {
			   XSSFReader.SheetIterator sheetiterator = (XSSFReader.SheetIterator)sheets;
			   while (sheetiterator.hasNext()) {
					InputStream dummy = sheetiterator.next();
					tempWorkbookTabMapping.put(sheetiterator.getSheetName(), tempWorkbook);
					dummy.close();
			   }
			}
		  pkg.close();
		}
		return tempWorkbookTabMapping;
	}
	public static void closeWorkbook(HashMap<String, Workbook> tabWorkbookMapping) {
		Iterator<Entry<String, Workbook>> it = tabWorkbookMapping.entrySet().iterator();
		// iterating every set of entry in the HashMap. 
		while (it.hasNext()) {
			Map.Entry<String, Workbook> set = (Map.Entry<String, Workbook>) it.next();
			try {
				set.getValue().close();
			}catch(Exception e) {}
			//System.out.println(set.getKey() + " = " + set.getValue());
		}
	}
	public static String authenticateString(String str) {
		String finalStr=str;
		if(!StringUtils.equals(str,null)) {
			finalStr= str.trim();
			Integer[] charsToBeRemovedArr= new Integer[] {160,161};
			String tempStr="";
			for(int i=0;i<finalStr.length();i++){
				int asciiValue=finalStr.charAt(i);
				Boolean removeChar=false;
				for (int charsToBeRemoved : charsToBeRemovedArr) {
		            if (asciiValue == charsToBeRemoved) {
		            	removeChar = true;
		                break;
		            }
		        }
				if(!removeChar)
					tempStr=tempStr+finalStr.charAt(i);
			}
			finalStr=tempStr;
		}
		return finalStr;
	}
	
	public static void writeToCSVReport(String fieldName, String stepDetails, String stepStatus, String screenshot) throws Exception{
		File timetravelReportFile=new File(Report.timetravelReportExcelFilePath);
		PrintStream print = null;
		
		//create header
		print = new PrintStream(new FileOutputStream(timetravelReportFile, true));
		int dataPresent=checkDataPresenceInFile(timetravelReportFile);
		if(dataPresent==0) {
			print.print("TestCaseID,SysDate,Timestamp,TransactionName,FieldName,Details,Status,ScreenshotPath");
			print.println();
			print.close();
		}
		
		//write step result
		print = new PrintStream(new FileOutputStream(timetravelReportFile, true));
		char dquoteChar = 34;
		
		//retrieve timestamp..
		 Date date= new Date();
		 long time = date.getTime();
		 Timestamp timestamp = new Timestamp(time);
	 
		print.print(dquoteChar + Report.getTestCaseId() + dquoteChar + "," + 
		dquoteChar + Report.getSysDate() + dquoteChar + "," + 
		dquoteChar + timestamp + dquoteChar + "," + 
		dquoteChar + Report.getTransactionName() + dquoteChar + "," + 
		dquoteChar + fieldName + dquoteChar + "," + 
		dquoteChar + stepDetails + dquoteChar + "," + 
		dquoteChar + stepStatus + dquoteChar + ","
		+ dquoteChar + screenshot + dquoteChar
		);
		
		print.println();
		print.close();
	}
	public static int checkDataPresenceInFile(File filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}
	public static void writeToTimetravelExcelReport(String fieldName, String stepDetails, String stepStatus, String screenshot) throws Exception{
		File carrierFile=new File(Report.timetravelReportExcelFilePath);
		if(!carrierFile.exists()) {
			XSSFWorkbook wb=new XSSFWorkbook();
			Sheet sheet=wb.createSheet("TimetravelReport");
			Row headerRow=sheet.createRow(0);
			headerRow.createCell(0).setCellValue("TestCaseID");
			headerRow.createCell(1).setCellValue("SysDate");
			headerRow.createCell(2).setCellValue("Timestamp");
			headerRow.createCell(3).setCellValue("TransactionName");
			headerRow.createCell(4).setCellValue("FieldName");
			headerRow.createCell(5).setCellValue("Details");
			headerRow.createCell(6).setCellValue("Status");
			headerRow.createCell(7).setCellValue("ScreenshotPath");
			
			FileOutputStream fileOut=new FileOutputStream(Report.timetravelReportExcelFilePath);
			wb.write(fileOut);
			fileOut.close();
			wb.close();
		}
		Workbook wb=getWorkbook(Report.timetravelReportExcelFilePath);
		Sheet timetravelSheet=getWorkSheetFromWorkbook(wb,"TimetravelReport");
		
		HashMap<String, Integer> headers = ExcelUtilities.getColumnHeader(timetravelSheet);
		int tcIdIndex = headers.get("TestCaseID");
		int statusIndex = headers.get("Status");
		int screenshotPathIndex = headers.get("ScreenshotPath");
		int sysDateIndex=headers.get("SysDate");
		int timestampIndex=headers.get("Timestamp");
		int transactionNameIndex=headers.get("TransactionName");
		int fieldNameIndex=headers.get("FieldName");
		int detailsIndex=headers.get("Details");
		
		//retrieve timestamp
		Date date= new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		SimpleDateFormat formatter = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss"); 
	    String timestampStr = formatter.format(timestamp);
	        
		String tcID=Report.getTestCaseId();
		if(Report.getTestCaseId().equalsIgnoreCase(Report.getSysDate()))
			 tcID="Common";
				
		int writeToRow=timetravelSheet.getPhysicalNumberOfRows();
		timetravelSheet.createRow(writeToRow);
		timetravelSheet.getRow(writeToRow).createCell(tcIdIndex).setCellValue(tcID);
		timetravelSheet.getRow(writeToRow).createCell(sysDateIndex).setCellValue(Report.getSysDate());
		timetravelSheet.getRow(writeToRow).createCell(timestampIndex).setCellValue(timestampStr);
		timetravelSheet.getRow(writeToRow).createCell(transactionNameIndex).setCellValue(Report.getTransactionName());
		timetravelSheet.getRow(writeToRow).createCell(fieldNameIndex).setCellValue(fieldName);
		timetravelSheet.getRow(writeToRow).createCell(detailsIndex).setCellValue(stepDetails);
		timetravelSheet.getRow(writeToRow).createCell(statusIndex).setCellValue(stepStatus);
		timetravelSheet.getRow(writeToRow).createCell(screenshotPathIndex).setCellValue(screenshot);
		
		FileOutputStream fileOut=new FileOutputStream(Report.timetravelReportExcelFilePath);
		wb.write(fileOut);
		fileOut.close();
		wb.close();
	}
}
