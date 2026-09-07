package com.paramount.test.ff.common.listeners;

import com.paramount.test.ff.common.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Records per-filter left-filter test results to persistent history and refreshes the Excel tracker.
 * Registered on {@code LeftFilterOrdersTabBaseTest} / {@code LeftFilterLineItemsTabBaseTest} so it
 * works for Maven, IntelliJ, and any TestNG suite XML run.
 */
public class PerFilterTestTrackerListener implements ISuiteListener, ITestListener {

    private static final String HISTORY_FILE = "docs/test-tracking/perfilter_execution_history.json";
    private static final String UPDATE_SCRIPT = "scripts/update_perfilter_test_tracker.py";

    @Override
    public void onStart(ISuite suite) {
        // no-op
    }

    @Override
    public void onFinish(ISuite suite) {
        if (!suiteContainsPerFilterTests(suite)) {
            return;
        }
        refreshExcelTracker();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        recordResult(result, "Pass");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        recordResult(result, "Fail");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        recordResult(result, "Skipped");
    }

    private static boolean suiteContainsPerFilterTests(ISuite suite) {
        return suite.getAllMethods().stream()
                .anyMatch(method -> method.getRealClass().getName().contains(".perfilter."));
    }

    private static void recordResult(ITestResult result, String status) {
        String className = result.getTestClass().getName();
        if (!className.contains(".perfilter.")) {
            return;
        }
        if (className.endsWith("LeftFilterOrdersTabBaseTest")
                || className.endsWith("LeftFilterLineItemsTabBaseTest")) {
            return;
        }
        if (result.getMethod().isBeforeMethodConfiguration()
                || result.getMethod().isAfterMethodConfiguration()
                || result.getMethod().isBeforeClassConfiguration()
                || result.getMethod().isAfterClassConfiguration()) {
            return;
        }

        try {
            File projectRoot = findProjectRoot();
            File historyFile = new File(projectRoot, HISTORY_FILE);
            historyFile.getParentFile().mkdirs();

            JSONObject root = loadHistory(historyFile);
            JSONArray runs = (JSONArray) root.get("runs");
            if (runs == null) {
                runs = new JSONArray();
                root.put("runs", runs);
            }

            JSONObject entry = new JSONObject();
            entry.put("testClass", result.getTestClass().getRealClass().getSimpleName());
            entry.put("testMethod", result.getMethod().getMethodName());
            entry.put("status", status);
            entry.put("environment", inferEnvironment(result));
            entry.put("suiteName", result.getTestContext().getSuite().getName());
            entry.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
            entry.put("failure", status.equals("Fail") ? truncate(result.getThrowable() != null
                    ? result.getThrowable().getMessage() : "", 300) : "");
            entry.put("source", "testng-listener");
            runs.add(entry);

            try (FileWriter writer = new FileWriter(historyFile, StandardCharsets.UTF_8)) {
                writer.write(root.toJSONString());
            }
        } catch (Exception e) {
            Logger.logReportMessage("PerFilterTestTrackerListener: failed to record result — " + e.getMessage());
        }
    }

    private static JSONObject loadHistory(File historyFile) throws Exception {
        if (!historyFile.exists()) {
            JSONObject root = new JSONObject();
            root.put("runs", new JSONArray());
            return root;
        }
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(historyFile, StandardCharsets.UTF_8)) {
            Object parsed = parser.parse(reader);
            if (parsed instanceof JSONObject) {
                return (JSONObject) parsed;
            }
        }
        JSONObject root = new JSONObject();
        root.put("runs", new JSONArray());
        return root;
    }

    private static String inferEnvironment(ITestResult result) {
        ITestContext context = result.getTestContext();
        String env = context.getSuite().getParameter("TestEnvironment");
        if (env != null && !env.isEmpty()) {
            return env.toUpperCase();
        }
        String suiteName = context.getSuite().getName().toUpperCase();
        if (suiteName.contains("PROD")) return "PROD";
        if (suiteName.contains("UAT")) return "UAT";
        if (suiteName.contains("LOCAL")) return "LOCAL";
        if (suiteName.contains("DEV")) return "DEV";
        return "UNKNOWN";
    }

    private static String truncate(String value, int max) {
        if (value == null) return "";
        String cleaned = value.replaceAll("\\s+", " ").trim();
        return cleaned.length() <= max ? cleaned : cleaned.substring(0, max);
    }

    private static File findProjectRoot() {
        File dir = new File(System.getProperty("user.dir"));
        while (dir != null) {
            if (new File(dir, "pom.xml").exists() && new File(dir, "scripts/update_perfilter_test_tracker.py").exists()) {
                return dir;
            }
            dir = dir.getParentFile();
        }
        return new File(System.getProperty("user.dir"));
    }

    private static void refreshExcelTracker() {
        try {
            File projectRoot = findProjectRoot();
            File script = new File(projectRoot, UPDATE_SCRIPT);
            if (!script.exists()) {
                Logger.logReportMessage("PerFilterTestTrackerListener: tracker script not found at " + script.getAbsolutePath());
                return;
            }
            ProcessBuilder builder = new ProcessBuilder("python", script.getAbsolutePath());
            builder.directory(projectRoot);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            int exitCode = process.waitFor();
            Logger.logReportMessage("PerFilterTestTrackerListener: Excel tracker refresh exit code " + exitCode);
        } catch (Exception e) {
            Logger.logReportMessage("PerFilterTestTrackerListener: Excel refresh failed — " + e.getMessage());
        }
    }
}
