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
import com.synergy.core.enums.WebKey;

import java.awt.Dimension;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;
import java.util.TimeZone;

import static com.paramount.test.ff.common.base.BaseTest.driver;

/**
 * Export dropdown + Excel validation for BSD-29441.
 * PROD Synergy: Export → Orders → wait for in-app "Exporting File" job → click completion
 * notification or Chrome Downloads toolbar → import FulfillmentExport-*.xlsx from VM Downloads.
 * Imported files land on the local runner under {@code %LOCALAPPDATA%\Temp\com.viacom.synergy}.
 */
public class PtsPackagingExportUtil {

    private static final int MENU_WAIT_S = 15;
    private static final int EXPORT_POLL_MS = 3000;
    /** Large PTS-filtered grids can take several minutes on PROD. */
    private static final int EXPORT_MAX_WAIT_MS = 300000;
    private static final int EXPORT_JOB_MAX_WAIT_MS = 240000;
    private static final int EXPORT_JOB_POLL_MS = 2000;
    /** Brief pause after clicking Export menu item — async export starts on PROD. */
    private static final int DOWNLOAD_START_WAIT_MS = 2000;
    private static final int NOTIFICATION_SETTLE_MS = 1500;
    private static final int EXPORT_READY_UI_MAX_WAIT_MS = 60000;
    private static final Pattern FULFILLMENT_EXPORT_PATTERN = Pattern.compile(
            "FulfillmentExport-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.xlsx",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ANY_XLSX_PATTERN = Pattern.compile(
            "[A-Za-z0-9_\\- ()]+\\.xlsx");

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
        Verify.softAssert1(DriverUtil.clickOnElement(homePage.exportButton(), MENU_WAIT_S),
                "Clicked Export dropdown (export-btn)", softAssert);
        Thread.sleep(800);
        Verify.softAssert1(DriverUtil.clickOnElement(menuItemFor(target), MENU_WAIT_S),
                "Clicked Export menu item: " + target.menuLabel, softAssert);
        Thread.sleep(DOWNLOAD_START_WAIT_MS);
        if (Config.isLocalExecution()) {
            confirmLocalSaveAsDialogIfPresent();
        } else {
            waitForAsyncExportJobToFinish();
            waitForExportReadyUi();
        }
        Logger.logReportMessage("Triggered Excel export: " + target.menuLabel
                + " — waiting for download via notification or Chrome Downloads");
    }

    /**
     * PROD export is async — grid toolbar shows "Exporting File N%" until the xlsx is ready.
     * Must wait for this to finish before Downloads folder or Chrome tray has the file.
     */
    private void waitForAsyncExportJobToFinish() throws InterruptedException {
        if (!WaitUtil.isDisplayFast(homePage.exportProgressIndicator(), 20)) {
            Logger.logReportMessage("No in-app export progress indicator — continuing to poll Downloads");
            return;
        }
        Logger.logReportMessage("Export job in progress (Exporting File…) — waiting for completion");
        long deadline = System.currentTimeMillis() + EXPORT_JOB_MAX_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (!WaitUtil.isDisplayFast(homePage.exportProgressIndicator(), 2)) {
                Logger.logReportMessage("Export job finished — progress indicator cleared");
                Thread.sleep(NOTIFICATION_SETTLE_MS);
                return;
            }
            Thread.sleep(EXPORT_JOB_POLL_MS);
        }
        Logger.logConsoleMessage("Export progress indicator still visible after "
                + (EXPORT_JOB_MAX_WAIT_MS / 1000) + "s — will still try Downloads");
    }

    /** PROD video: after progress clears, toast / toolbar link / bell notification appears (not always Chrome Downloads). */
    private void waitForExportReadyUi() throws InterruptedException {
        long deadline = System.currentTimeMillis() + EXPORT_READY_UI_MAX_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (WaitUtil.isDisplayFast(homePage.exportReadyToast(), 1)) {
                Logger.logReportMessage("Export-ready toast visible");
                return;
            }
            if (WaitUtil.isDisplayFast(homePage.exportToolbarDownloadLink(), 1)) {
                Logger.logReportMessage("Export toolbar download link visible");
                return;
            }
            if (WaitUtil.isDisplayFast(homePage.exportCompleteNotification(), 1)) {
                Logger.logReportMessage("Export-complete notification visible");
                return;
            }
            Thread.sleep(2000);
        }
        Logger.logConsoleMessage("No export-ready UI within " + (EXPORT_READY_UI_MAX_WAIT_MS / 1000) + "s — polling Downloads");
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

    public File waitForDownloadedExcel(ExportTarget target, long exportStartedAtMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + EXPORT_MAX_WAIT_MS;
        boolean chromeDownloadsOpened = false;
        long lastChromeDownloadsPageTry = 0;
        while (System.currentTimeMillis() < deadline) {
            clickExportReadyNotification();
            clickExportToolbarDownloadLink();
            File fromDetectedName = importExportFileByDetectedFilename(exportStartedAtMs);
            if (fromDetectedName != null) {
                return fromDetectedName;
            }
            File fromPageScan = importExportFileFromPageSourceScan(exportStartedAtMs);
            if (fromPageScan != null) {
                return fromPageScan;
            }
            if (!chromeDownloadsOpened && elapsedSec(exportStartedAtMs) >= 5) {
                chromeDownloadsOpened = clickChromeDownloadsToolbar();
                File fromShelf = importExportFileByDetectedFilename(exportStartedAtMs);
                if (fromShelf != null) {
                    return fromShelf;
                }
            }
            if (System.currentTimeMillis() - lastChromeDownloadsPageTry > 45000) {
                lastChromeDownloadsPageTry = System.currentTimeMillis();
                File fromChromePage = importExportFileByChromeDownloadsPage();
                if (fromChromePage != null) {
                    return fromChromePage;
                }
            }
            File fulfillmentExport = findFulfillmentExportFile(target, exportStartedAtMs);
            if (fulfillmentExport != null) {
                return fulfillmentExport;
            }
            for (String candidate : buildDownloadNameCandidates(target, exportStartedAtMs)) {
                try {
                    File file = driver.get().fileSystem().importFile(FileSystemDirectory.DOWNLOADS, candidate);
                    if (file != null && file.exists() && file.length() > 0) {
                        Logger.logReportMessage("Downloaded export file from Synergy VM: " + candidate);
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
            File synergyTemp = findSynergyImportTempExport(exportStartedAtMs, target);
            if (synergyTemp != null) {
                return synergyTemp;
            }
            Thread.sleep(EXPORT_POLL_MS);
        }
        Logger.logConsoleMessage("Export file not found in Synergy DOWNLOADS after "
                + (EXPORT_MAX_WAIT_MS / 1000) + "s for " + target.menuLabel);
        return null;
    }

    private static long elapsedSec(long startMs) {
        return (System.currentTimeMillis() - startMs) / 1000L;
    }

    private boolean clickExportToolbarDownloadLink() throws InterruptedException {
        if (!WaitUtil.isDisplayFast(homePage.exportToolbarDownloadLink(), 2)) {
            return false;
        }
        if (DriverUtil.clickOnElementJs(homePage.exportToolbarDownloadLink(), 0)) {
            Logger.logReportMessage("Clicked export download link in grid toolbar");
            Thread.sleep(NOTIFICATION_SETTLE_MS);
            return true;
        }
        return false;
    }

    /**
     * Chrome Downloads icon sits LEFT of the profile avatar on Synergy VM (1296px: ~88% not ~94%).
     * 94% hits profile and opens "Sign in to Chrome" — see PROD screenshots.
     */
    private boolean clickChromeDownloadsToolbar() {
        try {
            dismissChromeProfileMenuIfOpen();
            Dimension win = driver.get().browser().getWindowSize();
            if (win == null || win.width < 200) {
                return false;
            }
            // Downloads arrow — left of profile; profile ≈ 94%, downloads ≈ 86–88%
            int downloadX = (int) (win.width * 0.875);
            int downloadY = Math.max(48, (int) (win.height * 0.055));
            driver.get().browser().click(downloadX, downloadY);
            Logger.logReportMessage("Clicked Chrome Downloads icon (~" + downloadX + "," + downloadY
                    + ", window " + win.width + "x" + win.height + ")");
            Thread.sleep(1500);

            File imported = importFileFromChromeDownloadShelfOcr(downloadX, downloadY, win);
            if (imported != null) {
                Logger.logReportMessage("Imported export after opening Chrome Downloads shelf");
                return true;
            }

            // Shelf first row sits directly under the downloads icon
            int shelfX = downloadX;
            int shelfY = downloadY + 52;
            driver.get().browser().click(shelfX, shelfY);
            Logger.logReportMessage("Clicked first item in Chrome download shelf (~" + shelfX + "," + shelfY + ")");
            Thread.sleep(NOTIFICATION_SETTLE_MS);
            return true;
        } catch (Exception e) {
            Logger.logConsoleMessage("Chrome Downloads toolbar click failed: " + e.getMessage());
            return false;
        }
    }

    /** Close profile menu if a prior mis-click opened it. */
    private void dismissChromeProfileMenuIfOpen() {
        try {
            driver.get().browser().sendKeys(WebKey.ESCAPE);
            Thread.sleep(400);
        } catch (Exception ignored) {
            // optional
        }
    }

    /** OCR the download bubble (top-right) and import FulfillmentExport-*.xlsx from Synergy VM Downloads. */
    private File importFileFromChromeDownloadShelfOcr(int downloadX, int downloadY, Dimension win) {
        try {
            int ocrX = Math.max(0, downloadX - 280);
            int ocrY = downloadY - 5;
            int ocrW = Math.min(360, win.width - ocrX);
            int ocrH = Math.min(140, win.height - ocrY);
            String ocrText = driver.get().screen().getTextAsString(ocrX, ocrY, ocrW, ocrH);
            String filename = detectExportFilenameFromText(ocrText);
            if (filename.isEmpty()) {
                filename = detectExportFilenameViaOcr();
            }
            if (filename.isEmpty()) {
                Logger.logConsoleMessage("OCR did not find export filename in download shelf (text="
                        + truncateForLog(ocrText, 120) + ")");
                return null;
            }
            Logger.logReportMessage("OCR export filename in Chrome shelf: " + filename);
            File file = driver.get().fileSystem().importFile(FileSystemDirectory.DOWNLOADS, filename);
            if (file != null && file.exists() && file.length() > 0) {
                return file;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Chrome shelf OCR import failed: " + e.getMessage());
        }
        return null;
    }

    private static String truncateForLog(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String oneLine = text.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= maxLen ? oneLine : oneLine.substring(0, maxLen) + "...";
    }

    /** Ctrl+J opens chrome://downloads — scrape FulfillmentExport filename then return to app. */
    private File importExportFileByChromeDownloadsPage() {
        try {
            String beforeUrl = driver.get().browser().getCurrentUrl();
            driver.get().browser().sendKeys(WebKey.CONTROL, WebKey.getKeyFromUnicode('j'));
            Thread.sleep(2500);
            String filename = detectExportFilenameFromText(driver.get().screen().getPageSource());
            if (filename.isEmpty()) {
                filename = detectExportFilenameOnPage();
            }
            if (!beforeUrl.equals(driver.get().browser().getCurrentUrl())) {
                driver.get().browser().goBack();
                Thread.sleep(1500);
            }
            if (filename.isEmpty()) {
                return null;
            }
            File file = driver.get().fileSystem().importFile(FileSystemDirectory.DOWNLOADS, filename);
            if (file != null && file.exists() && file.length() > 0) {
                Logger.logReportMessage("Imported export from Chrome downloads page: " + filename);
                return file;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Chrome downloads page import failed: " + e.getMessage());
        }
        return null;
    }

    private String detectExportFilenameFromText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String last = "";
        Matcher fulfillment = FULFILLMENT_EXPORT_PATTERN.matcher(text);
        while (fulfillment.find()) {
            last = fulfillment.group();
        }
        if (!last.isEmpty()) {
            return last;
        }
        Matcher any = ANY_XLSX_PATTERN.matcher(text);
        while (any.find()) {
            String candidate = any.group().trim();
            if (candidate.length() > 5 && !candidate.toLowerCase(Locale.ROOT).contains("template")) {
                last = candidate;
            }
        }
        return last;
    }

    private String detectExportFilenameViaOcr() {
        try {
            Dimension size = driver.get().screen().getSize();
            if (size == null || size.width < 200) {
                return "";
            }
            int x = (int) (size.width * 0.5);
            int y = 0;
            int w = (int) (size.width * 0.5);
            int h = Math.max(80, (int) (size.height * 0.18));
            String ocrText = driver.get().screen().getTextAsString(x, y, w, h);
            String detected = detectExportFilenameFromText(ocrText);
            if (!detected.isEmpty()) {
                Logger.logReportMessage("OCR detected export filename: " + detected);
            }
            return detected;
        } catch (Exception e) {
            Logger.logConsoleMessage("OCR export filename scan failed: " + e.getMessage());
            return "";
        }
    }

    private File importExportFileFromPageSourceScan(long exportStartedAtMs) {
        Set<String> names = collectXlsxFilenamesFromPage();
        String ocrName = detectExportFilenameViaOcr();
        if (!ocrName.isEmpty()) {
            names.add(ocrName);
        }
        for (String name : names) {
            try {
                File file = driver.get().fileSystem().importFile(FileSystemDirectory.DOWNLOADS, name);
                if (file != null && file.exists() && file.length() > 0) {
                    Logger.logReportMessage("Imported export from page/OCR scan: " + name);
                    return file;
                }
            } catch (Exception ignored) {
                // try next name
            }
        }
        return null;
    }

    private Set<String> collectXlsxFilenamesFromPage() {
        Set<String> names = new LinkedHashSet<>();
        names.addAll(extractXlsxNamesFromText(
                driver.get().screen() != null ? driver.get().screen().getPageSource() : ""));
        try {
            Object hrefs = driver.get().browser().executeScript(
                    "(function() {"
                            + "var out = [];"
                            + "var nodes = document.querySelectorAll("
                            + "'a[href*=\".xlsx\"], a[download], [download*=\".xlsx\"], button');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  var t = nodes[i].getAttribute('download') || nodes[i].getAttribute('href') || "
                            + "nodes[i].innerText || nodes[i].textContent || '';"
                            + "  if (t.indexOf('.xlsx') >= 0) out.push(t);"
                            + "}"
                            + "return out.join('\\n');"
                            + "})();");
            if (hrefs != null) {
                names.addAll(extractXlsxNamesFromText(String.valueOf(hrefs)));
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scrape xlsx hrefs from DOM: " + e.getMessage());
        }
        return names;
    }

    private Set<String> extractXlsxNamesFromText(String text) {
        Set<String> names = new LinkedHashSet<>();
        if (text == null || text.isEmpty()) {
            return names;
        }
        Matcher fulfillment = FULFILLMENT_EXPORT_PATTERN.matcher(text);
        while (fulfillment.find()) {
            names.add(sanitizeXlsxFilename(fulfillment.group()));
        }
        Matcher any = ANY_XLSX_PATTERN.matcher(text);
        while (any.find()) {
            names.add(sanitizeXlsxFilename(any.group()));
        }
        return names;
    }

    private static String sanitizeXlsxFilename(String raw) {
        if (raw == null) {
            return "";
        }
        String name = raw.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        int q = name.indexOf('?');
        if (q > 0) {
            name = name.substring(0, q);
        }
        return name.trim();
    }

    /**
     * After export completes: in-app notification, toast, or Chrome download tray (PROD Synergy).
     */
    private boolean clickExportReadyNotification() throws InterruptedException {
        if (WaitUtil.isDisplayFast(homePage.exportCompleteNotification(), 2)) {
            if (DriverUtil.clickOnElementJs(homePage.exportCompleteNotification(), 0)) {
                Logger.logReportMessage("Clicked export-complete in-app notification");
                Thread.sleep(NOTIFICATION_SETTLE_MS);
                return true;
            }
        }
        if (WaitUtil.isDisplayFast(homePage.exportReadyToastAction(), 2)) {
            if (DriverUtil.clickOnElementJs(homePage.exportReadyToastAction(), 0)) {
                Logger.logReportMessage("Clicked export-ready toast action (download link/button)");
                Thread.sleep(NOTIFICATION_SETTLE_MS);
                return true;
            }
        }
        if (WaitUtil.isDisplayFast(homePage.exportReadyToast(), 2)) {
            if (DriverUtil.clickOnElementJs(homePage.exportReadyToast(), 0)) {
                Logger.logReportMessage("Clicked export-ready toast notification");
                Thread.sleep(NOTIFICATION_SETTLE_MS);
                return true;
            }
        }
        if (WaitUtil.isDisplayFast(homePage.exportNotificationBell(), 2)) {
            DriverUtil.clickOnElementJs(homePage.exportNotificationBell(), 0);
            Thread.sleep(800);
            if (WaitUtil.isDisplayFast(homePage.exportReadyNotificationItem(), 3)) {
                if (DriverUtil.clickOnElementJs(homePage.exportReadyNotificationItem(), 0)) {
                    Logger.logReportMessage("Clicked export item in notification bell panel");
                    Thread.sleep(NOTIFICATION_SETTLE_MS);
                    return true;
                }
            }
        }
        if (WaitUtil.isDisplayFast(homePage.exportReadyNotificationItem(), 1)) {
            if (DriverUtil.clickOnElementJs(homePage.exportReadyNotificationItem(), 0)) {
                Logger.logReportMessage("Clicked export-ready notification item");
                Thread.sleep(NOTIFICATION_SETTLE_MS);
                return true;
            }
        }
        return false;
    }

    /** Parse FulfillmentExport-*.xlsx from page text (toast/notification) and import from Synergy Downloads. */
    private File importExportFileByDetectedFilename(long exportStartedAtMs) {
        Set<String> names = collectXlsxFilenamesFromPage();
        String detected = detectExportFilenameOnPage();
        if (detected != null && !detected.trim().isEmpty()) {
            names.add(detected.trim());
        }
        String ocrName = detectExportFilenameViaOcr();
        if (!ocrName.isEmpty()) {
            names.add(ocrName);
        }
        for (String name : names) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            try {
                File file = driver.get().fileSystem().importFile(FileSystemDirectory.DOWNLOADS, name);
                if (file != null && file.exists() && file.length() > 0) {
                    Logger.logReportMessage("Imported export via notification filename: " + name);
                    return file;
                }
            } catch (Exception e) {
                Logger.logConsoleMessage("importFile failed for detected name '" + name + "': " + e.getMessage());
            }
        }
        return null;
    }

    private String detectExportFilenameOnPage() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var re = /FulfillmentExport[-0-9]+\\.xlsx/gi;"
                            + "var bodyText = (document.body && document.body.innerText) || '';"
                            + "var matches = bodyText.match(re);"
                            + "if (matches && matches.length) return matches[matches.length - 1];"
                            + "var nodes = document.querySelectorAll("
                            + "'a[href*=\".xlsx\"], [download*=\".xlsx\"], div[class*=\"toast\"], "
                            + "div[class*=\"notification\"], *[class*=\"export\"]');"
                            + "for (var i = nodes.length - 1; i >= 0; i--) {"
                            + "  var t = nodes[i].getAttribute('download') || nodes[i].getAttribute('href') || "
                            + "nodes[i].innerText || '';"
                            + "  var m = t.match(re);"
                            + "  if (m) return m[m.length - 1];"
                            + "}"
                            + "return '';");
            if (result != null) {
                String name = String.valueOf(result).trim();
                if (!name.isEmpty() && !"undefined".equals(name) && !"null".equals(name)) {
                    return name;
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not detect export filename on page: " + e.getMessage());
        }
        return detectExportFilenameFromText(
                driver.get().screen() != null ? driver.get().screen().getPageSource() : "");
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
        names.addAll(collectXlsxFilenamesFromPage());
        return names;
    }

    /** Finds newest FulfillmentExport-*.xlsx created after exportStartedAtMs. */
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
     * Scans for newest FulfillmentExport-*.xlsx (or target candidates) created after export started.
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
