package com.paramount.test.ff.common.util.reporting;

import com.paramount.test.ff.common.util.EmailUtil;
import com.paramount.test.ff.common.util.Logger;

/**
 * Sends today's passed test cases from {@code allure-results} without running a full suite.
 */
public final class TodayPassedReportSender {

    private TodayPassedReportSender() {
    }

    public static void main(String[] args) {
        System.setProperty("suiteXmlFile", "src/test/resources/TestNGSuiteConfig.xml");
        System.setProperty("system.test.sendreportautoemails", "true");
        System.setProperty("system.test.consolidatereportemail", "false");
        System.setProperty("system.test.finalizeconsolidatedreport", "false");

        boolean yesterday = args.length > 0 && "yesterday".equalsIgnoreCase(args[0].trim());
        String suiteTitle = null;
        if (args.length > 0 && !yesterday) {
            suiteTitle = args[0];
        } else if (args.length > 1) {
            suiteTitle = args[1];
        }
        try {
            if (yesterday) {
                EmailUtil.sendYesterdayPassedReport(suiteTitle);
                Logger.logMessage("Yesterday's passed test report dispatch completed.");
            } else {
                EmailUtil.sendTodayPassedReport(suiteTitle);
                Logger.logMessage("Today's passed test report dispatch completed.");
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to send today's passed test report.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
