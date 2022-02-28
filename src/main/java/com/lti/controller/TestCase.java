package com.lti.controller;

import com.aventstack.extentreports.Status;
import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.TestUtil.Report;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.controller.Controller;
import com.lti.dataProviders.DataProviders;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.testng.SkipException;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class TestCase extends Base {
    static Logger log = Logger.getLogger(TestCase.class.getName());
    public static Sheet testSuiteSheet;
    public static HashMap<String, Integer> colHeader;
    public static HashMap<Integer, Object> rowData;
    public int lastRowIndex;
    public int currentRowCellCount;
    public static boolean executeTC = false;

    @Test(dataProvider = "Test Suite Iterator", dataProviderClass = DataProviders.class)

    public void Test(Hashtable<Integer, String> data) throws Exception {
        executeTC = false;
        String executionFlag = data.get("ExecutionFlag").trim();
        Report.setTestCaseExecuteFlag(executionFlag);
        if (executionFlag.equalsIgnoreCase("Y")) {
            executeTC = true;
            log.info("*************** Starting execution of " + data.get("TestCaseID") + " ***************");
            Controller.loadTransactions(data.get("TestCaseID"));
            sAssert.assertAll();

        } else if (executionFlag.equalsIgnoreCase("Cont")) {
            executeTC = true;
            log.info("*************** Starting execution of " + data.get("TestCaseID") + " ***************");
            Controller.loadTransactions2(data.get("TestCaseID"),executionFlag);
            sAssert.assertAll();
        } else if (executionFlag.equalsIgnoreCase("") || executionFlag.equalsIgnoreCase("N")) {
            log.info("*************** Skipping execution of " + data.get("TestCaseID") + " ***************");
            executeTC = false;
            throw new SkipException("Skipping execution of " + data.get("TestCaseID"));

        }
    }

}
