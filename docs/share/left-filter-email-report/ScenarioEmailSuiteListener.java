package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Optional standalone TestNG listener for scenario-wise email reporting.
 * Fulfillment Console uses global wiring in {@link SuiteListeners} instead;
 * copy this to other Synergy projects if needed.
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
