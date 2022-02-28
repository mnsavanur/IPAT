package com.lti.base;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.webDriver.UiHandler;

public class Config {
	
	public static HashMap<String,Object> configMap = new HashMap<String,Object>();
	//public static Object configValue = null;
	public static String testInputFile=null, testSuiteTab=null;
	public static String ObjectRepo=null;
	public static String platform = null;
	public static String implicitWait = null;
	public static String applicationUrl = null;
	public static String rootDir = null;
	public static String mongoConnectionString=null;
	public static String mongoDBName=null;
	public static String mongoResultTable=null;
	public static long timeOut = 0;
	public static String dataCarrier=null;
	public static String continueOnValidationFailure="Y";
	public static String downloadPath="", pdfCompPath="", xmlCompPath="", xmlCompTagsToExclude="", xmlCompAttributesToExclude="";
	public static String jmeterPath="";
	public static String userInteraction="", screenRecording="";
	public static String Region;
	public static String executionType="";
	static Logger log = Logger.getLogger(Config.class.getName());
	public static List<ArrayList<String>> listOfDatabse = new ArrayList<ArrayList<String>>();
	/*
	 * Date - 11/8/2020
	 * Author - Vipin Phogat
	 * Description - The below method is responsible for reading the config file and load it in the memory
	*/
	
	public static void readConfigFile(String filePath,String sheetName) throws Exception {
		
		try {
			
			Sheet configSheet = null;
			//Row currentRow = null;
			//String paramName;
			//String value;
			int rowCount=0;
			
			configSheet = ExcelUtilities.getWorkSheet(filePath,sheetName);
			rowCount = configSheet.getPhysicalNumberOfRows();
			for(int i=0;i<rowCount;i++) {
				Row currentRow = configSheet.getRow(i);
				String paramName = currentRow.getCell(0).toString().trim();
				String value=currentRow.getCell(1).toString().trim().replace("_LOCALPATH", System.getProperty("user.dir").toString());
				if(paramName!=null || paramName!="") {
					configMap.put(paramName,value);
					
				}
			}
			log.info("Read the config file successfully");
			initConfigParameters();
			log.info("Loaded the configuration parameters");
			
		}catch(Exception ex) {
			ex.printStackTrace();
			log.error("Unable to read the config file", ex);
		}
	}
	
	public static void initConfigParameters() {
		double waitTime;
		//webBrowser = getConfigParamValue("Browser").toString();
		testInputFile = getConfigParamValue("TestInput File").toString();
		testSuiteTab= getConfigParamValue("TestSuite Tab").toString();
		platform = getConfigParamValue("Platform").toString();
		ObjectRepo = getConfigParamValue("OR Sheet Path").toString();
		applicationUrl = getConfigParamValue("Application URL").toString();
		rootDir = System.getProperty("user.dir").toString();
		if(getConfigParamValue("MongoDB Connection").toString() =="") {
			mongoConnectionString = null;
			mongoDBName = null;
			mongoResultTable = null;
		}else {
			mongoConnectionString = getConfigParamValue("MongoDB Connection").toString();
			mongoDBName = getConfigParamValue("DB Name").toString();
			mongoResultTable = getConfigParamValue("Results Collection").toString();
		}
		
		waitTime = Double.parseDouble(getConfigParamValue("Timeout").toString());
		timeOut = (long)waitTime;
		dataCarrier = getConfigParamValue("Data Carrier").toString();
		continueOnValidationFailure = String.valueOf(getConfigParamValue("Continue On Validation Failure"));
		downloadPath=String.valueOf(getConfigParamValue("Download Path"));
		pdfCompPath=String.valueOf(getConfigParamValue("PDFComp Path"));
		jmeterPath=String.valueOf(getConfigParamValue("JMeter Installation Dir Path"));
		userInteraction=String.valueOf(getConfigParamValue("User Interaction"));
		xmlCompPath =String.valueOf(getConfigParamValue("XMLComp Path"));
		xmlCompTagsToExclude=String.valueOf(getConfigParamValue("XMLComp Tags To Exclude"));
		xmlCompAttributesToExclude=String.valueOf(getConfigParamValue("XMLComp Attributes To Exclude"));
		screenRecording=String.valueOf(getConfigParamValue("Screen Recording"));
		Region=String.valueOf(getConfigParamValue("Environment"));
		executionType=String.valueOf(getConfigParamValue("Execution Type"));
		loadDatabaseParamValues();
	}
	
	public static Object getConfigParamValue(String parameterName) {
		Object configParamValue = configMap.get(parameterName);
		Object configValue = null;
		if(configParamValue != null) {
			configValue = configParamValue;
		}
		return configValue;
	}
	public static void loadDatabaseParamValues() {
		
		for(int i=1;i<=10;i++) {
			try {
				String dbName=String.valueOf(getConfigParamValue("DB"+i+"_Name"));
				String dbType=String.valueOf(getConfigParamValue("DB"+i+"_Type"));
				String dbHost=String.valueOf(getConfigParamValue("DB"+i+"_Host"));
				String dbPort=String.valueOf(getConfigParamValue("DB"+i+"_Port"));
				String dbUser=String.valueOf(getConfigParamValue("DB"+i+"_User"));
				String dbPassword=String.valueOf(getConfigParamValue("DB"+i+"_Password"));
				if(dbName != null && !StringUtils.equalsIgnoreCase(dbName, "null")) {
					ArrayList<String> tempList = new ArrayList<String>();
					tempList.add("dbName="+dbName);
					tempList.add("dbType="+dbType);
					tempList.add("dbHost="+dbHost);
					tempList.add("dbPort="+dbPort);
					tempList.add("dbUser="+dbUser);
					tempList.add("dbPassword="+dbPassword);
					listOfDatabse.add(tempList);
				}
			}catch(Exception e) {
				continue;
			}
		}
		//System.out.println("test");
	}

}
