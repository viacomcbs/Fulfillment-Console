package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.reporting.ExecutionReportReasonFormatter;
import com.paramount.test.ff.common.util.reporting.ExecutionReportReasonFormatter.ExecutionOutcome;
import com.paramount.test.ff.common.util.reporting.FailureCategoryAnalyzer;
import com.paramount.test.ff.common.util.reporting.TestResultRecord;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds scenario-wise pass/fail HTML for TestNG suites (email body section).
 * Enabled globally by default via {@link com.paramount.test.ff.common.listeners.SuiteListeners};
 * opt out with {@code LeftFilterEmailReport=false} in suite XML.
 */
public final class LeftFilterEmailReport {

    private static final Pattern LEFT_FILTER_CLASS = Pattern.compile("^(LF_[OLI]_TC\\d+)_(.+?)Test$");
    private static final Pattern TABLE_VIEW_CLASS = Pattern.compile("^(TC_\\d+)_(.+)$");
    private static final Pattern BSD_TABLE_VIEW_CLASS = Pattern.compile("^(TC_BSD_\\d+_\\d+)_(.+)$");
    private static final String EMPTY_CELL = "—";

    private static final Map<String, String> RESULTS = new LinkedHashMap<>();
    private static final Map<String, LeftFilterFailureAnalyzer.FailureDetail> FAILURE_DETAILS = new LinkedHashMap<>();
    private static final Map<String, Long> DURATIONS = new LinkedHashMap<>();
    /** Scenario labels for harness classes when merging saved TestNG XML (no @LeftFilterEmailScenario at runtime). */
    private static final Map<String, String> MERGED_SCENARIO_OVERRIDES = Map.of(
            "LF_O_TC999_Job_ReportDemoFailTest", "Job - Active Filters (report demo fail)");
    private static final DateTimeFormatter TESTNG_TIMESTAMP = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss z", Locale.ENGLISH);
    private static boolean enabled;
    private static String suiteTitle = "Left Filter Validation";
    private static long suiteStartMs;
    /** Earliest test start / latest test finish when merging saved session XML files. */
    private static long mergedEarliestStartMs;
    private static long mergedLatestFinishMs;

    private LeftFilterEmailReport() {
    }

    public static void enable(String title) {
        enabled = true;
        RESULTS.clear();
        FAILURE_DETAILS.clear();
        DURATIONS.clear();
        mergedEarliestStartMs = 0L;
        mergedLatestFinishMs = 0L;
        suiteStartMs = System.currentTimeMillis();
        if (title != null && !title.trim().isEmpty()) {
            suiteTitle = title.trim();
        } else {
            suiteTitle = "Left Filter Validation";
        }
    }

    public static long getSuiteDurationMs() {
        if (mergedEarliestStartMs > 0 && mergedLatestFinishMs > mergedEarliestStartMs) {
            return mergedLatestFinishMs - mergedEarliestStartMs;
        }
        long summed = sumRecordedDurations();
        if (summed > 0) {
            return summed;
        }
        if (suiteStartMs <= 0) {
            return 0L;
        }
        return System.currentTimeMillis() - suiteStartMs;
    }

    public static void disable() {
        enabled = false;
        RESULTS.clear();
        FAILURE_DETAILS.clear();
        DURATIONS.clear();
        mergedEarliestStartMs = 0L;
        mergedLatestFinishMs = 0L;
        suiteTitle = "Fulfillment Console — Test Execution";
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

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Merge results from a prior suite run ({@code target/surefire-reports/testng-results.xml})
     * so multiple filter suites can produce one consolidated email.
     */
    public static void mergeFromTestNgResultsFile(File file) {
        if (file == null || !file.isFile()) {
            return;
        }
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            NodeList methods = doc.getElementsByTagName("test-method");
            for (int i = 0; i < methods.getLength(); i++) {
                Node node = methods.item(i);
                if (!(node instanceof Element)) {
                    continue;
                }
                Element method = (Element) node;
                if ("true".equalsIgnoreCase(method.getAttribute("is-config"))) {
                    continue;
                }
                String className = resolveClassName(method);
                if (className == null || shouldSkipReportClass(className)) {
                    continue;
                }
                String simpleName = className.contains(".") ? className.substring(className.lastIndexOf('.') + 1)
                        : className;
                String status = mapTestNgStatus(method.getAttribute("status"));
                String exceptionMessage = extractExceptionMessage(method);
                long durationMs = parseDurationMs(method.getAttribute("duration-ms"));
                trackMergedTimestamps(method.getAttribute("started-at"), method.getAttribute("finished-at"));
                recordMergedResult(simpleName, status, exceptionMessage, durationMs);
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not merge TestNG results from " + file.getName() + ": " + e.getMessage());
        }
    }

    private static void recordMergedResult(String className, String status, String exceptionMessage, long durationMs) {
        if (!enabled) {
            enable("Left Filter Validation");
        }
        String existingValue = RESULTS.get(className);
        if (existingValue != null && consolidatedDedupeEnabled()) {
            String existingStatus = parseStatusFromResultValue(existingValue);
            if (statusRank(existingStatus) >= statusRank(status)) {
                Logger.logConsoleMessage("Consolidated merge: keeping " + className + " as " + existingStatus
                        + " (skipped duplicate " + status + " run)");
                return;
            }
            Logger.logConsoleMessage("Consolidated merge: " + className + " upgraded from " + existingStatus
                    + " to " + status + " (re-run result)");
        }
        String manualId = deriveManualId(className);
        String scenario = MERGED_SCENARIO_OVERRIDES.getOrDefault(className, deriveScenarioFromClassName(className));
        RESULTS.put(className, manualId + "|" + scenario + "|" + status);
        if (durationMs > 0) {
            DURATIONS.put(className, durationMs);
        }
        if ("FAIL".equals(status) || "ERROR".equals(status)) {
            if ("LF_O_TC999_Job_ReportDemoFailTest".equals(className)) {
                FAILURE_DETAILS.put(className,
                        LeftFilterFailureAnalyzer.detailForReportPreviewDemoFail(manualId, scenario));
            } else {
                FAILURE_DETAILS.put(className,
                        LeftFilterFailureAnalyzer.analyzeFromMessage(exceptionMessage, manualId, scenario));
            }
        } else if ("SKIP".equals(status)) {
            FAILURE_DETAILS.put(className,
                    LeftFilterFailureAnalyzer.detailForSkip(manualId, scenario, exceptionMessage));
        } else {
            FAILURE_DETAILS.remove(className);
        }
    }

    private static String extractExceptionMessage(Element method) {
        NodeList exceptions = method.getElementsByTagName("exception");
        if (exceptions.getLength() == 0) {
            return "";
        }
        Node node = exceptions.item(0);
        if (!(node instanceof Element)) {
            return "";
        }
        Element exception = (Element) node;
        StringBuilder combined = new StringBuilder();
        appendIfPresent(combined, exception.getAttribute("message"));
        NodeList messageNodes = exception.getElementsByTagName("message");
        if (messageNodes.getLength() > 0) {
            appendIfPresent(combined, messageNodes.item(0).getTextContent());
        }
        if (combined.length() == 0) {
            NodeList stackNodes = exception.getElementsByTagName("full-stacktrace");
            if (stackNodes.getLength() > 0) {
                appendIfPresent(combined, stackNodes.item(0).getTextContent());
            }
        }
        if (combined.length() == 0) {
            NodeList children = exception.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE) {
                    appendIfPresent(combined, child.getTextContent());
                }
            }
        }
        return LeftFilterFailureAnalyzer.primaryFailureLine(combined.toString());
    }

    private static void appendIfPresent(StringBuilder target, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (target.length() > 0) {
            target.append(' ');
        }
        target.append(value.trim());
    }

    private static String resolveClassName(Element method) {
        Node parent = method.getParentNode();
        if (parent instanceof Element && "class".equals(parent.getNodeName())) {
            return ((Element) parent).getAttribute("name");
        }
        return method.getAttribute("class");
    }

    private static String mapTestNgStatus(String status) {
        if (status == null) {
            return "UNKNOWN";
        }
        switch (status.toUpperCase()) {
        case "PASS":
            return "PASS";
        case "FAIL":
            return "FAIL";
        case "SKIP":
            return "SKIP";
        default:
            return "UNKNOWN";
        }
    }

    public static void recordResult(ITestResult result) {
        if (!enabled || result == null) {
            return;
        }
        Class<?> testClass = result.getTestClass().getRealClass();
        String className = testClass.getSimpleName();
        if (shouldSkipReportClass(className) || shouldSkipReportClass(testClass.getName())) {
            return;
        }
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
        long durationMs = Math.max(0L, result.getEndMillis() - result.getStartMillis());
        if (durationMs > 0) {
            DURATIONS.put(className, durationMs);
        }
        if ("FAIL".equals(status) || "ERROR".equals(status)) {
            FAILURE_DETAILS.put(className, LeftFilterFailureAnalyzer.analyze(result, manualId, scenario));
        } else if ("SKIP".equals(status)) {
            String skipMessage = result.getThrowable() != null ? result.getThrowable().getMessage() : "";
            FAILURE_DETAILS.put(className, LeftFilterFailureAnalyzer.detailForSkip(manualId, scenario, skipMessage));
        } else {
            FAILURE_DETAILS.remove(className);
        }
    }

    public static int getPassedCount() {
        return countByStatus("PASS");
    }

    public static int getFailedCount() {
        return countByStatus("FAIL") + countByStatus("ERROR") + countByStatus("Failed") + countByStatus("Error");
    }

    public static int getSkippedCount() {
        return countByStatus("SKIP");
    }

    public static int getTotalCount() {
        return (int) RESULTS.keySet().stream().filter(k -> !shouldSkipReportClass(k)).count();
    }

    public static String buildHtmlSection() {
        return buildHtmlSection(false);
    }

    /**
     * @param passedOnly when {@code true}, include only PASS rows and omit fail/skip counts (stakeholder view).
     */
    public static String buildHtmlSection(boolean passedOnly) {
        String env = Config.getString("TestEnvironment");

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(escapeHtml(suiteTitle)).append("</h2>");
        html.append("<p><b>Environment:</b> ").append(escapeHtml(env != null ? env : "")).append("</p>");
        if (RESULTS.isEmpty()) {
            html.append("<p><i>No automated test case results were recorded for this run.</i></p>");
            return html.toString();
        }

        int displayPassed = getPassedCount();
        int displayFailed = passedOnly ? 0 : getFailedCount();

        html.append("<p><b>Automated test cases:</b> ")
                .append("<span style=\"color:green\"><b>Passed : ").append(displayPassed).append("</b></span>")
                .append(" , ")
                .append("<span style=\"color:red\"><b>Failed : ").append(displayFailed).append("</b></span>")
                .append("</p>");

        html.append("<table id=\"LeftFilterResults\" style=\"border-collapse:collapse;width:100%;"
                + "font-family:Calibri,Helvetica,sans-serif;font-size:medium;margin-top:8px;\">");
        html.append("<tr style=\"background-color:#04AA6D;color:white;\">");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">#</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Test case ID</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Test Scenario</th>");
        html.append("<th style=\"border:2px solid black;padding:8px;text-align:left;\">Status</th>");
        html.append("</tr>");

        int row = 1;
        for (Map.Entry<String, String> entry : RESULTS.entrySet()) {
            if (shouldSkipReportClass(entry.getKey())) {
                continue;
            }
            String[] parts = entry.getValue().split("\\|", 3);
            String testCaseId = parts.length > 0 ? parts[0] : "";
            String scenario = parts.length > 1 ? parts[1] : entry.getKey();
            String status = parts.length > 2 ? parts[2] : "UNKNOWN";
            if (passedOnly && !"PASS".equals(status)) {
                continue;
            }
            html.append("<tr style=\"background-color:").append(statusBgColor(status)).append(";\">");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(row++).append("</td>");
            html.append("<td style=\"border:2px solid black;padding:8px;\">").append(escapeHtml(testCaseId))
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

    /**
     * Converts recorded left-filter results into {@link TestResultRecord} rows for PDF generation.
     */
    public static List<TestResultRecord> toResultRecords(boolean passedOnly) {
        List<TestResultRecord> records = new ArrayList<>();
        int index = 1;
        for (Map.Entry<String, String> entry : RESULTS.entrySet()) {
            if (shouldSkipReportClass(entry.getKey())) {
                continue;
            }
            String[] parts = entry.getValue().split("\\|", 3);
            String testCaseId = parts.length > 0 ? parts[0] : "";
            String scenario = parts.length > 1 ? parts[1] : entry.getKey();
            String status = parts.length > 2 ? parts[2] : "UNKNOWN";
            if (passedOnly && !"PASS".equals(status)) {
                continue;
            }
            LeftFilterFailureAnalyzer.FailureDetail detail = resolveFailureDetail(entry.getKey(), testCaseId,
                    scenario, status);
            ExecutionOutcome outcome = ExecutionReportReasonFormatter.outcomeFromStatus(detail.status());
            String reportStatus = ExecutionReportReasonFormatter.statusLabel(outcome);
            String expected = ExecutionReportReasonFormatter.expectedForReport(outcome, detail.expected());
            String actual = ExecutionReportReasonFormatter.actualForReport(outcome, detail.actual(), detail.summary());
            String reason = ExecutionReportReasonFormatter.fullFailureReason(outcome, detail.expected(),
                    detail.actual(), detail.stepFailureReason(), detail.summary());
            records.add(new TestResultRecord(index++, testCaseId, scenario, reportStatus, expected, actual, reason,
                    DURATIONS.getOrDefault(entry.getKey(), 0L)));
        }
        return records;
    }

    private static LeftFilterFailureAnalyzer.FailureDetail resolveFailureDetail(String className, String testCaseId,
            String scenario, String status) {
        ExecutionOutcome outcome = ExecutionReportReasonFormatter.outcomeFromStatus(status);
        if (outcome == ExecutionOutcome.FAILED || outcome == ExecutionOutcome.ERROR) {
            LeftFilterFailureAnalyzer.FailureDetail detail = FAILURE_DETAILS.get(className);
            if (detail != null) {
                return detail;
            }
            return LeftFilterFailureAnalyzer.analyze(null, testCaseId, scenario);
        }
        if (outcome == ExecutionOutcome.PASSED) {
            return LeftFilterFailureAnalyzer.detailForPass(testCaseId, scenario);
        }
        if (outcome == ExecutionOutcome.SKIPPED) {
            LeftFilterFailureAnalyzer.FailureDetail detail = FAILURE_DETAILS.get(className);
            if (detail != null) {
                return detail;
            }
            return LeftFilterFailureAnalyzer.detailForSkip(testCaseId, scenario, null);
        }
        return new LeftFilterFailureAnalyzer.FailureDetail(
                LeftFilterFailureAnalyzer.formatTestCaseName(testCaseId, scenario),
                testCaseId, scenario, status,
                EMPTY_CELL,
                EMPTY_CELL,
                EMPTY_CELL,
                EMPTY_CELL,
                LeftFilterFailureAnalyzer.NextAction.RAISE_DEFECT);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank() && !EMPTY_CELL.equals(value.trim())) {
                return value.trim();
            }
        }
        return "";
    }

    /** Exclude email/Slack harness tests from scenario tables (not real LF/TV scenarios). */
    public static boolean shouldSkipReportClass(String className) {
        if (className == null || className.isBlank()) {
            return true;
        }
        String simple = className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : className;
        return simple.endsWith("CombinedEmailReportTest")
                || simple.endsWith("EmailReportTest")
                || "LeftFilterCombinedEmailReportTest".equals(simple)
                || "FulfillmentCombinedEmailReportTest".equals(simple);
    }

    /** After merge, drop any harness rows that may have been recorded by suite listeners. */
    public static void removeHarnessResults() {
        RESULTS.keySet().removeIf(LeftFilterEmailReport::shouldSkipReportClass);
        FAILURE_DETAILS.keySet().removeIf(LeftFilterEmailReport::shouldSkipReportClass);
        DURATIONS.keySet().removeIf(LeftFilterEmailReport::shouldSkipReportClass);
    }

    /**
     * End-of-day / consolidated email prep: dedupe by test-case id (prefer PASS on re-runs),
     * then drop Synergy infrastructure failures (UnknownHost, session lost, etc.).
     */
    public static void finalizeConsolidatedResults() {
        int before = getTotalCount();
        if (consolidatedDedupeEnabled()) {
            dedupeByManualIdPreferPass();
        }
        if (consolidatedExcludeSynergyEnabled()) {
            removeSynergyInfrastructureFailures();
        }
        Logger.logConsoleMessage("Consolidated report finalized: " + before + " raw -> " + getTotalCount()
                + " scenarios (" + getPassedCount() + " passed, " + getFailedCount() + " failed, "
                + getSkippedCount() + " skipped)");
    }

    private static boolean consolidatedDedupeEnabled() {
        return Config.getBoolean("ConsolidatedEmailDedupePreferPass");
    }

    private static boolean consolidatedExcludeSynergyEnabled() {
        return Config.getBoolean("ConsolidatedEmailExcludeSynergyIssues");
    }

    private static void dedupeByManualIdPreferPass() {
        Map<String, String> winnerClassByManualId = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : new LinkedHashMap<>(RESULTS).entrySet()) {
            String className = entry.getKey();
            if (shouldSkipReportClass(className)) {
                continue;
            }
            String manualId = parseManualIdFromResultValue(entry.getValue());
            String incumbentClass = winnerClassByManualId.get(manualId);
            if (incumbentClass == null) {
                winnerClassByManualId.put(manualId, className);
                continue;
            }
            String incumbentStatus = parseStatusFromResultValue(RESULTS.get(incumbentClass));
            String candidateStatus = parseStatusFromResultValue(entry.getValue());
            if (statusRank(candidateStatus) > statusRank(incumbentStatus)) {
                removeResultEntry(incumbentClass);
                winnerClassByManualId.put(manualId, className);
            } else if (!incumbentClass.equals(className)) {
                removeResultEntry(className);
            }
        }
    }

    private static void removeSynergyInfrastructureFailures() {
        List<String> excluded = new ArrayList<>();
        for (String className : new ArrayList<>(RESULTS.keySet())) {
            if (shouldSkipReportClass(className)) {
                continue;
            }
            if ("PASS".equals(parseStatusFromResultValue(RESULTS.get(className)))) {
                continue;
            }
            if (isSynergyInfrastructureFailure(className)) {
                excluded.add(className);
            }
        }
        for (String className : excluded) {
            Logger.logConsoleMessage("Consolidated report: excluding Synergy issue — " + className);
            removeResultEntry(className);
        }
    }

    private static boolean isSynergyInfrastructureFailure(String className) {
        LeftFilterFailureAnalyzer.FailureDetail detail = FAILURE_DETAILS.get(className);
        if (detail != null && "Synergy Issue".equals(detail.nextAction())) {
            return true;
        }
        String combined = "";
        if (detail != null) {
            combined = firstNonBlank(detail.summary(), detail.stepFailureReason(), detail.actual());
        }
        return FailureCategoryAnalyzer.SYNERGY.equals(FailureCategoryAnalyzer.categorize(combined, combined));
    }

    private static void removeResultEntry(String className) {
        RESULTS.remove(className);
        FAILURE_DETAILS.remove(className);
        DURATIONS.remove(className);
    }

    private static String parseManualIdFromResultValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String[] parts = value.split("\\|", 3);
        return parts.length > 0 ? parts[0].trim() : value.trim();
    }

    private static String parseStatusFromResultValue(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        String[] parts = value.split("\\|", 3);
        return parts.length > 2 ? parts[2].trim() : "UNKNOWN";
    }

    private static int statusRank(String status) {
        if (status == null) {
            return 0;
        }
        switch (status.toUpperCase(Locale.ROOT)) {
        case "PASS":
            return 4;
        case "SKIP":
            return 2;
        case "FAIL":
        case "ERROR":
            return 1;
        default:
            return 0;
        }
    }

    private static long sumRecordedDurations() {
        long total = 0L;
        for (Long duration : DURATIONS.values()) {
            if (duration != null && duration > 0) {
                total += duration;
            }
        }
        return total;
    }

    private static long parseDurationMs(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static void trackMergedTimestamps(String startedAt, String finishedAt) {
        long started = parseTestNgTimestamp(startedAt);
        long finished = parseTestNgTimestamp(finishedAt);
        if (started > 0 && (mergedEarliestStartMs <= 0 || started < mergedEarliestStartMs)) {
            mergedEarliestStartMs = started;
        }
        if (finished > 0 && finished > mergedLatestFinishMs) {
            mergedLatestFinishMs = finished;
        }
    }

    private static long parseTestNgTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        try {
            return ZonedDateTime.parse(raw.trim(), TESTNG_TIMESTAMP).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }

    /** e.g. {@code LF_O_TC120_ActivityType_BasicTest} → {@code LF_O_TC120}, {@code TC_011_COL_...} → {@code TC_011}. */
    static String deriveManualId(String className) {
        Matcher lf = LEFT_FILTER_CLASS.matcher(className);
        if (lf.matches()) {
            return lf.group(1);
        }
        Matcher tv = TABLE_VIEW_CLASS.matcher(className);
        if (tv.matches()) {
            return tv.group(1);
        }
        Matcher bsd = BSD_TABLE_VIEW_CLASS.matcher(className);
        if (bsd.matches()) {
            return bsd.group(1);
        }
        return className;
    }

    /**
     * e.g. {@code LF_O_TC420_ActivityType_SelectAllTest} → {@code Activity Type — Select All},
     * {@code TC_011_COL_titleSeasonEpisode_Show} → {@code Column — Title Season Episode — Show}.
     */
    static String deriveScenarioFromClassName(String className) {
        Matcher lf = LEFT_FILTER_CLASS.matcher(className);
        if (lf.matches()) {
            return scenarioFromUnderscoreParts(lf.group(2));
        }
        Matcher tv = TABLE_VIEW_CLASS.matcher(className);
        if (tv.matches()) {
            return scenarioFromUnderscoreParts(tv.group(2));
        }
        Matcher bsd = BSD_TABLE_VIEW_CLASS.matcher(className);
        if (bsd.matches()) {
            return scenarioFromUnderscoreParts(bsd.group(2));
        }
        return humanizeToken(className);
    }

    private static String scenarioFromUnderscoreParts(String tail) {
        String[] parts = tail.split("_");
        StringBuilder scenario = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                scenario.append(" - ");
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
        return (int) RESULTS.entrySet().stream()
                .filter(e -> !shouldSkipReportClass(e.getKey()))
                .filter(e -> e.getValue().endsWith("|" + status))
                .count();
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
