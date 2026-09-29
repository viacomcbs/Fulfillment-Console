package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.util.AllureAttachment;
import com.paramount.test.ff.common.util.Config;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Email summary for BSD-29967 only — per-workflow DSID validation table.
 * Other suites use their own report classes ({@code LeftFilterEmailReport}, etc.).
 */
public final class Bsd29967EmailReport {

    public static final String JIRA_KEY = "BSD-29967";
    public static final String JIRA_URL = "https://paramount.atlassian.net/browse/BSD-29967";
    public static final String JIRA_SUMMARY =
            "Multi-submission orders — FC Details DSID vs ops-console Jobs dsIdList";
    public static final String FEATURE_TITLE = "BSD-29967 — Multi-submission DSID";
    public static final String REPORT_BUILD_VERSION = "2026-09-04-v21";

    private static final List<Bsd29967EnvironmentResult> ENVIRONMENT_RESULTS = new ArrayList<>();
    private static String jiraEvidenceStatus = "";
    private static int expectedEnvironmentCount = 0;
    private static String workflowSelectionSummary = "";

    private static final Map<String, String> SCENARIO_BY_CLASS = new LinkedHashMap<>();

    static {
        SCENARIO_BY_CLASS.put("FF_BSD29967_O_001_ValidateMultiSubmissionOrderDetailsDsid",
                "Orders: Yesterday + round-robin 3 non-zero Environment workflows + mandatory UWFFSP + (i) row + Details DSIDs (max 2 orders per workflow when DSIDs blank)");
        SCENARIO_BY_CLASS.put("FF_BSD29967_O_002_ValidateDsidMatchesOpsConsoleDsIdList",
                "Orders: per-Environment FC Details DSIDs vs ops-console dsIdList summary");
    }

    private static final Map<String, String> RESULTS = new LinkedHashMap<>();
    private static boolean enabled;
    private static String synergySessionId = "";

    private Bsd29967EmailReport() {
    }

    public static void enable() {
        enabled = true;
        RESULTS.clear();
        synergySessionId = "";
        clearEnvironmentSnapshots();
    }

    public static void disable() {
        enabled = false;
        RESULTS.clear();
        synergySessionId = "";
        clearEnvironmentSnapshots();
    }

    private static void clearEnvironmentSnapshots() {
        ENVIRONMENT_RESULTS.clear();
        jiraEvidenceStatus = "";
        expectedEnvironmentCount = 0;
        workflowSelectionSummary = "";
    }

    public static void setWorkflowSelectionSummary(String summary) {
        workflowSelectionSummary = summary == null ? "" : summary.trim();
    }

    public static String getWorkflowSelectionSummary() {
        return workflowSelectionSummary;
    }

    public static void setExpectedEnvironmentCount(int count) {
        expectedEnvironmentCount = Math.max(0, count);
    }

    public static int getExpectedEnvironmentCount() {
        return expectedEnvironmentCount;
    }

    public static boolean isFullWorkflowValidationPass() {
        if (getFailedEnvironmentCount() > 0 || getPassedEnvironmentCount() == 0) {
            return false;
        }
        if (getSkippedEnvironmentCount() > 0) {
            return false;
        }
        if (expectedEnvironmentCount > 0 && ENVIRONMENT_RESULTS.size() < expectedEnvironmentCount) {
            return false;
        }
        return true;
    }

    public static boolean isAttachJiraOnPassEnabled() {
        String raw = Config.getString("AttachJiraEvidenceOnPass");
        return raw == null || raw.trim().isEmpty() || "true".equalsIgnoreCase(raw.trim());
    }

    public static void setJiraEvidenceStatus(String status) {
        jiraEvidenceStatus = status == null ? "" : status.trim();
    }

    public static String getJiraEvidenceStatus() {
        return jiraEvidenceStatus;
    }

    /** Persists all per-workflow rows for email (including workflows without DSIDs). */
    public static void snapshotEnvironmentResult(Bsd29967EnvironmentResult result) {
        if (result == null) {
            return;
        }
        ENVIRONMENT_RESULTS.add(result);
    }

    /** PASS rows with populated FC DSIDs — used for Jira accumulation only. */
    public static List<Bsd29967EnvironmentResult> getCurrentRunPassRowsWithDsids() {
        List<Bsd29967EnvironmentResult> passWithDsids = new ArrayList<>();
        for (Bsd29967EnvironmentResult result : ENVIRONMENT_RESULTS) {
            if (Bsd29967EnvironmentResult.STATUS_PASS.equals(result.getStatus())
                    && result.hasPopulatedFcDsids()) {
                passWithDsids.add(result);
            }
        }
        return passWithDsids;
    }

    public static int getPassedEnvironmentCount() {
        return countEnvironmentsByStatus(Bsd29967EnvironmentResult.STATUS_PASS);
    }

    public static int getFailedEnvironmentCount() {
        return countEnvironmentsByStatus(Bsd29967EnvironmentResult.STATUS_FAIL);
    }

    public static int getSkippedEnvironmentCount() {
        return countEnvironmentsByStatus(Bsd29967EnvironmentResult.STATUS_SKIP);
    }

    private static int countEnvironmentsByStatus(String status) {
        int count = 0;
        for (Bsd29967EnvironmentResult result : ENVIRONMENT_RESULTS) {
            if (status.equals(result.getStatus())) {
                count++;
            }
        }
        return count;
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
        return enabled;
    }

    public static boolean isBsd29967TestClass(String classSimpleName) {
        return classSimpleName != null && classSimpleName.startsWith("FF_BSD29967_");
    }

    public static void recordResult(ITestResult result) {
        if (!enabled || result == null) {
            return;
        }
        String className = result.getTestClass().getRealClass().getSimpleName();
        if (!isBsd29967TestClass(className)) {
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

    public static List<Bsd29967EnvironmentResult> getCurrentRunEnvironmentResults() {
        return new ArrayList<>(ENVIRONMENT_RESULTS);
    }

    public static String buildHtmlSection() {
        StringBuilder html = new StringBuilder();
        html.append("<p><b>Test Environment :</b> ").append(resolveTestEnvironmentLabel()).append("</p>");
        int noDsidCount = countWorkflowsWithoutFcDsids(ENVIRONMENT_RESULTS);
        if (noDsidCount > 0) {
            html.append("<p><i>").append(noDsidCount)
                    .append(" workflow(s) had no FC Details DSID — listed below; excluded from Jira evidence.</i></p>");
        }
        html.append(buildEmailWorkflowResultsTableHtml(ENVIRONMENT_RESULTS));
        return html.toString();
    }

    /** Email table — all workflows for this run, including those without DSIDs. */
    public static String buildEmailWorkflowResultsTableHtml(List<Bsd29967EnvironmentResult> results) {
        return renderWorkflowResultsTable(results, false);
    }

    /** Per-workflow DSID table for Jira — PASS workflows with populated DSIDs only. */
    public static String buildWorkflowResultsTableHtml() {
        return buildWorkflowResultsTableHtml(getCurrentRunPassRowsWithDsids());
    }

    /** Combined PASS workflows across runs (Jira evidence). */
    public static String buildAccumulatedWorkflowResultsTableHtml() {
        List<Bsd29967EnvironmentResult> accumulated =
                Bsd29967AccumulatedResultsStore.mergeCurrentRunPassRows(getCurrentRunPassRowsWithDsids());
        if (accumulated.isEmpty()) {
            return "<p><i>No per-workflow validation rows recorded.</i></p>";
        }
        return buildWorkflowResultsTableHtml(accumulated);
    }

    public static int getAccumulatedPassWorkflowCount() {
        return Bsd29967AccumulatedResultsStore.loadAllPassRows().size();
    }

    public static String buildWorkflowResultsTableHtml(List<Bsd29967EnvironmentResult> results) {
        return renderWorkflowResultsTable(results, true);
    }

    private static String renderWorkflowResultsTable(List<Bsd29967EnvironmentResult> results,
            boolean dsidPopulatedOnly) {
        List<Bsd29967EnvironmentResult> rows = dsidPopulatedOnly
                ? filterReportableWorkflows(results) : filterEmailWorkflows(results);
        if (rows.isEmpty()) {
            return "<p><i>No per-workflow validation rows recorded.</i></p>";
        }
        StringBuilder table = new StringBuilder();
        table.append("<table style=\"border-collapse:collapse;width:100%;\">");
        table.append("<tr style=\"background-color:#04AA6D;color:white;\">");
        table.append("<th style=\"border:2px solid black;padding:8px;\">Workflow</th>");
        table.append("<th style=\"border:2px solid black;padding:8px;\">Details panel — DSID</th>");
        table.append("<th style=\"border:2px solid black;padding:8px;\">Order Inspector latest job DSID</th>");
        table.append("<th style=\"border:2px solid black;padding:8px;\">Data validation</th>");
        table.append("<th style=\"border:2px solid black;padding:8px;\">Next Action</th>");
        table.append("</tr>");
        for (Bsd29967EnvironmentResult result : rows) {
            String validation = resolveValidationLabel(result);
            String rowColor = Bsd29967EnvironmentResult.STATUS_PASS.equals(result.getStatus()) ? "#e8ffe8"
                    : Bsd29967EnvironmentResult.STATUS_FAIL.equals(result.getStatus()) ? "#ffe8e8" : "#fff8e0";
            table.append("<tr style=\"background-color:").append(rowColor).append(";\">");
            table.append("<td style=\"border:2px solid black;padding:8px;\"><b>")
                    .append(escapeHtml(result.getEnvironment())).append("</b>");
            if (!result.getOrderId().isEmpty()) {
                table.append("<br/><small>Order ").append(escapeHtml(result.getOrderId())).append("</small>");
            }
            table.append("</td>");
            table.append("<td style=\"border:2px solid black;padding:8px;\">")
                    .append(escapeHtml(formatFcDsidsForDisplay(result))).append("</td>");
            table.append("<td style=\"border:2px solid black;padding:8px;\">")
                    .append(escapeHtml(formatDsids(result.getOpsDsids())));
            if (!result.getOpsExtractionSource().isEmpty()) {
                table.append("<br/><small>Source: ")
                        .append(escapeHtml(result.getOpsExtractionSource())).append("</small>");
            }
            table.append("</td>");
            table.append("<td style=\"border:2px solid black;padding:8px;\"><b>")
                    .append(validation).append("</b></td>");
            table.append("<td style=\"border:2px solid black;padding:8px;\">")
                    .append(escapeHtml(resolveWorkflowNextAction(result))).append("</td>");
            table.append("</tr>");
        }
        table.append("</table>");
        return table.toString();
    }

    private static String formatDsids(List<String> dsids) {
        if (dsids == null || dsids.isEmpty()) {
            return "—";
        }
        return String.join(", ", dsids);
    }

    private static List<Bsd29967EnvironmentResult> filterReportableWorkflows(
            List<Bsd29967EnvironmentResult> results) {
        if (results == null || results.isEmpty()) {
            return new ArrayList<>();
        }
        List<Bsd29967EnvironmentResult> reportable = new ArrayList<>();
        for (Bsd29967EnvironmentResult result : results) {
            if (result != null && result.hasPopulatedFcDsids()) {
                reportable.add(result);
            }
        }
        return reportable;
    }

    private static List<Bsd29967EnvironmentResult> filterEmailWorkflows(
            List<Bsd29967EnvironmentResult> results) {
        if (results == null || results.isEmpty()) {
            return new ArrayList<>();
        }
        List<Bsd29967EnvironmentResult> rows = new ArrayList<>();
        for (Bsd29967EnvironmentResult result : results) {
            if (result != null) {
                rows.add(result);
            }
        }
        return rows;
    }

    private static int countWorkflowsWithoutFcDsids(List<Bsd29967EnvironmentResult> results) {
        int count = 0;
        for (Bsd29967EnvironmentResult result : results) {
            if (result != null && !result.hasPopulatedFcDsids()) {
                count++;
            }
        }
        return count;
    }

    private static String resolveValidationLabel(Bsd29967EnvironmentResult result) {
        if (!result.hasPopulatedFcDsids()) {
            return "SKIP";
        }
        String validation = result.getStatus();
        if (Bsd29967EnvironmentResult.STATUS_PASS.equals(validation)) {
            return "PASS";
        }
        if (Bsd29967EnvironmentResult.STATUS_FAIL.equals(validation)) {
            return "FAIL";
        }
        if (Bsd29967EnvironmentResult.STATUS_SKIP.equals(validation)) {
            return "SKIP";
        }
        return validation;
    }

    private static String formatFcDsidsForDisplay(Bsd29967EnvironmentResult result) {
        if (!result.hasPopulatedFcDsids()) {
            return "No DSID";
        }
        return formatDsids(result.getFcDsids());
    }

    private static String resolveWorkflowNextAction(Bsd29967EnvironmentResult result) {
        if (!result.hasPopulatedFcDsids()) {
            return "Code Issue";
        }
        if (Bsd29967EnvironmentResult.STATUS_PASS.equals(result.getStatus())) {
            return "—";
        }
        if (Bsd29967EnvironmentResult.STATUS_FAIL.equals(result.getStatus())) {
            return "Raise defect";
        }
        String message = result.getMessage();
        if (message != null && message.toLowerCase().contains("synergy session unavailable")) {
            return "Synergy Issue";
        }
        return "Code Issue";
    }

    private static String resolveTestEnvironmentLabel() {
        String fromConfig = Config.getString("TestEnvironment");
        if (fromConfig == null || fromConfig.trim().isEmpty()) {
            return "DEV";
        }
        return fromConfig.trim().toUpperCase();
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

    private static String emptyToDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value.trim();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
