package com.paramount.test.ff.common.util.reporting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.paramount.test.ff.common.util.Logger;

public final class ConsolidatedReportStore {

    private static final String STORE_DIR = "target" + File.separator + "consolidated-report";

    private ConsolidatedReportStore() {
    }

    public static void reset(String batchId) {
        File file = resolveStoreFile(batchId);
        if (file.exists() && !file.delete()) {
            Logger.logConsoleMessage("Failed to delete consolidated report state: " + file.getAbsolutePath());
        }
    }

    public static ConsolidatedReportState load(String batchId) {
        File file = resolveStoreFile(batchId);
        if (!file.exists()) {
            return emptyState(batchId);
        }
        try (FileReader reader = new FileReader(file)) {
            JSONObject root = (JSONObject) new JSONParser().parse(reader);
            String applicationTitle = stringValue(root.get("applicationTitle"));
            String environment = stringValue(root.get("environment"));
            String createdAt = stringValue(root.get("createdAt"));
            JSONArray runsJson = (JSONArray) root.get("runs");
            List<ConsolidatedReportRun> runs = new ArrayList<>();
            if (runsJson != null) {
                for (Object runObject : runsJson) {
                    runs.add(parseRun((JSONObject) runObject));
                }
            }
            return new ConsolidatedReportState(batchId, applicationTitle, environment, createdAt, runs);
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to read consolidated report state. Starting fresh.");
            e.printStackTrace();
            return emptyState(batchId);
        }
    }

    public static void appendRun(String batchId, ConsolidatedReportRun run, String applicationTitle,
            String environment) {
        ConsolidatedReportState existing = load(batchId);
        List<ConsolidatedReportRun> runs = new ArrayList<>(existing.getRuns());
        runs.add(run);

        String createdAt = existing.getCreatedAt();
        if (createdAt == null || createdAt.isEmpty()) {
            createdAt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
        }

        ConsolidatedReportState updated = new ConsolidatedReportState(batchId,
                isBlank(applicationTitle) ? existing.getApplicationTitle() : applicationTitle,
                isBlank(environment) ? existing.getEnvironment() : environment, createdAt, runs);
        save(updated);
    }

    private static void save(ConsolidatedReportState state) {
        File file = resolveStoreFile(state.getBatchId());
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        JSONObject root = new JSONObject();
        root.put("batchId", state.getBatchId());
        root.put("applicationTitle", state.getApplicationTitle());
        root.put("environment", state.getEnvironment());
        root.put("createdAt", state.getCreatedAt());

        JSONArray runsJson = new JSONArray();
        for (ConsolidatedReportRun run : state.getRuns()) {
            runsJson.add(toJson(run));
        }
        root.put("runs", runsJson);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(root.toJSONString());
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to save consolidated report state.");
            e.printStackTrace();
        }
    }

    private static JSONObject toJson(ConsolidatedReportRun run) {
        JSONObject runJson = new JSONObject();
        runJson.put("suiteTitle", run.getSuiteTitle());
        runJson.put("synergyReportUrl", run.getSynergyReportUrl());
        runJson.put("durationMs", run.getDurationMs());
        runJson.put("completedAt", run.getCompletedAt());

        JSONArray testsJson = new JSONArray();
        for (ConsolidatedTestResult test : run.getPassedTests()) {
            JSONObject testJson = new JSONObject();
            testJson.put("manualId", test.getManualId());
            testJson.put("testCaseName", test.getTestCaseName());
            testJson.put("scenario", test.getScenario());
            testJson.put("durationMs", test.getDurationMs());
            testsJson.add(testJson);
        }
        runJson.put("passedTests", testsJson);
        return runJson;
    }

    private static ConsolidatedReportRun parseRun(JSONObject runJson) {
        String suiteTitle = stringValue(runJson.get("suiteTitle"));
        String synergyReportUrl = stringValue(runJson.get("synergyReportUrl"));
        long durationMs = longValue(runJson.get("durationMs"));
        String completedAt = stringValue(runJson.get("completedAt"));

        List<ConsolidatedTestResult> tests = new ArrayList<>();
        JSONArray testsJson = (JSONArray) runJson.get("passedTests");
        if (testsJson != null) {
            for (Object testObject : testsJson) {
                JSONObject testJson = (JSONObject) testObject;
                tests.add(new ConsolidatedTestResult(stringValue(testJson.get("manualId")),
                        stringValue(testJson.get("testCaseName")), stringValue(testJson.get("scenario")),
                        longValue(testJson.get("durationMs"))));
            }
        }
        return new ConsolidatedReportRun(suiteTitle, synergyReportUrl, durationMs, completedAt, tests);
    }

    private static ConsolidatedReportState emptyState(String batchId) {
        return new ConsolidatedReportState(batchId, "Automation", "UNKNOWN", "", new ArrayList<>());
    }

    private static File resolveStoreFile(String batchId) {
        String safeBatchId = (batchId == null || batchId.trim().isEmpty() ? "default" : batchId.trim())
                .replaceAll("[^A-Za-z0-9_-]", "_");
        return new File(System.getProperty("user.dir") + File.separator + STORE_DIR,
                safeBatchId + "-consolidated-report.json");
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return 0L;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
