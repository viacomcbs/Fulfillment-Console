package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.util.AllureAttachment;
import com.paramount.test.ff.common.util.Config;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds scenario-wise pass/fail HTML for left-filter TestNG suites (email body section).
 * Each test class registers its scenario via {@link LeftFilterEmailScenario} on the class.
 */
public final class LeftFilterEmailReport {

    private static final Map<String, String> RESULTS = new LinkedHashMap<>();
    private static boolean enabled;
    private static String suiteTitle = "Left Filter Validation";
    private static String synergySessionId = "";

    private LeftFilterEmailReport() {
    }

    public static void enable(String title) {
        enabled = true;
        RESULTS.clear();
        synergySessionId = "";
        if (title != null && !title.trim().isEmpty()) {
            suiteTitle = title.trim();
        } else {
            suiteTitle = "Left Filter Validation";
        }
    }

    public static void disable() {
        enabled = false;
        RESULTS.clear();
        suiteTitle = "Left Filter Validation";
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

    public static boolean isEnabled() {
        return enabled && !RESULTS.isEmpty();
    }

    public static void recordResult(ITestResult result) {
        if (!enabled || result == null) {
            return;
        }
        Class<?> testClass = result.getTestClass().getRealClass();
        LeftFilterEmailScenario scenarioAnn = testClass.getAnnotation(LeftFilterEmailScenario.class);
        if (scenarioAnn == null) {
            return;
        }
        String className = testClass.getSimpleName();
        String status = resolveStatus(result.getStatus());
        RESULTS.put(className, scenarioAnn.manualId() + "|" + scenarioAnn.scenario() + "|" + status);
    }

    public static int getPassedCount() {
        return countByStatus("PASS");
    }

    public static int getFailedCount() {
        return countByStatus("FAIL");
    }

    public static int getSkippedCount() {
        return countByStatus("SKIP");
    }

    public static int getTotalCount() {
        return RESULTS.size();
    }

    public static String buildHtmlSection() {
        captureSynergySessionId();
        String recordingUrl = AllureAttachment.buildRecordingUrl(synergySessionId);
        String env = Config.getString("TestEnvironment");

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(escapeHtml(suiteTitle)).append("</h2>");
        html.append("<p><b>Environment:</b> ").append(escapeHtml(env != null ? env : "")).append("</p>");
        html.append("<p><b>Synergy screen recording:</b> ");
        if (recordingUrl.isEmpty()) {
            html.append("<i>Not available — session may have ended before the link was captured.</i>");
        } else {
            html.append("<a href=\"").append(recordingUrl).append("\">Open session recording</a>");
        }
        html.append("</p>");
        html.append("<p><b>Automated scenarios:</b> ").append(getTotalCount())
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

        html.append("<table id=\"LeftFilterResults\" style=\"border-collapse:collapse;width:100%;"
                + "font-family:Calibri,Helvetica,sans-serif;font-size:medium;margin-top:8px;\">");
        html.append("<tr style=\"background-color:#04AA6D;color:white;\">");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">#</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Manual ID</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Scenario</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Status</th>");
        html.append("</tr>");

        int row = 1;
        for (Map.Entry<String, String> entry : RESULTS.entrySet()) {
            String[] parts = entry.getValue().split("\\|", 3);
            String manualId = parts.length > 0 ? parts[0] : "";
            String scenario = parts.length > 1 ? parts[1] : entry.getKey();
            String status = parts.length > 2 ? parts[2] : "UNKNOWN";
            html.append("<tr style=\"background-color:").append(statusBgColor(status)).append(";\">");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(row++).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(escapeHtml(manualId))
                    .append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(escapeHtml(scenario))
                    .append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;color:")
                    .append(statusTextColor(status)).append(";\"><b>").append(status).append("</b></td>");
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    private static int countByStatus(String status) {
        return (int) RESULTS.values().stream().filter(v -> v.endsWith("|" + status)).count();
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

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
