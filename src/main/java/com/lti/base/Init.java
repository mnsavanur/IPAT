package com.lti.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;

import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.TestUtil.Report;
import com.lti.controller.Controller;
import com.lti.webDriver.UiHandler;
import com.lti.webDriver.WebDriverHelper;

public class Init {
	static Logger log = Logger.getLogger(Init.class.getName());
	public static Sheet testSuiteSheet;
	public static HashMap<String, Integer> colHeader;
	public static HashMap<Integer, Object> rowData;
	public static Sheet transSheet=null;
	public static HashMap<String,Integer> colMap =null; 
	public static int lastRowIndex;
	
	public int currentRowCellCount;

	public static void initConfigFile(String filePath, String sheetName) throws Exception {
		log.info("Closing all excel files..");
		Report.killProcess("EXCEL.EXE");
		Config.readConfigFile(filePath, sheetName);
		cleanTempDir();
		takeBackup();
		//initBrowser();
	}

	/*
	 * Date - 11/08/2020 Author - Vipin Phogat Description - The below method is
	 * responsible for cleaning the temp directory
	 */

	public static void cleanTempDir() {
		Properties properties = System.getProperties();
		String tempDir = properties.getProperty("java.io.tmpdir");
		File file = new File(tempDir);
		clearFolder(file);
	}
	
	private static void takeBackup()throws IOException {
		File backUpFolderPath=new File(Report.backUpFolderPath);
		File dataCarrierBackUp=new File(Report.backUpFolderPath+"\\"+Config.dataCarrier.substring(Config.dataCarrier.lastIndexOf("\\")));
		File dataCarrierFilePath= new File(Config.dataCarrier);

		if(dataCarrierFilePath.exists() && backUpFolderPath.exists()) {
			Files.copy(dataCarrierFilePath.toPath(), dataCarrierBackUp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	public static void clearFolder(File directoryPath) {
		try {
			if(directoryPath.isDirectory()) {
				if(directoryPath.list().length==0)
					directoryPath.delete();
				else{
					File[] files = directoryPath.listFiles();
					for(File file : files) {
						clearFolder(file);
					}
				}
			}else {
				directoryPath.delete();
			}
		} catch (Exception ex) {}
	}





}
