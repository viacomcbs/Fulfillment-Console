package com.paramount.test.ff.common.listeners;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
					} else if (LeftFilterEmailReport.isEnabled()) {
						LeftFilterEmailReport.captureSynergySessionId();
					}
					Logger.logConsoleMessage("Sending result email to " + recipient + " (before Allure/Jira)...");
					EmailUtil.sendResultEmail(s3ReportUrl, passCount, failCount, skipCount, brknCount);
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
