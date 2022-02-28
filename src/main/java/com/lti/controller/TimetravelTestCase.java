package com.lti.controller;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.TestUtil.MongoDB;
import com.lti.TestUtil.Report;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.controller.Controller;
import com.lti.dataProviders.DataProviders;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.maven.surefire.shared.utils.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.testng.SkipException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/*
 * Date - 7/30/2021
 * Author - Sheetal Jadhav
 * Description - Timetravel data provider, generation of test case wise report for timetravel execution
*/

public class TimetravelTestCase extends Base {
	static Logger log = Logger.getLogger(TimetravelTestCase.class.getName());
	public static Sheet testSuiteSheet;
	public static HashMap<String,Integer> colHeader;
	public static HashMap<Integer, Object> rowData;
	public int lastRowIndex;
	public int currentRowCellCount;
	public static boolean executeTC=false;
	
	@Test(dataProvider="Timetravel Test Suite Iterator", dataProviderClass = DataProviders.class)
	public void Test(String date, String colIndex) throws Exception {
		//Report.setSysDate((String)data[0][0]);
		Report.setSysDate(date);
		Report.setCurrentTimeTravelCol(Integer.parseInt(colIndex));
		Report.setRuntimeValue1(date);
		if(Base.stopTheSuite) {
			log.info("*************** Skipping execution of transactions scheduled on System Date '"+ Report.getSysDate() +"'. ***************");
			executeTC=false;	
			throw new SkipException("*************** Skipping execution of transactions scheduled on System Date '"+ Report.getSysDate() +"'. ***************");
		}else {
			executeTC=true;
			log.info("*************** Starting execution of trasnactions scheduled on System Date'"+ Report.getSysDate() + "'. ***************");
			//Controller.loadTTTransactions(Integer.parseInt((String)data[0][1]));
			Controller.loadTimetravelTransactions();
			sAssert.assertAll();
		}
		
	}
	public static void generateTimeTravelExtent()throws Exception {
	//public static void main(String args[])throws Exception {
		ExtentReports extentReports = new ExtentReports();
		ExtentTest extentTest = null;
		
		ExtentSparkReporter spark = new
		ExtentSparkReporter(System.getProperty("user.dir") + "\\TestExecutionReport\\SparkTimetravel.html");
		spark.loadXMLConfig(System.getProperty("user.dir") + "\\dependencies\\ExtentConfig.xml"); 
		extentReports.attachReporter(spark);
		
		Workbook wb=ExcelUtilities.getWorkbook(Report.timetravelReportExcelFilePath);
		//Workbook wb=ExcelUtilities.getWorkbook("C:\\Users\\ltisjadhav\\Automation\\IPAT\\repository\\ProductAutomationCenter\\TestExecutionReport\\TimetravelReport.xlsx");
		Sheet timetravelSheet=ExcelUtilities.getWorkSheetFromWorkbook(wb,"TimetravelReport");
		int rowCount=timetravelSheet.getPhysicalNumberOfRows();
		int colCount=timetravelSheet.getRow(0).getPhysicalNumberOfCells();
		
		HashMap<String, Integer> headers = ExcelUtilities.getColumnHeader(timetravelSheet);
		int tcIdIndex = headers.get("TestCaseID");
		int statusIndex = headers.get("Status");
		int screenshotPathIndex = headers.get("ScreenshotPath");
		int sysDateIndex=headers.get("SysDate");
		int timestampIndex=headers.get("Timestamp");
		int transactionNameIndex=headers.get("TransactionName");
		int fieldNameIndex=headers.get("FieldName");
		int detailsIndex=headers.get("Details");
		
		List<String> tcList=new ArrayList<String>();
		for(int i=1;i<rowCount;i++) {
			String tempTcId=ExcelUtilities.getCellData(timetravelSheet,tcIdIndex,i);
			if(!StringUtils.equalsIgnoreCase(tempTcId, "Common")) {
				if(!tcList.contains(tempTcId))
					tcList.add(tempTcId);
			}
		}
		
		for(int tcIterator=0;tcIterator<tcList.size();tcIterator++)
		{
			String curExtentTC=tcList.get(tcIterator);
			test = extentReports.createTest(curExtentTC); 
			testReport.set(test);
			int failCounter=0;
			for(int i=1;i<rowCount;i++) {
				
				String tempTcId=ExcelUtilities.getCellData(timetravelSheet,tcIdIndex,i);
				if(tempTcId.equalsIgnoreCase(curExtentTC)) {
					String tempStatus=ExcelUtilities.getCellData(timetravelSheet,statusIndex,i);
					String tempScreenshot=ExcelUtilities.getCellData(timetravelSheet,screenshotPathIndex,i);
					Boolean embedScreenshot=false;
					String tempSysDate=ExcelUtilities.getCellData(timetravelSheet,sysDateIndex,i);
					String tempTimestamp=ExcelUtilities.getCellData(timetravelSheet,timestampIndex,i);
					String tempTransactionName=ExcelUtilities.getCellData(timetravelSheet,transactionNameIndex,i);
					String tempFieldName=ExcelUtilities.getCellData(timetravelSheet,fieldNameIndex,i);
					String tempDetails=ExcelUtilities.getCellData(timetravelSheet,detailsIndex,i);
					
					if(tempScreenshot !=null && !StringUtils.equalsIgnoreCase(tempScreenshot, "")) 
						embedScreenshot=true;
					
					if(StringUtils.equalsIgnoreCase(tempStatus, "pass")) {
						if(embedScreenshot==true)
							test.log(Status.PASS, "["+tempTimestamp + "]&Tab;<font color='lightcoral'>[Sys Date " + tempSysDate + "]</font> : <b>" + tempTransactionName + "</br>"+tempFieldName+ " : "+"</b>"+tempDetails+"</br>",MediaEntityBuilder.createScreenCaptureFromPath(tempScreenshot).build());
						else
							test.log(Status.PASS,"["+ tempTimestamp + "]&Tab;<font color='lightcoral'>[Sys Date " + tempSysDate + "]</font> : <b>" + tempTransactionName + "</br>"+tempFieldName+ " : "+"</b>"+tempDetails);
				
					}else if(StringUtils.equalsIgnoreCase(tempStatus, "fail")) {
						failCounter++;
						if(embedScreenshot==true)
							test.log(Status.FAIL, "<font color=red>[" + tempTimestamp + "]&Tab;[Sys Date " + tempSysDate + "] : <b>" + tempTransactionName + "</br>"+ tempFieldName+ " : "+"</b>"+tempDetails+"</font></br>",MediaEntityBuilder.createScreenCaptureFromPath(tempScreenshot).build());
						else
							test.log(Status.FAIL, "<font color=red>[" + tempTimestamp + "]&Tab;[Sys Date " + tempSysDate + "] : <b>" + tempTransactionName + "</br>"+ tempFieldName+ " : "+"</b>"+tempDetails+"</font>");
					}else {
						log.info("TBD");
					}
				}
			}
			if(failCounter>0) {
				String textLog = "<font color=" + "white>" + "Test case '"+curExtentTC +"' FAILED"+"</font>"; 
				Markup marker = MarkupHelper.createLabel(textLog, ExtentColor.RED); 
				test.fail(marker); 
				extentReports.flush();
			}else {
				String textLog = "<font color=" + "white>" +"Test case '"+curExtentTC +"' PASSED"+"</font>"; 
				Markup marker = MarkupHelper.createLabel(textLog, ExtentColor.GREEN); 
				test.pass(marker); 
				extentReports.flush();
			}
		}
	}
	

}
