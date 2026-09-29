package com.paramount.test.ff.common.util.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.util.Optional;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.paramount.test.ff.common.util.Logger;

public final class ExecutionReportPdfGenerator {

    private static final BaseColor TEAL = new BaseColor(15, 118, 110);
    private static final BaseColor PASS_BG = new BaseColor(220, 252, 231);
    private static final BaseColor PASS_TEXT = new BaseColor(22, 101, 52);
    private static final BaseColor FAIL_BG = new BaseColor(254, 226, 226);
    private static final BaseColor FAIL_TEXT = new BaseColor(153, 27, 27);
    private static final BaseColor SKIP_BG = new BaseColor(254, 243, 199);
    private static final BaseColor SKIP_TEXT = new BaseColor(146, 64, 14);
    private static final BaseColor BORDER = new BaseColor(209, 213, 219);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.WHITE);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, new BaseColor(236, 253, 245));
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TEAL);
    private static final Font META_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
    private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);

    private ExecutionReportPdfGenerator() {
    }

    public static File generate(String applicationTitle, String suiteTitle, String environment, int passCount,
            int failCount, int skipCount, long totalDurationMs, String testerName,
            List<TestResultRecord> testResults, File outputDir) {
        if (outputDir == null) {
            outputDir = new File(System.getProperty("user.dir") + File.separator + "test-output" + File.separator
                    + "reports");
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String safeApp = applicationTitle.replaceAll("[^A-Za-z0-9_-]", "_");
        File pdfFile = new File(outputDir, safeApp + "_Execution_Report_" + timestamp + ".pdf");

        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try (FileOutputStream outputStream = new FileOutputStream(pdfFile)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(buildHeaderBanner(applicationTitle, suiteTitle));
            document.add(buildMetaPanel(applicationTitle, environment, passCount, failCount, skipCount,
                    totalDurationMs, testerName));

            Paragraph detailsTitle = new Paragraph("Automated test cases", SECTION_FONT);
            detailsTitle.setSpacingBefore(14f);
            detailsTitle.setSpacingAfter(6f);
            document.add(detailsTitle);
            document.add(buildResultsTable(testResults));
            document.close();
            Logger.logMessage("Execution PDF report created: " + pdfFile.getAbsolutePath());
            return pdfFile;
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to generate PDF execution report.");
            e.printStackTrace();
            return null;
        }
    }

    private static PdfPTable buildHeaderBanner(String applicationTitle, String suiteTitle) throws DocumentException {
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        banner.setSpacingAfter(10f);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setBackgroundColor(TEAL);
        cell.setPaddingTop(14f);
        cell.setPaddingBottom(14f);
        cell.setPaddingLeft(16f);
        cell.setPaddingRight(16f);

        String resolvedApp = applicationTitle == null || applicationTitle.isBlank() ? "Automation" : applicationTitle.trim();
        Paragraph title = new Paragraph(resolvedApp + " - Automation Report", TITLE_FONT);
        title.setSpacingAfter(4f);
        cell.addElement(title);

        if (suiteTitle != null && !suiteTitle.isBlank()) {
            Paragraph subtitle = new Paragraph(suiteTitle.trim(), SUBTITLE_FONT);
            cell.addElement(subtitle);
        }
        banner.addCell(cell);
        return banner;
    }

    private static PdfPTable buildMetaPanel(String applicationTitle, String environment, int passCount, int failCount,
            int skipCount, long totalDurationMs, String testerName) throws DocumentException {
        PdfPTable panel = new PdfPTable(new float[] { 1.2f, 1.8f, 1.2f, 1.8f });
        panel.setWidthPercentage(100);
        panel.setSpacingAfter(10f);

        String executionDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
        String testStatus = "Passed : " + passCount + " , Skipped : " + skipCount + " , Failed : " + failCount;
        String resolvedTester = testerName == null || testerName.isBlank() ? "Automation Bot" : testerName.trim();
        String envLabel = environment == null ? "" : environment.trim();
        if ("prod".equalsIgnoreCase(envLabel)) {
            envLabel = "PROD";
        } else if ("dev".equalsIgnoreCase(envLabel)) {
            envLabel = "DEV";
        } else if ("uat".equalsIgnoreCase(envLabel)) {
            envLabel = "UAT";
        }

        addMetaRow(panel, "Application", applicationTitle, "Environment", envLabel);
        addMetaRow(panel, "Test Status", testStatus, "Execution Date", executionDate);
        addMetaRow(panel, "Tester Name", resolvedTester, "Execution Duration",
                formatDurationClock(totalDurationMs));
        return panel;
    }

    private static void addMetaRow(PdfPTable table, String label1, String value1, String label2, String value2) {
        addMetaLabelCell(table, label1);
        addMetaValueCell(table, value1);
        addMetaLabelCell(table, label2);
        addMetaValueCell(table, value2);
    }

    private static void addMetaLabelCell(PdfPTable table, String label) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, META_HEADER_FONT));
        labelCell.setBackgroundColor(TEAL);
        labelCell.setBorderColor(BORDER);
        labelCell.setPadding(8f);
        table.addCell(labelCell);
    }

    private static void addMetaValueCell(PdfPTable table, String value) {
        PdfPCell valueCell = new PdfPCell(new Phrase(value == null ? "" : value, VALUE_FONT));
        valueCell.setBackgroundColor(BaseColor.WHITE);
        valueCell.setBorderColor(BORDER);
        valueCell.setPadding(8f);
        table.addCell(valueCell);
    }

    private static PdfPTable buildResultsTable(List<TestResultRecord> testResults) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 0.4f, 1.0f, 2.2f, 0.6f, 1.5f, 1.5f, 2.0f });
        table.setWidthPercentage(100);
        table.setSpacingBefore(4f);

        addHeaderCell(table, "#");
        addHeaderCell(table, "Test case ID");
        addHeaderCell(table, "Scenario");
        addHeaderCell(table, "Status");
        addHeaderCell(table, "Expected");
        addHeaderCell(table, "Actual");
        addHeaderCell(table, "Failure reason (If any)");

        if (testResults == null || testResults.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No automated test cases to report.", BODY_FONT));
            emptyCell.setColspan(7);
            table.addCell(emptyCell);
            return table;
        }

        for (TestResultRecord record : testResults) {
            addBodyCell(table, String.valueOf(record.getIndex()));
            addBodyCell(table, record.getTestCaseId());
            addBodyCell(table, record.getScenario());
            addStatusCell(table, record.getStatus());
            addBodyCell(table, displayOutcomeCell(record.getStatus(), record.getExpected()));
            addBodyCell(table, displayOutcomeCell(record.getStatus(), record.getActual()));
            addFailureReasonCell(table, displayFailureReasonCell(record.getStatus(), record.getFailureReason()));
        }
        return table;
    }

    private static String displayCell(String value) {
        if (value == null || value.trim().isEmpty() || "—".equals(value.trim())) {
            return "—";
        }
        return value;
    }

    /** Expected and Actual show {@code —} for Unknown; Reason stays failure-only. */
    private static String displayOutcomeCell(String status, String value) {
        if (status == null) {
            return displayCell(value);
        }
        String normalized = status.trim();
        if ("Failed".equalsIgnoreCase(normalized) || "Error".equalsIgnoreCase(normalized)
                || "Passed".equalsIgnoreCase(normalized) || "Skipped".equalsIgnoreCase(normalized)) {
            return displayCell(value);
        }
        return "—";
    }

    private static String displayFailureReasonCell(String status, String value) {
        if (status == null) {
            return displayCell(value);
        }
        String normalized = status.trim();
        if ("Failed".equalsIgnoreCase(normalized) || "Error".equalsIgnoreCase(normalized)) {
            return displayCell(value);
        }
        return "—";
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,
                BaseColor.WHITE)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(TEAL);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private static void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, BODY_FONT));
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private static void addFailureReasonCell(PdfPTable table, String text) {
        Optional<ExecutionReportReasonFormatter.DefectReference> defect =
                ExecutionReportReasonFormatter.parseDefectReference(text);
        if (defect.isEmpty()) {
            addBodyCell(table, text);
            return;
        }
        ExecutionReportReasonFormatter.DefectReference reference = defect.get();
        Font defectFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, FAIL_TEXT);
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(reference.getLabel() + " ", defectFont));
        Anchor link = new Anchor(reference.getUrl(), defectFont);
        link.setReference(reference.getUrl());
        phrase.add(link);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private static void addStatusCell(PdfPTable table, String status) {
        ExecutionReportReasonFormatter.ExecutionOutcome outcome =
                ExecutionReportReasonFormatter.outcomeFromStatus(status);
        BaseColor bg = BaseColor.WHITE;
        BaseColor textColor = BaseColor.BLACK;
        String label = status == null ? "" : status;

        switch (outcome) {
        case PASSED:
            bg = PASS_BG;
            textColor = PASS_TEXT;
            label = ExecutionReportReasonFormatter.statusLabel(outcome);
            break;
        case FAILED:
            bg = FAIL_BG;
            textColor = FAIL_TEXT;
            label = ExecutionReportReasonFormatter.statusLabel(outcome);
            break;
        case ERROR:
            bg = FAIL_BG;
            textColor = FAIL_TEXT;
            label = ExecutionReportReasonFormatter.statusLabel(outcome);
            break;
        case SKIPPED:
            bg = SKIP_BG;
            textColor = SKIP_TEXT;
            label = ExecutionReportReasonFormatter.statusLabel(outcome);
            break;
        default:
            break;
        }
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textColor);

        PdfPCell cell = new PdfPCell(new Phrase(label, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private static String formatDurationClock(long durationMs) {
        if (durationMs <= 0) {
            return "N/A";
        }
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
