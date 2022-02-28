package com.lti.api;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.log4j.Logger;
import org.apache.maven.surefire.shared.io.FileUtils;
import org.testng.Assert;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.lti.TestUtil.MongoDB;
import com.lti.TestUtil.Report;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.base.Init;
import com.lti.controller.Controller;
import com.lti.controller.CustomAssert;
import com.lti.webDriver.Assertions;
/*
 * Date - 4/7/2021
 * Author - Sheetal Jadhav
 * Description - WebService Testing through JMeter
*/
public class WebServiceUtil {
	static Logger log = Logger.getLogger(WebServiceUtil.class.getName());
	public static boolean wsFailure =false;
	public static boolean wsAssertionFailure =false;
	public static String WsFolderName="WebServiceFiles";
	public static String parentResultPath="", defaultResultPath="";
	public static void executeWebService(String jmxFileName, String[][] paramValueArr)throws Exception
	{
		try {
			wsFailure=false;
			wsAssertionFailure=false;
	        StandardJMeterEngine jmeter = new StandardJMeterEngine();
	        JMeterUtils.loadJMeterProperties(Config.jmeterPath + "\\bin\\jmeter.properties");
	        JMeterUtils.setJMeterHome(Config.jmeterPath);
	        JMeterUtils.initLocale();
	        SaveService.loadProperties();
	        
	        JMeterUtils.setProperty("resultcollector.action_if_file_exists", "DELETE");
	        if(paramValueArr.length>0) {
	        	for(int i=0;i<paramValueArr.length;i++)
	        	  JMeterUtils.setProperty(paramValueArr[i][0], paramValueArr[i][1]);
	        }
	        //JMeterUtils.setProperty("pwd","123@test");
	        File in = new File(Config.jmeterPath + "\\bin\\" + WsFolderName + "\\" + jmxFileName);
	        HashTree testPlanTree = SaveService.loadTree(in);
			
			Summariser summer = null; String
			summariserName=JMeterUtils.getPropDefault("summariser.name", "summary");
			if(summariserName.length() >0){ 
				summer=new Summariser(summariserName);
			 } 
			String  resultFile=defaultResultPath +"/default.csv";
			MyResultCollector logger=new MyResultCollector(summer);
			logger.setFilename(resultFile);
			testPlanTree.add(testPlanTree.getArray()[0],logger);
	        jmeter.configure(testPlanTree);
	        
	        log.info("Executing WebServices from : "+in.getAbsolutePath());
	        jmeter.run(); 
	        String command="C:/Windows/System32/cmd.exe /c " + Config.jmeterPath.replace("\\","/") + "/bin/jmeter -g "+ resultFile +" -o "+ defaultResultPath+"/HTML";
	        //Runtime.getRuntime().exec("C:/Windows/System32/cmd.exe /c C:/Software/apache-jmeter-5.4.1/bin/jmeter -g C:/IPAT/gitRepository/ProductAutomationCenter/WebServiceFiles/TC1/Results/TC1.csv -o C:/IPAT/gitRepository/ProductAutomationCenter/WebServiceFiles/TC1/Results/HTML3");
	        Runtime.getRuntime().exec(command);
	        Thread.sleep(1000);
		}
		catch(Exception e) {
			throw e;
		}
	}
	
	public static void handleWebService(String fieldName, String inputValue, String action)throws Exception {
		String jmxFileName="";
		try {
			parentResultPath="";
			defaultResultPath="";
			
			//extract param-value pairs from inputValue and store in array
			String[][]paramValueArr = new String[0][0];
			if(inputValue.contains("|")) {
				String[] splitInputValue=inputValue.split("\\|");
				jmxFileName=splitInputValue[0];
				String[] paramArr=splitInputValue[1].split(";;");
				if(paramArr.length>0) {
					paramValueArr=new String[paramArr.length][2];
					for(int i=0;i<paramArr.length;i++) {
						String[] temp=paramArr[i].split("\\=");
						paramValueArr[i][0]=temp[0];
						paramValueArr[i][1]=temp[1];
					}
				}
			}
			else {
				jmxFileName = inputValue;
			}
			createWsResultsFolder(jmxFileName);
			executeWebService(jmxFileName,paramValueArr);
			
			//Create back up of results folder and provide backup path in reports
			String backUpResultPath=createWsResultsFolderBackup();
			
			if(wsFailure || wsAssertionFailure) {
				String msg="Failure while executing WebService(s) under '.."+ jmxFileName +"'. Please find detailed results at - "+backUpResultPath;
				String msgForExtent="Failure while executing WebService(s) under '.."+ jmxFileName +"'.</br> Please find detailed results at - <a href='"+backUpResultPath+"'> Click Here</a>";
				log.info(msg);
				Assertions.writeResults("fail", msg, msgForExtent, false,"");
			}else {
				String msg="WebService(s) under '.."+ jmxFileName +"' are executed successfully. Please find detailed results at - "+backUpResultPath;
				String msgForExtent="WebService(s) under '.."+ jmxFileName +"' are executed successfully.</br> Please find detailed results at - <a href='"+backUpResultPath+"'> Click Here</a>";
				log.info(msg);
				Assertions.writeResults("pass", msg, msgForExtent, false,"");
			}
			
			if((wsFailure || wsAssertionFailure) && action.equalsIgnoreCase("V")) {
				Base.sAssert.fail();
				Base.sa.fail();
			}else if(wsAssertionFailure && !wsFailure && action.equalsIgnoreCase("I")) {
				Base.sa.fail();
				Base.sAssert.fail(); 
			}else if(wsFailure && action.equalsIgnoreCase("I")) {
				//Assert.fail();
				CustomAssert.fail();
			}else if((wsFailure || wsAssertionFailure) && action.equalsIgnoreCase("M")) {
				Base.stopTheSuite=true;
				log.error("Webservice failure. Execution of entire test suite will be stopped.");
				//Assert.fail();
				CustomAssert.fail();
			}
		}catch(Exception e) {
			String msg="Issue while executing WebService(s) under "+ jmxFileName +" : "+ e.getMessage();
			log.error(msg);
			Assertions.writeResults("fail", msg, msg, false,"");
			
			if(action.equalsIgnoreCase("V")) {
				Base.sAssert.fail();
				Base.sa.fail();
			}else {
				Assert.fail();
			}
		}
	}
	
	public static void createWsResultsFolder(String jmxFileName) {
		String subFolder = "";
		if(jmxFileName.contains("\\"))
			subFolder = "\\"+jmxFileName.substring(0, jmxFileName.lastIndexOf("\\"));
		
		String resultPathInIPAT = System.getProperty("user.dir")+"\\"+WsFolderName+ subFolder;
		File tcFolder=new File(resultPathInIPAT);
		if(!tcFolder.exists())
			tcFolder.mkdirs();
		
		parentResultPath= resultPathInIPAT+"\\"+"Results";
		File parentResultFolder=new File(parentResultPath);
		if(parentResultFolder.exists()) {
			Init.clearFolder(parentResultFolder);
			parentResultFolder.delete();
		}
		parentResultFolder.mkdirs();
		
		defaultResultPath = parentResultPath + "\\DefaultResults";
	}
	
	public static String createWsResultsFolderBackup()throws Exception {
		File parentResultFolder=new File(parentResultPath);
		String returnPath=parentResultFolder.getAbsolutePath();
		if(parentResultFolder.exists()) {
			String timeStampforFileBackup=new SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date());
			File backUpFile=new File(parentResultFolder + "_" + timeStampforFileBackup);
			FileUtils.copyDirectory(parentResultFolder, backUpFile);
			returnPath = backUpFile.getAbsolutePath();
		}
		return returnPath;
	}
}
