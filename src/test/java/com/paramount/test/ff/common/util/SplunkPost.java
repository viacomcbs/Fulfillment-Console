package com.paramount.test.ff.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.ITestResult;

import com.paramount.test.ff.common.util.props.BrowserType;
import com.paramount.test.ff.common.util.props.DesktopOSType;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.synergy.core.reporting.SplunkPoster;

public class SplunkPost {
	// Use if execution is on jenkins and POST_SPLUNK_DATA is selected on jenkins
	public static Boolean POST_SPLUNK_DATA = Boolean.parseBoolean(System.getenv("POST_SPLUNK_DATA"));

	// Use if execution on local
	public static Boolean POST_SPLUNK_DATA_LOCAL = Boolean.parseBoolean(ConfigProps.POST_SPLUNK);
	public static String RELEASE_NAME = ConfigProps.RELEASE_NAME;
	
	// Lab Test result specific
	public static void postLabTestResult(ITestResult test) {

		if ((POST_SPLUNK_DATA && TestUtil.isLabExecution()) || POST_SPLUNK_DATA_LOCAL) {
			@SuppressWarnings("unused")
			String testType = "other";
			// if (ConfigProps.TEST_ENVIRONMENT.equalsIgnoreCase("dev")) {
			// testType = "Smoke";
			// } else if (ConfigProps.TEST_ENVIRONMENT.equalsIgnoreCase("qa")) {
			// testType = "Regression";
			// }

			/*TestCaseId annotation = test.getMethod().getConstructorOrMethod().getMethod()
					.getAnnotation(TestCaseId.class);
			String ids = annotation.value();*/

			// Use if execution is on jenkins and POST_SPLUNK_DATA is selected on jenkins
			// String buildNum = System.getenv("BUILD_NUMBER");

			String buildNum = RELEASE_NAME;

			String testResult = test.getStatus() == 1 ? "PASS" : "FAIL";
			String testFailReason = test.getThrowable() != null ? String.valueOf(test.getThrowable().getMessage())
					: "NotDefined";
			testFailReason = testFailReason.replace("\\", "\\\\");
			testFailReason = testFailReason.replaceAll("\\R+", "");

			SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dateTime = dateTimeFormat.format(new Date());
			StringBuilder Entry = new StringBuilder();
			DesktopOSType desktopOS = TestRun.getOS();
			BrowserType browser = TestRun.getBrowser();

			// Duration totalTimeDifference = Duration.between( startDateTime ,
			// endDateTime );
			Entry.append("{\"ScriptExecutionDate\": " + "\"" + dateTime + "\", ");
			Entry.append("\"BuildNumber\": " + "\"" + buildNum + "\", ");
			Entry.append("\"TestRailProjectID\": " + "\"" + ConfigProps.TESTRAIL_PROJECT_ID + "\", ");
			// Entry.append("\"TestType\": " + "\"" + testType + "\", ");
			Entry.append("\"OS\": " + "\"" + desktopOS.value().toUpperCase() + "\", ");
			Entry.append("\"Browser\": " + "\"" + browser.value().toUpperCase() + "\", ");
			Entry.append("\"AppName\": " + "\"" + "CRM" + "\", ");
			Entry.append("\"Environment\": " + "\"" + ConfigProps.TEST_ENVIRONMENT.toUpperCase() + "\", ");
			//Entry.append("\"TestID\": " + "\"" + ids + "\", ");
			Entry.append("\"TestScriptName\": " + "\"" + test.getName() + "\", ");
			Entry.append("\"TestResult\": " + "\"" + testResult + "\", ");
			Entry.append(
					"\"ExecutionTimeInSecs\": " + "\"" + (test.getEndMillis() - test.getStartMillis()) / 1000 + "\", ");
			Entry.append("\"LabTestFailReason\": " + "\"" + testFailReason + "\"}");
			SplunkPoster splunkPoster = new SplunkPoster(TestUtil.serverLabURL);
			Logger.logConsoleMessage(Entry.toString());
			splunkPoster.postEvent(ConfigProps.SPLUNK_INDEX, Entry.toString());
		} else {
			Logger.logMessage("LabTestData will only be posted for Regression/Smoke");
		}

	}


}