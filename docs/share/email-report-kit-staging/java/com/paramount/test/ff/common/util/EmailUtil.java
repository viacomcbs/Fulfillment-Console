package com.paramount.test.ff.common.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.common.util.reporting.AllureResultsReader;
import com.paramount.test.ff.common.util.reporting.AutomationReportAssets;
import com.paramount.test.ff.common.util.reporting.ExecutionReportEmailBuilder;
import com.paramount.test.ff.common.util.reporting.ExecutionReportMailer;
import com.paramount.test.ff.common.util.reporting.ExecutionReportPdfGenerator;
import com.paramount.test.ff.common.util.reporting.PassedOnlyAllureReportGenerator;
import com.paramount.test.ff.common.util.reporting.TestResultRecord;
import com.paramount.test.ff.common.util.reporting.TestResultReportFilter;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967EmailReport;
import com.paramount.test.ff.uitests.helpers.dsid.DsidEmailReport;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingEmailReport;
import com.synergy.core.reporting.Emailer;
import com.synergy.core.reporting.FileUploader;

public class EmailUtil {

	protected static String senderAddress = Config.getString("senderAddress");
	protected static String recipientAddress = Config.getString("recipientAddress");
	private static String cachedParamountLogoUrl = "";
	private static final String PARAMOUNT_LOGO_UPLOAD_VERSION = "v3";

	public static void sendTodayPassedReport() {
		sendTodayPassedReport(null);
	}

	public static void sendTodayPassedReport(String suiteTitle) {
		File resultsDir = new File(System.getProperty("user.dir") + File.separator + "allure-results");
		List<TestResultRecord> todayResults = AllureResultsReader.readTodayResults(resultsDir);
		sendRegressionDualReport(suiteTitle, todayResults, resultsDir);
	}

	public static void sendYesterdayPassedReport(String suiteTitle) {
		File resultsDir = new File(System.getProperty("user.dir") + File.separator + "allure-results");
		List<TestResultRecord> yesterdayResults = AllureResultsReader.readYesterdayResults(resultsDir);
		String reportDate = new SimpleDateFormat("MMM dd, yyyy").format(
				new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000));
		String resolvedSuiteTitle = suiteTitle;
		if (resolvedSuiteTitle == null || resolvedSuiteTitle.trim().isEmpty()) {
			resolvedSuiteTitle = "Yesterday's Passed Test Cases (" + reportDate + ")";
		}
		sendRegressionDualReport(resolvedSuiteTitle, yesterdayResults, resultsDir);
	}

	public static void sendRegressionDualReport(String suiteTitle, List<TestResultRecord> allResults, File resultsDir) {
		if (allResults == null) {
			allResults = java.util.Collections.emptyList();
		}
		if (resultsDir == null) {
			resultsDir = new File(System.getProperty("user.dir") + File.separator + "allure-results");
		}
		List<TestResultRecord> passedResults = TestResultReportFilter.passedOnly(allResults);
		String reportDate = new SimpleDateFormat("MMM dd, yyyy").format(new Date());
		String resolvedSuiteTitle = suiteTitle;
		if (resolvedSuiteTitle == null || resolvedSuiteTitle.trim().isEmpty()) {
			resolvedSuiteTitle = "Today's Test Cases (" + reportDate + ")";
		}
		long totalDurationMs = TestResultReportFilter.totalDurationMs(allResults);
		String screenRecordingLink = AllureResultsReader.findScreenRecordingLink(resultsDir);
		String allureReportUrl = PassedOnlyAllureReportGenerator.generatePassedReportUrl(resultsDir, false);

		String stakeholderEmail = Config.getString("SendReportEmailAddress");
		if (stakeholderEmail != null && !stakeholderEmail.isBlank()) {
			Logger.logMessage("Sending passed-only summary to " + stakeholderEmail);
			dispatchExecutionReport(resolvedSuiteTitle + " — Passed Summary", passedResults, passedResults.size(),
					totalDurationMs, allureReportUrl, screenRecordingLink, stakeholderEmail);
		}

		String internalEmail = Config.getString("SendReportInternalEmailAddress");
		if (internalEmail != null && !internalEmail.isBlank()) {
			int failCount = countNonPassed(allResults);
			Logger.logMessage("Sending full-status summary to " + internalEmail);
			dispatchExecutionReport(resolvedSuiteTitle + " — Full Status", reindexAll(allResults),
					passedResults.size(), totalDurationMs, allureReportUrl, screenRecordingLink, internalEmail,
					failCount);
		}
	}

	private static int countNonPassed(List<TestResultRecord> results) {
		int count = 0;
		for (TestResultRecord record : results) {
			if (record != null && !"PASS".equalsIgnoreCase(record.getStatus())) {
				count++;
			}
		}
		return count;
	}

	private static List<TestResultRecord> reindexAll(List<TestResultRecord> results) {
		List<TestResultRecord> indexed = new java.util.ArrayList<>();
		int index = 1;
		for (TestResultRecord record : results) {
			if (record == null) {
				continue;
			}
			indexed.add(new TestResultRecord(index++, record.getTestCaseId(), record.getScenario(), record.getStatus(),
					record.getExpected(), record.getActual(), record.getFailureReason(), record.getDurationMs()));
		}
		return indexed;
	}

	private static void dispatchExecutionReport(String suiteTitle, List<TestResultRecord> rows, int passCount,
			long totalDurationMs, String allureReportUrl, String screenRecordingLink, String recipient) {
		dispatchExecutionReport(suiteTitle, rows, passCount, totalDurationMs, allureReportUrl, screenRecordingLink,
				recipient, 0);
	}

	private static void dispatchExecutionReport(String suiteTitle, List<TestResultRecord> rows, int passCount,
			long totalDurationMs, String allureReportUrl, String screenRecordingLink, String recipient, int failCount) {
		int skipCount = countByStatus(rows, "SKIP") + countByStatus(rows, "SKIPPED");
		String pdfHostedUrl = "";
		if (shouldAttachPdfReport()) {
			File reportsDir = new File(Config.getFilePath("PathToReports"));
			File pdfFile = ExecutionReportPdfGenerator.generate(Config.getString("ApplicationTitle"), suiteTitle,
					Config.getString("TestEnvironment"), passCount, failCount, skipCount, totalDurationMs,
					resolveAutomationReportTesterName(), rows, reportsDir);
			if (pdfFile != null && shouldUploadPdfToSynergy()) {
				pdfHostedUrl = uploadPdfToSynergy(pdfFile);
			}
		}

		ExecutionReportEmailBuilder builder = applyReportLogo(new ExecutionReportEmailBuilder()
				.applicationTitle(Config.getString("ApplicationTitle"))
				.suiteTitle(suiteTitle)
				.environment(Config.getString("TestEnvironment"))
				.synergyReportUrl(allureReportUrl)
				.pdfReportUrl(pdfHostedUrl)
				.screenRecordingLink(screenRecordingLink)
				.testerName(resolveAutomationReportTesterName())
				.passCount(passCount)
				.failCount(failCount)
				.skipCount(skipCount)
				.totalDurationMs(totalDurationMs)
				.testResults(rows));

		sendAutomationReportEmail(recipient, builder);
	}

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
		sendResultEmail(jenkinsReportURL, passCount, failCount, skipCount, brokenCount, null, false);
	}

	/**
	 * @param recipientOverride when non-null, send to this address instead of {@code SendReportEmailAddress}
	 * @param passedOnlyReport when {@code true} and left-filter report is enabled, hide FAIL/SKIP rows and counts
	 */
	public static void sendResultEmail(String jenkinsReportURL, Integer passCount, Integer failCount, Integer skipCount,
			Integer brokenCount, String recipientOverride, boolean passedOnlyReport) {

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
			if (passedOnlyReport) {
				summaryFail = 0;
				summarySkip = 0;
			}
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
			subject = applicationTitle + " — Automation Report: " + appEnv
					+ (summaryFail == 0 ? " - Passed" : " - Failed");
			if (passedOnlyReport) {
				subject = applicationTitle + " — Automation Report: " + appEnv + " - Passed";
			} else if (recipientOverride != null && !recipientOverride.isBlank()) {
				subject = applicationTitle + " — Automation Report (Full Status): " + appEnv
						+ (summaryFail == 0 ? " - Passed" : " - Failed");
			}
		}

		String reportAttachmentTxt = buildReportLinksCell(jenkinsReportURL, "");
		String env = Config.getString("TestEnvironment");
		String scenarioSection = "";
		String pdfHostedUrl = "";
		String messageContent = null;
		if (PtsPackagingEmailReport.isEnabled()) {
			scenarioSection = PtsPackagingEmailReport.buildHtmlSection();
		} else if (Bsd29967EmailReport.isEnabled()) {
			scenarioSection = Bsd29967EmailReport.buildHtmlSection();
		} else if (DsidEmailReport.isEnabled()) {
			scenarioSection = DsidEmailReport.buildHtmlSection();
		} else if (LeftFilterEmailReport.isEnabled()) {
			List<TestResultRecord> reportRows = LeftFilterEmailReport.toResultRecords(passedOnlyReport);
			if (shouldAttachPdfReport()) {
				File reportsDir = new File(Config.getFilePath("PathToReports"));
				File pdfFile = ExecutionReportPdfGenerator.generate(Config.getString("ApplicationTitle"),
						Config.getString("LeftFilterEmailSuiteTitle"), env, summaryPass, summaryFail, summarySkip,
						LeftFilterEmailReport.getSuiteDurationMs(), resolveAutomationReportTesterName(), reportRows,
						reportsDir);
				if (pdfFile != null && shouldUploadPdfToSynergy()) {
					pdfHostedUrl = uploadPdfToSynergy(pdfFile);
				}
				reportAttachmentTxt = buildReportLinksCell(jenkinsReportURL, pdfHostedUrl);
			}
			messageContent = buildLeftFilterAutomationReportEmail(reportRows, summaryPass, summaryFail, summarySkip,
					jenkinsReportURL, pdfHostedUrl);
		}

		if (messageContent == null) {
			if (Bsd29967EmailReport.isEnabled()) {
				messageContent = buildBsd29967EmailBody(scenarioSection, dateFormat.format(date));
			} else {
				messageContent = buildExecutionEmailBody(Config.getString("ApplicationTitle"), env, summaryPass,
						summaryFail, reportAttachmentTxt, scenarioSection, dateFormat.format(date));
			}
		}

		String sender = Config.getString("EmailSenderAddress");
		String recipient = recipientOverride != null && !recipientOverride.isBlank()
				? recipientOverride.trim()
				: Config.getString("SendReportEmailAddress");
		if (recipient == null || recipient.trim().isEmpty()) {
			Logger.logMessage("Email not sent: SendReportEmailAddress is not configured.");
			return;
		}

		if (LeftFilterEmailReport.isEnabled() && messageContent != null) {
			sendAutomationReportEmail(recipient, subject, messageContent);
		} else {
			try {
				new Emailer(TestUtil.getServerUrl()).sendEmail(sender, recipient, subject, messageContent);
				Logger.logMessage("Email sent succesfully to " + recipient);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.logMessage("Email not sent");
			}
		}
	}

	private static String buildBsd29967EmailBody(String workflowSection, String executionDate) {
		return "<body><head><style>"
				+ "table{font-family:Calibri,Helvetica,sans-serif;border-collapse:collapse;width:100%;}"
				+ "td,th{border:2px solid black;padding:8px;}"
				+ "</style></head>"
				+ "<h1>Automation Test Result</h1>"
				+ (workflowSection.isEmpty() ? "" : workflowSection)
				+ "<br /><br />Execution Date : " + executionDate
				+ "<br /><br /><b>Regards,</b><br /><br />ViacomCBS </body>";
	}

	private static String buildExecutionEmailBody(String applicationTitle, String env, int summaryPass,
			int summaryFail, String reportLinksCell, String scenarioSection, String executionDate) {
		return "<body><b>Execution Completed!!!</b>"
				+ "<head><style>"
				+ "#TestResults{font-family:Calibri,Helvetica,sans-serif;font-size:large;border-collapse:collapse;width:100%;}"
				+ "#TestResults td,#TestResults th{border:2px solid black;padding:8px;}"
				+ "#TestResults tr:nth-child(even){background-color:#f2f2f2;}"
				+ "#TestResults tr:hover{background-color:#ddd;}"
				+ "#TestResults th{padding-top:12px;padding-bottom:12px;text-align:left;background-color:#04AA6D;color:white;}"
				+ "#pass{background-color:#9ff51d}#fail{background-color:#ffcccc}"
				+ "</style></head>"
				+ "<h1>Automation Test Result</h1>"
				+ (scenarioSection.isEmpty() ? "" : scenarioSection + "<br />")
				+ "<h2>Execution Summary</h2>"
				+ "<table id=TestResults><tr><th>Application</th><th>Environment</th>"
				+ "<th>Test Status</th><th>Report</th></tr>"
				+ "<tr><td>" + applicationTitle + "</td><td>" + env + "</td>"
				+ "<td><span id=pass>Passed : " + summaryPass + "</span> , "
				+ "<span id=fail>Failed : " + summaryFail + "</span></td>"
				+ "<td>" + reportLinksCell + "</td></tr></table>"
				+ "<br /><br />Execution Date : " + executionDate
				+ "<br /><br /><b>Regards,</b><br /><br />ViacomCBS </body>";
	}

	private static String buildLeftFilterAutomationReportEmail(List<TestResultRecord> rows, int summaryPass,
			int summaryFail, int summarySkip, String allureReportUrl, String pdfReportUrl) {
		String suiteTitle = Config.getString("LeftFilterEmailSuiteTitle");
		if (suiteTitle == null || suiteTitle.isBlank()) {
			suiteTitle = "Left Filter Validation";
		}
		File resultsDir = new File(System.getProperty("user.dir") + File.separator + "allure-results");
		String screenRecordingLink = AllureResultsReader.findScreenRecordingLink(resultsDir);
		return applyReportLogo(new ExecutionReportEmailBuilder()
				.applicationTitle(Config.getString("ApplicationTitle"))
				.suiteTitle(suiteTitle)
				.environment(Config.getString("TestEnvironment"))
				.synergyReportUrl(allureReportUrl)
				.pdfReportUrl(pdfReportUrl)
				.screenRecordingLink(screenRecordingLink)
				.testerName(resolveAutomationReportTesterName())
				.passCount(summaryPass)
				.failCount(summaryFail)
				.skipCount(summarySkip)
				.totalDurationMs(LeftFilterEmailReport.getSuiteDurationMs())
				.testResults(rows)).buildHtml();
	}

	private static ExecutionReportEmailBuilder applyReportLogo(ExecutionReportEmailBuilder builder) {
		return builder;
	}

	private static void sendAutomationReportEmail(String recipient, ExecutionReportEmailBuilder builder) {
		sendAutomationReportEmail(recipient, builder.buildSubject(), builder.buildHtml());
	}

	private static void sendAutomationReportEmail(String recipient, String subject, String htmlBody) {
		String sender = Config.getString("EmailSenderAddress");
		if (recipient == null || recipient.trim().isEmpty()) {
			Logger.logMessage("Email not sent: recipient is not configured.");
			return;
		}
		if (ExecutionReportMailer.isSmtpConfigured()
				&& ExecutionReportMailer.sendHtmlEmail(recipient, subject, htmlBody)) {
			return;
		}
		try {
			new Emailer(TestUtil.getServerUrl()).sendEmail(sender, recipient, subject, htmlBody);
			Logger.logMessage("Execution report sent to " + recipient);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.logMessage("Failed to send execution report to " + recipient);
		}
	}

	private static String resolveParamountLogoUrl() {
		if (cachedParamountLogoUrl != null && !cachedParamountLogoUrl.isBlank()) {
			return cachedParamountLogoUrl;
		}
		if (!AutomationReportAssets.hasParamountLogo()) {
			return "";
		}
		java.nio.file.Path logoPath = AutomationReportAssets.writeParamountLogoTempFile(PARAMOUNT_LOGO_UPLOAD_VERSION);
		if (logoPath == null) {
			return "";
		}
		try {
			FileUploader fileUploader = new FileUploader(ConfigProps.LAB_URL + "?key=" + ConfigProps.USER_KEY);
			String url = fileUploader.uploadFile(logoPath.toFile());
			if (url != null && !url.isBlank()) {
				cachedParamountLogoUrl = url.trim();
				Logger.logMessage("Paramount logo uploaded for report email: " + cachedParamountLogoUrl);
			}
			return cachedParamountLogoUrl;
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to upload Paramount logo for report email; using inline fallback.");
			e.printStackTrace();
			return "";
		}
	}

	private static final String FULFILLMENT_CONSOLE_TESTER_NAMES =
			"Akilandeswari Sundararajan, Babloo Kumar, Shubham Zurang";

	private static String resolveAutomationReportTesterName() {
		String configured = Config.getString("AutomationReportTesterName");
		if (configured != null && !configured.isBlank()) {
			return configured.trim();
		}
		String applicationTitle = Config.getString("ApplicationTitle");
		if (applicationTitle != null
				&& applicationTitle.toLowerCase().contains("fulfillment console")) {
			return FULFILLMENT_CONSOLE_TESTER_NAMES;
		}
		return "Automation Bot";
	}

	private static int countByStatus(List<TestResultRecord> rows, String status) {
		if (rows == null || rows.isEmpty()) {
			return 0;
		}
		int count = 0;
		for (TestResultRecord record : rows) {
			if (record != null && status.equalsIgnoreCase(record.getStatus())) {
				count++;
			}
		}
		return count;
	}

	private static String buildReportLinksCell(String allureReportUrl, String pdfReportUrl) {
		StringBuilder links = new StringBuilder();
		if (allureReportUrl != null && !allureReportUrl.isBlank()) {
			links.append("<a href='").append(escapeHtmlAttr(allureReportUrl)).append("'>SS</a>");
		}
		if (pdfReportUrl != null && !pdfReportUrl.isBlank()) {
			if (links.length() > 0) {
				links.append("<br/>");
			}
			links.append("<a href='").append(escapeHtmlAttr(pdfReportUrl)).append("'>PDF Report</a>");
		}
		if (links.length() == 0) {
			return "Not available";
		}
		return links.toString();
	}

	private static String escapeHtmlAttr(String value) {
		return value.replace("&", "&amp;").replace("'", "&#39;").replace("\"", "&quot;");
	}

	private static boolean shouldAttachPdfReport() {
		return Config.getBoolean("AttachPdfReport") || Config.getBoolean("UploadPDF");
	}

	private static boolean shouldUploadPdfToSynergy() {
		return Config.getBoolean("UploadPdfToSynergy") || Config.getBoolean("UploadPDF");
	}

	private static String uploadPdfToSynergy(File pdfFile) {
		try {
			FileUploader fileUploader = new FileUploader(ConfigProps.LAB_URL + "?key=" + ConfigProps.USER_KEY);
			String url = fileUploader.uploadFile(pdfFile);
			Logger.logMessage("PDF report uploaded to Synergy: " + url);
			return url == null ? "" : url;
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to upload PDF report to Synergy.");
			e.printStackTrace();
			return "";
		}
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
