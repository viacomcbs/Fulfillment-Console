package com.paramount.test.ff.common.util.reporting;

import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.paramount.test.ff.common.util.Logger;

public final class AllureResultsReader {

    private static final Pattern MANUAL_ID_FROM_DESCRIPTION = Pattern.compile("^([A-Za-z0-9_-]+)\\s*:");
    private static final Pattern MANUAL_ID_FROM_CLASS = Pattern.compile("TC_([A-Z0-9_]+)");

    private AllureResultsReader() {
    }

    public static List<TestResultRecord> readResults(File allureResultsDir) {
        return readResults(allureResultsDir, null);
    }

    public static List<TestResultRecord> readTodayResults(File allureResultsDir) {
        return readResultsForDate(allureResultsDir, LocalDate.now());
    }

    public static List<TestResultRecord> readYesterdayResults(File allureResultsDir) {
        return readResultsForDate(allureResultsDir, LocalDate.now().minusDays(1));
    }

    private static List<TestResultRecord> readResultsForDate(File allureResultsDir, LocalDate targetDate) {
        return readResults(allureResultsDir, targetDate);
    }

    private static List<TestResultRecord> readResults(File allureResultsDir, LocalDate targetDate) {
        List<TestResultRecord> records = new ArrayList<>();
        if (allureResultsDir == null || !allureResultsDir.exists()) {
            return records;
        }

        File[] files = allureResultsDir.listFiles((dir, name) -> name.endsWith("-result.json"));
        if (files == null) {
            return records;
        }

        int index = 1;
        List<JSONObject> parsed = new ArrayList<>();
        for (File file : files) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(new FileReader(file));
                if (targetDate != null && !isOnDate(json, targetDate)) {
                    continue;
                }
                parsed.add(json);
            } catch (Exception e) {
                Logger.logConsoleMessage("Failed to parse Allure result: " + file.getName());
            }
        }

        parsed.sort(Comparator.comparing(AllureResultsReader::getSortKey));

        for (JSONObject json : parsed) {
            records.add(toRecord(index++, json));
        }
        return records;
    }

    public static String findScreenRecordingLink(File allureResultsDir) {
        if (allureResultsDir == null || !allureResultsDir.exists()) {
            return "";
        }
        File[] files = allureResultsDir.listFiles((dir, name) -> name.endsWith("-result.json"));
        if (files == null) {
            return "";
        }

        for (File file : files) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(new FileReader(file));
                JSONArray attachments = (JSONArray) json.get("attachments");
                if (attachments == null) {
                    continue;
                }
                for (Object attachmentObj : attachments) {
                    JSONObject attachment = (JSONObject) attachmentObj;
                    String name = stringValue(attachment.get("name"));
                    if (name == null || !name.toLowerCase().contains("screen recording")) {
                        continue;
                    }
                    String source = stringValue(attachment.get("source"));
                    if (source != null && source.startsWith("http")) {
                        return source;
                    }
                    if (source != null && !source.isEmpty()) {
                        File attachmentFile = new File(allureResultsDir, source);
                        if (attachmentFile.exists()) {
                            String content = readAttachmentContent(attachmentFile);
                            if (content.startsWith("http")) {
                                return content.trim();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.logConsoleMessage("Failed to read screen recording from: " + file.getName());
            }
        }
        return "";
    }

    private static String readAttachmentContent(File attachmentFile) {
        try (FileReader reader = new FileReader(attachmentFile)) {
            char[] buffer = new char[(int) Math.min(attachmentFile.length(), 2048)];
            int read = reader.read(buffer);
            return read > 0 ? new String(buffer, 0, read) : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static long calculateTotalDurationMs(List<TestResultRecord> records) {
        long total = 0;
        for (TestResultRecord record : records) {
            total += record.getDurationMs();
        }
        return total;
    }

    private static TestResultRecord toRecord(int index, JSONObject json) {
        String status = normalizeStatus(stringValue(json.get("status")));
        String description = stringValue(json.get("description"));
        String testClass = getLabel(json, "testClass");
        String testMethod = stringValue(json.get("name"));
        String testCaseName = extractTestCaseName(testClass, testMethod);
        String manualId = extractManualId(description, testClass);
        String scenario = extractScenario(description, testMethod, testClass);

        String message = "";
        String trace = "";
        JSONObject statusDetails = (JSONObject) json.get("statusDetails");
        if (statusDetails != null) {
            message = stringValue(statusDetails.get("message"));
            trace = stringValue(statusDetails.get("trace"));
        }

        ExecutionReportReasonFormatter.ExecutionOutcome outcome = ExecutionReportReasonFormatter.outcomeFromStatus(status);
        String reportStatus = ExecutionReportReasonFormatter.statusLabel(outcome);
        String expected = ExecutionReportReasonFormatter.EMPTY;
        String actual = ExecutionReportReasonFormatter.EMPTY;
        String failureReason = ExecutionReportReasonFormatter.EMPTY;
        if (ExecutionReportReasonFormatter.showsDiagnostics(outcome)) {
            String parsedExpected = parseExpectedOperand(message);
            String parsedActual = parseActualOperand(message);
            if (outcome == ExecutionReportReasonFormatter.ExecutionOutcome.ERROR) {
                actual = ExecutionReportReasonFormatter.actualForReport(outcome, "",
                        firstNonBlank(parsedActual, message));
            } else {
                expected = ExecutionReportReasonFormatter.expectedForReport(outcome,
                        firstNonBlank(parsedExpected, FailureCategoryAnalyzer.summarizeFailure(message)));
                actual = ExecutionReportReasonFormatter.actualForReport(outcome,
                        firstNonBlank(parsedActual, message), message);
            }
            failureReason = ExecutionReportReasonFormatter.fullFailureReason(outcome, expected, actual, message,
                    message);
        }

        long durationMs = 0;
        Object start = json.get("start");
        Object stop = json.get("stop");
        if (start instanceof Number && stop instanceof Number) {
            durationMs = ((Number) stop).longValue() - ((Number) start).longValue();
        }

        return new TestResultRecord(index, manualId, testCaseName, scenario, reportStatus, expected, actual,
                failureReason, durationMs);
    }

    private static String normalizeStatus(String status) {
        if (status == null) {
            return "UNKNOWN";
        }
        if ("passed".equalsIgnoreCase(status)) {
            return "PASS";
        }
        if ("failed".equalsIgnoreCase(status)) {
            return "FAIL";
        }
        if ("broken".equalsIgnoreCase(status)) {
            return "BROKEN";
        }
        if ("skipped".equalsIgnoreCase(status)) {
            return "SKIP";
        }
        return status.toUpperCase();
    }

    private static String extractTestCaseName(String testClass, String testMethod) {
        if (testClass != null && !testClass.trim().isEmpty()) {
            int lastDot = testClass.lastIndexOf('.');
            String shortName = lastDot >= 0 ? testClass.substring(lastDot + 1) : testClass;
            if (!shortName.isEmpty()) {
                return shortName;
            }
        }
        return humanize(testMethod);
    }

    private static String extractManualId(String description, String testClass) {
        if (description != null) {
            Matcher matcher = MANUAL_ID_FROM_DESCRIPTION.matcher(description.trim());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        if (testClass != null) {
            Matcher matcher = MANUAL_ID_FROM_CLASS.matcher(testClass);
            if (matcher.find()) {
                return formatManualId(matcher.group(1));
            }
            int lastDot = testClass.lastIndexOf('.');
            return lastDot >= 0 ? testClass.substring(lastDot + 1) : testClass;
        }
        return "N/A";
    }

    private static String formatManualId(String raw) {
        if (raw.startsWith("BSD_")) {
            return raw.replaceFirst("BSD_", "BSD-").replace('_', '-');
        }
        return raw.replace('_', '-');
    }

    private static String extractScenario(String description, String testMethod, String testClass) {
        if (description != null && !description.trim().isEmpty()) {
            int colon = description.indexOf(':');
            if (colon >= 0 && colon + 1 < description.length()) {
                return description.substring(colon + 1).trim();
            }
            return description.trim();
        }
        if (testMethod != null && !testMethod.trim().isEmpty()) {
            return humanize(testMethod);
        }
        return testClass == null ? "Test Scenario" : testClass;
    }

    private static String humanize(String value) {
        return value.replaceAll("([a-z])([A-Z])", "$1 $2").replace('_', ' ');
    }

    private static String getLabel(JSONObject json, String labelName) {
        JSONArray labels = (JSONArray) json.get("labels");
        if (labels == null) {
            return "";
        }
        for (Object labelObj : labels) {
            JSONObject label = (JSONObject) labelObj;
            if (labelName.equals(stringValue(label.get("name")))) {
                return stringValue(label.get("value"));
            }
        }
        return "";
    }

    private static String getSortKey(JSONObject json) {
        String testClass = getLabel(json, "testClass");
        String testMethod = stringValue(json.get("name"));
        return testClass + "#" + testMethod;
    }

    private static boolean isOnDate(JSONObject json, LocalDate targetDate) {
        Object stop = json.get("stop");
        if (!(stop instanceof Number)) {
            return false;
        }
        LocalDate resultDate = Instant.ofEpochMilli(((Number) stop).longValue())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return resultDate.equals(targetDate);
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String parseExpectedOperand(String message) {
        Matcher matcher = Pattern.compile("expected \\[(.+?)\\] but found \\[(.+?)\\]").matcher(safe(message));
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private static String parseActualOperand(String message) {
        Matcher matcher = Pattern.compile("expected \\[(.+?)\\] but found \\[(.+?)\\]").matcher(safe(message));
        return matcher.find() ? matcher.group(2).trim() : "";
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
