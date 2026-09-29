package com.paramount.test.ff.common.util.reporting;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;
import com.synergy.common.SynergyKey;
import com.synergy.core.reporting.AllureReportGenerator;

public final class PassedOnlyAllureReportGenerator {

    private static final String PASSED_ONLY_ROOT = "target" + File.separator + "passed-only-report";
    private static final String PASSED_ONLY_DIR = PASSED_ONLY_ROOT + File.separator + "allure-results";

    private PassedOnlyAllureReportGenerator() {
    }

    public static String generatePassedReportUrl(File allureResultsDir, boolean todayOnly) {
        if (allureResultsDir == null || !allureResultsDir.exists()) {
            return "";
        }

        File outputDir = new File(System.getProperty("user.dir") + File.separator + PASSED_ONLY_DIR);
        prepareOutputDir(outputDir);
        int copied = copyPassedResults(allureResultsDir, outputDir, todayOnly);
        if (copied == 0) {
            Logger.logConsoleMessage("No passed Allure results available to publish.");
            return "";
        }
        Logger.logMessage("Prepared passed-only Allure results: " + copied + " test(s)");

        try {
            AllureReportGenerator generator = new AllureReportGenerator(
                    SynergyKey.fromString(ConfigProps.USER_KEY), ConfigProps.APPLICATION_TITLE, outputDir);
            generator.setBrokenTestsToFailures();
            generator.removePendingTests();
            generator.generateReport();
            String reportUrl = generator.getReportUrl();
            Logger.logMessage("Passed-only Allure report generated: " + reportUrl);
            return reportUrl == null ? "" : reportUrl;
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to generate passed-only Allure report.");
            e.printStackTrace();
            return "";
        }
    }

    private static void prepareOutputDir(File outputDir) {
        if (outputDir.exists()) {
            File[] existing = outputDir.listFiles();
            if (existing != null) {
                for (File file : existing) {
                    if (!file.delete()) {
                        Logger.logConsoleMessage("Failed to delete old passed-only Allure file: " + file.getName());
                    }
                }
            }
        } else {
            outputDir.mkdirs();
        }
    }

    private static int copyPassedResults(File sourceDir, File outputDir, boolean todayOnly) {
        File[] files = sourceDir.listFiles((dir, name) -> name.endsWith("-result.json"));
        if (files == null) {
            return 0;
        }

        int copied = 0;
        JSONParser parser = new JSONParser();
        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JSONObject json = (JSONObject) parser.parse(reader);
                if (!isPassed(json)) {
                    continue;
                }
                if (todayOnly && !isToday(json)) {
                    continue;
                }
                Files.copy(file.toPath(), new File(outputDir, file.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                copied++;
            } catch (Exception e) {
                Logger.logConsoleMessage("Skipped Allure result while building passed-only report: " + file.getName());
            }
        }
        return copied;
    }

    private static boolean isPassed(JSONObject json) {
        return "passed".equalsIgnoreCase(String.valueOf(json.get("status")));
    }

    private static boolean isToday(JSONObject json) {
        Object stop = json.get("stop");
        if (!(stop instanceof Number)) {
            return false;
        }
        LocalDate resultDate = Instant.ofEpochMilli(((Number) stop).longValue())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return resultDate.equals(LocalDate.now());
    }
}
