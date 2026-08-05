package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingEmailReport;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingSessionHelper;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class PtsPackagingSuiteListener implements ISuiteListener, ITestListener {

    @Override
    public void onStart(ISuite suite) {
        PtsPackagingSessionHelper.enableSharedSession();
        if (isPtsPackagingSuite(suite)) {
            PtsPackagingEmailReport.enable(suite.getParameter("PtsConsoleTab"));
            Logger.logReportMessage("PTS Packaging email report enabled for suite: " + suite.getName());
        }
        String parallelMode = suite.getParameter("PtsParallelMode");
        if (parallelMode != null && !parallelMode.trim().isEmpty()) {
            PtsPackagingSessionHelper.setParallelMode(parallelMode);
            Logger.logReportMessage("PTS Packaging suite parallel mode: " + parallelMode);
        }
    }

    private static boolean isPtsPackagingSuite(ISuite suite) {
        String name = suite.getName();
        return name != null && (name.contains("PTS") || name.contains("BSD-29441"));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        PtsPackagingEmailReport.recordResult(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        PtsPackagingEmailReport.recordResult(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        PtsPackagingEmailReport.recordResult(result);
    }

    @Override
    public void onFinish(ISuite suite) {
        if (!PtsPackagingSessionHelper.isSharedSessionEnabled()) {
            return;
        }
        // Sequential suite: one shared driver stopped here.
        // Parallel modes stop drivers per test context or per class (@AfterClass).
        if (!PtsPackagingSessionHelper.isParallelExecution()) {
            PtsPackagingSessionHelper.stopSharedDriver();
        }
    }

    /** Parallel {@code tests} mode: stop Synergy session when Orders or Line Items block completes. */
    @Override
    public void onFinish(ITestContext context) {
        if (PtsPackagingSessionHelper.isSharedSessionEnabled()
                && PtsPackagingSessionHelper.isParallelTestsMode()) {
            PtsPackagingSessionHelper.stopSharedDriver();
        }
    }
}
