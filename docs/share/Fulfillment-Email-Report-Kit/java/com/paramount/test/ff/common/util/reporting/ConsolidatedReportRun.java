package com.paramount.test.ff.common.util.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConsolidatedReportRun {

    private final String suiteTitle;
    private final String synergyReportUrl;
    private final long durationMs;
    private final String completedAt;
    private final List<ConsolidatedTestResult> passedTests;

    public ConsolidatedReportRun(String suiteTitle, String synergyReportUrl, long durationMs, String completedAt,
            List<ConsolidatedTestResult> passedTests) {
        this.suiteTitle = suiteTitle == null ? "Test Execution" : suiteTitle;
        this.synergyReportUrl = synergyReportUrl == null ? "" : synergyReportUrl;
        this.durationMs = durationMs;
        this.completedAt = completedAt == null ? "" : completedAt;
        this.passedTests = passedTests == null ? Collections.emptyList() : passedTests;
    }

    public static ConsolidatedReportRun from(String suiteTitle, String synergyReportUrl,
            List<TestResultRecord> passedResults, long durationMs, String completedAt) {
        List<ConsolidatedTestResult> tests = new ArrayList<>();
        if (passedResults != null) {
            for (TestResultRecord record : passedResults) {
                tests.add(new ConsolidatedTestResult(record.getManualId(), record.getTestCaseName(),
                        record.getScenario(), record.getDurationMs()));
            }
        }
        return new ConsolidatedReportRun(suiteTitle, synergyReportUrl, durationMs, completedAt, tests);
    }

    public String getSuiteTitle() {
        return suiteTitle;
    }

    public String getSynergyReportUrl() {
        return synergyReportUrl;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public List<ConsolidatedTestResult> getPassedTests() {
        return passedTests;
    }

    public int getPassCount() {
        return passedTests.size();
    }
}
