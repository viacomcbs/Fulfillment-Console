package com.paramount.test.ff.common.util.reporting;

public final class ConsolidatedTestResult {

    private final String manualId;
    private final String testCaseName;
    private final String scenario;
    private final long durationMs;

    public ConsolidatedTestResult(String manualId, String testCaseName, String scenario, long durationMs) {
        this.manualId = manualId == null ? "" : manualId;
        this.testCaseName = testCaseName == null ? "" : testCaseName;
        this.scenario = scenario == null ? "" : scenario;
        this.durationMs = durationMs;
    }

    public String getManualId() {
        return manualId;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public String getScenario() {
        return scenario;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
