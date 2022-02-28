package com.lti.TestUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.lti.base.Config;
import com.lti.base.Init;
import com.lti.webDriver.UiHandler;
//import com.google.common.io.Files;
//import com.aventstack.extentreports.reporter.configuration.ViewName;
import com.lti.webDriver.WebDriverHelper;

public class Report extends WebDriverHelper {
	static Logger log = Logger.getLogger(Report.class.getName());
	private static ExtentReports extent;
	public static String screenshotPath;
	public static String screenshotName;
	public static String folderName;
	private static String testCaseId="",  timetravelTestCaseId="";
	private static String transactionName="";
	private static String fieldName="";
	private static String fieldType="";
	private static String fieldAction="";
	private static String locator="";
	private static String fieldValue="";
	private static String testCaseExecuteFlag="";
	public static String backUpFolderPath="";
	public static boolean optionalFieldFlag=false;
	public static String screenRecordingName="";
	public static boolean screenRecordingStarted=false;
	public static String browserName="";
	public static String sysDate="";
	public static int currentTimeTravelCol=-1;
	public static String timetravelReportExcelFilePath=System.getProperty("user.dir")+"\\TestExecutionReport\\TimetravelReport.xlsx";
	public static String runtimeValue1="";
	public static boolean launchBrowser=true;
	public static ExtentReports getInstance(){
		if (extent == null) {
			String resultDir;
			String reportPath = System.getProperty("user.dir")+"\\TestExecutionReport\\Spark.html";
			String screenshotFolder = System.getProperty("user.dir") + "\\TestExecutionReport\\Screenshot";
			String timetravelReportPath = System.getProperty("user.dir")+"\\TestExecutionReport\\SparkTimetravel.html";
			try {
				resultDir = createReportDirectory(reportPath,screenshotFolder,timetravelReportPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			File file = new File("reportPath");

			extent = new ExtentReports();
			
			ExtentSparkReporter spark = new ExtentSparkReporter(System.getProperty("user.dir") + "\\TestExecutionReport\\Spark.html");
			//spark.loadXMLConfig(System.getProperty("user.dir") + "\\ExtentConfig.xml");
			try {
				spark.loadXMLConfig(System.getProperty("user.dir") + "\\dependencies\\ExtentConfig.xml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			 * spark.viewConfigurer() .viewOrder() .as(new ViewName[] { ViewName.DASHBOARD,
			 * ViewName.TEST, ViewName.AUTHOR, ViewName.DEVICE, ViewName.EXCEPTION,
			 * ViewName.LOG }).apply();
			 */
			extent.attachReporter(spark);
			
		}
		
		return extent;
	}
	
	public static void captureScreenshot() {

		File scrFile = ((TakesScreenshot)iDriver).getScreenshotAs(OutputType.FILE);
		Date d = new Date();
		screenshotName = d.toString().replace(":", "_").replace(" ", "_") + ".jpg";
		screenshotPath = System.getProperty("user.dir") + "\\TestExecutionReport\\Screenshot\\" + screenshotName;

		try {
			FileUtils.copyFile(scrFile, new File(System.getProperty("user.dir") + "\\TestExecutionReport\\Screenshot\\" + screenshotName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		screenshotPath=screenshotPath.substring(screenshotPath.indexOf("\\Screenshot\\"));
		screenshotPath="."+screenshotPath;
	
	}
	public static boolean captureSelectiveScreenshot() {
		boolean screenshotTaken=false;
		File scrFile = ((TakesScreenshot)iDriver).getScreenshotAs(OutputType.FILE);
		Date d = new Date();
		String timeStampforFileName=new SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date());
		screenshotName = Report.getTransactionName().replace("#","_") + "_"+ timeStampforFileName + ".jpg";
		screenshotPath = System.getProperty("user.dir") + "\\TestExecutionReport\\Screenshot\\SelectiveScreenshots\\" + screenshotName;
		screenshotTaken =true;
		try {
			FileUtils.copyFile(scrFile, new File(screenshotPath));
		} catch (IOException e) {
			screenshotTaken=false;
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		screenshotPath=screenshotPath.substring(screenshotPath.indexOf("\\Screenshot\\"));
		screenshotPath="."+screenshotPath;
		return screenshotTaken;
	
	}
	public static String createReportDirectory(String existingFilePath, String screenshotFolder, String existingTimetravelFilePath) throws IOException {
		Date d = new Date();
		folderName = d.toString().replace(":", "_").replace(" ", "_");
		backUpFolderPath = System.getProperty("user.dir") + "\\TestExecutionReport\\Backup_"+folderName;
		File reportBackUp = new File(backUpFolderPath);
		File screenshotBackup = new File(System.getProperty("user.dir") + "\\TestExecutionReport\\Backup_"+folderName + "\\Screenshot");
		
		File ReportPath = new File(existingFilePath);
		File screenShots = new File(screenshotFolder);
		File timetravelReportPath=new File(existingTimetravelFilePath);
		File timetravelReportExcelFile=new File(timetravelReportExcelFilePath);
		if(ReportPath.exists() || timetravelReportPath.exists()) {
			if(reportBackUp.exists()) {
				log.info("Backup directory '"+folderName+ "' already exist....Overwriting the directory content");
			}else {
				reportBackUp.mkdir();
				log.info("Backup directory '"+folderName+ "' not found....creating the directory");	
			}
			if(screenShots.exists()) {
				//Files.copy(screenShots.toPath(), screenshotBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
				FileUtils.copyDirectory(screenShots, screenshotBackup);
				Init.clearFolder(screenShots);
			}
			if(ReportPath.exists()) {
				reportBackUp = new File(System.getProperty("user.dir") + "\\TestExecutionReport\\Backup_"+folderName+"\\Spark.html");
				Files.move(ReportPath.toPath(), reportBackUp.toPath(),StandardCopyOption.REPLACE_EXISTING);
			}
			if(timetravelReportPath.exists()) {
				reportBackUp = new File(System.getProperty("user.dir") + "\\TestExecutionReport\\Backup_"+folderName+"\\SparkTimetravel.html");
				Files.move(timetravelReportPath.toPath(), reportBackUp.toPath(),StandardCopyOption.REPLACE_EXISTING);
			}
			
			if(timetravelReportExcelFile.exists())
				timetravelReportExcelFile.delete();
		}
		
		
		return folderName;
	}
	
	public static void killProcess(String serviceName) throws Exception {
		String command= "\\System32\\taskkill /F /IM ";
		command = System.getenv("SystemRoot") + command;
		try {
			Runtime.getRuntime().exec(command + serviceName);
		} catch (Exception e) {
			log.error("Failed to kill process " + serviceName +"." , e);
			throw new Exception(e);
		}
	}
	
	public static void setTestCaseId(String testCaseId) {
		Report.testCaseId = testCaseId;
	}
	public static String getTestCaseId() {
		return Report.testCaseId;
	}
	public static void setTransactionName(String transactionName) {
		Report.transactionName = transactionName;
	}
	public static String getTransactionName() {
		return Report.transactionName;
	}
	public static void setFieldName(String fieldName) {
		Report.fieldName = fieldName;
	}
	public static String getFieldName() {
		return Report.fieldName;
	}
	public static void setFieldType(String fieldType) {
		Report.fieldType = fieldType;
	}
	public static String getFieldType() {
		return Report.fieldType;
	}
	public static void setFieldAction(String fieldAction) {
		Report.fieldAction = fieldAction;
	}
	public static String getFieldAction() {
		return Report.fieldAction;
	}
	public static void setLocator(String locator) {
		Report.locator = locator;
	}
	public static String getLocator() {
		return Report.locator;
	}
	public static void setFieldValue(String fieldValue) {
		Report.fieldValue = fieldValue;
	}
	public static String getFieldValue() {
		return Report.fieldValue;
	}
	public static void setTestCaseExecuteFlag(String testCaseExecuteFlag) {
		Report.testCaseExecuteFlag = testCaseExecuteFlag;
	}
	public static String getTestCaseExecuteFlag() {
		return Report.testCaseExecuteFlag;
	}
	public static void setOptionalFieldFlag(Boolean optionalFieldFlag) {
		Report.optionalFieldFlag = optionalFieldFlag;
	}
	public static boolean getOptionalFieldFlag() {
		return Report.optionalFieldFlag;
	}
	public static void setScreenRecordingName(String screenRecordingName) {
		Report.screenRecordingName = screenRecordingName;
	}
	public static String getScreenRecordingName() {
		return Report.screenRecordingName;
	}
	public static void setScreenRecordingStarted(Boolean screenRecordingStarted) {
		Report.screenRecordingStarted = screenRecordingStarted;
	}
	public static boolean getScreenRecordingStarted() {
		return Report.screenRecordingStarted;
	}
	public static void setBrowserName(String browserName) {
		Report.browserName = browserName;
	}
	public static String getBrowserName() {
		return Report.browserName;
	}
	public static void setSysDate(String sysDate) {
		Report.sysDate = sysDate;
	}
	public static String getSysDate() {
		return Report.sysDate;
	}
	public static void setCurrentTimeTravelCol(int currentTimeTravelCol) {
		Report.currentTimeTravelCol = currentTimeTravelCol;
	}
	public static int getCurrentTimeTravelCol() {
		return Report.currentTimeTravelCol;
	}
	public static void setTimetravelTestCaseId(String timetravelTestCaseId) {
		Report.timetravelTestCaseId = timetravelTestCaseId;
	}
	public static String getTimetravelTestCaseId() {
		return Report.timetravelTestCaseId;
	}
	public static void setRuntimeValue1(String runtimeValue1) {
		Report.runtimeValue1 = runtimeValue1;
	}
	public static String getRuntimeValue1() {
		return Report.runtimeValue1;
	}
	public static void setLaunchBrowser(Boolean launchBrowser) {
		Report.launchBrowser = launchBrowser;
	}
	public static boolean getLaunchBrowser() {
		return Report.launchBrowser;
	}
}


