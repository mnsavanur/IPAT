package com.lti.controller;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.maven.surefire.shared.utils.StringUtils;
import org.apache.poi.xddf.usermodel.chart.Shape;
import org.testng.Assert;

import com.lti.TestUtil.Report;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.webDriver.WebDriverHelper;
public class CustomAssert extends Assert{
	 public static void fail(String message) {
		 
		 if(StringUtils.equalsIgnoreCase(Config.userInteraction, "on")){
			 if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) 
				message=Report.sysDate + " : "+ Report.getTestCaseId() +" : " + Report.getTransactionName() + " : "+ Report.getFieldName() + "\n\n" + message;
			 else
				message=Report.getTestCaseId() +" : " + Report.getTransactionName() + " : "+ Report.getFieldName() + "\n\n" + message;
			 
			 int response = Controller.displayJFrame("IPAT : Click Yes to continue even after below failure(s)", message,  JOptionPane.YES_NO_OPTION);
			 if(response==JOptionPane.NO_OPTION) {
				 if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
					 if(!Base.timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
							Base.timetravelFailedTestCaseList.add(Report.getTestCaseId());
				 }else
					 throw new AssertionError(message);
			 }
		 }else {
			 if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
				 if(!Base.timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
						Base.timetravelFailedTestCaseList.add(Report.getTestCaseId());
			 }
			 else
				 throw new AssertionError(message);
		 }
	 }
	 
	 public static void fail(String message, Throwable realCause) {
		 AssertionError ae = new AssertionError(message);
		    ae.initCause(realCause);
		    
		 if(StringUtils.equalsIgnoreCase(Config.userInteraction, "on")){
			 if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel"))
				 message=Report.sysDate + " : "+ Report.getTestCaseId() +" : " + Report.getTransactionName() + " : "+ Report.getFieldName() + "\n\n" + message;
			 else
				 message=Report.getTestCaseId() +" : " + Report.getTransactionName() + " : "+ Report.getFieldName() + "\n\n" + message;
			 
			int response = Controller.displayJFrame("IPAT : Click Yes to continue even after below failure(s)", message,  JOptionPane.YES_NO_OPTION);
			if(response==JOptionPane.NO_OPTION) {
				 if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
					 if(!Base.timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
							Base.timetravelFailedTestCaseList.add(Report.getTestCaseId());
				 }else
					 throw ae;
			}
		 }else {
			 if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel")) {
				 if(!Base.timetravelFailedTestCaseList.contains(Report.getTestCaseId()))
						Base.timetravelFailedTestCaseList.add(Report.getTestCaseId());
			 }
			 else
				 throw ae;
		 }
	 }
	 

}
