package com.lti.webDriver;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.maven.surefire.shared.utils.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import com.aspose.pdf.Document;
import com.aspose.pdf.HtmlLoadOptions;
import com.aspose.pdf.SaveFormat;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.google.common.io.Files;
import com.google.inject.Key;
import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.TestUtil.Calculations;
import com.lti.TestUtil.DatabaseHandling;
import com.lti.TestUtil.ExternalFileHandling;
import com.lti.TestUtil.MongoDB;
import com.lti.TestUtil.Report;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.controller.CustomAssert;
import com.lti.controller.Controller;

public class UiHandler extends WebDriverHelper {
    public static WebElement element;
    public static String actualResult = null;
    //public static SoftAssert sa;
    public static Exception ex = null;
    public static WebElement wElement;
    public static SimpleDateFormat formatter;
    public static Date date;
    public static String dateValue;
    static Logger log = Logger.getLogger(UiHandler.class.getName());

    public void pleaseWaitDisappear() {
        try {
            while (iDriver.findElement(By.xpath("//span[text()='Please Wait ...']")).isDisplayed()) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {

        }
    }

    public static String evaluateLocator(String locator, String fieldname, String inputValue) throws Exception {
        if (locator.startsWith("//") || locator.startsWith("(//")) {
            if (locator.contains("_FIELDNAME")) {
                locator = locator.replace("_FIELDNAME", fieldname);
            }
            if (locator.contains("_VALUE")) {
                locator = locator.replace("_VALUE", inputValue);
            }
        }
        return locator;
    }

    public static WebElement getElement(String locator, String fieldname, String inputValue, Boolean throwErrorIfNotFound) throws Exception {
        try {
            locator = evaluateLocator(locator, fieldname, inputValue);
            if (locator.startsWith("//") || locator.startsWith("(//")) {
                Thread.sleep(400);
                wElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
                try {
                    WebDriverWait wait2 = new WebDriverWait(iDriver, 1);
                    wait2.until(ExpectedConditions.visibilityOf(wElement));
                    wait2.until(ExpectedConditions.elementToBeClickable(wElement));
                } catch (Exception e) {
                    log.error("VisibilityOfElement\\ElementToBeClickable condition failed");
                }
                try {
                    js.executeScript("arguments[0].scrollIntoView(true);", wElement);
                } catch (Exception e) {
                    //log.info("ScrollToElement failed.");
                }
                highlightElement(wElement);
                //unHighilightElement(element);

            } else {
                Thread.sleep(400);
                wElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(locator)));
                try {
                    WebDriverWait wait2 = new WebDriverWait(iDriver, 1);
                    wait2.until(ExpectedConditions.visibilityOf(wElement));
                    wait2.until(ExpectedConditions.elementToBeClickable(wElement));
                } catch (Exception e) {
                    log.error("VisibilityOfElement\\ElementToBeClickable condition failed");
                }

                //Thread.sleep(1000);
                js.executeScript("arguments[0].scrollIntoView(true);", wElement);
                highlightElement(wElement);
                //unHighilightElement(element);

            }
        } catch (Exception e) {
            if (Report.getOptionalFieldFlag()) {
                String msg = "Optional field not present on screen.";
                log.info(msg);
                Assertions.writeResults("pass", msg, msg, false, "");
                throwErrorIfNotFound = false;
            }
            if (throwErrorIfNotFound == true) {
                String msg = "Issue while locating XPath\\Id : " + locator;
                log.error("Issue while locating XPath\\Id : " + locator, e);
                Assertions.writeResults("fail", msg, msg, true, "hard");
                wElement = null;
            } else {
                wElement = null;
            }
        }
        return wElement;
    }

    public static void highlightElement(WebElement ele) {
        //js.executeScript("arguments[0].setAttribute('style', arguments[1]);", ele, "border: 3px solid red;");
    }

    public static void unHighilightElement(WebElement ele) {
        //js.executeScript("arguments[0].setAttribute('style', arguments[1]);", ele, "border: ");
    }

    public static void printLog() {
        log.info("--->" + Report.getFieldName() + " | " + Report.getFieldType() + "-" + Report.getFieldAction() + " | Value=" + Report.getFieldValue()
                + " | Locator=" + Report.getLocator());
    }

    public static void handleRuntimeData(String fieldName, String inputValue, String action, String locator) throws Exception {
        String[] actionSplit = action.split(";;");
        new UiHandler().pleaseWaitDisappear();
        if (actionSplit.length < 2) {
            String msg = fieldName + ": Incorrect action against FieldType 'DataHandler' in Object Repository. Action should be in the format of 'W;;ColumnNameFromDataCarrier'.";
            log.info(msg);
            Assertions.writeResults("fail", msg, msg, false, "soft");
            return;
        }
        action = actionSplit[0];
        String columnNameInDC = actionSplit[1];
        String tcToWriteInDC = Report.getTestCaseId();
        String valueToWriteInDC = null;

        //identify column to write
        if (inputValue.toUpperCase().contains("GETVALUE") && !inputValue.toUpperCase().contains("_GETVALUE")) {
            try {
                String indexStr = inputValue.substring(inputValue.indexOf("_") + 1, inputValue.indexOf("GETVALUE"));

                int index = Integer.parseInt(indexStr);
                columnNameInDC = actionSplit[index];
            } catch (Exception e) {
                String msg = "Issue while writing to particular column in Data Carrier file. Check data input and Object reposity action.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                return;
            }
        }

        if (!locator.equalsIgnoreCase("NA")) {
            element = getElement(locator, fieldName, inputValue, true);
            //if(!inputValue.equalsIgnoreCase("_GETVALUE"))
            if (!inputValue.toUpperCase().contains("GETVALUE"))
                tcToWriteInDC = inputValue;

            //retrieve Value from UI
            valueToWriteInDC = element.getText();

            if (valueToWriteInDC == null || valueToWriteInDC.equalsIgnoreCase(""))
                valueToWriteInDC = element.getAttribute("value");
            if (action.equalsIgnoreCase("RSW"))//RSW=remove space and write
                valueToWriteInDC = valueToWriteInDC.replace(" ", "");
        } else {
            valueToWriteInDC = inputValue;
        }

        if (valueToWriteInDC == null)
            valueToWriteInDC = "Not Found\\Empty";

        if (action.equalsIgnoreCase("W") || action.equalsIgnoreCase("RSW")) {
            try {
                ExcelUtilities.writeToDataCarrier(tcToWriteInDC, columnNameInDC, valueToWriteInDC);
                String msg = "Value '" + valueToWriteInDC + "' successfully written in Data Carrier file against TC '" + tcToWriteInDC + "' and column '" + columnNameInDC + "'.";
                log.info(msg);
                Assertions.writeResults("pass", msg, msg, false, "");
            } catch (Exception e) {
                Base.sa.fail();
                String msg = "Unable to write to Data Carrier.";
                log.info(msg);
                Assertions.writeResults("fail", msg, msg, false, "soft");
            }
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void handleReadDataFromDataCarrier(String fieldName, String inputValue, String action, String locator) throws Exception {
        //String[] actionSplit = action.split(";;");
        /*if (actionSplit.length < 1) {
            String msg = fieldName + ": Incorrect action against FieldType 'DataReader' in Object Repository. Action should be in the format of 'R;;ColumnNameFromDataCarrier'.";
            log.info(msg);
            Assertions.writeResults("fail", msg, msg, false, "soft");
            return;
        }*/
        //action = actionSplit[0];
        String columnNameInDC = "PolicyNum";
        String tcToReadInDC = inputValue;
        String valueToReadInDC = null;
        new UiHandler().pleaseWaitDisappear();

        if (action.equalsIgnoreCase("R") || action.equalsIgnoreCase("RSW")) {
            try {
                valueToReadInDC = ExcelUtilities.readFromDataCarrier(tcToReadInDC, columnNameInDC);
                String msg = "Value '" + valueToReadInDC + "' successfully read from Data Carrier file against TC '" + tcToReadInDC + "' and column '" + columnNameInDC + "'.";
                log.info(msg);
                Assertions.writeResults("pass", msg, msg, false, "");
            } catch (Exception e) {
                Base.sa.fail();
                String msg = "Unable to read from Data Carrier.";
                log.info(msg);
                Assertions.writeResults("fail", msg, msg, false, "soft");
            }
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
        if (!locator.equalsIgnoreCase("NA")) {
            element = getElement(locator, fieldName, inputValue, true);

            //Send Value to particular field in UI
            if (valueToReadInDC != null) {
                new UiHandler().pleaseWaitDisappear();
                element.sendKeys(valueToReadInDC);
            } else {
                String msg = "Value not found in Data Carrier : '" + Report.getFieldType() + "-" + action + "'. Update the Runtime Data Carrier.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        } else {
            valueToReadInDC = inputValue;
        }
    }

    public static void handleTransactionStatus(String fieldName, String inputValue, String action, String locator) throws Exception {
        element = getElement(locator, fieldName, inputValue, true);
        //printLog();
        new UiHandler().pleaseWaitDisappear();
        if (element != null) {
            unHighilightElement(element);
            //Mouse hover
            try {
                for (int checkCounter = 0; checkCounter < 35; checkCounter++) {
                    WebElement transactionStatusLabel = iDriver.findElement(By.xpath("//label[@id='CoPolicySelectPg_PolicyGrid_lblProcessingStatus']//span"));
                    if (transactionStatusLabel.getText().equals("Available")) {
                        String msg = "Process Status: " + transactionStatusLabel.getText();
                        log.info(msg);
                        WebDriver.handleLoader();
                        Assertions.writeResults("pass", msg, msg, false, "");
                        Thread.sleep(2000);
                        break;
                    }
                    new UiHandler().pleaseWaitDisappear();
                    element.click();
                    new UiHandler().pleaseWaitDisappear();
                }
            } catch (Exception e) {
                Actions act = new Actions(iDriver);
                act.moveToElement(element).click().perform();
            }
        }
    }

    public static void handleMaskedTextBox(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("I")) {
            String[] splitInputandVerification = inputValue.split("\\|");
            element = getElement(locator, fieldName, inputValue, true);
            if (element != null) {
                //printLog();
                unHighilightElement(element);

                element.clear();
                element.sendKeys(splitInputandVerification[0]);
                element.sendKeys(Keys.TAB);

                WebDriver.handleLoader();
                Assertions.validateMaskedTextBox(locator, inputValue, fieldName, action);
            }
        } else if (action.equalsIgnoreCase("V")) {
            Assertions.validateMaskedTextBox(locator, inputValue, fieldName, action);
        } else if (action.equalsIgnoreCase("F")) {
            element = getElement(locator, fieldName, inputValue, true);
            if (element != null) {
                //printLog();
                unHighilightElement(element);
                element.clear();
                Thread.sleep(1000);
                element.click();
                for (int i = 0; i < inputValue.length(); i++) {
                    element.sendKeys(inputValue.substring(i, i + 1));
                    Thread.sleep(1000);
                }

                element.sendKeys(Keys.TAB);

                Assertions.validateMaskedTextBox(locator, inputValue, fieldName, action);
            }
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }


    public static void clearFieldValue(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("I")) {
            element = getElement(locator, fieldName, inputValue, true);
            if (element != null) {
                //printLog();
                unHighilightElement(element);
                element.clear();
                element.sendKeys(Keys.TAB);
                WebDriver.handleLoader();
                String msg = "Successfully cleared the value from the field.";
                log.info(msg);
                Assertions.writeResults("pass", msg, msg, false, "");
            }
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void handleTextBox(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("I")) {
            element = getElement(locator, fieldName, inputValue, true);
            new UiHandler().pleaseWaitDisappear();
            if (element != null) {
                //printLog();
                unHighilightElement(element);
                if (StringUtils.equalsIgnoreCase(inputValue, "_clear")) {
                    element.clear();
                    element.sendKeys(Keys.TAB);
                    WebDriver.handleLoader();
                    Assertions.validateTextBox(locator, "", fieldName, action);
                } else {
                    //element.clear();
                    //JS clear added to handle fields which defaults values after .clear command
                    JavascriptExecutor js1 = (JavascriptExecutor) iDriver;
                    js1.executeScript("arguments[0].value = '';", element);
                    element.sendKeys(inputValue);
                    element.sendKeys(Keys.TAB);
                    WebDriver.handleLoader();
                    Assertions.validateTextBox(locator, inputValue, fieldName, action);
                }
            }
        } else if (action.equalsIgnoreCase("V")) {
            Assertions.validateTextBox(locator, inputValue, fieldName, action);
        } else if (action.equalsIgnoreCase("F")) {
            element = getElement(locator, fieldName, inputValue, true);
            if (element != null) {
                //printLog();
                unHighilightElement(element);
                element.clear();
                Thread.sleep(1000);
                element.click();
                for (int i = 0; i < inputValue.length(); i++) {
                    element.sendKeys(inputValue.substring(i, i + 1));
                    Thread.sleep(1000);
                }

                element.sendKeys(Keys.TAB);

                Assertions.validateTextBox(locator, inputValue, fieldName, action);
            }
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void handleButton(String fieldName, String inputValue, String action, String locator) throws Exception {
        element = getElement(locator, fieldName, inputValue, true);
        new UiHandler().pleaseWaitDisappear();
        //printLog();
        if (element != null) {
            unHighilightElement(element);
            //Mouse hover
            if (action.equalsIgnoreCase("MH")) {
                Actions actions = new Actions(iDriver);
                actions.moveToElement(element).perform();
                String msg = "Successfully Clicked on " + fieldName + " button";
                log.info(msg);
                WebDriver.handleLoader();
                Assertions.writeResults("pass", msg, msg, false, "");

            } else {
                try {
                    element.click();
                    new UiHandler().pleaseWaitDisappear();
                } catch (Exception e) {
                    Actions act = new Actions(iDriver);
                    act.moveToElement(element).click().perform();
                }

                String msg = "Successfully Clicked on " + fieldName + " button";
                log.info(msg);
                WebDriver.handleLoader();
                Assertions.writeResults("pass", msg, msg, false, "");
            }
        }
    }

    public static void handleLink(String fieldName, String inputValue, String action, String locator) throws Exception {
        element = getElement(locator, fieldName, inputValue, true);
        new UiHandler().pleaseWaitDisappear();
        //printLog();
        if (element != null) {
            unHighilightElement(element);
            String value;
            if (action.equalsIgnoreCase("V")) {
                WebDriver.handleLoader();
                Assertions.validateLink(element, inputValue, fieldName);
            } else if (action.equalsIgnoreCase("C")) {
                WebDriver.handleLoader();
                Assertions.validateLink(element, inputValue, fieldName);
                element.click();
                new UiHandler().pleaseWaitDisappear();
            } else {
                String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        }
    }

    public static void handleInsurityDropdown(String fieldName, String inputValue, String action, String locator) throws Exception {
        element = getElement(locator, fieldName, inputValue, true);
        String controllerId;
        String drpDownLocator;
        String listId;
        String listLocator;
        new UiHandler().pleaseWaitDisappear();
        if (element != null) {
            if (action.equalsIgnoreCase("I")) {
                controllerId = element.getAttribute("id");
                drpDownLocator = locator + "//following-sibling::a";

                listId = controllerId + "list";
                listLocator = "//ul[@id=\"" + listId + "\"]/li[@data-value=\"" + inputValue + "\"]";
                try {
                    element = getElement(drpDownLocator, fieldName, inputValue, true);
                    element.click();
                    new UiHandler().pleaseWaitDisappear();

                    element = getElement(listLocator, fieldName, inputValue, true);
                    element.click();
                    new UiHandler().pleaseWaitDisappear();

                    String msg = "Successfully selected " + inputValue + " as value from the " + fieldName + " dropdown";
                    log.info(msg);
                    Assertions.writeResults("pass", msg, msg, false, "");
                } catch (Exception e) {
                    String msg = "Unable to Select " + inputValue + " as value from the " + fieldName + " dropdown";
                    log.info(msg);
                    Assertions.writeResults("fail", msg, msg, true, "soft");
                }

            }
        }
    }

    public static void handleDropdown(String fieldName, String inputValue, String action, String locator)
            throws Exception {
        //printLog();
        element = getElement(locator, fieldName, inputValue, true);
        int eleAttempts = 0;
        boolean result;
        if (element != null) {
            unHighilightElement(element);
            if (action.equalsIgnoreCase("I")) {
                //Thread.sleep(500);

                //Step 1 - Selecting value from the dropdown
                Select drpDown = new Select(element);
                try {
                    drpDown.selectByVisibleText(inputValue);
                    //Thread.sleep(500);

                    //Step 2- Tabbing out and handling exceptions
                    while (eleAttempts < 10) {
                        try {
                            element = getElement(locator, fieldName, inputValue, true);
                            result = true;
                            Thread.sleep(1000);
                            break;
                        } catch (StaleElementReferenceException ex) {
                        }
                        eleAttempts++;
                        element.sendKeys(Keys.TAB);
                    }
                    WebDriver.handleLoader();
                    Assertions.validateDropdown(locator, inputValue, fieldName, action);
                } catch (Exception e) {
                    e.printStackTrace();
                    String msg = "Unable to find value '" + inputValue + "' in the dropdown.";
                    log.info(msg);
                    WebDriver.handleLoader();
                    Assertions.writeResults("fail", msg, msg, true, "hard");
                }
            } else if (action.equalsIgnoreCase("V")) {
                WebDriver.handleLoader();
                Assertions.validateDropdownValues(locator, inputValue, fieldName, action);
            } else {
                String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        }
    }

    public static void handleRadioButton(String fieldName, String inputValue, String action, String locator) throws Exception {
        element = getElement(locator, fieldName, inputValue, true);
        new UiHandler().pleaseWaitDisappear();
        //printLog();
        if (element != null) {
            unHighilightElement(element);
            if (action.equalsIgnoreCase("C")) {
                element.click();
                String msg = "Successfully clicked on " + inputValue + " option for " + fieldName;
                log.info(msg);
                WebDriver.handleLoader();
                Assertions.writeResults("pass", msg, msg, false, "");
                //Assertions.validateRadioButton(element, inputValue, fieldName);
            } else if (action.equalsIgnoreCase("V")) {
                WebDriver.handleLoader();
                Assertions.validateRadioButton(element, inputValue, fieldName);
            } else {
                String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        }
    }

    public static void handleSwitchToFrame(String fieldName, String inputValue, String action, String locator) throws Exception {
        element = getElement(locator, fieldName, inputValue, true);
        if (element != null) {
            iDriver.switchTo().frame(element);
            log.info("Switched to frame");
        }
    }

    public static void handleSwitchToBrowser(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("I")) {
            Set<String> handlers = null;
            handlers = iDriver.getWindowHandles();
            for (String handler : handlers) {
                iDriver = iDriver.switchTo().window(handler);
                if (iDriver.getTitle().contains(locator)) {
                    log.info("Focus on window with title: " + iDriver.getTitle());
                    break;
                }
            }
        } else if (action.equalsIgnoreCase("V")) {
            Assertions.validateBrowserWindow(inputValue, fieldName, locator);
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }

    }

    public static void closeBrowserWindow(String fieldName, String inputValue, String action, String locator) throws Exception {
        Set<String> handlers = null;
        handlers = iDriver.getWindowHandles();
        for (String handler : handlers) {
            iDriver = iDriver.switchTo().window(handler);
            String title = iDriver.getTitle();
            if (title.contains(inputValue) && !inputValue.trim().equals("")) {
                log.info("Focus on window with title: " + title);
                iDriver.close();
                log.info("Window with title: " + title + " is closed.");
                break;
            }
        }
    }

    public static void quitBrowser() throws Exception {
        WebDriverHelper.iDriver.quit();
    }

    public static void launchBrowser() throws Exception {
        if (!StringUtils.equalsIgnoreCase(Report.getBrowserName(), "NA")) {
            WebDriverHelper.launchDriver(Report.getBrowserName(), Config.applicationUrl);
        }
    }

    public static void handleTabClick(String fieldName, String inputValue, String action, String locator) throws Exception {
        new UiHandler().pleaseWaitDisappear();
        try {
            element = getElement(locator, fieldName, inputValue, false);
            element.sendKeys(Keys.TAB);
        } catch (Exception e) {
            log.error("Tab hit failed");
        }
    }

    public static void handleURL(String fieldName, String inputValue, String action, String locator) throws Exception {
        try {
            WebDriverHelper.launchDriver(Report.getBrowserName(), locator);
            String msg = "URL '" + locator + "' Successfully launched.";
            log.info(msg);

            Assertions.writeResults("pass", msg, msg, false, "");
        } catch (Exception e) {
            log.error("Unable to launch URL:" + locator);
            e.printStackTrace();
            String msg = "Unable to launch URL '" + locator + "'.\n" + e.getMessage();
            Assertions.writeResults("fail", msg, msg, true, "hard");
        }
    }

    //designed for DC-Claims
    public static void handleMultiCheckboxDropdown(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("I")) {
            element = getElement(locator, fieldName, inputValue, true);
            //printLog();
            if (element != null) {
                String[] inputSplit = inputValue.split("\\|");
                unHighilightElement(element);
                element.click();

                for (int i = 0; i < inputSplit.length; i++) {
                    //label[text()='Additional Insured']/preceding-sibling::input
                    String tempInputValue = inputSplit[i].trim();
                    String tempCheckboxValue = "Check";
                    if (tempInputValue.toLowerCase().startsWith("uncheck-")) {
                        tempCheckboxValue = "uncheck";
                        tempInputValue = tempInputValue.replace("uncheck-", "");
                        tempInputValue = tempInputValue.trim();
                    }
                    String tempFieldName = fieldName + " Checkbox: " + tempInputValue;
                    String tempAction = "C";
                    String tempLocator = "//label[text()='" + tempInputValue + "']/preceding-sibling::input";
                    handleCheckbox(tempFieldName, tempCheckboxValue, tempAction, tempLocator);
                }
                element = getElement(locator, fieldName, inputValue, true);
                Thread.sleep(200);
                element.sendKeys(Keys.TAB);
                Thread.sleep(300);
            }
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void hanldeFieldVerification(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("V")) {
            if (inputValue.equalsIgnoreCase("_Present") || inputValue.equalsIgnoreCase("_NotPresent")) {
                String result[] = Assertions.validatePresenceOfField(locator, inputValue, fieldName);
                String status = result[0];
                String msg = result[1];
                String msgForExtent = msg;
                log.info(msg);
                if (status.equalsIgnoreCase("pass"))
                    Assertions.writeResults("pass", msg, msgForExtent, false, "");
                else
                    Assertions.writeResults("fail", msg, msgForExtent, true, "soft");
            } else if (inputValue.equalsIgnoreCase("_Enabled") || inputValue.equalsIgnoreCase("_Disabled")) {
                String result[] = Assertions.validateAccessibilityOfElement(locator, inputValue, fieldName);
                String status = result[0];
                String msg = result[1];
                String msgForExtent = msg;
                log.info(msg);
                if (status.equalsIgnoreCase("pass"))
                    Assertions.writeResults("pass", msg, msgForExtent, false, "");
                else
                    Assertions.writeResults("fail", msg, msgForExtent, true, "soft");
            } else if (inputValue.equalsIgnoreCase("_readOnly") || inputValue.equalsIgnoreCase("_notReadOnly")) {
                String result[] = Assertions.validateAccessibilityOfElement2(locator, inputValue, fieldName);
                String status = result[0];
                String msg = result[1];
                String msgForExtent = msg;
                log.info(msg);
                if (status.equalsIgnoreCase("pass"))
                    Assertions.writeResults("pass", msg, msgForExtent, false, "");
                else
                    Assertions.writeResults("fail", msg, msgForExtent, true, "soft");
            } else if (inputValue.equalsIgnoreCase("_mandatory") || inputValue.equalsIgnoreCase("_optional")) {
                String result[] = Assertions.validateNecessityOfElement(locator, inputValue, fieldName);
                String status = result[0];
                String msg = result[1];
                String msgForExtent = msg;
                log.info(msg);
                if (status.equalsIgnoreCase("pass"))
                    Assertions.writeResults("pass", msg, msgForExtent, false, "");
                else
                    Assertions.writeResults("fail", msg, msgForExtent, true, "soft");
            } else {
                String msg = "Inorrect input for FieldType 'Field Verification'. Valid inputs are: _Present, _NotPresent, _Enabled, _Disabled, _mandatory, _optional.";
                Assertions.writeResults("fail", msg, msg, true, "soft");
            }
        } else if (action.equalsIgnoreCase("VC")) {//verify count of elements
            locator = evaluateLocator(locator, fieldName, inputValue);
            List<WebElement> loaderList = WebDriverHelper.iDriver.findElements(By.xpath(locator));
            int actualSearchedElementCount = loaderList.size();
            int expectedSearchedElementCount = 0;
            String msg = "", status = "", msgForExtent = "";
            try {
                expectedSearchedElementCount = Integer.parseInt(inputValue);
                if (expectedSearchedElementCount == actualSearchedElementCount) {
                    status = "pass";
                    msg = "Expected and actual occurrence(" + expectedSearchedElementCount + ") of field matches.";
                    msgForExtent = msg;
                } else {
                    status = "fail";
                    msg = "Mismatch in expected occurrence of field. Expected Occurrence=" + expectedSearchedElementCount + ", Actual Occurrence=" + actualSearchedElementCount + ".";
                    msgForExtent = "Mismatch in expected occurrence of field. <b>Expected Occurrence:</b>" + expectedSearchedElementCount + ", <b>Actual Occurrence:</b>" + actualSearchedElementCount + ".";
                }
            } catch (Exception e) {
                msg = "Provide input as expected count of searched form.";
                status = "fail";
                msgForExtent = msg;
            }

            if (status.equalsIgnoreCase("pass"))
                Assertions.writeResults("pass", msg, msgForExtent, false, "");
            else
                Assertions.writeResults("fail", msg, msgForExtent, true, "soft");
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void handleCheckbox(String fieldName, String inputValue, String action, String locator) throws Exception {
        new UiHandler().pleaseWaitDisappear();
        if (action.equalsIgnoreCase("C")) {
            element = getElement(locator, fieldName, inputValue, true);
            //printLog();
            if (element != null) {
                unHighilightElement(element);
                String checkboxStatus = element.getAttribute("checked");
                Boolean isSelected = false;
                if (StringUtils.equalsIgnoreCase(checkboxStatus, "true"))
                    isSelected = true;
                else
                    isSelected = false;
                if (inputValue.equalsIgnoreCase("check")) {
                    if (!isSelected) {
                        //element.click();
                        try {
                            Actions actionBuild = new Actions(WebDriverHelper.iDriver);
                            Action clickAction = actionBuild.moveToElement(element).clickAndHold().release().build();
                            clickAction.perform();
                        } catch (Exception e) {
                            //added for DC claims as Action click gives error
                            element.click();
                        }

                        String msg = "Successfully selected checkbox '" + fieldName + "'.";
                        log.info(msg);
                        Assertions.writeResults("pass", msg, msg, false, "");
                    } else {
                        String msg = "Checkbox '" + fieldName + "' is already selected.";
                        log.info(msg);
                        Assertions.writeResults("pass", msg, msg, false, "");
                    }
                } else if (inputValue.equalsIgnoreCase("uncheck")) {
                    if (isSelected) {
                        //element.click();
                        try {
                            Actions actionBuild = new Actions(WebDriverHelper.iDriver);
                            Action clickAction = actionBuild.moveToElement(element).clickAndHold().release().build();
                            clickAction.perform();
                        } catch (Exception e) {
                            //added for DC claims as Action click gives error
                            element.click();
                        }

                        String msg = "Successfully deselected checkbox '" + fieldName + "'.";
                        log.info(msg);
                        Assertions.writeResults("pass", msg, msg, false, "");
                    } else {
                        String msg = "Checkbox '" + fieldName + "' is already deselected.";
                        log.info(msg);
                        Assertions.writeResults("pass", msg, msg, false, "");
                    }
                }

            }
        } else if (action.equalsIgnoreCase("V")) {
            WebDriver.handleLoader();
            Assertions.validateCheckbox(locator, inputValue, fieldName, action);
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void handleAlert(String fieldName, String inputValue, String action) throws Exception {
        //printLog();
        try {
            if (action.equalsIgnoreCase("C")) {
                if (inputValue.equalsIgnoreCase("OK"))
                    WebDriverHelper.iDriver.switchTo().alert().accept();
                else if (inputValue.equalsIgnoreCase("Cancel"))
                    WebDriverHelper.iDriver.switchTo().alert().dismiss();

                String msg = fieldName + " : Successfully Clicked on '" + inputValue + "' button of alert";
                log.info(msg);
                Assertions.writeResults("pass", msg, msg, false, "");
            } else if (action.equalsIgnoreCase("I")) {
                WebDriverHelper.iDriver.switchTo().alert().sendKeys(inputValue);
                String msg = fieldName + " : Entered input '" + inputValue + "' in alert textbox";
                log.info(msg);
                Assertions.writeResults("pass", msg, msg, false, "");
            } else if (action.equalsIgnoreCase("V")) {
                Assertions.validateAlert(inputValue, fieldName);
            } else {
                String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        } catch (Exception e) {
            String msg = "Alert not found.";
            log.info(msg);
            Assertions.writeResults("fail", msg, msg, true, "hard");
        }
    }

    public static void handleDate(String fieldName, String inputValue, String action, String locator) throws Exception {
        //Thread.sleep(1000);
        new UiHandler().pleaseWaitDisappear();
        element = getElement(locator, fieldName, inputValue, true);
        //printLog();
        if (element != null) {
            unHighilightElement(element);

            //Getting the date field based on value mentioned in the Data source
            formatter = new SimpleDateFormat("MM/dd/yyyy");
            date = new Date();
            if (inputValue.equalsIgnoreCase("Current Date")) {
                dateValue = formatter.format(date).toString();
            } else if (inputValue.contains("Current Date +")) {
                int addDays;
                String[] addDate = inputValue.split("\\+");
                inputValue = addDate[1].trim();
                addDays = Integer.parseInt(inputValue);

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, addDays);
                dateValue = formatter.format(cal.getTime());
            } else if (inputValue.contains("Current date -")) {
                int subDays;
                String[] addDate = inputValue.split("\\-");
                inputValue = addDate[1].trim();
                subDays = Integer.parseInt(inputValue);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -subDays);
                dateValue = formatter.format(cal.getTime());
            } else {
                dateValue = inputValue;
            }

            if (action.equalsIgnoreCase("I")) {
                js.executeScript("arguments[0].value='';", element);
                element.sendKeys(dateValue);
                element.sendKeys(Keys.TAB);
                Thread.sleep(560);
                WebDriver.handleLoader();
                Assertions.validateDate(locator, dateValue, fieldName, action);
            } else if (action.equalsIgnoreCase("V")) {
                WebDriver.handleLoader();
                Assertions.validateDate(locator, dateValue, fieldName, action);
            } else {
                String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        }
    }

    public static void handleLabel(String fieldName, String inputValue, String action, String locator) throws Exception {
        element = getElement(locator, fieldName, inputValue, true);
        //printLog();
        new UiHandler().pleaseWaitDisappear();
        if (element != null) {
            unHighilightElement(element);
            if (action.equalsIgnoreCase("V")) {
                WebDriver.handleLoader();
                Assertions.validateLabel(element, inputValue, fieldName, action);
            } else if (action.equalsIgnoreCase("VF")) {
                WebDriver.handleLoader();
                Assertions.validateFormats(element, inputValue, fieldName);
            } else if (action.equalsIgnoreCase("VRF")) { //remove format and verify
                WebDriver.handleLoader();
                Assertions.validateLabel(element, inputValue, fieldName, action);
            } else if (action.equalsIgnoreCase("VP")) { //verify partial text
                WebDriver.handleLoader();
                Assertions.validateLabel(element, inputValue, fieldName, action);
            } else if (action.equalsIgnoreCase("VNE")) { //verify not equal
                WebDriver.handleLoader();
                Assertions.validateNotEqual(element, inputValue, fieldName, action);
            } else {
                String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        }
    }

    public static void iDropDown(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("I")) {
            element = getElement(locator, fieldName, inputValue, true);
            //printLog();
            if (element != null) {
                unHighilightElement(element);
                element.clear();
                if (!StringUtils.equalsIgnoreCase(inputValue, "_blank")) {
                    element.sendKeys(inputValue);
                    Thread.sleep(200);
                } else {
                    inputValue = "";
                }
                element.sendKeys(Keys.TAB);
                Thread.sleep(300);
                Assertions.validateIDropDown(locator, inputValue, fieldName, action);
            }
        } else if (action.equalsIgnoreCase("P")) {
            element = getElement(locator, fieldName, inputValue, true);
            element.clear();
            if (!StringUtils.equalsIgnoreCase(inputValue, "_blank")) {
                element.sendKeys(inputValue);
                Thread.sleep(200);
            }
            Thread.sleep(200);
            element.sendKeys(Keys.TAB);
            Assertions.validateIDropDown(locator, inputValue, fieldName, action);
        } else if (action.equalsIgnoreCase("V")) {
            if (inputValue.equalsIgnoreCase("_Present") || inputValue.equalsIgnoreCase("_NotPresent")) {
                String result[] = Assertions.validatePresenceOfField(locator, inputValue, fieldName);
                String status = result[0];
                String msg = result[1];
                String msgForExtent = msg;
                log.info(msg);
                if (status.equalsIgnoreCase("pass"))
                    Assertions.writeResults("pass", msg, msgForExtent, false, "");
                else
                    Assertions.writeResults("fail", msg, msgForExtent, true, "soft");
            } else {
                Assertions.validateDropdownValues(locator, inputValue, fieldName, action);
            }
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }

    }

    public static void handleUnqorkIntegrationField(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("I")) {
            String preLocator = locator + "/../div/span";
            String postLocator = locator + "/following-sibling::ul//li/div[3]";

            element = getElement(preLocator, fieldName, inputValue, false);
            element.click();
            element = getElement(locator, fieldName, inputValue, true);

            if (element != null) {
                //printLog();
                unHighilightElement(element);

                element.click();
                element.sendKeys(inputValue);
                WebElement listItem = getElement(postLocator, fieldName, inputValue, false);
                Thread.sleep(2000);
                listItem.click();

                //element.sendKeys(Keys.TAB);
                //Assertions.validateTextBox(locator, inputValue, fieldName, action);
            }
        } else if (action.equalsIgnoreCase("V")) {
            Assertions.validateTextBox(locator, inputValue, fieldName, action);
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void handleWait(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (StringUtils.equalsIgnoreCase(action, "IVS")) {
            log.info("Waiting for '" + locator + "' to be invisible.");
            try {
                for (int i = 1; i <= 1500; i++) {
                    List<WebElement> elementList = WebDriverHelper.iDriver.findElements(By.xpath(locator));
                    int elementCount = elementList.size();
                    if (elementCount > 0)
                        Thread.sleep(400);
                    else
                        break;
                }
            } catch (Exception e) {
            }

            Thread.sleep(500);
            log.info("Wait Completed.");
        } else {
            log.info("Waiting for " + action + " seconds.");
            Thread.sleep(Integer.parseInt(action) * 1000);
            String msg = "Waited for " + action + " seconds.";
            Assertions.writeResults("pass", msg, msg, false, "");
        }
    }

    public static void downloadFile(String fieldName, String inputValue, String action, String locator) throws Exception {
        //retrieve details from inputValue
        try {
            String[] splitInputValue = inputValue.split("\\|");
            String downloadedFileExt = "", renameToFileName = "", downloadedFileName = "";

            if (splitInputValue.length < 2) {
                String msg = fieldName + ": Incorrect input for FieldType 'Download File'. Input should be in the format of 'DownloadedFileExtension|RenameToFileNameWithExtension'.";
                log.info(msg);
                Assertions.writeResults("fail", msg, msg, false, "soft");
                return;
            }
            downloadedFileExt = splitInputValue[0];
            renameToFileName = splitInputValue[1];

            //delete existing files from DownloadFolder
            ExternalFileHandling.clearDownloadFolder();

            //Click on download button\link to start document download
            handleButton(fieldName, inputValue, "C", locator);

            //Retrieve downloaded file name if random name is expected


            File downloadDir = new File(Config.downloadPath);

            for (int i = 1; i < 90; i++) {
                File[] files = downloadDir.listFiles();
                Boolean fileFound = false;
                for (File file : files) {
                    String fileExtn = FilenameUtils.getExtension(file.getName());
                    if (fileExtn.equalsIgnoreCase(downloadedFileExt)) {
                        downloadedFileName = file.getName();
                        fileFound = true;
                        break;
                    }
                }
                if (fileFound)
                    break;
                else
                    Thread.sleep(1000);
            }

            //wait for file to download
            File dFile = new File(Config.downloadPath + "\\" + downloadedFileName);
            for (int i = 1; i < 90; i++) {
                if (dFile.exists())
                    break;
                else
                    Thread.sleep(1000);
            }

            //Rename the file
            File dFileWithNewName = new File(Config.downloadPath + "\\" + renameToFileName);
            dFile.renameTo(dFileWithNewName);
            Thread.sleep(200);

            //Create Downloads folder if not exists

            File downloadsFolder = new File(Config.downloadPath + "\\Downloads");
            if (!downloadsFolder.exists())
                downloadsFolder.mkdirs();

            //Delete file with same name from Downloads folder
            File existingActFile = new File(Config.downloadPath + "\\Downloads\\", renameToFileName);
            if (existingActFile.exists())
                existingActFile.delete();

            //Move file from download folder to downloads folder
            File destFile = new File(Config.downloadPath + "\\Downloads\\", renameToFileName);
            if (dFileWithNewName.renameTo(destFile))
                log.info("Moved the downloaded file to Downloads folder.");
            else
                log.info("Failed to move the file " + dFileWithNewName.getAbsolutePath());

            //create backup of new file
            String timeStampforFileBackup = new SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date());
            //File backUpFile=new File(destFile.getAbsolutePath().replace(".pdf", "_"+timeStampforFileBackup + ".pdf"));
            String fileExt = "." + FilenameUtils.getExtension(destFile.getName());
            File backUpFile = new File(destFile.getAbsolutePath().replace(fileExt, "_" + timeStampforFileBackup + fileExt));
            Files.copy(destFile, backUpFile);
            String msg = "File dowloaded successfully : " + destFile.getAbsolutePath();
            String msgForExt = "File dowloaded successfully. <a href='" + destFile.getAbsolutePath() + "'>Click here</a> to access the file.";
            Assertions.writeResults("pass", msg, msgForExt, false, "");
        } catch (Exception e) {
            String msg = "Issue while downloading file.";
            Assertions.writeResults("fail", msg, msg, true, "soft");
        }

    }

    public static void handleFileOperations(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (StringUtils.equalsIgnoreCase(action, "DV")) {
            downloadConvertCompare(fieldName, inputValue, action, locator);
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void downloadConvertCompare(String fieldName, String inputValue, String action, String locator) throws Exception {//only downloads to 'Download Path'.
        //retrieve details from inputValue
        try {
            String[] splitInputValue = inputValue.split("\\|");
            String downloadedFileExt = "", renameToFileName = "", downloadedFileName = "";

            if (splitInputValue.length < 2) {
                String msg = fieldName + ": Incorrect input for FieldType 'Download File'. Input should be in the format of 'DownloadedFileExtension|RenameToFileNameWithExtension'.";
                log.info(msg);
                Assertions.writeResults("fail", msg, msg, false, "soft");
                return;
            }
            downloadedFileExt = splitInputValue[0];
            renameToFileName = splitInputValue[1];

            //delete existing files from DownloadFolder
            ExternalFileHandling.clearDownloadFolder();

            //Click on download button\link to start document download
            handleButton(fieldName, inputValue, "C", locator);

            //Retrieve downloaded file name if random name is expected
            File downloadDir = new File(Config.downloadPath);

            for (int i = 1; i < 90; i++) {
                File[] files = downloadDir.listFiles();
                Boolean fileFound = false;
                for (File file : files) {
                    String fileExtn = FilenameUtils.getExtension(file.getName());
                    if (fileExtn.equalsIgnoreCase(downloadedFileExt)) {
                        downloadedFileName = file.getName();
                        fileFound = true;
                        break;
                    }
                }
                if (fileFound)
                    break;
                else
                    Thread.sleep(1000);
            }

            //wait for file to download
            File dFile = new File(Config.downloadPath + "\\" + downloadedFileName);
            for (int i = 1; i < 90; i++) {
                if (dFile.exists())
                    break;
                else
                    Thread.sleep(1000);
            }

            log.info("File " + downloadedFileName + " downloaded successfully to " + Config.downloadPath);
            //Rename the file or rename+convert the file
            //String downloadedFileExtension = FilenameUtils.getExtension(dFile.getAbsolutePath());
            String newFileExtension = FilenameUtils.getExtension(renameToFileName);
            File dFileWithNewName = null;
            if (downloadedFileExt.equalsIgnoreCase(newFileExtension)) {
                dFileWithNewName = new File(Config.downloadPath + "\\" + renameToFileName);
                dFile.renameTo(dFileWithNewName);
            } else {
                File downloadsFolder = new File(Config.downloadPath + "\\Downloads");
                if (!downloadsFolder.exists())
                    downloadsFolder.mkdirs();
                String timeStampforFileBackup = new SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date());
                File backUpFile = new File(downloadsFolder.getAbsolutePath() + "\\" + renameToFileName.substring(0, renameToFileName.lastIndexOf(".")) + "_" + timeStampforFileBackup + "." + downloadedFileExt);
                Files.copy(dFile, backUpFile);
                log.info("Downloaded file copied to Downloads folder :" + backUpFile.getAbsolutePath());
                switch (downloadedFileExt.toLowerCase()) {
                    case "docm":
                        log.info("Converting docm file to pdf.");
                        ExternalFileHandling.convertDocmToPdf(dFile.getAbsolutePath(), Config.downloadPath + "\\" + renameToFileName);
                        Thread.sleep(200);
                        dFileWithNewName = new File(Config.downloadPath + "\\" + renameToFileName);
                        if (dFile.exists()) {
                            dFile.delete();
                        }
                        log.info("docm File successfully converted to pdf : " + Config.downloadPath + "\\" + renameToFileName);
                        break;
                    case "html":
                        log.info("Converting html file to pdf.");
                        HtmlLoadOptions htmloptions = new HtmlLoadOptions();
                        Document doc = new Document(Config.downloadPath + "\\" + downloadedFileName, htmloptions);
                        doc.save(Config.downloadPath + "\\" + renameToFileName);
                        Thread.sleep(200);
                        dFileWithNewName = new File(Config.downloadPath + "\\" + renameToFileName);
                        if (dFile.exists()) {
                            dFile.delete();
                        }
                        log.info("html File successfully converted to pdf : " + Config.downloadPath + "\\" + renameToFileName);
                        break;
                    default:
                        throw new Exception(downloadedFileExt + " to pdf converversion not configured.");
                }
            }
            Thread.sleep(200);

            handlePDFCompare(renameToFileName, null);

				/*String msg="File dowloaded successfully : " + dFileWithNewName.getAbsolutePath();
				String msgForExt="File dowloaded successfully("+ dFileWithNewName.getAbsolutePath() +").";
				Assertions.writeResults("pass", msg, msgForExt, false,"");*/
        } catch (Exception e) {
            String msg = "Issue while downloading file." + e.getMessage();
            log.error(msg);
            Assertions.writeResults("fail", msg, msg, true, "soft");
        }
    }

    public static void downloadRenameComparePDF(String fieldName, String inputValue, String action, String locator) throws Exception {

        //retrieve details from inputValue
        String[] splitInputValue = inputValue.split("\\|");
        String downloadedFileName = "", renameToFileName = "", exclusionString = "";
        String[] exclusionStringArray = null;
        if (splitInputValue.length < 2) {
            String msg = fieldName + ": Incorrect input for FieldType 'PDFCompare'. Input should be in the format of 'DownloadedFileName|RenameToFileName|exclusionString(optional)'.";
            log.info(msg);
            Assertions.writeResults("fail", msg, msg, false, "soft");
            return;
        }
        downloadedFileName = splitInputValue[0];
        renameToFileName = splitInputValue[1];
        if (splitInputValue.length > 2) {
            exclusionString = splitInputValue[2];
            exclusionStringArray = exclusionString.split(";;");
        }

        //delete existing files from DownloadFolder
        ExternalFileHandling.clearDownloadFolder();

        //Click on download button\link to start document download
        handleButton(fieldName, inputValue, "C", locator);


        //Retrieve downloaded file name if random name is expected

        if (downloadedFileName.equalsIgnoreCase("_ANYNAME")) {
            File downloadDir = new File(Config.downloadPath);

            for (int i = 1; i < 90; i++) {
                File[] files = downloadDir.listFiles();
                Boolean fileFound = false;
                for (File file : files) {
                    String fileExtn = FilenameUtils.getExtension(file.getName());
                    if (fileExtn.equalsIgnoreCase("pdf")) {
                        downloadedFileName = file.getName();
                        fileFound = true;
                        break;
                    }
                }
                if (fileFound)
                    break;
                else
                    Thread.sleep(1000);
            }

        }

        //wait for file to download
        File dFile = new File(Config.downloadPath + "\\" + downloadedFileName);
        for (int i = 1; i < 90; i++) {
            if (dFile.exists())
                break;
            else
                Thread.sleep(1000);
        }

        //Rename the file
        File dFileWithNewName = new File(Config.downloadPath + "\\" + renameToFileName);
        dFile.renameTo(dFileWithNewName);
        Thread.sleep(200);

        handlePDFCompare(renameToFileName, exclusionStringArray);
        /* Moved to method 'handlePDFCompare'
         * //Delete file with same name from Actual folder File existingActFile=new
         * File(Config.pdfCompPath+"\\Actual\\", renameToFileName);
         * if(existingActFile.exists()) existingActFile.delete();
         *
         * //Move file from download folder to actual folder File destFile=new
         * File(Config.pdfCompPath+"\\Actual\\", renameToFileName);
         * if(dFileWithNewName.renameTo(destFile))
         * log.info("Moved the downloaded file to actual folder."); else
         * log.info("Failed to move the file "+dFileWithNewName.getAbsolutePath());
         *
         * //create backup of new file String timeStampforFileBackup=new
         * SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date()); File backUpFile=new
         * File(destFile.getAbsolutePath().replace(".pdf", "_"+timeStampforFileBackup +
         * ".pdf")); Files.copy(destFile, backUpFile);
         *
         * //call PDF Compare String
         * expFile=Config.pdfCompPath+"\\Expected\\"+renameToFileName; String
         * actFile=Config.pdfCompPath+"\\Actual\\"+renameToFileName; String
         * resPath=Config.pdfCompPath+"\\MismatchResult\\"+Report.getTestCaseId()+"\\
         * "+Report.getTransactionName().replace("#", "_")+"\\"+new SimpleDateFormat("
         * ddMMMyy_HH_mm_ss").format(new Date());
         *
         * String res[] = ExternalFileHandling.comparePDFNew(expFile, actFile,
         * exclusionStringArray, resPath); //String res[] =
         * ExternalFileHandling.comparePDF(expFile, actFile, exclusionStringArray,
         * resPath); //String messageForExtent=res[1].replaceAll("\n", "<br>");
         * if(res[0].equalsIgnoreCase("Pass")) { log.info(res[1]);
         * Assertions.writeResults("pass", res[1], res[2], false,""); }else {
         * log.info(res[1]); Assertions.writeResults("fail", res[1], res[2],
         * false,"soft"); }
         */
    }

    public static void handlePDFCompare(String compareFileName, String[] exclusionStringArray) throws Exception {
        File dFileWithNewName = new File(Config.downloadPath + "\\" + compareFileName);

        //Delete file with same name from Actual folder
        File existingActFile = new File(Config.pdfCompPath + "\\Actual\\", compareFileName);
        if (existingActFile.exists())
            existingActFile.delete();

        //Move file from download folder to actual folder
        File destFile = new File(Config.pdfCompPath + "\\Actual\\", compareFileName);
        if (dFileWithNewName.renameTo(destFile))
            log.info("Moved the downloaded file to actual folder.");
        else
            log.info("Failed to move the file " + dFileWithNewName.getAbsolutePath());

        //create backup of new file
        String timeStampforFileBackup = new SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date());
        File backUpFile = new File(destFile.getAbsolutePath().replace(".pdf", "_" + timeStampforFileBackup + ".pdf"));
        Files.copy(destFile, backUpFile);

        //call PDF Compare
        String expFile = Config.pdfCompPath + "\\Expected\\" + compareFileName;
        String actFile = Config.pdfCompPath + "\\Actual\\" + compareFileName;
        String resPath = Config.pdfCompPath + "\\MismatchResult\\" + Report.getTestCaseId() + "\\" + Report.getTransactionName().replace("#", "_") + "\\" + new SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date());

        log.info("PDF comparison in progress.");
        try {
            //String res[] = ExternalFileHandling.comparePDFNew(expFile, actFile, exclusionStringArray, resPath);
            String res[] = ExternalFileHandling.divideAndComparePDF(expFile, actFile, exclusionStringArray, resPath);
            log.info("PDF comparison completed.");
            //String res[] = ExternalFileHandling.comparePDF(expFile, actFile, exclusionStringArray, resPath);
            //String messageForExtent=res[1].replaceAll("\n", "<br>");
            if (res[0].equalsIgnoreCase("Pass")) {
                log.info(res[1]);
                Assertions.writeResults("pass", res[1], res[2], false, "");
            } else {
                log.info(res[1]);
                Assertions.writeResults("fail", res[1], res[2], false, "soft");
            }
        } catch (Exception e) {
            String msg = "Issue during PDF comparison: " + e.getMessage();
            log.error(msg);
            Assertions.writeResults("fail", msg, msg, false, "soft");
        }
    }

    public static void handleXMLCompare(String fieldName, String inputValue, String action, String locator) throws Exception {

        //To be corrected
        //retrieve details from inputValue
        String[] splitInputValue = inputValue.split("\\|");
        String downloadedFileName = "", renameToFileName = "", tagsToExclude = "", attributesToExclude = "";
        String[] tagsToExcludeArray = null;
        if (splitInputValue.length < 2) {
            String msg = fieldName + ": Incorrect input for FieldType 'XMLCompare'. Input should be in the format of 'DownloadedFileName|RenameToFileName|TagsToExclude(Optional)|AttributesToExclude(Optional)'.";
            log.info(msg);
            Assertions.writeResults("fail", msg, msg, false, "soft");
            return;
        }
        downloadedFileName = splitInputValue[0];
        renameToFileName = splitInputValue[1];
        if (splitInputValue.length > 2) {
            tagsToExclude = splitInputValue[2];
            tagsToExcludeArray = tagsToExclude.split(",");
        }

        //delete existing files from DownloadFolder
        ExternalFileHandling.clearDownloadFolder();

        //Click on download button\link to start document download
        handleButton(fieldName, inputValue, "C", locator);


        //Retrieve downloaded file name if random name is expected
        if (downloadedFileName.equalsIgnoreCase("_ANYNAME")) {
            File downloadDir = new File(Config.downloadPath);

            for (int i = 1; i < 90; i++) {
                File[] files = downloadDir.listFiles();
                Boolean fileFound = false;
                for (File file : files) {
                    String fileExtn = FilenameUtils.getExtension(file.getName());
                    if (fileExtn.equalsIgnoreCase("xml")) {
                        downloadedFileName = file.getName();
                        fileFound = true;
                        break;
                    }
                }
                if (fileFound)
                    break;
                else
                    Thread.sleep(1000);
            }

        }

        //wait for file to download
        File dFile = new File(Config.downloadPath + "\\" + downloadedFileName);
        for (int i = 1; i < 90; i++) {
            if (dFile.exists())
                break;
            else
                Thread.sleep(1000);
        }

        //Rename the file
        File dFileWithNewName = new File(Config.downloadPath + "\\" + renameToFileName);
        dFile.renameTo(dFileWithNewName);
        Thread.sleep(200);

        //Delete file with same name from Actual folder
        File existingActFile = new File(Config.xmlCompPath + "\\Actual\\", renameToFileName);
        if (existingActFile.exists())
            existingActFile.delete();

        //Move file from download folder to actual folder
        File destFile = new File(Config.xmlCompPath + "\\Actual\\", renameToFileName);
        if (dFileWithNewName.renameTo(destFile))
            log.info("Moved the downloaded file to actual folder.");
        else
            log.info("Failed to move the file " + dFileWithNewName.getAbsolutePath());

        //create backup of new file
        String timeStampforFileBackup = new SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date());
        File backUpFile = new File(destFile.getAbsolutePath().replace(".xml", "_" + timeStampforFileBackup + ".xml"));
        Files.copy(destFile, backUpFile);

        //call PDF Compare
        String expFile = Config.xmlCompPath + "\\Expected\\" + renameToFileName;
        String actFile = Config.xmlCompPath + "\\Actual\\" + renameToFileName;
        String resPath = Config.xmlCompPath + "\\MismatchResult\\" + Report.getTestCaseId() + "\\" + Report.getTransactionName() + "\\" + new SimpleDateFormat("ddMMMyy_HH_mm_ss").format(new Date());

        /*
         * String res[] = ExternalFileHandling.compareXML(expFile, actFile,
         * tagsToExcludeArray, tagsToExcludeArray, resPath); //String
         * messageForExtent=res[1].replaceAll("\n", "<br>");
         * if(res[0].equalsIgnoreCase("Pass")) { log.info(res[1]);
         * Assertions.writeResults("pass", res[1], res[2], false,""); }else {
         * log.info(res[1]); Assertions.writeResults("fail", res[1], res[2],
         * false,"soft"); }
         */

    }

    public static void handleFileUpload(String fieldName, String inputValue, String action, String locator) throws Exception {
        element = getElement(locator, fieldName, inputValue, true);
        if (element != null) {
            if (action.equalsIgnoreCase("I")) {
                element.sendKeys(inputValue);
                String msg = "Attachment '" + inputValue + "' uploaded successfully.";
                log.info(msg);
                Assertions.writeResults("pass", msg, msg, false, "");
            } else {
                String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        }

    }

    public static void handleCalculations(String fieldName, String inputValue, String action, String locator) throws Exception {
        String[] actionSplit = action.split(";;");
        action = actionSplit[0];
        if (action.equalsIgnoreCase("V")) {
            if (!locator.equalsIgnoreCase("NA")) {
                element = getElement(locator, fieldName, inputValue, true);
                String actualValue = Assertions.getElementValue(element);
                String expectedValue = "";
                String verificationKeyword = inputValue.toUpperCase();
                if (verificationKeyword.contains("CALC")) {
                    verificationKeyword = "CALC_PARTIAL";
                }
                /*
                 * if(verificationKeyword.startsWith("CALC")) { verificationKeyword="CALC"; }
                 */
                if (verificationKeyword.startsWith("ASIS")) {
                    verificationKeyword = "ASIS";
                }
                switch (verificationKeyword) {
                    case "BIL_INITIAL_INSTALL1":
                    case "BIL_INITIAL_INSTALL2":
                    case "BIL_INITIAL_INSTALL3":
                    case "BIL_INITIAL_INSTALL4":
                    case "BIL_INITIAL_INSTALL5":
                    case "BIL_INITIAL_INSTALL6":
                    case "BIL_INITIAL_INSTALL7":
                    case "BIL_INITIAL_INSTALL8":
                    case "BIL_INITIAL_INSTALL9":
                    case "BIL_INITIAL_INSTALL10":
                        expectedValue = ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), verificationKeyword);
                        actualValue = Calculations.removeFormattingFromNumber(actualValue);
                        if (actualValue.equals(expectedValue)) {
                            String msg = "Expected and actual values matched. Expected Value: " + expectedValue + " Actual Value:  " + actualValue;
                            log.info(msg);
                            Assertions.writeResults("pass", msg, msg, false, "");
                        } else {
                            String msg = "Mismatch in expected and actual. Expected: " + expectedValue + ", Actual: " + actualValue;
                            String msgForExtent = "Mismatch in expected and actual. <b>Expected Value: </b>" + expectedValue + ", <b>Actual Value:</b> " + actualValue;
                            log.info(msg);
                            Assertions.writeResults("fail", msg, msgForExtent, true, "soft");
                        }
                        break;

                    case "_BLANK":
                        //actualValue = Assertions.getElementValue(element);
                        Assertions.validateEquality(inputValue, actualValue);
                        break;
                    case "_GETACCOUTSTANDING":
                        //actualValue = Assertions.getElementValue(element);
                        actualValue = Calculations.removeFormattingFromNumber(actualValue);
                        String accOutstandingFromDC = ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), "AccOutstanding");
                        Assertions.validateEquality(accOutstandingFromDC, actualValue);
                        break;

                    /*
                     * case "CALC":
                     * actualValue=Calculations.removeFormattingFromNumber(actualValue);
                     * //inputValue=inputValue.trim().replace("CALC(", "");
                     * //inputValue=inputValue.replace(")","");
                     * //expectedValue=String.format("%.2f",
                     * Calculations.identifyAndEvaluateOperation(inputValue)); try {
                     * expectedValue=String.format("%.2f",
                     * Calculations.identifyAndEvaluateExpresssion(inputValue));
                     * Assertions.validateEquality(expectedValue, actualValue); }catch(Exception e)
                     * { Assertions.writeResults("fail", e.getMessage(), e.getMessage(),
                     * false,"soft"); } break;
                     */
                    case "ASIS":
                        inputValue = inputValue.trim().replace("AsIs(", "");
                        inputValue = inputValue.replace(")", "");
                        Assertions.validateEquality(inputValue, actualValue);
                        break;

                    case "CALC_PARTIAL":
                        actualValue = Calculations.removeFormattingFromNumber(actualValue);
                        try {
                            int expStartIndex = inputValue.indexOf("CALC");
                            int expEndIndex = inputValue.indexOf("|", expStartIndex) + 1;
                            String expressionToEval = inputValue.substring(expStartIndex, expEndIndex);
                            String expResult = String.format("%.2f", Calculations.identifyAndEvaluateExpresssion(expressionToEval));
                            expectedValue = inputValue.substring(0, expStartIndex) + expResult + inputValue.substring(expEndIndex);
                            Assertions.validateEquality(expectedValue, actualValue);
                        } catch (Exception e) {
                            Assertions.writeResults("fail", e.getMessage(), e.getMessage(), false, "soft");
                        }
                        break;
                    default:
                        actualValue = Calculations.removeFormattingFromNumber(actualValue);
                        Assertions.validateEquality(inputValue, actualValue);
                        /*
                         * String msg="Keyword '" + inputValue +
                         * "' is not defined under FieldType 'Calculations'."; log.info(msg);
                         * Assertions.writeResults("fail", msg, msg, false,"soft");
                         */
                        break;
                }
            }
        } else if (action.equalsIgnoreCase("EW")) {
            if (actionSplit.length < 2) {
                String msg = fieldName + ": Incorrect action against FieldType 'Calculations-EW' in Object Repository. Action should be in the format of 'EW;;ColumnNameFromDataCarrier'.";
                log.info(msg);
                Assertions.writeResults("fail", msg, msg, false, "soft");
                return;
            }
            action = actionSplit[0];
            String columnNameInDC = actionSplit[1];

            //if(locator.equalsIgnoreCase("_PayOnAcc")) {
            //Calculations.calcAccOutstandingAfterPayment(fieldName, inputValue, columnNameInDC, locator);
            //}else if(locator.equalsIgnoreCase("_PremiumOnAcc")){
            //Calculations.calcAccOutstandingAfterPremiumChange(fieldName, inputValue, columnNameInDC, locator);
            //}else
            if (locator.equalsIgnoreCase("_DeriveAccOutstanding")) {
                Calculations.deriveAccountOutstanding(fieldName, inputValue, columnNameInDC, locator);
            } else if (locator.equalsIgnoreCase("_DeriveAccFee1")) {
                Calculations.deriveAccFee1(fieldName, inputValue, columnNameInDC, locator);
            } else {
                String valueToWriteInDC = "Not Found\\Empty";
                //retrive value to write
                if (!locator.equalsIgnoreCase("NA")) {
                    element = getElement(locator, fieldName, inputValue, true);
                    valueToWriteInDC = Assertions.getElementValue(element);
                } else {
                    if (inputValue.toUpperCase().startsWith("CALC")) {
                        valueToWriteInDC = String.format("%.2f", Calculations.identifyAndEvaluateExpresssion(inputValue));
                    } else if (inputValue.equalsIgnoreCase("_GETACCOUTSTANDING")) {
                        valueToWriteInDC = ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), "AccOutstanding");
                    } else {
                        valueToWriteInDC = inputValue;
                    }
                }
                //remove $ and comma formatting from number
                if (columnNameInDC.endsWith("[RF]")) {
                    valueToWriteInDC = Calculations.removeFormattingFromNumber(valueToWriteInDC);
                    columnNameInDC = columnNameInDC.replace("[RF]", "");
                }
                //remove $ from number
                if (columnNameInDC.endsWith("[RC]")) {
                    valueToWriteInDC = Calculations.removeCurrencyFromNumber(valueToWriteInDC);
                    columnNameInDC = columnNameInDC.replace("[RC]", "");
                }

                //write to data carrier
                try {
                    String tcToWriteInDC = Report.getTestCaseId();
                    ExcelUtilities.writeToDataCarrier(tcToWriteInDC, columnNameInDC, valueToWriteInDC);
                    String msg = "Value '" + valueToWriteInDC + "' successfully written in Data Carrier file against TC '" + tcToWriteInDC + "' and column '" + columnNameInDC + "'.";
                    log.info(msg);
                    Assertions.writeResults("pass", msg, msg, false, "");
                } catch (Exception e) {
                    Base.sa.fail();
                    String msg = "Unable to write to Data Carrier.";
                    log.info(msg);
                    Assertions.writeResults("fail", msg, msg, false, "soft");
                }
            }
        } else if (action.equalsIgnoreCase("EI")) {//evaluate and input
            String finalInputStr = inputValue;
            String inputKeyword = inputValue.toUpperCase();
            if (inputKeyword.startsWith("CALC")) {
                inputKeyword = "CALC";
            }
            switch (inputKeyword) {
                case "CALC":
                    //inputValue=inputValue.trim().replace("CALC(", "");
                    //inputValue=inputValue.replace(")","");
                    Double finalInput = Calculations.identifyAndEvaluateExpresssion(inputValue);
                    //finalInputStr=String.format("%.2f", Calculations.identifyAndEvaluateExpresssion(inputValue));
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    decimalFormat.setGroupingUsed(true);
                    decimalFormat.setGroupingSize(3);
                    finalInputStr = decimalFormat.format(finalInput);
                    //NumberFormat numberFormat = NumberFormat.getInstance();
                    //finalInputStr= numberFormat.format(finalInputStr);
                    log.info("Evaluated input is " + finalInputStr);
                    break;
                default:
                    log.info("No evaluation keyword. Input is " + finalInputStr);
                    break;
            }
            handleTextBox(fieldName, finalInputStr, "I", locator);
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void handleTooltip(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("V")) {
            Assertions.validateTooltip(locator, inputValue, fieldName, action);
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void takeScreenshot(String fieldName, String inputValue, String action, String locator) throws Exception {
        Boolean screenshotTaken = Report.captureSelectiveScreenshot();
        if (screenshotTaken) {
            String msg = "Screenshot captured.";
            log.info(msg);
            test.log(Status.PASS, "<b>" + Controller.transactionName + " : " + Report.getFieldName() + " : " + "</b>" + msg + "</br>", MediaEntityBuilder.createScreenCaptureFromPath(Report.screenshotPath).build());
        } else
            log.error("Issue while capturing screenshot.");
    }

    public static void handleListVerification(String fieldName, String inputValue, String action, String locator) throws Exception {
        //verify presence(VP) of element in list
        if (action.equalsIgnoreCase("VP")) {
            WebDriver.handleLoader();
            Assertions.validatePresenceOfElementInList(fieldName, inputValue, locator);
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }

    public static void handleDatabaseOperations(String fieldName, String inputValue, String action, String locator) throws Exception {
        if (action.equalsIgnoreCase("Compare_SingleRecord")) {
            String[] splitInput = inputValue.trim().split(";;");
            if (splitInput.length < 3) {
                String msg = fieldName + ": Incorrect input for FieldType 'Database-CompareSingleRecord'. Input should be in the format of 'DB Name;;Query To retrive single result;;Expected Query Result'.";
                log.info(msg);
                Assertions.writeResults("fail", msg, msg, false, "soft");
                return;
            }
            String dbName = splitInput[0].trim();
            String query = splitInput[1].trim();
            String expectedQResult = splitInput[2].trim();
            try {
                String actualQResult = DatabaseHandling.singleResultQuery(dbName, query);
                Assertions.validateEquality(expectedQResult, actualQResult);
            } catch (SQLException e) {
                e.printStackTrace();
                String msg = "No results retrieved for query '" + query + "' on '" + dbName + "' database";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = "";
                if (e.getMessage().equalsIgnoreCase("MoreThanOneResult"))
                    msg = "More than one result retrieved for query '" + query + "' on '" + dbName + "' database. Expected Result=" + expectedQResult;
                else
                    msg = "Issue while querying database. Database='" + dbName + "', Query='" + query + "'";
                Assertions.writeResults("fail", msg, msg, false, "soft");
                log.info(msg);
            }
        } else {
            String msg = "Unable to find fieldType-action combination : '" + Report.getFieldType() + "-" + action + "'. Update the Object Repository.";
            Assertions.writeResults("fail", msg, msg, false, "soft");
            log.info(msg);
        }
    }
}
