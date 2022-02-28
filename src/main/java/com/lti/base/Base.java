package com.lti.base;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.TestUtil.CustomScreenRecorder;
import com.lti.TestUtil.MongoDB;
import com.lti.TestUtil.Report;
import com.lti.controller.Controller;
import com.lti.controller.CustomSoftAssert;
import com.lti.controller.TestCase;
import com.lti.controller.TimetravelTestCase;
import com.lti.webDriver.UiHandler;
import com.lti.webDriver.WebDriverHelper;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Base implements ITest {
	private ThreadLocal<String> testName = new ThreadLocal<>();
	//public static String filePath;
	public static WebDriverWait wait;
	public  ExtentReports report = Report.getInstance();
	public static com.mongodb.client.MongoClient mongoClient;
	public static ExtentTest test = null;
	public static CustomSoftAssert sa=new CustomSoftAssert();
	public static CustomSoftAssert sAssert;
	public static ThreadLocal<ExtentTest> testReport = new ThreadLocal<ExtentTest>();
	static Logger log = Logger.getLogger(Base.class.getName());
	public static Document mongoDoc;
	public static List<DBObject> dataLog = new ArrayList<DBObject>();
	public static List<Document> docList;
	public static List<Document> dbLog;
	public static Document resultDoc;
	public static Document resultLog;
	public static HashMap<String, String> testInputFileTabMapping = new HashMap<String, String>();
	public static HashMap<String, String> objectRepositoryFileTabMapping = new HashMap<String, String>();
	public static boolean stopTheSuite=false;
	public static List<String> timetravelFailedTestCaseList=new ArrayList<String>();
	 
	@BeforeSuite
	public void setUp() throws Exception {
		testInputFileTabMapping = ExcelUtilities.retrieveFileAndTabList(Config.testInputFile);
		objectRepositoryFileTabMapping = ExcelUtilities.retrieveFileAndTabList(Config.ObjectRepo);
		timetravelFailedTestCaseList.clear();
	}
	
	@AfterSuite
	public void tearDown() throws Exception {
		WebDriverHelper.quitWebDriver();
	}
	
	@BeforeMethod
	public void beforeTest(Method method, Object[] testData,ITestContext ctx) throws Exception {
		resultDoc = new Document();
		resultLog = new Document();
		sAssert=new CustomSoftAssert();
		//log.info("Starting test case execution");
		String[] tcDetails;
		String tcName;
		
		if (testData.length > 0) {
			if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
				tcName="Date-"+((String)testData[0]).replace("/", "_");
				Report.setTimetravelTestCaseId(tcName);
			}else {
				tcDetails = testData[0].toString().split("=");
				tcName = tcDetails[2].toString();
				tcName=tcName.substring(0, tcName.length() - 1);
				Report.setTestCaseId(tcName);
			}
		      testName.set(tcName);
		      ctx.setAttribute("testName", testName.get());
		   } else
		      ctx.setAttribute("testName", method.getName());
		Report.setScreenRecordingStarted(false);
		
	}
	
	@AfterMethod
	public void afterTest() throws Exception {
		if(TestCase.executeTC || TimetravelTestCase.executeTC) {
			try {
				//WebDriverHelper.quitWebDriver();
				log.info("Completed test case execution");
				CustomScreenRecorder.stopRecording();
			}catch(Exception e) {}
		}
		Report.setTestCaseId("");
		Report.setBrowserName("");
		Report.setTimetravelTestCaseId("");
	}

	@Override
	public String getTestName() {
		// TODO Auto-generated method stub
		return testName.get();
	}
	

}
