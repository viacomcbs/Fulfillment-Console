package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967EmailReport;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967SessionHelper;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class Bsd29967SuiteListener implements ISuiteListener, ITestListener {

    @Override
    public void onStart(ISuite suite) {
        Bsd29967SessionHelper.enableSharedSession();
        if (isBsd29967Suite(suite)) {
            Bsd29967EmailReport.enable();
            Logger.logReportMessage("BSD-29967 email report enabled for suite: " + suite.getName());
        }
    }

    private static boolean isBsd29967Suite(ISuite suite) {
        String name = suite.getName();
        return name != null && (name.contains("BSD-29967") || name.contains("BSD29967"));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Bsd29967EmailReport.recordResult(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Bsd29967EmailReport.recordResult(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Bsd29967EmailReport.recordResult(result);
    }

    @Override
    public void onFinish(ISuite suite) {
        if (Bsd29967SessionHelper.isSharedSessionEnabled()) {
            Bsd29967SessionHelper.stopSharedDriver();
        }
    }
}
