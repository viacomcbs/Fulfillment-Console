package com.paramount.test.ff.common.util;

import java.util.ArrayList;
import java.util.List;

import com.paramount.test.ff.common.util.props.IProps.ConfigProps;

//import ru.yandex.qatools.allure.annotations.TestCaseId;

public final class TestRailUtil {

	private TestRailUtil() {

	}

	//private static TestRailUpdater testRailUpdater;
	private static String testRailRunUrl;
	private static final List<String> testCaseIds = new ArrayList<>();
	public static Boolean POST_TESTRAIL_DATA = Boolean.parseBoolean(System.getenv("POST_TESTRAIL_DATA"));
	public static Boolean POST_TESTRAIL_DATA_LOCAL = Boolean.parseBoolean(ConfigProps.POST_DATA_TO_TESTRAIL);
	
	/*public static void startTestRailRun(ISuite suite, String testRunName) {
		if (POST_TESTRAIL_DATA || POST_TESTRAIL_DATA_LOCAL) {
		//int testRailProjectId = ConfigProps.TESTRAIL_PROJECT_ID;
		//	Logger.logConsoleMessage("Initiating TestRail run name '" + testRunName + "' against project id '"
			//		+ testRailProjectId + "'.");

			suite.getAllMethods().forEach(test -> {
				TestCaseId annotation = test.getConstructorOrMethod().getMethod().getAnnotation(TestCaseId.class);
				if (annotation != null) {
					String[] ids = annotation.value().split(",");
					testCaseIds.addAll(Arrays.asList(ids));
				}
			});

			if (testCaseIds.isEmpty()) {
				Logger.logConsoleMessage("No tests annotated with @TestCaseId, skipping TestRail suite run.");
				return;
			}
			Set<String> testIds = new HashSet<String>(testCaseIds);

			//testRailUpdater = new TestRailUpdater(TestUtil.getServerUrl(), testRailProjectId, testRunName,
				//	"Lab Execution");
			//testRailRunUrl = testRailUpdater.startTestRun(testIds);
			testRailRunUrl = "Unknown".equalsIgnoreCase(testRailRunUrl) ? null : testRailRunUrl;

			Logger.logConsoleMessage("Started TestRail run at: " + testRailRunUrl);
		}
	}

	public static String getRunUrl() {
		return testRailRunUrl;
	}


	public static void updateTestRailResult(ITestResult result) {
		if (POST_TESTRAIL_DATA || POST_TESTRAIL_DATA_LOCAL) {
			TestCaseId annotation = result.getMethod().getConstructorOrMethod().getMethod()
					.getAnnotation(TestCaseId.class);
			if (annotation != null) {
				String[] ids = annotation.value().split(",");
				try {
			//		Arrays.stream(ids).forEach(id -> testRailUpdater.addTestCaseResult(id, testRailResultFrom(result)));
				} catch (Exception e) {
					Logger.logConsoleMessage("Error while updating testrail test case ids.");
					e.printStackTrace();
				}
			}
		}
	}



	private static TestRailResultType testRailResultFrom(ITestResult result) {
		switch (result.getStatus()) {
		case ITestResult.SUCCESS:
			return TestRailResultType.SUCCESS;
		case ITestResult.FAILURE:
			return TestRailResultType.FAILURE;
		case ITestResult.SKIP:
			return TestRailResultType.BROKEN;
		case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
			return TestRailResultType.BROKEN;
		}
		return TestRailResultType.FAILURE;
	}

	 */
}
