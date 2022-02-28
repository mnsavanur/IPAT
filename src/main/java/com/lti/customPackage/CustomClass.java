package com.lti.customPackage;

import com.lti.TestUtil.Calculations;
/*
 * Date - 10/22/2021
 * Author - Sheetal Jadhav
 * Description - Class for project specific methods
*/
public class CustomClass {
	//Can be removed
	public static double calc10thInstallWithRoundingLogic(double amount) {
		String tempNum=String.format("%.4f", amount);
		String tempDecimalStr="0.00"+tempNum.substring(tempNum.length()-2, tempNum.length());
		return(Double.parseDouble(tempDecimalStr)*8+amount);
	}
}
