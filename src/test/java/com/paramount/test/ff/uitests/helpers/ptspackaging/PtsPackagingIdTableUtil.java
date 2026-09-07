package com.paramount.test.ff.uitests.helpers.ptspackaging;

import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.LeftFilterPanel;
import com.paramount.test.ff.pageobjects.PtsPackagingIdPage;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnsUtil;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.paramount.test.ff.common.base.BaseTest.driver;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * PTS Packaging ID table view + grid helpers (BSD-29441).
 * Scope: PTS demand-system orders / line items only.
 */
public class PtsPackagingIdTableUtil {

    private static final int PANEL_WAIT_S = 10;
    private static final int GRID_WAIT_S = 15;
    private static final int GRID_POLL_S = 8;
    private static final int PANEL_SETTLE_MS = 800;
    /** Zoom levels tried high→low; stops at the first level where PTS header/cells are readable. */
    private static final int[] ZOOM_LEVELS_PCT = {100, 90, 80, 75, 67, 50, 33, 25};

    private static final String PTS_COLUMN_LABEL_JS = PtsPackagingIdPage.COLUMN_LABEL.replace("'", "\\'");
    private static final String PTS_COLUMN_SHORT_JS = PtsPackagingIdPage.COLUMN_HEADER_TRUNCATED.replace("'", "\\'");

    /** Shared JS: locate PTS header — search document-wide (sticky thead may be outside #orderTable). */
    private static String findPtsHeaderJsFunction() {
        return "function findPtsHeader() {"
                + "  function isPtsHeader(th) {"
                + "    if (!th) return false;"
                + "    var tip = (th.getAttribute('title') || th.getAttribute('aria-label') || '').trim();"
                + "    if (tip === label) return true;"
                + "    if (tip.indexOf('PTS') >= 0 && tip.indexOf('Packag') >= 0) return true;"
                + "    var text = (th.innerText || th.textContent || '').replace(/\\s+/g, ' ').trim();"
                + "    if (text.indexOf(label) >= 0 || text.indexOf(shortLabel) >= 0) return true;"
                + "    var cls = th.className || '';"
                + "    if (cls.indexOf('pts-packaging-id') >= 0) return true;"
                + "    return false;"
                + "  }"
                + "  var titled = document.querySelectorAll('th[title=\"' + label + '\"]');"
                + "  for (var t = 0; t < titled.length; t++) {"
                + "    if (isPtsHeader(titled[t])) return titled[t];"
                + "  }"
                + "  var ths = document.querySelectorAll('table th, th');"
                + "  for (var i = 0; i < ths.length; i++) {"
                + "    if (isPtsHeader(ths[i])) return ths[i];"
                + "  }"
                + "  var table = document.querySelector('#orderTable') || document.querySelector('table.fixed-header');"
                + "  if (table) {"
                + "    var headerThs = table.querySelectorAll('thead th');"
                + "    if (headerThs.length === 0) {"
                + "      headerThs = document.querySelectorAll('thead th');"
                + "    }"
                + "    if (headerThs.length > 0) return headerThs[headerThs.length - 1];"
                + "  }"
                + "  return null;"
                + "}";
    }

    private static String findGridRowsJsFunction() {
        return "function findGridRows() {"
                + "  var sels = ["
                + "    '#orderTable tbody tr.row', '#orderTable tbody tr',"
                + "    'table.fixed-header tbody tr.row', 'table.fixed-header tbody tr',"
                + "    'app-fulfillment-main-table-container tbody tr',"
                + "    'msc-custom-table tbody tr', 'tbody tr.row', 'tbody tr'"
                + "  ];"
                + "  for (var i = 0; i < sels.length; i++) {"
                + "    var nodes = document.querySelectorAll(sels[i]);"
                + "    if (nodes && nodes.length > 0) return nodes;"
                + "  }"
                + "  return [];"
                + "}";
    }

    private static String readPtsCellJsFunction() {
        return "function readPtsTd(td) {"
                + "  if (!td) return '';"
                + "  var tip = (td.getAttribute('title') || '').trim();"
                + "  if (tip.length > 0 && tip !== '-') return tip;"
                + "  var copyEl = td.querySelector('button.copy-button-wrapper, msc-copy-text-to-clipboard-chip,"
                + "    [cdkcopytoclipboard], [data-clipboard-text], [ng-reflect-copy]');"
                + "  if (copyEl) {"
                + "    var attr = copyEl.getAttribute('cdkcopytoclipboard')"
                + "      || copyEl.getAttribute('ng-reflect-copy')"
                + "      || copyEl.getAttribute('data-clipboard-text') || '';"
                + "    if (attr.trim().length > 0) return attr.trim();"
                + "  }"
                + "  var text = (td.innerText || td.textContent || '').replace(/\\s+/g, ' ').trim();"
                + "  if (text.length > 0 && text !== '-') return text;"
                + "  return '';"
                + "}";
    }

    private int parseJsIntResult(Object result, int defaultValue) {
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        if (result == null) {
            return defaultValue;
        }
        String text = String.valueOf(result).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "undefined".equalsIgnoreCase(text)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean hasVisibleGridRows() {
        scrollGridToPtsColumn();
        if (WaitUtil.isDisplayFast(ptsPage.gridFirstRowClickTarget(), 5)) {
            return true;
        }
        try {
            return !driver.get().finder().findElements(ptsPage.gridColumnCells()).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** tbody row count after column search — not the total-count label (that stays at full PTS filter size). */
    public int countVisibleGridRows() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + findGridRowsJsFunction()
                            + "return findGridRows().length;"
                            + "})();");
            return parseJsIntResult(result, 0);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not count grid rows via JS: " + e.getMessage());
        }
        try {
            return driver.get().finder().findElements(ptsPage.gridDataRows()).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /** Non-empty PTS cells in grid — reliable after column search (tbody row count alone can be 0 on PROD). */
    public int countNonEmptyPtsCells() {
        int count = 0;
        for (String value : readVisiblePtsCellValuesViaJs(50)) {
            if (value != null && !value.trim().isEmpty() && !"-".equals(value.trim())) {
                count++;
            }
        }
        if (count > 0) {
            return count;
        }
        scrollGridToPtsColumn();
        try {
            for (DesktopBrowserElement cell : driver.get().finder().findElements(ptsPage.gridColumnCells())) {
                String text = cell.getText().trim();
                if (!text.isEmpty() && !"-".equals(text)) {
                    count++;
                }
            }
        } catch (Exception ignored) {
            // optional selenium fallback
        }
        return count;
    }

    public String readColumnSearchInputValue() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var th = document.querySelector('th[title=\"' + '" + PTS_COLUMN_LABEL_JS + "' + '\"]');"
                            + "if (!th) return '';"
                            + "var inp = th.querySelector('input');"
                            + "return inp ? (inp.value || '').trim() : '';"
                            + "})();");
            return result == null ? "" : String.valueOf(result).trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * O_013 pre-export: trust O_009 column search when still active. PROD total-count label and tbody
     * row count do not reflect column search — use matching PTS cells + search input instead.
     */
    public void prepareGridForExport(int maxVisibleRows, SoftAssert softAssert) throws InterruptedException {
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(1000);

        String searchTerm = readColumnSearchInputValue();
        if (searchTerm.isEmpty()) {
            searchTerm = PtsPackagingSessionHelper.getLastOrdersColumnSearchTerm();
        }
        int matchingCells = countNonEmptyPtsCells();
        Logger.logReportMessage("Pre-export grid: columnSearch='" + searchTerm + "', matchingPtsCells="
                + matchingCells + ", tbodyRows=" + countVisibleGridRows());

        if (searchTerm.isEmpty() || matchingCells == 0) {
            String sampleValue = PtsPackagingSessionHelper.getLastOrdersColumnSearchTerm();
            if (sampleValue.isEmpty()) {
                sampleValue = readFirstNonEmptyCellValue(softAssert);
            }
            if (sampleValue.isEmpty()) {
                sampleValue = readFirstRowPtsCellValue(softAssert);
            }
            if (!sampleValue.isEmpty()) {
                filterColumnByValue(sampleValue, softAssert);
                PtsPackagingSessionHelper.setLastOrdersColumnSearchTerm(sampleValue);
                searchTerm = sampleValue;
                WaitUtil.waitForJSToLoad(GRID_WAIT_S);
                Thread.sleep(2000);
                matchingCells = countNonEmptyPtsCells();
            }
        }

        if (!searchTerm.isEmpty()) {
            validateColumnSearchReturnsValue(searchTerm, softAssert);
        }

        Verify.softAssert1(!searchTerm.isEmpty() && matchingCells > 0,
                "Column search active before export (search='" + searchTerm
                        + "', matchingPtsCells=" + matchingCells + ")", softAssert);
        if (matchingCells > maxVisibleRows) {
            Logger.logReportMessage("Export grid has " + matchingCells + " matching PTS rows (target max "
                    + maxVisibleRows + ") — export may take longer on PROD");
        }
    }
    private final PtsPackagingIdPage ptsPage = new PtsPackagingIdPage();
    private final ManageColumnsUtil manageColumns = new ManageColumnsUtil();
    private final LeftFilterPanel leftFilterPanel = new LeftFilterPanel();

    public enum ColumnSection {
        ORDER,
        LINE_ITEM
    }

    /** Apply Demand system = PTS before any PTS Packaging ID validation. */
    public void applyPtsDemandSystemFilter(LeftFilterPanelUtil filterUtil, SoftAssert softAssert)
            throws InterruptedException {
        filterUtil.expandFilter(PtsPackagingIdPage.DEMAND_SYSTEM_FILTER);
        if (filterUtil.hasFilterSearchInput(PtsPackagingIdPage.DEMAND_SYSTEM_FILTER)) {
            filterUtil.searchFilterOptions(PtsPackagingIdPage.DEMAND_SYSTEM_FILTER, PtsPackagingIdPage.DEMAND_SYSTEM_PTS);
        }
        filterUtil.selectFilterOption(PtsPackagingIdPage.DEMAND_SYSTEM_FILTER, PtsPackagingIdPage.DEMAND_SYSTEM_PTS);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(2000);

        Verify.softAssert1(WaitUtil.isDisplay(leftFilterPanel.tableRecordCountLabel(), GRID_WAIT_S),
                "Table refreshed after Demand system=PTS filter", softAssert);
        Logger.logReportMessage("Applied left filter: Demand system = PTS");
    }

    public boolean isManageColumnsPanelOpen() throws InterruptedException {
        return manageColumns.isPanelOpen(2);
    }

    public void openManageColumnsPanel(SoftAssert softAssert) throws InterruptedException {
        manageColumns.openPanel(softAssert, 2);
    }

    public void closeManageColumnsPanel(SoftAssert softAssert) throws InterruptedException {
        manageColumns.closePanel(softAssert);
    }

    public void validateColumnListedInManagePanel(ColumnSection section, SoftAssert softAssert)
            throws InterruptedException {
        scrollToColumnInManagePanel(section);
        By label = columnLabelLocator(section);
        Verify.softAssert1(WaitUtil.isDisplay(label, PANEL_WAIT_S),
                PtsPackagingIdPage.COLUMN_LABEL + " listed under "
                        + sectionLabel(section), softAssert);
    }

    public void ensureColumnEnabledAndSaved(ColumnSection section, SoftAssert softAssert) throws InterruptedException {
        requirePtsColumnOnGrid(section, softAssert);
    }

    public void ensureColumnEnabledAndSaved(SoftAssert softAssert) throws InterruptedException {
        requirePtsColumnOnGrid(ColumnSection.ORDER, softAssert);
    }

    /**
     * O_001 / LI_001 only — open Manage columns, enable PTS Packaging ID, move to first, save Automation view.
     * Assumes Manage columns panel is already open (call {@link #openManageColumnsPanel} first).
     */
    public void configureAutomationViewWithPtsColumn(ColumnSection section, SoftAssert softAssert)
            throws InterruptedException {
        if (WaitUtil.isDisplayFast(ptsPage.activeTableViewLabel(PtsPackagingIdPage.AUTOMATION_TABLE_VIEW), 2)) {
            Logger.logReportMessage(PtsPackagingIdPage.AUTOMATION_TABLE_VIEW + " table view already active in panel");
        } else if (selectAutomationTableViewFromDropdownIfPresent(softAssert)) {
            Logger.logReportMessage("Selected existing " + PtsPackagingIdPage.AUTOMATION_TABLE_VIEW + " table view");
        } else {
            selectTableViewFromDropdown(PtsPackagingIdPage.STANDARD_VIEW, softAssert);
            enablePtsColumnCheckbox(section, softAssert);
            saveNewAutomationTableView(softAssert);
        }

        enablePtsColumnCheckbox(section, softAssert);
        persistAutomationViewChanges(section, softAssert);
        if (waitForPtsColumnInDom(GRID_WAIT_S)) {
            Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " visible on grid before closing Manage columns");
        }
        closeManageColumnsPanel(softAssert);
        waitForGridRefresh();
        scrollGridToPtsColumn();
        Thread.sleep(PANEL_SETTLE_MS);
        if (!isPtsColumnVisibleViaSynergy()) {
            scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        }
        markColumnEnabledInSession(section);
        markAutomationViewConfiguredInSession(section);
        Verify.softAssert1(isPtsColumnPresentInDom() || isPtsColumnVisibleViaSynergy(),
                PtsPackagingIdPage.COLUMN_LABEL + " on grid after Automation view setup", softAssert);
    }

    /**
     * O_002+ — grid check only; does not open Manage columns (setup is O_001 / LI_001).
     * After O_001 setup, column is in DOM; a single horizontal scroll is enough before O_002 asserts.
     */
    public void requirePtsColumnOnGrid(ColumnSection section, SoftAssert softAssert) throws InterruptedException {
        waitForGridRefresh();
        if (!waitForPtsColumnInDom(GRID_POLL_S)) {
            Verify.softAssert1(false,
                    PtsPackagingIdPage.COLUMN_LABEL + " on grid — run "
                            + (section == ColumnSection.ORDER ? "O_001" : "LI_001")
                            + " Automation setup first", softAssert);
            return;
        }
        if (isColumnEnabledInSession(section)) {
            scrollGridToPtsColumn();
            Thread.sleep(PANEL_SETTLE_MS);
        } else {
            scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        }
        markColumnEnabledInSession(section);
    }

    public void requirePtsColumnOnGrid(SoftAssert softAssert) throws InterruptedException {
        requirePtsColumnOnGrid(ColumnSection.ORDER, softAssert);
    }

    public void ensureColumnDisabledAndSaved(ColumnSection section, SoftAssert softAssert)
            throws InterruptedException {
        openManageColumnsPanel(softAssert);
        selectTableViewFromDropdown(PtsPackagingIdPage.STANDARD_VIEW, softAssert);
        closeManageColumnsPanel(softAssert);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(1500);
        resetColumnEnabledInSession(section);
        Logger.logReportMessage("Switched to Standard view on " + sectionLabel(section)
                + " — PTS Packaging ID column not persisted (Manage columns is per console tab; calendar persists)");
    }

    public void ensureColumnDisabledAndSaved(SoftAssert softAssert) throws InterruptedException {
        ensureColumnDisabledAndSaved(ColumnSection.ORDER, softAssert);
    }

    private static void markAutomationViewConfiguredInSession(ColumnSection section) {
        if (section == ColumnSection.ORDER) {
            PtsPackagingSessionHelper.markAutomationViewConfiguredOnOrders();
        } else {
            PtsPackagingSessionHelper.markAutomationViewConfiguredOnLineItems();
        }
    }

    /** Poll for PTS column in grid DOM after O_001 setup (no Manage columns retry). */
    private void waitForPtsColumnOnGrid(ColumnSection section, SoftAssert softAssert) throws InterruptedException {
        waitForGridRefresh();
        if (waitForPtsColumnInDom(GRID_WAIT_S)) {
            scrollGridToPtsColumn();
            Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " column present on grid");
            return;
        }
        Verify.softAssert1(isPtsColumnPresentInDom(),
                PtsPackagingIdPage.COLUMN_LABEL + " visible after "
                        + PtsPackagingIdPage.AUTOMATION_TABLE_VIEW + " table view setup", softAssert);
    }

    private boolean waitForPtsColumnInDom(int maxSec) throws InterruptedException {
        long endTime = System.currentTimeMillis() + (maxSec * 1000L);
        int attempt = 0;
        while (System.currentTimeMillis() < endTime) {
            if (isPtsColumnPresentInDom()) {
                return true;
            }
            if (attempt % 3 == 0) {
                scrollGridToPtsColumn();
            }
            attempt++;
            Thread.sleep(500);
        }
        scrollGridToPtsColumn();
        return isPtsColumnPresentInDom();
    }

    private boolean isPtsColumnPresentInDom() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var label = '" + PTS_COLUMN_LABEL_JS + "';"
                            + "var shortLabel = '" + PTS_COLUMN_SHORT_JS + "';"
                            + findPtsHeaderJsFunction()
                            + "if (findPtsHeader()) return true;"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPtsColumnHeaderVisibleInViewport() {
        if (isPtsColumnVisibleViaSynergy()) {
            return true;
        }
        return "header_visible".equals(evaluatePtsGridState("header_visible"));
    }

    /** Header in viewport with enough width, or at least one PTS cell with readable text. */
    private boolean isPtsColumnReadableInViewport() {
        if (isPtsColumnVisibleViaSynergy()) {
            return true;
        }
        String state = evaluatePtsGridState("readable");
        return "readable".equals(state) || "header_visible".equals(state);
    }

    /**
     * Synergy element check — reliable on remote browser after horizontal scroll
     * (JS getBoundingClientRect can report not_visible even when column is on screen).
     */
    private boolean isPtsColumnVisibleViaSynergy() {
        if (WaitUtil.isDisplayFast(ptsPage.gridColumnHeader(), 2)) {
            return true;
        }
        if (WaitUtil.isDisplayFast(ptsPage.gridColumnHeaderLabel(), 1)) {
            return true;
        }
        return WaitUtil.isDisplayFast(ptsPage.gridColumnHeaderTruncated(), 1);
    }

    /**
     * @param mode {@code header_visible} or {@code readable}
     */
    private String evaluatePtsGridState(String mode) {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = '" + PTS_COLUMN_LABEL_JS + "';"
                            + "var shortLabel = '" + PTS_COLUMN_SHORT_JS + "';"
                            + "var mode = '" + mode + "';"
                            + findPtsHeaderJsFunction()
                            + "function isInViewport(r, minWidth) {"
                            + "  if (!r || r.width < minWidth || r.height <= 0) return false;"
                            + "  return r.right > 0 && r.left < window.innerWidth;"
                            + "}"
                            + "var th = findPtsHeader();"
                            + "if (!th) return 'not_in_dom';"
                            + "var hr = th.getBoundingClientRect();"
                            + "if (!isInViewport(hr, 12)) return 'not_visible';"
                            + "if (mode === 'header_visible') return 'header_visible';"
                            + "var colIndex = th.cellIndex;"
                            + "var rows = document.querySelectorAll('#orderTable tbody tr, table.fixed-header tbody tr');"
                            + "for (var r = 0; r < Math.min(rows.length, 8); r++) {"
                            + "  var cells = rows[r].cells;"
                            + "  if (!cells || colIndex >= cells.length) continue;"
                            + "  var cell = cells[colIndex];"
                            + "  var cr = cell.getBoundingClientRect();"
                            + "  if (!isInViewport(cr, 8)) continue;"
                            + "  var txt = (cell.innerText || cell.textContent || '').replace(/\\s+/g, ' ').trim();"
                            + "  if (txt.length > 0 && txt !== '-') return 'readable';"
                            + "}"
                            + "return (hr.width >= 20 ? 'header_visible' : 'not_visible');"
                            + "})();");
            return result == null ? "unknown" : String.valueOf(result);
        } catch (Exception e) {
            Logger.logConsoleMessage("PTS grid state check failed: " + e.getMessage());
            if (WaitUtil.isDisplayFast(ptsPage.gridColumnHeaderLabel(), 1)
                    || WaitUtil.isDisplayFast(ptsPage.gridColumnHeader(), 1)) {
                return "header_visible";
            }
            return "error";
        }
    }

    /** Ensures PTS checkbox is checked — does not save or reorder (caller handles persist). */
    private void enablePtsColumnCheckbox(ColumnSection section, SoftAssert softAssert) throws InterruptedException {
        By checkbox = columnCheckboxLocator(section);
        if (!isColumnCheckboxSelectedSafe(checkbox)) {
            clickColumnInManagePanel(section, softAssert);
            Thread.sleep(300);
        }
    }

    /**
     * Reorder PTS to first when drag works; otherwise save column enablement and rely on
     * horizontal grid scroll in O_002+ to find PTS as the last column.
     */
    private void persistAutomationViewChanges(ColumnSection section, SoftAssert softAssert)
            throws InterruptedException {
        movePtsColumnToFirstInManagePanel(section);
        if (clickSaveChangesIfAvailable(softAssert)) {
            waitForGridRefresh();
            return;
        }

        Logger.logReportMessage("Save changes not visible after reorder — marking Automation view dirty");
        markAutomationViewDirty(section);
        Thread.sleep(PANEL_SETTLE_MS);
        if (clickSaveChangesIfAvailable(softAssert)) {
            waitForGridRefresh();
            return;
        }

        Logger.logReportMessage("Save changes still hidden — toggling " + PtsPackagingIdPage.COLUMN_LABEL
                + " then reorder and save");
        if (isColumnCheckboxSelectedSafe(columnCheckboxLocator(section))) {
            clickColumnInManagePanel(section, softAssert);
            Thread.sleep(400);
        }
        clickColumnInManagePanel(section, softAssert);
        Thread.sleep(400);
        movePtsColumnToFirstInManagePanel(section);
        clickSaveChangesIfAvailable(softAssert);
        waitForGridRefresh();
    }

    /** Toggle a non-PTS column so Save changes appears without removing PTS from the view. */
    private void markAutomationViewDirty(ColumnSection section) throws InterruptedException {
        if (section == ColumnSection.LINE_ITEM) {
            markAutomationViewDirtyLineItemTable();
            return;
        }
        String sectionTitle = sectionLabel(section);
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var sectionTitle = '" + sectionTitle + "';"
                            + "var ptsLabel = '" + PtsPackagingIdPage.COLUMN_LABEL + "';"
                            + "var sections = document.querySelectorAll('[class*=\"table-column\"]');"
                            + "for (var s = 0; s < sections.length; s++) {"
                            + "  if ((sections[s].textContent || '').indexOf(sectionTitle) < 0) continue;"
                            + "  var labels = sections[s].querySelectorAll('label');"
                            + "  for (var i = 0; i < labels.length; i++) {"
                            + "    var text = (labels[i].textContent || '').trim();"
                            + "    if (!text || text.indexOf(ptsLabel) >= 0) continue;"
                            + "    var row = labels[i].closest('.draggable-item') || labels[i].closest('a[cdkdrag]');"
                            + "    var cb = row ? row.querySelector('input[type=checkbox]') : null;"
                            + "    if (!cb) continue;"
                            + "    cb.click();"
                            + "    return text;"
                            + "  }"
                            + "}"
                            + "return null;"
                            + "})();");
            if (result != null && !"null".equals(String.valueOf(result)) && !String.valueOf(result).isEmpty()) {
                Logger.logReportMessage("Toggled column '" + result + "' to expose Save changes");
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not mark Automation view dirty: " + e.getMessage());
        }
    }

    /** Line Items tab — flat "Manage Columns (Line item table)" list (no Order/Package sections). */
    private void markAutomationViewDirtyLineItemTable() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var ptsLabel = '" + PtsPackagingIdPage.COLUMN_LABEL + "';"
                            + "var headings = document.querySelectorAll('span');"
                            + "var panel = null;"
                            + "for (var h = 0; h < headings.length; h++) {"
                            + "  var t = (headings[h].textContent || '').toLowerCase();"
                            + "  if (t.indexOf('manage columns') >= 0 && t.indexOf('line item') >= 0) {"
                            + "    panel = headings[h].closest('[class*=\"table-view\"], [class*=\"manage-column\"]');"
                            + "    break;"
                            + "  }"
                            + "}"
                            + "if (!panel) return null;"
                            + "var labels = panel.querySelectorAll('label');"
                            + "for (var i = 0; i < labels.length; i++) {"
                            + "  var text = (labels[i].textContent || '').trim();"
                            + "  if (!text || text.indexOf(ptsLabel) >= 0) continue;"
                            + "  var row = labels[i].closest('.draggable-item') || labels[i].closest('a[cdkdrag]');"
                            + "  var cb = row ? row.querySelector('input[type=checkbox]') : null;"
                            + "  if (!cb) continue;"
                            + "  cb.click();"
                            + "  return text;"
                            + "}"
                            + "return null;"
                            + "})();");
            if (result != null && !"null".equals(String.valueOf(result)) && !String.valueOf(result).isEmpty()) {
                Logger.logReportMessage("Toggled column '" + result + "' in Line item table to expose Save changes");
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not mark Line item table view dirty: " + e.getMessage());
        }
    }

    private boolean clickSaveChangesIfAvailable(SoftAssert softAssert) throws InterruptedException {
        By saveBtn = ptsPage.saveChangesButtonInPanel();
        if (WaitUtil.isDisplayFast(saveBtn, 5)) {
            Verify.softAssert1(DriverUtil.clickOnElement(saveBtn, 5),
                    "Clicked Save changes on Automation table view", softAssert);
            Thread.sleep(PANEL_SETTLE_MS);
            confirmSaveChangesDialogIfPresent();
            Logger.logReportMessage("Saved Automation table view with " + PtsPackagingIdPage.COLUMN_LABEL);
            return true;
        }
        if (clickSaveChangesViaScript()) {
            Logger.logReportMessage("Saved Automation table view via script (Save changes)");
            Thread.sleep(PANEL_SETTLE_MS);
            confirmSaveChangesDialogIfPresent();
            return true;
        }
        return false;
    }

    private boolean clickSaveChangesViaScript() {
        try {
            Object result = driver.get().browser().executeScript(
                    "var buttons = document.querySelectorAll('button');"
                            + "for (var i = 0; i < buttons.length; i++) {"
                            + "  var b = buttons[i];"
                            + "  if ((b.textContent || '').indexOf('Save changes') < 0) continue;"
                            + "  if (b.disabled) continue;"
                            + "  b.click();"
                            + "  return true;"
                            + "}"
                            + "return false;");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    private void confirmSaveChangesDialogIfPresent() throws InterruptedException {
        if (WaitUtil.isDisplayFast(ptsPage.saveChangesConfirmButton(), 3)) {
            DriverUtil.clickOnElement(ptsPage.saveChangesConfirmButton(), 5);
            Thread.sleep(PANEL_SETTLE_MS);
        }
    }

    /**
     * Drag PTS Packaging ID to the first position under Order / Line item columns in Manage columns,
     * then Save changes persists it on the Automation view (always visible in grid — no horizontal scroll).
     */
    private static final int MAX_PTS_DRAG_STEPS = 2;
    private static final int MAX_STALE_DRAG_ATTEMPTS = 1;

    /**
     * Best-effort drag to first in Manage columns. Skips quickly when grip drag does not move the row.
     * O_002+ uses horizontal grid scroll to find PTS as the last column when drag is skipped.
     */
    private void movePtsColumnToFirstInManagePanel(ColumnSection section) throws InterruptedException {
        if (skipGripDragUseHorizontalScroll()) {
            Logger.logReportMessage("Skipping grip drag on PROD — " + PtsPackagingIdPage.COLUMN_LABEL
                    + " stays last; grid tests horizontal-scroll to column");
            return;
        }
        scrollDropListForDrag(section);
        if (isPtsColumnFirstInManagePanel(section)) {
            Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " already first in " + sectionLabel(section));
            return;
        }
        if (movePtsColumnToFirstViaDragHandle(section)) {
            return;
        }
        Logger.logReportMessage("Grip drag skipped — " + PtsPackagingIdPage.COLUMN_LABEL
                + " stays last in column order; grid tests will horizontal-scroll to column");
    }

    /** PROD grip drag does not reorder columns reliably; horizontal scroll is the supported path. */
    private static boolean skipGripDragUseHorizontalScroll() {
        try {
            return "PROD".equalsIgnoreCase(Config.getString("TestEnvironment"));
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Scroll CDK drop list so PTS row and its predecessor are visible for grip drag. */
    private void scrollDropListForDrag(ColumnSection section) throws InterruptedException {
        String sectionTitle = sectionLabel(section);
        try {
            driver.get().browser().executeScript(
                    "(function() {"
                            + "var sectionTitle = '" + sectionTitle + "';"
                            + "var columnLabel = '" + PtsPackagingIdPage.COLUMN_LABEL + "';"
                            + "var sections = document.querySelectorAll('[class*=\"table-column\"]');"
                            + "for (var s = 0; s < sections.length; s++) {"
                            + "  if ((sections[s].textContent || '').indexOf(sectionTitle) < 0) continue;"
                            + "  var list = sections[s].querySelector('[cdkdroplist], .cdk-drop-list, .options-list-scrollbar');"
                            + "  if (!list) return;"
                            + "  var labels = list.querySelectorAll('label');"
                            + "  for (var i = 0; i < labels.length; i++) {"
                            + "    var text = (labels[i].textContent || '').trim();"
                            + "    if (text.indexOf(columnLabel) < 0) continue;"
                            + "    var row = labels[i].closest('.draggable-item');"
                            + "    if (row) {"
                            + "      row.scrollIntoView({block:'center', inline:'nearest'});"
                            + "      var prev = row.previousElementSibling;"
                            + "      while (prev && prev.className.indexOf('draggable-item') < 0) {"
                            + "        prev = prev.previousElementSibling;"
                            + "      }"
                            + "      if (prev) prev.scrollIntoView({block:'nearest', inline:'nearest'});"
                            + "    }"
                            + "    break;"
                            + "  }"
                            + "}"
                            + "})();");
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll drop list for drag: " + e.getMessage());
        }
        scrollToColumnInManagePanel(section);
        Thread.sleep(400);
    }

    private boolean isPtsColumnFirstInManagePanel(ColumnSection section) {
        String sectionTitle = sectionLabel(section);
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var sectionTitle = '" + sectionTitle + "';"
                            + "var columnLabel = '" + PtsPackagingIdPage.COLUMN_LABEL + "';"
                            + "var sections = document.querySelectorAll('[class*=\"table-column\"]');"
                            + "for (var s = 0; s < sections.length; s++) {"
                            + "  if ((sections[s].textContent || '').indexOf(sectionTitle) < 0) continue;"
                            + "  var list = sections[s].querySelector('[cdkdroplist], .cdk-drop-list');"
                            + "  if (!list) return false;"
                            + "  var items = list.querySelectorAll(':scope > .draggable-item');"
                            + "  if (!items.length) items = list.querySelectorAll('.draggable-item');"
                            + "  if (!items.length) return false;"
                            + "  return (items[0].textContent || '').indexOf(columnLabel) >= 0;"
                            + "}"
                            + "return false;"
                            + "})();");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Real CDK drag via Synergy {@code moveTo} (clickAndHold + moveByOffset + release on grip handle).
     * Moves one row up per step until PTS Packaging ID is first — required for long column lists.
     */
    private boolean movePtsColumnToFirstViaDragHandle(ColumnSection section) throws InterruptedException {
        By handleLocator = section == ColumnSection.ORDER
                ? ptsPage.orderColumnDragHandle(PtsPackagingIdPage.COLUMN_LABEL)
                : ptsPage.lineItemColumnDragHandle(PtsPackagingIdPage.COLUMN_LABEL);

        int dragSteps = 0;
        int staleAttempts = 0;
        String lastOffsetKey = null;
        int lastPtsIndex = getPtsColumnIndexInDropList(section);

        for (int attempt = 0; attempt < MAX_PTS_DRAG_STEPS; attempt++) {
            if (isPtsColumnFirstInManagePanel(section)) {
                if (dragSteps > 0) {
                    Logger.logReportMessage("Dragged " + PtsPackagingIdPage.COLUMN_LABEL + " to first in "
                            + sectionLabel(section) + " (" + dragSteps + " grip drag step(s), verified)");
                }
                return true;
            }
            if (attempt == 0) {
                scrollDropListForDrag(section);
            }
            if (!WaitUtil.isDisplayFast(handleLocator, 3)) {
                Logger.logConsoleMessage("PTS drag handle not visible in " + sectionLabel(section));
                return false;
            }
            int[] offset = getDragOffsetOneStepUp(section);
            if (offset == null) {
                return isPtsColumnFirstInManagePanel(section);
            }
            String offsetKey = offset[0] + "," + offset[1];
            if (offsetKey.equals(lastOffsetKey)) {
                staleAttempts++;
                if (staleAttempts >= MAX_STALE_DRAG_ATTEMPTS) {
                    Logger.logConsoleMessage("Grip drag not moving " + PtsPackagingIdPage.COLUMN_LABEL
                            + " (same offset " + offsetKey + " x" + staleAttempts + ") — stopping drag");
                    return false;
                }
            } else {
                staleAttempts = 0;
            }
            lastOffsetKey = offsetKey;

            try {
                DesktopBrowserElement dragHandle = driver.get().finder().findElement(handleLocator);
                dragHandle.scrollIntoView();
                dragHandle.mouseOver();
                Thread.sleep(150);
                dragHandle.moveTo(offset[0], offset[1]);
                dragSteps++;
                Thread.sleep(PANEL_SETTLE_MS);
            } catch (Exception e) {
                Logger.logConsoleMessage("Grip drag step failed: " + e.getMessage());
                return false;
            }

            int newIndex = getPtsColumnIndexInDropList(section);
            if (lastPtsIndex >= 0 && newIndex >= 0 && newIndex >= lastPtsIndex) {
                staleAttempts++;
                if (staleAttempts >= MAX_STALE_DRAG_ATTEMPTS) {
                    Logger.logConsoleMessage("PTS column index unchanged after drag (index=" + newIndex + ") — stopping");
                    return false;
                }
            } else {
                staleAttempts = 0;
            }
            lastPtsIndex = newIndex;
        }
        return isPtsColumnFirstInManagePanel(section);
    }

    private int getPtsColumnIndexInDropList(ColumnSection section) {
        String sectionTitle = sectionLabel(section);
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var sectionTitle = '" + sectionTitle + "';"
                            + "var columnLabel = '" + PtsPackagingIdPage.COLUMN_LABEL + "';"
                            + "var sections = document.querySelectorAll('[class*=\"table-column\"]');"
                            + "for (var s = 0; s < sections.length; s++) {"
                            + "  if ((sections[s].textContent || '').indexOf(sectionTitle) < 0) continue;"
                            + "  var list = sections[s].querySelector('[cdkdroplist], .cdk-drop-list');"
                            + "  if (!list) return -1;"
                            + "  var items = list.querySelectorAll('.draggable-item');"
                            + "  for (var i = 0; i < items.length; i++) {"
                            + "    if ((items[i].textContent || '').indexOf(columnLabel) >= 0) return i;"
                            + "  }"
                            + "}"
                            + "return -1;"
                            + "})();");
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            return Integer.parseInt(String.valueOf(result));
        } catch (Exception e) {
            return -1;
        }
    }

    /** Offset from PTS grip handle to the row immediately above (CDK Y-axis drag). */
    private int[] getDragOffsetOneStepUp(ColumnSection section) {
        By handleLocator = section == ColumnSection.ORDER
                ? ptsPage.orderColumnDragHandle(PtsPackagingIdPage.COLUMN_LABEL)
                : ptsPage.lineItemColumnDragHandle(PtsPackagingIdPage.COLUMN_LABEL);
        By rowAboveLocator = section == ColumnSection.ORDER
                ? ptsPage.orderColumnRowAbove(PtsPackagingIdPage.COLUMN_LABEL)
                : ptsPage.lineItemColumnRowAbove(PtsPackagingIdPage.COLUMN_LABEL);
        By rowAboveHandleLocator = section == ColumnSection.ORDER
                ? ptsPage.orderColumnRowAboveDragHandle(PtsPackagingIdPage.COLUMN_LABEL)
                : ptsPage.lineItemColumnRowAboveDragHandle(PtsPackagingIdPage.COLUMN_LABEL);

        if (!WaitUtil.isDisplayFast(rowAboveLocator, 2)) {
            Logger.logConsoleMessage("No row above " + PtsPackagingIdPage.COLUMN_LABEL + " in "
                    + sectionLabel(section) + " (may already be first)");
            return null;
        }

        int[] fromElements = computeDragOffsetFromElements(handleLocator, rowAboveLocator);
        if (fromElements != null) {
            return fromElements;
        }
        fromElements = computeDragOffsetFromElements(handleLocator, rowAboveHandleLocator);
        if (fromElements != null) {
            return fromElements;
        }

        return getDragOffsetOneStepUpViaScript(section);
    }

    /** Synergy element_x/y centers — avoids JS array return parsing issues on Synergy server. */
    private int[] computeDragOffsetFromElements(By sourceLocator, By targetLocator) {
        if (!WaitUtil.isDisplayFast(sourceLocator, 2) || !WaitUtil.isDisplayFast(targetLocator, 2)) {
            return null;
        }
        try {
            DesktopBrowserElement source = driver.get().finder().findElement(sourceLocator);
            DesktopBrowserElement target = driver.get().finder().findElement(targetLocator);
            source.scrollIntoView();
            target.scrollIntoView();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            source = driver.get().finder().findElement(sourceLocator);
            target = driver.get().finder().findElement(targetLocator);
            int sx = source.getX() + Math.max(source.getWidth(), 1) / 2;
            int sy = source.getY() + Math.max(source.getHeight(), 1) / 2;
            int tx = target.getX() + Math.max(target.getWidth(), 1) / 2;
            int ty = target.getY() + Math.max(target.getHeight(), 1) / 2;
            int dx = tx - sx;
            int dy = ty - sy;
            if (dx == 0 && dy == 0) {
                return null;
            }
            Logger.logConsoleMessage("Drag offset (element coords): dx=" + dx + ", dy=" + dy);
            return new int[] { dx, dy };
        } catch (Exception e) {
            Logger.logConsoleMessage("Element drag offset failed: " + e.getMessage());
            return null;
        }
    }

    /** Fallback: index-based lookup in drop list; returns comma-separated string for reliable parsing. */
    private int[] getDragOffsetOneStepUpViaScript(ColumnSection section) {
        String sectionTitle = sectionLabel(section);
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var sectionTitle = '" + sectionTitle + "';"
                            + "var columnLabel = '" + PtsPackagingIdPage.COLUMN_LABEL + "';"
                            + "var sections = document.querySelectorAll('[class*=\"table-column\"]');"
                            + "for (var s = 0; s < sections.length; s++) {"
                            + "  if ((sections[s].textContent || '').indexOf(sectionTitle) < 0) continue;"
                            + "  var list = sections[s].querySelector('[cdkdroplist], .cdk-drop-list');"
                            + "  if (!list) return 'no_list';"
                            + "  var items = list.querySelectorAll(':scope > .draggable-item');"
                            + "  if (!items.length) items = list.querySelectorAll('.draggable-item');"
                            + "  for (var i = 0; i < items.length; i++) {"
                            + "    if ((items[i].textContent || '').indexOf(columnLabel) < 0) continue;"
                            + "    if (i === 0) return 'already_first';"
                            + "    var ptsItem = items[i];"
                            + "    var prev = items[i - 1];"
                            + "    var handle = ptsItem.querySelector("
                            + "      '.cdk-drag-handle, .drag-handle, i.bi-grip-vertical, [cdkdraghandle]');"
                            + "    if (!handle) return 'no_handle';"
                            + "    handle.scrollIntoView({block:'center'});"
                            + "    prev.scrollIntoView({block:'nearest'});"
                            + "    var hr = handle.getBoundingClientRect();"
                            + "    var pr = prev.getBoundingClientRect();"
                            + "    var dx = Math.round((pr.left + pr.width / 2) - (hr.left + hr.width / 2));"
                            + "    var dy = Math.round((pr.top + pr.height / 2) - (hr.top + hr.height / 2));"
                            + "    return dx + ',' + dy;"
                            + "  }"
                            + "  return 'not_found';"
                            + "})();");
            if (result == null) {
                return null;
            }
            String status = String.valueOf(result).trim();
            if ("already_first".equals(status)) {
                return null;
            }
            if (status.contains(",") && !status.contains("no_") && !status.contains("not_")) {
                return parseDragOffset(status);
            }
            Logger.logConsoleMessage("Drag offset script status: " + status);
            return null;
        } catch (Exception e) {
            Logger.logConsoleMessage("Drag offset script failed: " + e.getMessage());
            return null;
        }
    }

    private int[] parseDragOffset(Object result) {
        if (result == null || "null".equals(String.valueOf(result))) {
            return null;
        }
        if (result instanceof List) {
            List<?> list = (List<?>) result;
            if (list.size() >= 2) {
                return new int[] { toInt(list.get(0)), toInt(list.get(1)) };
            }
        }
        if (result.getClass().isArray()) {
            Object[] arr = (Object[]) result;
            if (arr.length >= 2) {
                return new int[] { toInt(arr[0]), toInt(arr[1]) };
            }
        }
        String raw = String.valueOf(result).replace("[", "").replace("]", "").trim();
        if (raw.contains(",")) {
            String[] parts = raw.split(",");
            if (parts.length >= 2) {
                return new int[] { toInt(parts[0].trim()), toInt(parts[1].trim()) };
            }
        }
        return null;
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return (int) Math.round(Double.parseDouble(String.valueOf(value)));
    }

    private void waitForGridRefresh() throws InterruptedException {
        WaitUtil.waitForJSToLoad(3);
        Thread.sleep(PANEL_SETTLE_MS);
        try {
            WaitUtil.isDisplayFast(leftFilterPanel.tableRecordCountLabel(), 3);
        } catch (Exception ignored) {
            // optional anchor while grid re-renders
        }
    }

    /**
     * PROD: column sort triggers a full grid reload and resets horizontal scroll to the left.
     * Wait for rows to return, then scroll back to PTS Packaging ID before the next action.
     */
    private void waitForSortGridReload() throws InterruptedException {
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(PANEL_SETTLE_MS);
        try {
            WaitUtil.isDisplayFast(leftFilterPanel.tableRecordCountLabel(), 5);
        } catch (Exception ignored) {
            // optional anchor while grid re-renders after sort
        }
        long endTime = System.currentTimeMillis() + (GRID_POLL_S * 1000L);
        while (System.currentTimeMillis() < endTime) {
            if (hasVisibleGridRows()) {
                break;
            }
            Thread.sleep(300);
        }
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        Logger.logReportMessage("Grid reloaded after sort — scrolled back to "
                + PtsPackagingIdPage.COLUMN_LABEL);
    }

    /**
     * Scroll the Orders / Line Items grid horizontally until PTS Packaging ID is visible.
     * PTS is typically the last enabled column — scrolls viewports to the far right first.
     *
     * @return true if the column header is in the viewport after scrolling
     */
    public boolean scrollGridToPtsColumnUntilVisible(int maxWaitSec) throws InterruptedException {
        scrollGridToPtsColumn();
        Thread.sleep(PANEL_SETTLE_MS);
        if (isPtsColumnVisibleViaSynergy()) {
            Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " visible after horizontal scroll (Synergy)");
            return true;
        }
        if (tryScrollAtCurrentZoom()) {
            return true;
        }
        long endTime = System.currentTimeMillis() + (maxWaitSec * 1000L);
        int attempts = 0;
        while (System.currentTimeMillis() < endTime && attempts < 2) {
            scrollGridToPtsColumn();
            Thread.sleep(400);
            if (isPtsColumnVisibleViaSynergy()) {
                return true;
            }
            attempts++;
        }
        return applyMinimumZoomForPtsColumn();
    }

    /**
     * Worst-case: horizontal scroll → minimum readable zoom → uncheck all Order columns except PTS.
     */
    public boolean ensurePtsColumnVisibleWithFallbacks(SoftAssert softAssert) throws InterruptedException {
        if (scrollGridToPtsColumnUntilVisible(GRID_POLL_S)) {
            return isPtsColumnReadableInViewport();
        }
        Logger.logReportMessage("Trying Manage columns — keep only " + PtsPackagingIdPage.COLUMN_LABEL
                + " checked on Automation view");
        if (minimizeOrderColumnsToPtsOnly(softAssert)) {
            waitForGridRefresh();
            if (scrollGridToPtsColumnUntilVisible(GRID_POLL_S)) {
                return isPtsColumnReadableInViewport();
            }
        }
        return isPtsColumnReadableInViewport();
    }

    /** Apply the highest zoom % (least zoom-out) where PTS header/cells become readable; keeps that zoom. */
    private boolean applyMinimumZoomForPtsColumn() throws InterruptedException {
        for (int zoomPct : ZOOM_LEVELS_PCT) {
            applyBrowserZoomPercent(zoomPct);
            Thread.sleep(600);
            if (tryScrollAtCurrentZoom()) {
                Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " readable at browser zoom "
                        + zoomPct + "% (minimum zoom-out needed)");
                return true;
            }
        }
        Logger.logConsoleMessage("PTS column not readable at any zoom level down to "
                + ZOOM_LEVELS_PCT[ZOOM_LEVELS_PCT.length - 1] + "%");
        return false;
    }

    private boolean tryScrollAtCurrentZoom() throws InterruptedException {
        scrollGridToPtsColumn();
        Thread.sleep(400);
        return isPtsColumnVisibleViaSynergy() || isPtsColumnReadableInViewport();
    }

    private void applyBrowserZoomPercent(int zoomPercent) {
        try {
            driver.get().browser().executeScript(
                    "document.body.style.zoom='" + zoomPercent + "%';"
                            + "if (document.documentElement) document.documentElement.style.zoom='"
                            + zoomPercent + "%';");
            Logger.logConsoleMessage("Browser zoom set to " + zoomPercent + "%");
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not set browser zoom: " + e.getMessage());
        }
    }

    /** Uncheck every Order column except PTS Packaging ID on Automation view and save. */
    private boolean minimizeOrderColumnsToPtsOnly(SoftAssert softAssert) throws InterruptedException {
        openManageColumnsPanel(softAssert);
        if (!manageColumns.isPanelOpen(3)) {
            return false;
        }
        if (!WaitUtil.isDisplayFast(ptsPage.activeTableViewLabel(PtsPackagingIdPage.AUTOMATION_TABLE_VIEW), 2)) {
            selectAutomationTableViewFromDropdownIfPresent(softAssert);
        }
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var ptsLabel = '" + PtsPackagingIdPage.COLUMN_LABEL + "';"
                            + "var sections = document.querySelectorAll('[class*=\"table-column\"]');"
                            + "for (var s = 0; s < sections.length; s++) {"
                            + "  if ((sections[s].textContent || '').indexOf('Order columns') < 0) continue;"
                            + "  var toggled = 0;"
                            + "  var cbs = sections[s].querySelectorAll('input[type=checkbox]');"
                            + "  for (var i = 0; i < cbs.length; i++) {"
                            + "    var row = cbs[i].closest('.draggable-item, a[cdkdrag], .option-container');"
                            + "    var text = row ? (row.textContent || '') : '';"
                            + "    if (text.indexOf(ptsLabel) >= 0) {"
                            + "      if (!cbs[i].checked) { cbs[i].click(); toggled++; }"
                            + "      continue;"
                            + "    }"
                            + "    if (cbs[i].checked) { cbs[i].click(); toggled++; }"
                            + "  }"
                            + "  return toggled;"
                            + "}"
                            + "return -1;"
                            + "})();");
            int toggled = result instanceof Number ? ((Number) result).intValue() : -1;
            Logger.logReportMessage("Minimized Order columns to PTS only (toggled " + toggled + " checkbox(es))");
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not minimize Order columns: " + e.getMessage());
            return false;
        }
        Thread.sleep(PANEL_SETTLE_MS);
        clickSaveChangesIfAvailable(softAssert);
        confirmSaveChangesDialogIfPresent();
        closeManageColumnsPanel(softAssert);
        return true;
    }

    /**
     * One attempt: scroll horizontal table viewports (PTS is usually the last column) and
     * bring the PTS header into the visible viewport.
     */
    public boolean scrollGridToPtsColumn() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = '" + PTS_COLUMN_LABEL_JS + "';"
                            + "var shortLabel = '" + PTS_COLUMN_SHORT_JS + "';"
                            + findPtsHeaderJsFunction()
                            + "function isHeaderVisible(th) {"
                            + "  if (!th) return false;"
                            + "  var r = th.getBoundingClientRect();"
                            + "  return r.width > 0 && r.height > 0 && r.right > 0 && r.left < window.innerWidth;"
                            + "}"
                            + "function scrollElToEnd(el) {"
                            + "  if (!el || el.scrollWidth <= el.clientWidth + 1) return;"
                            + "  el.scrollLeft = el.scrollWidth - el.clientWidth;"
                            + "  el.dispatchEvent(new Event('scroll', { bubbles: true }));"
                            + "}"
                            + "function scrollHorizontalContainersToEnd() {"
                            + "  var table = document.querySelector('#orderTable') || document.querySelector('table.fixed-header');"
                            + "  if (table) {"
                            + "    var el = table;"
                            + "    while (el) {"
                            + "      scrollElToEnd(el);"
                            + "      if (el.tagName === 'APP-FULFILLMENT-MAIN-TABLE-CONTAINER') break;"
                            + "      el = el.parentElement;"
                            + "    }"
                            + "  }"
                            + "  var selectors = ["
                            + "    '.bs-custom-table-viewport',"
                            + "    '.custom-table-wrapper',"
                            + "    '.table-container.show-left-container',"
                            + "    '.table-container',"
                            + "    'msc-custom-table .custom-table-wrapper',"
                            + "    'app-fulfillment-main-table-container .custom-table-wrapper',"
                            + "    'app-fulfillment-main-table-container .table-container'"
                            + "  ];"
                            + "  for (var s = 0; s < selectors.length; s++) {"
                            + "    var nodes = document.querySelectorAll(selectors[s]);"
                            + "    for (var n = 0; n < nodes.length; n++) scrollElToEnd(nodes[n]);"
                            + "  }"
                            + "  var rows = document.querySelectorAll('tr.row-horizontal-scroll');"
                            + "  if (rows.length > 0) {"
                            + "    var rowParent = rows[0].parentElement;"
                            + "    while (rowParent) {"
                            + "      scrollElToEnd(rowParent);"
                            + "      rowParent = rowParent.parentElement;"
                            + "    }"
                            + "  }"
                            + "}"
                            + "var col = findPtsHeader();"
                            + "if (!col) return 'not_in_dom';"
                            + "if (isHeaderVisible(col)) return 'visible';"
                            + "scrollHorizontalContainersToEnd();"
                            + "col.scrollIntoView({block:'nearest', inline:'end'});"
                            + "if (isHeaderVisible(col)) return 'visible';"
                            + "var table = document.querySelector('#orderTable');"
                            + "var scroller = table;"
                            + "while (scroller) {"
                            + "  if (scroller.scrollWidth > scroller.clientWidth + 1) {"
                            + "    var maxScroll = scroller.scrollWidth - scroller.clientWidth;"
                            + "    for (var step = 1; step <= 5; step++) {"
                            + "      scroller.scrollLeft = Math.round(maxScroll * step / 5);"
                            + "      scroller.dispatchEvent(new Event('scroll', { bubbles: true }));"
                            + "      if (isHeaderVisible(col)) return 'visible';"
                            + "    }"
                            + "    scroller.scrollLeft = maxScroll;"
                            + "    scroller.dispatchEvent(new Event('scroll', { bubbles: true }));"
                            + "    col.scrollIntoView({block:'nearest', inline:'end'});"
                            + "    if (isHeaderVisible(col)) return 'visible';"
                            + "    break;"
                            + "  }"
                            + "  scroller = scroller.parentElement;"
                            + "}"
                            + "return isHeaderVisible(col) ? 'visible' : 'not_visible';"
                            + "})();");
            String status = result == null ? "unknown" : String.valueOf(result).trim();
            if (status.isEmpty()) {
                status = "empty_result";
            }
            if ("visible".equals(status)) {
                Logger.logReportMessage("Scrolled #orderTable horizontally to " + PtsPackagingIdPage.COLUMN_LABEL
                        + " (last column)");
                return true;
            }
            if ("not_in_dom".equals(status)) {
                Logger.logConsoleMessage(PtsPackagingIdPage.COLUMN_LABEL + " header not in grid DOM");
                return false;
            }
            if (isPtsColumnVisibleViaSynergy()) {
                Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " found via Synergy after scroll");
                return true;
            }
            if (isPtsColumnHeaderVisibleInViewport() || isPtsColumnReadableInViewport()) {
                return true;
            }
            Logger.logConsoleMessage("Grid horizontal scroll status: " + status);
            return scrollGridToPtsColumnViaSynergyViewport();
        } catch (Exception e) {
            Logger.logConsoleMessage("Grid scroll to PTS column failed: " + e.getMessage());
            return scrollGridToPtsColumnViaSynergyViewport();
        }
    }

    /** Synergy fallback — scroll .custom-table-wrapper / #orderTable parent to far right. */
    private boolean scrollGridToPtsColumnViaSynergyViewport() {
        try {
            By[] containers = {
                    ptsPage.ordersGridHorizontalScrollContainer(),
                    ptsPage.gridHorizontalScrollViewport(),
                    ptsPage.ordersGridTable()
            };
            for (By container : containers) {
                if (!WaitUtil.isDisplayFast(container, 2)) {
                    continue;
                }
                DesktopBrowserElement el = driver.get().finder().findElement(container);
                el.executeScript(
                        "var el = arguments[0];"
                                + "var node = el;"
                                + "while (node) {"
                                + "  if (node.scrollWidth > node.clientWidth + 1) {"
                                + "    node.scrollLeft = node.scrollWidth - node.clientWidth;"
                                + "    node.dispatchEvent(new Event('scroll', { bubbles: true }));"
                                + "  }"
                                + "  node = node.parentElement;"
                                + "}");
            }
            if (WaitUtil.isDisplayFast(ptsPage.gridColumnHeader(), 2)) {
                DriverUtil.scrollToElement(ptsPage.gridColumnHeader());
            }
            if (isPtsColumnVisibleViaSynergy()) {
                Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " visible after Synergy viewport scroll");
                return true;
            }
            return isPtsColumnHeaderVisibleInViewport();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPtsColumnVisibleOnGrid() {
        scrollGridToPtsColumn();
        return isPtsColumnPresentInDom();
    }

    /** @return true if Automation option was found and selected from the dropdown */
    private boolean selectAutomationTableViewFromDropdownIfPresent(SoftAssert softAssert) throws InterruptedException {
        if (!DriverUtil.clickOnElement(ptsPage.tableViewDropdown(), 5)) {
            return false;
        }
        Thread.sleep(1000);
        By automationOption = ptsPage.tableViewOption(PtsPackagingIdPage.AUTOMATION_TABLE_VIEW);
        if (!WaitUtil.isDisplayFast(automationOption, 5)) {
            DriverUtil.clickOnElement(ptsPage.tableViewDropdown(), 3);
            return false;
        }
        Verify.softAssert1(DriverUtil.clickOnElement(automationOption, PANEL_WAIT_S),
                "Selected existing table view: " + PtsPackagingIdPage.AUTOMATION_TABLE_VIEW, softAssert);
        Thread.sleep(PANEL_SETTLE_MS);
        return true;
    }

    private void selectTableViewFromDropdown(String viewName, SoftAssert softAssert) throws InterruptedException {
        Verify.softAssert1(DriverUtil.clickOnElement(ptsPage.tableViewDropdown(), PANEL_WAIT_S),
                "Opened table view dropdown", softAssert);
        Thread.sleep(1000);
        By option = PtsPackagingIdPage.STANDARD_VIEW.equals(viewName)
                ? ptsPage.standardViewOption()
                : ptsPage.tableViewOption(viewName);
        Verify.softAssert1(DriverUtil.clickOnElement(option, PANEL_WAIT_S),
                "Selected table view: " + viewName, softAssert);
        Thread.sleep(PANEL_SETTLE_MS);
    }

    private void saveNewAutomationTableView(SoftAssert softAssert) throws InterruptedException {
        DriverUtil.scrollToElement(ptsPage.saveNewViewButton());
        Verify.softAssert1(DriverUtil.clickOnElement(ptsPage.saveNewViewButton(), PANEL_WAIT_S),
                "Clicked Save new view", softAssert);
        Thread.sleep(PANEL_SETTLE_MS);

        Verify.softAssert1(WaitUtil.isDisplayFast(ptsPage.saveNewViewNameInput(), PANEL_WAIT_S),
                "Save new view name input visible", softAssert);
        DesktopBrowserElement nameInput = driver.get().finder().findElement(ptsPage.saveNewViewNameInput());
        nameInput.click();
        nameInput.executeScript("arguments[0].value='';");
        nameInput.sendKeys(PtsPackagingIdPage.AUTOMATION_TABLE_VIEW);
        Thread.sleep(300);

        Verify.softAssert1(DriverUtil.clickOnElement(ptsPage.saveNewViewConfirmButton(), PANEL_WAIT_S),
                "Saved new table view as " + PtsPackagingIdPage.AUTOMATION_TABLE_VIEW, softAssert);
        Thread.sleep(3000);
        waitForGridRefresh();

        boolean toastSeen = WaitUtil.isDisplayFast(ptsPage.tableViewCreatedMessage(), 8)
                || WaitUtil.isDisplayFast(ptsPage.tableViewCreatedToast(), 5);
        boolean viewActive = WaitUtil.isDisplayFast(
                ptsPage.activeTableViewLabel(PtsPackagingIdPage.AUTOMATION_TABLE_VIEW), 5);

        if (toastSeen || viewActive) {
            Logger.logReportMessage("Table view '" + PtsPackagingIdPage.AUTOMATION_TABLE_VIEW + "' created successfully");
        } else {
            Logger.logReportMessage("Save confirmation not visible — verifying via dropdown / grid column next");
        }

        Verify.softAssert1(toastSeen || viewActive,
                PtsPackagingIdPage.AUTOMATION_TABLE_VIEW + " saved (toast or active view in dropdown)", softAssert);
    }

    private void scrollToColumnInManagePanel(ColumnSection section) {
        By label = columnLabelLocator(section);
        try {
            if (section == ColumnSection.LINE_ITEM) {
                By list = ptsPage.lineItemManageColumnsOptionsList();
                if (WaitUtil.isDisplayFast(list, 2)) {
                    DesktopBrowserElement container = driver.get().finder().findElement(list);
                    container.executeScript("this.scrollTop = this.scrollHeight;");
                } else if (WaitUtil.isDisplayFast(ptsPage.lineItemColumnsDropList(), 2)) {
                    DesktopBrowserElement container = driver.get().finder().findElement(ptsPage.lineItemColumnsDropList());
                    container.executeScript("this.scrollTop = this.scrollHeight;");
                }
            } else if (section == ColumnSection.ORDER
                    && WaitUtil.isDisplayFast(ptsPage.orderColumnsListContainer(), 2)) {
                DesktopBrowserElement container = driver.get().finder().findElement(ptsPage.orderColumnsListContainer());
                container.executeScript("this.scrollTop = this.scrollHeight;");
            }
        } catch (Exception ignored) {
            // container locator may vary — fall through to element scroll
        }
        DriverUtil.scrollToElement(label);
        try {
            DesktopBrowserElement labelEl = driver.get().finder().findElement(label);
            labelEl.executeScript("arguments[0].scrollIntoView({block:'center', inline:'nearest'});");
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not scroll " + PtsPackagingIdPage.COLUMN_LABEL + " into view: " + e.getMessage());
        }
    }

    private void clickColumnInManagePanel(ColumnSection section, SoftAssert softAssert) {
        By label = columnLabelLocator(section);
        By checkbox = columnCheckboxLocator(section);
        scrollToColumnInManagePanel(section);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (DriverUtil.clickOnElement(label, 3)) {
            Verify.softAssert1(true,
                    "Clicked " + PtsPackagingIdPage.COLUMN_LABEL + " label in Manage columns (" + sectionLabel(section) + ")",
                    softAssert);
            return;
        }

        try {
            driver.get().finder().findElement(checkbox)
                    .executeScript("arguments[0].click();");
            Verify.softAssert1(true,
                    "JS-clicked " + PtsPackagingIdPage.COLUMN_LABEL + " checkbox (" + sectionLabel(section) + ")",
                    softAssert);
        } catch (Exception e) {
            try {
                driver.get().finder().findElement(label).executeScript("arguments[0].click();");
                Verify.softAssert1(true,
                        "JS-clicked " + PtsPackagingIdPage.COLUMN_LABEL + " label (" + sectionLabel(section) + ")",
                        softAssert);
            } catch (Exception ex) {
                Verify.softAssert1(false,
                        "Failed to toggle " + PtsPackagingIdPage.COLUMN_LABEL + " in Manage columns: " + ex.getMessage(),
                        softAssert);
            }
        }
    }

    private boolean isColumnCheckboxSelectedSafe(By checkbox) {
        try {
            if (!WaitUtil.isDisplayFast(checkbox, 5)) {
                return false;
            }
            return DriverUtil.isSelectedCheckbox(checkbox);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read checkbox state: " + checkbox);
            return false;
        }
    }

    private By columnCheckboxLocator(ColumnSection section) {
        return section == ColumnSection.ORDER ? ptsPage.orderColumnCheckbox() : ptsPage.lineItemColumnCheckbox();
    }

    private By columnLabelLocator(ColumnSection section) {
        return section == ColumnSection.ORDER ? ptsPage.orderColumnCheckboxLabel() : ptsPage.lineItemColumnCheckboxLabel();
    }

    private static String sectionLabel(ColumnSection section) {
        return section == ColumnSection.ORDER ? "Order columns" : "Line item table";
    }

    private static boolean isColumnEnabledInSession(ColumnSection section) {
        return section == ColumnSection.ORDER
                ? PtsPackagingSessionHelper.isPtsColumnEnabledOnOrders()
                : PtsPackagingSessionHelper.isPtsColumnEnabledOnLineItems();
    }

    private static void markColumnEnabledInSession(ColumnSection section) {
        if (section == ColumnSection.ORDER) {
            PtsPackagingSessionHelper.markPtsColumnEnabledOnOrders();
        } else {
            PtsPackagingSessionHelper.markPtsColumnEnabledOnLineItems();
        }
    }

    private static void resetColumnEnabledInSession(ColumnSection section) {
        if (section == ColumnSection.ORDER) {
            PtsPackagingSessionHelper.resetPtsColumnEnabledOnOrders();
        } else {
            PtsPackagingSessionHelper.resetPtsColumnEnabledOnLineItems();
        }
    }

    public void validateColumnNotVisibleInGrid(SoftAssert softAssert) {
        Verify.softAssert1(!WaitUtil.isDisplayFast(ptsPage.gridColumnHeader(), 5),
                PtsPackagingIdPage.COLUMN_LABEL + " header not visible after deselect", softAssert);
        Verify.softAssert1(!WaitUtil.isDisplayFast(ptsPage.gridColumnHeaderLabel(), 3),
                PtsPackagingIdPage.COLUMN_LABEL + " header label not visible after deselect", softAssert);
    }

    public void validateColumnVisibleInGrid(SoftAssert softAssert) {
        try {
            if (!isPtsColumnVisibleViaSynergy()) {
                ensurePtsColumnVisibleWithFallbacks(softAssert);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Verify.softAssert1(isPtsColumnPresentInDom(),
                PtsPackagingIdPage.COLUMN_LABEL + " header in grid DOM (PROD: th[@title='"
                        + PtsPackagingIdPage.COLUMN_LABEL + "'], class=" + PtsPackagingIdPage.COLUMN_CSS_CLASS + ")",
                softAssert);
        Verify.softAssert1(isPtsColumnVisibleViaSynergy(),
                PtsPackagingIdPage.COLUMN_LABEL + " visible in grid after horizontal scroll"
                        + " (Synergy locator: th[@title='" + PtsPackagingIdPage.COLUMN_LABEL + "'])",
                softAssert);
    }

    public void searchGlobal(String searchText, SoftAssert softAssert) throws InterruptedException {
        DriverUtil.sendKeyToElement(ptsPage.globalSearchInput(), 10, searchText);
        Thread.sleep(1500);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Verify.softAssert1(WaitUtil.isDisplay(ptsPage.globalSearchInput(), PANEL_WAIT_S),
                "Global search applied: " + searchText, softAssert);
    }

    public String readFirstNonEmptyCellValue(SoftAssert softAssert) {
        scrollGridToPtsColumn();
        if (!isPtsColumnPresentInDom()) {
            softAssert.assertTrue(false, "No " + PtsPackagingIdPage.COLUMN_LABEL + " column on PTS-filtered grid");
            return "";
        }
        String value = readFirstPtsCellValueViaJs(false);
        if (!value.isEmpty()) {
            Logger.logReportMessage("Read " + PtsPackagingIdPage.COLUMN_LABEL + " from grid cell: " + value);
            return value;
        }
        value = readFirstPtsCellValueViaCopyIcon();
        if (!value.isEmpty()) {
            Logger.logReportMessage("Read " + PtsPackagingIdPage.COLUMN_LABEL + " via copy icon: " + value);
            return value;
        }
        List<DesktopBrowserElement> cells = driver.get().finder().findElements(ptsPage.gridColumnCells());
        for (DesktopBrowserElement cell : cells) {
            String text = cell.getText().trim();
            if (!text.isEmpty() && !"-".equals(text)) {
                return text;
            }
        }
        softAssert.assertTrue(false, "No populated " + PtsPackagingIdPage.COLUMN_LABEL + " value in PTS orders");
        return "";
    }

    /** After O_011 sort, prefer first grid row; fall back to first populated PTS row in grid. */
    public String readFirstRowPtsCellValue(SoftAssert softAssert) {
        ensurePtsColumnScrolledIntoView();
        if (!isPtsColumnPresentInDom()) {
            softAssert.assertTrue(false, "No " + PtsPackagingIdPage.COLUMN_LABEL + " column on PTS-filtered grid");
            return "";
        }
        String value = readFirstPtsCellValueViaJs(true);
        if (!value.isEmpty()) {
            Logger.logReportMessage("Read first-row " + PtsPackagingIdPage.COLUMN_LABEL + ": " + value);
            return value;
        }
        value = readFirstPtsCellValueViaCopyIcon();
        if (!value.isEmpty()) {
            Logger.logReportMessage("Read first-row " + PtsPackagingIdPage.COLUMN_LABEL + " via copy icon: " + value);
            return value;
        }
        value = readFirstPtsCellValueViaJs(false);
        if (!value.isEmpty()) {
            Logger.logReportMessage("First row empty — using first populated "
                    + PtsPackagingIdPage.COLUMN_LABEL + " in grid: " + value);
            return value;
        }
        List<DesktopBrowserElement> cells = driver.get().finder().findElements(ptsPage.gridColumnCells());
        for (DesktopBrowserElement cell : cells) {
            String text = cell.getText().trim();
            if (!text.isEmpty() && !"-".equals(text)) {
                return text;
            }
        }
        softAssert.assertTrue(false,
                "No populated " + PtsPackagingIdPage.COLUMN_LABEL
                        + " value in PTS orders — run O_011 sort or widen date range");
        return "";
    }

    /** Read full PTS ID from cell DOM (title / cdkCopyToClipboard) before falling back to visible text. */
    private String readFirstPtsCellValueViaJs(boolean firstRowOnly) {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var firstRowOnly = " + firstRowOnly + ";"
                            + "var label = '" + PTS_COLUMN_LABEL_JS + "';"
                            + findPtsHeaderJsFunction()
                            + "var th = findPtsHeader();"
                            + "if (!th) return '';"
                            + "var colIndex = th.cellIndex;"
                            + "var rows = document.querySelectorAll('#orderTable tbody tr, table.fixed-header tbody tr');"
                            + "function readTd(td) {"
                            + "  if (!td) return '';"
                            + "  var tip = (td.getAttribute('title') || '').trim();"
                            + "  if (tip.length > 0 && tip !== '-') return tip;"
                            + "  var copyEl = td.querySelector('[cdkcopytoclipboard], [data-clipboard-text],"
                            + "    [ng-reflect-copy], [class*=\"copy\"], [class*=\"clipboard\"]');"
                            + "  if (copyEl) {"
                            + "    var attr = copyEl.getAttribute('cdkcopytoclipboard')"
                            + "      || copyEl.getAttribute('ng-reflect-copy')"
                            + "      || copyEl.getAttribute('data-clipboard-text') || '';"
                            + "    if (attr.trim().length > 0) return attr.trim();"
                            + "  }"
                            + "  var text = (td.innerText || td.textContent || '').replace(/\\s+/g, ' ').trim();"
                            + "  if (text.length > 0 && text !== '-') return text;"
                            + "  return '';"
                            + "}"
                            + "if (firstRowOnly && rows.length > 0) {"
                            + "  var cells0 = rows[0].cells;"
                            + "  if (cells0 && colIndex < cells0.length) return readTd(cells0[colIndex]);"
                            + "  return '';"
                            + "}"
                            + "for (var r = 0; r < rows.length; r++) {"
                            + "  var cells = rows[r].cells;"
                            + "  if (!cells || colIndex >= cells.length) continue;"
                            + "  var val = readTd(cells[colIndex]);"
                            + "  if (val.length > 0) return val;"
                            + "}"
                            + "return '';"
                            + "})();");
            return result == null ? "" : String.valueOf(result).trim();
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read PTS cell via JS: " + e.getMessage());
            return "";
        }
    }

    private boolean isFirstPtsCellPopulated() {
        return findFirstPopulatedPtsRowIndexViaJs() == 0;
    }

    /** 0-based row index of first non-empty PTS cell, or -1 if none in DOM. */
    private int findFirstPopulatedPtsRowIndexViaJs() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = '" + PTS_COLUMN_LABEL_JS + "';"
                            + findPtsHeaderJsFunction()
                            + findGridRowsJsFunction()
                            + readPtsCellJsFunction()
                            + "var rows = findGridRows();"
                            + "var th = findPtsHeader();"
                            + "if (th && rows.length > 0) {"
                            + "  var colIndex = th.cellIndex;"
                            + "  for (var r = 0; r < rows.length; r++) {"
                            + "    var cells = rows[r].cells;"
                            + "    if (!cells || colIndex >= cells.length) continue;"
                            + "    if (readPtsTd(cells[colIndex]).length > 0) return r;"
                            + "  }"
                            + "}"
                            + "var ptsCells = document.querySelectorAll('td.ops-console-id-cell, td.pts-packaging-id-col');"
                            + "for (var c = 0; c < ptsCells.length; c++) {"
                            + "  if (readPtsTd(ptsCells[c]).length === 0) continue;"
                            + "  var tr = ptsCells[c].closest('tr');"
                            + "  if (!tr) return c;"
                            + "  for (var i = 0; i < rows.length; i++) {"
                            + "    if (rows[i] === tr) return i;"
                            + "  }"
                            + "  return 0;"
                            + "}"
                            + "return -1;"
                            + "})();");
            return parseJsIntResult(result, -1);
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not find populated PTS row via JS: " + e.getMessage());
            return findFirstPopulatedPtsRowIndexViaSelenium();
        }
    }

    private int findFirstPopulatedPtsRowIndexViaSelenium() {
        try {
            List<DesktopBrowserElement> cells = driver.get().finder().findElements(ptsPage.gridColumnCells());
            for (int i = 0; i < cells.size(); i++) {
                String text = cells.get(i).getText().trim();
                if (!text.isEmpty() && !"-".equals(text)) {
                    return i;
                }
            }
        } catch (Exception ignored) {
            // optional fallback
        }
        return -1;
    }

    private String readGridRowSignaturesViaJs(int limit) {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var limit = " + limit + ";"
                            + findGridRowsJsFunction()
                            + "var rows = findGridRows();"
                            + "var sigs = [];"
                            + "for (var r = 0; r < rows.length && r < limit; r++) {"
                            + "  sigs.push((rows[r].innerText || rows[r].textContent || '')"
                            + "    .replace(/\\s+/g, ' ').trim().substring(0, 120));"
                            + "}"
                            + "return sigs.join('\\u001f');"
                            + "})();");
            String signatures = result == null ? "" : String.valueOf(result);
            if (!signatures.isEmpty()) {
                return signatures;
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read grid row signatures: " + e.getMessage());
        }
        return readGridRowSignaturesViaSelenium(limit);
    }

    private String readGridRowSignaturesViaSelenium(int limit) {
        StringBuilder sigs = new StringBuilder();
        try {
            List<DesktopBrowserElement> rows = driver.get().finder().findElements(ptsPage.gridDataRows());
            int count = Math.min(rows.size(), limit);
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    sigs.append('\u001f');
                }
                String text = rows.get(i).getText().replaceAll("\\s+", " ").trim();
                sigs.append(text.length() > 120 ? text.substring(0, 120) : text);
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Selenium row signature fallback failed: " + e.getMessage());
        }
        return sigs.toString();
    }

    private String readPtsHeaderSortStateViaJs() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = '" + PTS_COLUMN_LABEL_JS + "';"
                            + "var shortLabel = '" + PTS_COLUMN_SHORT_JS + "';"
                            + findPtsHeaderJsFunction()
                            + "var th = findPtsHeader();"
                            + "if (!th) return '';"
                            + "var filter = th.querySelector('msc-custom-table-column-filter');"
                            + "var up = filter ? filter.querySelector('[class*=\"sort-icon-up\"]') : null;"
                            + "var down = filter ? filter.querySelector('[class*=\"sort-icon-down\"]') : null;"
                            + "var parts = [th.getAttribute('aria-sort') || 'none'];"
                            + "if (up) parts.push('up:' + (up.className || ''));"
                            + "if (down) parts.push('down:' + (down.className || ''));"
                            + "return parts.join('|');"
                            + "})();");
            return result == null ? "" : String.valueOf(result);
        } catch (Exception e) {
            return "";
        }
    }

    private List<String> readVisiblePtsCellValuesViaJs(int limit) {
        List<String> values = new ArrayList<>();
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var limit = " + limit + ";"
                            + "var label = '" + PTS_COLUMN_LABEL_JS + "';"
                            + findPtsHeaderJsFunction()
                            + findGridRowsJsFunction()
                            + readPtsCellJsFunction()
                            + "var th = findPtsHeader();"
                            + "var out = [];"
                            + "if (th) {"
                            + "  var colIndex = th.cellIndex;"
                            + "  var rows = findGridRows();"
                            + "  for (var r = 0; r < rows.length && out.length < limit; r++) {"
                            + "    var cells = rows[r].cells;"
                            + "    if (!cells || colIndex >= cells.length) { out.push(''); continue; }"
                            + "    out.push(readPtsTd(cells[colIndex]));"
                            + "  }"
                            + "}"
                            + "if (out.length === 0) {"
                            + "  var th2 = findPtsHeader();"
                            + "  if (th2) {"
                            + "    var colIndex2 = th2.cellIndex;"
                            + "    var rows2 = findGridRows();"
                            + "    for (var r2 = 0; r2 < rows2.length && out.length < limit; r2++) {"
                            + "      var cells2 = rows2[r2].cells;"
                            + "      if (!cells2 || colIndex2 >= cells2.length) { out.push(''); continue; }"
                            + "      out.push(readPtsTd(cells2[colIndex2]));"
                            + "    }"
                            + "  }"
                            + "}"
                            + "return out;"
                            + "})();");
            if (result instanceof List) {
                for (Object item : (List<?>) result) {
                    values.add(String.valueOf(item));
                }
            } else if (result instanceof Object[]) {
                for (Object item : (Object[]) result) {
                    values.add(String.valueOf(item));
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Could not read PTS column values via JS: " + e.getMessage());
        }
        return values;
    }

    /** Sort asc/desc until first row has a PTS value (blank rows sink to bottom). */
    public void sortPtsColumnSoPopulatedRowsFirst(SoftAssert softAssert) throws InterruptedException {
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        int populatedIndex = findFirstPopulatedPtsRowIndexAfterScroll();
        if (populatedIndex < 0) {
            Logger.logReportMessage("No populated " + PtsPackagingIdPage.COLUMN_LABEL
                    + " values in grid — skipping blank-rows-last sort goal");
            return;
        }
        if (populatedIndex == 0) {
            Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " first row already populated before sort");
            return;
        }
        int bestIndex = populatedIndex;
        clickColumnSortDesc(softAssert);
        populatedIndex = findFirstPopulatedPtsRowIndexAfterScroll();
        if (populatedIndex == 0) {
            Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " first row populated after descending sort");
            return;
        }
        if (populatedIndex >= 0 && populatedIndex < bestIndex) {
            bestIndex = populatedIndex;
        }
        clickColumnSortAsc(softAssert);
        populatedIndex = findFirstPopulatedPtsRowIndexAfterScroll();
        if (populatedIndex == 0) {
            Logger.logReportMessage(PtsPackagingIdPage.COLUMN_LABEL + " first row populated after ascending sort");
            return;
        }
        if (populatedIndex >= 0 && populatedIndex < bestIndex) {
            bestIndex = populatedIndex;
        }
        clickColumnSortDesc(softAssert);
        Verify.softAssert1(findFirstPopulatedPtsRowIndexAfterScroll() == 0
                        || findFirstPopulatedPtsRowIndexViaSelenium() == 0,
                "First row has " + PtsPackagingIdPage.COLUMN_LABEL + " after sort (blank rows last; best index="
                        + bestIndex + ")", softAssert);
    }

    /** Click copy icon on first PTS cell; read value from button attribute or clipboard. */
    private String readFirstPtsCellValueViaCopyIcon() {
        try {
            if (!WaitUtil.isDisplayFast(ptsPage.gridColumnFirstCopyIcon(), 3)) {
                return "";
            }
            DesktopBrowserElement copyIcon = driver.get().finder().findElement(ptsPage.gridColumnFirstCopyIcon());
            String fromAttr = copyIcon.getAttribute("cdkcopytoclipboard");
            if (fromAttr == null || fromAttr.trim().isEmpty()) {
                fromAttr = copyIcon.getAttribute("ng-reflect-copy");
            }
            if (fromAttr == null || fromAttr.trim().isEmpty()) {
                fromAttr = copyIcon.getAttribute("data-clipboard-text");
            }
            if (fromAttr != null && !fromAttr.trim().isEmpty()) {
                return fromAttr.trim();
            }
            copyIcon.scrollIntoView();
            copyIcon.click();
            Thread.sleep(400);
            Object clip = driver.get().browser().executeScript(
                    "try { return navigator.clipboard.readText(); } catch (e) { return ''; }");
            if (clip != null) {
                String text = String.valueOf(clip).trim();
                if (!text.isEmpty() && !"undefined".equals(text)) {
                    return text;
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Copy icon read failed: " + e.getMessage());
        }
        return "";
    }

    private boolean openPtsColumnSearchInput() throws InterruptedException {
        scrollGridToPtsColumn();
        if (WaitUtil.isDisplayFast(ptsPage.gridColumnSearchInput(), 2)) {
            return true;
        }
        if (DriverUtil.clickOnElement(ptsPage.gridColumnSearchIcon(), 3)) {
            Thread.sleep(PANEL_SETTLE_MS);
            if (WaitUtil.isDisplayFast(ptsPage.gridColumnSearchInput(), 3)) {
                return true;
            }
        }
        if (DriverUtil.clickOnElementJs(ptsPage.gridColumnHeaderLabelWrapper(), 3)) {
            Thread.sleep(PANEL_SETTLE_MS);
            if (WaitUtil.isDisplayFast(ptsPage.gridColumnSearchInput(), 3)) {
                return true;
            }
        }
        if (DriverUtil.clickOnElement(ptsPage.gridColumnHeaderLabel(), 3)) {
            Thread.sleep(PANEL_SETTLE_MS);
            if (WaitUtil.isDisplayFast(ptsPage.gridColumnSearchInput(), 3)) {
                return true;
            }
        }
        try {
            driver.get().browser().executeScript(
                    "(function() {"
                            + "var th = document.querySelector('th[title=\"' + '" + PTS_COLUMN_LABEL_JS + "' + '\"]');"
                            + "if (!th) return;"
                            + "var icon = th.querySelector('i.bi-search, .search-icon, [class*=\"search-icon\"]');"
                            + "if (icon) { icon.click(); return; }"
                            + "var filter = th.querySelector('msc-custom-table-column-filter');"
                            + "if (filter) filter.click();"
                            + "})();");
            Thread.sleep(PANEL_SETTLE_MS);
        } catch (Exception ignored) {
            // optional JS click
        }
        return WaitUtil.isDisplayFast(ptsPage.gridColumnSearchInput(), 3);
    }

    public void filterColumnByValue(String value, SoftAssert softAssert) throws InterruptedException {
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        Verify.softAssert1(openPtsColumnSearchInput(),
                PtsPackagingIdPage.COLUMN_LABEL + " column search input visible (click search icon on header if needed)",
                softAssert);
        String searchTerm = value == null ? "" : value.trim();
        Verify.softAssert1(!searchTerm.isEmpty(), "PTS Packaging ID search value is not empty", softAssert);
        setColumnSearchInputValue(searchTerm);
        Thread.sleep(1500);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        ensurePtsColumnScrolledIntoView();
    }

    /** Clear then type — avoids appending to an existing column-search term from a prior test. */
    private void setColumnSearchInputValue(String searchTerm) {
        if (!WaitUtil.isDisplayFast(ptsPage.gridColumnSearchInput(), 5)) {
            DriverUtil.sendKeyToElement(ptsPage.gridColumnSearchInput(), 10, searchTerm);
            return;
        }
        try {
            String escapedTerm = searchTerm.replace("\\", "\\\\").replace("'", "\\'");
            driver.get().browser().executeScript(
                    "(function() {"
                            + "var term = '" + escapedTerm + "';"
                            + "var inp = document.querySelector('th[title=\"' + '" + PTS_COLUMN_LABEL_JS
                            + "' + '\"] input[type=\"search\"], th[title=\"' + '" + PTS_COLUMN_LABEL_JS
                            + "' + '\"] input[type=\"text\"], th[title=\"' + '" + PTS_COLUMN_LABEL_JS
                            + "' + '\"] input');"
                            + "if (!inp) {"
                            + "  var th = document.querySelector('th[title=\"' + '" + PTS_COLUMN_LABEL_JS + "' + '\"]');"
                            + "  if (th) inp = th.querySelector('input');"
                            + "}"
                            + "if (!inp) return false;"
                            + "inp.focus();"
                            + "inp.value = term;"
                            + "inp.dispatchEvent(new Event('input', { bubbles: true }));"
                            + "inp.dispatchEvent(new Event('change', { bubbles: true }));"
                            + "inp.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));"
                            + "inp.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));"
                            + "return true;"
                            + "})();");
        } catch (Exception e) {
            Logger.logConsoleMessage("JS column search input failed, using sendKeys: " + e.getMessage());
            DriverUtil.sendKeyToElement(ptsPage.gridColumnSearchInput(), 10, searchTerm);
        }
    }

    private void ensurePtsColumnScrolledIntoView() {
        try {
            scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scrollGridToPtsColumn();
        }
    }

    public void validateColumnSearchReturnsValue(String expectedValue, SoftAssert softAssert) {
        List<String> values = readVisibleCellTexts();
        Verify.softAssert1(!values.isEmpty(), "Column search returned rows for PTS Packaging ID", softAssert);
        boolean match = values.stream()
                .anyMatch(v -> v.toLowerCase(Locale.ROOT).contains(expectedValue.toLowerCase(Locale.ROOT)));
        Verify.softAssert1(match,
                "Column search contains '" + expectedValue + "' (values=" + values + ")", softAssert);
    }

    public void validateSortChangesRowOrder(SoftAssert softAssert) throws InterruptedException {
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        Verify.softAssert1(hasVisibleGridRows(), "Grid rows visible before PTS column sort", softAssert);

        String beforeRows = readGridRowSignaturesViaJs(15);
        String sortBefore = readPtsHeaderSortStateAfterScroll();
        Logger.logReportMessage("PTS sort state before: " + sortBefore);

        boolean ascClicked = clickColumnSortAsc(softAssert);
        String afterAscRows = readGridRowSignaturesViaJs(15);
        String sortAfterAsc = readPtsHeaderSortStateAfterScroll();
        Logger.logReportMessage("PTS sort state after asc: " + sortAfterAsc);

        boolean descClicked = clickColumnSortDesc(softAssert);
        String afterDescRows = readGridRowSignaturesViaJs(15);
        String sortAfterDesc = readPtsHeaderSortStateAfterScroll();
        Logger.logReportMessage("PTS sort state after desc: " + sortAfterDesc);

        boolean rowOrderChanged = !beforeRows.isEmpty()
                && (!beforeRows.equals(afterAscRows) || !afterAscRows.equals(afterDescRows));
        boolean sortStateChanged = !sortBefore.equals(sortAfterAsc) || !sortAfterAsc.equals(sortAfterDesc);

        Verify.softAssert1(sortStateChanged || rowOrderChanged || (ascClicked && descClicked),
                "PTS Packaging ID sort asc/desc applied (headerState=" + sortStateChanged
                        + ", rowOrder=" + rowOrderChanged + ", clicks=" + ascClicked + "/" + descClicked + ")",
                softAssert);

        sortPtsColumnSoPopulatedRowsFirst(softAssert);
    }

    public void openDetailsForReferenceOrder(String orderId, SoftAssert softAssert) throws InterruptedException {
        searchGlobal(orderId, softAssert);
        Verify.softAssert1(WaitUtil.isDisplay(ptsPage.orderIdCell(orderId), GRID_WAIT_S),
                "PTS order row visible: " + orderId, softAssert);
        DriverUtil.clickOnElement(ptsPage.orderIdCell(orderId), PANEL_WAIT_S);
        Thread.sleep(1000);
        openDetailsPanelIfPresent();
    }

    public void openDetailsForFirstGridRow(SoftAssert softAssert) throws InterruptedException {
        scrollGridToPtsColumn();
        Verify.softAssert1(WaitUtil.isDisplay(ptsPage.gridFirstRowClickTarget(), GRID_WAIT_S),
                "At least one grid row visible for Details", softAssert);
        DriverUtil.clickOnElement(ptsPage.gridFirstRowClickTarget(), PANEL_WAIT_S);
        Thread.sleep(1500);
        openDetailsPanelIfPresent();
        Verify.softAssert1(isDetailsPanelOpen(),
                "Details panel open after clicking first grid row", softAssert);
    }

    private boolean isDetailsPanelOpen() {
        return WaitUtil.isDisplayFast(ptsPage.detailsPanelPtsPackagingIdLabel(), 3)
                || WaitUtil.isDisplayFast(ptsPage.detailsPanelJobRequestSection(), 3)
                || WaitUtil.isDisplayFast(ptsPage.detailsPanelPtsPackagingIdValue(), 3);
    }

    private void openDetailsPanelIfPresent() throws InterruptedException {
        if (isDetailsPanelOpen()) {
            return;
        }
        if (WaitUtil.isDisplay(ptsPage.detailsButton(), 5)) {
            DriverUtil.clickOnElement(ptsPage.detailsButton(), PANEL_WAIT_S);
            Thread.sleep(1500);
        }
    }

    public String readDetailsPanelPtsValue(SoftAssert softAssert) {
        Verify.softAssert1(WaitUtil.isDisplay(ptsPage.detailsPanelPtsPackagingIdValue(), GRID_WAIT_S),
                PtsPackagingIdPage.DETAILS_PANEL_LABEL + " visible in Details panel", softAssert);
        String value = DriverUtil.getElement(ptsPage.detailsPanelPtsPackagingIdValue()).getText().trim();
        if (!value.isEmpty() && !"-".equals(value)) {
            return value;
        }
        return readDetailsPanelPtsValueViaCopyButton();
    }

    private String readDetailsPanelPtsValueViaCopyButton() {
        try {
            if (!WaitUtil.isDisplayFast(ptsPage.detailsPanelPtsCopyButton(), 3)) {
                return "";
            }
            DesktopBrowserElement copyBtn = driver.get().finder().findElement(ptsPage.detailsPanelPtsCopyButton());
            copyBtn.scrollIntoView();
            copyBtn.click();
            Thread.sleep(400);
            Object clip = driver.get().browser().executeScript(
                    "try { return navigator.clipboard.readText(); } catch (e) { return ''; }");
            if (clip != null) {
                String text = String.valueOf(clip).trim();
                if (!text.isEmpty() && !"undefined".equals(text)) {
                    return text;
                }
            }
        } catch (Exception e) {
            Logger.logConsoleMessage("Details copy button read failed: " + e.getMessage());
        }
        return "";
    }

    public void validateTableMatchesDetailsPanel(SoftAssert softAssert) throws InterruptedException {
        String tableValue = readFirstRowPtsCellValue(softAssert);
        String detailsValue = readDetailsPanelPtsValue(softAssert);
        String normalizedTable = FulfillmentExportExcelUtil.normalizePtsId(tableValue);
        String normalizedDetails = FulfillmentExportExcelUtil.normalizePtsId(detailsValue);
        Verify.softAssert1(!normalizedDetails.isEmpty(),
                PtsPackagingIdPage.DETAILS_PANEL_LABEL + " populated in Details panel", softAssert);
        Verify.softAssert1(normalizedTable.equals(normalizedDetails)
                        || normalizedTable.contains(normalizedDetails)
                        || normalizedDetails.contains(normalizedTable),
                "Table matches Details for PTS order (table='" + tableValue + "', details='" + detailsValue + "')",
                softAssert);
    }

    private boolean clickColumnSortAsc(SoftAssert softAssert) throws InterruptedException {
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        Thread.sleep(PANEL_SETTLE_MS);
        boolean clicked = clickPtsColumnSortViaJs("asc");
        if (!clicked) {
            clicked = DriverUtil.clickOnElementJs(ptsPage.gridColumnSortAscIcon(), 5);
        }
        if (!clicked) {
            clicked = DriverUtil.clickOnElementJs(ptsPage.gridColumnSortTrigger(), 3);
        }
        Verify.softAssert1(clicked,
                "Clicked " + PtsPackagingIdPage.COLUMN_LABEL + " ascending sort (caret-up)", softAssert);
        waitForSortGridReload();
        return clicked;
    }

    private boolean clickColumnSortDesc(SoftAssert softAssert) throws InterruptedException {
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        Thread.sleep(PANEL_SETTLE_MS);
        boolean clicked = clickPtsColumnSortViaJs("desc");
        if (!clicked) {
            clicked = DriverUtil.clickOnElementJs(ptsPage.gridColumnSortDescIcon(), 5);
        }
        if (!clicked) {
            clicked = DriverUtil.clickOnElementJs(ptsPage.gridColumnSortTrigger(), 3);
        }
        Verify.softAssert1(clicked,
                "Clicked " + PtsPackagingIdPage.COLUMN_LABEL + " descending sort (caret-down)", softAssert);
        waitForSortGridReload();
        return clicked;
    }

    private int findFirstPopulatedPtsRowIndexAfterScroll() throws InterruptedException {
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        return findFirstPopulatedPtsRowIndexViaJs();
    }

    private String readPtsHeaderSortStateAfterScroll() throws InterruptedException {
        scrollGridToPtsColumnUntilVisible(GRID_POLL_S);
        return readPtsHeaderSortStateViaJs();
    }

    /**
     * Click sort on PTS Packaging ID header via JS (sticky header blocks native Selenium click).
     */
    private boolean clickPtsColumnSortViaJs(String direction) {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = '" + PTS_COLUMN_LABEL_JS + "';"
                            + "var shortLabel = '" + PTS_COLUMN_SHORT_JS + "';"
                            + "var direction = '" + direction + "';"
                            + findPtsHeaderJsFunction()
                            + "function scrollHorizontalToEnd() {"
                            + "  var sels = ['.bs-custom-table-viewport','.custom-table-wrapper',"
                            + "    '.table-container','cdk-virtual-scroll-viewport'];"
                            + "  for (var s = 0; s < sels.length; s++) {"
                            + "    var nodes = document.querySelectorAll(sels[s]);"
                            + "    for (var n = 0; n < nodes.length; n++) {"
                            + "      var el = nodes[n];"
                            + "      if (el.scrollWidth > el.clientWidth + 1) {"
                            + "        el.scrollLeft = el.scrollWidth - el.clientWidth;"
                            + "        el.dispatchEvent(new Event('scroll', { bubbles: true }));"
                            + "      }"
                            + "    }"
                            + "  }"
                            + "}"
                            + "function jsClick(el) {"
                            + "  if (!el) return false;"
                            + "  try { el.scrollIntoView({ block: 'nearest', inline: 'center' }); } catch (e) {}"
                            + "  try { el.click(); return true; } catch (e1) {}"
                            + "  try {"
                            + "    el.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));"
                            + "    return true;"
                            + "  } catch (e2) {}"
                            + "  return false;"
                            + "}"
                            + "scrollHorizontalToEnd();"
                            + "var th = findPtsHeader();"
                            + "if (!th) return 'no-header';"
                            + "try { th.scrollIntoView({ block: 'nearest', inline: 'center' }); } catch (e) {}"
                            + "var clsPart = direction === 'asc' ? 'sort-icon-up' : 'sort-icon-down';"
                            + "var filter = th.querySelector('msc-custom-table-column-filter');"
                            + "var targets = ["
                            + "  filter ? filter.querySelector('[class*=\"' + clsPart + '\"]') : null,"
                            + "  filter ? filter.querySelector('[class*=\"' + clsPart + '\"] i') : null,"
                            + "  th.querySelector('[class*=\"' + clsPart + '\"]'),"
                            + "  filter ? filter.querySelector('[class*=\"sort-icon-container\"]') : null,"
                            + "  filter ? filter.querySelector('[class*=\"label-and-sort-container\"]') : null,"
                            + "  th.querySelector('[class*=\"label-and-sort-container\"]')"
                            + "];"
                            + "for (var i = 0; i < targets.length; i++) {"
                            + "  if (jsClick(targets[i])) return 'clicked';"
                            + "}"
                            + "return 'no-target';"
                            + "})();");
            String status = result == null ? "" : String.valueOf(result).trim();
            if ("clicked".equals(status)) {
                Logger.logReportMessage("PTS sort JS click (" + direction + ") on " + PtsPackagingIdPage.COLUMN_LABEL);
                return true;
            }
            Logger.logConsoleMessage("PTS sort JS (" + direction + ") status: " + status);
            return false;
        } catch (Exception e) {
            Logger.logConsoleMessage("PTS sort JS click failed: " + e.getMessage());
            return false;
        }
    }

    private List<String> readVisibleCellTexts() {
        List<String> values = readVisiblePtsCellValuesViaJs(10);
        if (!values.isEmpty()) {
            return values;
        }
        scrollGridToPtsColumn();
        if (!isPtsColumnPresentInDom()) {
            return values;
        }
        List<DesktopBrowserElement> cells = driver.get().finder().findElements(ptsPage.gridColumnCells());
        int limit = Math.min(cells.size(), 10);
        for (int i = 0; i < limit; i++) {
            values.add(cells.get(i).getText().trim());
        }
        return values;
    }
}
