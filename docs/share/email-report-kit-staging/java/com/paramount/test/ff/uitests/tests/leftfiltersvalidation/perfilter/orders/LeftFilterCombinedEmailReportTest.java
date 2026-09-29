package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders;

import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.EmailUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SlackMessenger;
import com.paramount.test.ff.common.util.TestUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterEmailReport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Sends one consolidated email (+ optional Slack) after
 * {@code scripts/run-orders-left-filter-passed-prod.ps1} copies session result files.
 */
public class LeftFilterCombinedEmailReportTest {

    @Test
    public void sendCombinedPassedOrdersEmail() {
        String title = Config.getString("LeftFilterEmailSuiteTitle");
        LeftFilterEmailReport.enable(title != null && !title.isBlank() ? title
                : "Orders Tab — Left Filters Regression (PROD)");

        String dirPath = Config.getString("AggregateResultsDir");
        File dir = new File(System.getProperty("user.dir"), dirPath.replace("/", File.separator));
        Assert.assertTrue(dir.isDirectory(), "Aggregate results dir missing: " + dir.getAbsolutePath());

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".xml"));
        Assert.assertNotNull(files);
        Assert.assertTrue(files.length >= 1, "Expected session result files in " + dir.getAbsolutePath());

        for (File file : files) {
            LeftFilterEmailReport.mergeFromTestNgResultsFile(file);
        }
        LeftFilterEmailReport.removeHarnessResults();
        LeftFilterEmailReport.finalizeConsolidatedResults();
        int merged = LeftFilterEmailReport.getTotalCount();
        Logger.logConsoleMessage("Consolidated email merged " + merged + " scenario(s) from " + files.length
                + " file(s) in " + dir.getAbsolutePath());
        Assert.assertTrue(merged > 0, "No scenarios merged for consolidated email");

        int pass = LeftFilterEmailReport.getPassedCount();
        int fail = LeftFilterEmailReport.getFailedCount();
        int skip = LeftFilterEmailReport.getSkippedCount();

        String stakeholderEmail = Config.getString("SendReportEmailAddress");
        Assert.assertFalse(stakeholderEmail == null || stakeholderEmail.isBlank(),
                "SendReportEmailAddress must be set for stakeholder email");

        Logger.logConsoleMessage("Sending stakeholder email (passed scenarios only) to " + stakeholderEmail);
        EmailUtil.sendResultEmail("", pass, fail, skip, 0, stakeholderEmail, true);

        String internalEmail = Config.getString("SendReportInternalEmailAddress");
        if (internalEmail != null && !internalEmail.isBlank()) {
            Logger.logConsoleMessage("Sending internal email (full pass/fail status) to " + internalEmail);
            EmailUtil.sendResultEmail("", pass, fail, skip, 0, internalEmail, false);
        }

        sendSlackSummary(pass, fail, skip);
    }

    private static void sendSlackSummary(int pass, int fail, int skip) {
        if (!Config.getBoolean("SendChatReport")) {
            return;
        }
        String webhook = firstNonBlank(Config.getString("SlackWebhookUrl"), System.getenv("SLACK_WEBHOOK_URL"));
        if (webhook.isBlank()) {
            Logger.logConsoleMessage(
                    "Slack webhook not set — skipping Slack API post (Slack channel may still receive email copy).");
            return;
        }
        String channel = firstNonBlank(Config.getString("SlackChannel"), System.getenv("SLACK_CHANNEL"));
        Assert.assertFalse(channel.isBlank(), "SlackChannel must be set when SendChatReport=true");
        String title = Config.getString("LeftFilterEmailSuiteTitle");
        if (title == null || title.isBlank()) {
            title = "Orders Tab — Left Filters Regression (PROD)";
        }
        String env = Config.getString("TestEnvironment");
        int slackFail = 0;
        String icon = "https://s3.amazonaws.com/mqeappprodpackages/success.png";
        String message = String.format("*%s* (%s)\nPass: %d", title, env, pass);
        if (fail > 0) {
            Logger.logConsoleMessage(
                    "Slack summary uses passed-only counts (failures omitted from stakeholder channel).");
        }
        if (slackFail > 0) {
            icon = "https://s3.amazonaws.com/mqeappprodpackages/failure.png";
            message = String.format("*%s* (%s)\nPass: %d | Fail: %d", title, env, pass, slackFail);
        }
        try {
            new SlackMessenger(TestUtil.getServerUrl(), webhook).sendMessage(channel, message, icon);
            Logger.logConsoleMessage("Slack report sent to " + channel);
        } catch (Exception e) {
            Logger.logConsoleMessage("Slack report failed: " + e.getMessage());
        }
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return "";
    }
}
