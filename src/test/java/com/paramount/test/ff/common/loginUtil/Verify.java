package com.paramount.test.ff.common.loginUtil;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.AllureAttachment;
import com.paramount.test.ff.common.util.SoftAssert;

import org.testng.Assert;

public class Verify extends BaseTest{


	public static void hardAssert(boolean actual, String message) {
		if (actual) {
			AllureAttachment.writeStepToPass(message);

		} else {
			AllureAttachment.markStepAsFailed(message);

		}
		Assert.assertTrue(actual, message);
	}
	
	public static void softAssert(boolean actual, String message) {
		//org.testng.asserts.SoftAssert softAssert= new org.testng.asserts.SoftAssert();
		softAssert.assertTrue(actual, message);

			if (actual) {
				AllureAttachment.writeStepToPass(message);

			} else {
				AllureAttachment.markStepAsFailed(message);
			}


	}
	
	public static void softAssert1(boolean actual, String message, SoftAssert softAssert) {
		//softAssert.assertTrue(actual, message);
		softAssert.assertTrue(actual, message);
	}
	
	
		
	
}
