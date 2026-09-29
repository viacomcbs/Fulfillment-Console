package com.paramount.test.ff.common.util.reporting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class ExecutionReportEmailBuilder {

    /** Fixed report header bar — do not vary by pass/fail/skip. */
    private static final String REPORT_HEADER_COLOR = "#A93226";

    private String applicationTitle = "Automation";
    private String suiteTitle = "Test Execution";
    private String environment = "UNKNOWN";
    private String synergyReportUrl = "";
    private String pdfReportUrl = "";
    private String screenRecordingNote = "";
    private String testerName = "Automation Bot";
    /** When set, used as img src (hosted URL or cid:…). Falls back to classpath data URI. */
    private String logoSrc = "";
    private int passCount;
    private int failCount;
    private int skipCount;
    private long totalDurationMs;
    private List<TestResultRecord> testResults = java.util.Collections.emptyList();

    public ExecutionReportEmailBuilder applicationTitle(String value) {
        this.applicationTitle = value == null || value.isBlank() ? "Automation" : value.trim();
        return this;
    }

    public ExecutionReportEmailBuilder suiteTitle(String value) {
        this.suiteTitle = value == null ? "Test Execution" : value;
        return this;
    }

    public ExecutionReportEmailBuilder environment(String value) {
        this.environment = value == null ? "UNKNOWN" : value;
        return this;
    }

    public ExecutionReportEmailBuilder synergyReportUrl(String value) {
        this.synergyReportUrl = value == null ? "" : value;
        return this;
    }

    public ExecutionReportEmailBuilder pdfReportUrl(String value) {
        this.pdfReportUrl = value == null ? "" : value;
        return this;
    }

    public ExecutionReportEmailBuilder screenRecordingLink(String value) {
        if (value != null && !value.trim().isEmpty()) {
            this.screenRecordingNote = value.trim();
        }
        return this;
    }

    public ExecutionReportEmailBuilder testerName(String value) {
        if (value != null && !value.trim().isEmpty()) {
            this.testerName = value.trim();
        }
        return this;
    }

    public ExecutionReportEmailBuilder logoSrc(String value) {
        this.logoSrc = value == null ? "" : value.trim();
        return this;
    }

    public ExecutionReportEmailBuilder passCount(int passCount) {
        this.passCount = Math.max(0, passCount);
        return this;
    }

    public ExecutionReportEmailBuilder failCount(int failCount) {
        this.failCount = Math.max(0, failCount);
        return this;
    }

    public ExecutionReportEmailBuilder skipCount(int skipCount) {
        this.skipCount = Math.max(0, skipCount);
        return this;
    }

    public ExecutionReportEmailBuilder totalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
        return this;
    }

    public ExecutionReportEmailBuilder testResults(List<TestResultRecord> testResults) {
        this.testResults = testResults == null ? java.util.Collections.emptyList() : testResults;
        return this;
    }

    public String buildSubject() {
        String envLabel = environmentLabel();
        String outcome = failCount > 0 ? "Failed" : "Passed";
        return applicationTitle + " — Automation Report: " + envLabel + " - " + outcome;
    }

    public String buildHtml() {
        int totalCount = passCount + failCount + skipCount;
        if (totalCount == 0 && !testResults.isEmpty()) {
            totalCount = testResults.size();
            passCount = countByStatus("PASS");
            failCount = countByStatus("FAIL") + countByStatus("ERROR") + countByStatus("FAILED");
            skipCount = countByStatus("SKIP") + countByStatus("SKIPPED");
        }

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss");
        String executionDate = dateFormat.format(new Date());
        int passRate = totalCount > 0 ? Math.round((passCount * 100f) / totalCount) : 0;

        String headerColor = REPORT_HEADER_COLOR;
        String passRateColor = failCount > 0 ? "#A93226" : (skipCount > 0 ? "#92400E" : "#0F5132");
        String detailReportUrl = resolveDetailReportUrl();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<title>Automation Report</title></head>");
        html.append("<body style='margin:0;padding:0;background:#eef0f3;font-family:Arial,Helvetica,sans-serif;'>");
        html.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0' ");
        html.append("style='width:100%;background:#eef0f3;'><tr><td style='padding:12px 16px;'>");
        html.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0' ");
        html.append("style='width:100%;max-width:100%;background:#ffffff;border-radius:10px;overflow:hidden;"
                + "box-shadow:0 1px 3px rgba(0,0,0,0.12);'>");

        appendHeader(html, headerColor);
        appendExecutionSummaryRow(html, executionDate, passRate, passRateColor);
        appendKpiCards(html, totalCount, passCount, failCount, skipCount);
        appendReportButton(html, detailReportUrl);

        html.append("</table></td></tr></table></body></html>");
        return html.toString();
    }

    private void appendHeader(StringBuilder html, String headerColor) {
        html.append("<tr><td style='background:").append(headerColor).append(";");
        html.append("padding:16px 28px 16px 10px;text-align:left;'>");
        html.append("<div style='font-family:Arial,Helvetica,sans-serif;color:#ffffff;font-size:18px;");
        html.append("font-weight:bold;line-height:1.25;text-align:left;mso-line-height-rule:exactly;");
        html.append("margin:0;padding:0;'>");
        html.append(escapeHtml(applicationTitle)).append(" - Automation Report</div>");
        html.append("</td></tr>");
    }

    private void appendExecutionSummaryRow(StringBuilder html, String executionDate, int passRate,
            String passRateColor) {
        html.append("<tr><td style='padding:24px 28px 6px 28px;'>");
        html.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
        html.append("<td>");
        html.append("<div style='font-family:Arial,sans-serif;font-size:13px;font-weight:bold;color:#1b1b1b;'>");
        html.append("Execution Summary</div>");
        html.append("<div style='font-family:Arial,sans-serif;font-size:13px;color:#1b1b1b;margin-top:4px;'>");
        html.append("Environment: ").append(escapeHtml(environmentLabel())).append(" &middot; ");
        html.append(escapeHtml(executionDate)).append("</div></td>");
        html.append("<td align='right' valign='bottom'>");
        html.append("<div style='font-family:Arial,sans-serif;font-size:28px;font-weight:bold;color:");
        html.append(passRateColor).append(";line-height:1;'>").append(passRate).append("%</div>");
        html.append("<div style='font-family:Arial,sans-serif;font-size:11px;color:#999;letter-spacing:.3px;"
                + "margin-top:2px;'>PASS RATE</div></td>");
        html.append("</tr></table></td></tr>");
    }

    private void appendKpiCards(StringBuilder html, int total, int passed, int failed, int skipped) {
        html.append("<tr><td style='background:#FBEDEC;padding:14px 20px 22px 20px;'>");
        html.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
        appendKpiCard(html, "TOTAL", "#2E86DE", "#", String.valueOf(total));
        appendKpiCard(html, "PASSED", "#219653", "&#10003;", String.valueOf(passed));
        appendKpiCard(html, "FAILED", "#E74C3C", "&#10005;", String.valueOf(failed));
        appendKpiCard(html, "SKIPPED", "#E67E22", "&#187;", String.valueOf(skipped));
        html.append("</tr></table></td></tr>");
    }

    private void appendKpiCard(StringBuilder html, String label, String color, String icon, String value) {
        html.append("<td width='25%' valign='top' style='padding:0 6px;'>");
        html.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0' ");
        html.append("style='background:").append(color).append(";border-radius:6px;'><tr>");
        html.append("<td style='padding:14px;'><table role='presentation' width='100%' cellpadding='0' ");
        html.append("cellspacing='0'><tr><td style='font-family:Arial,sans-serif;font-size:13px;font-weight:bold;"
                + "letter-spacing:.3px;color:#1b1b1b;'>").append(label).append("</td><td align='right'>");
        html.append("<table role='presentation' cellpadding='0' cellspacing='0' width='24' height='24' ");
        html.append("style='background:rgba(255,255,255,0.85);border-radius:6px;'><tr><td align='center' ");
        html.append("valign='middle' style='font-size:13px;font-weight:bold;color:").append(color);
        html.append(";font-family:Arial,sans-serif;'>").append(icon).append("</td></tr></table></td></tr>");
        html.append("</table><div style='font-family:Arial,sans-serif;font-size:26px;font-weight:bold;color:#1b1b1b;"
                + "margin-top:2px;'>").append(value).append("</div></td></tr></table></td>");
    }

    private void appendReportButton(StringBuilder html, String detailReportUrl) {
        html.append("<tr><td style='padding:0 28px 30px 28px;'>");
        if (detailReportUrl.isEmpty()) {
            html.append("<div style='text-align:center;background:#e5e7eb;color:#6b7280;font-family:Arial,sans-serif;"
                    + "font-size:14px;font-weight:bold;padding:13px 0;border-radius:8px;'>");
            html.append("Detailed report not available</div>");
        } else {
            html.append("<a href='").append(escapeHtmlAttr(detailReportUrl)).append("' ");
            html.append("style='display:block;text-align:center;background:#1b1b1b;color:#ffffff;"
                    + "font-family:Arial,sans-serif;font-size:14px;font-weight:bold;text-decoration:none;"
                    + "padding:13px 0;border-radius:8px;'>View Detailed Report &rarr;</a>");
        }
        html.append("</td></tr>");
    }

    private String resolveDetailReportUrl() {
        if (pdfReportUrl != null && !pdfReportUrl.isBlank()) {
            return pdfReportUrl.trim();
        }
        if (synergyReportUrl != null && !synergyReportUrl.isBlank()) {
            return synergyReportUrl.trim();
        }
        return "";
    }

    private int countByStatus(String status) {
        int count = 0;
        for (TestResultRecord record : testResults) {
            if (record != null && status.equalsIgnoreCase(normalizeStatus(record.getStatus()))) {
                count++;
            }
        }
        return count;
    }

    private static String normalizeStatus(String status) {
        if (status == null) {
            return "UNKNOWN";
        }
        String upper = status.trim().toUpperCase();
        if ("FAILED".equals(upper) || "BROKEN".equals(upper)) {
            return "FAIL";
        }
        if ("SKIPPED".equals(upper)) {
            return "SKIP";
        }
        if ("PASSED".equals(upper)) {
            return "PASS";
        }
        return upper;
    }

    private String environmentLabel() {
        if ("prod".equalsIgnoreCase(environment)) {
            return "PROD";
        }
        if ("dev".equalsIgnoreCase(environment)) {
            return "DEV";
        }
        if ("uat".equalsIgnoreCase(environment)) {
            return "UAT";
        }
        return environment;
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String escapeHtmlAttr(String value) {
        return escapeHtml(value).replace("'", "&#39;");
    }

}
