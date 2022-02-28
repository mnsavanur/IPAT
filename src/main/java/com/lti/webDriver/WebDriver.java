package com.lti.webDriver;

import static org.testng.Assert.assertThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.TestUtil.MongoDB;
import com.lti.TestUtil.Report;
import com.lti.api.WebServiceUtil;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.controller.Controller;
import com.lti.controller.CustomAssert;

public class WebDriver {
    static Logger log = Logger.getLogger(WebDriver.class.getName());
    public static boolean fileCheck;
    public static HashMap<String, Integer> testDataHeader;
    public static HashMap<String, Integer> objectDataHeader;
    public static HashMap<Integer, String> fieldNameList;
    public static HashMap<String, String> fieldValue;
    public static HashMap<String, String> objectData;
    public static Sheet dataSheet;
    public static Sheet repositorySheet;
    public static SoftAssert sa = new SoftAssert();
    public static String loaderXpath = "";
    public static String loaderXpath2 = "";
    public static String loaderXpath3 = "";


    public static void dispatch(String inputData, String objectData) {

        try {

            //Storing all the input parameters into variables
            int repoSize;
            String inputValue = inputData;
            String[] objectRepo = objectData.split("\\|");
            repoSize = objectRepo.length;
            String fieldName = objectRepo[0];
            String fieldType = objectRepo[1];
            String action = objectRepo[2];
            String xpath = objectRepo[3];
            String id = objectRepo[4];

            //Retrieve dynamic data of inputValue
            if (!StringUtils.equalsIgnoreCase(fieldType, "DataHandler") && (inputValue.toUpperCase().contains("_GETVALUE") || action.contains(";;")) && !action.startsWith("EW")) {
                String[] actionSplit = action.split(";;");
                if (actionSplit.length < 2) {
                    log.info(fieldName + ": As data input is dynamic with keyword '_GETVALUE', action in Object Repository should be in the format of 'action;;ColumnNameFromDataCarrier'.");
                    Base.test.log(Status.FAIL, "<font color=" + "red>" + "<b>" + Controller.transactionName + " : " + fieldName + " : " + "</b> As data input is dynamic with keyword '_GETVALUE', action in Object Repository should be in the format of 'action;;ColumnNameFromDataCarrier'. </font></br>");
                    MongoDB.addTestStepLog(Controller.transactionName + " : " + fieldName + " : " + "As data input is dynamic with keyword '_GETVALUE', action in Object Repository should be in the format of 'action;;ColumnNameFromDataCarrier'.", "Failed");
                    Base.sa.fail();
                    return;
                }
                action = actionSplit[0];
                //String columnNameInDC=actionSplit[1];
                //String dynamicValue = ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), columnNameInDC);
                //inputValue=inputValue.replace("_GETVALUE", dynamicValue);

                for (int i = 1; i < actionSplit.length; i++) {
                    String columnNameInDC = actionSplit[i];
                    String dynamicValue = ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), columnNameInDC);
                    String valueToReplace = "_GETVALUE";
                    if (i > 1)
                        valueToReplace = "_" + i + "GETVALUE";
                    inputValue = inputValue.replace(valueToReplace, dynamicValue);
                }
            }

            //Replace _GETRTVALUE1 keyword in inputValue. Designed for system date change in Billing TT approach, where date is part of TestSuite file
            if (inputValue.toUpperCase().contains("_GETRTVALUE1")) {
                inputValue = inputValue.replace("_GETRTVALUE1", Report.getRuntimeValue1());
            }

            //Replace _LOCALPATH keyword in inputValue
            if (inputValue.toUpperCase().contains("_LOCALPATH")) {
                inputValue = inputValue.replace("_LOCALPATH", System.getProperty("user.dir").toString());
            }

            //Replace _TCNAME keyword in inputValue
            if (inputValue.toUpperCase().contains("_TCNAME")) {
                inputValue = inputValue.replace("_TCNAME", Report.getTestCaseId());
            }

            //Fetching the locator based on xpath and id mentioned in Object Repository sheet
            String locator;
            locator = getLocator(xpath, id);
            if (locator == null) {
                Base.test.log(Status.FAIL, "<font color=" + "red>" + "<b>" + Controller.transactionName + " : " + fieldName + " : " + "</b>" + "Locator value missing in both XPath and ID. </font>");
                log.info(fieldName + " : Locator value missing in both XPath and ID");
                Base.sa.fail();
                MongoDB.addTestStepLog(Controller.transactionName + " : " + fieldName + " : " + "Locator value missing in both XPath and ID", "Failed");
            } else {

                //if (locator.startsWith("//") || locator.startsWith("(//")) {
                if (locator.contains("_FIELDNAME")) {
                    locator = locator.replace("_FIELDNAME", fieldName);
                }
                if (locator.contains("_INDEX")) {
                    String[] inputSplit = inputValue.split("\\|");
                    if (inputSplit.length < 2) {
                        String msg = "Incorrect input= '" + inputValue + "'. Provide input in the format of 'indexValue|inputValue'.";
                        log.info(msg);
                        Assertions.writeResults("fail", msg, msg, true, "hard");
                        return;
                    } else {
                        locator = locator.replace("_INDEX", inputSplit[0]);
                        //inputValue=inputSplit[1];
                        //to handle inputs which have '|' more than once. Ex fomrat verification using VF with xpath having_INDEX
                        inputValue = inputValue.substring(inputValue.indexOf("|") + 1, inputValue.length());
                    }
                }
                if (locator.contains("_VALUE")) {
                    if (fieldType.equalsIgnoreCase("checkbox") && inputValue.contains("|")) {
                        String[] splitInput = new String[2];
                        splitInput = inputValue.split("\\|");
                        inputValue = splitInput[1];
                        locator = locator.replace("_VALUE", splitInput[0]);
                    } else {
                        locator = locator.replace("_VALUE", inputValue);
                    }
                }
                //}
                Report.setFieldName(fieldName);
                Report.setFieldType(fieldType);
                Report.setFieldAction(action);
                Report.setFieldValue(inputValue);
                Report.setLocator(locator);
                UiHandler.printLog();
                if (inputValue != "") {
                    switch (fieldType) {
                        case "Text Box":
                            UiHandler.handleTextBox(fieldName, inputValue, action, locator);
                            break;
                        case "Button":
                            UiHandler.handleButton(fieldName, inputValue, action, locator);
                            break;
                        case "Link":
                            UiHandler.handleLink(fieldName, inputValue, action, locator);
                            break;
                        case "Dropdown":
                            UiHandler.handleInsurityDropdown(fieldName, inputValue, action, locator);
                            break;
                        case "Radio Button":
                            UiHandler.handleRadioButton(fieldName, inputValue, action, locator);
                            break;
                        case "Date":
                            UiHandler.handleDate(fieldName, inputValue, action, locator);
                            break;
                        case "Label":
                            UiHandler.handleLabel(fieldName, inputValue, action, locator);
                            break;
                        case "iDropdown":
                            UiHandler.iDropDown(fieldName, inputValue, action, locator);
                            break;
                        case "Checkbox":
                            UiHandler.handleCheckbox(fieldName, inputValue, action, locator);
                            break;
                        case "Alert":
                            UiHandler.handleAlert(fieldName, inputValue, action);
                            break;
                        case "DataHandler":
                            UiHandler.handleRuntimeData(fieldName, inputValue, action, locator);
                            break;
                        case "DataReader":
                            UiHandler.handleReadDataFromDataCarrier(fieldName, inputValue, action, locator);
                            break;
                        case "Status":
                            UiHandler.handleTransactionStatus(fieldName, inputValue, action, locator);
                            break;
                        case "PDFCompare":
                            UiHandler.downloadRenameComparePDF(fieldName, inputValue, action, locator);
                            break;
                        case "WebService":
                            WebServiceUtil.handleWebService(fieldName, inputValue, action);
                            break;
                        case "Wait":
                            UiHandler.handleWait(fieldName, inputValue, action, locator);
                            break;
                        case "File Upload":
                            UiHandler.handleFileUpload(fieldName, inputValue, action, locator);
                            break;
                        case "XMLCompare":
                            UiHandler.handleXMLCompare(fieldName, inputValue, action, locator);
                            break;
                        case "Download File":
                            UiHandler.downloadFile(fieldName, inputValue, action, locator);
                            break;
                        case "FileHandler":
                            UiHandler.handleFileOperations(fieldName, inputValue, action, locator);
                            break;
                        case "Screenshot":
                            UiHandler.takeScreenshot(fieldName, inputValue, action, locator);
                            break;
                        case "Switch To Frame":
                            UiHandler.handleSwitchToFrame(fieldName, inputValue, action, locator);
                            break;
                        case "Switch To Browser":
                            UiHandler.handleSwitchToBrowser(fieldName, inputValue, action, locator);
                            break;
                        case "Close Browser Window":
                            UiHandler.closeBrowserWindow(fieldName, inputValue, action, locator);
                            break;
                        case "Tab":
                            UiHandler.handleTabClick(fieldName, inputValue, action, locator);
                            break;
                        case "URL":
                            UiHandler.handleURL(fieldName, inputValue, action, locator);
                            break;
                        case "Activated Dropdown":
                            UiHandler.handleUnqorkIntegrationField(fieldName, inputValue, action, locator);
                            break;
                        case "Field Verification":
                            UiHandler.hanldeFieldVerification(fieldName, inputValue, action, locator);
                            break;
                        case "MultiCheckbox Dropdown":
                            UiHandler.handleMultiCheckboxDropdown(fieldName, inputValue, action, locator);
                            break;
                        case "Masked Text Box":
                            UiHandler.handleMaskedTextBox(fieldName, inputValue, action, locator);
                            break;
                        case "Clear":
                            UiHandler.clearFieldValue(fieldName, inputValue, action, locator);
                            break;
                        case "Quit Browser":
                            UiHandler.quitBrowser();
                            break;
                        case "Launch Browser":
                            UiHandler.launchBrowser();
                            break;
                        case "Calculations":
                            UiHandler.handleCalculations(fieldName, inputValue, action, locator);
                            break;
                        case "Tooltip":
                            UiHandler.handleTooltip(fieldName, inputValue, action, locator);
                            break;
                        case "List Verification":
                            UiHandler.handleListVerification(fieldName, inputValue, action, locator);
                            break;
                        case "Database":
                            UiHandler.handleDatabaseOperations(fieldName, inputValue, action, locator);
                            break;
                        default:
                            String msg = "Unable to find fieldType-action combination : '" + fieldType + "-" + action + "'. Update the Object Repository.";
                            Assertions.writeResults("fail", msg, msg, false, "soft");
                            log.info(msg);
                            break;
                    }
                }
                //Thread.sleep(1000);
                //Calling methods based on field type
                /*
                 * Report.setFieldName(""); Report.setFieldType(""); Report.setFieldAction("");
                 * Report.setFieldValue(""); Report.setLocator("");
                 */
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Unexpected issue." + ex.getMessage();
            Assertions.writeResults("fail", msg, msg, true, "hard");
        }
    }

    public static String getLocator(String xpath, String id) {
        String locator;
        if (xpath.equals("NP") && id.equals("NP"))
            locator = null;
        else if (!xpath.equals("NP") && id.equals("NP"))
            locator = xpath;
        else if (xpath.equals("NP") && !id.equals("NP"))
            locator = id;
        else
            locator = null;

        return locator;
    }

    public static void handleLoader() throws Exception {
        if (WebDriverHelper.iDriver != null) {
            WebDriverWait noWait = new WebDriverWait(WebDriverHelper.iDriver, 0);
            boolean alertFound = false;
            try {
                noWait.until(ExpectedConditions.alertIsPresent());
                alertFound = true;
            } catch (Exception e) {
                alertFound = false;
            }
            if (!alertFound) {

                switch (Config.platform) {
                    case "GuideWire":
                    case "Guidewire":
                    case "Guide Wire":
                        Thread.sleep(200);
                        loaderXpath = "//div[@class='gw-click-overlay gw-disable-click']";
                        break;
                    case "Duck Creek":
                    case "DuckCreek":
                    case "Duckcreek":
                        loaderXpath = "//body[@class=\"has-error-position-inline is-chatting\"]";
                        loaderXpath2 = "//div[@class='spinner']";
                        loaderXpath3 = "//body[@style='overflow: hidden;']";
                        break;
				/*case "Unqork":
					loaderXpath="";
				default:
					throw new Exception("Unable to handle Loader for platform: "+Config.platform);*/
                }
				/*if(StringUtils.equalsIgnoreCase(Config.platform, "Guidewire"))
					loaderXpath="//div[@class='gw-click-overlay gw-disable-click']";
				if(StringUtils.equalsIgnoreCase(Config.platform, "Duck Creek")) {
					loaderXpath="//body[@class=\"has-error-position-inline is-chatting\"]";
					//Claims loaders
					loaderXpath2="//div[@class='spinner']";
					loaderXpath3="//body[@style='overflow: hidden;']";
				}*/

                if (!loaderXpath.equals("")) {
                    try {
                        for (int i = 1; i <= 75; i++) {
                            List<WebElement> loaderList = WebDriverHelper.iDriver.findElements(By.xpath(loaderXpath));
                            int loaderSize = loaderList.size();
                            if (loaderSize > 0) {
                                Thread.sleep(400);
                            } else
                                break;
                        }
                    } catch (Exception e) {
                    }
                }
                if (!loaderXpath2.equals("")) {
                    try {
                        for (int i = 1; i <= 75; i++) {
                            List<WebElement> loaderList = WebDriverHelper.iDriver.findElements(By.xpath(loaderXpath2));
                            int loaderSize = loaderList.size();
                            if (loaderSize > 0) {
                                //System.out.println("claims loader 1");
                                Thread.sleep(400);
                            } else
                                break;
                        }
                    } catch (Exception e) {
                    }
                }
                if (!loaderXpath3.equals("")) {
                    try {
                        for (int i = 1; i <= 75; i++) {
                            List<WebElement> loaderList = WebDriverHelper.iDriver.findElements(By.xpath(loaderXpath3));
                            int loaderSize = loaderList.size();
                            if (loaderSize > 0) {
                                //System.out.println("claims loader 2");
                                Thread.sleep(400);
                            } else
                                break;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

}
