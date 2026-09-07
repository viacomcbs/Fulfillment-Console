package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterSessionHelper;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Left-filter shared browser session lifecycle.
 * Scenario email reporting is handled globally by {@link SuiteListeners}.
 */
public class LeftFilterSuiteListener implements ISuiteListener, ITestListener {

    @Override
    public void onStart(ISuite suite) {
        if (isLeftFilterSuite(suite)) {
            LeftFilterSessionHelper.enableSharedSession();
            Logger.logReportMessage("Left filter shared session enabled for suite: " + suite.getName());
        }
    }

    private static boolean isLeftFilterSuite(ISuite suite) {
        String name = suite.getName();
        if (name == null) {
            return false;
        }
        String upper = name.toUpperCase();
        return name.startsWith("LF_") || upper.contains("LEFT FILTER") || upper.contains("LEFTFILTER");
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

    @Override
    public void onFinish(ISuite suite) {
        if (LeftFilterSessionHelper.isSharedSessionEnabled()) {
            LeftFilterSessionHelper.stopSharedDriver();
        }
    }
}
