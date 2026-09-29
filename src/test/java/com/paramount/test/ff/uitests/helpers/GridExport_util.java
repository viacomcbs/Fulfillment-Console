package com.paramount.test.ff.uitests.helpers;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.pageobjects.TableView;
import com.synergy.core.driver.By;
import com.synergy.core.enums.FileSystemDirectory;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Orders grid Export (Excel) helpers for Fulfillment Console.
 */
public class GridExport_util {

    private static final int EXPORT_WAIT_SEC = 120;
    private static final int EXPORT_LINK_WAIT_SEC = 90;
    private static final int EXPORT_POLL_MS = 500;
    private static final String[] LEGACY_EXPORT_HEADERS = {"Delivery Offset", "Delivery offset"};

    private static final By EXPORT_BUTTON = By.XPath(
            "//button[normalize-space()='Export' or contains(normalize-space(),'Export')]"
                    + " | //span[normalize-space()='Export']/ancestor::button[1]"
                    + " | //*[self::button or @role='button'][contains(normalize-space(),'Export')][1]");

    private static final By EXPORT_ORDERS_OPTION = By.XPath(
            "//div[contains(@class,'cdk-overlay-pane') or contains(@class,'dropdown-menu') or contains(@class,'mat-mdc-menu-panel')]"
                    + "//*[normalize-space()='Orders' or normalize-space()='Order']"
                    + " | //*[contains(@class,'dropdown-menu')]//*[normalize-space()='Orders']"
                    + " | //span[normalize-space()='Orders']"
                    + " | //button[normalize-space()='Orders']");

    private static final By EXPORT_DOWNLOAD_LINK = By.XPath(
            "//a[contains(@href,'FulfillmentExport') and contains(@href,'.xlsx')]"
                    + " | //a[contains(@href,'cloudfront.net') and contains(@href,'FulfillmentExport')]"
                    + " | //a[contains(@href,'ops-console-backend-export-data')]");

    private static final String EXCEL_HEADER_DELIVERY_OFFSET = "Delivery Offset";
    private static final String EXCEL_HEADER_OFFSET_DELIVERY_DATE = "Offset Delivery date";
    private static final String EXCEL_HEADER_DELIVERY_DATE = "Delivery date";
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern US_DATE_PATTERN = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");
    private static final Pattern OFFSET_DAYS_PATTERN = Pattern.compile("(\\d+)\\s*d(?:ays?)?", Pattern.CASE_INSENSITIVE);

    private final GridSort_util gridSortUtil = new GridSort_util();

    /** UI values read from the first visible Orders grid row before row-level export. */
    public static final class FirstRowUiSnapshot {
        public final String offsetDeliveryDateCell;
        public final String deliveryDateCell;

        public FirstRowUiSnapshot(String offsetDeliveryDateCell, String deliveryDateCell) {
            this.offsetDeliveryDateCell = offsetDeliveryDateCell;
            this.deliveryDateCell = deliveryDateCell;
        }
    }

    /** Result of first-row Excel export: UI snapshot, downloaded file, and header row. */
    public static final class FirstRowExportResult {
        public final FirstRowUiSnapshot ui;
        public final File exportFile;
        public final List<String> headers;
        public final boolean exportClicked;

        public FirstRowExportResult(FirstRowUiSnapshot ui, File exportFile, List<String> headers, boolean exportClicked) {
            this.ui = ui;
            this.exportFile = exportFile;
            this.headers = headers == null ? Collections.<String>emptyList() : headers;
            this.exportClicked = exportClicked;
        }
    }

    public static final String HEADER_DELIVERY_OFFSET = EXCEL_HEADER_DELIVERY_OFFSET;
    public static final String HEADER_OFFSET_DELIVERY_DATE = EXCEL_HEADER_OFFSET_DELIVERY_DATE;
    public static final String HEADER_DELIVERY_DATE = EXCEL_HEADER_DELIVERY_DATE;

    public FirstRowUiSnapshot captureFirstRowDeliveryUiValues(String offsetColumnId, String deliveryColumnId) {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(EXCEL_HEADER_OFFSET_DELIVERY_DATE, offsetColumnId);
        FulfillmentJsUtil.scrollColumnHeaderIntoView(EXCEL_HEADER_DELIVERY_DATE, deliveryColumnId);
        String resolvedOffsetId = gridSortUtil.resolveHeaderColId(offsetColumnId, EXCEL_HEADER_OFFSET_DELIVERY_DATE);
        String resolvedDeliveryId = gridSortUtil.resolveHeaderColId(deliveryColumnId, EXCEL_HEADER_DELIVERY_DATE);
        if (resolvedOffsetId != null && !resolvedOffsetId.isEmpty()) {
            offsetColumnId = resolvedOffsetId;
        }
        if (resolvedDeliveryId != null && !resolvedDeliveryId.isEmpty()) {
            deliveryColumnId = resolvedDeliveryId;
        }
        gridSortUtil.scrollHeaderIntoView(offsetColumnId, new String[] {EXCEL_HEADER_OFFSET_DELIVERY_DATE});
        gridSortUtil.scrollHeaderIntoView(deliveryColumnId, new String[] {EXCEL_HEADER_DELIVERY_DATE});
        String offsetCell = firstNonEmpty(gridSortUtil.readVisibleColumnValues(offsetColumnId, 1));
        String deliveryCell = firstNonEmpty(gridSortUtil.readVisibleColumnValues(deliveryColumnId, 1));
        if (offsetCell == null) {
            offsetCell = FulfillmentJsUtil.readFirstRowCellByColumnLabel(EXCEL_HEADER_OFFSET_DELIVERY_DATE);
        }
        if (deliveryCell == null) {
            deliveryCell = FulfillmentJsUtil.readFirstRowCellByColumnLabel(EXCEL_HEADER_DELIVERY_DATE);
        }
        Logger.logReportMessage("First row UI — Offset Delivery date: [" + offsetCell + "], Delivery date: ["
                + deliveryCell + "]");
        return new FirstRowUiSnapshot(offsetCell, deliveryCell);
    }

    /**
     * Capture first-row UI values, export via row action menu, and return file + headers.
     */
    public FirstRowExportResult prepareAndExportFirstRow(String offsetColumnId, String deliveryColumnId)
            throws InterruptedException {
        return prepareAndExportFirstRow(offsetColumnId, deliveryColumnId, false);
    }

    /**
     * Capture first-row UI values, export via toolbar Export > Orders, and return file + headers.
     * @param gridPrepped when true, skips long grid bootstrap waits (suite already prepared grid)
     */
    public FirstRowExportResult prepareAndExportFirstRow(String offsetColumnId, String deliveryColumnId,
            boolean gridPrepped) throws InterruptedException {
        if (!gridPrepped) {
            FulfillmentJsUtil.waitForOrdersGridReadyForExport(12);
            FulfillmentJsUtil.waitForOrdersGridTopRows(1, 8);
        } else if (FulfillmentJsUtil.countOrdersGridTopRows() < 1) {
            FulfillmentJsUtil.waitForOrdersGridTopRows(1, 8);
        }
        FulfillmentJsUtil.applyPageZoom("0.67");
        FulfillmentJsUtil.closeManageColumnsPanel();
        FulfillmentJsUtil.dismissBlockingOverlays();
        if (!FulfillmentJsUtil.waitForOrdersGridHeadersReady(30)) {
            if (FulfillmentJsUtil.countOrdersGridTopRows() < 3) {
                Logger.logReportMessage("Export prep: grid headers not ready — refreshing table");
                FulfillmentJsUtil.clickRefreshOrdersTable();
                Thread.sleep(2500);
                FulfillmentJsUtil.waitForOrdersGridHeadersReady(20);
            } else {
                Logger.logReportMessage("Export prep: headers not detected but rows present — skipping table refresh");
            }
        }
        if (!FulfillmentJsUtil.isOrdersGridHeadersReady()) {
            FulfillmentJsUtil.logOrdersGridDiagnostics();
        }
        FirstRowUiSnapshot ui = captureFirstRowDeliveryUiValues(offsetColumnId, deliveryColumnId);
        FulfillmentJsUtil.dismissBlockingOverlays();
        long exportStartMs = System.currentTimeMillis();
        FulfillmentJsUtil.installExportCaptureHook();
        boolean exported = clickExportOrdersToolbar();
        if (!exported) {
            Logger.logReportMessage("Toolbar Export > Orders failed — trying row action menu export");
            exported = clickExportFirstRowExcelDocument();
        }
        if (!exported) {
            Logger.logReportMessage("Export menu click failed — skipping file wait");
            return new FirstRowExportResult(ui, null, Collections.<String>emptyList(), false);
        }
        File exportFile = waitForExportedFile(exportStartMs);
        List<String> headers = (exportFile != null && exportFile.exists())
                ? readExcelHeaderRow(exportFile) : Collections.<String>emptyList();
        if (!headers.isEmpty()) {
            Logger.logReportMessage("Export headers: " + headers);
        }
        return new FirstRowExportResult(ui, exportFile, headers, exported);
    }

    public void assertExportFileDownloaded(FirstRowExportResult result) {
        Verify.softAssert(result.exportClicked, "Export > Orders menu clicked");
        Verify.softAssert(result.exportFile != null && result.exportFile.exists(),
                "Order export file downloaded: "
                        + (result.exportFile == null ? "null" : result.exportFile.getName()));
        Verify.softAssert(!result.headers.isEmpty(), "Export file contains a header row");
    }

    /**
     * Done filter → first row action menu → Export → Excel Document; validate three export columns vs UI.
     */
    public void verifyFirstRowExportDeliveryColumns(FirstRowUiSnapshot ui) throws InterruptedException {
        FulfillmentJsUtil.installExportCaptureHook();
        FulfillmentJsUtil.dismissBlockingOverlays();
        long exportStartMs = System.currentTimeMillis();
        boolean exported = clickExportFirstRowExcelDocument();
        Verify.softAssert(exported, "First row Export > Excel Document menu clicked");
        File exportFile = waitForExportedFile(exportStartMs);
        Verify.softAssert(exportFile != null && exportFile.exists(),
                "Row export file downloaded: " + (exportFile == null ? "null" : exportFile.getName()));
        if (exportFile == null || !exportFile.exists()) {
            return;
        }
        List<String> headers = readExcelHeaderRow(exportFile);
        Logger.logReportMessage("Export headers: " + headers);

        verifyExportDeliveryOffsetColumn(headers, exportFile, ui);
        verifyExportOffsetDeliveryDateColumn(headers, exportFile, ui);
        verifyExportDeliveryDateColumn(headers, exportFile, ui);
    }

    public void verifyExportDeliveryOffsetColumn(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportDeliveryOffsetColumn(result.headers, result.exportFile, result.ui);
    }

    public void verifyExportOffsetDeliveryDateColumn(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportOffsetDeliveryDateColumn(result.headers, result.exportFile, result.ui);
    }

    public void verifyExportDeliveryDateColumn(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportDeliveryDateColumn(result.headers, result.exportFile, result.ui);
    }

    public void verifyExportDeliveryOffsetHeader(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportDeliveryOffsetHeader(result.headers);
    }

    public void verifyExportOffsetDeliveryDateHeader(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportOffsetDeliveryDateHeader(result.headers);
    }

    public void verifyExportDeliveryDateHeader(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportDeliveryDateHeader(result.headers);
    }

    public void verifyExportDeliveryOffsetData(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportDeliveryOffsetData(result.exportFile, result.ui);
    }

    public void verifyExportOffsetDeliveryDateData(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportOffsetDeliveryDateData(result.exportFile, result.ui);
    }

    public void verifyExportDeliveryDateData(FirstRowExportResult result) {
        assertExportFileDownloaded(result);
        if (result.exportFile == null || !result.exportFile.exists()) {
            return;
        }
        verifyExportDeliveryDateData(result.exportFile, result.ui);
    }

    public void verifyExportDeliveryOffsetColumn(List<String> headers, File exportFile, FirstRowUiSnapshot ui) {
        verifyExportDeliveryOffsetHeader(headers);
        verifyExportDeliveryOffsetData(exportFile, ui);
    }

    public void verifyExportOffsetDeliveryDateColumn(List<String> headers, File exportFile, FirstRowUiSnapshot ui) {
        verifyExportOffsetDeliveryDateHeader(headers);
        verifyExportOffsetDeliveryDateData(exportFile, ui);
    }

    public void verifyExportDeliveryDateColumn(List<String> headers, File exportFile, FirstRowUiSnapshot ui) {
        verifyExportDeliveryDateHeader(headers);
        verifyExportDeliveryDateData(exportFile, ui);
    }

    public void verifyExportDeliveryOffsetHeader(List<String> headers) {
        Verify.softAssert(headerPresent(headers, EXCEL_HEADER_DELIVERY_OFFSET),
                "Export contains column header: " + EXCEL_HEADER_DELIVERY_OFFSET);
    }

    public void verifyExportOffsetDeliveryDateHeader(List<String> headers) {
        Verify.softAssert(headerPresent(headers, EXCEL_HEADER_OFFSET_DELIVERY_DATE),
                "Export contains column header: " + EXCEL_HEADER_OFFSET_DELIVERY_DATE);
    }

    public void verifyExportDeliveryDateHeader(List<String> headers) {
        Verify.softAssert(headerPresent(headers, EXCEL_HEADER_DELIVERY_DATE),
                "Export contains column header: " + EXCEL_HEADER_DELIVERY_DATE);
    }

    public void verifyExportDeliveryOffsetData(File exportFile, FirstRowUiSnapshot ui) {
        String excelValue = readExcelCellByHeader(exportFile, EXCEL_HEADER_DELIVERY_OFFSET, 0);
        Logger.logReportMessage("Excel Delivery Offset value: [" + excelValue + "]");
        Verify.softAssert(excelValue != null && !excelValue.trim().isEmpty(),
                "Delivery Offset column has data in export row");
        Verify.softAssert(deliveryOffsetMatchesUi(ui.offsetDeliveryDateCell, excelValue),
                "Delivery Offset Excel value matches UI offset (UI=[" + ui.offsetDeliveryDateCell + "], Excel=["
                        + excelValue + "])");
    }

    public void verifyExportOffsetDeliveryDateData(File exportFile, FirstRowUiSnapshot ui) {
        String excelValue = readExcelCellByHeader(exportFile, EXCEL_HEADER_OFFSET_DELIVERY_DATE, 0);
        Logger.logReportMessage("Excel Offset Delivery date value: [" + excelValue + "]");
        Verify.softAssert(excelValue != null && !excelValue.trim().isEmpty(),
                "Offset Delivery date column has data in export row");
        Verify.softAssert(dateValueMatchesUi(ui.offsetDeliveryDateCell, excelValue),
                "Offset Delivery date Excel value matches UI date (UI=[" + ui.offsetDeliveryDateCell
                        + "], Excel=[" + excelValue + "])");
    }

    public void verifyExportDeliveryDateData(File exportFile, FirstRowUiSnapshot ui) {
        String excelValue = readExcelCellByHeader(exportFile, EXCEL_HEADER_DELIVERY_DATE, 0);
        Logger.logReportMessage("Excel Delivery date value: [" + excelValue + "]");
        Verify.softAssert(excelValue != null && !excelValue.trim().isEmpty(),
                "Delivery date column has data in export row");
        Verify.softAssert(dateValueMatchesUi(ui.deliveryDateCell, excelValue),
                "Delivery date Excel value matches UI date (UI=[" + ui.deliveryDateCell + "], Excel=["
                        + excelValue + "])");
    }

    public boolean clickExportFirstRowExcelDocument() throws InterruptedException {
        FulfillmentJsUtil.dismissBlockingOverlays();
        FulfillmentJsUtil.installExportCaptureHook();
        Thread.sleep(300);
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (!FulfillmentJsUtil.openFirstRowActionMenu()) {
                Logger.logReportMessage("Row export attempt " + attempt + ": could not open row action menu");
                FulfillmentJsUtil.logFirstRowActionButtonDiag();
                Thread.sleep(400);
                continue;
            }
            Thread.sleep(500);
            if (!FulfillmentJsUtil.isRowActionMenuOpen()) {
                Logger.logReportMessage("Row export attempt " + attempt + ": menu not visible after open");
                Thread.sleep(300);
                continue;
            }
            if (FulfillmentJsUtil.hoverRowMenuSubmenu("export")) {
                Thread.sleep(500);
                if (FulfillmentJsUtil.clickVisibleRowMenuItem("excel")) {
                    Thread.sleep(800);
                    return true;
                }
                Logger.logReportMessage("Row export attempt " + attempt + ": Excel item not found after Export hover");
            }
            if (FulfillmentJsUtil.clickVisibleRowMenuItem("export")) {
                Thread.sleep(500);
                if (FulfillmentJsUtil.clickVisibleRowMenuItem("excel")) {
                    Thread.sleep(800);
                    return true;
                }
                Logger.logReportMessage("Row export attempt " + attempt + ": Excel item not found after Export click");
            } else {
                Logger.logReportMessage("Row export attempt " + attempt + ": Export item not found in menu");
            }
            FulfillmentJsUtil.dismissBlockingOverlays();
            Thread.sleep(300);
        }
        FulfillmentJsUtil.logFirstRowActionButtonDiag();
        Logger.logReportMessage("JS row export failed after 3 attempts — trying XPath fallback");
        try {
            if (DriverUtil.clickOnElementSafely(tableView.firstRowActionMenuButton(), 5)) {
                Thread.sleep(400);
                if (DriverUtil.clickOnElementSafely(tableView.rowMenuExportOption(), 5)) {
                    Thread.sleep(400);
                    if (DriverUtil.clickOnElementSafely(tableView.rowMenuExcelExportOption(), 5)) {
                        Thread.sleep(800);
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.log("XPath row export fallback failed: " + ex.getMessage());
        }
        return false;
    }

    public String readExcelCellByHeader(File exportFile, String headerName, int dataRowIndex) {
        if (exportFile == null || !exportFile.exists() || headerName == null) {
            return null;
        }
        DataFormatter formatter = new DataFormatter();
        FileInputStream inputStream = null;
        Workbook workbook = null;
        try {
            inputStream = new FileInputStream(exportFile);
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return null;
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return null;
            }
            int colIndex = findHeaderColumnIndex(headerRow, formatter, headerName);
            if (colIndex < 0) {
                return null;
            }
            Row dataRow = sheet.getRow(1 + dataRowIndex);
            if (dataRow == null || dataRow.getCell(colIndex) == null) {
                return null;
            }
            return formatter.formatCellValue(dataRow.getCell(colIndex)).trim();
        } catch (Exception ex) {
            Logger.log("Failed to read export cell [" + headerName + "]: " + ex.getMessage());
            return null;
        } finally {
            closeQuietly(workbook);
            closeQuietly(inputStream);
        }
    }

    private int findHeaderColumnIndex(Row headerRow, DataFormatter formatter, String headerName) {
        String expected = normalizeHeader(headerName);
        short lastCell = headerRow.getLastCellNum();
        for (int i = 0; i < lastCell; i++) {
            if (headerRow.getCell(i) == null) {
                continue;
            }
            String value = normalizeHeader(formatter.formatCellValue(headerRow.getCell(i)));
            if (value.equals(expected) || value.contains(expected) || expected.contains(value)) {
                return i;
            }
        }
        return -1;
    }

    private boolean deliveryOffsetMatchesUi(String uiOffsetCell, String excelValue) {
        if (excelValue == null || excelValue.trim().isEmpty()) {
            return false;
        }
        String excelLower = excelValue.toLowerCase(Locale.ROOT);
        if (!excelLower.contains("day")) {
            return false;
        }
        if (uiOffsetCell == null || uiOffsetCell.trim().isEmpty()) {
            return excelLower.matches(".*\\d+\\s*days?.*");
        }
        Matcher matcher = OFFSET_DAYS_PATTERN.matcher(uiOffsetCell);
        if (!matcher.find()) {
            return excelLower.matches(".*\\d+\\s*days?.*");
        }
        String uiDays = matcher.group(1);
        return excelLower.contains(uiDays);
    }

    private boolean dateValueMatchesUi(String uiCell, String excelValue) {
        String uiDate = extractIsoDatePart(uiCell);
        String excelDate = extractIsoDatePart(excelValue);
        if (uiDate == null || excelDate == null) {
            return false;
        }
        return uiDate.equals(excelDate);
    }

    private String extractIsoDatePart(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Matcher iso = ISO_DATE_PATTERN.matcher(text);
        if (iso.find()) {
            return iso.group(1);
        }
        Matcher us = US_DATE_PATTERN.matcher(text);
        if (us.find()) {
            int month = Integer.parseInt(us.group(1));
            int day = Integer.parseInt(us.group(2));
            int year = Integer.parseInt(us.group(3));
            return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
        }
        return null;
    }

    private static String firstNonEmpty(List<String> values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static Object runExportScript(String script) {
        try {
            return BaseTest.driver.get().browser().executeScript(script);
        } catch (Exception e) {
            Logger.log("GridExport script failed: " + e.getMessage());
            return null;
        }
    }

    private static boolean truthy(Object result) {
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return "true".equalsIgnoreCase(String.valueOf(result));
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    private final TableView tableView = new TableView();

    public void verifyOrderExportContainsColumns(String[] expectedHeaders) throws InterruptedException {
        FulfillmentJsUtil.installExportCaptureHook();
        FulfillmentJsUtil.dismissBlockingOverlays();
        long exportStartMs = System.currentTimeMillis();
        clickExportOrders();
        File exportFile = waitForExportedFile(exportStartMs);
        Verify.softAssert(exportFile != null && exportFile.exists(),
                "Order export file downloaded: " + (exportFile == null ? "null" : exportFile.getName()));
        if (exportFile == null || !exportFile.exists()) {
            return;
        }
        List<String> headers = readExcelHeaderRow(exportFile);
        Logger.logReportMessage("Export headers: " + headers);
        Verify.softAssert(!headers.isEmpty(), "Export file contains a header row");
        for (String expected : expectedHeaders) {
            Verify.softAssert(headerPresent(headers, expected),
                    "Export contains column header: " + expected);
        }
        for (String legacy : LEGACY_EXPORT_HEADERS) {
            Verify.softAssert(!headerPresent(headers, legacy),
                    "Export does not contain legacy header: " + legacy);
        }
    }

    public void clickExportOrders() throws InterruptedException {
        clickExportOrdersToolbar();
    }

    /** Toolbar Export -> Orders (.xlsx). Returns true when the menu path was clicked. */
    public boolean clickExportOrdersToolbar() throws InterruptedException {
        FulfillmentJsUtil.dismissBlockingOverlays();
        FulfillmentJsUtil.applyPageZoom("0.67");
        FulfillmentJsUtil.waitForOrdersGridHeadersReady(20);
        FulfillmentJsUtil.installExportCaptureHook();
        if (!DriverUtil.clickOnElementSafely(EXPORT_BUTTON, 10)) {
            if (!FulfillmentJsUtil.clickExportOrdersMenu()) {
                return false;
            }
        } else {
            Thread.sleep(500);
            if (!DriverUtil.clickOnElementSafely(EXPORT_ORDERS_OPTION, 8)) {
                if (!FulfillmentJsUtil.clickExportOrdersMenu()) {
                    return false;
                }
            }
        }
        Logger.logReportMessage("Export > Orders clicked — waiting for CloudFront download link (TC024)");
        Thread.sleep(2000);
        String earlyUrl = FulfillmentJsUtil.waitForFulfillmentExportDownloadUrl(EXPORT_LINK_WAIT_SEC);
        if (earlyUrl != null && !earlyUrl.isEmpty()) {
            Logger.logReportMessage("FulfillmentExport URL detected: " + earlyUrl);
            FulfillmentJsUtil.clickFulfillmentExportDownloadLink();
            File fetched = FulfillmentJsUtil.fetchLatestExportFileToTemp();
            if (fetched != null && fetched.exists()) {
                return true;
            }
        }
        clickExportDownloadLinkWithRetry();
        return true;
    }

    /** TC024: after Export > Orders, click the CloudFront download link when it appears. */
    public void clickExportDownloadLinkIfPresent() throws InterruptedException {
        clickExportDownloadLinkOnce();
    }

    private void clickExportDownloadLinkOnce() throws InterruptedException {
        if (DriverUtil.clickOnElementSafely(EXPORT_DOWNLOAD_LINK, 2)) {
            Logger.logReportMessage("Clicked FulfillmentExport download link (TC024)");
            Thread.sleep(500);
            return;
        }
        if (FulfillmentJsUtil.clickFulfillmentExportDownloadLink()) {
            Logger.logReportMessage("Clicked FulfillmentExport download link via JS (TC024)");
            Thread.sleep(500);
        }
    }

    private void clickExportDownloadLinkWithRetry() throws InterruptedException {
        for (int attempt = 0; attempt < 45; attempt++) {
            String exportUrl = FulfillmentJsUtil.readFulfillmentExportDownloadUrl();
            if (exportUrl != null && !exportUrl.isEmpty()) {
                Logger.logReportMessage("FulfillmentExport URL detected: " + exportUrl);
            }
            clickExportDownloadLinkOnce();
            File fetched = FulfillmentJsUtil.fetchLatestExportFileToTemp();
            if (fetched != null && fetched.exists()) {
                return;
            }
            if (attempt == 20 || attempt == 40) {
                FulfillmentJsUtil.logExportCaptureDiagnostics();
            }
            Thread.sleep(2000);
        }
    }

    private File waitForExportedFile(long exportStartMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (EXPORT_WAIT_SEC * 1000L);
        String capturedName = null;
        int pollCount = 0;
        while (System.currentTimeMillis() < deadline) {
            String exportUrl = FulfillmentJsUtil.readFulfillmentExportDownloadUrl();
            if (exportUrl != null && !exportUrl.isEmpty()) {
                Logger.logReportMessage("FulfillmentExport URL detected: " + exportUrl);
                File fromNavigate = downloadExportFromUrl(exportUrl);
                if (fromNavigate != null) {
                    return fromNavigate;
                }
                FulfillmentJsUtil.clickFulfillmentExportDownloadLink();
            }
            File fromUrl = FulfillmentJsUtil.fetchLatestExportFileToTemp();
            if (fromUrl != null && fromUrl.exists() && fromUrl.length() > 0) {
                return fromUrl;
            }
            if (pollCount % 3 == 0) {
                clickExportDownloadLinkIfPresent();
            }
            capturedName = FulfillmentJsUtil.readCapturedExportFileName();
            if (capturedName != null && !capturedName.trim().isEmpty()) {
                File imported = tryImportDownload(capturedName.trim());
                if (imported != null) {
                    return imported;
                }
            }
            pollCount++;
            if (pollCount % 4 == 0) {
                for (String candidate : buildExportFilenameCandidates(capturedName)) {
                    File imported = tryImportDownload(candidate);
                    if (imported != null) {
                        return imported;
                    }
                }
                File recent = tryImportRecentFulfillmentExports(exportStartMs - 5000L);
                if (recent != null) {
                    return recent;
                }
                File synergyFulfillment = findNewestSynergyImportXlsxByPrefix(
                        exportStartMs - 5000L, "fulfillmentexport");
                if (synergyFulfillment != null) {
                    return synergyFulfillment;
                }
                File userDownload = findNewestMatchingDownloadXlsx(exportStartMs - 5000L,
                        "fulfillmentexport", "orders");
                if (userDownload != null) {
                    return userDownload;
                }
            }
            Thread.sleep(EXPORT_POLL_MS);
        }
        FulfillmentJsUtil.logExportCaptureDiagnostics();
        Logger.log("Export file not found after " + EXPORT_WAIT_SEC + "s");
        return null;
    }

    private List<String> buildExportFilenameCandidates(String capturedName) {
        List<String> candidates = new ArrayList<String>();
        if (capturedName != null && !capturedName.isEmpty()) {
            candidates.add(capturedName);
        }
        candidates.addAll(Arrays.asList(
                "Orders.xlsx",
                "orders.xlsx",
                "Order.xlsx",
                "FulfillmentExport",
                "Fulfillment Orders.xlsx",
                "FulfillmentOrders.xlsx",
                "fulfillment_orders.xlsx"));
        return candidates;
    }

    private File tryImportRecentFulfillmentExports(long notBeforeMs) {
        LocalDateTime start = Instant.ofEpochMilli(notBeforeMs).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = LocalDateTime.now().plusSeconds(10);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        for (LocalDateTime t = start; !t.isAfter(end); t = t.plusSeconds(1)) {
            String name = "FulfillmentExport-" + t.format(fmt) + ".xlsx";
            File imported = tryImportDownload(name);
            if (imported != null) {
                return imported;
            }
        }
        return null;
    }

    private File findNewestSynergyImportXlsxByPrefix(long notBeforeMs, String prefix) {
        Path dir = synergyImportTempDir();
        if (!Files.isDirectory(dir)) {
            return null;
        }
        File[] files = dir.toFile().listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        String lowerPrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<File> xlsx = new ArrayList<File>();
        for (File file : files) {
            if (!file.isFile() || !file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
                continue;
            }
            if (file.lastModified() < notBeforeMs || file.length() <= 0) {
                continue;
            }
            if (!lowerPrefix.isEmpty()
                    && !file.getName().toLowerCase(Locale.ROOT).contains(lowerPrefix)) {
                continue;
            }
            xlsx.add(file);
        }
        if (xlsx.isEmpty()) {
            return null;
        }
        Collections.sort(xlsx, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return Long.compare(b.lastModified(), a.lastModified());
            }
        });
        File newest = xlsx.get(0);
        Logger.logReportMessage("Using Synergy temp FulfillmentExport xlsx: " + newest.getAbsolutePath());
        return newest;
    }

    private File downloadExportFromUrl(String exportUrl) throws InterruptedException {
        if (exportUrl == null || exportUrl.trim().isEmpty() || BaseTest.driver.get() == null) {
            return null;
        }
        String fileName = exportUrl.split("/")[exportUrl.split("/").length - 1].split("\\?")[0];
        try {
            BaseTest.driver.get().browser().getUrl(exportUrl);
            Thread.sleep(2500);
            File imported = tryImportDownload(fileName);
            if (imported != null) {
                return imported;
            }
        } catch (Exception ex) {
            Logger.log("Navigate export URL failed: " + ex.getMessage());
        }
        File fetched = FulfillmentJsUtil.fetchLatestExportFileToTemp();
        if (fetched != null && fetched.exists()) {
            return fetched;
        }
        return tryImportDownload(fileName);
    }

    private File tryImportDownload(String fileName) {
        if (fileName == null || fileName.trim().isEmpty() || BaseTest.driver.get() == null) {
            return null;
        }
        try {
            File imported = BaseTest.driver.get().fileSystem().importFile(FileSystemDirectory.DOWNLOADS, fileName);
            if (imported != null && imported.exists() && imported.length() > 0) {
                Logger.logReportMessage("Imported export from Downloads: " + fileName);
                return imported;
            }
        } catch (Exception ex) {
            Logger.log("importFile failed for " + fileName + ": " + ex.getMessage());
        }
        return null;
    }

    private File findNewestSynergyImportXlsx(long notBeforeMs) {
        Path dir = synergyImportTempDir();
        if (!Files.isDirectory(dir)) {
            return null;
        }
        File[] files = dir.toFile().listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        List<File> xlsx = new ArrayList<File>();
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")
                    && file.lastModified() >= notBeforeMs && file.length() > 0) {
                xlsx.add(file);
            }
        }
        if (xlsx.isEmpty()) {
            return null;
        }
        Collections.sort(xlsx, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return Long.compare(b.lastModified(), a.lastModified());
            }
        });
        File newest = xlsx.get(0);
        Logger.logReportMessage("Using newest Synergy import xlsx: " + newest.getAbsolutePath());
        return newest;
    }

    private File findNewestMatchingDownloadXlsx(long notBeforeMs, String... nameTokens) {
        String userProfile = System.getenv("USERPROFILE");
        if (userProfile == null || userProfile.trim().isEmpty()) {
            return null;
        }
        Path downloads = Paths.get(userProfile, "Downloads");
        if (!Files.isDirectory(downloads)) {
            return null;
        }
        File[] files = downloads.toFile().listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        List<File> xlsx = new ArrayList<File>();
        for (File file : files) {
            if (!file.isFile() || !file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
                continue;
            }
            if (file.lastModified() < notBeforeMs || file.length() <= 0) {
                continue;
            }
            String lowerName = file.getName().toLowerCase(Locale.ROOT);
            boolean matches = nameTokens == null || nameTokens.length == 0;
            if (!matches) {
                for (String token : nameTokens) {
                    if (token != null && lowerName.contains(token.toLowerCase(Locale.ROOT))) {
                        matches = true;
                        break;
                    }
                }
            }
            if (matches) {
                xlsx.add(file);
            }
        }
        if (xlsx.isEmpty()) {
            return null;
        }
        Collections.sort(xlsx, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return Long.compare(b.lastModified(), a.lastModified());
            }
        });
        File newest = xlsx.get(0);
        Logger.logReportMessage("Using matched Downloads xlsx: " + newest.getAbsolutePath());
        return newest;
    }

    private File findNewestUserDownloadXlsx(long notBeforeMs) {
        String userProfile = System.getenv("USERPROFILE");
        if (userProfile == null || userProfile.trim().isEmpty()) {
            return null;
        }
        Path downloads = Paths.get(userProfile, "Downloads");
        if (!Files.isDirectory(downloads)) {
            return null;
        }
        File[] files = downloads.toFile().listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        List<File> xlsx = new ArrayList<File>();
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")
                    && file.lastModified() >= notBeforeMs && file.length() > 0) {
                xlsx.add(file);
            }
        }
        if (xlsx.isEmpty()) {
            return null;
        }
        Collections.sort(xlsx, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return Long.compare(b.lastModified(), a.lastModified());
            }
        });
        File newest = xlsx.get(0);
        Logger.logReportMessage("Using newest user Downloads xlsx: " + newest.getAbsolutePath());
        return newest;
    }

    private Path synergyImportTempDir() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.trim().isEmpty()) {
            return Paths.get(localAppData, "Temp", "com.viacom.synergy");
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "com.viacom.synergy");
    }

    public List<String> readExcelHeaderRow(File exportFile) {
        List<String> headers = new ArrayList<String>();
        if (exportFile == null || !exportFile.exists()) {
            return headers;
        }
        DataFormatter formatter = new DataFormatter();
        FileInputStream inputStream = null;
        Workbook workbook = null;
        try {
            inputStream = new FileInputStream(exportFile);
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return headers;
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return headers;
            }
            short lastCell = headerRow.getLastCellNum();
            for (int i = 0; i < lastCell; i++) {
                if (headerRow.getCell(i) == null) {
                    continue;
                }
                String value = formatter.formatCellValue(headerRow.getCell(i)).trim();
                if (!value.isEmpty()) {
                    headers.add(value);
                }
            }
        } catch (Exception ex) {
            Logger.log("Failed to read export headers: " + ex.getMessage());
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
        }
        return headers;
    }

    private boolean headerPresent(List<String> headers, String expected) {
        if (expected == null || headers == null) {
            return false;
        }
        String normalizedExpected = normalizeHeader(expected);
        for (String header : headers) {
            if (normalizeHeader(header).equals(normalizedExpected)) {
                return true;
            }
            if (normalizeHeader(header).contains(normalizedExpected)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeHeader(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }
}