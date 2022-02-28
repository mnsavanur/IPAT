package com.lti.webDriver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.maven.surefire.shared.utils.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.TestUtil.Calculations;
import com.lti.TestUtil.MongoDB;
import com.lti.TestUtil.Report;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.controller.Controller;
import com.lti.controller.CustomAssert;

public class Assertions extends Base {
	static Logger log = Logger.getLogger(Assertions.class.getName());
	public static String actualResult=null;
	public static String actualValue=null;
	public static WebElement assertElement=null;
	
	public static String getElementValue(WebElement element) {
		actualResult="";
		actualResult = element.getText();
		if(element.getText().equals(null)|| element.getText().equals("")) {
			actualResult = element.getAttribute("value");
		}
		if(!StringUtils.equals(actualResult, null))
			actualResult=actualResult.trim();
		return actualResult;
	}
	public static void validateNotEqual(WebElement element,String inputValue, String fieldName, String action)throws Exception {
		String[] inputSplit = inputValue.split("\\|");
		String msg="", msgForExtent="", status="";
		if (inputSplit.length <= 1) {
			msg="Incorrect input with action 'VNE'. Input should be in the format of 'Value1ToComare|Value2ToCompare'";
			log.info(msg);
			Assertions.writeResults("fail", msg, msg, false,"soft");
			return;
		}
		String value1=inputSplit[0];
		String value2=inputSplit[1];
	
		if(value1.equalsIgnoreCase("_actualValue")) {
			value1 = getElementValue(element);
		}
		if(value2.equalsIgnoreCase("_actualValue")) {
			value2 = getElementValue(element);
		}
		if(value1.equals(value2)) {
			status="fail";
			msg="Values are equal, but expected NOT to be equal. Value 1 ='"+ value1+"', Value 2 ='"+ value2+"'";
			msgForExtent="Values are equal, but expected NOT to be equal. <b>Value 1:</b>"+ value1+ ", <b>Value 2:</b> "+ value2;
			Assertions.writeResults("fail", msg, msg, true,"soft");
		}else {
			status="pass";
			msg="Values are not equal, as expected. Value 1 ='"+ value1+"', Value 2 ='"+ value2+"'";
			msgForExtent="Values are not equal, as expected. <b>Value 1:</b>"+ value1+ ", <b>Value 2:</b> "+ value2;
			Assertions.writeResults("pass", msg, msgForExtent, false,"");
		}
	}
	
	public static void validateLabel(WebElement element,String inputValue, String fieldName, String action)throws IOException {
		actualValue = getElementValue(element);
		if(action.equalsIgnoreCase("VRF")) {
			try {
				if(!StringUtils.equals(actualValue, null))
					actualValue=Calculations.removeFormattingFromNumber(actualValue);
			}catch(Exception e) {}
		}
		if(inputValue.equalsIgnoreCase("_Blank")) {
			inputValue="";
			if(StringUtils.equals(actualValue, null))
				actualValue="";
			if(actualValue.equals(inputValue)) {
				String msg="Actual value is blank as expected.";
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msg ="Mismatch in expected and actual. Expected: Blank, Actual: "+ actualValue;
				String msgForExtent="Mismatch in expected and actual. <b>Expected:</b> Blank, <b>Actual:</b> "+ actualValue;
				log.info(msg);
				writeResults("fail", msg, msgForExtent, true,"soft");
			}
		}else if(inputValue.equalsIgnoreCase("_NotBlank")) {
			inputValue="";
			if(StringUtils.equals(actualValue, null))
				actualValue="";
			if(!actualValue.equals("")) {
				String msg="Actual value is not blank as expected. Actual Value: "+ actualValue;
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msg ="Actual Value is Blank and expected not to be blank.";
				log.info(msg);
				writeResults("fail", msg, msg, true,"soft");
			}
		}else {
			if(action.equalsIgnoreCase("VP") && actualValue.contains(inputValue)) {
				String msg="Partial verification Successful. Expected Value: " + inputValue + " Actual Value:  " + actualValue;
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else if(!action.equalsIgnoreCase("VP") && actualValue.equals(inputValue)) {
				String msg="Expected Value: " + inputValue + " Actual Value:  " + actualValue;
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msgForExtent="Validation failed. Mismatch between expected and actual value. <b>Actual Value: </b>" + actualValue + ",<b> Expected value: </b>"+inputValue;
				String msg="Validation failed. Mismatch between expected and actual value. Actual Value: " + actualValue + ", Expected value: "+inputValue;
				log.info(msg);
				writeResults("fail", msg, msgForExtent, true,"soft");
			}
		}
	}
	
	//use validateFormats instead to verify one or more formats
	public static void validateFormat(WebElement element,String inputValue, String fieldName)throws IOException {
		//String[] formatSpec = inputValue.split("\\|");
		String[] formatSpec = new String[2];
		int indexOfSeparator=inputValue.indexOf("|");
		String msg="", msgForExtent="", status="";
		//if (formatSpec.length != 2) {
		if(indexOfSeparator==-1) {
			msg="Incorrect input for Format Verification. Input should be in the format of 'X|DC-WCXXXXX-01', where first letter specifies dynamic value.";
			log.info(msg);
			Assertions.writeResults("fail", msg, msg, false,"soft");
			return;
		}
		//String dynamic = formatSpec[0];
		//inputValue = formatSpec[1];
		String dynamic = inputValue.substring(0, indexOfSeparator);
		inputValue = inputValue.substring(indexOfSeparator+1, inputValue.length());
		
		String actualValue= element.getAttribute("value");
		if (StringUtils.equalsIgnoreCase(actualValue, null) || actualValue.equalsIgnoreCase("")) {
			actualValue = element.getText();
			if (StringUtils.equalsIgnoreCase(actualValue, null)) {
				actualValue = "";
			}
		}
		if (actualValue.length() == inputValue.length()) {
			Boolean matched = true;
			for (int j = 0; j < inputValue.length(); j++) {
				String curChar = Character.toString(actualValue.charAt(j));
				String expChar = Character.toString(inputValue.charAt(j));
				if (!expChar.equalsIgnoreCase(dynamic) && !expChar.equals(curChar)) {
					status="fail";
					msg="Format mismatch. Expected Format: "+inputValue+", Actual Value: "+actualValue;
					msgForExtent="Format mismatch. <b>Expected Format:</b> "+inputValue+", <b>Actual Value:</b> "+actualValue;
					matched = false;
					break;
				}
			}
			if (matched) {
				status="pass";
				msg="Format matched. Expected Format: "+inputValue+", Actual Value: "+actualValue;
				msgForExtent=msg;
			}
		} else {
			status="fail";
			msg="Format mismatch. Character count differs. Expected Format: "+inputValue+", Actual Value: "+actualValue;
			msgForExtent="Format mismatch. Character count differs. <b>Expected Format:</b> "+inputValue+", <b>Actual Value:</b> "+actualValue;
		}
		log.info(msg);
		if(status.equalsIgnoreCase("pass"))
			Assertions.writeResults("pass", msg, msgForExtent, false,"");
		else
			Assertions.writeResults("fail", msg, msgForExtent, true,"soft");
	}
	
	public static void validateFormats(WebElement element,String inputValue, String fieldName)throws Exception {
	String[] formatSpec = inputValue.split(";;");
	String msg="", msgForExtent="", status="";
	if (formatSpec.length <= 1) {
		msg="Incorrect input for Format Verification. Input should be in the format of 'X;;DC-WCXXXXX-01' OR 'X;;DC-WCXXXXX-01;;DC-WCXX-01', where first letter specifies dynamic value.";
		log.info(msg);
		Assertions.writeResults("fail", msg, msg, false,"soft");
		return;
	}
	String dynamic = formatSpec[0].trim();
	String allFormats= inputValue.substring(inputValue.indexOf(";;")+2, inputValue.length());
	Boolean matchFound=false;
	actualValue="";
	actualValue=getElementValue(element);
	if (StringUtils.equalsIgnoreCase(actualValue, null)) {
		actualValue = "";
	}
	for(int i=1; i<=formatSpec.length-1;i++) {
		inputValue=formatSpec[i].trim();
		if (actualValue.length() == inputValue.length()) {
			Boolean matched = true;
			for (int j = 0; j < inputValue.length(); j++) {
				String curChar = Character.toString(actualValue.charAt(j));
				String expChar = Character.toString(inputValue.charAt(j));
				if (!expChar.equalsIgnoreCase(dynamic) && !expChar.equals(curChar)) {
					matched = false;
					break;
				}
			}
			if (matched) {
				matchFound=true;
				break;
			}
		}
	}
	if (matchFound) {
		status="pass";
		msg="Format matched. Expected Format(s): "+allFormats+", Actual Value: "+actualValue;
		msgForExtent=msg;
	} else {
		status="fail";
		msg="Format mismatch. Expected Format(s): "+allFormats+", Actual Value: "+actualValue;
		msgForExtent="Format mismatch.<b>Expected Format(s):</b> "+allFormats+", <b>Actual Value:</b> "+actualValue;
	}
	log.info(msg);
	if(status.equalsIgnoreCase("pass"))
		Assertions.writeResults("pass", msg, msgForExtent, false,"");
	else
		Assertions.writeResults("fail", msg, msgForExtent, true,"soft");
}
	
	public static void validateTextBox(String locator,String inputValue, String fieldName, String action)throws Exception {
		if(action.equalsIgnoreCase("I")||action.equalsIgnoreCase("F")) {
			WebElement assertElement = UiHandler.getElement(locator,fieldName,inputValue, true);
			UiHandler.unHighilightElement(assertElement);
			actualValue = getElementValue(assertElement);
			if(actualValue.equals(inputValue)) {
				String msg="Successfully Entered '" + inputValue + "' as value in the textbox.";
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msgForExtent="Input value is not matching with value present in field. <b>Expected:</b> " + inputValue + ",<b> Actual: </b>"+actualValue;
				String msg="Input value is not matching with value present in field. Expected: " + inputValue + ", Actual: "+actualValue;
				log.info(msg);
				writeResults("fail", msg, msgForExtent, true,"soft");
			}
		}else if(action.equalsIgnoreCase("V")) {
			String status="", msg="", msgForExtent="";
			if(inputValue.equalsIgnoreCase("_Present") || inputValue.equalsIgnoreCase("_NotPresent") ){
				String result[]=validatePresenceOfField(locator, inputValue, fieldName);
				status=result[0];
				msg=result[1];
				msgForExtent=msg;
			}else {
				WebElement assertElement = UiHandler.getElement(locator,fieldName,inputValue, true);
				actualValue = getElementValue(assertElement);
				if(inputValue.equalsIgnoreCase("_Blank")) {
					inputValue="";
					if(actualValue.equals(inputValue)) {
						msg="Actual value is blank as expected.";
						msgForExtent=msg;
						status="pass";
					}else {
						msg ="Mismatch in expected and actual. Expected: Blank, Actual: "+ actualValue;
						msgForExtent="Mismatch in expected and actual. <b>Expected:</b> Blank, <b>Actual:</b> "+ actualValue;
						status="fail";
					}
				}else {
					if(actualValue.equals(inputValue)) {
						msg="Actual and expected values matched : " +actualValue;
						msgForExtent=msg;
						status="pass";
					}else {
						msg ="Mismatch in expected and actual. Expected: "+ inputValue +", Actual: "+ actualValue;
						msgForExtent="Mismatch in expected and actual. <b>Expected:</b>"+ inputValue +", <b>Actual:</b> "+ actualValue;
						status="fail";
					}
				}
			}
			log.info(msg);
			if(status.equalsIgnoreCase("pass"))
				writeResults("pass", msg, msgForExtent, false,"");
			else
				writeResults("fail", msg, msgForExtent, true,"soft");
		}
	}
	public static void validateMaskedTextBox(String locator,String inputValue, String fieldName, String action)throws Exception {
		if(action.equalsIgnoreCase("I")||action.equalsIgnoreCase("F")) {
			try {
				String[] splitInputandVerification=inputValue.split("\\|");
				WebElement assertElement = UiHandler.getElement(locator,fieldName,inputValue, true);
				UiHandler.unHighilightElement(assertElement);
				actualValue = getElementValue(assertElement);
				if(actualValue.equals(splitInputandVerification[1])) {
					String msg="Successfully Entered " + splitInputandVerification[0] + " and it's masked as "+ splitInputandVerification[1] + " in the textbox.";
					log.info(msg);
					writeResults("pass", msg, msg, false,"");
				}else {
					String msgForExtent="Expected input value is not matching with value present in field. <b>Expected:</b> " + splitInputandVerification[1] + ",<b> Actual: </b>"+actualValue;
					String msg="Expected input value is not matching with value present in field. Expected: " + splitInputandVerification[1] + ", Actual: "+actualValue;
					log.info(msg);
					writeResults("fail", msg, msgForExtent, true,"soft");
				}
			}catch(ArrayIndexOutOfBoundsException e) {
				String msg="Incorrect input for Masked Text Box. Input should be in the format of 'Input|Verificaton', example '123-456|*******'.";
				log.info(msg);
				writeResults("fail", msg, msg, false,"hard");
			}
		}else if(action.equalsIgnoreCase("V")) {
			String status="", msg="", msgForExtent="";
			if(inputValue.equalsIgnoreCase("_Present") || inputValue.equalsIgnoreCase("_NotPresent") ){
				String result[]=validatePresenceOfField(locator, inputValue, fieldName);
				status=result[0];
				msg=result[1];
				msgForExtent=msg;
			}else {
				WebElement assertElement = UiHandler.getElement(locator,fieldName,inputValue, true);
				actualValue = getElementValue(assertElement);
				if(inputValue.equalsIgnoreCase("_Blank")) {
					inputValue="";
					if(actualValue.equals(inputValue)) {
						msg="Actual value is blank as expected.";
						msgForExtent=msg;
						status="pass";
					}else {
						msg ="Mismatch in expected and actual. Expected: Blank, Actual: "+ actualValue;
						msgForExtent="Mismatch in expected and actual. <b>Expected:</b> Blank, <b>Actual:</b> "+ actualValue;
						status="fail";
					}
				}else {
					if(actualValue.equals(inputValue)) {
						msg="Actual and expected values matched : " +actualValue;
						msgForExtent=msg;
						status="pass";
					}else {
						msg ="Mismatch in expected and actual. Expected: "+ inputValue +", Actual: "+ actualValue;
						msgForExtent="Mismatch in expected and actual. <b>Expected:</b>"+ inputValue +", <b>Actual:</b> "+ actualValue;
						status="fail";
					}
				}
			}
			log.info(msg);
			if(status.equalsIgnoreCase("pass"))
				writeResults("pass", msg, msgForExtent, false,"");
			else
				writeResults("fail", msg, msgForExtent, true,"soft");
		}
	}
	public static void validateAlert(String inputValue, String fieldName)throws IOException {
		actualValue = WebDriverHelper.iDriver.switchTo().alert().getText();
		if(actualValue.equals(inputValue)) {
			String msg="Expected & Actual Alert message : " + inputValue;
			log.info(msg);
			writeResults("pass", msg, msg, false,"");
		}else {
			String msgForExtent="Alert message is not matching with the expected message. <b>Expected: </b>" + inputValue + "<b>Actual: </b>"+actualValue;;
			String msg="Alert message is not matching with the expected message. Expected: " + inputValue + "Actual: "+actualValue;
			log.info(msg);
			writeResults("fail", msg, msgForExtent, true,"soft");
		}
	}
	
	public static void validateLink(WebElement element,String inputValue, String fieldName) throws IOException {
		actualValue = getElementValue(element);
		if(actualValue.equals(inputValue)) {
			String msg="Field values matched. Actual: "+actualValue+", Expected: "+ inputValue;
			log.info(msg);
			writeResults("pass", msg, msg, false,"");
		}else {
			String msgForExtent="Field values mismatched. <b>Actual:</b> "+actualValue+",<b> Expected: </b>"+ inputValue;
			String msg="Field values mismatched. Actual: "+actualValue+", Expected: "+ inputValue;
			log.info(msg);
			writeResults("fail", msg, msgForExtent, true,"soft");
		}
	}
	
	public static void validateDropdown(String locator, String inputValue, String fieldName, String action) throws Exception 
	{
		WebElement element = UiHandler.getElement(locator,fieldName,inputValue, true);
		actualValue = getElementValue(element);
		actualValue = actualValue.substring(1);
		
		Select drpDown = new Select(element);
		WebElement value=drpDown.getFirstSelectedOption();
		actualValue = value.getText();
		if (action.equalsIgnoreCase("I")) {
			if (actualValue.equalsIgnoreCase(inputValue)) {
				String msg="Successfully selected " + inputValue + " as value from the "+fieldName+" dropdown";
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			} else {
				String msg="Unable to Select " + inputValue + " as value from the "+fieldName+" dropdown";
				log.info(msg);
				writeResults("fail", msg, msg, true,"soft");
			}
		}
	}
	public static void validateDropdownValues(String locator, String inputValue, String fieldName, String action) throws IOException 
	{
		//Retrieve actual dropdown values
		ArrayList<WebElement> actualDropDownWE = (ArrayList<WebElement>) WebDriverHelper.iDriver.findElements(By.xpath(locator));
		List<String> actualDropDownValues = new ArrayList<String>();
		
		ListIterator<WebElement> weIterator = actualDropDownWE.listIterator(); 
		while(weIterator.hasNext()){
			WebElement tempWE=weIterator.next();
			String val=tempWE.getText();
			if(val==null || val.isEmpty()) {
				val=tempWE.getAttribute("value");
				if(val==null) {
					val="blank";
				}else if(val.equals("")) {
					val="blank";
				}
				/*
				 * if(StringUtils.equals(val,"")) val="_BLANK";
				 */
			}
			actualDropDownValues.add(val);
		}
		
		List<String> expectedDropDownValues = new ArrayList<String>();
		
		String[] expUserInput = inputValue.trim().split("\\|");
		for(int k=0;k<expUserInput.length;k++)
			expectedDropDownValues.add(expUserInput[k].trim());
		
		List<String> missingDPValues = new ArrayList<String>();
		List<String> additionalDPValues = new ArrayList<String>();
		
		for(int i=0;i<expectedDropDownValues.size();i++) {
			if(!actualDropDownValues.contains(expectedDropDownValues.get(i)))
				missingDPValues.add(expectedDropDownValues.get(i));
		}
		for(int i=0;i<actualDropDownValues.size();i++) {
			if(!expectedDropDownValues.contains(actualDropDownValues.get(i)))
				additionalDPValues.add(actualDropDownValues.get(i));
		}
		String message="";
		Boolean countMatch = (expectedDropDownValues.size()==actualDropDownValues.size())?true:false;
		if(missingDPValues.size()>0 || additionalDPValues.size()>0 || !countMatch)
		{
			message= "Dropdown values mismatched. Expected Count=" + expectedDropDownValues.size() + " | Actual Count="+actualDropDownValues.size()+".";
			if(missingDPValues.size()>0){
				message = message + "\nMissing Values=";
				for(int i=0;i<missingDPValues.size();i++) {
					message=message+missingDPValues.get(i)+", ";
				}
				if(message.endsWith(", "))
					message=message.substring(0, message.length()-2);
				message=message+".";
			}
			
			if(additionalDPValues.size()>0){
				message = message + "\nAdditional Values=";
				for(int i=0;i<additionalDPValues.size();i++) {
					message=message+additionalDPValues.get(i)+", ";
				}
				if(message.endsWith(", "))
					message=message.substring(0, message.length()-2);
				message=message+".";
			}
			
			String messageForExtent= message;
			messageForExtent=messageForExtent.replaceAll("<", "&lt;");
			messageForExtent=messageForExtent.replaceAll(">", "&gt;");
			messageForExtent=messageForExtent.replaceAll("\n", "<br>");

			log.info(message);
			writeResults("fail", message, messageForExtent, true,"soft");
		}else {
			message= "Dropdown values matched. Expected & Actual values count=" + expectedDropDownValues.size() +".\n";
			message = message + "Expected & Actual values= ";
			for(int i=0;i<actualDropDownValues.size();i++) {
				message=message+actualDropDownValues.get(i)+", ";
			}
			if(message.endsWith(", "))
				message=message.substring(0, message.length()-2);
			
			
			String messageForExtent= message;
			messageForExtent=messageForExtent.replaceAll("<", "&lt;");
			messageForExtent=messageForExtent.replaceAll(">", "&gt;");
			messageForExtent=messageForExtent.replaceAll("\n", "<br>");
			
			log.info(message);
			writeResults("pass", message, messageForExtent, false,"");
		}
		
	}
	
	public static void validateBrowserWindow(String inputValue, String fieldName, String locator)throws Exception {
			int countOfMatchedWindows=0;
			Set<String> handlers = null;
			String Parent_window = WebDriverHelper.iDriver.getWindowHandle();
			locator=UiHandler.evaluateLocator(locator,fieldName,inputValue);
			handlers = WebDriverHelper.iDriver.getWindowHandles();
			for (String handler : handlers) {
				WebDriverHelper.iDriver = WebDriverHelper.iDriver.switchTo().window(handler);
				if (WebDriverHelper.iDriver.getTitle().contains(locator)) {
					countOfMatchedWindows=countOfMatchedWindows+1;
				}
			}
			
			WebDriverHelper.iDriver.switchTo().window(Parent_window);
			String msg="", status="";
			if(inputValue.equalsIgnoreCase("_Present")) {
				if(countOfMatchedWindows>0) {
					msg=countOfMatchedWindows + " browser window(s) with text as '"+ locator +"' in the title exist(s), as expected.";
					status="pass";
				}else {
					msg="No browser window(s) with text as '"+ locator +"' in the title exist(s), NOT as expected.";
					status="fail";
				}
			}else if(inputValue.equalsIgnoreCase("_NotPresent")) {
				if(countOfMatchedWindows>0) {
					msg=countOfMatchedWindows + " browser window(s) with text as '"+ locator +"' in the title exist(s), NOT as expected.";
					status="fail";
				}else {
					msg="No browser window(s) with text as '"+ locator +"' in the title exist(s), as expected.";
					status="pass";
				}
			}
		log.info(msg);
		if(status.equalsIgnoreCase("pass"))
			writeResults("pass", msg, msg, false,"");
		else
			writeResults("fail", msg, msg, true,"soft");
		
	}
	public static void validateRadioButton(WebElement element, String inputValue, String fieldName)throws IOException {
		String msg="";
		if(StringUtils.equalsIgnoreCase(Config.platform, "Duck Creek")) {
			String radioStatus = element.getAttribute("data-value");
			if(radioStatus==null) {
				radioStatus = element.getAttribute("data-dcchecked");
				if(StringUtils.equalsIgnoreCase(radioStatus, "true"))
					radioStatus="1";
				else if(StringUtils.equalsIgnoreCase(radioStatus, "false"))
					radioStatus="0";
			}
			if(StringUtils.equalsIgnoreCase(radioStatus, "1"))
				actualValue="check";
			else
				actualValue="uncheck";
		}	
		if(StringUtils.equalsIgnoreCase(Config.platform, "Guidewire")) {
			String radioStatus = element.getAttribute("value");
			if(StringUtils.equalsIgnoreCase(radioStatus, "true"))
				actualValue="check";
			else
				actualValue="uncheck";
		}
			
			if(inputValue.equalsIgnoreCase("check")){
				if(actualValue.equalsIgnoreCase(inputValue)) {
					msg="Radio button '" + fieldName+"' is selected as expected.";
					log.info(msg);
					writeResults("pass", msg, msg, false,"");
				}else {
					msg="Radio button '"+fieldName+"' is not selected but expected to be selected.";
					log.info(msg);
					writeResults("fail", msg, msg, true,"soft");
				}
			}
			else if(inputValue.equalsIgnoreCase("uncheck")){
				if(actualValue.equalsIgnoreCase(inputValue)) {
					msg="Radio button '" + fieldName+"' is not selected as expected.";
					log.info(msg);
					writeResults("pass", msg, msg, false,"");
				}else {
					msg="Radio button '"+fieldName+"' is selected but expected NOT to be selected.";
					log.info(msg);
					writeResults("fail", msg, msg, true,"soft");
				}
			}else {
				msg="Incorrect Input: Input for verification of Radio button '"+ fieldName + "'should be 'Check' or 'Uncheck'";
				log.info(msg);
				writeResults("fail", msg, msg, true,"soft");
			}
			actualValue="";
	}
	
	public static String[] validatePresenceOfField(String locator, String inputValue, String fieldName)throws Exception {
		String status="", msg="";
		String[] resString=new String[2];
		
		locator=UiHandler.evaluateLocator(locator,fieldName,inputValue);
		WebElement assertElement=null;
		WebDriverWait wait = new WebDriverWait(WebDriverHelper.iDriver,1);
		
		try {
			if (locator.startsWith("//") || locator.startsWith("(//")) {
				assertElement=	wait.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));
			}else {
				assertElement=	wait.until(ExpectedConditions.elementToBeClickable(By.id(locator)));
			}
		}catch(Exception e) {
			assertElement=null;
		}
		
		if(inputValue.equalsIgnoreCase("_Present")) {
			if(assertElement==null) {
				msg="Field is NOT present on the page but expected to be present. Locator : "+ locator;
				status="fail";
			}else {
				msg="Field is present on the page as expected. Locator : "+ locator;
				status="pass";
			}
		}else if(inputValue.equalsIgnoreCase("_NotPresent")) {
			if(assertElement==null) {
				msg="Field is NOT present on the page as expected. Locator : "+ locator;
				status="pass";
			}else {
				msg="Field is present on the page but NOT expected to be present. Locator : "+ locator;
				status="fail";
			}
		}
		resString[0]= status;
		resString[1]=msg;
		return resString;
	}
	public static String[] validateAccessibilityOfElement(String locator, String inputValue, String fieldName)throws Exception {

		String status="", msg="";
		String[] resString=new String[2];
		
		locator=UiHandler.evaluateLocator(locator,fieldName,inputValue);
		WebElement assertElement=null;
		WebDriverWait wait = new WebDriverWait(WebDriverHelper.iDriver,1);
		
		try {
			if (locator.startsWith("//") || locator.startsWith("(//")) {
				assertElement=	wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
			}else {
				assertElement=	wait.until(ExpectedConditions.presenceOfElementLocated(By.id(locator)));
			}
		}catch(Exception e) {
			assertElement=null;
		}
		if(assertElement!=null) {
			String disableStatus = assertElement.getAttribute("disabled");
	
			if(StringUtils.equalsIgnoreCase(disableStatus, "true")) {
				if(inputValue.equalsIgnoreCase("_disabled")) {
					msg="Field is disabled as expected.";
					status="pass";
				}else if(inputValue.equalsIgnoreCase("_enabled")){
					msg="Field is disabled but expected to be enabled\\accessible.";
					status="fail";
				}
			}else {
				if(inputValue.equalsIgnoreCase("_disabled")) {
					msg="Field is enabled but expected to be disabled.";
					status="fail";
				}else if(inputValue.equalsIgnoreCase("_enabled")){
					msg="Field is enabled as expected.";
					status="pass";
				}
			}
		}else {
			msg="Field is NOT present on screen,so could not test accessibility of the field. Expected accessibility: "+inputValue;
			status="fail";
		}
		resString[0]= status;
		resString[1]=msg;
		return resString;
	}
	
	//for readOnly property
	public static String[] validateAccessibilityOfElement2(String locator, String inputValue, String fieldName)throws Exception {

		String status="", msg="";
		String[] resString=new String[2];
		
		locator=UiHandler.evaluateLocator(locator,fieldName,inputValue);
		WebElement assertElement=null;
		WebDriverWait wait = new WebDriverWait(WebDriverHelper.iDriver,1);
		
		try {
			if (locator.startsWith("//") || locator.startsWith("(//")) {
				assertElement=	wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
			}else {
				assertElement=	wait.until(ExpectedConditions.presenceOfElementLocated(By.id(locator)));
			}
		}catch(Exception e) {
			assertElement=null;
		}
		if(assertElement!=null) {
			String readOnlyStatus = assertElement.getAttribute("readonly");
	
			if(StringUtils.equalsIgnoreCase(readOnlyStatus, "true")) {
				if(inputValue.equalsIgnoreCase("_readOnly")) {
					msg="Field is read only as expected.";
					status="pass";
				}else if(inputValue.equalsIgnoreCase("_notreadOnly")){
					msg="Field is read only but expected to be editable.";
					status="fail";
				}
			}else {
				if(inputValue.equalsIgnoreCase("_readonly")) {
					msg="Field is editable but expected to be read only.";
					status="fail";
				}else if(inputValue.equalsIgnoreCase("_notreadonly")){
					msg="Field is editable as expected.";
					status="pass";
				}
			}
		}else {
			msg="Field is NOT present on screen,so could not test accessibility of the field. Expected accessibility: "+inputValue;
			status="fail";
		}
		resString[0]= status;
		resString[1]=msg;
		return resString;
	}
	
	
	//for required property
	public static String[] validateNecessityOfElement(String locator, String inputValue, String fieldName)throws Exception {
		String status="", msg="";
		String[] resString=new String[2];
		
		locator=UiHandler.evaluateLocator(locator,fieldName,inputValue);
		WebElement assertElement=null;
		WebDriverWait wait = new WebDriverWait(WebDriverHelper.iDriver,1);
		
		try {
			if (locator.startsWith("//") || locator.startsWith("(//")) {
				assertElement=	wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
			}else {
				assertElement=	wait.until(ExpectedConditions.presenceOfElementLocated(By.id(locator)));
			}
		}catch(Exception e) {
			assertElement=null;
		}
		if(assertElement!=null) {
			String requiredStatus = assertElement.getAttribute("required");
	
			if(StringUtils.equalsIgnoreCase(requiredStatus, "true")) {
				if(inputValue.equalsIgnoreCase("_mandatory")) {
					msg="Field is mandatory as expected.";
					status="pass";
				}else if(inputValue.equalsIgnoreCase("_optional")){
					msg="Field is mandatory but expected to be optional.";
					status="fail";
				}
			}else {
				if(inputValue.equalsIgnoreCase("_mandatory")) {
					msg="Field is optional but expected to be mandatory.";
					status="fail";
				}else if(inputValue.equalsIgnoreCase("_optional")){
					msg="Field is optional as expected.";
					status="pass";
				}
			}
		}else {
			msg="Field is NOT present on screen,so could not test necessity of the field. Expected necessity: "+inputValue;
			status="fail";
		}
		resString[0]= status;
		resString[1]=msg;
		return resString;
	}
	
	public static void validateCheckbox(String locator, String inputValue, String fieldName, String action)throws Exception {
		if(action.equalsIgnoreCase("V")) {
			String status="", msg="", msgForExtent="";
			
			if(inputValue.equalsIgnoreCase("_Present") || inputValue.equalsIgnoreCase("_NotPresent") ){
				String result[]=validatePresenceOfField(locator, inputValue, fieldName);
				status=result[0];
				msg=result[1];
				msgForExtent=msg;
			}else {
				WebElement assertElement = UiHandler.getElement(locator,fieldName,inputValue, true);
				String checkboxStatus = assertElement.getAttribute("checked");
				if(StringUtils.equalsIgnoreCase(checkboxStatus, "true"))
					actualValue="check";
				else
					actualValue="uncheck";
				
				if(inputValue.equalsIgnoreCase("check")){
					if(actualValue.equalsIgnoreCase(inputValue)) {
						msg="Checkbox '"+ fieldName+ "'is checked as expected.";
						msgForExtent=msg;
						status="pass";
					}else {
						msg="Checkbox '"+fieldName+"' is not checked, but expected to be checked.";
						msgForExtent=msg;
						status="fail";
					}
				}
				else if(inputValue.equalsIgnoreCase("uncheck")){
					if(actualValue.equalsIgnoreCase(inputValue)) {
						msg="Checkbox '"+ fieldName+ "'is NOT checked as expected.";
						msgForExtent=msg;
						status="pass";
					}else {
						msg="Checkbox '"+fieldName+"' is checked, but expected NOT to be checked.";
						msgForExtent=msg;
						status="fail";
					}
				}else {
					msg="Incorrect Input: Input for Checkbox '"+ fieldName + "'should be 'Check' or 'Uncheck'";
					msgForExtent=msg;
					status="fail";
				}
				actualValue="";
			}
			log.info(msg);
			if(status.equalsIgnoreCase("pass"))
				writeResults("pass", msg, msgForExtent, false,"");
			else
				writeResults("fail", msg, msgForExtent, true,"soft");
		}
	}
	
	public static void validateDate(String locator, String inputValue, String fieldName, String action) throws Exception {
		assertElement = UiHandler.getElement(locator,fieldName,inputValue, true);
		UiHandler.unHighilightElement(assertElement);
		actualValue = getElementValue(assertElement);
		
		if(action.equals("I")) {
			if(actualValue.equalsIgnoreCase(inputValue)) {
				String msg="Successfully entered date as " + inputValue + " as value for " + fieldName;
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msg="Unable to enter date as '" + inputValue+"'";
				log.info(msg);
				writeResults("fail", msg, msg, true,"soft");
			}
		}else if(action.equals("V")){
			if(actualValue.equalsIgnoreCase(inputValue)) {
				String msg="Validation for field " + fieldName + " Passed. Actual Value: "+actualValue+", Expected Value: "+inputValue;
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msg="Validation Failure. Actual: "+actualValue+", Expected: "+ inputValue;
				String msgForExtent="Validation Failure. <b>Actual:</b> "+actualValue+",<b> Expected: </b>"+ inputValue;
				log.info(msg);
				writeResults("fail", msg, msgForExtent, true,"soft");
			}
		}
	}
	
	public static void validateIDropDown(String locator, String inputValue, String fieldName, String action)throws Exception {
		Thread.sleep(100);
		WebDriver.handleLoader();
		WebElement element = UiHandler.getElement(locator,fieldName,inputValue, true);
		actualValue = getElementValue(element);
		actualValue=StringUtils.trim(actualValue);
		if(action.equalsIgnoreCase("I")) {
			if(actualValue.equalsIgnoreCase(inputValue)) {
				String msg="Selected '" + inputValue + "' as value in " + fieldName;
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msg="Unable to select '" + inputValue + "' as value in " + fieldName + ". Current Selected value is: "+actualValue;
				log.info(msg);
				writeResults("fail", msg, msg, true,"hard");
			}
		}else if(action.equalsIgnoreCase("P")) {
			if(actualValue.contains(inputValue)) {
				String msg="Selected '" + inputValue + "' as value in " + fieldName;
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msg="Unable to select '" + inputValue + "' as value in " + fieldName;
				log.info(msg);
				writeResults("fail", msg, msg, true,"hard");
			}
		}else if(action.equalsIgnoreCase("V")) {
			if(actualValue.equalsIgnoreCase(inputValue)) {
				String msg=inputValue + " is displayed as selected in " + fieldName + " dropdown.";
				log.info(msg);
				writeResults("pass", msg, msg, false,"");
			}else {
				String msg="'" + actualValue + "' is displayed as selected in " + fieldName + " dropdown. Expected entry: " + inputValue;
				log.info(msg);
				writeResults("fail", msg, msg, true,"soft");
			}
		}
	}
	
	
	public static void validateTooltip(String locator, String inputValue, String fieldName, String action)throws Exception {
		String status="", msg="", msgForExtent="";
		WebElement assertElement = UiHandler.getElement(locator,fieldName,inputValue, true);
		//data-qtip is tooltip property in DC product
		actualValue = assertElement.getAttribute("data-qtip");
		if(actualValue.equals(inputValue)) {
			msg="Actual and expected tooltip values matched : " +actualValue;
			msgForExtent=msg;
			status="pass";
		}else {
			msg ="Mismatch in expected and actual tooltip. Expected: "+ inputValue +", Actual: "+ actualValue;
			msgForExtent="Mismatch in expected and actual tooltip. <b>Expected:</b>"+ inputValue +", <b>Actual:</b> "+ actualValue;
			status="fail";
		}
		log.info(msg);
		if(status.equalsIgnoreCase("pass"))
			writeResults("pass", msg, msgForExtent, false,"");
		else
			writeResults("fail", msg, msgForExtent, true,"soft");
	}
	
	public static void validateEquality(String expectedValue, String actualValue)throws Exception {
		String msg="", msgForExtent="", status="";
		if(expectedValue.equalsIgnoreCase("_Blank")) {
			expectedValue="";
			if(StringUtils.equals(actualValue, null))
				actualValue="";
			if(actualValue.equals(expectedValue)) {
				msg="Actual value is blank as expected.";
				msgForExtent=msg;
				status="pass";
			}else {
				msg ="Mismatch in expected and actual. Expected: Blank, Actual: "+ actualValue;
				msgForExtent="Mismatch in expected and actual. <b>Expected:</b> Blank, <b>Actual:</b> "+ actualValue;
				status="fail";
			}
		}else {
			if(actualValue.equals(expectedValue)) {
				msg="Actual and expected values matched : " +actualValue;
				msgForExtent=msg;
				status="pass";
			}else {
				msg ="Mismatch in expected and actual. Expected: "+ expectedValue +", Actual: "+ actualValue;
				msgForExtent="Mismatch in expected and actual. <b>Expected:</b>"+ expectedValue +", <b>Actual:</b> "+ actualValue;
				status="fail";
			}
		}
		log.info(msg);
		if(status.equalsIgnoreCase("pass"))
			writeResults("pass", msg, msgForExtent, false,"");
		else
			writeResults("fail", msg, msgForExtent, true,"soft");
	}
	
	public static void writeResults(String status, String message, String mesageForExtent, Boolean captureScreenshot, String assertType){
		try {
			if(captureScreenshot==true && !(Report.getBrowserName().equalsIgnoreCase("NA")||Report.getBrowserName().equalsIgnoreCase("")))
				Report.captureScreenshot();
			else
				captureScreenshot=false;
			
			String identifier="";
			if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel"))
				identifier=Controller.tcId +" : " + Controller.transactionName;
			else
				identifier=Controller.transactionName;
			
			if(status.equalsIgnoreCase("pass")){
				if(captureScreenshot==true)
					test.log(Status.PASS, "<b>"+identifier + " : "+Report.getFieldName()+ " : "+"</b>"+mesageForExtent+"</br>",MediaEntityBuilder.createScreenCaptureFromPath(Report.screenshotPath).build());
				else
					test.log(Status.PASS, "<b>"+identifier + " : "+Report.getFieldName()+ " : "+"</b>"+mesageForExtent);
				MongoDB.addTestStepLog(identifier + " : "+Report.getFieldName()+ " : "+ message, "Passed");
			}else if(status.equalsIgnoreCase("fail")) {
				if(captureScreenshot==true)
					test.log(Status.FAIL, "<font color=red><b>"+identifier + " : "+Report.getFieldName()+ " : "+"</b>"+mesageForExtent+"</font></br>",MediaEntityBuilder.createScreenCaptureFromPath(Report.screenshotPath).build());
				else
					test.log(Status.FAIL, "<font color=red><b>"+identifier + " : "+Report.getFieldName()+ " : "+"</b>"+mesageForExtent+"</font>");
				MongoDB.addTestStepLog(identifier + " : "+Report.getFieldName()+ " : "+message, "Failed");
			}
			
			if(assertType.equalsIgnoreCase("hard")) {
				CustomAssert.fail(message);
			}else if(assertType.equalsIgnoreCase("soft")) {
				sAssert.fail(message);
			}
			
			if(StringUtils.equalsIgnoreCase(Config.executionType, "timetravel"))
				if(captureScreenshot==true)
					ExcelUtilities.writeToTimetravelExcelReport(Report.getFieldName(),mesageForExtent, status,Report.screenshotPath);
				else
					ExcelUtilities.writeToTimetravelExcelReport(Report.getFieldName(),mesageForExtent, status,"");
			
		}catch(Exception e) {
			log.error("Issue while writing to Extent Report.");
			e.printStackTrace();
		}
	}
	public static void validatePresenceOfElementInList(String fieldName, String inputValue, String locator) throws IOException {
		int ctr = 0;
		List<WebElement> allElements = WebDriverHelper.iDriver.findElements(By.xpath(locator));
		List<String> presentFieldTexts= new ArrayList<String>();;
		//for (WebElement we : allElements) {
		for(int i=0; i<allElements.size(); i++) {
			actualValue = getElementValue(allElements.get(i));
			presentFieldTexts.add(actualValue);
			if(StringUtils.equals(inputValue, actualValue)) {
				ctr++;	
			}	
		}	
		//}
		if(ctr >0) {
			String msg = "Field '" + inputValue +"' is present on screen as expected.";
			log.info(msg);
			writeResults("pass", msg, msg, false, "");
			}
		else {
			String msg="Field '" + inputValue +"' is not present on screen.";
			msg= msg+"\n Present Values are:";
			for(int i=0;i<presentFieldTexts.size();i++) {
				msg=msg+ "\n" + presentFieldTexts.get(i);
			}
			String msgForExtent=msg.replaceAll("\n", "<br>");
			log.info(msg);
			writeResults("fail", msg, msgForExtent, true,"soft");
			
		}	
	}	
}
