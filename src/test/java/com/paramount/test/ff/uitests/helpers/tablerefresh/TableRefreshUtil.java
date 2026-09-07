package com.paramount.test.ff.uitests.helpers.tablerefresh;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.pageobjects.TableRefreshHeaderPage;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.synergy.core.driver.By;
import com.synergy.core.driver.elements.DesktopBrowserElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.DEFAULT_WAIT_SECONDS;

/**
 * UI actions and assertions for BSD-29582 Refresh table button (toolbar next to Export).
 *
 * <p>Duplicate-row checks intentionally allow total count to increase (live updates on Today)
 * but fail when the same business key appears more than once in the grid.
 */
public class TableRefreshUtil extends BaseTest {

    private static final int API_WAIT_SECONDS = 20;
    private static final int GRID_STABLE_POLLS = 4;
    private static final int GRID_STABLE_POLL_MS = 500;
    private static final int VISIBLE_KEY_SAMPLE = 30;

    private final TableRefreshHeaderPage headerPage = new TableRefreshHeaderPage();
    private final LeftFilterPanelUtil leftFilterPanelUtil = new LeftFilterPanelUtil();
    private final TableRefreshNetworkUtil networkUtil = new TableRefreshNetworkUtil();
    private final TableRefreshColumnUtil columnUtil = new TableRefreshColumnUtil();

    public void validateRefreshTableButtonVisibleNearExport(SoftAssert softAssert) {
        Verify.softAssert1(WaitUtil.isDisplay(headerPage.tableButtonSection(), DEFAULT_WAIT_SECONDS),
                "Table button section (Export / Refresh table toolbar) is visible", softAssert);
        Verify.softAssert1(WaitUtil.isDisplay(headerPage.exportButton(), DEFAULT_WAIT_SECONDS),
                "Export button is visible in table toolbar", softAssert);
        Verify.softAssert1(WaitUtil.isDisplay(headerPage.refreshButton(), DEFAULT_WAIT_SECONDS),
                "Refresh table button (refresh-table-btn) is visible near Export", softAssert);
        Verify.softAssert1(WaitUtil.isDisplay(headerPage.refreshTableButtonLabel(), DEFAULT_WAIT_SECONDS),
                "Refresh table button shows 'Refresh table' label", softAssert);
        Verify.softAssert1(WaitUtil.isDisplay(headerPage.refreshIcon(), DEFAULT_WAIT_SECONDS),
                "Refresh icon (bi-arrow-repeat) is visible on Refresh table button", softAssert);
        Logger.logReportMessage("Refresh table button is visible next to Export in table toolbar");
    }

    public void validateRefreshTriggersFilterOrder(SoftAssert softAssert) throws InterruptedException {
        networkUtil.installCaptureHook();
        networkUtil.clearCapture();
        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
        waitForGridCountStable();
        clickRefreshButton(softAssert);
        boolean called = networkUtil.waitForFilterOrderCall(API_WAIT_SECONDS);
        waitForGridCountStable();
        int count = networkUtil.getFilterOrderCount();
        List<String> failures = networkUtil.readApiFailures();
        Verify.softAssert1(called && count >= 1,
                "filterOrder API called after Orders refresh click (count=" + count + ")", softAssert);
        Verify.softAssert1(failures.isEmpty(),
                "No failed API responses after Orders refresh (failures=" + failures + ")", softAssert);
        Logger.logReportMessage("Orders refresh triggered filterOrder — count=" + count);
    }

    public void ensureLineItemsGridLoaded(SoftAssert softAssert) throws InterruptedException {
        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
        int count = leftFilterPanelUtil.getTableRecordCount();
        Verify.softAssert1(count >= 0,
                "Line Items grid results count available (count=" + count + ")", softAssert);
    }

    public void validateRefreshTriggersLineItemViewLanding(SoftAssert softAssert) throws InterruptedException {
        networkUtil.installCaptureHook();
        networkUtil.clearCapture();
        ensureLineItemsGridLoaded(softAssert);
        waitForGridCountStable();
        clickRefreshButton(softAssert);
        boolean called = networkUtil.waitForLineItemViewLandingCall(API_WAIT_SECONDS);
        waitForGridCountStable();
        int count = networkUtil.getLineItemViewLandingCount();
        List<String> failures = networkUtil.readApiFailures();
        Verify.softAssert1(called && count >= 1,
                "LineItemViewLanding API called after Line Items refresh click (count=" + count + ")", softAssert);
        Verify.softAssert1(failures.isEmpty(),
                "No failed API responses after Line Items refresh (failures=" + failures + ")", softAssert);
        Logger.logReportMessage("Line Items refresh triggered LineItemViewLanding — count=" + count);
    }

    public void validateRefreshDoesNotDuplicateOrdersRows(SoftAssert softAssert) throws InterruptedException {
        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
        columnUtil.ensureOrderIdColumnVisible(softAssert);
        validateRefreshDoesNotDuplicateRows(
                softAssert,
                headerPage.visibleOrderIdCells(),
                TableRefreshColumnUtil.ORDER_ID,
                "Orders");
    }

    public void validateRefreshDoesNotDuplicateLineItemsRows(SoftAssert softAssert) throws InterruptedException {
        ensureLineItemsGridLoaded(softAssert);
        columnUtil.ensureLineItemIdColumnVisible(softAssert);
        validateRefreshDoesNotDuplicateRows(
                softAssert,
                headerPage.visibleLineItemIdCells(),
                TableRefreshColumnUtil.LINE_ITEM_ID,
                "Line Items");
    }

    /**
     * Verifies refresh replaces grid data instead of appending duplicates.
     * Uses business keys: Order ID (Orders) or LineItem ID (Line Items).
     * Total count may increase when live records arrive on Today — that is expected.
     */
    private void validateRefreshDoesNotDuplicateRows(SoftAssert softAssert, By rowKeyLocator,
            String keyColumnLabel, String tabLabel) throws InterruptedException {
        waitForGridCountStable();
        int countBefore = leftFilterPanelUtil.getTableRecordCount();
        Verify.softAssert1(countBefore >= 0,
                tabLabel + " table record count available before refresh (count=" + countBefore + ")", softAssert);

        List<String> keysBeforeRefresh = collectVisibleRowKeys(rowKeyLocator, keyColumnLabel);
        Verify.softAssert1(!keysBeforeRefresh.isEmpty(),
                tabLabel + " sampled at least one visible " + keyColumnLabel + " before refresh", softAssert);

        Set<String> uniqueBefore = new LinkedHashSet<>(keysBeforeRefresh);
        Logger.logReportMessage(tabLabel + " before refresh — total=" + countBefore
                + ", sampled " + keyColumnLabel + " keys=" + keysBeforeRefresh.size()
                + ", unique=" + uniqueBefore.size());

        clickRefreshButton(softAssert);
        waitForGridCountStable();
        assertNoDuplicateRowKeys(softAssert, rowKeyLocator, keyColumnLabel, tabLabel, "after 1st refresh");

        int countAfterFirst = leftFilterPanelUtil.getTableRecordCount();
        List<String> keysAfterFirst = collectVisibleRowKeys(rowKeyLocator, keyColumnLabel);
        assertKeysFromBeforeAppearAtMostOnce(softAssert, uniqueBefore, keysAfterFirst, keyColumnLabel, tabLabel);

        clickRefreshButton(softAssert);
        waitForGridCountStable();
        assertNoDuplicateRowKeys(softAssert, rowKeyLocator, keyColumnLabel, tabLabel, "after 2nd refresh");

        int countAfterSecond = leftFilterPanelUtil.getTableRecordCount();
        List<String> keysAfterSecond = collectVisibleRowKeys(rowKeyLocator, keyColumnLabel);
        assertKeysFromBeforeAppearAtMostOnce(softAssert, uniqueBefore, keysAfterSecond, keyColumnLabel, tabLabel);

        if (countBefore > 0 && countAfterSecond > 0) {
            Verify.softAssert1(countAfterSecond < countBefore * 2,
                    tabLabel + " total count did not double after refresh"
                            + " (before=" + countBefore + ", after2nd=" + countAfterSecond
                            + ") — indicates append-instead-of-replace bug", softAssert);
        }

        Logger.logReportMessage(tabLabel + " " + keyColumnLabel + " duplicate check OK — before=" + countBefore
                + ", after1st=" + countAfterFirst + ", after2nd=" + countAfterSecond
                + " (count increase allowed for live Today updates)");
    }

    private void assertNoDuplicateRowKeys(SoftAssert softAssert, By rowKeyLocator, String keyColumnLabel,
            String tabLabel, String phase) {
        List<String> keys = collectVisibleRowKeys(rowKeyLocator, keyColumnLabel);
        Map<String, Integer> occurrences = countOccurrences(keys);
        List<String> duplicated = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
            if (entry.getValue() > 1) {
                duplicated.add(entry.getKey() + " x" + entry.getValue());
            }
        }
        Verify.softAssert1(duplicated.isEmpty(),
                tabLabel + " " + phase + ": no duplicate " + keyColumnLabel + " on visible page"
                        + " (sampled=" + keys.size() + ", duplicates=" + duplicated + ")", softAssert);
    }

    private void assertKeysFromBeforeAppearAtMostOnce(SoftAssert softAssert, Set<String> keysBeforeRefresh,
            List<String> keysAfterRefresh, String keyColumnLabel, String tabLabel) {
        Map<String, Integer> afterCounts = countOccurrences(keysAfterRefresh);
        List<String> duplicatedFromBefore = new ArrayList<>();
        for (String key : keysBeforeRefresh) {
            Integer count = afterCounts.get(key);
            if (count != null && count > 1) {
                duplicatedFromBefore.add(key + " x" + count);
            }
        }
        Verify.softAssert1(duplicatedFromBefore.isEmpty(),
                tabLabel + ": " + keyColumnLabel + " values present before refresh still appear at most once"
                        + " (duplicated=" + duplicatedFromBefore + ")", softAssert);
    }

    private Map<String, Integer> countOccurrences(List<String> keys) {
        Map<String, Integer> counts = new HashMap<>();
        for (String key : keys) {
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }

    /** Waits until total-count label reads the same value for several consecutive polls. */
    private void waitForGridCountStable() throws InterruptedException {
        int lastCount = leftFilterPanelUtil.getTableRecordCount();
        int stableReads = 0;
        for (int i = 0; i < GRID_STABLE_POLLS * 3 && stableReads < GRID_STABLE_POLLS; i++) {
            Thread.sleep(GRID_STABLE_POLL_MS);
            int current = leftFilterPanelUtil.getTableRecordCount();
            if (current == lastCount) {
                stableReads++;
            } else {
                stableReads = 0;
                lastCount = current;
            }
        }
        Logger.logMessage("Grid count stabilized at " + lastCount);
    }

    public void clickRefreshButton(SoftAssert softAssert) {
        Verify.softAssert1(WaitUtil.isDisplay(headerPage.refreshButton(), DEFAULT_WAIT_SECONDS),
                "Refresh table button visible before click", softAssert);
        DriverUtil.clickOnElement(headerPage.refreshButton(), DEFAULT_WAIT_SECONDS);
        Logger.logReportMessage("Clicked Refresh table button");
    }

    private List<String> collectVisibleRowKeys(By locator, String keyColumnLabel) {
        List<String> keys = new ArrayList<>();
        try {
            List<DesktopBrowserElement> cells = driver.get().finder().findElements(locator);
            int limit = Math.min(cells.size(), VISIBLE_KEY_SAMPLE);
            for (int i = 0; i < limit; i++) {
                String normalized = normalizeBusinessKey(cells.get(i).getText(), keyColumnLabel);
                if (!normalized.isEmpty()) {
                    keys.add(normalized);
                }
            }
        } catch (Exception e) {
            Logger.logMessage("Could not collect visible " + keyColumnLabel + " keys: " + e.getMessage());
        }
        return keys;
    }

    /** Extracts numeric Order ID / LineItem ID from cell text (ignores copy-icon noise). */
    private static String normalizeBusinessKey(String rawText, String keyColumnLabel) {
        if (rawText == null) {
            return "";
        }
        String trimmed = rawText.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d{5,12}").matcher(trimmed);
        if (matcher.find()) {
            return matcher.group();
        }
        Logger.logMessage("No numeric " + keyColumnLabel + " parsed from cell text: " + trimmed);
        return trimmed;
    }
}
