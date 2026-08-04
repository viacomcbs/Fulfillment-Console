package com.paramount.test.ff.uitests.helpers.partneroptions;

import com.paramount.test.ff.common.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads and writes Partner filter option lists for scroll-free batch validation.
 */
public final class PartnerOptionsJsonStore {

    public static final class PartnerOptionsData {
        private final String environment;
        private final String filter;
        private final String collectedAt;
        private final int expectedTotal;
        private final List<String> partners;

        public PartnerOptionsData(String environment, String filter, String collectedAt,
                                  int expectedTotal, List<String> partners) {
            this.environment = environment;
            this.filter = filter;
            this.collectedAt = collectedAt;
            this.expectedTotal = expectedTotal;
            this.partners = partners;
        }

        public String getEnvironment() {
            return environment;
        }

        public String getFilter() {
            return filter;
        }

        public String getCollectedAt() {
            return collectedAt;
        }

        public int getExpectedTotal() {
            return expectedTotal;
        }

        public List<String> getPartners() {
            return partners;
        }
    }

    private PartnerOptionsJsonStore() {
    }

    public static void write(String filePath, String environment, String filterName,
                             int expectedTotal, List<String> partners) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create directory for partner JSON: " + parent.getAbsolutePath());
        }

        int uniquePartnerCount = PartnerOptionsDuplicateAnalyzer.uniqueCount(partners);
        List<PartnerOptionsDuplicateAnalyzer.DuplicateEntry> exactDuplicates =
                PartnerOptionsDuplicateAnalyzer.findExactDuplicates(partners);

        JSONObject root = new JSONObject();
        root.put("environment", environment);
        root.put("filter", filterName);
        root.put("collectedAt", Instant.now().toString());
        root.put("expectedTotal", expectedTotal);
        root.put("partnerCount", partners.size());
        root.put("uniquePartnerCount", uniquePartnerCount);
        root.put("preserveExactDuplicates", true);

        JSONArray partnerArray = new JSONArray();
        partnerArray.addAll(partners);
        root.put("partners", partnerArray);

        JSONArray exactDuplicateArray = new JSONArray();
        for (PartnerOptionsDuplicateAnalyzer.DuplicateEntry duplicate : exactDuplicates) {
            JSONObject duplicateObject = new JSONObject();
            duplicateObject.put("label", duplicate.getLabel());
            duplicateObject.put("count", duplicate.getCount());
            exactDuplicateArray.add(duplicateObject);
        }
        root.put("exactDuplicates", exactDuplicateArray);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(root.toJSONString());
        }
        Logger.logMessage("Wrote " + partners.size() + " raw partner option(s) ("
                + uniquePartnerCount + " unique, " + exactDuplicates.size()
                + " exact duplicate group(s)) to " + file.getAbsolutePath());
    }

    public static PartnerOptionsData read(String filePath) throws IOException, ParseException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Partner options JSON not found: " + file.getAbsolutePath());
        }

        JSONParser parser = new JSONParser();
        JSONObject root;
        try (FileReader reader = new FileReader(file)) {
            root = (JSONObject) parser.parse(reader);
        }

        String environment = stringValue(root, "environment");
        String filter = stringValue(root, "filter");
        String collectedAt = stringValue(root, "collectedAt");
        int expectedTotal = intValue(root, "expectedTotal");

        JSONArray partnerArray = (JSONArray) root.get("partners");
        List<String> partners = new ArrayList<>();
        if (partnerArray != null) {
            for (Object item : partnerArray) {
                if (item != null) {
                    partners.add(item.toString());
                }
            }
        }

        return new PartnerOptionsData(environment, filter, collectedAt, expectedTotal,
                Collections.unmodifiableList(partners));
    }

    private static String stringValue(JSONObject root, String key) {
        Object value = root.get(key);
        return value == null ? "" : value.toString();
    }

    private static int intValue(JSONObject root, String key) {
        Object value = root.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }
}
