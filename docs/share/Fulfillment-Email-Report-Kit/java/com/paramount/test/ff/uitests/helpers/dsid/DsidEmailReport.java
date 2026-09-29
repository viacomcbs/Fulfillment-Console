package com.paramount.test.ff.uitests.helpers.dsid;

import com.paramount.test.ff.common.util.AllureAttachment;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Email summary for BSD-29870 DSID validation suite. */
public final class DsidEmailReport {

    public static final String JIRA_KEY = "BSD-29870";
    public static final String JIRA_URL = "https://paramount.atlassian.net/browse/BSD-29870";
    public static final String JIRA_SUMMARY =
            "DSID is blank in Order table for MetadataOnlyDelivery orders (Specific partner)";
    public static final String FEATURE_TITLE = "DSID — MetadataOnlyDelivery";

    private static final String[] CLASS_ORDER = {
            "FF_DSID_O_001_ValidateAtLeastOneOrderShowsDsid",
            "FF_DSID_O_002_ValidateTableDsidMatchesDetailsPanelLastDsid",
            "FF_DSID_LI_001_ValidateLineItemsDsidTableMatchesDetails"
    };

    private static final Map<String, String> SCENARIO_BY_CLASS = new LinkedHashMap<>();

    static {
        SCENARIO_BY_CLASS.put("FF_DSID_O_001_ValidateAtLeastOneOrderShowsDsid",
                "Orders: at least one row shows DSID after enabling column");
        SCENARIO_BY_CLASS.put("FF_DSID_O_002_ValidateTableDsidMatchesDetailsPanelLastDsid",
                "Orders: table DSID matches last DSID in Details panel");
        SCENARIO_BY_CLASS.put("FF_DSID_LI_001_ValidateLineItemsDsidTableMatchesDetails",
                "Line Items: table DSID matches last DSID in Details panel");
    }

    private static final Map<String, String> RESULTS = new LinkedHashMap<>();
    private static boolean enabled;
    private static String synergySessionId = "";

    private DsidEmailReport() {
    }

    public static void enable() {
        enabled = true;
        RESULTS.clear();
        synergySessionId = "";
    }

    public static void disable() {
        enabled = false;
        RESULTS.clear();
        synergySessionId = "";
    }

    public static void setSynergySessionId(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            synergySessionId = sessionId.trim();
        }
    }

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

    public static boolean isDsidTestClass(String classSimpleName) {
        return classSimpleName != null && classSimpleName.startsWith("FF_DSID_");
    }

    public static void recordResult(ITestResult result) {
        if (!enabled || result == null) {
            return;
        }
        String className = result.getTestClass().getRealClass().getSimpleName();
        if (!isDsidTestClass(className)) {
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

    public static String buildHtmlSection() {
        captureSynergySessionId();
        String recordingUrl = getSynergyRecordingUrl();
        String tableTitle = FEATURE_TITLE + " — " + JIRA_KEY + ": " + resolveJiraSummary();
        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(FEATURE_TITLE).append("</h2>");
        html.append("<p><b>Jira Bug:</b> <a href=\"").append(JIRA_URL).append("\">").append(JIRA_KEY)
                .append("</a> — ").append(resolveJiraSummary()).append("</p>");
        html.append("<p><b>Synergy screen recording:</b> ");
        if (recordingUrl.isEmpty()) {
            html.append("<i>Not available</i>");
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
        html.append("</p>");
        html.append("<table style=\"border-collapse:collapse;width:100%;\">");
        html.append("<caption style=\"caption-side:top;text-align:left;font-weight:bold;padding:8px 0;\">")
                .append(tableTitle).append("</caption>");
        html.append("<tr style=\"background-color:#04AA6D;color:white;\">");
        html.append("<th style=\"border:2px solid black;padding:8px;\">#</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;\">Scenario</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;\">Manual ID</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;\">Status</th>");
        html.append("</tr>");

        int row = 1;
        for (String className : orderedClassNames()) {
            String[] parts = RESULTS.get(className).split("\\|", 2);
            String scenario = parts[0];
            String status = parts.length > 1 ? parts[1] : "UNKNOWN";
            String rowColor = "PASS".equals(status) ? "#e8ffe8" : "FAIL".equals(status) ? "#ffe8e8" : "#fff8e0";
            html.append("<tr style=\"background-color:").append(rowColor).append(";\">");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(row++).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(scenario).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">")
                    .append(manualIdForClass(className)).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\"><b>").append(status).append("</b></td>");
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    private static List<String> orderedClassNames() {
        List<String> ordered = new ArrayList<>();
        for (String className : CLASS_ORDER) {
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
        switch (className) {
        case "FF_DSID_O_001_ValidateAtLeastOneOrderShowsDsid":
            return "FF_DSID_001";
        case "FF_DSID_O_002_ValidateTableDsidMatchesDetailsPanelLastDsid":
            return "FF_DSID_002";
        case "FF_DSID_LI_001_ValidateLineItemsDsidTableMatchesDetails":
            return "FF_DSID_003";
        default:
            return className;
        }
    }

    private static String resolveJiraSummary() {
        String fromConfig = com.paramount.test.ff.common.util.Config.getString("JiraStoryTitle");
        return fromConfig != null && !fromConfig.trim().isEmpty() ? fromConfig.trim() : JIRA_SUMMARY;
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
}
