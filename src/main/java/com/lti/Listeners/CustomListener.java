package com.lti.Listeners;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.lti.TestUtil.MongoDB;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.controller.Controller;
import com.lti.dataProviders.DataProviders;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class CustomListener extends Base implements ITestListener {
	static Logger log = Logger.getLogger(CustomListener.class.getName());
	public static String tcName;
	public static Date startDate,endDate;
	public static Timestamp startTime,endTime;
	public static Calendar cal;
	public static long durationInmillis;
	public static int durationInsec;
	public static int durationInMins;

	@Override
	public void onTestStart(ITestResult result) {
		// TODO Auto-generated method stub
		docList=new ArrayList<Document>();
		dbLog = new ArrayList<Document>();
		tcName = result.getTestContext().getAttribute("testName").toString();
		test = report.createTest(tcName);
		testReport.set(test);
		/*MongoDB.addTcName(tcName);
		MongoDB.addEnvDetails();
		MongoDB.addTimeStamp();*/
		mongoDoc = new Document();
		startDate = new Date();
		startTime = new Timestamp(startDate.getTime());
		cal = Calendar.getInstance();
		cal.setTimeInMillis(startTime.getTime());
		
		dataLog.clear();
		
		
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		// TODO Auto-generated method stub
		String textLog = "<font color=" + "white>" + "Test case '"+result.getTestContext().getAttribute("testName").toString()+"' PASSED"+"</font>";
		Markup marker = MarkupHelper.createLabel(textLog, ExtentColor.GREEN);
		test.pass(marker);
		report.flush();
		startDate = new Date();
		endTime = new Timestamp(startDate.getTime());
		durationInmillis = endTime.getTime() - startTime.getTime();
		durationInsec = (int) (durationInmillis/1000);
		durationInMins = (durationInsec%3600)/60;
		MongoDB.addDocument(tcName, "Passed", dataLog, Config.platform, Config.Region,durationInMins);
		durationInMins = 0;
		durationInsec = 0;
		durationInmillis = 0;
		/*MongoDB.addTcStatus("Passed");
		MongoDB.appendTestStepLog(dbLog, resultDoc);
		MongoDB.addDocument(resultDoc);*/
		
		
	}

	@Override
	public void onTestFailure(ITestResult result) {
		// TODO Auto-generated method stub
		docList = new ArrayList<Document>();
		String textLog = "<font color=" + "white>" + "Test case '"+result.getTestContext().getAttribute("testName").toString()+"' FAILED"+"</font>";
		Markup marker = MarkupHelper.createLabel(textLog, ExtentColor.RED);
		test.fail(marker);
		report.flush();
		startDate = new Date();
		endTime = new Timestamp(startDate.getTime());
		durationInmillis = endTime.getTime() - startTime.getTime();
		durationInsec = (int) (durationInmillis/1000);
		durationInMins = (durationInsec%3600)/60;
		MongoDB.addDocument(tcName, "Failed", dataLog, Config.platform, Config.Region,durationInMins);
		//MongoDB.addTcStatus("Failed");
		//MongoDB.addDocument(resultDoc);
		
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		test.log(Status.SKIP, "<font color="+ "amber>" + "Skipping the execution of Test case '"+result.getTestContext().getAttribute("testName").toString()+"'" + "</font></b>");
		String textLog = "<b><font color=" + "black>" + "Test case '"+result.getTestContext().getAttribute("testName").toString()+"' SKIPPED"+"</font></b>";
		Markup marker = MarkupHelper.createLabel(textLog, ExtentColor.AMBER);
		test.skip(marker);
		report.flush();
		
		durationInMins = 0;
		MongoDB.addDocument(tcName, "Skipped", dataLog, Config.platform, Config.Region,durationInMins);
		//MongoDB.addTcStatus("Skipped");
		//MongoDB.appendTestStepLog(resultLog, resultDoc);
		//MongoDB.addDocument(resultDoc);
		
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart(ITestContext context) {
		MongoDB.createConnection();
	}

	@Override
	public void onFinish(ITestContext context) {
		// TODO Auto-generated method stub
		MongoDB.closeConnection();
		
	}

	

}
