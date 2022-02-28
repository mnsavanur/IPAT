package com.lti.base;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.collections.Lists;

import com.lti.TestUtil.Report;
import com.lti.controller.TestCase;
import com.lti.controller.TimetravelTestCase;

import org.apache.log4j.PropertyConfigurator;
/*
 * Date - 4/15/2021
 * Author - Sheetal Jadhav
 * Description - Trigger execution of TestNG xml, trigger generation of test case wise report for timetravel execution
*/

public class MainClass {
	static Logger log = Logger.getLogger(MainClass.class.getName());
	public static String configFilePath ="";
	
	public static void main(String[] args)throws Exception {
			configFilePath = System.getProperty("user.dir") +"\\"+ args[0];
			
			log.info("Reading config file kept at "+ MainClass.configFilePath);
			Init.initConfigFile(MainClass.configFilePath, "Config");
			
			TestListenerAdapter tla = new TestListenerAdapter();
		    TestNG testng = new TestNG();
		    List<String> suites = Lists.newArrayList();
		    
			if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
				String suiteXmlPATH = System.getProperty("user.dir")+"\\testngTT.xml";
				suites.add(suiteXmlPATH);
			}else {
				String suiteXmlPATH = System.getProperty("user.dir")+"\\testng.xml";
				suites.add(suiteXmlPATH);
			}
		    
		    testng.setTestSuites(suites);
		    
		    try {
		    testng.run();
		    }catch(Exception e) {
		    	log.error(e.getMessage());
		    }
		    finally {
		    	if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
		    		
		    		TimetravelTestCase.generateTimeTravelExtent();
		    		
		    	}
		    }
		    System.exit(0);
	}
}
