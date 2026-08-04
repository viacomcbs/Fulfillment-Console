package com.paramount.test.ff.common.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.synergy.core.reporting.AllureReportGenerator;
import com.synergy.core.reporting.Emailer;
import com.synergy.core.reporting.TeamsMessenger;

public final class ReportEmailBuilder {

    private static final String LINE_BREAK = "<br />";
    private static final String REP_UPLOAD_ERR = "The test report was not uploaded.";
    private static final String INFO_IMG = "https://s3.amazonaws.com/mqeappprodpackages/info.png";

    private String appName = "Unknown brand";
    private String platformName = "Unknown platform";
    private String browserName = "Unknown browser";
    private String enviroment = "Unknown env";
    private final StringBuilder emailBuilder;
    private final StringBuilder notificationBuilderHtml;
    private final StringBuilder notificationBuilderMrkdwn;
    private boolean hasFailures = false;

    public ReportEmailBuilder() {
        emailBuilder = new StringBuilder();
        notificationBuilderHtml = new StringBuilder();
        notificationBuilderMrkdwn = new StringBuilder();
        addMessageIntroPart();
    }

    public ReportEmailBuilder addAppUrl(String appUrl) {
        return addParameterValueLine("App URL", appUrl);
    }

    public ReportEmailBuilder addBranchName(String branchName) {
        return addParameterValueLine("Code Branch", branchName);
    }

    public ReportEmailBuilder addBrandName(String brandNameVal) {
        this.appName = brandNameVal;
        return addParameterValueLine("Application", brandNameVal);
    }

    public ReportEmailBuilder addEnvironment(String envVal) {
        this.enviroment = envVal;
        return addParameterValueLine("Environment", envVal);
    }

    public ReportEmailBuilder addJenkinsReportUrl(String jenkinsReportUrl) {
        StringBuilder msgHtml = new StringBuilder().append(LINE_BREAK).append(Optional.ofNullable(jenkinsReportUrl).map(reportUrl
                -> String.format("A detailed functional report is available: <a href='%s'>Test execution report</a>", reportUrl))
                .orElse(REP_UPLOAD_ERR)).append(LINE_BREAK);
        String msgMrkdwn = Optional.ofNullable(jenkinsReportUrl).map(reportUrl
                        -> String.format("\\n\\nA detailed functional report is available: <%s|report>\\n", reportUrl))
                .orElse(REP_UPLOAD_ERR);
        emailBuilder.append(msgHtml);
        notificationBuilderHtml.append(msgHtml);
        notificationBuilderMrkdwn.append(msgMrkdwn);
        return this;
    }

    public ReportEmailBuilder addPlatformName(String platformNameVal) {
        this.platformName = platformNameVal;
        return addParameterValueLine("Platform", platformNameVal);
    }
    public ReportEmailBuilder addBrowserName(String browserNameVal) {
        this.browserName = browserNameVal;
        return addParameterValueLine("Browser", browserNameVal);
    }

    public ReportEmailBuilder addTestResults(AllureReportGenerator allureReportGenerator) {
        return addTestResults(allureReportGenerator.getPassedTestCount(), allureReportGenerator.getSkippedTestCount(),
                allureReportGenerator.getFailedTestCount());
    }

    public ReportEmailBuilder addTestResults(int passed, int retried, int failed) {
        if (failed > 0) {
            hasFailures = true;
        }
        StringBuilder msgHtml = new StringBuilder().append("Tests Passed:<strong><a style=\"color:green\"> ").append(passed)
                .append("</a></strong>").append(LINE_BREAK).append("Tests Retried:<strong><a style=\"color:orange\"> ")
                .append(retried).append("</a></strong>").append(LINE_BREAK)
                .append("Tests Failed: <strong><a style=\"color:red\"> ").append(failed).append("</a></strong>");
        String msgMrkdwn = "\\n :green_book: Tests Passed: " + passed +
                " :orange_book: Tests Retried: " + retried + " :closed_book: Tests Failed: " + failed;
        emailBuilder.append(LINE_BREAK).append(msgHtml).append(LINE_BREAK);
        notificationBuilderHtml.append(LINE_BREAK).append(msgHtml.toString().replaceAll(LINE_BREAK, ", ")).append(LINE_BREAK);
        notificationBuilderMrkdwn.append(msgMrkdwn);
        return this;
    }

    public void sendEmail(String senderEmailAddress, String sendReportEmailAddress, String synergyURL) {
        Emailer emailer = new Emailer(synergyURL);
        String emailSubject = generateSubject();
        try {
            emailer.sendEmail(senderEmailAddress, sendReportEmailAddress, emailSubject, emailBuilder.toString());
        } catch (Exception e) {
            Logger.logMessage("Error: Failed to Send Email --"+e);
            Logger.logConsoleMessage("senderEmailAddress: " + senderEmailAddress);
            Logger.logConsoleMessage("sendReportEmailAddress: " + sendReportEmailAddress);
            Logger.logConsoleMessage("emailSubject: " + emailSubject);
            Logger.logConsoleMessage("Message: " + emailBuilder.toString());
        }
    }

    public void sendSlackReportsMessage(String teamsWebHookURL, String synergyURL, String channel ) {
        if (TestUtil.isLabExecution()) {
            System.out.println("Slack Update turned on");
            String title = generateSubject();
            String msgHtml = notificationBuilderHtml.toString().replaceAll("\"", "'");
            String msgMrkdwn = notificationBuilderMrkdwn.toString();
            try {
                TeamsMessenger teamsMessenger = new TeamsMessenger(synergyURL, teamsWebHookURL);
                teamsMessenger.sendMessage(title, msgHtml, INFO_IMG);

            } catch (Exception e) {
                Logger.logMessage("Error: Failed to Send MS Teams Message"+e);
                Logger.logConsoleMessage("webHookURL: " + teamsWebHookURL);
                Logger.logConsoleMessage("Message: " + msgHtml);
            }
            // Slack do not require error handling as its already done in SlackMessenger
           // SlackMessenger.sendMessageReports(title, msgMrkdwn, channel);
        }
        else {
            System.out.println("Slack Update turned off");
        }
    }
    public void sendSlackAlertsMessage(String teamsWebHookURL, String synergyURL, String channel ) {
        if (TestUtil.isLabExecution()){
            System.out.println("Slack Update turned on");
        String title = generateSubject();
        String msgHtml = notificationBuilderHtml.toString().replaceAll("\"", "'");
        String msgMrkdwn = notificationBuilderMrkdwn.toString();
        try {
            TeamsMessenger teamsMessenger = new TeamsMessenger(synergyURL, teamsWebHookURL);
            teamsMessenger.sendMessage(title, msgHtml, INFO_IMG);

        } catch (Exception e) {
            Logger.logMessage("Error: Failed to Send MS Teams Message"+e);
            Logger.logConsoleMessage("webHookURL: " + teamsWebHookURL);
            Logger.logConsoleMessage("Message: " + msgHtml);
        }
        // Slack do not require error handling as its already done in SlackMessenger
        //SlackMessenger.sendMessageReports(title, msgMrkdwn, channel);
    }
     else {
        System.out.println("Slack Update turned off");
    }
    }

    private ReportEmailBuilder addIssuesMessage(Map<String, String> issues) {
        Set<String> resolvedSet = new HashSet<>(issues.values());
        resolvedSet.forEach(issueLink -> emailBuilder.append(LINE_BREAK).append(issueLink));
        emailBuilder.append(LINE_BREAK);
        return this;
    }

    private ReportEmailBuilder addMessageIntroPart() {
        emailBuilder.append("<body><b>Lab Execution Completed</b>").append(LINE_BREAK).append(LINE_BREAK);
        notificationBuilderHtml.append("Lab Execution Completed").append(LINE_BREAK);
        notificationBuilderMrkdwn.append(" \\nTest Run completed. ");
        return this;
    }

    public ReportEmailBuilder addMessageEndPart() {
        emailBuilder.append(LINE_BREAK).append("Regards").append(LINE_BREAK);
        emailBuilder.append("MIP Automation Team");
        return this;
    }

    private ReportEmailBuilder addParameterValueLine(String parameter, String value) {
        emailBuilder.append(parameter).append(": ").append(value).append(LINE_BREAK);
        if (parameter.equals("Test Groups") || parameter.equals("Code Branch")) {
            notificationBuilderHtml.append(parameter).append(": <strong>").append(value).append("</strong> ");
            notificationBuilderMrkdwn.append(parameter).append(": *").append(value).append("* ");
        }
        return this;
    }

    private String generateSubject() {
        return String.format("Automation Tests Result - %s - %s (%s/%s)", appName,
                 hasFailures ? "Failed" : "Succeed",platformName,browserName);
    }
}
