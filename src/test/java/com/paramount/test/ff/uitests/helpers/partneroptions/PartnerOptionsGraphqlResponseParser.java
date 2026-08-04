package com.paramount.test.ff.uitests.helpers.partneroptions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Parses captured AppSync GraphQL payloads and extracts Partner filter labels.
 * Preserves exact duplicate labels within API responses so raw exports can be analyzed in Excel.
 */
public final class PartnerOptionsGraphqlResponseParser {

    private PartnerOptionsGraphqlResponseParser() {
    }

    public static List<String> extractPartnerLabels(JSONArray captures) {
        List<String> merged = new ArrayList<>();
        for (Object captureObject : captures) {
            if (!(captureObject instanceof JSONObject)) {
                continue;
            }
            JSONObject capture = (JSONObject) captureObject;
            List<String> captureLabels = extractLabelsFromCapture(capture);
            merged = mergeBatches(merged, captureLabels);
        }
        return merged;
    }

    /**
     * Merges a new GraphQL batch into an accumulated list.
     * Preserves exact duplicates within the same batch, but skips scroll-overlap repeats across batches.
     */
    public static List<String> mergeBatches(List<String> merged, List<String> newBatch) {
        if (newBatch == null || newBatch.isEmpty()) {
            return merged == null || merged.isEmpty() ? Collections.emptyList() : new ArrayList<>(merged);
        }

        List<String> result = merged == null || merged.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(merged);
        Set<String> seenFromPriorCaptures = new LinkedHashSet<>(result);

        Set<String> seenInThisCapture = new HashSet<>();
        for (String label : newBatch) {
            if (!isValidPartnerLabel(label)) {
                continue;
            }
            if (seenInThisCapture.contains(label)) {
                result.add(label);
                continue;
            }

            seenInThisCapture.add(label);
            if (!seenFromPriorCaptures.contains(label)) {
                result.add(label);
                seenFromPriorCaptures.add(label);
            }
        }
        return result;
    }

    private static List<String> extractLabelsFromCapture(JSONObject capture) {
        String request = stringValue(capture.get("request"));
        String response = stringValue(capture.get("response"));
        if (!isPartnerRelated(request, response)) {
            return Collections.emptyList();
        }

        Object parsedResponse = parseJson(response);
        if (parsedResponse == null) {
            return Collections.emptyList();
        }
        return extractLargestCandidateFromNode(parsedResponse);
    }

    private static List<String> extractLargestCandidateFromNode(Object node) {
        List<CandidateList> candidates = new ArrayList<>();
        collectCandidates(node, candidates);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        CandidateList best = null;
        for (CandidateList candidate : candidates) {
            if (best == null || candidate.size() > best.size()) {
                best = candidate;
            }
        }
        return best == null ? Collections.emptyList() : new ArrayList<>(best.labels());
    }

    private static boolean isFilterArrayKey(String key) {
        return key.contains("partner")
                || key.contains("title")
                || key.contains("series");
    }

    private static boolean isValidPartnerLabel(String label) {
        if (label == null || label.isEmpty()) {
            return false;
        }
        if ("Select All".equalsIgnoreCase(label)) {
            return false;
        }
        if (label.startsWith("Select All (")) {
            return false;
        }
        if ("[No Value]".equalsIgnoreCase(label)) {
            return false;
        }
        return true;
    }

    public static List<String> extractFilterLabels(JSONArray captures) {
        return extractPartnerLabels(captures);
    }

    private static boolean isPartnerRelated(String request, String response) {
        String requestLower = request.toLowerCase(Locale.ROOT);
        String responseLower = response.toLowerCase(Locale.ROOT);
        return requestLower.contains("partner")
                || responseLower.contains("\"partner\"")
                || responseLower.contains("partnername")
                || responseLower.contains("partner_name")
                || requestLower.contains("title")
                || requestLower.contains("series")
                || responseLower.contains("\"title\"")
                || responseLower.contains("seriestitle")
                || responseLower.contains("series_title")
                || responseLower.contains("seriesname")
                || responseLower.contains("series_name");
    }

    private static void collectCandidates(Object node, List<CandidateList> candidates) {
        if (node instanceof JSONObject) {
            JSONObject object = (JSONObject) node;
            for (Object value : object.values()) {
                collectCandidates(value, candidates);
            }
            collectFromObjectMap(object, candidates);
        } else if (node instanceof JSONArray) {
            JSONArray array = (JSONArray) node;
            collectFromArray(array, candidates);
            for (Object item : array) {
                collectCandidates(item, candidates);
            }
        }
    }

    private static void collectFromObjectMap(JSONObject object, List<CandidateList> candidates) {
        for (Object entryObject : object.entrySet()) {
            if (!(entryObject instanceof Map.Entry)) {
                continue;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
            String key = String.valueOf(entry.getKey()).toLowerCase(Locale.ROOT);
            Object value = entry.getValue();
            if (value instanceof JSONArray && isFilterArrayKey(key)) {
                addCandidateFromArray((JSONArray) value, candidates);
            }
        }
    }

    private static void collectFromArray(JSONArray array, List<CandidateList> candidates) {
        if (array.isEmpty()) {
            return;
        }

        Object first = array.get(0);
        if (first instanceof String) {
            addCandidateFromStringArray(array, candidates);
            return;
        }
        if (first instanceof JSONObject) {
            addCandidateFromObjectArray(array, candidates);
        }
    }

    private static void addCandidateFromStringArray(JSONArray array, List<CandidateList> candidates) {
        List<String> labels = new ArrayList<>();
        for (Object item : array) {
            if (item != null) {
                String label = item.toString().trim();
                if (isValidPartnerLabel(label)) {
                    labels.add(label);
                }
            }
        }
        if (!labels.isEmpty()) {
            candidates.add(new CandidateList(labels));
        }
    }

    private static void addCandidateFromObjectArray(JSONArray array, List<CandidateList> candidates) {
        List<String> labels = new ArrayList<>();
        for (Object item : array) {
            if (!(item instanceof JSONObject)) {
                continue;
            }
            JSONObject object = (JSONObject) item;
            String label = firstNonEmpty(
                    stringValue(object.get("label")),
                    stringValue(object.get("name")),
                    stringValue(object.get("value")),
                    stringValue(object.get("displayName")),
                    stringValue(object.get("partnerName")),
                    stringValue(object.get("partner")),
                    stringValue(object.get("seriesTitle")),
                    stringValue(object.get("seriesName")),
                    stringValue(object.get("title")));
            if (!label.isEmpty() && isValidPartnerLabel(label)) {
                labels.add(label);
            }
        }
        if (labels.size() >= 3) {
            candidates.add(new CandidateList(labels));
        }
    }

    private static void addCandidateFromArray(JSONArray array, List<CandidateList> candidates) {
        collectFromArray(array, candidates);
    }

    private static Object parseJson(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return new JSONParser().parse(text);
        } catch (ParseException e) {
            return null;
        }
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private static final class CandidateList {
        private final List<String> labels;

        private CandidateList(List<String> labels) {
            this.labels = labels;
        }

        private int size() {
            return labels.size();
        }

        private List<String> labels() {
            return labels;
        }
    }
}
