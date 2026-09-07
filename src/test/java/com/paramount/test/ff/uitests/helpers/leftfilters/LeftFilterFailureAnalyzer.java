package com.paramount.test.ff.uitests.helpers.leftfilters;

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
            "^\\s*(.+?)\\s+expected \\[.+?\\] but found \\[.+?\\]\\s*$", Pattern.MULTILINE);
    private static final Pattern ACTUAL_IN_MESSAGE = Pattern.compile(
            "Actual:\\s*(.+?)(?:\\s+expected\\s|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private LeftFilterFailureAnalyzer() {
    }

    static FailureDetail analyze(ITestResult result, String manualId, String scenario) {
        String testCaseName = formatTestCaseName(manualId, scenario);
        Throwable throwable = result != null ? result.getThrowable() : null;
        if (throwable == null) {
            return new FailureDetail(testCaseName, manualId, scenario, "FAIL",
                    "The test failed but no error details were captured.",
                    "Test completes without assertion errors.",
                    "Unknown — see Allure report.",
                    NextAction.RAISE_DEFECT);
        }

        String fullMessage = extractFullMessage(throwable);
        List<String> assertLines = extractFailedAssertMessages(fullMessage);
        String primary = assertLines.isEmpty() ? trimToSingleLine(fullMessage) : assertLines.get(0);
        String assertionText = stripExpectedSuffix(primary);

        String summary = buildSummary(assertionText, scenario, assertLines.size());
        String expected = inferExpected(assertionText, scenario);
        String actual = extractActualValue(assertionText, fullMessage, throwable);
        NextAction nextAction = classifyNextAction(assertionText, fullMessage, throwable);

        return new FailureDetail(testCaseName, manualId, scenario, "FAIL", summary, expected, actual, nextAction);
    }

    private static String formatTestCaseName(String manualId, String scenario) {
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
            lines.add(matcher.group(1).trim());
        }
        if (lines.isEmpty() && marker >= 0) {
            for (String part : body.split("[\\r\\n]+")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    lines.add(trimmed);
                }
            }
        }
        return lines;
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
        } else if (lower.contains("divider")) {
            base = "The divider between non-zero and zero-count filter options is missing or incorrect.";
        } else if (lower.contains("non-zero") && lower.contains("before")) {
            base = "Non-zero filter options are not listed before zero-count options.";
        } else if (lower.contains("manage columns") || lower.contains("save changes")) {
            base = "Manage Columns setup did not enable or save the required column.";
        } else if (lower.contains("login") || lower.contains("filter panel not visible")) {
            base = "The test could not reach the left-filter panel (login or page load issue).";
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

    private static String inferExpected(String assertionText, String scenario) {
        String lower = assertionText.toLowerCase(Locale.ROOT);
        if (lower.contains("alphabetical") || lower.contains("alphabet")) {
            return "Visible filter options appear in A–Z order (non-zero group first, then zero-count group).";
        }
        if (lower.contains("active filter") || lower.contains("active filters")) {
            return "Active Filters shows a chip for the selected filter option.";
        }
        if (lower.contains("table") && (lower.contains("match") || lower.contains("sync"))) {
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
        String className = throwable.getClass().getSimpleName();
        if (className.contains("Timeout") || fullMessage.toLowerCase(Locale.ROOT).contains("timeout")) {
            return "Timed out waiting for the UI element or page state.";
        }
        if (fullMessage.toLowerCase(Locale.ROOT).contains("nosuchelement")
                || fullMessage.toLowerCase(Locale.ROOT).contains("no such element")) {
            return "Element not found in the DOM.";
        }
        if (!assertionText.isBlank()) {
            return trimToSingleLine(assertionText);
        }
        return trimToSingleLine(fullMessage);
    }

    private static NextAction classifyNextAction(String assertionText, String fullMessage, Throwable throwable) {
        String combined = (assertionText + " " + fullMessage + " " + throwable.getClass().getName())
                .toLowerCase(Locale.ROOT);

        if (containsAny(combined,
                "synergy", "session id", "webdriver", "remote browser", "grid connection",
                "connection refused", " unreachable ", "browser not started", "driver quit",
                "session not created", "invalid session", "disconnected")) {
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

    static final class FailureDetail {
        private final String testCaseName;
        private final String manualId;
        private final String scenario;
        private final String status;
        private final String summary;
        private final String expected;
        private final String actual;
        private final NextAction nextAction;

        FailureDetail(String testCaseName, String manualId, String scenario, String status,
                String summary, String expected, String actual, NextAction nextAction) {
            this.testCaseName = testCaseName;
            this.manualId = manualId;
            this.scenario = scenario;
            this.status = status;
            this.summary = summary;
            this.expected = expected;
            this.actual = actual;
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

        String nextAction() {
            return nextAction.label();
        }
    }
}
