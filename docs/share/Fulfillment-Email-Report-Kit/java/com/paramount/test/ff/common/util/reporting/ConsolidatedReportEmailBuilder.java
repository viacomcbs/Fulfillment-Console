package com.paramount.test.ff.common.util.reporting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.paramount.test.ff.common.util.TestUtil;

public final class ConsolidatedReportEmailBuilder {

    private ConsolidatedReportEmailBuilder() {
    }

    public static String buildSubject(ConsolidatedReportState state) {
        String envLabel = environmentLabel(state.getEnvironment());
        return state.getApplicationTitle() + " : Consolidated Test Report : " + envLabel + " - Passed ("
                + state.getTotalPassCount() + " tests across " + state.getRunCount() + " runs)";
    }

    public static String buildHtml(ConsolidatedReportState state, String pdfReportUrl) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String executionDate = dateFormat.format(new Date());
        String pdfLink = pdfReportUrl == null || pdfReportUrl.isEmpty()
                ? ""
                : "<a href='" + escapeHtml(pdfReportUrl) + "'>Download hosted PDF summary</a>";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body{font-family:Calibri,Arial,Helvetica,sans-serif;color:#1f2937;margin:0;padding:20px;background:#f4f7f6;}");
        html.append(".container{max-width:1100px;margin:0 auto;background:#fff;border:1px solid #d9e2df;border-radius:8px;overflow:hidden;}");
        html.append(".header{padding:24px 28px;border-bottom:4px solid #0f766e;background:linear-gradient(180deg,#f8fffd 0%,#ffffff 100%);}");
        html.append("h1{margin:0 0 8px 0;font-size:30px;color:#0f172a;}");
        html.append("h2{margin:0 0 16px 0;font-size:18px;color:#334155;font-weight:500;}");
        html.append(".meta{font-size:14px;line-height:1.7;color:#475569;}");
        html.append(".summary-pill{display:inline-block;margin-top:10px;padding:8px 12px;border-radius:20px;background:#ecfdf5;border:1px solid #a7f3d0;font-size:14px;}");
        html.append(".pass{color:#15803d;font-weight:700;}");
        html.append(".section{padding:20px 28px 28px;}");
        html.append("table{width:100%;border-collapse:collapse;font-size:14px;}");
        html.append("th{background:#0f766e;color:#fff;text-align:left;padding:10px 8px;border:1px solid #0b5f58;}");
        html.append("td{padding:9px 8px;border:1px solid #d1d5db;vertical-align:top;}");
        html.append("tr:nth-child(even){background:#f8fafc;}");
        html.append(".status-pass{color:#166534;background:#dcfce7;font-weight:700;text-align:center;}");
        html.append(".footer{padding:0 28px 24px;font-size:13px;color:#64748b;}");
        html.append(".report-links{margin:14px 0;padding:14px 16px;border:1px solid #99f6e4;border-radius:8px;background:#f0fdfa;}");
        html.append(".report-links a{color:#0f766e;font-weight:700;text-decoration:none;}");
        html.append("#passCell{background:#dcfce7;color:#166534;font-weight:700;text-align:center;}");
        html.append(".run-title{margin:18px 0 8px 0;font-size:16px;color:#0f766e;}");
        html.append("</style></head><body><div class='container'>");

        html.append("<div class='header'>");
        html.append("<h1>Consolidated Automation Test Result</h1>");
        html.append("<h2>").append(escapeHtml(state.getApplicationTitle())).append("</h2>");
        html.append("<div class='meta'>");
        html.append("<div><strong>Environment:</strong> ").append(escapeHtml(state.getEnvironment())).append("</div>");
        html.append("<div><strong>Batch ID:</strong> ").append(escapeHtml(state.getBatchId())).append("</div>");
        html.append("<div class='summary-pill'><strong>Total passed test cases:</strong> ")
                .append("<span class='pass'>").append(state.getTotalPassCount()).append("</span></div>");
        html.append("<div style='margin-top:8px;'><strong>Total runs:</strong> ").append(state.getRunCount())
                .append("</div>");
        html.append("<div style='margin-top:8px;'><strong>Total execution time:</strong> ")
                .append(formatDuration(state.getTotalDurationMs())).append("</div>");
        if (!pdfLink.isEmpty()) {
            html.append("<div class='report-links'>");
            html.append("<div><strong>PDF summary (hosted):</strong> ").append(pdfLink).append("</div>");
            html.append("</div>");
        }
        html.append("</div></div>");

        html.append("<div class='section'><h3 style='margin-top:0;'>Run Summary</h3>");
        html.append("<table><thead><tr>");
        html.append("<th>#</th><th>Suite / Run</th><th>Passed</th><th>Duration</th><th>Completed At</th>");
        html.append("</tr></thead><tbody>");
        int runIndex = 1;
        for (ConsolidatedReportRun run : state.getRuns()) {
            html.append("<tr>");
            html.append("<td>").append(runIndex++).append("</td>");
            html.append("<td>").append(escapeHtml(run.getSuiteTitle())).append("</td>");
            html.append("<td class='status-pass'>").append(run.getPassCount()).append("</td>");
            html.append("<td>").append(formatDuration(run.getDurationMs())).append("</td>");
            html.append("<td>").append(escapeHtml(run.getCompletedAt())).append("</td>");
            html.append("</tr>");
        }
        if (state.getRuns().isEmpty()) {
            html.append("<tr><td colspan='5'>No runs accumulated.</td></tr>");
        }
        html.append("</tbody></table></div>");

        html.append("<div class='section'><h3 style='margin-top:0;'>All Passed Test Cases</h3>");
        List<TestResultRecord> flatResults = state.toFlatPassedResults();
        html.append("<table><thead><tr>");
        html.append("<th>#</th><th>Manual ID</th><th>Test Case Name</th><th>Scenario</th><th>Status</th>");
        html.append("</tr></thead><tbody>");
        for (TestResultRecord record : flatResults) {
            html.append("<tr>");
            html.append("<td>").append(record.getIndex()).append("</td>");
            html.append("<td>").append(escapeHtml(record.getManualId())).append("</td>");
            html.append("<td>").append(escapeHtml(record.getTestCaseName())).append("</td>");
            html.append("<td>").append(escapeHtml(record.getScenario())).append("</td>");
            html.append("<td class='status-pass'>PASS</td>");
            html.append("</tr>");
        }
        if (flatResults.isEmpty()) {
            html.append("<tr><td colspan='5'>No passed test cases to report.</td></tr>");
        }
        html.append("</tbody></table></div>");

        html.append("<div class='footer'>");
        html.append("<div><strong>Batch started:</strong> ").append(escapeHtml(state.getCreatedAt())).append("</div>");
        html.append("<div><strong>Report generated:</strong> ").append(executionDate).append("</div>");
        if (TestUtil.isLabExecution()) {
            html.append("<div><strong>Execution mode:</strong> Lab / CI</div>");
        }
        html.append("<div style='margin-top:12px;'><strong>Regards,</strong><br/>Paramount MSC Automation Team</div>");
        html.append("</div></div></body></html>");
        return html.toString();
    }

    private static String environmentLabel(String environment) {
        if ("prod".equalsIgnoreCase(environment)) {
            return "PROD";
        }
        if ("dev".equalsIgnoreCase(environment)) {
            return "DEV System Test";
        }
        if ("uat".equalsIgnoreCase(environment)) {
            return "UAT Automation";
        }
        return environment;
    }

    private static String formatDuration(long durationMs) {
        if (durationMs <= 0) {
            return "N/A";
        }
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        }
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
