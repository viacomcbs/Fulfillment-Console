package com.paramount.test.ff.common.util.reporting;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives Expected, Actual, and Reason for execution report templates (image + PDF).
 *
 * <p>{@link ExecutionOutcome#PASSED} and {@link ExecutionOutcome#SKIPPED} populate Expected and Actual when provided;
 * Reason stays {@code —}. Only {@link ExecutionOutcome#FAILED} and {@link ExecutionOutcome#ERROR} populate Reason.
 *
 * <ul>
 *   <li><b>Failed</b> — assertion mismatch; Reason prefers {@code expected X but was Y}.</li>
 *   <li><b>Error</b> — automation could not complete (locator, timeout, code); Reason shows execution error text.</li>
 * </ul>
 */
public final class ExecutionReportReasonFormatter {

    public static final String EMPTY = "—";

    private static final Pattern EXPECTED_BUT_WAS = Pattern.compile(
            "expected\\s+(?:exact\\s+text\\s+)?['\"]?(.+?)['\"]?\\s+but\\s+was\\s+(?:exact\\s+text\\s+)?['\"]?(.+?)['\"]\\s*$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern GENERIC_SOFT_ASSERT = Pattern.compile(
            ".*\\b(?:one or more soft assertions failed|the following asserts failed:?|assertion failed)\\b.*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern DEFECT_REFERENCE = Pattern.compile(
            "^(Defect\\s*:\\s*)(https?://\\S+)\\s*$",
            Pattern.CASE_INSENSITIVE);

    private ExecutionReportReasonFormatter() {
    }

    /** Parsed defect override such as {@code Defect : https://…/browse/BSD-30575}. */
    public static final class DefectReference {
        private final String label;
        private final String url;

        public DefectReference(String label, String url) {
            this.label = label == null ? "" : label;
            this.url = url == null ? "" : url;
        }

        public String getLabel() {
            return label;
        }

        public String getUrl() {
            return url;
        }
    }

    /** When failure reason is a known defect override, returns label + Jira URL for styled rendering. */
    public static Optional<DefectReference> parseDefectReference(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = DEFECT_REFERENCE.matcher(text.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new DefectReference(matcher.group(1).trim(), matcher.group(2).trim()));
    }

    public enum ExecutionOutcome {
        PASSED,
        FAILED,
        ERROR,
        SKIPPED,
        UNKNOWN
    }

    /** Maps TestNG / Allure status strings to report outcomes. BROKEN → ERROR. */
    public static ExecutionOutcome outcomeFromStatus(String status) {
        if (status == null) {
            return ExecutionOutcome.UNKNOWN;
        }
        switch (status.trim().toUpperCase(Locale.ROOT)) {
        case "PASS":
        case "PASSED":
            return ExecutionOutcome.PASSED;
        case "FAIL":
        case "FAILED":
            return ExecutionOutcome.FAILED;
        case "ERROR":
        case "BROKEN":
            return ExecutionOutcome.ERROR;
        case "SKIP":
        case "SKIPPED":
            return ExecutionOutcome.SKIPPED;
        default:
            return ExecutionOutcome.UNKNOWN;
        }
    }

    /** Display label for the Test Case Status column. */
    public static String statusLabel(ExecutionOutcome outcome) {
        switch (outcome) {
        case FAILED:
            return "Failed";
        case ERROR:
            return "Failed";
        case PASSED:
            return "Passed";
        case SKIPPED:
            return "Skipped";
        default:
            return "Unknown";
        }
    }

    public static String statusLabel(String rawStatus) {
        return statusLabel(outcomeFromStatus(rawStatus));
    }

    /** True when Reason should be populated (failures and automation errors only). */
    public static boolean showsDiagnostics(ExecutionOutcome outcome) {
        return outcome == ExecutionOutcome.FAILED || outcome == ExecutionOutcome.ERROR;
    }

    /** True when Expected / Actual columns should be populated. */
    public static boolean showsOutcomeColumns(ExecutionOutcome outcome) {
        return outcome == ExecutionOutcome.PASSED
                || outcome == ExecutionOutcome.SKIPPED
                || showsDiagnostics(outcome);
    }

    /**
     * Expected column: scenario inference for PASS/SKIP; failed-step or inferred value for FAILED; {@code —} for ERROR.
     */
    public static String expectedForReport(ExecutionOutcome outcome, String expected) {
        if (!showsOutcomeColumns(outcome)) {
            return EMPTY;
        }
        return blankToDash(expected);
    }

    /**
     * Actual column: pass confirmation for PASSED; skip reason for SKIPPED; failed-step actual for FAILED;
     * execution error text for ERROR.
     */
    public static String actualForReport(ExecutionOutcome outcome, String actual, String errorDetail) {
        if (!showsOutcomeColumns(outcome)) {
            return EMPTY;
        }
        if (outcome == ExecutionOutcome.ERROR) {
            return blankToDash(firstNonBlank(errorDetail, actual));
        }
        return blankToDash(actual);
    }

    /**
     * Reason for narrow image cells — prefers compact {@code expected X but was Y}, then compacted step reason,
     * then non-generic summary, else {@code Failed} / {@code Error}.
     */
    public static String failureReasonForImage(ExecutionOutcome outcome, String expected, String actual,
            String stepFailureReason, String failureSummary) {
        if (!showsDiagnostics(outcome)) {
            return EMPTY;
        }
        if (outcome == ExecutionOutcome.ERROR) {
            return compactReason(firstNonBlank(stepFailureReason, failureSummary, actual, "Error"));
        }
        String fromOperands = buildExpectedButWas(expected, actual);
        if (!fromOperands.isEmpty()) {
            return compactReason(fromOperands);
        }
        if (stepFailureReason != null && !stepFailureReason.isBlank()) {
            return compactReason(stepFailureReason);
        }
        if (failureSummary != null && !failureSummary.isBlank() && !isGenericSoftAssertSummary(failureSummary)) {
            return compactReason(failureSummary);
        }
        return "Failed";
    }

    /**
     * Reason for PDF — plain-English summary first, then readable actual, then technical step text.
     */
    public static String fullFailureReason(ExecutionOutcome outcome, String expected, String actual,
            String stepFailureReason, String failureSummary) {
        if (!showsDiagnostics(outcome)) {
            return EMPTY;
        }
        if (outcome == ExecutionOutcome.ERROR) {
            return firstNonBlank(failureSummary, actual, stepFailureReason, "Automation error — test could not complete.");
        }
        if (failureSummary != null && !failureSummary.isBlank() && !isGenericSoftAssertSummary(failureSummary)) {
            return failureSummary.trim();
        }
        if (actual != null && !actual.isBlank() && !EMPTY.equals(actual.trim())
                && !looksLikeTechnicalMismatch(actual)) {
            return actual.trim();
        }
        if (stepFailureReason != null && !stepFailureReason.isBlank()) {
            return compactReason(stepFailureReason);
        }
        String fromOperands = buildExpectedButWas(expected, actual);
        if (!fromOperands.isEmpty()) {
            return compactReason(fromOperands);
        }
        return "Verification failed — result did not match expected behavior.";
    }

    private static boolean looksLikeTechnicalMismatch(String value) {
        if (value == null) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.contains("expected [") && lower.contains("] but found [");
    }

    /**
     * Classify a TestNG failure as FAILED (assertion) vs ERROR (automation/code) using message heuristics.
     */
    public static ExecutionOutcome classifyFailure(String assertionText, String fullMessage, Throwable throwable) {
        String combined = safe(assertionText) + " " + safe(fullMessage);
        if (throwable != null) {
            combined = combined + " " + throwable.getClass().getName();
        }
        String lower = combined.toLowerCase(Locale.ROOT);

        if (containsAny(lower,
                "synergy", "session id", "webdriver", "remote browser", "grid connection",
                "connection refused", "browser not started", "driver quit",
                "session not created", "invalid session", "disconnected")) {
            return ExecutionOutcome.ERROR;
        }
        if (containsAny(lower,
                "nosuchelement", "no such element", "stale element", "element click intercepted",
                "element not interactable", "timeout", "timed out", "timeoutexception",
                "cannot click", "locator", "xpath", "nullpointerexception", "classcastexception",
                "index out of bounds", "unable to locate", "expected condition")) {
            return ExecutionOutcome.ERROR;
        }
        if (containsAny(lower,
                "expected [", "but found [", "assertion", "the following asserts failed",
                "mismatch", "does not match", "wrong order", "alphabetical")) {
            return ExecutionOutcome.FAILED;
        }
        if (throwable != null && assertionText != null && !assertionText.isBlank()) {
            return ExecutionOutcome.FAILED;
        }
        return ExecutionOutcome.ERROR;
    }

    /** Strip boilerplate so mismatch text fits narrow Reason cells. */
    static String compactReason(String text) {
        if (text == null || text.isBlank()) {
            return EMPTY;
        }
        String trimmed = text.replaceAll("[\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();

        Matcher matcher = EXPECTED_BUT_WAS.matcher(trimmed);
        if (matcher.find()) {
            return "expected " + stripQuotes(matcher.group(1)) + " but was " + stripQuotes(matcher.group(2));
        }

        int because = trimmed.toLowerCase(Locale.ROOT).indexOf(" because ");
        if (because >= 0 && because + " because ".length() < trimmed.length()) {
            trimmed = trimmed.substring(because + " because ".length()).trim();
            matcher = EXPECTED_BUT_WAS.matcher(trimmed);
            if (matcher.find()) {
                return "expected " + stripQuotes(matcher.group(1)) + " but was " + stripQuotes(matcher.group(2));
            }
        }

        return stripQuotes(trimmed);
    }

    static String buildExpectedButWas(String expected, String actual) {
        if (expected == null || actual == null || expected.isBlank() || actual.isBlank()) {
            return "";
        }
        if (EMPTY.equals(expected.trim()) || EMPTY.equals(actual.trim())) {
            return "";
        }
        return "expected " + stripQuotes(expected.trim()) + " but was " + stripQuotes(actual.trim());
    }

    static String stripQuotes(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        if ((v.startsWith("'") && v.endsWith("'")) || (v.startsWith("\"") && v.endsWith("\""))) {
            return v.substring(1, v.length() - 1).trim();
        }
        return v;
    }

    static boolean isGenericSoftAssertSummary(String summary) {
        return summary != null && GENERIC_SOFT_ASSERT.matcher(summary).matches();
    }

    private static String blankToDash(String value) {
        if (value == null || value.isBlank() || EMPTY.equals(value.trim())) {
            return EMPTY;
        }
        return value.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank() && !EMPTY.equals(value.trim())) {
                return value.trim();
            }
        }
        return EMPTY;
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
