package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Standalone TestNG listener for scenario-wise email reporting.
 * Copy to your project and update the package + imports.
 *
 * <p>Records pass/fail/skip per test class. Pair with {@link LeftFilterEmailReport#configureForSuite(ISuite)}
 * in your SuiteListeners.onStart (or call configureForSuite here).
 */
public class ScenarioEmailSuiteListener implements ISuiteListener, ITestListener {

    @Override
    public void onStart(ISuite suite) {
        LeftFilterEmailReport.configureForSuite(suite);
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
}
