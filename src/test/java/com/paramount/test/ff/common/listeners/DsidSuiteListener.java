package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.dsid.DsidEmailReport;
import com.paramount.test.ff.uitests.helpers.dsid.DsidSessionHelper;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestResult;
import org.testng.ITestListener;

public class DsidSuiteListener implements ISuiteListener, ITestListener {

    @Override
    public void onStart(ISuite suite) {
        DsidSessionHelper.enableSharedSession();
        if (isDsidSuite(suite)) {
            DsidEmailReport.enable();
            Logger.logReportMessage("DSID email report enabled for suite: " + suite.getName());
        }
    }

    private static boolean isDsidSuite(ISuite suite) {
        String name = suite.getName();
        return name != null && (name.contains("DSID") || name.contains("BSD-29870"));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        DsidEmailReport.recordResult(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        DsidEmailReport.recordResult(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        DsidEmailReport.recordResult(result);
    }

    @Override
    public void onFinish(ISuite suite) {
        if (DsidSessionHelper.isSharedSessionEnabled()) {
            DsidSessionHelper.stopSharedDriver();
        }
    }
}
