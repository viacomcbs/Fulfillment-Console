package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.Bsd29967Page;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnsUtil;
import com.synergy.core.driver.By;

import static com.paramount.test.ff.common.base.BaseTest.driver;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/** Environment filter + Order start date column + multi-submission row scan for BSD-29967. */
public class Bsd29967ColumnUtil {

    private static final int GRID_WAIT_S = 20;
    private static final int MAX_SCROLL_PASSES = 4;
    private static final int FIRST_VISIBLE_ROWS_SCAN_PASSES = 2;

    private final Bsd29967Page bsd29967Page = new Bsd29967Page();
    private final ManageColumnsUtil manageColumns = new ManageColumnsUtil();

    public void applyFirstNonZeroEnvironmentFilter(LeftFilterPanelUtil filterUtil, SoftAssert softAssert)
            throws InterruptedException {
        if (Bsd29967SessionHelper.isEnvironmentFilterApplied()) {
            Logger.logReportMessage("Environment filter already applied: " + Bsd29967SessionHelper.getEnvironmentLabel());
            return;
        }
        String filterName = OrdersLeftFilter.ENVIRONMENT.getDisplayName();
        filterUtil.ensureLeftFilterPanelOpen();
        filterUtil.ensureOrdersDataLoaded(softAssert);
        if (!filterUtil.waitForFilterHeaderVisible(filterName, 30)) {
            Verify.softAssert1(false, filterName + " filter header not visible in expanded left panel", softAssert);
            return;
        }
        filterUtil.expandFilter(filterName);
        String optionLabel = filterUtil.resolveFirstSelectableFilterOption(filterName);
        Verify.softAssert1(optionLabel != null && !optionLabel.isEmpty(),
                filterName + " has a selectable non-zero option", softAssert);
        if (optionLabel == null) {
            return;
        }
        filterUtil.selectFilterOption(filterName, optionLabel);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(1500);
        Bsd29967SessionHelper.setEnvironmentLabel(optionLabel);
        Bsd29967SessionHelper.markEnvironmentFilterApplied();
        Logger.logReportMessage("Applied Environment filter: " + optionLabel);
    }

    public void applyEnvironmentFilter(LeftFilterPanelUtil filterUtil, String optionLabel, SoftAssert softAssert)
            throws InterruptedException {
        String filterName = OrdersLeftFilter.ENVIRONMENT.getDisplayName();
        filterUtil.ensureLeftFilterPanelOpen();
        if (filterUtil.hasActiveFilterChips()) {
            filterUtil.clearAllActiveFiltersIfPresent();
        }
        filterUtil.ensureOrdersDataLoaded(softAssert);
        if (!filterUtil.waitForFilterHeaderVisible(filterName, 30)) {
            Verify.softAssert1(false, filterName + " filter header not visible in expanded left panel", softAssert);
            return;
        }
        filterUtil.expandFilter(filterName);
        Verify.softAssert1(optionLabel != null && !optionLabel.isEmpty(),
                filterName + " option label provided", softAssert);
        if (optionLabel == null || optionLabel.isEmpty()) {
            return;
        }
        filterUtil.selectFilterOption(filterName, optionLabel);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(1500);
        scrollOrdersGridToTop();
        Bsd29967SessionHelper.setEnvironmentLabel(optionLabel);
        Bsd29967SessionHelper.markEnvironmentFilterApplied();
        Logger.logReportMessage("Applied Environment filter: " + optionLabel);
    }

    public void clearEnvironmentFilterForNextIteration(LeftFilterPanelUtil filterUtil) throws InterruptedException {
        filterUtil.clearAllActiveFiltersIfPresent();
        scrollOrdersGridToTop();
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
    }

    public void returnToOrdersGrid(LeftFilterPanelUtil filterUtil) throws InterruptedException {
        filterUtil.navigateToTab(ConsoleTab.ORDERS);
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        filterUtil.ensureLeftFilterPanelOpen();
        ensureOrderStartDateColumnVisible();
        scrollOrdersGridToTop();
    }

    /** Quick scan of first visible rows only — used to skip workflows with no multi-submission icon. */
    public boolean hasMultiSubmissionIconInFirstVisibleRows() throws InterruptedException {
        ensureOrderStartDateColumnVisible();
        scrollOrdersGridToTop();
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(500);

        if (WaitUtil.isDisplay(bsd29967Page.orderStartDateInfoIconByIndex(1), 5)) {
            return true;
        }
        for (int pass = 0; pass < FIRST_VISIBLE_ROWS_SCAN_PASSES; pass++) {
            if (findRowIndexWithOrderStartDateInfoIcon() >= 0) {
                return true;
            }
            if (!scrollOrdersGridVertically()) {
                break;
            }
            Thread.sleep(400);
        }
        return false;
    }

    public void enableOrderStartDateColumnIfNeeded(SoftAssert softAssert) throws InterruptedException {
        if (Bsd29967SessionHelper.isOrderStartDateColumnEnabled()) {
            return;
        }
        manageColumns.ensureColumnEnabledForView(softAssert, ManageColumnOptions.ORDER_START_DATE);
        ensureOrderStartDateColumnVisible();
        Bsd29967SessionHelper.markOrderStartDateColumnEnabled();
    }

    public int findFirstMultiSubmissionRowIndex(SoftAssert softAssert) throws InterruptedException {
        ensureOrderStartDateColumnVisible();
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(1000);

        int rowIndex = findRowIndexWithOrderStartDateInfoIcon();
        if (rowIndex < 0) {
            rowIndex = scanForMultiSubmissionRowWithVerticalScroll();
        }

        boolean iconVisible = WaitUtil.isDisplay(bsd29967Page.orderStartDateInfoIconByIndex(1), 3)
                || WaitUtil.isDisplay(bsd29967Page.rowWithOrderStartDateInfoIcon(1), 3);
        Verify.softAssert1(rowIndex >= 0 || iconVisible,
                "At least one Orders row shows (i) icon in " + Bsd29967Page.MULTI_SUBMISSION_COLUMN
                        + " (" + Bsd29967Page.ORDER_START_DATE_COL_CLASS + ")", softAssert);
        if (rowIndex >= 0) {
            Bsd29967SessionHelper.setMultiSubmissionRowIndex(rowIndex);
            Logger.logReportMessage("Found multi-submission (i) icon on row " + (rowIndex + 1));
        } else if (iconVisible) {
            Logger.logReportMessage("Located (i) icon in Order start date column (row index resolved on click)");
        }
        return rowIndex;
    }

    public int countMultiSubmissionIconsInGrid() throws InterruptedException {
        ensureOrderStartDateColumnVisible();
        scrollOrdersGridToTop();
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(400);
        try {
            int count = driver.get().finder().findElements(bsd29967Page.orderStartDateInfoIcons()).size();
            Logger.logReportMessage("Multi-submission (i) icons visible in grid: " + count);
            return count;
        } catch (Exception e) {
            Logger.logConsoleMessage("Count (i) icons failed: " + e.getMessage());
            return 0;
        }
    }

    public void openDetailsForMultiSubmissionRow(SoftAssert softAssert) throws InterruptedException {
        openDetailsForMultiSubmissionIconIndex(1, softAssert);
    }

    public boolean openDetailsForMultiSubmissionIconIndex(int oneBasedIconIndex, SoftAssert softAssert)
            throws InterruptedException {
        ensureOrderStartDateColumnVisible();
        WaitUtil.waitForJSToLoad(GRID_WAIT_S);
        Thread.sleep(500);

        if (!WaitUtil.isDisplay(bsd29967Page.orderStartDateInfoIconByIndex(oneBasedIconIndex), 5)) {
            scrollOrdersGridVertically();
            Thread.sleep(400);
        }
        if (!WaitUtil.isDisplay(bsd29967Page.orderStartDateInfoIconByIndex(oneBasedIconIndex), GRID_WAIT_S)) {
            Logger.logReportMessage("(i) icon index " + oneBasedIconIndex + " not found in Orders grid");
            return false;
        }

        int rowIndex = resolveRowIndexFromInfoIcon(oneBasedIconIndex);
        if (rowIndex >= 0) {
            Bsd29967SessionHelper.setMultiSubmissionRowIndex(rowIndex);
        }
        clickRowWithInfoIcon(oneBasedIconIndex, softAssert);
        Thread.sleep(2000);
        WaitUtil.waitForJSToLoad(15);
        return isDetailsPanelOpen();
    }

    /**
     * Finds the first row with (i) icon strictly inside Order start date ({@code start-time-col}).
     */
    private int findRowIndexWithOrderStartDateInfoIcon() {
        if (!WaitUtil.isDisplay(bsd29967Page.orderStartDateInfoIconByIndex(1), GRID_WAIT_S)) {
            return -1;
        }
        int rowIndex = resolveRowIndexFromInfoIcon(1);
        if (rowIndex >= 0) {
            Logger.logReportMessage("Located (i) icon in Order start date column on row " + (rowIndex + 1));
        }
        return rowIndex;
    }

    private int resolveRowIndexFromInfoIcon(int oneBasedIconIndex) {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var iconIdx = " + (oneBasedIconIndex - 1) + ";"
                            + "var icons = document.querySelectorAll("
                            + "'#orderTable tbody td.start-time-col i.bi-info-circle');"
                            + "if (!icons.length || iconIdx < 0 || iconIdx >= icons.length) return -1;"
                            + "var row = icons[iconIdx].closest('tr');"
                            + "if (!row) return -1;"
                            + "var rows = document.querySelectorAll('#orderTable tbody tr:not(.inner)');"
                            + "for (var i = 0; i < rows.length; i++) {"
                            + "  if (rows[i] === row) return i;"
                            + "}"
                            + "return -1;"
                            + "})()");
            return parseRowIndex(result);
        } catch (Exception e) {
            Logger.logConsoleMessage("Resolve row from (i) icon failed: " + e.getMessage());
            return -1;
        }
    }

    private void clickRowWithInfoIcon(int oneBasedIconIndex, SoftAssert softAssert) throws InterruptedException {
        By rowLocator = bsd29967Page.rowWithOrderStartDateInfoIcon(oneBasedIconIndex);
        Verify.softAssert1(WaitUtil.isDisplay(rowLocator, GRID_WAIT_S),
                "Row with (i) icon in Order start date is visible", softAssert);
        if (!WaitUtil.isDisplay(rowLocator, 3)) {
            return;
        }
        DriverUtil.scrollToElement(rowLocator);
        Thread.sleep(500);

        By clickTarget = bsd29967Page.multiSubmissionRowClickTarget(oneBasedIconIndex);
        boolean clicked = false;
        if (WaitUtil.isDisplay(clickTarget, GRID_WAIT_S)) {
            DriverUtil.scrollToElement(clickTarget);
            clicked = DriverUtil.clickOnElementJs(clickTarget, 0);
            if (!clicked) {
                clicked = DriverUtil.clickOnElement(clickTarget, GRID_WAIT_S);
            }
            Logger.logReportMessage("Clicked row with (i) icon in Order start date");
        }
        if (!clicked) {
            clicked = DriverUtil.clickOnElement(rowLocator, GRID_WAIT_S);
            Logger.logReportMessage("Fallback: clicked full row with (i) icon");
        }

        Verify.softAssert1(clicked,
                "Clicked row with (i) icon in Order start date to open Details panel", softAssert);
        if (!clicked) {
            return;
        }
        Thread.sleep(1500);
        openDetailsPanelIfPresent();
        Verify.softAssert1(isDetailsPanelOpen(),
                "Details panel open after clicking row with (i) icon", softAssert);
    }

    private int scanForMultiSubmissionRowWithVerticalScroll() throws InterruptedException {
        for (int pass = 0; pass < MAX_SCROLL_PASSES; pass++) {
            int rowIndex = findRowIndexWithOrderStartDateInfoIcon();
            if (rowIndex >= 0) {
                return rowIndex;
            }
            if (!scrollOrdersGridVertically()) {
                break;
            }
            Thread.sleep(600);
        }
        return -1;
    }

    private void clickDetailsCellOnRow(int oneBasedRowIndex, SoftAssert softAssert) throws InterruptedException {
        scrollRowIntoView(oneBasedRowIndex - 1);
        Thread.sleep(800);

        By clickTarget = bsd29967Page.rowDetailsClickTarget(oneBasedRowIndex);
        boolean clicked = false;
        if (WaitUtil.isDisplay(clickTarget, GRID_WAIT_S)) {
            DriverUtil.scrollToElement(clickTarget);
            clicked = DriverUtil.clickOnElementJs(clickTarget, 0);
            if (!clicked) {
                clicked = DriverUtil.clickOnElement(clickTarget, GRID_WAIT_S);
            }
            Logger.logReportMessage("Clicked Details cell on row " + oneBasedRowIndex);
        }

        if (!clicked) {
            By rowLocator = bsd29967Page.gridRowByIndex(oneBasedRowIndex);
            Verify.softAssert1(WaitUtil.isDisplay(rowLocator, GRID_WAIT_S),
                    "Grid row " + oneBasedRowIndex + " visible for Details panel", softAssert);
            if (WaitUtil.isDisplay(rowLocator, 3)) {
                DriverUtil.scrollToElement(rowLocator);
                clicked = DriverUtil.clickOnElement(rowLocator, GRID_WAIT_S);
                Logger.logReportMessage("Fallback: clicked full row " + oneBasedRowIndex);
            }
        }

        Verify.softAssert1(clicked,
                "Clicked non-Status cell on row " + oneBasedRowIndex + " to open Details panel", softAssert);
        if (!clicked) {
            return;
        }

        Thread.sleep(1500);
        openDetailsPanelIfPresent();
        Verify.softAssert1(isDetailsPanelOpen(),
                "Details panel open after clicking row " + oneBasedRowIndex, softAssert);
    }

    private void openDetailsPanelIfPresent() throws InterruptedException {
        if (isDetailsPanelOpen()) {
            return;
        }
        String url = driver.get().browser().getCurrentUrl();
        if (url != null && url.contains(Bsd29967Page.FOCUS_URL_FRAGMENT)) {
            return;
        }
        if (WaitUtil.isDisplay(bsd29967Page.detailsButton(), 5)) {
            DriverUtil.clickOnElement(bsd29967Page.detailsButton(), GRID_WAIT_S);
            Thread.sleep(1500);
        }
    }

    private boolean isDetailsPanelOpen() {
        String url = driver.get().browser().getCurrentUrl();
        if (url != null && url.contains(Bsd29967Page.FOCUS_URL_FRAGMENT)) {
            return true;
        }
        return WaitUtil.isDisplayFast(bsd29967Page.detailsPanelDsidSection(), 3)
                || WaitUtil.isDisplayFast(bsd29967Page.detailsPanelDsIdsValues(), 3);
    }

    /** Scroll only Order start date column into view — do not scroll to other date columns. */
    private void ensureOrderStartDateColumnVisible() {
        try {
            if (WaitUtil.isDisplay(bsd29967Page.orderStartDateColumnHeader(), 3)) {
                DriverUtil.scrollToElement(bsd29967Page.orderStartDateColumnHeader());
            }
            driver.get().browser().executeScript(
                    "(function() {"
                            + "var label = '" + Bsd29967Page.MULTI_SUBMISSION_COLUMN + "';"
                            + "var colClass = '" + Bsd29967Page.ORDER_START_DATE_COL_CLASS + "';"
                            + "var th = document.querySelector("
                            + "  'th[title=\"' + label + '\"], th.' + colClass + ', th[class*=\"' + colClass + '\"]');"
                            + "if (!th) {"
                            + "  var ths = document.querySelectorAll('th');"
                            + "  for (var i = 0; i < ths.length; i++) {"
                            + "    var text = (ths[i].innerText || ths[i].textContent || '').trim();"
                            + "    if (text === label || text.indexOf(label) >= 0) { th = ths[i]; break; }"
                            + "  }"
                            + "}"
                            + "if (!th) return false;"
                            + "th.scrollIntoView({block:'nearest', inline:'center'});"
                            + "return true;"
                            + "})()");
            Thread.sleep(400);
        } catch (Exception e) {
            Logger.logConsoleMessage("Order start date column visibility failed: " + e.getMessage());
        }
    }

    private void scrollRowIntoView(int zeroBasedRowIndex) {
        try {
            driver.get().browser().executeScript(
                    "(function() {"
                            + "var rowIdx = " + zeroBasedRowIndex + ";"
                            + "var rows = document.querySelectorAll('#orderTable tbody tr:not(.inner)');"
                            + "if (!rows.length) {"
                            + "  rows = document.querySelectorAll('tbody tr.row:not(.inner)');"
                            + "}"
                            + "if (rowIdx < 0 || rowIdx >= rows.length) return false;"
                            + "var row = rows[rowIdx];"
                            + "row.scrollIntoView({block:'center', inline:'nearest'});"
                            + "var vp = row.closest('cdk-virtual-scroll-viewport')"
                            + "  || document.querySelector('.cdk-virtual-scroll-viewport, .custom-table-wrapper');"
                            + "if (vp) {"
                            + "  var rowRect = row.getBoundingClientRect();"
                            + "  var vpRect = vp.getBoundingClientRect();"
                            + "  if (rowRect.top < vpRect.top || rowRect.bottom > vpRect.bottom) {"
                            + "    vp.scrollTop += (rowRect.top - vpRect.top) - (vpRect.height / 3);"
                            + "  }"
                            + "}"
                            + "return true;"
                            + "})()");
            Thread.sleep(400);
        } catch (Exception e) {
            Logger.logConsoleMessage("Scroll row into view failed: " + e.getMessage());
        }
    }

    private boolean scrollOrdersGridVertically() {
        try {
            Object result = driver.get().browser().executeScript(
                    "(function() {"
                            + "var vp = document.querySelector("
                            + "  '#orderTable .cdk-virtual-scroll-viewport, .cdk-virtual-scroll-viewport,"
                            + "   .custom-table-wrapper, .table-wrapper');"
                            + "if (vp && vp.scrollHeight > vp.clientHeight + 1) {"
                            + "  var before = vp.scrollTop;"
                            + "  vp.scrollTop = Math.min(vp.scrollTop + 320, vp.scrollHeight);"
                            + "  return vp.scrollTop > before;"
                            + "}"
                            + "return false;"
                            + "})()");
            return Boolean.TRUE.equals(result) || "true".equals(String.valueOf(result));
        } catch (Exception e) {
            Logger.logConsoleMessage("Orders grid vertical scroll failed: " + e.getMessage());
            return false;
        }
    }

    private void scrollOrdersGridToTop() {
        try {
            driver.get().browser().executeScript(
                    "(function() {"
                            + "var vp = document.querySelector("
                            + "  '#orderTable .cdk-virtual-scroll-viewport, .cdk-virtual-scroll-viewport,"
                            + "   .custom-table-wrapper, .table-wrapper');"
                            + "if (vp) { vp.scrollTop = 0; return true; }"
                            + "return false;"
                            + "})()");
            Thread.sleep(300);
        } catch (Exception e) {
            Logger.logConsoleMessage("Orders grid scroll-to-top failed: " + e.getMessage());
        }
    }

    private int parseRowIndex(Object result) {
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        if (result == null) {
            return -1;
        }
        String text = String.valueOf(result).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return -1;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
