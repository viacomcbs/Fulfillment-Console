package com.paramount.test.ff.uitests.helpers.ptspackaging;

import com.paramount.test.ff.common.util.AllureAttachment;

import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects TestNG results for BSD-29441 PTS Packaging ID suites and builds the HTML summary table
 * included in the execution email.
 */
public final class PtsPackagingEmailReport {

    public static final String JIRA_KEY = "BSD-29441";
    public static final String JIRA_URL = "https://paramount.atlassian.net/browse/BSD-29441";
    public static final String JIRA_SUMMARY =
            "Add PTS Packaging ID column to Orders and Line Items tables";
    public static final String FEATURE_TITLE = "PTS Packaging ID";

    private static final String[] ORDERS_CLASS_ORDER = {
            "FF_PTS_O_001_ValidateColumnAvailableInTableView",
            "FF_PTS_O_002_ValidateColumnVisibleInGrid",
            "FF_PTS_O_011_ValidateColumnSort",
            "FF_PTS_O_009_ValidateColumnSearch",
            "FF_PTS_O_015_ValidateTableMatchesDetails",
            "FF_PTS_O_013_ValidateColumnInExcelExport",
            "FF_PTS_O_017_ValidateColumnHiddenWhenUnselectedAndExportExcludes"
    };

    private static final String[] LINE_ITEMS_CLASS_ORDER = {
            "FF_PTS_LI_001_ValidateColumnAvailableInTableView",
            "FF_PTS_LI_002_ValidateColumnVisibleInGrid",
            "FF_PTS_LI_011_ValidateColumnSort",
            "FF_PTS_LI_009_ValidateColumnSearch",
            "FF_PTS_LI_013_ValidateColumnInExcelExport",
            "FF_PTS_LI_018_ValidateColumnHiddenWhenUnselectedAndExportExcludes"
    };

    private static final Map<String, String> SCENARIO_BY_CLASS = new LinkedHashMap<>();

    static {
        SCENARIO_BY_CLASS.put("FF_PTS_O_001_ValidateColumnAvailableInTableView",
                "PTS Packaging ID available in Table View (Manage Columns)");
        SCENARIO_BY_CLASS.put("FF_PTS_O_002_ValidateColumnVisibleInGrid",
                "PTS Packaging ID column visible in grid");
        SCENARIO_BY_CLASS.put("FF_PTS_O_009_ValidateColumnSearch",
                "PTS Packaging ID column search");
        SCENARIO_BY_CLASS.put("FF_PTS_O_011_ValidateColumnSort",
                "PTS Packaging ID column sorting");
        SCENARIO_BY_CLASS.put("FF_PTS_O_015_ValidateTableMatchesDetails",
                "PTS Packaging ID data comparison with details panel");
        SCENARIO_BY_CLASS.put("FF_PTS_O_013_ValidateColumnInExcelExport",
                "PTS Packaging ID export to Excel");
        SCENARIO_BY_CLASS.put("FF_PTS_O_017_ValidateColumnHiddenWhenUnselectedAndExportExcludes",
                "PTS Packaging ID unselect column + export excludes column");
        SCENARIO_BY_CLASS.put("FF_PTS_LI_001_ValidateColumnAvailableInTableView",
                "PTS Packaging ID available in Table View (Manage Columns)");
        SCENARIO_BY_CLASS.put("FF_PTS_LI_002_ValidateColumnVisibleInGrid",
                "PTS Packaging ID column visible in grid");
        SCENARIO_BY_CLASS.put("FF_PTS_LI_009_ValidateColumnSearch",
                "PTS Packaging ID column search");
        SCENARIO_BY_CLASS.put("FF_PTS_LI_011_ValidateColumnSort",
                "PTS Packaging ID column sorting");
        SCENARIO_BY_CLASS.put("FF_PTS_LI_013_ValidateColumnInExcelExport",
                "PTS Packaging ID export to Excel");
        SCENARIO_BY_CLASS.put("FF_PTS_LI_018_ValidateColumnHiddenWhenUnselectedAndExportExcludes",
                "PTS Packaging ID unselect column + export excludes column");
    }

    private static final Map<String, String> RESULTS = new LinkedHashMap<>();
    private static boolean enabled;
    private static String consoleTab = "";
    private static String synergySessionId = "";

    private PtsPackagingEmailReport() {
    }

    public static void enable() {
        enable(null);
    }

    public static void enable(String tabLabel) {
        enabled = true;
        RESULTS.clear();
        consoleTab = tabLabel != null ? tabLabel.trim() : "";
        synergySessionId = "";
    }

    public static void disable() {
        enabled = false;
        RESULTS.clear();
        consoleTab = "";
        synergySessionId = "";
    }

    public static void setSynergySessionId(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            synergySessionId = sessionId.trim();
        }
    }

    /** Captures session ID from the active driver, or keeps the last stored value. */
    public static void captureSynergySessionId() {
        String fromDriver = AllureAttachment.captureSessionIdFromDriver();
        if (!fromDriver.isEmpty()) {
            setSynergySessionId(fromDriver);
        }
    }

    public static String getSynergyRecordingUrl() {
        return AllureAttachment.buildRecordingUrl(synergySessionId);
    }

    public static boolean isEnabled() {
        return enabled && !RESULTS.isEmpty();
    }

    public static boolean isPtsTestClass(String classSimpleName) {
        return classSimpleName != null && classSimpleName.startsWith("FF_PTS_");
    }

    public static void recordResult(ITestResult result) {
        if (!enabled || result == null) {
            return;
        }
        String className = result.getTestClass().getRealClass().getSimpleName();
        if (!isPtsTestClass(className)) {
            return;
        }
        String scenario = SCENARIO_BY_CLASS.getOrDefault(className, className);
        String status = resolveStatus(result.getStatus());
        RESULTS.put(className, scenario + "|" + status);
    }

    public static int getPassedCount() {
        return (int) RESULTS.values().stream().filter(v -> v.endsWith("|PASS")).count();
    }

    public static int getFailedCount() {
        return (int) RESULTS.values().stream().filter(v -> v.endsWith("|FAIL")).count();
    }

    public static int getSkippedCount() {
        return (int) RESULTS.values().stream().filter(v -> v.endsWith("|SKIP")).count();
    }

    public static int getTotalCount() {
        return RESULTS.size();
    }

    public static String getTableTitle() {
        String tab = resolveConsoleTab();
        if (tab.isEmpty()) {
            return FEATURE_TITLE + " — " + JIRA_KEY + ": " + resolveJiraSummary();
        }
        return FEATURE_TITLE + " — " + tab + " — " + JIRA_KEY + ": " + resolveJiraSummary();
    }

    private static String resolveConsoleTab() {
        if (!consoleTab.isEmpty()) {
            return consoleTab;
        }
        String fromConfig = com.paramount.test.ff.common.util.Config.getString("PtsConsoleTab");
        return fromConfig != null ? fromConfig.trim() : "";
    }

    private static String resolveJiraSummary() {
        String fromConfig = com.paramount.test.ff.common.util.Config.getString("JiraStoryTitle");
        if (fromConfig == null || fromConfig.trim().isEmpty()) {
            fromConfig = com.paramount.test.ff.common.util.Config.getString("JiraTicketSummary");
        }
        return fromConfig != null && !fromConfig.trim().isEmpty() ? fromConfig.trim() : JIRA_SUMMARY;
    }

    public static String buildHtmlSection() {
        captureSynergySessionId();
        String recordingUrl = getSynergyRecordingUrl();
        String tableTitle = getTableTitle();
        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(FEATURE_TITLE).append("</h2>");
        html.append("<p><b>Jira Story:</b> <a href=\"").append(JIRA_URL).append("\">").append(JIRA_KEY)
                .append("</a> — ").append(resolveJiraSummary()).append("</p>");
        html.append("<p><b>Synergy screen recording:</b> ");
        if (recordingUrl.isEmpty()) {
            html.append("<i>Not available — session may have ended before the link was captured.</i>");
        } else {
            html.append("<a href=\"").append(recordingUrl).append("\">Open session recording</a>");
        }
        html.append("</p>");
        html.append("<p><b>Automated TestNG scripts:</b> ").append(getTotalCount())
                .append(" &nbsp;|&nbsp; <span style=\"color:green\"><b>Passed: ")
                .append(getPassedCount()).append("</b></span>");
        if (getFailedCount() > 0) {
            html.append(" &nbsp;|&nbsp; <span style=\"color:red\"><b>Failed: ")
                    .append(getFailedCount()).append("</b></span>");
        }
        if (getSkippedCount() > 0) {
            html.append(" &nbsp;|&nbsp; <span style=\"color:orange\"><b>Skipped: ")
                    .append(getSkippedCount()).append("</b></span>");
        }
        html.append("</p>");
        html.append("<table id=\"PtsResults\" style=\"border-collapse:collapse;width:100%;"
                + "font-family:Calibri,Helvetica,sans-serif;font-size:medium;margin-top:8px;\">");
        html.append("<caption style=\"caption-side:top;text-align:left;font-size:large;font-weight:bold;"
                + "padding:8px 0;color:#1a1a1a;\">").append(tableTitle).append("</caption>");
        html.append("<tr style=\"background-color:#04AA6D;color:white;\">");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">#</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Scenario</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Manual ID</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Status</th>");
        html.append("</tr>");

        int row = 1;
        for (String className : orderedClassNames()) {
            if (!RESULTS.containsKey(className)) {
                continue;
            }
            String[] parts = RESULTS.get(className).split("\\|", 2);
            String scenario = parts[0];
            String status = parts.length > 1 ? parts[1] : "UNKNOWN";
            String manualId = manualIdForClass(className);
            String rowColor = statusBgColor(status);
            String statusColor = statusTextColor(status);
            html.append("<tr style=\"background-color:").append(rowColor).append(";\">");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(row++).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(scenario).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(manualId).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;color:")
                    .append(statusColor).append(";\"><b>").append(status).append("</b></td>");
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("<p style=\"font-size:small;color:#555;margin-top:10px;\">")
                .append("<i>Note: The Synergy Allure report may show a higher number (e.g. 36) because ")
                .append("each verification step inside a test is logged as a separate Allure step. ")
                .append("This table shows the ").append(getTotalCount())
                .append(" TestNG automation scripts only.</i></p>");
        return html.toString();
    }

    private static List<String> orderedClassNames() {
        List<String> ordered = new ArrayList<>();
        for (String className : ORDERS_CLASS_ORDER) {
            if (RESULTS.containsKey(className)) {
                ordered.add(className);
            }
        }
        for (String className : LINE_ITEMS_CLASS_ORDER) {
            if (RESULTS.containsKey(className)) {
                ordered.add(className);
            }
        }
        for (String className : RESULTS.keySet()) {
            if (!ordered.contains(className)) {
                ordered.add(className);
            }
        }
        return ordered;
    }

    private static String manualIdForClass(String className) {
        if (className == null) {
            return "";
        }
        switch (className) {
        case "FF_PTS_O_001_ValidateColumnAvailableInTableView":
            return "FF_PTS_001";
        case "FF_PTS_O_002_ValidateColumnVisibleInGrid":
            return "FF_PTS_002";
        case "FF_PTS_O_009_ValidateColumnSearch":
            return "FF_PTS_009";
        case "FF_PTS_O_011_ValidateColumnSort":
            return "FF_PTS_011";
        case "FF_PTS_O_015_ValidateTableMatchesDetails":
            return "FF_PTS_015";
        case "FF_PTS_O_013_ValidateColumnInExcelExport":
            return "FF_PTS_013";
        case "FF_PTS_O_017_ValidateColumnHiddenWhenUnselectedAndExportExcludes":
            return "FF_PTS_017";
        case "FF_PTS_LI_001_ValidateColumnAvailableInTableView":
            return "FF_PTS_003";
        case "FF_PTS_LI_002_ValidateColumnVisibleInGrid":
            return "FF_PTS_004";
        case "FF_PTS_LI_009_ValidateColumnSearch":
            return "FF_PTS_010";
        case "FF_PTS_LI_011_ValidateColumnSort":
            return "FF_PTS_012";
        case "FF_PTS_LI_013_ValidateColumnInExcelExport":
            return "FF_PTS_014";
        case "FF_PTS_LI_018_ValidateColumnHiddenWhenUnselectedAndExportExcludes":
            return "FF_PTS_018";
        default:
            return className;
        }
    }

    private static String resolveStatus(int testNgStatus) {
        switch (testNgStatus) {
        case ITestResult.SUCCESS:
            return "PASS";
        case ITestResult.FAILURE:
            return "FAIL";
        case ITestResult.SKIP:
            return "SKIP";
        default:
            return "UNKNOWN";
        }
    }

    private static String statusBgColor(String status) {
        switch (status) {
        case "PASS":
            return "#e8ffe8";
        case "FAIL":
            return "#ffe8e8";
        case "SKIP":
            return "#fff8e0";
        default:
            return "#f2f2f2";
        }
    }

    private static String statusTextColor(String status) {
        switch (status) {
        case "PASS":
            return "green";
        case "FAIL":
            return "red";
        case "SKIP":
            return "#cc8800";
        default:
            return "black";
        }
    }
}
