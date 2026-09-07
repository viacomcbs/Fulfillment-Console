package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.util.AllureAttachment;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import org.testng.ISuite;
import org.testng.ITestResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds scenario-wise pass/fail HTML for TestNG suites (email body section).
 * Enabled globally by default via {@link com.paramount.test.ff.common.listeners.SuiteListeners};
 * opt out with {@code LeftFilterEmailReport=false} in suite XML.
 * Failed rows include a crisp Next Action in the same table (no separate failure-details table).
 */
public final class LeftFilterEmailReport {

    private static final Pattern LEFT_FILTER_CLASS = Pattern.compile("^(LF_[OLI]_TC\\d+)_(.+?)Test$");

    private static final Map<String, String> RESULTS = new LinkedHashMap<>();
    private static final Map<String, LeftFilterFailureAnalyzer.FailureDetail> FAILURE_DETAILS = new LinkedHashMap<>();
    private static boolean enabled;
    private static String suiteTitle = "Left Filter Validation";
    private static String synergySessionId = "";

    private LeftFilterEmailReport() {
    }

    public static void enable(String title) {
        enabled = true;
        RESULTS.clear();
        FAILURE_DETAILS.clear();
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
        FAILURE_DETAILS.clear();
        suiteTitle = "Fulfillment Console — Test Execution";
        synergySessionId = "";
    }

    /**
     * Global default: scenario email report ON for every suite unless explicitly disabled
     * or a dedicated feature email report suite (DSID, BSD-29967, PTS) is running.
     */
    public static void configureForSuite(ISuite suite) {
        if (suite == null) {
            return;
        }
        if (isExplicitlyDisabled(suite)) {
            disable();
            Logger.logReportMessage("Scenario email report disabled (LeftFilterEmailReport=false)");
            return;
        }
        if (hasDedicatedEmailReportSuite(suite)) {
            disable();
            return;
        }
        String title = suite.getParameter("LeftFilterEmailSuiteTitle");
        if (title == null || title.isBlank()) {
            title = suite.getName();
        }
        if (title == null || title.isBlank()) {
            title = "Fulfillment Console — Test Execution";
        }
        enable(title);
        Logger.logReportMessage("Scenario email report enabled for suite: " + title);
    }

    private static boolean isExplicitlyDisabled(ISuite suite) {
        return "false".equalsIgnoreCase(suite.getParameter("LeftFilterEmailReport"));
    }

    private static boolean hasDedicatedEmailReportSuite(ISuite suite) {
        String name = suite.getName() != null ? suite.getName() : "";
        String upper = name.toUpperCase();
        return upper.contains("DSID") || upper.contains("BSD-29870")
                || upper.contains("BSD-29967") || upper.contains("BSD29967")
                || upper.contains("BSD-29441") || upper.contains("PTS PACKAGING")
                || upper.contains("FF_PTS");
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
        return enabled;
    }

    public static void recordResult(ITestResult result) {
        if (!enabled || result == null) {
            return;
        }
        Class<?> testClass = result.getTestClass().getRealClass();
        String className = testClass.getSimpleName();
        LeftFilterEmailScenario scenarioAnn = testClass.getAnnotation(LeftFilterEmailScenario.class);
        String manualId;
        String scenario;
        if (scenarioAnn != null) {
            manualId = scenarioAnn.manualId();
            scenario = scenarioAnn.scenario();
        } else {
            manualId = deriveManualId(className);
            scenario = deriveScenarioFromClassName(className);
        }
        String status = resolveStatus(result.getStatus());
        RESULTS.put(className, manualId + "|" + scenario + "|" + status);
        if ("FAIL".equals(status)) {
            FAILURE_DETAILS.put(className, LeftFilterFailureAnalyzer.analyze(result, manualId, scenario));
        } else {
            FAILURE_DETAILS.remove(className);
        }
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
        if (RESULTS.isEmpty()) {
            html.append("<p><i>No left-filter scenario results were recorded for this run.</i></p>");
            return html.toString();
        }

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
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Next Action</th>");
        html.append("</tr>");

        int row = 1;
        for (Map.Entry<String, String> entry : RESULTS.entrySet()) {
            String[] parts = entry.getValue().split("\\|", 3);
            String manualId = parts.length > 0 ? parts[0] : "";
            String scenario = parts.length > 1 ? parts[1] : entry.getKey();
            String status = parts.length > 2 ? parts[2] : "UNKNOWN";
            String nextAction = resolveNextAction(entry.getKey(), status, manualId, scenario);
            html.append("<tr style=\"background-color:").append(statusBgColor(status)).append(";\">");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(row++).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(escapeHtml(manualId))
                    .append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(escapeHtml(scenario))
                    .append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;color:")
                    .append(statusTextColor(status)).append(";\"><b>").append(status).append("</b></td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">")
                    .append(escapeHtml(nextAction)).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    private static String resolveNextAction(String className, String status, String manualId, String scenario) {
        if (!"FAIL".equals(status)) {
            return "";
        }
        LeftFilterFailureAnalyzer.FailureDetail detail = FAILURE_DETAILS.get(className);
        if (detail == null) {
            detail = LeftFilterFailureAnalyzer.analyze(null, manualId, scenario);
        }
        return detail.nextAction();
    }

    /** e.g. {@code LF_O_TC120_ActivityType_BasicTest} → {@code LF_O_TC120}. */
    static String deriveManualId(String className) {
        Matcher matcher = LEFT_FILTER_CLASS.matcher(className);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return className;
    }

    /**
     * e.g. {@code LF_O_TC420_ActivityType_SelectAllTest} → {@code Activity Type — Select All}.
     */
    static String deriveScenarioFromClassName(String className) {
        Matcher matcher = LEFT_FILTER_CLASS.matcher(className);
        if (!matcher.matches()) {
            return humanizeToken(className);
        }
        String[] parts = matcher.group(2).split("_");
        StringBuilder scenario = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                scenario.append(" — ");
            }
            scenario.append(humanizeToken(parts[i]));
        }
        return scenario.toString();
    }

    private static String humanizeToken(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        return token
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2")
                .trim();
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
