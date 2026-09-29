package com.paramount.test.ff.common.listeners;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.EmailUtil;
import com.paramount.test.ff.common.util.SynergyLocalPaths;
import com.paramount.test.ff.common.util.ExecuteFailedTests;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SlackMessenger;
import com.paramount.test.ff.common.util.TestUtil;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967EmailReport;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967JiraEvidenceUtil;
import com.paramount.test.ff.uitests.helpers.dsid.DsidEmailReport;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingEmailReport;
import com.synergy.common.SynergyKey;
import com.synergy.core.reporting.AllureReportGenerator;

public class SuiteListeners implements ISuiteListener, ITestListener {
	protected ExecuteFailedTests executeFailedTests;
	public static Boolean POST_TESTRAIL_DATA_LOCAL = Boolean.parseBoolean(ConfigProps.POST_DATA_TO_TESTRAIL);
	public static String BROWSER_NAME = "";
	public static String BROWSER_VERSION = "";

	@Override
	public void onStart(ISuite testSuite) {
		Config.applySuiteParameters(testSuite.getXmlSuite().getAllParameters());
		LeftFilterEmailReport.configureForSuite(testSuite);
		if (Config.isLocalExecution()) {
			String resolvedLocalUrl = SynergyLocalPaths.resolveLocalSynergyUrl();
			System.setProperty("system.test.localurl", resolvedLocalUrl);
			Logger.logMessage("Synergy local URL (resolved from synergy_running_port.txt): " + resolvedLocalUrl);
		}
		Logger.logMessage("Test environment: " + Config.getString("TestEnvironment")
				+ " | Target URL: " + Config.getTargetUrl());
		Logger.logMessage("Login user: " + Config.getString("Username"));
		if (Config.isLocalExecution()) {
			Logger.logMessage("Execution mode: LOCAL on ClientID " + Config.getString("ClientID")
					+ " via " + Config.getString("LocalURL"));
		} else {
			Logger.logMessage("Execution mode: Synergy server (SynergyPublicDevices)");
		}

		executeFailedTests = new ExecuteFailedTests();
		executeFailedTests.prepareTests();

		if (ConfigProps.EXECUTE_FAILED_CASES) {
			Logger.logMessage("*********************** Executing only Failed & Skipped Tests ***********************");
			Logger.logMessage("Total number of tests to be executed: " + testSuite.getAllMethods().size());
			testSuite.getAllMethods().stream().map(m -> m.getTestClass().getName() + "." + m.getMethodName())
					.forEach(System.out::println);
		}

		try {
			String projectDir = System.getProperty("user.dir");
			TestUtil.forceDelete(projectDir + File.separator + "allure-results.zip");
			File allureResultsDir = new File(projectDir + File.separator + "allure-results");
			if (allureResultsDir.exists()) {
				TestUtil.forceDelete(allureResultsDir.getAbsolutePath());
				Logger.logMessage("Cleared previous allure-results folder");
			}
		} catch (Exception e) {
			System.out.println("Failed to clear Allure previous reports.");
			e.printStackTrace();
		}
	}

	@Override
	public void onFinish(ISuite suite) {
		File allureResultDir = new File(System.getProperty("user.dir") + File.separator + "allure-results");
		System.out.println("Allure result directory: " + allureResultDir.getAbsolutePath());

		try {
			removeParametersInReport();
		} catch (Exception e) {
			Logger.logConsoleMessage("Allure parameter cleanup skipped: " + e.getMessage());
		}

		int[] localCounts = countAllureResultsFromDir(allureResultDir);
		int passCount = localCounts[0];
		int failCount = localCounts[1];
		int skipCount = localCounts[2];
		int brknCount = localCounts[3];
		String s3ReportUrl = "";

		boolean sendEmail = TestUtil.isLabExecution()
				|| "true".equalsIgnoreCase(Config.getString("SendReportAutoEmails"));
		if ("true".equalsIgnoreCase(Config.getString("SkipSuiteFinishAutoEmail"))
				|| isConsolidatedEmailOnlySuite(suite)) {
			sendEmail = false;
		}

		// Send email before slow Allure upload / Jira attach so a hung post-step cannot block delivery.
		if (sendEmail) {
			String recipient = Config.getString("SendReportEmailAddress");
			if (recipient == null || recipient.trim().isEmpty()) {
				Logger.logConsoleMessage(
						"SendReportAutoEmails is enabled but SendReportEmailAddress is empty; skipping email.");
			} else {
				try {
					if (PtsPackagingEmailReport.isEnabled()) {
						PtsPackagingEmailReport.captureSynergySessionId();
					} else if (Bsd29967EmailReport.isEnabled()) {
						Bsd29967EmailReport.captureSynergySessionId();
					} else if (DsidEmailReport.isEnabled()) {
						DsidEmailReport.captureSynergySessionId();
					}
					String internalRecipient = Config.getString("SendReportInternalEmailAddress");
					boolean dualLeftFilterEmail = LeftFilterEmailReport.isEnabled()
							&& internalRecipient != null && !internalRecipient.isBlank();
					if (dualLeftFilterEmail) {
						Logger.logConsoleMessage(
								"Sending stakeholder email (passed scenarios only) to " + recipient + "...");
						EmailUtil.sendResultEmail(s3ReportUrl, passCount, failCount, skipCount, brknCount, recipient,
								true);
						Logger.logConsoleMessage(
								"Sending internal email (full pass/fail status) to " + internalRecipient + "...");
						EmailUtil.sendResultEmail(s3ReportUrl, passCount, failCount, skipCount, brknCount,
								internalRecipient, false);
					} else {
						Logger.logConsoleMessage("Sending result email to " + recipient + " (before Allure/Jira)...");
						EmailUtil.sendResultEmail(s3ReportUrl, passCount, failCount, skipCount, brknCount);
					}
					sendSlackReportIfConfigured(passCount, failCount, skipCount);
				} catch (Exception e) {
					Logger.logConsoleMessage("Email sending failed: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} else {
			Logger.logMessage("Email skipped (SendReportAutoEmails=false and not Jenkins/CI lab execution).");
		}

		try {
			AllureReportGenerator allureReportGenerator = new AllureReportGenerator(
					SynergyKey.fromString(Config.getString("UserKey")),
					Config.getString("ApplicationTitle"),
					allureResultDir);

			JSONObject environmentJSON = new JSONObject();
			environmentJSON.put("Application", Config.getString("Application"));
			environmentJSON.put("TestEnvironment", Config.getString("TestEnvironment"));
			environmentJSON.put("OS", Config.getString("OS"));
			environmentJSON.put("Browser", BROWSER_NAME);
			environmentJSON.put("BrowserVersion", BROWSER_VERSION);
			allureReportGenerator.addEnvironmentProperties(environmentJSON);
			allureReportGenerator.setReportUploadThreads(20);
			allureReportGenerator.removePendingTests();
			allureReportGenerator.setUnexpectedSkippedTestsToFailures();
			allureReportGenerator.setBrokenTestsToFailures();
			allureReportGenerator.generateReport();

			s3ReportUrl = allureReportGenerator.getReportUrl();
			passCount = allureReportGenerator.getPassedTestCount();
			failCount = allureReportGenerator.getFailedTestCount();
			skipCount = allureReportGenerator.getSkippedTestCount();
			brknCount = allureReportGenerator.getBrokenTestCount();
			System.out.println("Report saved to: " + s3ReportUrl);
		} catch (Exception e) {
			Logger.logConsoleMessage(
					"Allure report upload failed after email was sent; using local result counts. Error: "
							+ e.getMessage());
			e.printStackTrace();
		}

		if (Bsd29967EmailReport.isEnabled() && failCount == 0 && brknCount == 0) {
			Bsd29967JiraEvidenceUtil.attachEvidenceOnPassIfConfigured();
		}

		appendToDailyAggregateIfConfigured(suite);

		executeFailedTests.createRerunFile();
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		LeftFilterEmailReport.recordResult(result);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		LeftFilterEmailReport.recordResult(result);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		LeftFilterEmailReport.recordResult(result);
	}

	private static void sendSlackReportIfConfigured(int passCount, int failCount, int skipCount) {
		if (!Config.getBoolean("SendChatReport")) {
			return;
		}
		String webhook = firstNonBlank(Config.getString("SlackWebhookUrl"), System.getenv("SLACK_WEBHOOK_URL"));
		if (webhook == null || webhook.isBlank()) {
			Logger.logConsoleMessage(
					"SendChatReport=true but SlackWebhookUrl / SLACK_WEBHOOK_URL not set; skipping Slack.");
			return;
		}
		String channel = firstNonBlank(Config.getString("SlackChannel"), System.getenv("SLACK_CHANNEL"));
		if (channel == null || channel.isBlank()) {
			Logger.logConsoleMessage("SendChatReport=true but SlackChannel / SLACK_CHANNEL not set; skipping Slack.");
			return;
		}
		int pass = LeftFilterEmailReport.isEnabled() ? LeftFilterEmailReport.getPassedCount() : passCount;
		int fail = LeftFilterEmailReport.isEnabled() ? LeftFilterEmailReport.getFailedCount() : failCount;
		int skip = LeftFilterEmailReport.isEnabled() ? LeftFilterEmailReport.getSkippedCount() : skipCount;
		String title = Config.getString("LeftFilterEmailSuiteTitle");
		if (title == null || title.isBlank()) {
			title = "Fulfillment Console — Left Filter Regression";
		}
		String env = Config.getString("TestEnvironment");
		String icon = fail > 0 ? "https://s3.amazonaws.com/mqeappprodpackages/failure.png"
				: "https://s3.amazonaws.com/mqeappprodpackages/success.png";
        String message = String.format("*%s* (%s)\nPass: %d | Fail: %d", title, env, pass, fail);
		try {
			new SlackMessenger(TestUtil.getServerUrl(), webhook).sendMessage(channel, message, icon);
			Logger.logConsoleMessage("Slack report sent to " + channel);
		} catch (Exception e) {
			Logger.logConsoleMessage("Slack report failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * When {@code AppendToDailyAggregate=true}, copies {@code target/surefire-reports/testng-results.xml}
	 * into {@code DailyAggregateDir} (defaults to {@code test-output/daily-aggregate/yyyy-MM-dd}) so
	 * multiple runs in one day can be merged into one end-of-day email.
	 */
	private static void appendToDailyAggregateIfConfigured(ISuite suite) {
		if (!Config.getBoolean("AppendToDailyAggregate")) {
			return;
		}
		if (isConsolidatedEmailOnlySuite(suite)) {
			return;
		}
		String dirPath = Config.getString("DailyAggregateDir");
		if (dirPath == null || dirPath.isBlank()) {
			dirPath = "test-output/daily-aggregate/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		File source = new File(System.getProperty("user.dir"), "target" + File.separator + "surefire-reports"
				+ File.separator + "testng-results.xml");
		if (!source.isFile()) {
			Logger.logConsoleMessage("Daily aggregate skipped — testng-results.xml missing: "
					+ source.getAbsolutePath());
			return;
		}
		File aggregateDir = new File(System.getProperty("user.dir"), dirPath.replace("/", File.separator));
		if (!aggregateDir.exists() && !aggregateDir.mkdirs()) {
			Logger.logConsoleMessage("Daily aggregate dir could not be created: " + aggregateDir.getAbsolutePath());
			return;
		}
		String suiteLabel = suite != null && suite.getName() != null ? suite.getName() : "suite";
		String safeName = suiteLabel.replaceAll("[^a-zA-Z0-9._-]+", "_");
		String timestamp = new SimpleDateFormat("HHmmss").format(new Date());
		File dest = new File(aggregateDir, safeName + "-" + timestamp + "-testng-results.xml");
		try {
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Logger.logConsoleMessage("Appended run to daily aggregate: " + dest.getAbsolutePath());
		} catch (IOException e) {
			Logger.logConsoleMessage("Failed to append daily aggregate copy: " + e.getMessage());
		}
	}

	private static boolean isConsolidatedEmailOnlySuite(ISuite suite) {
		if (suite == null || suite.getName() == null) {
			return false;
		}
		String name = suite.getName().toUpperCase();
		return name.contains("COMBINED EMAIL") || name.contains("CONSOLIDATED EMAIL");
	}

	private static String firstNonBlank(String primary, String fallback) {
		if (primary != null && !primary.isBlank()) {
			return primary.trim();
		}
		if (fallback != null && !fallback.isBlank()) {
			return fallback.trim();
		}
		return "";
	}

	private static int[] countAllureResultsFromDir(File allureResultDir) {
		int passed = 0;
		int failed = 0;
		int skipped = 0;
		int broken = 0;
		if (!allureResultDir.exists()) {
			return new int[] { passed, failed, skipped, broken };
		}
		File[] resultFiles = allureResultDir.listFiles((dir, name) -> name.endsWith("-result.json"));
		if (resultFiles == null) {
			return new int[] { passed, failed, skipped, broken };
		}
		JSONParser jsonParser = new JSONParser();
		for (File resultFile : resultFiles) {
			try (FileReader reader = new FileReader(resultFile)) {
				JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
				String status = jsonObject.get("status") != null ? jsonObject.get("status").toString() : "";
				switch (status.toLowerCase()) {
				case "passed":
					passed++;
					break;
				case "failed":
					failed++;
					break;
				case "skipped":
					skipped++;
					break;
				case "broken":
					broken++;
					break;
				default:
					break;
				}
			} catch (Exception e) {
				Logger.logConsoleMessage("Could not parse Allure result " + resultFile.getName() + ": "
						+ e.getMessage());
			}
		}
		return new int[] { passed, failed, skipped, broken };
	}

	public static void removeParametersInReport() throws Exception {
		File allureDir = new File(System.getProperty("user.dir") + File.separator + "allure-results");
		if (!allureDir.exists()) {
			return;
		}
		File[] directoryListing = allureDir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.getName().contains("result")) {
					removeParameterInJson(child);
				}
			}
		}
	}

	private static void removeParameterInJson(File fileToBeUpdated) {
		if (!fileToBeUpdated.exists() || fileToBeUpdated.length() == 0) {
			return;
		}
		try {
			FileReader reader = new FileReader(fileToBeUpdated);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
			reader.close();
			jsonObject.put("parameters", new LinkedList<>());
			try (FileWriter file = new FileWriter(fileToBeUpdated)) {
				file.write(jsonObject.toString());
			}
		} catch (Exception e) {
			Logger.logConsoleMessage("Could not update Allure result " + fileToBeUpdated.getName() + ": "
					+ e.getMessage());
		}
	}
}
