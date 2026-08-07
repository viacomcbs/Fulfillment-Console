package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterSessionHelper;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Shared left-filter session lifecycle and optional scenario-wise email reporting.
 */
public class LeftFilterSuiteListener implements ISuiteListener, ITestListener {

    @Override
    public void onStart(ISuite suite) {
        LeftFilterSessionHelper.enableSharedSession();
        if (isEmailReportEnabled(suite)) {
            LeftFilterEmailReport.enable(suite.getParameter("LeftFilterEmailSuiteTitle"));
            Logger.logReportMessage("Left filter email report enabled for suite: " + suite.getName());
        }
    }

    private static boolean isEmailReportEnabled(ISuite suite) {
        return "true".equalsIgnoreCase(suite.getParameter("LeftFilterEmailReport"));
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
