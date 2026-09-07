package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Round-robin selection for left-filter non-zero options across test runs.
 * State: {@code test-output/left-filter-option-rotation.json} (one index per filter pool).
 */
public final class LeftFilterOptionRotationUtil {

    private static final Path STATE_FILE =
            Paths.get("test-output", "left-filter-option-rotation.json");
    private static final String KEY_POOLS = "pools";
    private static final String KEY_POOL_SIGNATURE = "poolSignature";
    private static final String KEY_NEXT_START_INDEX = "nextStartIndex";

    private LeftFilterOptionRotationUtil() {
    }

    public static RotationResult pickNext(String poolKey, List<String> allOptions, int pickCount) {
        if (allOptions == null || allOptions.isEmpty()) {
            return new RotationResult(Collections.emptyList(), 0, 0, "");
        }
        String normalizedKey = poolKey == null ? "filter" : poolKey.trim();
        String poolSignature = String.join("|", allOptions);
        String poolId = normalizedKey + "::" + poolSignature;
        int startIndex = loadNextStartIndex(poolId, poolSignature);
        int count = Math.min(Math.max(pickCount, 0), allOptions.size());
        List<String> selected = new ArrayList<>();
        for (int offset = 0; offset < count; offset++) {
            String option = allOptions.get((startIndex + offset) % allOptions.size());
            if (!selected.contains(option)) {
                selected.add(option);
            }
        }
        int nextStartIndex = (startIndex + count) % allOptions.size();
        saveNextStartIndex(poolId, poolSignature, nextStartIndex);
        String summary = "round-robin " + count + " option(s) for " + normalizedKey
                + " (start index " + startIndex + ", next " + nextStartIndex
                + "; pool size " + allOptions.size() + ")";
        Logger.logReportMessage("Left filter rotation — " + summary + " → " + selected);
        return new RotationResult(selected, startIndex, nextStartIndex, summary);
    }

    @SuppressWarnings("unchecked")
    private static int loadNextStartIndex(String poolId, String poolSignature) {
        if (!Files.exists(STATE_FILE)) {
            migrateLegacyBsd29967State(poolId, poolSignature);
        }
        if (!Files.exists(STATE_FILE)) {
            return 0;
        }
        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(
                    new String(Files.readAllBytes(STATE_FILE), StandardCharsets.UTF_8));
            JSONObject pools = root.get(KEY_POOLS) instanceof JSONObject
                    ? (JSONObject) root.get(KEY_POOLS) : new JSONObject();
            if (!pools.containsKey(poolId)) {
                return 0;
            }
            JSONObject poolState = (JSONObject) pools.get(poolId);
            String savedSignature = poolState.get(KEY_POOL_SIGNATURE) == null
                    ? "" : poolState.get(KEY_POOL_SIGNATURE).toString();
            if (!poolSignature.equals(savedSignature)) {
                Logger.logReportMessage("Left filter rotation — pool changed for " + poolId + "; resetting to index 0");
                return 0;
            }
            Object raw = poolState.get(KEY_NEXT_START_INDEX);
            if (raw instanceof Number) {
                return Math.max(0, ((Number) raw).intValue());
            }
            if (raw != null) {
                return Math.max(0, Integer.parseInt(raw.toString()));
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Left filter rotation state read failed; starting at 0: " + e.getMessage());
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static void saveNextStartIndex(String poolId, String poolSignature, int nextStartIndex) {
        try {
            Files.createDirectories(STATE_FILE.getParent());
            JSONObject root = new JSONObject();
            JSONObject pools = new JSONObject();
            if (Files.exists(STATE_FILE)) {
                JSONParser parser = new JSONParser();
                JSONObject existing = (JSONObject) parser.parse(
                        new String(Files.readAllBytes(STATE_FILE), StandardCharsets.UTF_8));
                if (existing.get(KEY_POOLS) instanceof JSONObject) {
                    pools = (JSONObject) existing.get(KEY_POOLS);
                }
            }
            JSONObject poolState = new JSONObject();
            poolState.put(KEY_POOL_SIGNATURE, poolSignature);
            poolState.put(KEY_NEXT_START_INDEX, nextStartIndex);
            pools.put(poolId, poolState);
            root.put(KEY_POOLS, pools);
            Files.write(STATE_FILE, root.toJSONString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Logger.logConsoleMessage("Left filter rotation state write failed: " + e.getMessage());
        }
    }

    /** One-time import from BSD-29967-only rotation file (Environment pool). */
    private static void migrateLegacyBsd29967State(String poolId, String poolSignature) {
        Path legacy = Paths.get("test-output", "bsd29967-workflow-rotation.json");
        if (!Files.exists(legacy) || !poolId.startsWith("Environment::")) {
            return;
        }
        try {
            JSONParser parser = new JSONParser();
            JSONObject legacyState = (JSONObject) parser.parse(
                    new String(Files.readAllBytes(legacy), StandardCharsets.UTF_8));
            String savedSignature = legacyState.get(KEY_POOL_SIGNATURE) == null
                    ? "" : legacyState.get(KEY_POOL_SIGNATURE).toString();
            if (!poolSignature.equals(savedSignature)) {
                return;
            }
            Object raw = legacyState.get(KEY_NEXT_START_INDEX);
            int next = 0;
            if (raw instanceof Number) {
                next = ((Number) raw).intValue();
            } else if (raw != null) {
                next = Integer.parseInt(raw.toString());
            }
            saveNextStartIndex(poolId, poolSignature, next);
            Logger.logReportMessage("Left filter rotation — migrated index from bsd29967-workflow-rotation.json");
        } catch (Exception e) {
            Logger.logConsoleMessage("Left filter rotation legacy migrate skipped: " + e.getMessage());
        }
    }

    public static final class RotationResult {
        private final List<String> selected;
        private final int startIndex;
        private final int nextStartIndex;
        private final String summary;

        RotationResult(List<String> selected, int startIndex, int nextStartIndex, String summary) {
            this.selected = Collections.unmodifiableList(new ArrayList<>(selected));
            this.startIndex = startIndex;
            this.nextStartIndex = nextStartIndex;
            this.summary = summary == null ? "" : summary;
        }

        public List<String> getSelected() {
            return selected;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getNextStartIndex() {
            return nextStartIndex;
        }

        public String getSummary() {
            return summary;
        }

        public String getFirstOrNull() {
            return selected.isEmpty() ? null : selected.get(0);
        }
    }
}
