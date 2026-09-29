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
            if (isBatchSuite(suite)) {
                LeftFilterSessionHelper.enableBatchMode();
            }
            Logger.logReportMessage("Left filter shared session enabled for suite: " + suite.getName());
        }
    }

    private static boolean isBatchSuite(ISuite suite) {
        String param = suite.getParameter("LeftFilterBatchMode");
        if (param != null && "true".equalsIgnoreCase(param.trim())) {
            return true;
        }
        String name = suite.getName();
        return name != null && name.toLowerCase(java.util.Locale.ROOT).contains("batch");
    }

    private static boolean isLeftFilterSuite(ISuite suite) {
        String name = suite.getName();
        if (name == null) {
            return false;
        }
        String upper = name.toUpperCase(java.util.Locale.ROOT);
        return upper.startsWith("LF_") || upper.startsWith("LF ")
                || upper.contains("LEFT FILTER") || upper.contains("LEFTFILTER");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        recordAndSnapshot(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        recordAndSnapshot(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        recordAndSnapshot(result);
    }

    private static void recordAndSnapshot(ITestResult result) {
        LeftFilterEmailReport.recordResult(result);
        LeftFilterEmailReport.snapshotAggregateResultsIfConfigured();
    }

    @Override
    public void onFinish(ISuite suite) {
        if (LeftFilterEmailReport.hasFlowStepResults()) {
            String safeName = suite.getName() != null
                    ? suite.getName().replaceAll("[^A-Za-z0-9._-]", "_") : "lf-flow";
            java.io.File out = new java.io.File(System.getProperty("user.dir"),
                    "test-output/lf-o-flow-steps/" + safeName + "-flow-steps.xml");
            LeftFilterEmailReport.exportFlowSteps(out);
        }
        if (LeftFilterSessionHelper.isSharedSessionEnabled()) {
            LeftFilterSessionHelper.stopSharedDriver();
        }
    }
}
