package com.paramount.test.ff.common.util.reporting;

import java.util.ArrayList;
import java.util.List;

public final class TestResultReportFilter {

    private TestResultReportFilter() {
    }

    public static List<TestResultRecord> passedOnly(List<TestResultRecord> testResults) {
        List<TestResultRecord> passed = new ArrayList<>();
        if (testResults == null) {
            return passed;
        }
        int index = 1;
        for (TestResultRecord record : testResults) {
            if (isPassed(record)) {
                passed.add(copyWithIndex(record, index++));
            }
        }
        return passed;
    }

    public static long totalDurationMs(List<TestResultRecord> testResults) {
        long total = 0;
        if (testResults == null) {
            return total;
        }
        for (TestResultRecord record : testResults) {
            total += record.getDurationMs();
        }
        return total;
    }

    private static boolean isPassed(TestResultRecord record) {
        return record != null && "PASS".equalsIgnoreCase(record.getStatus());
    }

    private static TestResultRecord copyWithIndex(TestResultRecord record, int index) {
        return new TestResultRecord(index, record.getTestCaseId(), record.getScenario(), record.getStatus(),
                record.getExpected(), record.getActual(), record.getFailureReason(), record.getDurationMs());
    }
}
