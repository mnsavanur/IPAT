package com.lti.api;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.log4j.Logger;

/*
 * Date - 4/7/2021
 * Author - Sheetal Jadhav
 * Description - WebService Testing through JMeter
*/

public class MyResultCollector extends ResultCollector {
	static Logger log = Logger.getLogger(MyResultCollector.class.getName());
    public MyResultCollector(Summariser summer) {
        super(summer);
    }
    
    //Overriding to retrieve and analyze results of individual request
    @Override
    public void sampleOccurred(SampleEvent e) {
    	SampleResult r = e.getResult();
    	log.info("Request: " + r.getRequestHeaders());
        super.sampleOccurred(e);
        
        log.info("Response Code: "+  r.getResponseCode());
        if (r.getResponseCode().equalsIgnoreCase("200")) {
			/*
			 * if(r.getErrorCount() == 1) WebServiceUtil.wsFailure=true;
			 */
            if(r.getFirstAssertionFailureMessage() !=null)
            	WebServiceUtil.wsAssertionFailure=true;
            
            log.info("First Assertion Failure Message: "+ r.getFirstAssertionFailureMessage());
            log.info("Response Written to: "+r.getResultFileName());
        }else {
        	WebServiceUtil.wsFailure=true;
        	log.info("WebService Failure.");
        }
		/* System.out.println("Success: " + r.isSuccessful());
		 System.out.println("ErrorCount: " + r.getErrorCount()); */
		 
    }
}