package com.lti.dataProviders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.testng.annotations.DataProvider;

import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.webDriver.UiHandler;

public class DataProviders {
	static Logger log = Logger.getLogger(DataProviders.class.getName());
	public static Sheet testSuiteSheet = null;
	public static HashMap<String, Integer> colHeaders;
	public static HashMap<Integer, Object> rowData;
 	public static Row row;

	
	@DataProvider(name="Test Suite Iterator")
	public static Object[][] getTestSuiteData() throws Exception{
		
		log.info("Reading TestSuite Tab : " + Config.testSuiteTab);		
		int rowCount = 0;
		int colCount = 2;
		
		if(testSuiteSheet == null) {
			//String filePath = Config.testInputFile;
			//testSuiteSheet = ExcelUtilities.getWorkSheet(filePath, Config.testSuiteTab);
			testSuiteSheet = ExcelUtilities.getWorkSheet(Base.testInputFileTabMapping.get(Config.testSuiteTab), Config.testSuiteTab);
		}
		
		rowCount = ExcelUtilities.getAllRowCount(testSuiteSheet);
		Object[][] data = new Object[rowCount-1][1];
		Hashtable<String,String> testCaseIterator = null;
		for(int rowIndex = 1; rowIndex<rowCount;rowIndex++) {
			testCaseIterator = new Hashtable<String,String>();
			for(int colIndex = 0;colIndex<colCount;colIndex++ ) {
				String tempKey=ExcelUtilities.getCellData(testSuiteSheet, colIndex, 0);
				String tempValue=ExcelUtilities.getCellData(testSuiteSheet, colIndex, rowIndex);
				if(!tempKey.equalsIgnoreCase("") && !tempValue.equalsIgnoreCase("")) {
					testCaseIterator.put(tempKey, tempValue);
					data[rowIndex-1][0]=testCaseIterator;
				}
			}
		}
		return data;
		
	}
	
	@DataProvider(name="Timetravel Test Suite Iterator")
	public static Object[][] getTimetravelTestSuiteData() throws Exception{
		log.info("Reading TestSuite Tab : " + Config.testSuiteTab);		
		
		if(testSuiteSheet == null) {
			testSuiteSheet = ExcelUtilities.getWorkSheet(Base.testInputFileTabMapping.get(Config.testSuiteTab), Config.testSuiteTab);
		}
		int colCount=ExcelUtilities.getColumnsCount(testSuiteSheet, 0);

		Object[][] data=new Object[colCount-2][2];
		HashMap<String, Integer> colHeader;
		
		colHeader = ExcelUtilities.getColumnHeader(testSuiteSheet);
		int tcIdIndex = colHeader.get("TestCaseID");
		int ExecutionFlag = colHeader.get("ExecutionFlag");
		int transactionStartIndex=ExecutionFlag+1;
				
				
		for(int colIndex=transactionStartIndex, ctr=0;colIndex<colCount;colIndex++, ctr++) {
			try {
				String SDate=ExcelUtilities.getCellData(testSuiteSheet, colIndex, 0);
				if(SDate != null && !SDate.equals("")) {
					data[ctr][0]=SDate;
					data[ctr][1]=String.valueOf(colIndex);
				}else {
					break;
				}
			}catch(Exception e) {
				log.info("Error while reading TestSuite file system dates.");
			}
		}
		return data;
		
	}
}
