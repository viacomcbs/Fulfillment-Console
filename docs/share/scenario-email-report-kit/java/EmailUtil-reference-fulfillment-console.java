package com.paramount.test.ff.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967EmailReport;
import com.paramount.test.ff.uitests.helpers.dsid.DsidEmailReport;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingEmailReport;
import com.synergy.core.reporting.Emailer;

public class EmailUtil {

	protected static String senderAddress = Config.getString("senderAddress");
	protected static String recipientAddress = Config.getString("recipientAddress");

	public static void sendResultEmail(String jenkinsReportURL, Integer passCount, Integer failCount, Integer skipCount,
			Integer brokenCount, String fileStorageUrl) {
		// String testRailUrl = TestRailUtil.getRunUrl();

		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy/ HH:mm:ss");
		Date date = new Date();
		// get relevant email report data
		String applicationTitle = ConfigProps.APPLICATION_TITLE;
		String AppEnv = "";
		if (ConfigProps.TEST_ENVIRONMENT.equalsIgnoreCase("prod")) {
			AppEnv = "PROD Regression";
		} else if (ConfigProps.TEST_ENVIRONMENT.equalsIgnoreCase("dev")) {
			AppEnv = "DEV System";
		}else if (ConfigProps.TEST_ENVIRONMENT.equalsIgnoreCase("uat")) {
			AppEnv = "UAT System";
		}

		// set the email subject
		String subject = "";
		if (!failCount.equals(0)) {
			subject = applicationTitle + " : " + "Lab Execution Report" + ": " + AppEnv + " - Failed";
		} else {
			subject = applicationTitle + " : " + "Lab Execution Report" + ": " + AppEnv + " - Passed";
		}

		// set the email body

		String reportAttachmentTxt = "The test report was not uploaded.";
		String excelAttachmentTxt = "Excel file was not created.";
		if (!jenkinsReportURL.isEmpty())
			reportAttachmentTxt = "A detailed execution report is available <a href='" + jenkinsReportURL
					+ "'>here</a>";
		if (!fileStorageUrl.isEmpty())
			excelAttachmentTxt = "Alternatively, an excel report can be downloaded from <a href='" + fileStorageUrl
					+ "'>this link</a>";

		String env = "<a href='" + "'>" + ConfigProps.TEST_ENVIRONMENT + "</a>";
		String header = "<body><b>Execution Completed!!!</b>";
		if (TestUtil.isLabExecution())
			header = "<body><b>Lab Execution Completed!!!</b>";
		String messageContent = header + "<br /><br />Application : " + applicationTitle + "<br />Environment : " + env
				+ "<br /><br />Tests Passed = " + passCount.toString()
				// + "<br />Tests Skipped = " + skipCount.toString()
				+ "<br />Tests Failed = " + failCount
				// + "<br />Tests Broken = " + brokenCount.toString()
				+ "<br /><br />" + reportAttachmentTxt
				// + "<br /><br />Test Rail URL : " + testRailUrl
				+ "<br /><br />" + excelAttachmentTxt
				+ "<br /><br />(If you are unable to open the file on VDI, then please try opening it on Local system.)"
				+ "<br /><br />Execution Date : " + dateFormat.format(date) + "<br /><br /><b>Regards,</b>"
				+ "<br /><br />ViacomCBS EQE</body>";

		// send the email report
		Emailer emailer = new Emailer(TestUtil.getServerUrl());

		if (TestUtil.isLabExecution()) {
			try {
				// if (TestUtil.isLabExecution()) {
				emailer.sendEmail(ConfigProps.EMAIL_SENDER_ADDRESS, ConfigProps.SEND_REPORT_EMAIL_ADDRESS, subject,	messageContent);
				Logger.logMessage("Email sent succesfully");
			} catch (Exception e) {
				e.printStackTrace();
				Logger.logMessage("Email not sent");
			}
		}
	}
	
	public static void sendResultEmail(String jenkinsReportURL, Integer passCount, Integer failCount, Integer skipCount,
			Integer brokenCount) {

		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy/ HH:mm:ss");
		Date date = new Date();
		String applicationTitle = Config.getString("ApplicationTitle");
		String appEnv = resolveAppEnv(Config.getString("TestEnvironment"));

		int summaryPass = passCount;
		int summaryFail = failCount;
		int summarySkip = skipCount;
		if (PtsPackagingEmailReport.isEnabled()) {
			summaryPass = PtsPackagingEmailReport.getPassedCount();
			summaryFail = PtsPackagingEmailReport.getFailedCount();
			summarySkip = PtsPackagingEmailReport.getSkippedCount();
		} else if (Bsd29967EmailReport.isEnabled()) {
			summaryPass = Bsd29967EmailReport.getPassedCount();
			summaryFail = Bsd29967EmailReport.getFailedCount();
			summarySkip = Bsd29967EmailReport.getSkippedCount();
		} else if (DsidEmailReport.isEnabled()) {
			summaryPass = DsidEmailReport.getPassedCount();
			summaryFail = DsidEmailReport.getFailedCount();
			summarySkip = DsidEmailReport.getSkippedCount();
		} else if (LeftFilterEmailReport.isEnabled()) {
			summaryPass = LeftFilterEmailReport.getPassedCount();
			summaryFail = LeftFilterEmailReport.getFailedCount();
			summarySkip = LeftFilterEmailReport.getSkippedCount();
		}

		String subject = applicationTitle + " : Test Execution Report: " + appEnv
				+ (summaryFail == 0 ? " - Passed" : " - Failed");
		if (PtsPackagingEmailReport.isEnabled()) {
			subject = applicationTitle + " — " + PtsPackagingEmailReport.FEATURE_TITLE + " ("
					+ PtsPackagingEmailReport.JIRA_KEY + "): " + appEnv
					+ (summaryFail == 0 ? " - Passed" : " - Failed");
		} else if (Bsd29967EmailReport.isEnabled()) {
			subject = applicationTitle + " — " + Bsd29967EmailReport.FEATURE_TITLE + " ("
					+ Bsd29967EmailReport.JIRA_KEY + "): " + appEnv
					+ (summaryFail == 0 ? " - Passed" : " - Failed");
		} else if (DsidEmailReport.isEnabled()) {
			subject = applicationTitle + " — " + DsidEmailReport.FEATURE_TITLE + " ("
					+ DsidEmailReport.JIRA_KEY + "): " + appEnv
					+ (summaryFail == 0 ? " - Passed" : " - Failed");
		} else if (LeftFilterEmailReport.isEnabled()) {
			subject = applicationTitle + " — Left Filter Validation: " + appEnv
					+ (summaryFail == 0 ? " - Passed" : " - Failed");
		}

		String reportAttachmentTxt = jenkinsReportURL.isEmpty() ? "The test report was not uploaded."
				: "<a href='" + jenkinsReportURL + "'>Report link</a>";
		String env = Config.getString("TestEnvironment");
		String scenarioSection = "";
		if (PtsPackagingEmailReport.isEnabled()) {
			scenarioSection = PtsPackagingEmailReport.buildHtmlSection();
		} else if (Bsd29967EmailReport.isEnabled()) {
			scenarioSection = Bsd29967EmailReport.buildHtmlSection();
		} else if (DsidEmailReport.isEnabled()) {
			scenarioSection = DsidEmailReport.buildHtmlSection();
		} else if (LeftFilterEmailReport.isEnabled()) {
			scenarioSection = LeftFilterEmailReport.buildHtmlSection();
		}

		String messageContent = buildExecutionEmailBody(applicationTitle, env, summaryPass, summarySkip, summaryFail,
				reportAttachmentTxt, scenarioSection, dateFormat.format(date));

		String sender = Config.getString("EmailSenderAddress");
		String recipient = Config.getString("SendReportEmailAddress");
		if (recipient == null || recipient.trim().isEmpty()) {
			Logger.logMessage("Email not sent: SendReportEmailAddress is not configured.");
			return;
		}

		try {
			new Emailer(TestUtil.getServerUrl()).sendEmail(sender, recipient, subject, messageContent);
			Logger.logMessage("Email sent succesfully to " + recipient);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.logMessage("Email not sent");
		}
	}

	private static String buildExecutionEmailBody(String applicationTitle, String env, int summaryPass,
			int summarySkip, int summaryFail, String reportAttachmentTxt, String ptsSection, String executionDate) {
		return "<body><b>Execution Completed!!!</b>"
				+ "<head><style>"
				+ "#TestResults{font-family:Calibri,Helvetica,sans-serif;font-size:large;border-collapse:collapse;width:100%;}"
				+ "#TestResults td,#TestResults th{border:2px solid black;padding:8px;}"
				+ "#TestResults tr:nth-child(even){background-color:#f2f2f2;}"
				+ "#TestResults tr:hover{background-color:#ddd;}"
				+ "#TestResults th{padding-top:12px;padding-bottom:12px;text-align:left;background-color:#04AA6D;color:white;}"
				+ "#pass{background-color:#9ff51d}#skip{background-color:#f4ff2b}#fail{background-color:red}"
				+ "</style></head>"
				+ "<h1>Automation Test Result</h1>"
				+ (ptsSection.isEmpty() ? "" : ptsSection + "<br />")
				+ "<h2>Execution Summary</h2>"
				+ "<table id=TestResults><tr><th>Application</th><th>Environment</th>"
				+ "<th>Tests Passed</th><th>Tests Skipped</th><th>Tests Failed</th><th>Synergy Report</th></tr>"
				+ "<tr><td>" + applicationTitle + "</td><td>" + env + "</td>"
				+ "<td id=pass>" + summaryPass + "</td><td id=skip>" + summarySkip + "</td><td id=fail>" + summaryFail
				+ "</td><td>" + reportAttachmentTxt + "</td></tr></table>"
				+ "<br /><br />Execution Date : " + executionDate
				+ "<br /><br /><b>Regards,</b><br /><br />ViacomCBS </body>";
	}

	private static String resolveAppEnv(String testEnvironment) {
		if (testEnvironment == null) {
			return "";
		}
		switch (testEnvironment.toLowerCase()) {
		case "prod":
			return "PROD Regression";
		case "dev":
			return "DEV System Test";
		case "uat":
			return "UAT Automation";
		default:
			return testEnvironment;
		}
	}
}
