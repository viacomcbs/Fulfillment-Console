package com.paramount.test.ff.uitests.helpers.partneroptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Finds exact and case-insensitive duplicate partner labels in a raw collected list.
 */
public final class PartnerOptionsDuplicateAnalyzer {

    public static final class DuplicateEntry {
        private final String label;
        private final int count;

        public DuplicateEntry(String label, int count) {
            this.label = label;
            this.count = count;
        }

        public String getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }
    }

    public static final class CaseInsensitiveGroup {
        private final String canonicalLabel;
        private final List<String> variants;

        public CaseInsensitiveGroup(String canonicalLabel, List<String> variants) {
            this.canonicalLabel = canonicalLabel;
            this.variants = variants;
        }

        public String getCanonicalLabel() {
            return canonicalLabel;
        }

        public List<String> getVariants() {
            return variants;
        }
    }

    private PartnerOptionsDuplicateAnalyzer() {
    }

    public static int uniqueCount(List<String> partners) {
        return new LinkedHashSet<>(partners).size();
    }

    public static List<DuplicateEntry> findExactDuplicates(List<String> partners) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String partner : partners) {
            counts.merge(partner, 1, Integer::sum);
        }

        List<DuplicateEntry> duplicates = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > 1) {
                duplicates.add(new DuplicateEntry(entry.getKey(), entry.getValue()));
            }
        }
        duplicates.sort(Comparator.comparing(DuplicateEntry::getLabel, String.CASE_INSENSITIVE_ORDER));
        return duplicates;
    }

    public static List<CaseInsensitiveGroup> findCaseInsensitiveDuplicates(List<String> partners) {
        Map<String, LinkedHashSet<String>> grouped = new LinkedHashMap<>();
        for (String partner : partners) {
            String key = partner.toLowerCase(Locale.ROOT);
            grouped.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(partner);
        }

        List<CaseInsensitiveGroup> duplicates = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : grouped.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<String> variants = new ArrayList<>(entry.getValue());
                variants.sort(String.CASE_INSENSITIVE_ORDER);
                duplicates.add(new CaseInsensitiveGroup(variants.get(0), variants));
            }
        }
        duplicates.sort(Comparator.comparing(group -> group.getCanonicalLabel(), String.CASE_INSENSITIVE_ORDER));
        return duplicates;
    }

    public static void writeRawList(String filePath, List<String> partners) throws IOException {
        writeLines(filePath, partners, "# One partner label per line (duplicates preserved for Excel analysis)");
    }

    public static void writeExactDuplicateReport(String filePath, List<DuplicateEntry> duplicates) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# Exact duplicate partner labels (same string appears 2+ times in raw collection)");
        if (duplicates.isEmpty()) {
            lines.add("# No exact duplicates found");
        } else {
            for (DuplicateEntry duplicate : duplicates) {
                lines.add(duplicate.getCount() + "x\t" + duplicate.getLabel());
            }
        }
        writeLines(filePath, lines, null);
    }

    public static void writeCaseInsensitiveDuplicateReport(String filePath,
                                                           List<CaseInsensitiveGroup> groups) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# Case-insensitive duplicate groups (different strings, same label when ignoring case)");
        if (groups.isEmpty()) {
            lines.add("# No case-insensitive duplicates found");
        } else {
            for (CaseInsensitiveGroup group : groups) {
                lines.add(String.join(" | ", group.getVariants()));
            }
        }
        writeLines(filePath, lines, null);
    }

    private static void writeLines(String filePath, List<String> lines, String optionalHeader) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create directory for partner export: " + parent.getAbsolutePath());
        }

        try (FileWriter writer = new FileWriter(file)) {
            if (optionalHeader != null) {
                writer.write(optionalHeader);
                writer.write(System.lineSeparator());
            }
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        }
    }

    public static List<String> uniquePartners(List<String> partners) {
        return new ArrayList<>(new LinkedHashSet<>(partners));
    }
}
