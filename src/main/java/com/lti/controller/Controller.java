package com.lti.controller;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.testng.Assert;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.TestUtil.CustomScreenRecorder;
import com.lti.TestUtil.MongoDB;
import com.lti.TestUtil.Report;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.base.Init;
import com.lti.webDriver.Assertions;
import com.lti.webDriver.WebDriver;
import com.lti.webDriver.WebDriverHelper;

public class Controller extends Base {
	
	public static Sheet worksheet = null;
	public static String tcId=null;
	public static String Browser;
	public static HashMap<String, Integer> testDataHeader;
	public static HashMap<String, Integer> objectDataHeader;
	public static HashMap<Integer,String> fieldNameList;
	public static HashMap<Integer,String> object_fieldNameList;
	public static HashMap<String, String> fieldValue;
	public static HashMap<String,String> objectData;
	public static Sheet dataSheet;
	public static Sheet repositorySheet;
	public static String transactionName;
	public static int transactionRowIndexTT;
	static Logger log = Logger.getLogger(Controller.class.getName());
	
	/*
	 * Author - Vipin Phogat 
	 * Date Modified- 12/14/2020 
	 * Description - This method reads the Test Suite tab and stores the transaction values in an array list
	 * 
	 */
	
	public static void loadTransactions(String testCaseID) throws Exception {
		String transName;
		int totalRowCount=0;
		int totalColCount=0;
		int tcIdIndex = 0;
		int transactionIndex=0;
		HashMap<String, Integer> colHeader;
		Row row;
		ArrayList<String> transactions = new ArrayList<String>();
		String filePath = Config.testInputFile;
		
		try {
			//worksheet = ExcelUtilities.getWorkSheet(filePath, "TestSuite");
			worksheet = ExcelUtilities.getWorkSheet(Base.testInputFileTabMapping.get(Config.testSuiteTab), Config.testSuiteTab);
			totalRowCount = ExcelUtilities.getAllRowCount(worksheet);
			colHeader = ExcelUtilities.getColumnHeader(worksheet);
			tcIdIndex = colHeader.get("TestCaseID");
			transactionIndex = colHeader.get("Transactions");
			
			
			for(int rowIndex = 1; rowIndex<totalRowCount;rowIndex++) {
				row = worksheet.getRow(rowIndex);
				tcId = row.getCell(tcIdIndex).toString();
				if(tcId.equalsIgnoreCase(testCaseID)) {
					totalColCount = ExcelUtilities.getColumnsCount(worksheet, rowIndex);
					for(int colIndex=transactionIndex; colIndex<totalColCount; colIndex++) {
						
							transName = ExcelUtilities.getCellData(worksheet, colIndex, rowIndex).toString().trim();
							
							if(!StringUtils.equals(transName, null)) {
								if(!StringUtils.isEmpty(transName) && !StringUtils.equals(transName,"") && !StringUtils.equals(transName," ") && !StringUtils.isBlank(transName)) {
									transactions.add(transName);
								}
							}
					}
					break;
				}
			}
			
			startExecution(transactions);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public static void loadTransactions2(String testCaseID,String executionFlag) throws Exception {
		String transName;
		int totalRowCount=0;
		int totalColCount=0;
		int tcIdIndex = 0;
		int transactionIndex=0;
		HashMap<String, Integer> colHeader;
		Row row;
		ArrayList<String> transactions = new ArrayList<String>();
		String filePath = Config.testInputFile;

		try {
			//worksheet = ExcelUtilities.getWorkSheet(filePath, "TestSuite");
			worksheet = ExcelUtilities.getWorkSheet(Base.testInputFileTabMapping.get(Config.testSuiteTab), Config.testSuiteTab);
			totalRowCount = ExcelUtilities.getAllRowCount(worksheet);
			colHeader = ExcelUtilities.getColumnHeader(worksheet);
			tcIdIndex = colHeader.get("TestCaseID");
			transactionIndex = colHeader.get("Transactions");


			for(int rowIndex = 1; rowIndex<totalRowCount;rowIndex++) {
				row = worksheet.getRow(rowIndex);
				tcId = row.getCell(tcIdIndex).toString();
				if(tcId.equalsIgnoreCase(testCaseID)) {
					totalColCount = ExcelUtilities.getColumnsCount(worksheet, rowIndex);
					for(int colIndex=transactionIndex; colIndex<totalColCount; colIndex++) {

						transName = ExcelUtilities.getCellData(worksheet, colIndex, rowIndex).toString().trim();

						if(!StringUtils.equals(transName, null)) {
							if(!StringUtils.isEmpty(transName) && !StringUtils.equals(transName,"") && !StringUtils.equals(transName," ") && !StringUtils.isBlank(transName)) {
								transactions.add(transName);
							}
						}
					}
					break;
				}
			}

			startExecution2(transactions,executionFlag);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	public static void changeDate() throws Exception{
		Report.setLaunchBrowser(false);
		System.out.println("Date to be changed to "+Report.getSysDate());
		ArrayList<String> changeDateTransaction = new ArrayList<String>();
		changeDateTransaction.add("0|" + Report.getSysDate() +"|DateChange#WS@gbl");
		startExecution(changeDateTransaction);
		Report.setLaunchBrowser(true);
		
	}
	
	public static void loadTimetravelTransactions() throws Exception {
		ArrayList<String> transactions = new ArrayList<String>();
		try {
			HashMap<String, Integer> colHeader;
			int totalRowCount=0;
			worksheet = ExcelUtilities.getWorkSheet(Base.testInputFileTabMapping.get(Config.testSuiteTab), Config.testSuiteTab);
			colHeader = ExcelUtilities.getColumnHeader(worksheet);
			int executionFlag = colHeader.get("ExecutionFlag");
			int tcIdIndex = colHeader.get("TestCaseID");
			totalRowCount = ExcelUtilities.getAllRowCount(worksheet);
			
			transactions.add("0|" + Report.getSysDate() +"|DateChange#WS@gbl");
			for(int rowIndex = 2; rowIndex<totalRowCount;rowIndex++) {
				String tempTransaction=ExcelUtilities.getCellData(worksheet, Report.getCurrentTimeTravelCol(), rowIndex);
				String tempExecFlag=ExcelUtilities.getCellData(worksheet,executionFlag,rowIndex);
				String tempTestCaseId=ExcelUtilities.getCellData(worksheet,tcIdIndex,rowIndex);
				if(!tempTransaction.equals("") && tempExecFlag.equalsIgnoreCase("Y")) {
					if(!timetravelFailedTestCaseList.contains(tempTestCaseId))
						transactions.add(rowIndex +"|" + tempTestCaseId + "|" + tempTransaction);
				}
			}
			
			//changeDate();
			startExecution(transactions);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Author - Vipin Phogat 
	 * Date Modified - 12/14/2020 
	 * Description - This method is responsible for launching the web browser and initiate the execution on UI
	 */
	public static void startExecution(ArrayList list) throws Exception {
		
		//String transactionName;
		int tcIdIndex, transactionIndex;
		int dFielNameIndex;
		int object_fieldNameIndex;
		String browser = getBrowser(testInputFileTabMapping.get(Config.testSuiteTab),Config.testSuiteTab);
		Report.setBrowserName(browser);
		if(!StringUtils.equalsIgnoreCase(browser, "NA")) {
			if(!(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel") && list.size()<2)) {
			WebDriverHelper.launchDriver(browser, Config.applicationUrl);
			if(StringUtils.equalsIgnoreCase(Config.screenRecording, "on")) {
				try {
					if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel"))
						CustomScreenRecorder.startRecording(Report.getTimetravelTestCaseId());
					else
						CustomScreenRecorder.startRecording(Report.getTestCaseId());
					Report.setScreenRecordingStarted(true);
				}catch(Exception e) {
					log.error("Recording not started");
				}
			}
			Base.test.log(Status.PASS, "<font color="+ "green>" +"<b>Browser : "+browser+"</b></font>");
			}
		}
		
		//Workbook dataWorkBook=ExcelUtilities.getWorkbook(Config.testInputFile);
		//Workbook objectDataWorkBook=ExcelUtilities.getWorkbook(Config.ObjectRepo);
		HashMap<String, Workbook> dataWorkbookMapping = ExcelUtilities.retrieveWorkbookAndTabList(Config.testInputFile);
		HashMap<String, Workbook> orWorkbookMapping = ExcelUtilities.retrieveWorkbookAndTabList(Config.ObjectRepo);
		Workbook dataWorkBook;
		Workbook objectDataWorkBook;
		
		int size = list.size();
		for(int i=0;i<size;i++) {
			transactionRowIndexTT=-1;
			if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
				String[] splitIndexAndTrans=list.get(i).toString().split("\\|");
				transactionRowIndexTT=Integer.parseInt(splitIndexAndTrans[0]);
				Report.setTestCaseId(splitIndexAndTrans[1].trim());
				tcId=Report.getTestCaseId();
				transactionName =splitIndexAndTrans[2];
				if(timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
						continue;
			}else {
				transactionName = list.get(i).toString();
			}
				
				Report.setTransactionName(transactionName);
				String transactionTab=transactionName;
				
				log.info("================================================================================================================");
				if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel"))
					log.info("********* System Date : "+ Report.getSysDate() +" | Test Case : "+ tcId +" | Transaction : "+ transactionName+" *********");
				else
					log.info("********* Test Case : "+ tcId +" | Transaction : "+ transactionName+" *********");
				
				
				if(transactionName.equalsIgnoreCase("end")) {
					String msg="Execution of "+ tcId +" completed.";
					log.info(msg);
					String extentMsg= "<font color=" + "white>" + "Execution of Test case '"+tcId+"' Completed."+"</font>";
					Markup marker = MarkupHelper.createLabel(extentMsg, ExtentColor.TEAL);
					test.log(Status.PASS,marker);
					ExcelUtilities.writeToTimetravelExcelReport("NA","End of Test Case", "Pass","");
					break;
				}
					
				
				//Identify tab name from transaction name
				if(transactionName.contains("#")){
					String[] splitTransactionName=transactionName.split("#");
					transactionTab=splitTransactionName[0];
				}
				
				//Read data sheet
				
				//dataSheet = ExcelUtilities.getWorkSheetFromWorkbook(dataWorkBook, transactionTab);
				try {
					dataSheet = ExcelUtilities.getWorkSheetFromWorkbook(dataWorkbookMapping.get(transactionTab), transactionTab);
					log.info("Data sheet File :"+Base.testInputFileTabMapping.get(transactionTab));
				}catch(Exception e) {
					String msg="Issue while reading transaction '"+Controller.transactionName + "' in test data file.";
					log.error(msg,e);
					Assertions.writeResults("fail", msg, msg,  false,"hard");
				}
				testDataHeader = ExcelUtilities.getColumnHeader(dataSheet);
				transactionIndex = ExcelUtilities.getTransactionIndexFromTestData(dataSheet, tcId, transactionName);
			
				//Fail the test case if transaction is not found in data sheet
				if(transactionIndex==-1) {
					String msg="Transaction '"+Controller.transactionName + "' not found for test case '" + tcId + "'  in Tab '" + transactionTab+ "' of test data sheet.";
					log.error(msg);
					Assertions.writeResults("fail", msg, msg,  false,"hard");
				}
				
				dFielNameIndex = testDataHeader.get("FieldName");
				fieldNameList = ExcelUtilities.getTestData(dFielNameIndex, dataSheet);
				fieldValue = ExcelUtilities.getTestData(dFielNameIndex, transactionIndex, dataSheet);
				
				
				//Read Repository Sheet
				//repositorySheet = ExcelUtilities.getWorkSheetFromWorkbook(objectDataWorkBook, transactionTab);
				try {
					repositorySheet = ExcelUtilities.getWorkSheetFromWorkbook(orWorkbookMapping.get(transactionTab), transactionTab);
				}catch(Exception e) {
					String msg="Issue while reading object repository for transaction '"+Controller.transactionName + "'.";
					log.error(msg,e);
					Assertions.writeResults("fail", msg, msg,  false,"hard");
				}
				objectDataHeader = ExcelUtilities.getColumnHeader(repositorySheet);
				object_fieldNameIndex = objectDataHeader.get("FieldName");
				String[][] objectDataArray=ExcelUtilities.getObjectDataArray(object_fieldNameIndex, repositorySheet);
				for(int j=0;j<objectDataArray.length;j++) {
					  String key =  objectDataArray[j][0];
					  if(fieldValue.get(key)!=null && !fieldValue.get(key).equals("")) {
						  String inputValue=fieldValue.get(key);
						  if(inputValue.toLowerCase().contains("_ifpresent")){
							  Report.setOptionalFieldFlag(true);
							  inputValue=inputValue.replace("_ifpresent", "");
						  }
						  
						  if(inputValue.equalsIgnoreCase("_PAUSE") && StringUtils.equalsIgnoreCase(Config.userInteraction, "on")) {
							  String message="Currently waiting at - " + Report.getTestCaseId() +" : " + Report.getTransactionName() +" : "+ Report.getFieldName();
							  message=message+"\n\n" + "Click on any button to proceed..";
							  displayJFrame("IPAT",message,JOptionPane.OK_CANCEL_OPTION);
							  continue;
						  }
						  
						  WebDriver.dispatch(inputValue,objectDataArray[j][1]);
						  WebDriver.handleLoader();
						  if(Config.continueOnValidationFailure.equalsIgnoreCase("N")){
							 if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
									 if(!Base.timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
											Base.timetravelFailedTestCaseList.add(Report.getTestCaseId());
							}else {
								  Base.sAssert.assertAll();
								  Base.sa.assertAll();
							}
						  }
						Report.setFieldName("");
						Report.setFieldType("");
						Report.setFieldAction("");
						Report.setFieldValue("");
						Report.setLocator("");
						Report.setOptionalFieldFlag(false);
					  }
					  if(timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
							break;
				  }
				Report.setTransactionName("");
				
		}
		
		ExcelUtilities.closeWorkbook(dataWorkbookMapping);
		ExcelUtilities.closeWorkbook(orWorkbookMapping);
	}

	public static void startExecution2(ArrayList list,String executionFlag) throws Exception {

		//String transactionName;
		int tcIdIndex, transactionIndex;
		int dFielNameIndex;
		int object_fieldNameIndex;
		String browser = getBrowser(testInputFileTabMapping.get(Config.testSuiteTab),Config.testSuiteTab);
		Report.setBrowserName(browser);
		if(!StringUtils.equalsIgnoreCase(browser, "NA")) {
			if(!(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel") && list.size()<2)) {
				WebDriverHelper.launchDriver2(browser, Config.applicationUrl,executionFlag);
				if(StringUtils.equalsIgnoreCase(Config.screenRecording, "on")) {
					try {
						if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel"))
							CustomScreenRecorder.startRecording(Report.getTimetravelTestCaseId());
						else
							CustomScreenRecorder.startRecording(Report.getTestCaseId());
						Report.setScreenRecordingStarted(true);
					}catch(Exception e) {
						log.error("Recording not started");
					}
				}
				Base.test.log(Status.PASS, "<font color="+ "green>" +"<b>Browser : "+browser+"</b></font>");
			}
		}

		//Workbook dataWorkBook=ExcelUtilities.getWorkbook(Config.testInputFile);
		//Workbook objectDataWorkBook=ExcelUtilities.getWorkbook(Config.ObjectRepo);
		HashMap<String, Workbook> dataWorkbookMapping = ExcelUtilities.retrieveWorkbookAndTabList(Config.testInputFile);
		HashMap<String, Workbook> orWorkbookMapping = ExcelUtilities.retrieveWorkbookAndTabList(Config.ObjectRepo);
		Workbook dataWorkBook;
		Workbook objectDataWorkBook;

		int size = list.size();
		for(int i=0;i<size;i++) {
			transactionRowIndexTT=-1;
			if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
				String[] splitIndexAndTrans=list.get(i).toString().split("\\|");
				transactionRowIndexTT=Integer.parseInt(splitIndexAndTrans[0]);
				Report.setTestCaseId(splitIndexAndTrans[1].trim());
				tcId=Report.getTestCaseId();
				transactionName =splitIndexAndTrans[2];
				if(timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
					continue;
			}else {
				transactionName = list.get(i).toString();
			}

			Report.setTransactionName(transactionName);
			String transactionTab=transactionName;

			log.info("================================================================================================================");
			if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel"))
				log.info("********* System Date : "+ Report.getSysDate() +" | Test Case : "+ tcId +" | Transaction : "+ transactionName+" *********");
			else
				log.info("********* Test Case : "+ tcId +" | Transaction : "+ transactionName+" *********");


			if(transactionName.equalsIgnoreCase("end")) {
				String msg="Execution of "+ tcId +" completed.";
				log.info(msg);
				String extentMsg= "<font color=" + "white>" + "Execution of Test case '"+tcId+"' Completed."+"</font>";
				Markup marker = MarkupHelper.createLabel(extentMsg, ExtentColor.TEAL);
				test.log(Status.PASS,marker);
				ExcelUtilities.writeToTimetravelExcelReport("NA","End of Test Case", "Pass","");
				break;
			}


			//Identify tab name from transaction name
			if(transactionName.contains("#")){
				String[] splitTransactionName=transactionName.split("#");
				transactionTab=splitTransactionName[0];
			}

			//Read data sheet

			//dataSheet = ExcelUtilities.getWorkSheetFromWorkbook(dataWorkBook, transactionTab);
			try {
				dataSheet = ExcelUtilities.getWorkSheetFromWorkbook(dataWorkbookMapping.get(transactionTab), transactionTab);
				log.info("Data sheet File :"+Base.testInputFileTabMapping.get(transactionTab));
			}catch(Exception e) {
				String msg="Issue while reading transaction '"+Controller.transactionName + "' in test data file.";
				log.error(msg,e);
				Assertions.writeResults("fail", msg, msg,  false,"hard");
			}
			testDataHeader = ExcelUtilities.getColumnHeader(dataSheet);
			transactionIndex = ExcelUtilities.getTransactionIndexFromTestData(dataSheet, tcId, transactionName);

			//Fail the test case if transaction is not found in data sheet
			if(transactionIndex==-1) {
				String msg="Transaction '"+Controller.transactionName + "' not found for test case '" + tcId + "'  in Tab '" + transactionTab+ "' of test data sheet.";
				log.error(msg);
				Assertions.writeResults("fail", msg, msg,  false,"hard");
			}

			dFielNameIndex = testDataHeader.get("FieldName");
			fieldNameList = ExcelUtilities.getTestData(dFielNameIndex, dataSheet);
			fieldValue = ExcelUtilities.getTestData(dFielNameIndex, transactionIndex, dataSheet);


			//Read Repository Sheet
			//repositorySheet = ExcelUtilities.getWorkSheetFromWorkbook(objectDataWorkBook, transactionTab);
			try {
				repositorySheet = ExcelUtilities.getWorkSheetFromWorkbook(orWorkbookMapping.get(transactionTab), transactionTab);
			}catch(Exception e) {
				String msg="Issue while reading object repository for transaction '"+Controller.transactionName + "'.";
				log.error(msg,e);
				Assertions.writeResults("fail", msg, msg,  false,"hard");
			}
			objectDataHeader = ExcelUtilities.getColumnHeader(repositorySheet);
			object_fieldNameIndex = objectDataHeader.get("FieldName");
			String[][] objectDataArray=ExcelUtilities.getObjectDataArray(object_fieldNameIndex, repositorySheet);
			for(int j=0;j<objectDataArray.length;j++) {
				String key =  objectDataArray[j][0];
				if(fieldValue.get(key)!=null && !fieldValue.get(key).equals("")) {
					String inputValue=fieldValue.get(key);
					if(inputValue.toLowerCase().contains("_ifpresent")){
						Report.setOptionalFieldFlag(true);
						inputValue=inputValue.replace("_ifpresent", "");
					}

					if(inputValue.equalsIgnoreCase("_PAUSE") && StringUtils.equalsIgnoreCase(Config.userInteraction, "on")) {
						String message="Currently waiting at - " + Report.getTestCaseId() +" : " + Report.getTransactionName() +" : "+ Report.getFieldName();
						message=message+"\n\n" + "Click on any button to proceed..";
						displayJFrame("IPAT",message,JOptionPane.OK_CANCEL_OPTION);
						continue;
					}

					WebDriver.dispatch(inputValue,objectDataArray[j][1]);
					WebDriver.handleLoader();
					if(Config.continueOnValidationFailure.equalsIgnoreCase("N")){
						if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
							if(!Base.timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
								Base.timetravelFailedTestCaseList.add(Report.getTestCaseId());
						}else {
							Base.sAssert.assertAll();
							Base.sa.assertAll();
						}
					}
					Report.setFieldName("");
					Report.setFieldType("");
					Report.setFieldAction("");
					Report.setFieldValue("");
					Report.setLocator("");
					Report.setOptionalFieldFlag(false);
				}
				if(timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
					break;
			}
			Report.setTransactionName("");

		}

		ExcelUtilities.closeWorkbook(dataWorkbookMapping);
		ExcelUtilities.closeWorkbook(orWorkbookMapping);
	}


	/*
	 * Author - Vipin Phogat 
	 * Date - 12/14/2020 
	 * Description - This method is responsible for fetching the browser mentioned against each Test Case ID
	 */
	public static String getBrowser(String filePath, String sheetName) {
		if(Report.getLaunchBrowser()) {
			if(StringUtils.equalsIgnoreCase(Config.executionType,"timetravel")){
				try {
					Sheet testSuite = ExcelUtilities.getWorkSheet(filePath, sheetName);
					Browser = testSuite.getRow(1).getCell(Report.getCurrentTimeTravelCol()).toString();
					log.info("Broswer to be launched: " + Browser);
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					log.error("Broswer to be launched: " + Browser, ex);
				}
			}else {
				try {
					
					Sheet testSuite = ExcelUtilities.getWorkSheet(filePath, sheetName);
					HashMap<String,Integer> colHeader;
					String testID;
					
					String cellData;
					int totalRows;
					int testID_index;
					int browser_index;
					
					Cell cell;
					Row row;
					
					colHeader = ExcelUtilities.getColumnHeader(testSuite);
					totalRows = ExcelUtilities.getAllRowCount(testSuite);
					testID_index = colHeader.get("TestCaseID");
					browser_index = colHeader.get("Browser");
					
					for(int i=1;i<totalRows;i++) {
						row = testSuite.getRow(i);
						testID = row.getCell(testID_index).toString();
						if(testID.equalsIgnoreCase(tcId)) {
							Browser = row.getCell(browser_index).toString();
						}
					}
					log.info("Broswer to be launched: " + Browser);
		
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					log.error("Broswer to be launched: " + Browser, ex);
				}
			}
		}else {
			Browser="NA";
		}
		return Browser;
		
	}
	
	public static int displayJFrame(String title, String message, int jOpt) {
		
		JFrame frame=new JFrame("IPAT");
		frame.setAlwaysOnTop(true);
		frame.setResizable(true);
		frame.setBackground(Color.MAGENTA);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		 int response = JOptionPane.showConfirmDialog(frame, message, title, jOpt);
		 frame.dispose();
		 return response;
	}
	
	
	
	

}
