package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.util.reporting.ExecutionReportReasonFormatter;
import com.paramount.test.ff.common.util.reporting.ExecutionReportReasonFormatter.ExecutionOutcome;

import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses TestNG / SoftAssert failure output into plain-English summaries for the left-filter email report.
 */
final class LeftFilterFailureAnalyzer {

    private static final Pattern ASSERT_LINE = Pattern.compile(
            "^\\s*(?:\\d+\\)\\s*)?(.+?)\\s+expected \\[(.+?)\\] but found \\[(.+?)\\]\\s*,?\\s*$",
            Pattern.MULTILINE);
    private static final Pattern EXPECTED_FOUND_OPERANDS = Pattern.compile(
            "^\\s*(?:\\d+\\)\\s*)?(.+?)\\s+expected \\[(.+?)\\] but found \\[(.+?)\\]\\s*,?\\s*$",
            Pattern.MULTILINE);
    private static final Pattern EXPECTED_FOUND_FRAGMENT = Pattern.compile(
            "expected \\[(.+?)\\] but found \\[(.+?)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTUAL_IN_MESSAGE = Pattern.compile(
            "Actual:\\s*(.+?)(?:\\s+expected\\s|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private LeftFilterFailureAnalyzer() {
    }

    static FailureDetail analyze(ITestResult result, String manualId, String scenario) {
        Throwable throwable = result != null ? result.getThrowable() : null;
        if (throwable == null) {
            return analyzeFromMessage(null, manualId, scenario);
        }
        return analyzeFromMessage(extractFullMessage(throwable), manualId, scenario, throwable);
    }

    /** Used when merging {@code testng-results.xml} where only the exception message is available. */
    static FailureDetail analyzeFromMessage(String fullMessage, String manualId, String scenario) {
        return analyzeFromMessage(fullMessage, manualId, scenario, null);
    }

    private static FailureDetail analyzeFromMessage(String fullMessage, String manualId, String scenario,
            Throwable throwable) {
        String testCaseName = formatTestCaseName(manualId, scenario);
        if (fullMessage == null || fullMessage.isBlank()) {
            return new FailureDetail(testCaseName, manualId, scenario, "ERROR",
                    "The test failed but no error details were captured.",
                    inferExpectedForScenario(scenario),
                    ExecutionReportReasonFormatter.EMPTY,
                    ExecutionReportReasonFormatter.EMPTY,
                    NextAction.RAISE_DEFECT);
        }

        List<String> assertLines = extractFailedAssertMessages(fullMessage);
        String primary = assertLines.isEmpty() ? primaryFailureLine(fullMessage) : assertLines.get(0);
        String assertionText = stripExpectedSuffix(primary);
        String stepFailureReason = assertLines.size() > 1
                ? String.join("; ", assertLines)
                : primary;
        String parsedExpected = "";
        String parsedActual = "";
        ParsedAssertOperands operands = parseAssertOperands(primary);
        if (operands != null) {
            assertionText = stripExpectedSuffix(operands.message);
            stepFailureReason = operands.message;
            parsedExpected = operands.expected;
            parsedActual = operands.actual;
        } else {
            Matcher fragmentMatcher = EXPECTED_FOUND_FRAGMENT.matcher(primary);
            if (fragmentMatcher.find()) {
                parsedExpected = fragmentMatcher.group(1).trim();
                parsedActual = fragmentMatcher.group(2).trim();
                stepFailureReason = operandsToReason(parsedExpected, parsedActual, assertionText);
            }
        }

        ExecutionOutcome outcome = ExecutionReportReasonFormatter.classifyFailure(assertionText, fullMessage, throwable);
        String status = outcome == ExecutionOutcome.ERROR ? "ERROR" : "FAIL";
        String summary = buildSummary(assertionText, scenario, assertLines.size());
        boolean booleanOperands = isBooleanOperand(parsedExpected) && isBooleanOperand(parsedActual);
        String expected = outcome == ExecutionOutcome.ERROR
                ? firstNonBlank(inferExpectedForScenario(scenario), inferExpected(assertionText, scenario))
                : firstNonBlank(booleanOperands ? inferExpectedForScenario(scenario) : inferExpected(assertionText, scenario),
                        parsedExpected);
        String actual = outcome == ExecutionOutcome.ERROR
                ? extractActualValue(assertionText, fullMessage, throwable)
                : firstNonBlank(booleanOperands
                        ? actualForBooleanFailure(scenario, assertionText, summary)
                        : buildReadableActual(scenario, parsedExpected, parsedActual, summary),
                        extractActualValue(assertionText, fullMessage, throwable));
        NextAction nextAction = classifyNextAction(assertionText, fullMessage,
                throwable != null ? throwable : new RuntimeException(fullMessage));

        return new FailureDetail(testCaseName, manualId, scenario, status, summary, expected, actual,
                stepFailureReason, nextAction);
    }

    /** First line of a TestNG / Selenium exception (drops stack trace noise). */
    static String primaryFailureLine(String fullMessage) {
        if (fullMessage == null || fullMessage.isBlank()) {
            return "";
        }
        String normalized = fullMessage.replace("\r\n", "\n").trim();
        for (String line : normalized.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (isSoftAssertHeader(trimmed)) {
                continue;
            }
            if (trimmed.startsWith("Build info:") || trimmed.startsWith("System info:")
                    || trimmed.startsWith("Driver info:") || trimmed.startsWith("Command:")
                    || trimmed.startsWith("Capabilities ") || trimmed.startsWith("Session ID:")
                    || trimmed.startsWith("Element:") || trimmed.startsWith("at ")
                    || trimmed.startsWith("Caused by:")) {
                continue;
            }
            return trimToSingleLine(trimmed);
        }
        return trimToSingleLine(normalized);
    }

    static String formatTestCaseName(String manualId, String scenario) {
        if (manualId != null && !manualId.isBlank() && scenario != null && !scenario.isBlank()) {
            return manualId + " — " + scenario;
        }
        if (scenario != null && !scenario.isBlank()) {
            return scenario;
        }
        return manualId != null ? manualId : "Unknown test";
    }

    private static String extractFullMessage(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append(current.getMessage().trim());
            }
            current = current.getCause();
        }
        return sb.toString();
    }

    private static List<String> extractFailedAssertMessages(String fullMessage) {
        List<String> lines = new ArrayList<>();
        if (fullMessage == null || fullMessage.isBlank()) {
            return lines;
        }
        int marker = fullMessage.indexOf("The following asserts failed:");
        String body = marker >= 0
                ? fullMessage.substring(marker + "The following asserts failed:".length())
                : fullMessage;
        Matcher matcher = ASSERT_LINE.matcher(body);
        while (matcher.find()) {
            lines.add(formatAssertLine(matcher.group(1).trim(), matcher.group(2).trim(), matcher.group(3).trim()));
        }
        if (lines.isEmpty() && marker >= 0) {
            for (String part : body.split("[\\r\\n]+")) {
                String trimmed = part.trim();
                if (trimmed.isEmpty() || isSoftAssertHeader(trimmed)) {
                    continue;
                }
                ParsedAssertOperands operands = parseAssertOperands(trimmed);
                if (operands != null) {
                    lines.add(formatAssertLine(operands.message, operands.expected, operands.actual));
                } else {
                    lines.add(trimmed);
                }
            }
        }
        return lines;
    }

    private static boolean isSoftAssertHeader(String line) {
        if (line == null) {
            return false;
        }
        String lower = line.trim().toLowerCase(Locale.ROOT);
        return lower.equals("the following asserts failed:")
                || lower.equals("the following asserts failed")
                || lower.contains("one or more soft assertions failed");
    }

    private static ParsedAssertOperands parseAssertOperands(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        Matcher matcher = EXPECTED_FOUND_OPERANDS.matcher(line.trim());
        if (!matcher.matches()) {
            return null;
        }
        return new ParsedAssertOperands(
                matcher.group(1).trim(),
                matcher.group(2).trim(),
                matcher.group(3).trim());
    }

    private static String formatAssertLine(String message, String expected, String actual) {
        if (message != null && !message.isBlank()) {
            if (isBooleanOperand(expected) && isBooleanOperand(actual)) {
                return message.trim();
            }
            return message.trim() + " — expected [" + expected + "] but found [" + actual + "]";
        }
        return operandsToReason(expected, actual, message);
    }

    private static String operandsToReason(String expected, String actual, String message) {
        if (expected != null && !expected.isBlank() && actual != null && !actual.isBlank()) {
            return "expected [" + expected + "] but found [" + actual + "]";
        }
        return message != null ? message.trim() : "";
    }

    private static final class ParsedAssertOperands {
        private final String message;
        private final String expected;
        private final String actual;

        private ParsedAssertOperands(String message, String expected, String actual) {
            this.message = message;
            this.expected = expected;
            this.actual = actual;
        }
    }

    private static String stripExpectedSuffix(String line) {
        if (line == null) {
            return "";
        }
        int idx = line.indexOf(" expected [");
        return idx >= 0 ? line.substring(0, idx).trim() : line.trim();
    }

    private static String trimToSingleLine(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
    }

    private static String buildSummary(String assertionText, String scenario, int failureCount) {
        String lower = assertionText.toLowerCase(Locale.ROOT);
        String base;
        if (lower.contains("alphabetical") || lower.contains("alphabet")) {
            base = "Filter options are not listed in the expected alphabetical order.";
        } else if (lower.contains("active filter") || lower.contains("active filters")) {
            base = "The selected filter value was not reflected correctly in Active Filters.";
        } else if (lower.contains("table") && (lower.contains("match") || lower.contains("sync"))) {
            base = "Table column values do not match the selected left-filter option.";
        } else if (lower.contains("record count") || lower.contains("count reflects")) {
            base = "The table record count does not match the filter selection.";
        } else if (lower.contains("select all")) {
            base = "Select All checkbox behavior did not work as expected.";
        } else if (lower.contains("search")) {
            base = "Filter search did not return the expected options.";
        } else if (lower.contains("clear filter") || lower.contains("clear control")
                || lower.contains("active filters clear")) {
            base = "Clear Filters smoke failed — active filter chip(s) or Clear control were not available as expected.";
        } else if (lower.contains("divider")) {
            base = "The divider between non-zero and zero-count filter options is missing or incorrect.";
        } else if (lower.contains("non-zero") && lower.contains("before")) {
            base = "Non-zero filter options are not listed before zero-count options.";
        } else if (lower.contains("manage columns") || lower.contains("save changes")) {
            base = "Manage Columns setup did not enable or save the required column.";
        } else if (lower.contains("login") || lower.contains("filter panel not visible")) {
            base = "The test could not reach the left-filter panel (login or page load issue).";
        } else if (lower.contains("element click intercepted") || lower.contains("element not interactable")) {
            base = "A required UI control could not be clicked because another element was blocking it.";
        } else if (lower.contains("not visible") || lower.contains("not found") || lower.contains("has no")) {
            base = "A required UI element was not found or not visible on the page.";
        } else if (!assertionText.isBlank()) {
            base = assertionText;
        } else if (scenario != null && !scenario.isBlank()) {
            base = scenario + " did not pass verification.";
        } else {
            base = "One or more verification steps failed.";
        }
        if (failureCount > 1) {
            base = base + " (" + failureCount + " assertion failures in this test.)";
        }
        return base;
    }

    static String inferExpectedForScenario(String scenario) {
        return inferExpected(scenarioHintFromScenario(scenario), scenario);
    }

    private static boolean isBooleanOperand(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lower = value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(lower) || "false".equals(lower);
    }

    private static String actualForBooleanFailure(String scenario, String assertionText, String summary) {
        String lower = scenario == null ? "" : scenario.toLowerCase(Locale.ROOT);
        if (lower.contains("table sync")) {
            if (assertionText.toLowerCase(Locale.ROOT).contains("activity type")) {
                return "Expanded order/package line-item Activity Type values did not include the selected filter option "
                        + "(checked first package, then additional packages when present).";
            }
            return "Table column values did not match the selected left-filter option after filter apply.";
        }
        if (lower.contains("active filter")) {
            return "Selected Activity Type option(s) did not appear as Active Filters chip(s) after opening the panel.";
        }
        if (lower.contains("clear filter")) {
            String assertLower = assertionText == null ? "" : assertionText.toLowerCase(Locale.ROOT);
            if (assertLower.contains("clear control") || assertLower.contains("clear button")) {
                return "Active Filters Clear button was not visible or not clickable.";
            }
            if (assertLower.contains("chip") || assertLower.contains("active filter")) {
                return "Expected Active Filters chip(s) for the current selection were missing or incorrect.";
            }
            if (assertLower.contains("selection before clear")) {
                return "No active filter selection was present before the Clear Filters check.";
            }
            if (assertLower.contains("cleared")) {
                return "Filter chip(s) remained visible after clicking Active Filters Clear.";
            }
            return "Clear Filters smoke could not verify chips or Clear control.";
        }
        if (lower.contains("select all")) {
            return "Select All checkbox state or option selection did not match the expected step.";
        }
        if (lower.contains("search")) {
            return "Filter search results did not contain only options matching the typed search text.";
        }
        if (summary != null && !summary.isBlank()) {
            return summary.trim();
        }
        return "Verification step returned false — expected condition was not met.";
    }

    static String actualForPass(String scenario) {
        if (scenario != null && !scenario.isBlank()) {
            return "Verified successfully — \"" + scenario + "\" completed with no failures.";
        }
        return "Verified successfully — all steps passed.";
    }

    static FailureDetail detailForPass(String manualId, String scenario) {
        return new FailureDetail(
                formatTestCaseName(manualId, scenario),
                manualId,
                scenario,
                "PASS",
                "",
                inferExpectedForScenario(scenario),
                actualForPass(scenario),
                "",
                NextAction.RAISE_DEFECT);
    }

    /** Canned failure for LF_O_TC999 report-preview harness (email-only aggregate resend). */
    static FailureDetail detailForReportPreviewDemoFail(String manualId, String scenario) {
        String resolvedScenario = scenario != null && !scenario.isBlank()
                ? scenario
                : "Job - Active Filters (report demo fail)";
        return new FailureDetail(
                formatTestCaseName(manualId, resolvedScenario),
                manualId,
                resolvedScenario,
                "FAIL",
                "The selected Job filter value was not reflected in Active Filters — the chip showed the wrong type.",
                "Scenario \"" + resolvedScenario + "\" passes all verification steps.",
                "Active Filters chip showed \"ZZZ Demo Job Type\" instead of the selected Job value \"ABC Demo Job Type\".",
                "",
                NextAction.RAISE_DEFECT);
    }

    private static String buildReadableActual(String scenario, String expectedValue, String actualValue,
            String summary) {
        if (actualValue != null && !actualValue.isBlank() && expectedValue != null && !expectedValue.isBlank()) {
            String lower = scenario == null ? "" : scenario.toLowerCase(Locale.ROOT);
            if (lower.contains("active filter")) {
                return "Active Filters chip showed \"" + actualValue.trim()
                        + "\" instead of the selected filter value \"" + expectedValue.trim() + "\".";
            }
            if (lower.contains("table sync") || lower.contains("table")) {
                return "Table showed \"" + actualValue.trim() + "\" where \"" + expectedValue.trim()
                        + "\" was expected for the selected filter.";
            }
            return "Observed \"" + actualValue.trim() + "\" but expected \"" + expectedValue.trim() + "\".";
        }
        if (summary != null && !summary.isBlank()) {
            return summary.trim();
        }
        return "";
    }

    static FailureDetail detailForSkip(String manualId, String scenario, String skipMessage) {
        String actual = skipMessage != null && !skipMessage.isBlank()
                ? trimToSingleLine(skipMessage)
                : "Test was not executed (skipped).";
        return new FailureDetail(
                formatTestCaseName(manualId, scenario),
                manualId,
                scenario,
                "SKIP",
                "",
                inferExpectedForScenario(scenario),
                actual,
                "",
                NextAction.SYNERGY_ISSUE);
    }

    private static String scenarioHintFromScenario(String scenario) {
        if (scenario == null || scenario.isBlank()) {
            return "";
        }
        String lower = scenario.toLowerCase(Locale.ROOT);
        if (lower.contains("table sync")) {
            return "table sync";
        }
        if (lower.contains("select all")) {
            return "select all";
        }
        if (lower.contains("active filter")) {
            return "active filters";
        }
        if (lower.contains("clear filter")) {
            return "clear filters";
        }
        if (lower.contains("option order") || lower.contains("alphabet")) {
            return "alphabetical";
        }
        if (lower.contains("search")) {
            return "search";
        }
        if (lower.contains("scroll")) {
            return "scroll";
        }
        if (lower.contains("basic")) {
            return "filter panel visible";
        }
        return "";
    }

    private static String inferExpected(String assertionText, String scenario) {
        String lower = assertionText.toLowerCase(Locale.ROOT);
        if (lower.contains("alphabetical") || lower.contains("alphabet")) {
            return "Visible filter options appear in A–Z order (non-zero group first, then zero-count group).";
        }
        if (lower.contains("active filter") || lower.contains("active filters")) {
            return "Active Filters shows a chip for the selected filter option.";
        }
        if (lower.contains("table") && (lower.contains("match") || lower.contains("sync"))) {
            if (scenario != null && scenario.toLowerCase(Locale.ROOT).contains("activity type")) {
                return "After selecting an Activity Type option and enabling the column, at least one expanded "
                        + "line-item Activity Type cell (across all packages in the order) matches the filter value.";
            }
            return "Every visible table cell in the filtered column matches the selected filter value.";
        }
        if (lower.contains("record count") || lower.contains("count reflects")) {
            return "Orders/Line Items table record count matches the filter selection count.";
        }
        if (lower.contains("select all")) {
            return "Select All selects or clears all visible options according to the test step.";
        }
        if (lower.contains("search")) {
            return "Search results only show options that contain the typed search text.";
        }
        if (lower.contains("clear filter") || scenario != null && scenario.toLowerCase(Locale.ROOT).contains("clear")) {
            return "Active Filters shows chip(s) for the current selection and Clear control is visible and clickable.";
        }
        if (lower.contains("divider")) {
            return "A visual divider separates non-zero options from zero-count options.";
        }
        if (lower.contains("non-zero") && lower.contains("before")) {
            return "All non-zero-count options appear above zero-count options.";
        }
        if (lower.contains("manage columns") || lower.contains("save changes")) {
            return "Required column is checked in Manage Columns and saved on the table view.";
        }
        if (lower.contains("not visible") || lower.contains("not found")) {
            return "The UI element referenced in the assertion is visible and interactable.";
        }
        if (scenario != null && !scenario.isBlank()) {
            return "Scenario \"" + scenario + "\" passes all verification steps.";
        }
        return "Assertion condition is true.";
    }

    private static String extractActualValue(String assertionText, String fullMessage, Throwable throwable) {
        Matcher matcher = ACTUAL_IN_MESSAGE.matcher(assertionText);
        if (matcher.find()) {
            return trimToSingleLine(matcher.group(1));
        }
        matcher = ACTUAL_IN_MESSAGE.matcher(fullMessage);
        if (matcher.find()) {
            return trimToSingleLine(matcher.group(1));
        }
        String className = throwable != null ? throwable.getClass().getSimpleName() : "";
        if (className.contains("Timeout") || fullMessage.toLowerCase(Locale.ROOT).contains("timeout")) {
            return "Timed out waiting for the UI element or page state.";
        }
        if (fullMessage.toLowerCase(Locale.ROOT).contains("nosuchelement")
                || fullMessage.toLowerCase(Locale.ROOT).contains("no such element")) {
            return "Element not found in the DOM.";
        }
        if (fullMessage.toLowerCase(Locale.ROOT).contains("element click intercepted")) {
            return "Click blocked — another UI element overlapped the target control.";
        }
        if (!assertionText.isBlank()) {
            return primaryFailureLine(assertionText);
        }
        return primaryFailureLine(fullMessage);
    }

    private static NextAction classifyNextAction(String assertionText, String fullMessage, Throwable throwable) {
        String throwableType = throwable != null ? throwable.getClass().getName() : "";
        String combined = (assertionText + " " + fullMessage + " " + throwableType).toLowerCase(Locale.ROOT);

        if (containsAny(combined,
                "synergy", "synergyserver", "unknownhost", "unknown host",
                "session id", "webdriver", "remote browser", "grid connection",
                "connection refused", "connection reset", " unreachable ", "browser not started", "driver quit",
                "session not created", "invalid session", "disconnected", "screenexception",
                "could not start a new session", "failed to create session")) {
            return NextAction.SYNERGY_ISSUE;
        }
        if (containsAny(combined,
                "login failed", "filter panel not visible", "could not log in", "authentication")) {
            return NextAction.SYNERGY_ISSUE;
        }

        if (containsAny(combined,
                "alphabetical", "alphabet", "sort order", "divider", "non-zero options appear before",
                "table record", "matches filter", "table sync", "active filters chip",
                "does not match", "wrong order", "count reflects", "export", "data comparison",
                "indeterminate", "select all selects")) {
            return NextAction.RAISE_DEFECT;
        }

        if (containsAny(combined,
                "not visible", "not found", "nosuchelement", "stale element", "cannot click",
                "element click intercepted", "timeout", "manage columns panel", "panel open",
                "checkbox", "locator", "xpath", "isdisplay", "no such element", "element not interactable")) {
            return NextAction.CODE_ISSUE;
        }

        return NextAction.RAISE_DEFECT;
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    enum NextAction {
        SYNERGY_ISSUE("Synergy Issue"),
        RAISE_DEFECT("Raise defect"),
        CODE_ISSUE("Code Issue");

        private final String label;

        NextAction(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    static final class FailureDetail {
        private final String testCaseName;
        private final String manualId;
        private final String scenario;
        private final String status;
        private final String summary;
        private final String expected;
        private final String actual;
        /** Raw failed-step / assertion text for PDF Reason ladder (not compacted). */
        private final String stepFailureReason;
        private final NextAction nextAction;

        FailureDetail(String testCaseName, String manualId, String scenario, String status,
                String summary, String expected, String actual, String stepFailureReason, NextAction nextAction) {
            this.testCaseName = testCaseName;
            this.manualId = manualId;
            this.scenario = scenario;
            this.status = status;
            this.summary = summary;
            this.expected = expected;
            this.actual = actual;
            this.stepFailureReason = stepFailureReason == null ? "" : stepFailureReason;
            this.nextAction = nextAction;
        }

        String testCaseName() {
            return testCaseName;
        }

        String status() {
            return status;
        }

        String summary() {
            return summary;
        }

        String expected() {
            return expected;
        }

        String actual() {
            return actual;
        }

        String stepFailureReason() {
            return stepFailureReason;
        }

        String nextAction() {
            return nextAction.label();
        }
    }
}
