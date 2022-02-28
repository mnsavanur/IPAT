package com.lti.webDriver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.dataProviders.DataProviders;

public class WebDriverHelper extends Base {
    static Logger log = Logger.getLogger(WebDriverHelper.class.getName());
    public static String browserName = null;
    public static WebDriver iDriver = null;
    public static WebDriverWait wait;
    public static JavascriptExecutor js;
    public static String webDriverPath = System.getProperty("user.dir") + "\\dependencies\\webDrivers";

    public static void launchDriver(String browserName,String appURL) throws Exception {
        killWebDriver(browserName);
        if (browserName.equalsIgnoreCase("Chrome") || browserName.equalsIgnoreCase("Google Chrome")) {
            launchChrome();
        } else if (browserName.equalsIgnoreCase("IE") || browserName.equalsIgnoreCase("Internet Explorer")) {
            launchInternetExplorer();
        } else if (browserName.equalsIgnoreCase("Edge") || browserName.equalsIgnoreCase("Microsoft Edge")) {
            launchMicrosoftEdge();
        } else {
            Exception ex = new RuntimeException("Unable to find webdriver for the browser: " + browserName);
            log.error("Unable to launch browser", ex);
        }
        iDriver.manage().window().maximize();
        iDriver.manage().deleteAllCookies();
        //iDriver.get(Config.applicationUrl);
        iDriver.get(appURL);

        //browserName = Config.webBrowser;

        //Thread.sleep(10000);
        wait = new WebDriverWait(iDriver, Config.timeOut);
        js = (JavascriptExecutor) iDriver;

    }

    public static void launchDriver2(String browserName, String appURL,String executionFlag) throws Exception {
        //browserName = Config.webBrowser;

        //Thread.sleep(10000);
        wait = new WebDriverWait(iDriver,Config.timeOut);
        js = (JavascriptExecutor)iDriver;

    }


    public static WebDriver launchChrome() {

        try {
            System.setProperty("webdriver.chrome.driver", webDriverPath + "\\chromedriver.exe");

            HashMap<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("profile.default_content_setting_values.notifications", 2);
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            if (!Config.downloadPath.isEmpty())
                prefs.put("download.default_directory", Config.downloadPath);
            prefs.put("profile.default_content_settings.popups", 0);
            prefs.put("safebrowsing.enabled", true);
            prefs.put("plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"});
            prefs.put("plugins.always_open_pdf_externally", true);

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-infobars");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-popup-blocking");

            iDriver = new ChromeDriver(options);
            System.out.println("Launching Chrome");
            log.info("Launching Google Chrome web browser....");


        } catch (Exception ex) {
            log.error("Unable to launch Google Chrome", ex);
            //ex.printStackTrace();
        }

        return iDriver;

    }

    public static WebDriver launchInternetExplorer() {

        System.setProperty("webdriver.ie.driver", webDriverPath + "\\IEDriverServer.exe");
        iDriver = new InternetExplorerDriver();
        log.info("Launching Internet Explorer web browser....");
        return iDriver;

    }

    public static WebDriver launchMicrosoftEdge() {

        System.setProperty("webdriver.edge.driver", webDriverPath + "\\msedgedriver.exe");

        EdgeOptions options = new EdgeOptions();
        if (!Config.downloadPath.isEmpty())
            options.setCapability("download.default_directory", Config.downloadPath);

        iDriver = new EdgeDriver(options);
        log.info("Launching Microsoft Edge web browser....");
        return iDriver;

    }

    private static void killWebDriver(String browserName) throws Exception {

        if (browserName.equalsIgnoreCase("Chrome") || browserName.equalsIgnoreCase("Google Chrome")) {
            Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
            Thread.sleep(1000);
            log.info("Killed previous instances of Chrome WebDriver");
        } else if (browserName.equalsIgnoreCase("IE") || browserName.equalsIgnoreCase("Internet Explorer")) {
            Runtime.getRuntime().exec("taskkill /F /IM iexplore.exe /T");
            Thread.sleep(1000);
            log.info("Killed previous instances of Internet Explorer WebDriver");
        } else if (browserName.equalsIgnoreCase("Edge") || browserName.equalsIgnoreCase("Microsoft Edge")) {
            Runtime.getRuntime().exec("taskkill /F /IM msedgedriver.exe /T");
            Thread.sleep(1000);
            log.info("Killed previous instances of Microsoft Edge WebDriver");
        }

    }

    public static void quitWebDriver() throws Exception {
        try {
            Thread.sleep(1000);
            iDriver.quit();
            log.info("Closed the web driver session successfully....");
        } catch (Exception ex) {

        }

        //Thread.sleep(4000);
    }

    public static void captureEntireScreenshot() {

    }


}
