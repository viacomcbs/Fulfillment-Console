package com.paramount.test.ff.common.listeners;


import org.testng.IAnnotationTransformer;

public class TestAnnotationTransformer implements IAnnotationTransformer {
	/*ExecuteFailedTests executeFailedTests = new ExecuteFailedTests();

	@SuppressWarnings({ "rawtypes" })
	public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
		// disable a test if it doesn't match our specific runtime requirements.
		List<Annotation> testAnnotations = Arrays.asList(testMethod.getAnnotations());
		List<String> parameterAnnotations = new ArrayList<String>();

		for (Annotation testAnnotation : testAnnotations) {
			if (testAnnotation instanceof Parameters) {
				parameterAnnotations.add(testAnnotation.toString());
			}
		}

		// mark the test for retry analysis
		IRetryAnalyzer retry = annotation.getRetryAnalyzer();
		//IRetryAnalyzer retry = annotation.getRetryAnalyzer();
		if (retry == null) {
			annotation.setRetryAnalyzer(TestListeners.class);
		}
		/************************
		 * RERUN LOGIC STARTS HERE
		 ****************************
		// @Author: Anuja
		// Re-run failed test cases

		if (ConfigProps.EXECUTE_FAILED_CASES) {
			List<String> testsToBeRerun = new ArrayList<>();
			String[] groupToBeIncluded = { "Rerun" };
			String declaringClass = String.valueOf(testMethod.getDeclaringClass());
			declaringClass = declaringClass.substring(declaringClass.lastIndexOf(".") + 1);
			String testName = declaringClass + "-" + testMethod.getName();

			testsToBeRerun = executeFailedTests.getEligibleTests();

			if (testsToBeRerun.contains(testName))
				annotation.setGroups(groupToBeIncluded);
		}
	}


	*/
}