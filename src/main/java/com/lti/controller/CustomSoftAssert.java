package com.lti.controller;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.maven.surefire.shared.utils.StringUtils;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Maps;

import com.lti.TestUtil.Report;
import com.lti.base.Config;
public class CustomSoftAssert extends SoftAssert{
	 private final Map<AssertionError, IAssert<?>> m_errors_custom = Maps.newLinkedHashMap();
	 private static final String DEFAULT_SOFT_ASSERT_MESSAGE_CUSTOM = "The following asserts failed:";
	 protected void doAssert(IAssert<?> a) {
		    onBeforeAssert(a);
		    try {
		      a.doAssert();
		      onAssertSuccess(a);
		    } catch (AssertionError ex) {
		      onAssertFailure(a, ex);
		      m_errors_custom.put(ex, a);
		    } finally {
		      onAfterAssert(a);
		    }
		  }

		  public void assertAll() {
		    assertAll(null);
		  }

		  public void assertAll(String message) {
		    if (!m_errors_custom.isEmpty()) {
		      StringBuilder sb = new StringBuilder(null == message ? DEFAULT_SOFT_ASSERT_MESSAGE_CUSTOM : message);
		      boolean first = true;
		      for (AssertionError error : m_errors_custom.keySet()) {
		        if (first) {
		          first = false;
		        } else {
		          sb.append(",");
		        }
		        sb.append("\n\t");
		        sb.append(getErrorDetails(error));
		      }
		      
		      if(StringUtils.equalsIgnoreCase(Config.userInteraction, "on") && Config.continueOnValidationFailure.equalsIgnoreCase("N")){
					 String displayMsg=Report.getTestCaseId() +" : " + Report.getTransactionName() +" : "+ Report.getFieldName() + "\n\nValidation mismatch(es)";
					 int response = Controller.displayJFrame("IPAT : Click Yes to continue even after below failure(s).", displayMsg,  JOptionPane.YES_NO_OPTION);
					 if(response==JOptionPane.NO_OPTION) {
						 throw new AssertionError(sb.toString());
					 }else {
						 m_errors_custom.clear();
					 }
				 }else {
					 throw new AssertionError(sb.toString());
				 }
		    }
		  }
}
