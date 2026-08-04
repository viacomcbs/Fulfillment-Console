package com.paramount.test.ff.common.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.synergy.core.reporting.FileUploader;

/**
 * 
 * @author Anuja Kadloor This class will create appropriate folders required
 *         during test run, store execution results and create a map of test
 *         cases that were failed or skipped during previous execution.
 *         Executing failed or skipped tests will be done only if
 *         'executeOnlyFailedAndSkippedTests' flag is set.
 *
 */
public class ExecuteFailedTests {
	private static final ConcurrentMap<String, List<String>> failedTests = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, List<String>> allTests;
	private final String fileName = String.join(File.separator, System.getProperty("user.dir"), "rerun", "Test_Results_");
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private final List<String> testsToBeRerun = new ArrayList<>();
	File file = new File(fileName + sdf.format(new Timestamp(System.currentTimeMillis())) + ".xlsx");
	private final Map<String, String> rerunFiles = new HashMap<>();

	ExcelUtils excelUtils = new ExcelUtils();

	/**
	 * 
	 * @param testResults
	 */

	public void setFailedTests(ConcurrentMap<String, ConcurrentMap<String, String>> testResults) {
		List<String> data;
		for (Entry<String, ConcurrentMap<String, String>> entry : testResults.entrySet()) {
			data = new ArrayList<>();
			for (Map.Entry<String, String> innerMap : entry.getValue().entrySet()) {
				if (failedTests.containsKey(entry.getKey())) {
					failedTests.get(entry.getKey()).add(innerMap.getKey());
					failedTests.get(entry.getKey()).add(innerMap.getValue());
				} else {
					data.add(innerMap.getKey());
					data.add(innerMap.getValue());
					failedTests.put(entry.getKey(), data);
				}
			}

		}
	}

	/**
	 * Perform pre-requisite steps before test suite execution
	 */

	public void prepareTests() {
		// create rerun folder if not created
		File reRunFolder = new File(String.join(File.separator, System.getProperty("user.dir"), "ReRun_Report"));
		if (!reRunFolder.exists())
			reRunFolder.mkdir();

		// create screenshot folder if not created
		File reportDirectory = new File(
				String.join(File.separator, System.getProperty("user.dir"), "test-output", "screenshots"));
		if (!reportDirectory.exists())
			reportDirectory.mkdir();

		// read previous execution's data to get all tests
		if (ConfigProps.EXECUTE_FAILED_CASES)
			allTests = excelUtils.fetchAllTestResults();

	}

	/**
	 * 
	 * Excel file with results of all tests executed in current run would be
	 * created. Every module will be written in a separate worksheet. Summary
	 * sheet will give high level overview of the test result. Individual
	 * worksheets will contain detailed results.
	 */

	public void createRerunFile() {
		if (ConfigProps.EXECUTE_FAILED_CASES) {
			allTests.putAll(failedTests);
			excelUtils.createRerunFile(allTests);
		} else
			excelUtils.createRerunFile(failedTests);
	}

	/**
	 * 
	 * @return List of failed or skipped test cases
	 */

	public List<String> getEligibleTests() {
		ConcurrentMap<String, List<String>> dataMap = excelUtils.readPrevRerunReport();
		dataMap.forEach((key, value) -> {
			if (value.get(1).equals("F") || value.get(1).equals("S"))
				testsToBeRerun.add(key + "-" + value.get(0));
		});
		return testsToBeRerun;
	}

	public String storeLatestFileOnSynergyPlatform() {
		Path directory = Paths.get(System.getProperty("user.dir"), "ReRun_Report");

		Optional<File> mostRecentFile = Arrays.stream(directory.toFile().listFiles()).filter(f -> f.isFile())
				.max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

		// Upload rerun excel of every Jenkins node on SynergyTech Platform
		FileUploader fileUploader = new FileUploader("https://www.synergyserver.tech?key=" + ConfigProps.USER_KEY);
		String url = fileUploader.uploadFile(mostRecentFile.get());
		Logger.logMessage("Latest rerun report is available at: " + url);
		rerunFiles.put(mostRecentFile.get().getName(), url);
		return url;

	}
}
