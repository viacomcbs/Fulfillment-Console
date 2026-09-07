package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Persists PASS workflow rows across BSD-29967 runs so Jira gets one combined table.
 * State: {@code test-output/jira-evidence/BSD-29967_workflow-results-accumulated.json}
 */
public final class Bsd29967AccumulatedResultsStore {

    private static final Path STATE_FILE = Paths.get("test-output", "jira-evidence",
            "BSD-29967_workflow-results-accumulated.json");
    private static final String KEY_WORKFLOWS = "workflows";

    private Bsd29967AccumulatedResultsStore() {
    }

    public static void mergePassRows(List<Bsd29967EnvironmentResult> rows) {
        mergeCurrentRunPassRows(rows);
    }

    /** Merges PASS rows from the current run; returns all accumulated PASS rows (sorted by workflow). */
    public static List<Bsd29967EnvironmentResult> mergeCurrentRunPassRows(
            List<Bsd29967EnvironmentResult> currentRun) {
        if (currentRun == null || currentRun.isEmpty()) {
            return loadAllPassRows();
        }
        try {
            Files.createDirectories(STATE_FILE.getParent());
            Map<String, Bsd29967EnvironmentResult> merged = loadWorkflowMap();
            int added = 0;
            int updated = 0;
            for (Bsd29967EnvironmentResult result : currentRun) {
                if (!Bsd29967EnvironmentResult.STATUS_PASS.equals(result.getStatus())) {
                    continue;
                }
                if (!result.hasPopulatedFcDsids()) {
                    continue;
                }
                String key = result.getEnvironment();
                if (key.isEmpty()) {
                    continue;
                }
                if (merged.containsKey(key)) {
                    updated++;
                } else {
                    added++;
                }
                merged.put(key, result);
            }
            saveWorkflowMap(merged);
            Logger.logReportMessage("BSD-29967 — accumulated workflow results: "
                    + merged.size() + " total (+" + added + " new, " + updated + " updated this run)");
            return toSortedPassList(merged);
        } catch (Exception e) {
            Logger.logReportMessage("BSD-29967 — failed to merge accumulated workflow results: "
                    + e.getMessage());
            return passRowsFrom(currentRun);
        }
    }

    public static List<Bsd29967EnvironmentResult> loadAllPassRows() {
        try {
            return toSortedPassList(loadWorkflowMap());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static List<Bsd29967EnvironmentResult> passRowsFrom(List<Bsd29967EnvironmentResult> currentRun) {
        List<Bsd29967EnvironmentResult> passRows = new ArrayList<>();
        for (Bsd29967EnvironmentResult result : currentRun) {
            if (Bsd29967EnvironmentResult.STATUS_PASS.equals(result.getStatus())
                    && result.hasPopulatedFcDsids()) {
                passRows.add(result);
            }
        }
        Collections.sort(passRows, (a, b) -> a.getEnvironment().compareToIgnoreCase(b.getEnvironment()));
        return passRows;
    }

    private static List<Bsd29967EnvironmentResult> toSortedPassList(
            Map<String, Bsd29967EnvironmentResult> merged) {
        List<Bsd29967EnvironmentResult> rows = new ArrayList<>(merged.values());
        Collections.sort(rows, (a, b) -> a.getEnvironment().compareToIgnoreCase(b.getEnvironment()));
        return rows;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Bsd29967EnvironmentResult> loadWorkflowMap() throws Exception {
        Map<String, Bsd29967EnvironmentResult> map = new LinkedHashMap<>();
        if (!Files.exists(STATE_FILE)) {
            return map;
        }
        JSONObject root = (JSONObject) new JSONParser().parse(
                new String(Files.readAllBytes(STATE_FILE), StandardCharsets.UTF_8));
        JSONObject workflows = (JSONObject) root.get(KEY_WORKFLOWS);
        if (workflows == null) {
            return map;
        }
        for (Object keyObj : workflows.keySet()) {
            String key = String.valueOf(keyObj);
            JSONObject row = (JSONObject) workflows.get(keyObj);
            if (row == null) {
                continue;
            }
            Bsd29967EnvironmentResult result = fromJson(row);
            if (!result.getEnvironment().isEmpty() && result.hasPopulatedFcDsids()) {
                map.put(key, result);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static void saveWorkflowMap(Map<String, Bsd29967EnvironmentResult> merged) throws Exception {
        JSONObject root = new JSONObject();
        JSONObject workflows = new JSONObject();
        String stamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(new Date());
        for (Map.Entry<String, Bsd29967EnvironmentResult> entry : merged.entrySet()) {
            workflows.put(entry.getKey(), toJson(entry.getValue(), stamp));
        }
        root.put(KEY_WORKFLOWS, workflows);
        Files.write(STATE_FILE, root.toJSONString().getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private static JSONObject toJson(Bsd29967EnvironmentResult result, String stamp) {
        JSONObject row = new JSONObject();
        row.put("environment", result.getEnvironment());
        row.put("status", result.getStatus());
        row.put("orderId", result.getOrderId());
        row.put("fcDsids", toJsonArray(result.getFcDsids()));
        row.put("opsDsids", toJsonArray(result.getOpsDsids()));
        row.put("message", result.getMessage());
        row.put("opsExtractionSource", result.getOpsExtractionSource());
        row.put("opsExtractionDiagnostics", result.getOpsExtractionDiagnostics());
        row.put("updatedAt", stamp);
        return row;
    }

    @SuppressWarnings("unchecked")
    private static JSONArray toJsonArray(List<String> values) {
        JSONArray array = new JSONArray();
        if (values != null) {
            array.addAll(values);
        }
        return array;
    }

    private static Bsd29967EnvironmentResult fromJson(JSONObject row) {
        return new Bsd29967EnvironmentResult(
                stringValue(row.get("environment")),
                stringValue(row.get("status")),
                stringValue(row.get("orderId")),
                readStringList(row.get("fcDsids")),
                readStringList(row.get("opsDsids")),
                stringValue(row.get("message")),
                stringValue(row.get("opsExtractionSource")),
                stringValue(row.get("opsExtractionDiagnostics")));
    }

    @SuppressWarnings("unchecked")
    private static List<String> readStringList(Object raw) {
        if (!(raw instanceof JSONArray)) {
            return Collections.emptyList();
        }
        JSONArray array = (JSONArray) raw;
        List<String> values = new ArrayList<>();
        for (Object item : array) {
            if (item != null) {
                values.add(String.valueOf(item));
            }
        }
        return values;
    }

    private static String stringValue(Object raw) {
        return raw == null ? "" : String.valueOf(raw).trim();
    }
}
