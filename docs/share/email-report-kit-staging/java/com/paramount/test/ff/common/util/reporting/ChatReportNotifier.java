package com.paramount.test.ff.common.util.reporting;

import java.util.List;

import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SlackMessenger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;

public final class ChatReportNotifier {

    private static final String ICON_SUCCESS = "https://s3.amazonaws.com/mqeappprodpackages/success.png";

    private ChatReportNotifier() {
    }

    public static void sendExecutionNotifications(String subject, String applicationTitle, String suiteTitle,
            String environment, int passCount, long totalDurationMs, String synergyReportUrl, String pdfReportUrl,
            String screenRecordingUrl, List<TestResultRecord> passedResults) {
        if (!shouldSendChatReport()) {
            return;
        }

        String synergyServerUrl = buildSynergyServerUrl();
        String slackWebhook = Config.getString("SlackWebHookUrl");
        String slackChannel = resolveSlackChannel();
        String slackMessage = buildSlackMessage(subject, applicationTitle, suiteTitle, environment, passCount,
                totalDurationMs, synergyReportUrl, pdfReportUrl, screenRecordingUrl, passedResults);

        sendSlackMessage(synergyServerUrl, slackWebhook, slackChannel, slackMessage, ICON_SUCCESS);
    }

    public static void sendConsolidatedExecutionNotifications(String subject, ConsolidatedReportState state,
            String pdfReportUrl) {
        if (!shouldSendChatReport()) {
            return;
        }

        String synergyServerUrl = buildSynergyServerUrl();
        String slackWebhook = Config.getString("SlackWebHookUrl");
        String slackChannel = resolveSlackChannel();
        String slackMessage = buildConsolidatedSlackMessage(subject, state, pdfReportUrl);
        sendSlackMessage(synergyServerUrl, slackWebhook, slackChannel, slackMessage, ICON_SUCCESS);
    }

    private static boolean shouldSendChatReport() {
        return ConfigProps.SEND_REPORT_CHAT || "true".equalsIgnoreCase(Config.getString("SendChatReport"));
    }

    private static String resolveSlackChannel() {
        String channelId = Config.getString("SlackChannelId");
        if (channelId != null && !channelId.trim().isEmpty()) {
            return channelId.trim();
        }
        String channel = Config.getString("SlackChannel");
        return channel == null ? "" : channel.trim();
    }

    private static void sendSlackMessage(String synergyServerUrl, String webhookUrl, String channel,
            String slackMessage, String icon) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            Logger.logConsoleMessage("Slack notification skipped: SlackWebHookUrl is not configured.");
            return;
        }
        if (channel == null || channel.trim().isEmpty()) {
            Logger.logConsoleMessage("Slack notification skipped: SlackChannel is not configured.");
            return;
        }
        try {
            SlackMessenger slackMessenger = new SlackMessenger(synergyServerUrl, webhookUrl);
            slackMessenger.sendMessage(channel, slackMessage, icon);
            Logger.logMessage("Slack execution notification sent to " + channel);
        } catch (Exception e) {
            Logger.logMessage("Failed to send Slack notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String buildSlackMessage(String subject, String applicationTitle, String suiteTitle,
            String environment, int passCount, long totalDurationMs, String synergyReportUrl, String pdfReportUrl,
            String screenRecordingUrl, List<TestResultRecord> passedResults) {
        StringBuilder message = new StringBuilder();
        message.append("*").append(subject).append("*\n");
        message.append("*").append(applicationTitle).append("*\n");
        message.append(suiteTitle).append("\n");
        message.append("Environment: *").append(environment).append("*\n");
        message.append(":green_book: Passed: ").append(passCount).append("\n");
        message.append("Execution time: ").append(formatDuration(totalDurationMs)).append("\n");

        if (synergyReportUrl != null && !synergyReportUrl.isEmpty()) {
            message.append("Allure report (passed): <").append(synergyReportUrl).append("|Open Allure report>\n");
        }
        if (pdfReportUrl != null && !pdfReportUrl.isEmpty()) {
            message.append("PDF report: <").append(pdfReportUrl).append("|Download PDF>\n");
        }
        if (screenRecordingUrl != null && !screenRecordingUrl.isEmpty()) {
            message.append("Recording: <").append(screenRecordingUrl).append("|View recording>\n");
        }

        message.append("\n*Passed tests*\n");
        if (passedResults == null || passedResults.isEmpty()) {
            message.append("None\n");
        } else {
            for (TestResultRecord record : passedResults) {
                message.append("- ").append(record.getManualId()).append(" | ")
                        .append(record.getTestCaseName()).append(" | ").append(record.getScenario())
                        .append("\n");
            }
        }
        return message.toString();
    }

    private static String buildConsolidatedSlackMessage(String subject, ConsolidatedReportState state,
            String pdfReportUrl) {
        StringBuilder message = new StringBuilder();
        message.append("*").append(subject).append("*\n");
        message.append("*").append(state.getApplicationTitle()).append("*\n");
        message.append("Environment: *").append(state.getEnvironment()).append("*\n");
        message.append("Batch: *").append(state.getBatchId()).append("*\n");
        message.append(":green_book: Total passed: ").append(state.getTotalPassCount()).append("\n");
        message.append("Runs: ").append(state.getRunCount()).append("\n");
        message.append("Execution time: ").append(formatDuration(state.getTotalDurationMs())).append("\n");

        if (pdfReportUrl != null && !pdfReportUrl.isEmpty()) {
            message.append("PDF report: <").append(pdfReportUrl).append("|Download PDF>\n");
        }

        message.append("\n*Run summary*\n");
        int runIndex = 1;
        for (ConsolidatedReportRun run : state.getRuns()) {
            message.append(runIndex++).append(". ").append(run.getSuiteTitle()).append(" - ")
                    .append(run.getPassCount()).append(" passed\n");
        }

        message.append("\n*Passed tests*\n");
        List<TestResultRecord> flatResults = state.toFlatPassedResults();
        if (flatResults.isEmpty()) {
            message.append("None\n");
        } else {
            for (TestResultRecord record : flatResults) {
                message.append("- ").append(record.getManualId()).append(" | ")
                        .append(record.getTestCaseName()).append(" | ").append(record.getScenario())
                        .append("\n");
            }
        }
        return message.toString();
    }

    private static String buildSynergyServerUrl() {
        return ConfigProps.LAB_URL + "?key=" + ConfigProps.USER_KEY;
    }

    private static String formatDuration(long durationMs) {
        if (durationMs <= 0) {
            return "N/A";
        }
        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
