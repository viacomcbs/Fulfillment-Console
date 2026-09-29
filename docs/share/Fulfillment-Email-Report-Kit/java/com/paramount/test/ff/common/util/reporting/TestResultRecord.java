package com.paramount.test.ff.common.util.reporting;

public class TestResultRecord {

    private final int index;
    private final String testCaseId;
    private final String testCaseName;
    private final String scenario;
    private final String status;
    private final String expected;
    private final String actual;
    private final String failureReason;
    private final long durationMs;

    public TestResultRecord(int index, String testCaseId, String scenario, String status, String expected,
            String actual, String failureReason, long durationMs) {
        this(index, testCaseId, testCaseId, scenario, status, expected, actual, failureReason, durationMs);
    }

    /** Babloo Allure reader compatibility constructor. */
    public TestResultRecord(int index, String testCaseId, String testCaseName, String scenario, String status,
            String expected, String actual, String failureReason, long durationMs) {
        this.index = index;
        this.testCaseId = testCaseId == null ? "" : testCaseId;
        this.testCaseName = testCaseName == null ? this.testCaseId : testCaseName;
        this.scenario = scenario == null ? "" : scenario;
        this.status = status == null ? "UNKNOWN" : status;
        this.expected = expected == null ? "—" : expected;
        this.actual = actual == null ? "—" : actual;
        this.failureReason = failureReason == null ? "" : failureReason;
        this.durationMs = durationMs;
    }

    public int getIndex() {
        return index;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    /** @deprecated use {@link #getTestCaseId()} */
    @Deprecated
    public String getManualId() {
        return testCaseId;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public String getScenario() {
        return scenario;
    }

    public String getStatus() {
        return status;
    }

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public boolean isFailed() {
        return "FAIL".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)
                || "ERROR".equalsIgnoreCase(status) || "BROKEN".equalsIgnoreCase(status);
    }
}
