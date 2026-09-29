package com.paramount.test.ff.uitests.base;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SynergyRetryUtil;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.GridColumnSearch_util;
import com.paramount.test.ff.uitests.helpers.GridSort_util;
import com.paramount.test.ff.uitests.helpers.TableViewGraphqlClient;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Base for Orders grid per-column search / filter tests.
 */
public abstract class ColumnSearchBaseTest extends ManageColumnsBaseTest {

    private static boolean searchSuitePrepared;
    private static long searchSessionStartMs;
    private static String lastPreparedSearchColumnKey;
    private static String activeColumnSearchViewName;
    private static final long SEARCH_SESSION_RENEW_MS = 48L * 60L * 1000L;
    private static final long SEARCH_COLUMN_PREP_DEADLINE_MS = 12L * 60L * 1000L;
    private long searchColumnPrepDeadlineMs;

    protected GridColumnSearch_util gridSearchUtil;
    protected GridSort_util gridSortUtil;
    protected FilterPanel_Util filterPanelUtil;

    @Override
    protected void ensureHelpersInitialized() {
        super.ensureHelpersInitialized();
        if (gridSearchUtil == null) {
            gridSearchUtil = new GridColumnSearch_util();
        }
        if (gridSortUtil == null) {
            gridSortUtil = new GridSort_util();
        }
        if (filterPanelUtil == null) {
            filterPanelUtil = new FilterPanel_Util();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void ensureSearchSessionHealthy() {
        FulfillmentJsUtil.useFastElementTimeout();
        try {
            BaseTest.requireDriver().getSessionID();
        } catch (Exception e) {
            Logger.logReportMessage("Column search suite: Synergy session unhealthy — recovering before test");
            SynergyRetryUtil.recoverSessionIfNeeded();
            searchSuitePrepared = false;
        }
        ensureHelpersInitialized();
    }

    @AfterMethod(alwaysRun = true)
    public void renewSearchSessionAfterTestIfNeeded(ITestResult result) throws InterruptedException {
        if (result == null || result.getMethod() == null) {
            return;
        }
        String methodName = result.getMethod().getMethodName();
        if (!"validateColumnSearchFunctionality".equals(methodName)
                && !methodName.startsWith("validateColumnSearch")) {
            return;
        }
        boolean sessionStale = searchSessionStartMs > 0
                && System.currentTimeMillis() - searchSessionStartMs >= SEARCH_SESSION_RENEW_MS;
        boolean testFailed = !result.isSuccess();
        boolean sessionDead = !isSearchSessionAlive();
        if (sessionStale || testFailed || sessionDead) {
            Logger.logReportMessage("Column search: renewing Synergy session between tests (stale="
                    + sessionStale + ", failed=" + testFailed + ", dead=" + sessionDead + ")");
            forceRenewSearchSession();
        }
    }

    private boolean isSearchSessionAlive() {
        try {
            BaseTest.requireDriver().getSessionID();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void markSearchSuitePrepared() {
        searchSuitePrepared = true;
    }

    protected void prepareSearchSuiteOnce() throws InterruptedException {
        ensureHelpersInitialized();
        ensureSearchSuiteSessionHealthy();
        renewSearchSessionIfNearLimit();
        if (searchSuitePrepared) {
            filterPanelUtil.ensureOrdersGridHome();
            tableViewUtill.waitForOrdersGridReadyFast(3);
            return;
        }
        Logger.logReportMessage("Column search suite: login, Yesterday filter, orders grid ready");
        launchSearchSuiteWithRecovery();
        prepareSearchGridForColumn();
        searchSuitePrepared = true;
        searchSessionStartMs = System.currentTimeMillis();
    }

    private void ensureSearchSuiteSessionHealthy() {
        try {
            BaseTest.requireDriver().getSessionID();
        } catch (Exception e) {
            Logger.logReportMessage("Column search suite: recovering dead WebDriver session");
            SynergyRetryUtil.recoverSessionIfNeeded();
            searchSuitePrepared = false;
        }
    }

    private void renewSearchSessionIfNearLimit() throws InterruptedException {
        if (searchSessionStartMs <= 0
                || System.currentTimeMillis() - searchSessionStartMs < SEARCH_SESSION_RENEW_MS) {
            return;
        }
        Logger.logReportMessage("Column search suite: renewing Synergy session before max test time");
        forceRenewSearchSession();
    }

    private void forceRenewSearchSession() throws InterruptedException {
        try {
            BaseTest.requireDriver().stop();
        } catch (Exception ignored) {
        }
        BaseTest.driver.remove();
        SynergyRetryUtil.recoverSessionIfNeeded();
        resetSearchSuiteSessionOnly();
        launchSearchSuiteWithRecovery();
        prepareSearchGridForColumn();
        searchSuitePrepared = true;
        searchSessionStartMs = System.currentTimeMillis();
    }

    private static void resetSearchSuiteSessionOnly() {
        searchSuitePrepared = false;
        searchSessionStartMs = 0L;
        lastPreparedSearchColumnKey = null;
        activeColumnSearchViewName = null;
        FilterPanel_Util.resetYesterdayDateFilterState();
        ManageColumnsBaseTest.resetFulfillmentSession();
    }

    private void launchSearchSuiteWithRecovery() throws InterruptedException {
        if (searchSessionStartMs <= 0) {
            searchSessionStartMs = System.currentTimeMillis();
        }
        try {
            launchFulfillmentConsole();
            TableViewGraphqlClient.warmUpGraphqlAuth();
            ensureSearchSuiteConsoleReady();
        } catch (Exception e) {
            if (!SynergyRetryUtil.isConnectionError(e)) {
                throw e;
            }
            Logger.logReportMessage("Column search suite: launch failed — recovering WebDriver and retrying");
            SynergyRetryUtil.recoverSessionIfNeeded();
            launchFulfillmentConsole();
            TableViewGraphqlClient.warmUpGraphqlAuth();
            ensureSearchSuiteConsoleReady();
        }
    }

    private void ensureSearchSuiteConsoleReady() throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        if (!FulfillmentJsUtil.waitForFulfillmentConsoleReady(90)) {
            Logger.logReportMessage("Column search suite: Fulfillment Console slow — soft reload (keep session)");
            FulfillmentJsUtil.reloadFulfillmentPage();
            FulfillmentJsUtil.dismissBlockingOverlays();
            FulfillmentJsUtil.waitForFulfillmentConsoleReady(90);
        }
        if (!FulfillmentJsUtil.waitForOrdersGridHeadersReady(45)) {
            Logger.logReportMessage("Column search suite: grid headers slow — refresh orders table");
            FulfillmentJsUtil.clickRefreshOrdersTable();
            FulfillmentJsUtil.waitForOrdersGridHeadersReady(30);
        }
        filterPanelUtil.ensureOrdersGridHome();
        if (!FilterPanel_Util.isYesterdayDateFilterCached()) {
            filterPanelUtil.applyYesterdayDateFilter();
        }
        tableViewUtill.waitForOrdersGridReadyFast(15);
        if (!FulfillmentJsUtil.isOrdersGridHeadersReady()) {
            FulfillmentJsUtil.logOrdersGridHeaderLabels();
        }
    }

    private boolean isSearchPrepTimedOut() {
        return searchColumnPrepDeadlineMs > 0 && System.currentTimeMillis() > searchColumnPrepDeadlineMs;
    }

    private void startSearchColumnPrepDeadline() {
        searchColumnPrepDeadlineMs = System.currentTimeMillis() + SEARCH_COLUMN_PREP_DEADLINE_MS;
    }

    private String searchColumnKey(String columnName, String section) {
        return section + "|" + columnName;
    }

    private boolean prepareSearchColumnIfNeeded(String columnName, String section, boolean searchable)
            throws InterruptedException {
        String columnKey = searchColumnKey(columnName, section);
        if (columnKey.equals(lastPreparedSearchColumnKey)) {
            filterPanelUtil.ensureOrdersGridHome();
            tableViewUtill.waitForOrdersGridReadyFast(2);
            return true;
        }
        renewSearchSessionIfNearLimit();
        if (!isSearchSessionAlive()) {
            Logger.logReportMessage("Column search prep: session dead before prep — renewing");
            forceRenewSearchSession();
        }
        if (!columnKey.equals(lastPreparedSearchColumnKey)) {
            activeColumnSearchViewName = null;
        }
        startSearchColumnPrepDeadline();
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                if (!isSearchSessionAlive()) {
                    Logger.logReportMessage("Column search prep aborted — Synergy session not active");
                    return false;
                }
                if (ensureColumnVisibleForSearch(columnName, section, searchable)) {
                    lastPreparedSearchColumnKey = columnKey;
                    return true;
                }
                if (isSearchPrepTimedOut()) {
                    Logger.logReportMessage("Column search prep deadline exceeded for " + columnName);
                    return false;
                }
                return false;
            } catch (Exception e) {
                if (!SynergyRetryUtil.isConnectionError(e) || attempt >= 2) {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                }
                Logger.logReportMessage("Column search prep: Synergy error — renewing session and retrying");
                forceRenewSearchSession();
            }
        }
        return false;
    }

    private void ensureBsd30034DeliveryDateColumnsReady() throws InterruptedException {
        if (isBsd30034DateColumnsEnabled()) {
            return;
        }
        prepareBsd30034SuiteOnce("Delivery date", "Offset Delivery date", "order");
    }

    private boolean isBsd30034DeliveryDateColumn(String columnName) {
        return "Delivery date".equalsIgnoreCase(columnName)
                || "Offset Delivery date".equalsIgnoreCase(columnName);
    }

    protected boolean ensureColumnVisibleForSearch(String columnName, String section) throws InterruptedException {
        return ensureColumnVisibleForSearch(columnName, section, true);
    }

    protected boolean ensureColumnVisibleForSearch(String columnName, String section, boolean searchable)
            throws InterruptedException {
        if (!isSearchSessionAlive()) {
            Logger.log("Column search prep aborted — Synergy session not active for " + columnName);
            return false;
        }
        if (isSearchPrepTimedOut()) {
            Logger.log("Column search prep deadline hit — skip prep for " + columnName);
            return false;
        }
        if (isBsd30034DeliveryDateColumn(columnName)) {
            ensureBsd30034DeliveryDateColumnsReady();
            if (isBsd30034DateColumnsEnabled()) {
                filterPanelUtil.ensureOrdersGridHome();
                String columnId = gridSearchUtil.resolveColumnId(columnName, section);
                FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
                if (FulfillmentJsUtil.isColumnVisibleInGrid(columnName, columnId)) {
                    return true;
                }
            }
        }
        filterPanelUtil.ensureOrdersGridHome();
        String columnId = gridSearchUtil.resolveColumnId(columnName, section);
        if ("Title, Season, Episode".equalsIgnoreCase(columnName) && "order".equalsIgnoreCase(section)) {
            if (tryPrepareTitleSeasonEpisodeForSearch(columnName, section, searchable)) {
                return true;
            }
        }
        if (tableViewUtill.isExactOrderGridHeaderVisible(columnName)
                && (!searchable || hasSearchControlReady(columnName, section))) {
            Logger.log("Column header + search control ready — skip prep: " + columnName);
            return true;
        }
        if (!searchable && tableViewUtill.isExactOrderGridHeaderVisible(columnName)) {
            Logger.log("Column header visible (non-searchable) — skip prep: " + columnName);
            return true;
        }
        if ("lineitem".equalsIgnoreCase(section)) {
            return ensureLineItemColumnVisibleForSearch(columnName);
        }
        if ("Partner".equalsIgnoreCase(columnName)) {
            if (activeColumnSearchViewName != null
                    && tableViewUtill.isGridHeaderVisibleForPrep(columnName, section)
                    && hasSearchControlReady(columnName, section)) {
                Logger.log("Partner visible on saved column search view — skip prep: "
                        + activeColumnSearchViewName);
                return true;
            }
            Logger.log("Column search prep: Standard-view enable for Partner");
            if (tryEnableColumnOnStandardView(columnName, section)) {
                activeColumnSearchViewName = null;
                return true;
            }
            Logger.log("Column search prep: TC_006 custom-view path for Partner");
            return tryPartnerCustomViewForColumnSearch(columnName, section);
        }
        if (searchable && tableViewUtill.isExactOrderGridHeaderVisible(columnName)) {
            String prepColumnId = gridSearchUtil.resolveColumnId(columnName, section);
            if (gridSearchUtil.hasColumnSearchControl(prepColumnId, columnName)) {
                Logger.log("Search control ready on visible header — skip extended prep: " + columnName);
                return true;
            }
            Logger.log("Exact header visible but search control missing — refresh for " + columnName);
            FulfillmentJsUtil.clickRefreshOrdersTable();
            tableViewUtill.waitForOrdersGridReadyFast(8);
            if (hasSearchControlReady(columnName, section)) {
                return true;
            }
            if (tryEnableColumnViaManageColumns(columnName, section)
                    && hasSearchControlReady(columnName, section)) {
                return true;
            }
        }
        if ("Order ID".equalsIgnoreCase(columnName) && "order".equalsIgnoreCase(section) && searchable) {
            Logger.log("Column search prep: GraphQL-first for Order ID");
            if (tryGraphqlColumnSearchView(columnName, section, columnId)) {
                return true;
            }
            Logger.log("GraphQL-first failed for Order ID — Standard-view MC fallback");
        }
        if (searchable && "order".equalsIgnoreCase(section)
                && !"Partner".equalsIgnoreCase(columnName)
                && !isSearchPrepTimedOut()) {
            Logger.log("Column search prep: GraphQL-first for " + columnName);
            if (tryGraphqlColumnSearchView(columnName, section, columnId)) {
                return true;
            }
        }
        Logger.log("Column search prep: Standard-view enable (sort-style) for " + columnName);
        if (tryEnableColumnOnStandardView(columnName, section)) {
            return true;
        }
        if (isColumnHeaderReadyAfterPrep(columnName, section, columnId)
                && (!searchable || hasSearchControlReady(columnName, section))) {
            Logger.log("Skip custom-view fallback — column ready on grid: " + columnName);
            return true;
        }
        String gqlViewName = buildColumnSearchViewName(columnName);
        if (TableViewGraphqlClient.hasCachedViewId(gqlViewName) && !isSearchPrepTimedOut()) {
            Logger.log("Cached GraphQL view — re-apply before custom-view fallback: " + gqlViewName);
            if (applyGraphqlColumnSearchView(gqlViewName, columnName, section, columnId)) {
                return true;
            }
            if (tableViewUtill.isExactOrderGridHeaderVisible(columnName)
                    && hasSearchControlReady(columnName, section)) {
                Logger.log("Header + search control ready after cached GraphQL re-apply: " + columnName);
                activeColumnSearchViewName = gqlViewName;
                return true;
            }
        }
        Logger.log("Column search prep: custom table view fallback for " + columnName);
        return tryCustomTableViewForColumnSearch(columnName, section);
    }

    private boolean isColumnHeaderReadyAfterPrep(String columnName, String section, String columnId)
            throws InterruptedException {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        for (int attempt = 0; attempt < 8; attempt++) {
            if (isSearchPrepTimedOut()) {
                return false;
            }
            if (isSearchTargetColumnVisible(columnName, section, columnId)) {
                return true;
            }
            Thread.sleep(250);
        }
        return isSearchTargetColumnVisible(columnName, section, columnId);
    }

    private boolean isSearchTargetColumnVisible(String columnName, String section, String columnId)
            throws InterruptedException {
        if ("Title, Season, Episode".equalsIgnoreCase(columnName)) {
            if (tableViewUtill.isTitleSeasonEpisodeGridVisible()) {
                return true;
            }
            return FulfillmentJsUtil.isColumnVisibleInGrid("Title", "title")
                    || FulfillmentJsUtil.isColumnVisibleInGrid(columnName, columnId);
        }
        if (tableViewUtill.isExactOrderGridHeaderVisible(columnName)) {
            return true;
        }
        return FulfillmentJsUtil.isColumnVisibleInGrid(columnName, columnId)
                || tableViewUtill.isGridColumnHeaderVisibleForSearch(columnName, section);
    }

    private boolean mcEnableSucceededForSearch(String columnName, String section, String columnId)
            throws InterruptedException {
        if (!isColumnHeaderReadyAfterPrep(columnName, section, columnId)) {
            return false;
        }
        return waitForSearchControlReady(columnName, section, 8);
    }

    private void prepareSearchGridForColumn() throws InterruptedException {
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.activateStandardViewOnOrdersGrid();
        activeColumnSearchViewName = null;
        tableViewUtill.waitForOrdersGridReadyFast(8);
    }

    /** Enable column via Manage Columns without resetting the active Orders grid view. */
    private boolean tryEnableColumnViaManageColumns(String columnName, String section) throws InterruptedException {
        String columnId = gridSearchUtil.resolveColumnId(columnName, section);
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        Thread.sleep(300);
        FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section);
        if (!waitForColumnListedForSearch(columnName, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        if (!tableViewUtill.ensureColumnCheckboxEnabled(columnName, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        tableViewUtill.closeManageColumns();
        tableViewUtill.waitForOrdersGridReadyFast(8);
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        FulfillmentJsUtil.clickRefreshOrdersTable();
        return mcEnableSucceededForSearch(columnName, section, columnId);
    }

    /** Sort-style prep: Manage Columns enable on current grid view (do not reset Orders grid first). */
    private boolean tryEnableColumnOnStandardView(String columnName, String section) throws InterruptedException {
        filterPanelUtil.ensureOrdersGridHome();
        tableViewUtill.closeManageColumnsIfOpen();
        String columnId = gridSearchUtil.resolveColumnId(columnName, section);
        tableViewUtill.ensureManageColumnsStandardViewReady();
        FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section);
        if (!waitForColumnListedForSearch(columnName, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            Logger.log("Manage Columns UI miss for [" + section + "] " + columnName);
            return false;
        }
        if (!tableViewUtill.ensureColumnCheckboxEnabled(columnName, section)) {
            tableViewUtill.closeManageColumns();
            Logger.log("Target column checkbox not selected: " + columnName);
            return false;
        }
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        tableViewUtill.closeManageColumns();
        tableViewUtill.waitForOrdersGridReadyFast(8);
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        FulfillmentJsUtil.clickRefreshOrdersTable();
        if (mcEnableSucceededForSearch(columnName, section, columnId)) {
            return true;
        }
        Logger.log("Standard-view MC pass 1 missed header — toggle refresh for " + columnName);
        tableViewUtill.ensureManageColumnsStandardViewReady();
        FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section);
        if (!tableViewUtill.ensureColumnCheckboxEnabledWithRefresh(columnName, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        tableViewUtill.closeManageColumns();
        tableViewUtill.waitForOrdersGridReadyFast(8);
        FulfillmentJsUtil.clickRefreshOrdersTable();
        return mcEnableSucceededForSearch(columnName, section, columnId);
    }

    private boolean tryGraphqlColumnSearchView(String columnName, String section, String columnId)
            throws InterruptedException {
        String viewName = buildColumnSearchViewName(columnName);
        Logger.log("Column search prep: GraphQL table view for " + columnName + " as " + viewName);
        FulfillmentJsUtil.installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        TableViewGraphqlClient.warmUpGraphqlAuth();
        TableViewGraphqlClient.cacheViewFromGraphql(viewName);
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        if (TableViewGraphqlClient.hasCachedViewId(viewName)
                && applyGraphqlColumnSearchView(viewName, columnName, section, columnId)) {
            activeColumnSearchViewName = viewName;
            return true;
        }
        if (!tableViewUtill.saveTableViewForColumnSearch(
                viewName, "Title, Season, Episode", columnName, "Order ID")) {
            Logger.log("Column search prep: GraphQL view save failed for " + columnName);
            return false;
        }
        activeColumnSearchViewName = viewName;
        return applyGraphqlColumnSearchView(viewName, columnName, section, columnId);
    }

    private boolean applyGraphqlColumnSearchView(String viewName, String columnName, String section, String columnId)
            throws InterruptedException {
        filterPanelUtil.ensureOrdersGridHome();
        FulfillmentJsUtil.refreshTableViewsUiState();
        if (tableViewUtill.applySavedTableViewForColumnSearch(viewName, columnName, section)
                && waitForSearchControlReady(columnName, section, 20)) {
            return true;
        }
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if (tableViewUtill.waitForGridColumnVisibleForSearch(columnName, section, 15)) {
            return waitForSearchControlReady(columnName, section, 20);
        }
        return waitForSearchControlReady(columnName, section, 10);
    }

    /** TC_006 parity for Partner: Title + Partner + Order ID saved view without resetting via Standard view. */
    private boolean tryPartnerCustomViewForColumnSearch(String columnName, String section)
            throws InterruptedException {
        prepareSearchGridForColumn();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        Thread.sleep(300);
        FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section);
        if (!waitForColumnListedForSearch(columnName, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            Logger.log("Partner not in Manage Columns UI — GraphQL fallback");
            return tryGraphqlColumnSearchView(columnName, section, gridSearchUtil.resolveColumnId(columnName, section));
        }
        enableTc006StyleColumnsForSearch(columnName);
        if (!tableViewUtill.ensureColumnCheckboxEnabled(columnName, section)) {
            Logger.log("Partner checkbox not selected before save");
            tableViewUtill.closeManageColumns();
            return false;
        }
        tableViewUtill.applyColumnChanges();
        String viewName = buildColumnSearchViewName(columnName);
        Logger.log("Saving Partner table view (TC_006 UI save): " + viewName);
        tableViewUtill.saveTableView(viewName);
        activeColumnSearchViewName = viewName;
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.waitForOrdersGridReadyFast(8);
        Thread.sleep(300);
        if (tableViewUtill.waitForGridColumnVisibleForSearch(columnName, section, 25)) {
            Logger.log("Partner visible after TC_006 UI save: " + columnName);
            return true;
        }
        Logger.log("TC_006 UI save did not surface Partner — GraphQL fallback");
        tableViewUtill.openManageColumns();
        enableTc006StyleColumnsForSearch(columnName);
        tableViewUtill.applyColumnChanges();
        if (!tableViewUtill.saveTableViewForColumnSearch(viewName, "Title, Season, Episode", columnName, "Order ID")) {
            Logger.log("Partner column search view could not be saved: " + viewName);
            FulfillmentJsUtil.logOrdersGridHeaderLabels();
            return false;
        }
        if (tableViewUtill.waitForGridColumnVisibleForSearch(columnName, section, 25)) {
            return true;
        }
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, gridSearchUtil.resolveColumnId(columnName, section));
        return tableViewUtill.applySavedTableViewForColumnSearch(viewName, columnName, section);
    }

    /** TC_006-style custom view save when Standard-view toggle does not surface the column on PROD. */
    private boolean tryCustomTableViewForColumnSearch(String columnName, String section) throws InterruptedException {
        prepareSearchGridForColumn();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        Thread.sleep(300);
        FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section);
        if (!waitForColumnListedForSearch(columnName, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            Logger.log("Column not in Manage Columns UI — GraphQL path for custom view: " + columnName);
            return tryGraphqlColumnSearchView(columnName, section, gridSearchUtil.resolveColumnId(columnName, section));
        }
        enableTc006StyleColumnsForSearch(columnName);
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.waitForOrdersGridReadyFast(8);
        String viewName = buildColumnSearchViewName(columnName);
        Logger.log("Saving custom table view (TC_006 UI save) for column search: " + viewName);
        tableViewUtill.saveTableView(viewName);
        tableViewUtill.closeManageColumnsIfOpen();
        if (waitForSearchControlReady(columnName, section, 20)) {
            activeColumnSearchViewName = viewName;
            Logger.log("Column visible with search control after TC_006 UI save: " + columnName);
            return true;
        }
        Logger.log("TC_006 UI save did not surface column — GraphQL fallback for " + columnName);
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        enableTc006StyleColumnsForSearch(columnName);
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.waitForOrdersGridReadyFast(8);
        String gqlViewName = buildColumnSearchViewName(columnName);
        if (!tableViewUtill.saveTableViewForColumnSearch(gqlViewName, "Title, Season, Episode", columnName, "Order ID")) {
            Logger.log("Column search view could not be saved: " + gqlViewName);
            tableViewUtill.closeManageColumnsIfOpen();
            FulfillmentJsUtil.logOrdersGridHeaderLabels();
            return false;
        }
        activeColumnSearchViewName = gqlViewName;
        if (tableViewUtill.isGridHeaderVisibleForPrep(columnName, section)
                && hasSearchControlReady(columnName, section)) {
            return true;
        }
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, gridSearchUtil.resolveColumnId(columnName, section));
        if (tableViewUtill.waitForGridColumnVisibleForSearch(columnName, section, 20)) {
            return true;
        }
        FulfillmentJsUtil.logOrdersGridHeaderLabels();
        return tableViewUtill.applySavedTableViewForColumnSearch(gqlViewName, columnName, section);
    }

    /** TC_006-style column set: Title + target + Order ID (Partner uses the same three columns as TC_006). */
    private void enableTc006StyleColumnsForSearch(String targetColumn) throws InterruptedException {
        String[] columns = {"Title, Season, Episode", targetColumn, "Order ID"};
        java.util.LinkedHashSet<String> unique = new java.util.LinkedHashSet<>();
        for (String col : columns) {
            if (col != null && !col.trim().isEmpty()) {
                unique.add(col);
            }
        }
        for (String col : unique) {
            if (tableViewUtill.isColumnListed(col, "order")) {
                tableViewUtill.ensureColumnCheckboxEnabled(col, "order");
            }
        }
    }

    private String buildColumnSearchViewName(String columnName) {
        String safe = columnName.replaceAll("[^A-Za-z0-9]+", "-");
        if (safe.isEmpty()) {
            safe = "Col";
        }
        return "FF-SEARCH-" + safe;
    }

    private void reactivateColumnSearchViewIfNeeded(String columnName, String section) throws InterruptedException {
        if (activeColumnSearchViewName == null) {
            return;
        }
        if (isTargetColumnHeaderVisible(columnName, section)) {
            return;
        }
        Logger.log("Re-activating column search table view: " + activeColumnSearchViewName);
        tableViewUtill.applySavedTableViewForColumnSearch(activeColumnSearchViewName, columnName, section);
        Thread.sleep(1000);
        if (!isTargetColumnHeaderVisible(columnName, section) && "Partner".equalsIgnoreCase(columnName)) {
            Logger.log("Partner still missing — retry TC_006 view activation");
            FulfillmentJsUtil.selectSavedViewViaScript(activeColumnSearchViewName);
            tableViewUtill.closeManageColumnsIfOpen();
            FulfillmentJsUtil.clickRefreshOrdersTable();
            tableViewUtill.waitForOrdersGridReadyFast(8);
            Thread.sleep(300);
        }
    }

    /** PROD renders Title / Season / Episode as separate grid headers; enable via Standard view MC. */
    private boolean tryPrepareTitleSeasonEpisodeForSearch(String columnName, String section, boolean searchable)
            throws InterruptedException {
        filterPanelUtil.ensureOrdersGridHome();
        tableViewUtill.activateStandardViewOnOrdersGrid();
        tableViewUtill.waitForOrdersGridReadyFast(10);
        if (tableViewUtill.isTitleSeasonEpisodeGridVisible() || isTitleSeasonEpisodeJsVisible()) {
            if (!searchable || hasSearchControlReady(columnName, section)) {
                Logger.log("Title/Season/Episode visible on Standard view — skip extended prep");
                return true;
            }
            FulfillmentJsUtil.clickRefreshOrdersTable();
            tableViewUtill.waitForOrdersGridReadyFast(8);
            if (hasSearchControlReady(columnName, section)) {
                return true;
            }
        }
        Logger.log("Column search prep: Standard-view enable for Title/Season/Episode group");
        tableViewUtill.ensureManageColumnsStandardViewReady();
        String[] labels = {"Title", "Season", "Episode"};
        boolean anyListed = false;
        for (String label : labels) {
            FulfillmentJsUtil.scrollManagePanelToColumn(label, section);
            if (waitForColumnListedForSearch(label, section)) {
                anyListed = true;
                tableViewUtill.ensureColumnCheckboxEnabled(label, section);
            }
        }
        if (!anyListed) {
            tableViewUtill.closeManageColumnsIfOpen();
            Logger.log("Title/Season/Episode group not listed in Manage Columns UI");
            return false;
        }
        FulfillmentJsUtil.clickManageColumnsApplyIfPresent();
        tableViewUtill.closeManageColumns();
        tableViewUtill.waitForOrdersGridReadyFast(10);
        FulfillmentJsUtil.clickRefreshOrdersTable();
        if (!tableViewUtill.isTitleSeasonEpisodeGridVisible() && !isTitleSeasonEpisodeJsVisible()) {
            FulfillmentJsUtil.logOrdersGridHeaderLabels();
            return false;
        }
        return !searchable || waitForSearchControlReady(columnName, section, 15);
    }

    private boolean isTitleSeasonEpisodeJsVisible() {
        return FulfillmentJsUtil.isColumnVisibleInGrid("Title", "title")
                && FulfillmentJsUtil.isColumnVisibleInGrid("Season", "season")
                && FulfillmentJsUtil.isColumnVisibleInGrid("Episode", "episode");
    }

    private boolean hasSearchControlReady(String columnName, String section) {
        if ("Title, Season, Episode".equalsIgnoreCase(columnName)) {
            if (FulfillmentJsUtil.hasGridColumnSearchControl("Title", "title")
                    || FulfillmentJsUtil.hasGridColumnSearchControl(columnName, "titleSeasonEpisode")) {
                return true;
            }
            FulfillmentJsUtil.scrollColumnHeaderIntoView("Title", "titleSeasonEpisode");
            if (gridSearchUtil.hasColumnSearchControl("titleSeasonEpisode", columnName)
                    || gridSearchUtil.hasColumnSearchControl("titleSeasonEpisode", "Title")
                    || gridSearchUtil.hasColumnSearchControl("title", "Title")) {
                return true;
            }
        }
        String columnId = gridSearchUtil.resolveColumnId(columnName, section);
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        return gridSearchUtil.hasColumnSearchControl(columnId, columnName);
    }

    private boolean waitForSearchControlReady(String columnName, String section, int timeoutSec)
            throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (!isSearchSessionAlive() || isSearchPrepTimedOut()) {
                return false;
            }
            if (hasSearchControlReady(columnName, section)) {
                return true;
            }
            Thread.sleep(300);
        }
        return isSearchSessionAlive() && hasSearchControlReady(columnName, section);
    }

    private boolean waitForColumnListedForSearch(String columnName, String section) throws InterruptedException {
        for (int attempt = 0; attempt < 4; attempt++) {
            if (isSearchPrepTimedOut()) {
                return false;
            }
            if (tableViewUtill.isColumnListed(columnName, section)
                    || FulfillmentJsUtil.isExactColumnLabelInManagePanel(columnName, section)) {
                return true;
            }
            FulfillmentJsUtil.scrollManagePanelToColumn(columnName, section);
            Thread.sleep(300);
        }
        tableViewUtill.scrollToColumn(columnName, section);
        return tableViewUtill.isColumnListed(columnName, section)
                || FulfillmentJsUtil.isExactColumnLabelInManagePanel(columnName, section);
    }

    private boolean isTargetColumnHeaderVisible(String columnName, String section) throws InterruptedException {
        if ("Partner".equalsIgnoreCase(columnName)) {
            String columnId = gridSearchUtil.resolveColumnId(columnName, section);
            FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            return tableViewUtill.isExactOrderGridHeaderVisible(columnName)
                    || tableViewUtill.isGridColumnHeaderVisibleForSearch(columnName, section);
        }
        return tableViewUtill.isGridHeaderVisibleForPrep(columnName, section);
    }

    private boolean ensureLineItemColumnVisibleForSearch(String columnName) throws InterruptedException {
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        if (!tableViewUtill.isColumnListed(columnName, "lineitem")) {
            tableViewUtill.closeManageColumns();
            Logger.log("Skip — column not in Manage Columns UI: [lineitem] " + columnName);
            return false;
        }
        if (tableViewUtill.isColumnCheckboxSelected(columnName, "lineitem")) {
            Logger.log("Lineitem column already selected in Standard view — proceed to search test: " + columnName);
        } else {
            Logger.log("Lineitem column not selected in Standard view — enabling: " + columnName);
            tableViewUtill.enableColumn(columnName, "lineitem");
        }
        tableViewUtill.closeManageColumns();
        Thread.sleep(300);
        tableViewUtill.waitForOrdersGridReadyFast(8);
        return tableViewUtill.isNestedColumnVisible(columnName, "lineitem");
    }

    protected void runColumnSearchTest(String columnName, String section, boolean searchable, String searchType)
            throws InterruptedException {
        prepareSearchSuiteOnce();
        if (!prepareSearchColumnIfNeeded(columnName, section, searchable)) {
            Verify.softAssert(false, columnName + " could not be prepared for column search test");
            return;
        }
        if (!isBsd30034DateColumnsEnabled()) {
            reactivateColumnSearchViewIfNeeded(columnName, section);
        }
        tableViewUtill.waitForOrdersGridReadyFast(3);
        filterPanelUtil.ensureOrdersGridHome();
        String columnId = gridSearchUtil.resolveColumnId(columnName, section);
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if ("lineitem".equalsIgnoreCase(section)) {
            tableViewUtill.prepareNestedGridForColumnCheck("lineitem");
            Verify.softAssert(tableViewUtill.isNestedColumnVisible(columnName, section),
                    columnName + " is visible on lineitem grid");
            if (searchable) {
                if ("DATE".equalsIgnoreCase(searchType)) {
                    gridSearchUtil.verifySearchableDateColumn(columnName, columnId);
                } else {
                    gridSearchUtil.verifySearchableTextColumn(columnName, columnId);
                }
            } else {
                gridSearchUtil.verifyNonSearchableLineItemColumn(columnName, columnId);
            }
            return;
        }
        if (searchable) {
            if ("DATE".equalsIgnoreCase(searchType)) {
                gridSearchUtil.verifySearchableDateColumn(columnName, columnId);
            } else {
                gridSearchUtil.verifySearchableTextColumn(columnName, columnId);
            }
        } else {
            gridSearchUtil.verifyNonSearchableColumn(columnName, columnId);
        }
    }

    protected void runColumnSearchNegativeTest(String columnName, String section, String searchType)
            throws InterruptedException {
        prepareSearchSuiteOnce();
        if (!prepareSearchColumnIfNeeded(columnName, section, true)) {
            Verify.softAssert(false, columnName + " could not be prepared for column search test");
            return;
        }
        String columnId = gridSearchUtil.resolveColumnId(columnName, section);
        if ("DATE".equalsIgnoreCase(searchType)) {
            gridSearchUtil.verifyNegativeDateColumnSearch(columnName, columnId);
        } else {
            gridSearchUtil.verifyNegativeTextColumnSearch(columnName, columnId);
        }
    }

    public static void resetSearchSuiteState() {
        searchSuitePrepared = false;
        searchSessionStartMs = 0L;
        lastPreparedSearchColumnKey = null;
        activeColumnSearchViewName = null;
        FilterPanel_Util.resetYesterdayDateFilterState();
        ManageColumnsBaseTest.resetFulfillmentSession();
    }
}
