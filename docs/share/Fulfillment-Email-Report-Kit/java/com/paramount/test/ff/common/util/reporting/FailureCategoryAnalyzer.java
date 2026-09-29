package com.paramount.test.ff.common.util.reporting;

public final class FailureCategoryAnalyzer {

    public static final String SYNERGY = "Synergy Issue";
    public static final String CODE = "Code Issue (XPath / Timeout)";
    public static final String APPLICATION = "Application Issue";
    public static final String UNKNOWN = "Investigate";

    private FailureCategoryAnalyzer() {
    }

    public static String categorize(String message, String trace) {
        String combined = safe(message) + " " + safe(trace);
        String lower = combined.toLowerCase();

        if (isSynergyIssue(lower)) {
            return SYNERGY;
        }
        if (isCodeIssue(lower)) {
            return CODE;
        }
        if (isApplicationIssue(lower, message)) {
            return APPLICATION;
        }
        return UNKNOWN;
    }

    public static String summarizeFailure(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "No failure message captured.";
        }
        String cleaned = message.replace("\r", "").replace("\n", " ").trim();
        if (cleaned.length() > 220) {
            return cleaned.substring(0, 217) + "...";
        }
        return cleaned;
    }

    public static String resolveNextAction(String failureCategory) {
        if (SYNERGY.equals(failureCategory)) {
            return "—";
        }
        if (CODE.equals(failureCategory) || APPLICATION.equals(failureCategory) || UNKNOWN.equals(failureCategory)) {
            return "Raise defect";
        }
        return "—";
    }

    private static boolean isSynergyIssue(String lower) {
        return containsAny(lower,
                "synergy",
                "synergyserver",
                "unknownhost",
                "unknown host",
                "invalid session",
                "session not created",
                "session id",
                "disconnected",
                "chrome not reachable",
                "webdriver is null",
                "webdriverexception",
                "unable to connect",
                "connection refused",
                "connection reset",
                "browser failed",
                "target frame detached",
                "no such window",
                "session deleted",
                "no active session",
                "permanently blocked for this session",
                "grid timeout",
                "remote end closed",
                "screenexception",
                "could not start a new session",
                "failed to create session");
    }

    private static boolean isCodeIssue(String lower) {
        return containsAny(lower,
                "nosuchelement",
                "no such element",
                "timeoutexception",
                "timed out",
                "timeout",
                "staleelement",
                "stale element",
                "elementnotinteractable",
                "element not interactable",
                "invalid selector",
                "invalidselector",
                "xpath",
                "locator",
                "cannot find",
                "unable to locate",
                "waiting for",
                "expected condition",
                "index out of bounds",
                "nullpointerexception",
                "classcastexception",
                "numberformatexception");
    }

    private static boolean isApplicationIssue(String lower, String message) {
        if (message != null && message.toLowerCase().contains("assert")) {
            return true;
        }
        return containsAny(lower,
                "expected [",
                "but found [",
                "assertionerror",
                "assertion failed",
                "validation failed",
                "mismatch",
                "incorrect data",
                "wrong value",
                "http status 4",
                "http status 5",
                "api returned",
                "not displayed",
                "not visible on screen",
                "feature",
                "business rule");
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
