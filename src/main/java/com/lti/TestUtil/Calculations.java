package com.lti.TestUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;

import com.lti.ExcelUtil.ExcelUtilities;
import com.lti.base.Base;
import com.lti.customPackage.CustomClass;
import com.lti.webDriver.Assertions;

/*
 * Date - 11/5/2021
 * Author - Sheetal Jadhav
 * Description - Provision of arithmetic operations
*/
public class Calculations {
	static Logger log = Logger.getLogger(Calculations.class.getName());

	public static double getAccOutstanding() throws Exception {
		String polPremStringValue = ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), "AccOutstanding");
		polPremStringValue = removeFormattingFromNumber(polPremStringValue);
		return Double.parseDouble(polPremStringValue);
	}

	public static String removeFormattingFromNumber(String number) {
		return (number.replaceAll("[$,]", ""));
	}

	public static String removeCurrencyFromNumber(String number) {
		return (number.replaceAll("[$]", ""));
	}

	public static double getMultiplierFromPercent(String percent) {
		percent = percent.replaceAll("[%,]", "");
		return (Double.parseDouble(percent) / 100);
	}

	public static void calcAccOutstandingAfterPayment(String fieldName, String inputValue, String columnNameInDC,
			String locator) throws Exception {
		Double accOutstandingPriorToPayment = Double
				.parseDouble(ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), "AccOutstanding"));
		Double paymentAmount = Double.parseDouble(Calculations.removeFormattingFromNumber(
				ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), "PaymentAmount")));
		Double accOutstandingAfterPayment = accOutstandingPriorToPayment - paymentAmount;
		String accOutstandingAfterPaymentStr = String.format("%.2f", accOutstandingAfterPayment);
		try {
			String tcToWriteInDC = Report.getTestCaseId();
			ExcelUtilities.writeToDataCarrier(tcToWriteInDC, columnNameInDC, accOutstandingAfterPaymentStr);
			String msg = "Value '" + accOutstandingAfterPaymentStr
					+ "' successfully written in Data Carrier file against TC '" + tcToWriteInDC + "' and column '"
					+ columnNameInDC + "'.";
			log.info(msg);
			Assertions.writeResults("pass", msg, msg, false, "");
		} catch (Exception e) {
			Base.sa.fail();
			String msg = "Unable to write " + columnNameInDC + " to Data Carrier.";
			log.info(msg);
			Assertions.writeResults("fail", msg, msg, false, "soft");
		}

	}

	public static void deriveAccFee1(String fieldName, String inputValue, String columnNameInDC, String locator)
			throws Exception {
		String[] policyCount = inputValue.split(";;");
		Double accFee = 0.00;
		String accFeeStr = "";
		for (int i = 0; i < policyCount.length; i++) {
			int policyId = Integer.parseInt(policyCount[i].trim().toLowerCase().replace("pol", ""));
			String columnToRead = "Fee1_Pol" + policyId;
			String polFeeStringValue = "0";
			try {
				polFeeStringValue = ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), columnToRead);
				polFeeStringValue = removeFormattingFromNumber(polFeeStringValue);
			} catch (Exception e) {
				polFeeStringValue = "0";
				log.info("Column '" + columnToRead
						+ "' is not present in Data Carrier file. Considered value 0.00 for calculation.");
			}
			accFee = accFee + Double.parseDouble(polFeeStringValue);
			accFeeStr = String.format("%.2f", accFee);
		}
		try {
			String tcToWriteInDC = Report.getTestCaseId();
			ExcelUtilities.writeToDataCarrier(tcToWriteInDC, columnNameInDC, accFeeStr);
			String msg = "Value '" + accFeeStr + "' successfully written in Data Carrier file against TC '"
					+ tcToWriteInDC + "' and column '" + columnNameInDC + "'.";
			log.info(msg);
			Assertions.writeResults("pass", msg, msg, false, "");
		} catch (Exception e) {
			Base.sa.fail();
			String msg = "Unable to write " + columnNameInDC + " to Data Carrier.";
			log.info(msg);
			Assertions.writeResults("fail", msg, msg, false, "soft");
		}
	}

	public static void deriveAccountOutstanding(String fieldName, String inputValue, String columnNameInDC,
			String locator) throws Exception {
		String[] policyCount = inputValue.split(";;");
		Double accOutstanding = 0.00;
		String accOutstandingStr = "";
		for (int i = 0; i < policyCount.length; i++) {

			int policyId = Integer.parseInt(policyCount[i].trim().toLowerCase().replace("pol", ""));
			String columnToRead = "Premium_Pol" + policyId;
			String polPremStringValue = ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), columnToRead);
			polPremStringValue = removeFormattingFromNumber(polPremStringValue);
			accOutstanding = accOutstanding + Double.parseDouble(polPremStringValue);
			accOutstandingStr = String.format("%.2f", accOutstanding);
		}
		try {
			String tcToWriteInDC = Report.getTestCaseId();
			ExcelUtilities.writeToDataCarrier(tcToWriteInDC, columnNameInDC, accOutstandingStr);
			String msg = "Value '" + accOutstandingStr + "' successfully written in Data Carrier file against TC '"
					+ tcToWriteInDC + "' and column '" + columnNameInDC + "'.";
			log.info(msg);
			Assertions.writeResults("pass", msg, msg, false, "");
		} catch (Exception e) {
			Base.sa.fail();
			String msg = "Unable to write " + columnNameInDC + " to Data Carrier.";
			log.info(msg);
			Assertions.writeResults("fail", msg, msg, false, "soft");
		}
	}

	public static double retrieveValueFromBillingKeywords(String keyword) throws Exception {
		/*
		 * if(keyword.toUpperCase().contains("ACC_OUTSTANDING")) {
		 * return(Double.parseDouble(Calculations.removeFormattingFromNumber(
		 * ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(),
		 * "AccOutstanding")))); }
		 * 
		 * else if(keyword.toUpperCase().contains("ACC_FEE1")) { Double fee=0.00; try {
		 * fee =
		 * Double.parseDouble(Calculations.removeFormattingFromNumber(ExcelUtilities.
		 * readFromDataCarrier(Report.getTestCaseId(), "ACC_FEE1"))); }catch(Exception
		 * e) { log.info
		 * ("Acc Fee 1 not present in Data carrier file\\Issue while retreiving. Considered value 0.00"
		 * ); }finally { return fee; } }
		 * 
		 * else {
		 */
		try {
			return (Double.parseDouble(Calculations.removeFormattingFromNumber(ExcelUtilities.readFromDataCarrier(Report.getTestCaseId(), keyword))));
		} catch (Exception e) {
			return (Double.parseDouble(keyword));
		}
	}

	public static double performArithmeticOperation(Double leftSide, Double rightside, String operation) {
		switch (operation) {
		case "-":
			return (leftSide - rightside);
		case "+":
			return (leftSide + rightside);
		case "*":
			return (leftSide * rightside);
		case "/":
			return (leftSide / rightside);
		default:
			log.info("operation '" + operation + "' is not yet defined.");
			return 0;
		}
	}

	public static double identifyAndEvaluateOperation(String expression) throws Exception{
		String operation="", leftValueStr="", rightValueStr="";
		Double finalResult;
		
		if(expression.contains("+")){
			operation="+";
		}else if(expression.contains("*")){
			operation="*";
		}else if(expression.contains("/")) {
			operation="/";
		}else if(expression.toUpperCase().contains("CUSTRND")) {
			finalResult = CustomClass.calc10thInstallWithRoundingLogic(Double.parseDouble(expression.toUpperCase().trim().replace("CUSTRND", "")));
			return finalResult;
		}else if(expression.contains("-")){
			operation="-";
		}else {
			finalResult=Calculations.retrieveValueFromBillingKeywords(expression);
			return finalResult;
		}
		leftValueStr=expression.substring(0, expression.indexOf(operation)).trim();
		rightValueStr=expression.substring(expression.indexOf(operation)+1).trim();
		finalResult=Calculations.performArithmeticOperation(Calculations.retrieveValueFromBillingKeywords(leftValueStr), Calculations.retrieveValueFromBillingKeywords(rightValueStr), operation);
		return finalResult;
	}

	public static double identifyAndEvaluateExpresssion(String expression)throws Exception {
		Double finalOutput=0.00;
		try {
			int calcIndex = expression.toUpperCase().indexOf("CALC");
			
			if(calcIndex == -1) 
				throw new Exception("Expression expects CALC keyword. Ex 'CALC(a*b)|'");
			
			int expBeginIndex=calcIndex;
			int expEndIndex = expression.toUpperCase().indexOf("|", expBeginIndex);
			
			if(expEndIndex == -1)
				throw new Exception("Expression expects end of expression by '|'. Ex: 'CALC(a*b)|'.");

			String finalExpression =expression.substring(expBeginIndex, expEndIndex);
			long countOfOpeningBraces = finalExpression.chars().filter(ch -> ch == '(').count();
			long countOfClosingBraces = finalExpression.chars().filter(ch -> ch == ')').count();
			if(countOfOpeningBraces != countOfClosingBraces)
				throw new Exception("Invalid expression. Check opening and closing braces.Ex 'CALC(a*b)|'");
			
			//String[][] bracesList = new String[(int)countOfOpeningBraces][2];
			/*
			 * List<List<String>> bracesList = new ArrayList<List<String>>(); for(int
			 * i=expBeginIndex+4; i<expEndIndex; i++) {
			 * 
			 * //for(int j=1;j<=countOfOpeningBraces;j++) { if (finalExpression.charAt(i) ==
			 * '(' ) { //bracesList[j][0] = "O"; //bracesList[j][1] = String.valueOf(i);
			 * List<String> tempList = new ArrayList<String>(1); tempList.add("O");
			 * tempList.add(String.valueOf(i)); bracesList.add(tempList); } if
			 * (finalExpression.charAt(i) == ')' ) { //bracesList[j][0] = "C";
			 * //bracesList[j][1] = String.valueOf(i); List<String> tempList = new
			 * ArrayList<String>(1); tempList.add("C"); tempList.add(String.valueOf(i));
			 * bracesList.add(tempList); } //} }
			 */
			String finalOutputStr=finalExpression;
			while(finalOutputStr.contains("(") || finalOutputStr.contains(")")) {
				List<List<String>> bracesList = new ArrayList<List<String>>();
				for(int i=0; i<finalOutputStr.length(); i++) {
					
					//for(int j=1;j<=countOfOpeningBraces;j++) {
						if (finalOutputStr.charAt(i) == '(' ) {
							//bracesList[j][0] = "O";
							//bracesList[j][1] = String.valueOf(i);
							List<String> tempList = new ArrayList<String>(1);
							tempList.add("O");
							tempList.add(String.valueOf(i));
							bracesList.add(tempList);
						}
						if (finalOutputStr.charAt(i) == ')' ) {
							//bracesList[j][0] = "C";
							//bracesList[j][1] = String.valueOf(i);
							List<String> tempList = new ArrayList<String>(1);
							tempList.add("C");
							tempList.add(String.valueOf(i));
							bracesList.add(tempList);
						}
					//}
				}
				
				for(int i=0;i<bracesList.size()-1;i++) {
					String leftOfCurExp="", rightOfCurExp="", evaluatedExp="";
					if(bracesList.get(i).get(0) == "O" && bracesList.get(i+1).get(0) == "C") {
						leftOfCurExp=finalOutputStr.substring(0, Integer.parseInt(bracesList.get(i).get(1)));
						rightOfCurExp=finalOutputStr.substring(Integer.parseInt(bracesList.get(i+1).get(1))+1);
						String tempExpression=finalOutputStr.substring(Integer.parseInt(bracesList.get(i).get(1))+1, Integer.parseInt(bracesList.get(i+1).get(1)));
						evaluatedExp=String.valueOf(Calculations.identifyAndEvaluateOperation(tempExpression));
						finalOutputStr=leftOfCurExp+evaluatedExp+rightOfCurExp;
						//bracesList.remove(i);
						//bracesList.remove(i+1);
						break;
					}
				}
			}
			finalOutput=Double.parseDouble(finalOutputStr.replace("CALC",""));
			return (finalOutput);
		}catch(Exception e) {
			log.info("Issue while evaluating CALC expression.");
			log.error(e.getMessage());
			throw new Exception (e.getMessage());
			//Assertions.writeResults("fail", e.getMessage(), e.getMessage(), false,"");
		}
	}
}
