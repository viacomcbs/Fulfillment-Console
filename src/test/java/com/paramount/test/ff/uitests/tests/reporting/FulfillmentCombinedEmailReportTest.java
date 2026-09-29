package com.paramount.test.ff.uitests.tests.reporting;

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
 * Sends one consolidated scenario email after split LF + table-view session runs
 * ({@code scripts/run-lf-tableview-split-consolidated-email-prod.ps1}).
 */
public class FulfillmentCombinedEmailReportTest {

    @Test
    public void sendConsolidatedRegressionEmail() {
        String title = Config.getString("LeftFilterEmailSuiteTitle");
        LeftFilterEmailReport.enable(title != null && !title.isBlank() ? title
                : "Fulfillment Console - Orders LF + Table View Regression (PROD)");

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

        int total = LeftFilterEmailReport.getTotalCount();
        Assert.assertTrue(total > 0, "No scenarios merged for consolidated email");
        Logger.logConsoleMessage("Consolidated report scenarios merged: " + total);

        int pass = LeftFilterEmailReport.getPassedCount();
        int fail = LeftFilterEmailReport.getFailedCount();
        int skip = LeftFilterEmailReport.getSkippedCount();

        String recipient = Config.getString("SendReportEmailAddress");
        Assert.assertFalse(recipient == null || recipient.isBlank(), "SendReportEmailAddress must be set");

        boolean passedOnly = Config.getBoolean("PassedOnlyConsolidatedEmail");
        Logger.logConsoleMessage("Sending consolidated email to " + recipient
                + (passedOnly ? " (passed scenarios only)" : " (full pass/fail status)"));
        EmailUtil.sendResultEmail("", pass, fail, skip, 0, recipient, passedOnly);

        String internalEmail = Config.getString("SendReportInternalEmailAddress");
        if (internalEmail != null && !internalEmail.isBlank() && Config.getBoolean("SendInternalConsolidatedEmail")) {
            Logger.logConsoleMessage("Sending internal consolidated email to " + internalEmail);
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
            Logger.logConsoleMessage("Slack webhook not set - skipping Slack API post.");
            return;
        }
        String channel = firstNonBlank(Config.getString("SlackChannel"), System.getenv("SLACK_CHANNEL"));
        Assert.assertFalse(channel.isBlank(), "SlackChannel must be set when SendChatReport=true");
        String title = Config.getString("LeftFilterEmailSuiteTitle");
        if (title == null || title.isBlank()) {
            title = "Fulfillment Console - Orders LF + Table View Regression (PROD)";
        }
        String env = Config.getString("TestEnvironment");
        String icon = fail > 0 ? "https://s3.amazonaws.com/mqeappprodpackages/failure.png"
                : "https://s3.amazonaws.com/mqeappprodpackages/success.png";
        String message = String.format("*%s* (%s)\nPass: %d | Total: %d", title, env, pass, pass + fail + skip);
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
