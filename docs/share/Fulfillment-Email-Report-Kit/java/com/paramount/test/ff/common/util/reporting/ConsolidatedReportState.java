package com.paramount.test.ff.common.util.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConsolidatedReportState {

    private final String batchId;
    private final String applicationTitle;
    private final String environment;
    private final String createdAt;
    private final List<ConsolidatedReportRun> runs;

    public ConsolidatedReportState(String batchId, String applicationTitle, String environment, String createdAt,
            List<ConsolidatedReportRun> runs) {
        this.batchId = batchId == null ? "default" : batchId;
        this.applicationTitle = applicationTitle == null ? "Automation" : applicationTitle;
        this.environment = environment == null ? "UNKNOWN" : environment;
        this.createdAt = createdAt == null ? "" : createdAt;
        this.runs = runs == null ? Collections.emptyList() : runs;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getApplicationTitle() {
        return applicationTitle;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<ConsolidatedReportRun> getRuns() {
        return runs;
    }

    public int getRunCount() {
        return runs.size();
    }

    public int getTotalPassCount() {
        int total = 0;
        for (ConsolidatedReportRun run : runs) {
            total += run.getPassCount();
        }
        return total;
    }

    public long getTotalDurationMs() {
        long total = 0;
        for (ConsolidatedReportRun run : runs) {
            total += run.getDurationMs();
        }
        return total;
    }

    public List<TestResultRecord> toFlatPassedResults() {
        List<TestResultRecord> flat = new ArrayList<>();
        int index = 1;
        for (ConsolidatedReportRun run : runs) {
            for (ConsolidatedTestResult test : run.getPassedTests()) {
                flat.add(new TestResultRecord(index++, test.getManualId(), test.getTestCaseName(),
                        "[" + run.getSuiteTitle() + "] " + test.getScenario(), "PASS", "", "", "",
                        test.getDurationMs()));
            }
        }
        return flat;
    }
}
