package com.paramount.test.ff.uitests.helpers.ptspackaging;

import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.common.util.SynergyLocalPaths;
import com.paramount.test.ff.pageobjects.HomePage;
import com.paramount.test.ff.pageobjects.PtsPackagingIdPage;
import com.synergy.core.driver.By;
import com.synergy.core.enums.FileSystemDirectory;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import static com.paramount.test.ff.common.base.BaseTest.driver;

/**
 * Export + Excel validation for BSD-29441.
 * Orders tab: Export opens a dropdown (Orders / Line items / Packages).
 * Line Items tab: Export is a single click — export starts immediately (no menu selection).
 * Synergy stages FulfillmentExport-*.xlsx on the VM Downloads folder; {@code importFile} pulls it locally.
 */
public class PtsPackagingExportUtil {

    private static final int MENU_WAIT_S = 15;
    /** Poll Synergy Downloads / local import temp until export file appears. */
    private static final int EXPORT_POLL_MS = 2000;
    private static final int EXPORT_MAX_WAIT_MS = 300000;

    private final HomePage homePage = new HomePage();

    public enum ExportTarget {
        ORDERS("Orders",
                "Orders.xlsx",
                "orders.xlsx",
                "Orders Export.xlsx",
                "Fulfillment Orders.xlsx"),
        LINE_ITEMS("Line items",
                "Line items.xlsx",
                "Line Items.xlsx",
                "line_items.xlsx",
                "Line Items Export.xlsx");

        private final String menuLabel;
        private final String[] downloadCandidates;

        ExportTarget(String menuLabel, String... downloadCandidates) {
            this.menuLabel = menuLabel;
            this.downloadCandidates = downloadCandidates;
        }
    }

    public By menuItemFor(ExportTarget target) {
        if (target == ExportTarget.ORDERS) {
            return homePage.exportOrdersMenuItem();
        }
        return homePage.exportLineItemsMenuItem();
    }

    public void triggerExport(ExportTarget target, SoftAssert softAssert) throws InterruptedException {
        boolean triggered;
        if (target == ExportTarget.LINE_ITEMS) {
            triggered = clickLineItemsDirectExport();
            Verify.softAssert1(triggered,
                    "Clicked Export (Line Items — direct export, no dropdown menu)", softAssert);
        } else {
            triggered = clickExportMenuItemWithRetry(target);
            Verify.softAssert1(triggered,
                    "Clicked Export → " + target.menuLabel + " (Orders dropdown menu)", softAssert);
        }
        if (Config.isLocalExecution()) {
            Thread.sleep(1500);
            confirmLocalSaveAsDialogIfPresent();
        }
        Logger.logReportMessage("Triggered Excel export: " + target.menuLabel
                + " — polling Synergy Downloads / import temp (no download-icon click)");
    }

    /** Line Items console tab — one click on export-btn starts the export (PROD has no submenu). */
    private boolean clickLineItemsDirectExport() throws InterruptedException {
        dismissOpenOverlays();
        for (int attempt = 1; attempt <= 2; attempt++) {
            if (DriverUtil.clickOnElement(homePage.exportButton(), MENU_WAIT_S)) {
                return true;
            }
            Logger.logReportMessage("Line Items Export button not clickable (attempt " + attempt + ")");
            Thread.sleep(500);
        }
        return false;
    }

    /**
     * Orders tab only — click Export then pick Orders from the dropdown.
     * Retries if soft-assert screenshots or panel close dismissed the dropdown.
     */
    private boolean clickExportMenuItemWithRetry(ExportTarget target) throws InterruptedException {
        By menuItem = menuItemFor(target);
        for (int attempt = 1; attempt <= 2; attempt++) {
            dismissOpenOverlays();
            if (!DriverUtil.clickOnElement(homePage.exportButton(), MENU_WAIT_S)) {
                Logger.logReportMessage("Export button not clickable (attempt " + attempt + ")");
                continue;
            }
            Thread.sleep(400);
            if (WaitUtil.isDisplayFast(menuItem, 8) && DriverUtil.clickOnElement(menuItem, 8)) {
                return true;
            }
            Logger.logReportMessage(target.menuLabel + " export menu item not visible (attempt "
                    + attempt + ") — retrying Export dropdown");
            Thread.sleep(500);
        }
        return false;
    }

    private void dismissOpenOverlays() {
        try {
            driver.get().browser().executeScript(
                    "document.dispatchEvent(new KeyboardEvent('keydown', {key:'Escape', keyCode:27, bubbles:true}));");
            Thread.sleep(200);
        } catch (Exception ignored) {
            // optional cleanup before Export click
        }
    }

    /** Optional — only for local runs where a Windows Save As dialog may appear. */
    private void confirmLocalSaveAsDialogIfPresent() {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(100);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            Logger.logReportMessage("Local Save As — sent Enter (if dialog was open)");
        } catch (Exception e) {
            Logger.logConsoleMessage("Local Save As Enter not sent: " + e.getMessage());
        }
    }

    /**
     * Resolves the exported xlsx without clicking Chrome Downloads or in-app download links.
     * Checks Synergy import temp and VM Downloads via {@code importFile} on each poll.
     */
    public File waitForDownloadedExcel(ExportTarget target, long exportStartedAtMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + EXPORT_MAX_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            File synergyTemp = findSynergyImportTempExport(exportStartedAtMs, target);
            if (synergyTemp != null) {
                return synergyTemp;
            }
            File fulfillmentExport = findFulfillmentExportFile(target, exportStartedAtMs);
            if (fulfillmentExport != null) {
                return fulfillmentExport;
            }
            for (String candidate : buildDownloadNameCandidates(target, exportStartedAtMs)) {
                try {
                    File file = driver.get().fileSystem().importFile(FileSystemDirectory.DOWNLOADS, candidate);
                    if (file != null && file.exists() && file.length() > 0) {
                        Logger.logReportMessage("Imported export from Synergy VM Downloads: " + candidate);
                        return file;
                    }
                } catch (Exception ignored) {
                    // try next candidate or poll again
                }
            }
            if (Config.isLocalExecution()) {
                File local = findLocalDownloadedExcel(exportStartedAtMs, target);
                if (local != null) {
                    return local;
                }
            }
            Thread.sleep(EXPORT_POLL_MS);
        }
        Logger.logConsoleMessage("Export file not found after "
                + (EXPORT_MAX_WAIT_MS / 1000) + "s for " + target.menuLabel);
        return null;
    }

    private Set<String> buildDownloadNameCandidates(ExportTarget target, long exportStartedAtMs) {
        Set<String> names = new LinkedHashSet<>();
        names.addAll(Arrays.asList(target.downloadCandidates));
        for (int i = 1; i <= 5; i++) {
            names.add("Orders (" + i + ").xlsx");
            names.add("Line items (" + i + ").xlsx");
        }
        SimpleDateFormat secFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        SimpleDateFormat isoSecFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        SimpleDateFormat minFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        SimpleDateFormat secFmtUtc = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        secFmtUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat minFmtUtc = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        minFmtUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        long now = System.currentTimeMillis();
        for (long t = exportStartedAtMs - 10000; t <= now + 10000; t += 1000) {
            Date d = new Date(t);
            names.add(PtsPackagingIdPage.EXPORT_FILE_PREFIX + secFmt.format(d) + ".xlsx");
            names.add(PtsPackagingIdPage.EXPORT_FILE_PREFIX + isoSecFmt.format(d) + ".xlsx");
            names.add(PtsPackagingIdPage.EXPORT_FILE_PREFIX + secFmtUtc.format(d) + ".xlsx");
        }
        for (long t = exportStartedAtMs - 60000; t <= now + 60000; t += 60000) {
            Date d = new Date(t);
            names.add(PtsPackagingIdPage.EXPORT_FILE_PREFIX + minFmt.format(d) + ".xlsx");
            names.add(PtsPackagingIdPage.EXPORT_FILE_PREFIX + minFmtUtc.format(d) + ".xlsx");
        }
        return names;
    }

    private File findFulfillmentExportFile(ExportTarget target, long exportStartedAtMs) {
        if (Config.isLocalExecution()) {
            File local = findLocalFulfillmentExport(exportStartedAtMs);
            if (local != null) {
                return local;
            }
        }
        return findSynergyFulfillmentExport(target, exportStartedAtMs);
    }

    private File findLocalDownloadedExcel(long exportStartedAtMs, ExportTarget target) {
        File fulfillment = findLocalFulfillmentExport(exportStartedAtMs);
        if (fulfillment != null) {
            return fulfillment;
        }
        try {
            Path downloads = Paths.get(System.getProperty("user.home"), "Downloads");
            if (!Files.isDirectory(downloads)) {
                return null;
            }
            for (String candidate : target.downloadCandidates) {
                File f = downloads.resolve(candidate).toFile();
                if (f.exists() && f.length() > 0 && f.lastModified() >= exportStartedAtMs - 5000) {
                    Logger.logReportMessage("Found local export: " + candidate);
                    return f;
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Local candidate scan failed: " + e.getMessage());
        }
        return null;
    }

    private File findLocalFulfillmentExport(long exportStartedAtMs) {
        File fromSynergyTemp = findSynergyImportTempExport(exportStartedAtMs, null);
        if (fromSynergyTemp != null) {
            return fromSynergyTemp;
        }
        try {
            Path downloads = Paths.get(System.getProperty("user.home"), "Downloads");
            if (!Files.isDirectory(downloads)) {
                return null;
            }
            Optional<File> newest = Arrays.stream(downloads.toFile().listFiles())
                    .filter(f -> f.isFile()
                            && f.getName().startsWith(PtsPackagingIdPage.EXPORT_FILE_PREFIX)
                            && f.getName().endsWith(".xlsx")
                            && f.lastModified() >= exportStartedAtMs - 10000)
                    .max(Comparator.comparingLong(File::lastModified));
            if (newest.isPresent()) {
                Logger.logReportMessage("Found local FulfillmentExport: " + newest.get().getName());
                return newest.get();
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Local Downloads scan failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Synergy {@code importFile} copies VM downloads to {@link SynergyLocalPaths#synergyImportTempDir()}.
     */
    private File findSynergyImportTempExport(long exportStartedAtMs, ExportTarget target) {
        try {
            Path synergyTemp = SynergyLocalPaths.synergyImportTempDir();
            if (!Files.isDirectory(synergyTemp)) {
                return null;
            }
            Optional<File> fulfillment = Arrays.stream(synergyTemp.toFile().listFiles())
                    .filter(f -> f.isFile()
                            && f.getName().startsWith(PtsPackagingIdPage.EXPORT_FILE_PREFIX)
                            && f.getName().endsWith(".xlsx")
                            && f.length() > 0
                            && f.lastModified() >= exportStartedAtMs - 10000)
                    .max(Comparator.comparingLong(File::lastModified));
            if (fulfillment.isPresent()) {
                Logger.logReportMessage("Found Synergy import temp export: " + fulfillment.get().getAbsolutePath());
                return fulfillment.get();
            }
            if (target != null) {
                for (String candidate : target.downloadCandidates) {
                    File f = synergyTemp.resolve(candidate).toFile();
                    if (f.exists() && f.length() > 0 && f.lastModified() >= exportStartedAtMs - 5000) {
                        Logger.logReportMessage("Found Synergy import temp export: " + f.getAbsolutePath());
                        return f;
                    }
                }
            }
            Optional<File> anyXlsx = Arrays.stream(synergyTemp.toFile().listFiles())
                    .filter(f -> f.isFile()
                            && f.getName().endsWith(".xlsx")
                            && f.length() > 0
                            && f.lastModified() >= exportStartedAtMs - 10000)
                    .max(Comparator.comparingLong(File::lastModified));
            if (anyXlsx.isPresent()) {
                Logger.logReportMessage("Found Synergy import temp xlsx: " + anyXlsx.get().getAbsolutePath());
                return anyXlsx.get();
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Synergy import temp scan failed: " + e.getMessage());
        }
        return null;
    }

    private File findSynergyFulfillmentExport(ExportTarget target, long exportStartedAtMs) {
        List<String> candidates = new ArrayList<>(buildDownloadNameCandidates(target, exportStartedAtMs));
        for (String name : candidates) {
            if (!name.startsWith(PtsPackagingIdPage.EXPORT_FILE_PREFIX)) {
                continue;
            }
            try {
                File file = driver.get().fileSystem().importFile(FileSystemDirectory.DOWNLOADS, name);
                if (file != null && file.exists() && file.length() > 0) {
                    Logger.logReportMessage("Found Synergy FulfillmentExport: " + name);
                    return file;
                }
            } catch (Exception ignored) {
                // try next timestamp
            }
        }
        return null;
    }

    public void validateExcelColumnPresence(File excelFile, boolean shouldContain, SoftAssert softAssert)
            throws IOException {
        List<String> headers = FulfillmentExportExcelUtil.readHeaderRow(excelFile);
        boolean found = FulfillmentExportExcelUtil.headerRowContains(headers, PtsPackagingIdPage.COLUMN_LABEL);
        if (shouldContain) {
            Verify.softAssert1(found,
                    PtsPackagingIdPage.COLUMN_LABEL + " present in Excel export (headers=" + headers + ")",
                    softAssert);
        } else {
            Verify.softAssert1(!found,
                    PtsPackagingIdPage.COLUMN_LABEL + " absent from Excel export (headers=" + headers + ")",
                    softAssert);
        }
    }

    public void validateFirstPtsIdInExcel(File excelFile, String expectedGridValue, SoftAssert softAssert)
            throws IOException {
        String excelValue = FulfillmentExportExcelUtil.readFirstDataRowColumnValue(
                excelFile, PtsPackagingIdPage.COLUMN_LABEL);
        String normalizedExcel = FulfillmentExportExcelUtil.normalizePtsId(excelValue);
        String normalizedGrid = FulfillmentExportExcelUtil.normalizePtsId(expectedGridValue);
        Verify.softAssert1(!normalizedExcel.isEmpty(),
                "First PTS Packaging ID populated in Excel export row", softAssert);
        Verify.softAssert1(normalizedExcel.equals(normalizedGrid)
                        || normalizedExcel.contains(normalizedGrid)
                        || normalizedGrid.contains(normalizedExcel),
                "Excel first PTS Packaging ID matches grid (excel='" + excelValue
                        + "', grid='" + expectedGridValue + "')", softAssert);
    }

    public void exportAndValidateColumnInExcel(ExportTarget target, boolean shouldContain, SoftAssert softAssert)
            throws InterruptedException, IOException {
        long exportStartedAt = System.currentTimeMillis();
        triggerExport(target, softAssert);
        File exportFile = waitForDownloadedExcel(target, exportStartedAt);
        Verify.softAssert1(exportFile != null,
                "Excel export downloaded (FulfillmentExport-*.xlsx) for " + target.menuLabel, softAssert);
        if (exportFile != null) {
            validateExcelColumnPresence(exportFile, shouldContain, softAssert);
        }
    }

    public void exportAndValidateFirstPtsIdInExcel(ExportTarget target, String gridPtsValue, SoftAssert softAssert)
            throws InterruptedException, IOException {
        long exportStartedAt = System.currentTimeMillis();
        triggerExport(target, softAssert);
        File exportFile = waitForDownloadedExcel(target, exportStartedAt);
        Verify.softAssert1(exportFile != null,
                "Excel export downloaded (FulfillmentExport-*.xlsx) for " + target.menuLabel, softAssert);
        if (exportFile != null) {
            validateExcelColumnPresence(exportFile, true, softAssert);
            validateFirstPtsIdInExcel(exportFile, gridPtsValue, softAssert);
            Logger.logReportMessage("Validated Excel export file: " + exportFile.getAbsolutePath());
        }
    }
}
