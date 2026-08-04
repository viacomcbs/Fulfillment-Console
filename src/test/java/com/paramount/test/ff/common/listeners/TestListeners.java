package com.paramount.test.ff.common.listeners;

import org.testng.ITestListener;

public class TestListeners implements ITestListener {
//implements IRetryAnalyzer, ITestListener, IInvokedMethodListener
	/*private static final int MAX_COUNT = ConfigProps.RERUN_ON_FAILURE_COUNT;
 	private Map<String, AtomicInteger> retries = new HashMap<String, AtomicInteger>();
	private IResultMap failedCases = new ResultMap();
	private static String RUN_PROPS = "runProps";
	private static ThreadLocal<String> testID = new ThreadLocal<String>();
	private static ThreadLocal<String> testName = new ThreadLocal<String>();
	public static int cnt = 0;
	private boolean isLocalScreenshot = true;
	private ExecuteFailedTests executeFailedTests = new ExecuteFailedTests();
	public static Boolean POST_TESTRAIL_DATA_LOCAL = Boolean.parseBoolean(ConfigProps.POST_DATA_TO_TESTRAIL);
	public static Boolean POST_SPLUNK_DATA_LOCAL = Boolean.parseBoolean(ConfigProps.POST_SPLUNK);
	public static boolean TEST_STATUS_UPDATED;
	public static String SESSION_ID_TAG="";

	@Override
	public void onFinish(final ITestContext context) {
		Logger.logConsoleMessage("========TEST FINISHED========");
	}

	@Override
	public void onStart(final ITestContext test) {
		Logger.logConsoleMessage("========TEST STARTED=========");
		TEST_STATUS_UPDATED = false;
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(final ITestResult result) {

	}

	@Override
	public void onTestStart(final ITestResult result) {
		try {
			testName.set(result.getName());
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to set Test Name.");
		}
		try {
			// set attribute as a test id
			result.setAttribute(RUN_PROPS, new Object[] { TestRun.getOS().value() + TestRun.getBrowser().value() });

			// set the test id
			 String id = IProps.StaticProps.TEST_ID + result.getMethod().getMethodName() + " (" + IProps.ConfigProps.OS
	                    + " " + IProps.ConfigProps.BROWSER + ")";
	            testID.set(id.toLowerCase());
			
			Logger.logMessage(testID.get());
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to log test ids.");
		}

		try {
			// log the test initiation
			Logger.logMessage("========NEW TEST SESSION========");

		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to log startup data.");
			e.printStackTrace();
		}

		String sessionIDLogTag = AllureReportGenerator.getSessionIDLogTag(BaseTest.driver.get());
		SESSION_ID_TAG= sessionIDLogTag;
		Logger.log(sessionIDLogTag);
	}

	@Override
	public void onTestSuccess(final ITestResult result) {
		String className = result.getInstanceName().substring(result.getInstanceName().lastIndexOf(".") + 1);
		String testngMethod = String.valueOf(result.getMethod().getMethodName());
		String packageName = result.getInstance().getClass().getPackage().getName();
		try {
			Logger.logConsoleMessage("======TEST CASE SUCCESS======");
			Logger.logConsoleMessage("Test: " + result.getInstanceName() + "." + result.getName());

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// add the test passed id
			if(!TEST_STATUS_UPDATED)
				TestIDs.addPassedTest(testID.get());
			// Update Test Rail ID
			if (!TestUtil.isLabExecution() || POST_TESTRAIL_DATA_LOCAL) {
				//TestRailUtil.updateTestRailResult(result);
			}
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to log test id to results.");
			e.printStackTrace();
		}
		if (!TestUtil.isLabExecution() || POST_SPLUNK_DATA_LOCAL) {
			SplunkPost.postLabTestResult(result);
		}
		AllureAttachment.attachScreenRecordingLink();
		//AllureAttachment.attachScreenshot();
		//AllureAttachment.attachScreenRecording();
		//Failed TC code starts here
		setTestCaseStatus(packageName, className, testngMethod, StaticProps.PASS_FLG);

	}

	public void onTestFailure1(final ITestResult result) {
		String className = result.getInstanceName().substring(result.getInstanceName().lastIndexOf(".") + 1);
		String testngMethod = String.valueOf(result.getMethod().getMethodName());
		String packageName = result.getInstance().getClass().getPackage().getName();
		if (isLocalScreenshot) {
			// Add local failure screenshot
			try {
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat formater = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");
				String methodName = result.getName();
				if (!result.isSuccess()) {
					// File scrFile =
					// BaseTest.webDriver.get().browser().getScreenshot();
					File scrFile = BaseTest.driver.get().screen().getImage();
					try {
						String reportDirectory = new File(System.getProperty("user.dir")).getAbsolutePath()
								+ File.separator + "test-output" + File.separator + "screenshots" + File.separator
								+ System.getProperty("ScreenshotDirectory") + File.separator + "failure";
						System.out.println(reportDirectory);
						File destFile = new File((String) reportDirectory + File.separator + methodName + "_"
								+ formater.format(calendar.getTime()) + ".png");
						FileUtils.copyFile(scrFile, destFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			Logger.logConsoleMessage("======TEST CASE FAILURE======");
			Logger.logConsoleMessage("Test: " + result.getInstanceName() + "." + result.getName());
			//AllureAttachment.attachFailureScreenshot();
			//AllureAttachment.attachScreenRecording();
			AllureAttachment.attachScreenRecordingLink();
			if(!TEST_STATUS_UPDATED)
				TestIDs.addFailedTest(testID.get());
			failedCases.addResult(result, result.getMethod());

			// Splunkpost
			if (!TestUtil.isLabExecution() || POST_SPLUNK_DATA_LOCAL) {
				SplunkPost.postLabTestResult(result);
			}

		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to attach test artifacts.");
			e.printStackTrace();
		}

		try {
			// add the test failed id
			if (!TestUtil.isLabExecution() || POST_TESTRAIL_DATA_LOCAL) {
				//TestRailUtil.updateTestRailResult(result);
			}
			// check if the test should be re-executed based on retry logic
			/*
			 * if (result.getMethod().getRetryAnalyzer() != null &&
			 * ConfigProps.RERUN_ON_FAILURE) {
			 * Logger.logMessage("Retryig failed test cases"); TestListeners
			 * testRetryAnalyzer = (TestListeners) result.getMethod().getRetryAnalyzer(); if
			 * (testRetryAnalyzer.getCount(result.getMethod(),
			 * result.getAttribute(RUN_PROPS)).intValue() > 0) {
			 * result.setStatus(ITestResult.FAILURE); } else { failedCases.addResult(result,
			 * result.getMethod()); } }

		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to log test id to results.");
			e.printStackTrace();
		}


		 //* RERUN FAILED CASES CODE STARTS HERE

		// @Author: Anuja
		setTestCaseStatus(packageName, className, testngMethod, StaticProps.FAIL_FLG);
	}

	@Override
	public void onTestSkipped(final ITestResult result) {
		String className = result.getInstanceName().substring(result.getInstanceName().lastIndexOf(".") + 1);
		String testngMethod = String.valueOf(result.getMethod().getMethodName());
		String packageName = StringUtils.substringBeforeLast(result.getInstanceName(), ".");
		Logger.logConsoleMessage("======SKIPPED======");
		Logger.logConsoleMessage("Test: " + result.getInstanceName() + "." + result.getName());

		if(!TEST_STATUS_UPDATED)
			TestIDs.addSkippedTest(testID.get());
		if (!TestUtil.isLabExecution()) {
			// TestRailUtil.updateTestRailResult(result);
		}
		try {
			BaseTest.driver.get().stop();
			BaseTest.driver.remove();
			Logger.logConsoleMessage("WebDriver stopped after test case skipped.");
		} catch (Exception e) {
			BaseTest.driver.remove();
			Logger.logConsoleMessage("Failed to stop driver in first attempt.");
		}
		try {
			BaseTest.desktopDriver.get().stop();
			BaseTest.desktopDriver.remove();
			Logger.logConsoleMessage("Desktop driver stopped after test case skipped.");
		} catch (Exception e) {
			BaseTest.desktopDriver.remove();
			Logger.logConsoleMessage("Failed to stop Desktop driver.");
		}
		 //* RERUN FAILED CASES CODE STARTS HERE
		// @Author: Anuja
		setTestCaseStatus(packageName, className, testngMethod, StaticProps.SKIP_FLG);
	}


	//@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		IRetryAnalyzer retryAnalyzer = testResult.getMethod().getRetryAnalyzer(testResult);
		if (retryAnalyzer != null) {
			if (testResult.getStatus() == ITestResult.SKIP && testResult.getThrowable() != null
					&& !testResult.getThrowable().toString().contains("Test Skipped due to")) {
				testResult.setStatus(ITestResult.FAILURE);
				Reporter.setCurrentTestResult(testResult);
			}

			if (method.isTestMethod() && testResult.getStatus() == ITestResult.FAILURE
					&& testResult.getThrowable() != null) {
				if (testResult.getThrowable().toString().contains("WebDriverException")) {
					Logger.logMessage(
							"<--- Test mark as skipped due to WebDriverException. Might be MQE lab issue, Please check console log to debug. --->");
					testResult.setStatus(ITestResult.SKIP);
					Reporter.setCurrentTestResult(testResult);
				} else if (testResult.getThrowable().toString().contains("NoSuchSessionException")) {
					Logger.logMessage(
							"<--- Test mark as skipped due to NoSuchSessionException. Might be MQE lab issue, Please check console log to debug. --->");
					testResult.setStatus(ITestResult.SKIP);
					Reporter.setCurrentTestResult(testResult);
				} else if (testResult.getThrowable().toString().contains("ContentException")) {
					Logger.logMessage(
							"<--- Test mark as skipped due to ContentException. Might be a DP issue. Please check with content team --->");
					testResult.setStatus(ITestResult.SKIP);
					Reporter.setCurrentTestResult(testResult);
				} else if (testResult.getThrowable().toString().contains("InternetConnectionException")) {
					Logger.logMessage(
							"<--- Test mark as skipped due to InternetConnectionException. Might be a network issue. Please check your internet connection --->");
					testResult.setStatus(ITestResult.SKIP);
					Reporter.setCurrentTestResult(testResult);
				} else if (testResult.getThrowable().toString().contains("SessionNotStartedException")) {
					Logger.logMessage(
							"<--- Test mark as skipped due to session spinup error. Might be a lab issue. Please check your internet connection --->");
					testResult.setStatus(ITestResult.SKIP);
					Reporter.setCurrentTestResult(testResult);
				}
			}
		}
	}

	public boolean retry(ITestResult result) {
		boolean retry = false;
		if (ConfigProps.RERUN_ON_FAILURE) {
			if (getCount(result.getMethod(), result.getAttribute(RUN_PROPS)).intValue() > 0) {
				Logger.logConsoleMessage("RETRY TEST: " + result.getInstanceName() + "." + result.getName());
				getCount(result.getMethod(), result.getAttribute(RUN_PROPS)).decrementAndGet();
				retry = true;
			} else {
				cnt = 1;
				Logger.logConsoleMessage("RETRY COMPLETE: " + result.getInstanceName() + "." + result.getName());
			}
		}
		return retry;
	}

	private AtomicInteger getCount(ITestNGMethod result, Object attribute) {
		String id = getId(result, attribute);
		if (retries.get(id) == null) {
			retries.put(id, new AtomicInteger(MAX_COUNT));
		}
		return retries.get(id);
	}

	private String getId(ITestNGMethod result, Object attribute) {
		return result.getConstructorOrMethod().getMethod().toGenericString() + ":" + ArrayUtils.toString(attribute);
	}

	public static synchronized String getTestName() {
		return testName.get();
	}

	 //* RERUN FAILED CASES CODE STARTS HERE

	private void setTestCaseStatus(String packageName, String className, String methodName, String testCaseStatus) {
		ConcurrentMap<String, ConcurrentMap<String, String>> tcMap = new ConcurrentHashMap<>();
		ConcurrentMap<String, String> testResults = new ConcurrentHashMap<>();

		packageName = StringUtils.substringAfterLast(packageName, ".tests.");
		testResults.put(methodName, testCaseStatus);
		tcMap.put(packageName + "-" + className, testResults);
		executeFailedTests.setFailedTests(tcMap);
	}
	
	@Override
    public void onTestFailure(final ITestResult result) {
		String className = result.getInstanceName().substring(result.getInstanceName().lastIndexOf(".") + 1);
		String testngMethod = String.valueOf(result.getMethod().getMethodName());
		String packageName = result.getInstance().getClass().getPackage().getName();
        try {
            Logger.logConsoleMessage("======TEST CASE FAILURE======");
            Logger.logConsoleMessage("Test: " + result.getInstanceName() + "." + result.getName());
            TestIDs.addFailedTest(testID.get());
            failedCases.addResult(result, result.getMethod());


            // attach Artifacts
            if (isLocalScreenshot) {
                //AllureAttachment.attachFailureScreenshot();
				AllureAttachment.attachScreenRecordingLink();
               // AllureAttachment.attachScreenRecording();
               // AllureAttachment.attachScreenshot();
            }

            // Splunkpost
            if (TestUtil.isLabExecution() || POST_SPLUNK_DATA_LOCAL) {
				SplunkPost.postLabTestResult(result);
			}
            
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to attach test artifacts.");
            e.printStackTrace();
        }

        try {
            // add the test failed id
            if (TestUtil.isLabExecution()) {
                //TestRailUtil.updateTestRailResult(result);
            }
     
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to log test id to results.");
            e.printStackTrace();
        }

        try {
            CapabilityFactory.getDriver().stop();
            Logger.logConsoleMessage("WebDriver stopped after test case failure.");
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to stop driver in first attempt.");
        }
        getTextMessage(getTestName()+" Failed and screenshot taken!");
        
         //* RERUN FAILED CASES CODE STARTS HERE

		
		setTestCaseStatus(packageName, className, testngMethod, StaticProps.FAIL_FLG);

    }
	
	@Attachment(value= "{0}", type="text/plain")
	public static String getTextMessage(String message) {
		return message;
	}

	 */


}