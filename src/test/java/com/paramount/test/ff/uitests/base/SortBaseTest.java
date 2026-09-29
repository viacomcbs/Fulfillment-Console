package com.paramount.test.ff.uitests.base;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SynergyRetryUtil;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.GridSort_util;
import com.paramount.test.ff.uitests.helpers.TableViewGraphqlClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.ITestResult;

/**
 * Base for Orders grid column sort tests (ascending / descending).
 */
public abstract class SortBaseTest extends ManageColumnsBaseTest {

    private static boolean sortSuitePrepared;
    private static boolean sortViewsPrefetched;
    private static long sortSessionStartMs;
    private static String lastPreparedSortColumnKey;
    private static String lastSortTableViewName;
    private static final String SORT_VIEW_PREFIX = "FF-SORT-";
    private static final int SORT_GRID_WAIT_SEC = 2;
    /** Renew at 20 min — ~10 min buffer before Synergy 30 min limit (renewal takes ~3–5 min). */
    private static final long SORT_SESSION_RENEW_MS = 20L * 60L * 1000L;
    private static final long SORT_COLUMN_PREP_DEADLINE_MS = 10L * 60L * 1000L;
    private long sortColumnPrepDeadlineMs;

    protected GridSort_util gridSortUtil;
    protected FilterPanel_Util filterPanelUtil;

    @Override
    protected void ensureHelpersInitialized() {
        super.ensureHelpersInitialized();
        if (gridSortUtil == null) {
            gridSortUtil = new GridSort_util();
        }
        if (filterPanelUtil == null) {
            filterPanelUtil = new FilterPanel_Util();
        }
    }

    @Override
    @BeforeMethod(alwaysRun = true)
    public void ensureColumnSessionHealthy() {
        FulfillmentJsUtil.useFastElementTimeout();
        try {
            BaseTest.requireDriver().getSessionID();
            try {
                renewSortSessionIfNearLimit();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            Logger.logReportMessage("Sort suite: Synergy session unhealthy — recovering before test");
            SynergyRetryUtil.recoverSessionIfNeeded();
            sortSuitePrepared = false;
            sortSessionStartMs = 0L;
        }
        ensureHelpersInitialized();
    }

    @AfterMethod(alwaysRun = true)
    public void renewSortSessionAfterTestIfNeeded(ITestResult result) throws InterruptedException {
        if (result == null || result.getMethod() == null) {
            return;
        }
        String methodName = result.getMethod().getMethodName();
        if (!"validateSortAscending".equals(methodName) && !"validateSortDescending".equals(methodName)) {
            return;
        }
        boolean sessionStale = sortSessionStartMs > 0
                && System.currentTimeMillis() - sortSessionStartMs >= SORT_SESSION_RENEW_MS;
        boolean sessionDead = !isSortSessionAlive();
        if (sessionStale || sessionDead) {
            Logger.logReportMessage("Sort suite: renewing Synergy session between tests (stale="
                    + sessionStale + ", dead=" + sessionDead + ")");
            forceRenewSortSession();
        }
    }

    private boolean isSortSessionAlive() {
        try {
            BaseTest.requireDriver().getSessionID();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void markSortSuitePrepared() {
        sortSuitePrepared = true;
    }

    protected void prepareSortSuiteOnce() throws InterruptedException {
        ensureHelpersInitialized();
        ensureSortSuiteSessionHealthy();
        renewSortSessionIfNearLimit();
        if (sortSuitePrepared) {
            filterPanelUtil.ensureOrdersGridHome();
            tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
            return;
        }
        Logger.logReportMessage("Sort suite: login, Yesterday filter, orders grid ready");
        launchSortSuiteWithRecovery();
        prepareSortGridForColumn();
        sortSuitePrepared = true;
        sortSessionStartMs = System.currentTimeMillis();
        syncColumnSessionRenewed();
    }

    private void ensureSortSuiteSessionHealthy() {
        try {
            BaseTest.requireDriver().getSessionID();
        } catch (Exception e) {
            Logger.logReportMessage("Sort suite: recovering dead WebDriver session");
            SynergyRetryUtil.recoverSessionIfNeeded();
            sortSuitePrepared = false;
        }
    }

    private void renewSortSessionIfNearLimit() throws InterruptedException {
        if (sortSessionStartMs <= 0
                || System.currentTimeMillis() - sortSessionStartMs < SORT_SESSION_RENEW_MS) {
            return;
        }
        Logger.logReportMessage("Sort suite: renewing Synergy session before max test time");
        forceRenewSortSession();
    }

    private void forceRenewSortSession() throws InterruptedException {
        String preservedView = lastSortTableViewName;
        String preservedColumn = lastPreparedSortColumnKey;
        try {
            BaseTest.requireDriver().stop();
        } catch (Exception ignored) {
        }
        BaseTest.driver.remove();
        SynergyRetryUtil.recoverSessionIfNeeded();
        resetSortSuiteSessionOnly();
        lastSortTableViewName = preservedView;
        lastPreparedSortColumnKey = preservedColumn;
        launchSortSuiteWithRecovery();
        prepareSortGridForColumn();
        if (lastSortTableViewName != null && !lastSortTableViewName.isEmpty()) {
            reactivateSortTableView(lastSortTableViewName);
        }
        sortSuitePrepared = true;
        sortSessionStartMs = System.currentTimeMillis();
        syncColumnSessionRenewed();
    }

    private static void resetSortSuiteSessionOnly() {
        sortSuitePrepared = false;
        sortViewsPrefetched = false;
        sortSessionStartMs = 0L;
        FilterPanel_Util.resetYesterdayDateFilterState();
        ManageColumnsBaseTest.resetFulfillmentSession();
    }

    private void prefetchSortViewsOnce() {
        if (sortViewsPrefetched) {
            return;
        }
        TableViewGraphqlClient.warmUpGraphqlAuthIfNeeded();
        TableViewGraphqlClient.prefetchTableViewsByNamePrefix(SORT_VIEW_PREFIX);
        sortViewsPrefetched = true;
    }

    private void launchSortSuiteWithRecovery() throws InterruptedException {
        if (sortSessionStartMs <= 0) {
            sortSessionStartMs = System.currentTimeMillis();
        }
        try {
            launchFulfillmentConsole();
            TableViewGraphqlClient.warmUpGraphqlAuthIfNeeded();
            ensureSortSuiteConsoleReady();
        } catch (Exception e) {
            if (!SynergyRetryUtil.isConnectionError(e)) {
                throw e;
            }
            Logger.logReportMessage("Sort suite: launch failed — recovering WebDriver and retrying");
            SynergyRetryUtil.recoverSessionIfNeeded();
            launchFulfillmentConsole();
            TableViewGraphqlClient.warmUpGraphqlAuthIfNeeded();
            ensureSortSuiteConsoleReady();
        }
    }

    private void ensureSortSuiteConsoleReady() throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        if (!FulfillmentJsUtil.waitForFulfillmentConsoleReady(90)) {
            Logger.logReportMessage("Sort suite: Fulfillment Console not ready — reloading");
            ManageColumnsBaseTest.resetFulfillmentSession();
            launchFulfillmentConsole();
            FulfillmentJsUtil.waitForFulfillmentConsoleReady(90);
        }
        filterPanelUtil.ensureOrdersGridHome();
        tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
    }

    private void applyYesterdayFilterSafely() throws InterruptedException {
        if (FilterPanel_Util.isYesterdayDateFilterAttempted()) {
            return;
        }
        try {
            filterPanelUtil.applyYesterdayDateFilter();
        } catch (Exception e) {
            if (SynergyRetryUtil.isConnectionError(e)) {
                Logger.logReportMessage("Sort suite: Synergy error during Yesterday filter — recovering and retrying");
                SynergyRetryUtil.recoverSessionIfNeeded();
                sortSuitePrepared = false;
                launchSortSuiteWithRecovery();
                prepareSortGridForColumn();
                sortSuitePrepared = true;
                sortSessionStartMs = System.currentTimeMillis();
                return;
            }
            Logger.logConsoleMessage("Yesterday filter skipped: " + e.getMessage());
        }
    }

    private void prepareSortGridForColumn() throws InterruptedException {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                filterPanelUtil.ensureOrdersGridHome();
                tableViewUtill.activateStandardViewOnOrdersGrid();
                applyYesterdayFilterSafely();
                prefetchSortViewsOnce();
                tableViewUtill.waitForOrdersGridReadyFast(8);
                return;
            } catch (Exception e) {
                if (!SynergyRetryUtil.isConnectionError(e) || attempt >= 2) {
                    if (SynergyRetryUtil.isConnectionError(e)) {
                        Logger.logReportMessage("Sort suite: grid prep failed after Synergy recovery");
                    }
                    throw e instanceof RuntimeException ? (RuntimeException) e
                            : new RuntimeException(e);
                }
                Logger.logReportMessage("Sort suite: Synergy error during grid prep — recovering (attempt "
                        + attempt + ")");
                SynergyRetryUtil.recoverSessionIfNeeded();
                sortSuitePrepared = false;
                launchSortSuiteWithRecovery();
            }
        }
    }

    private void ensureSortGridReadyLight(String columnName, String section) throws InterruptedException {
        filterPanelUtil.ensureOrdersGridHome();
        String gridColumnLabel = sortGridColumnLabel(columnName);
        String columnId = gridSortUtil.resolveColumnId(gridColumnLabel, section);
        if (isGridColumnReadyForSortFast(gridColumnLabel, columnId)) {
            Logger.log("Sort suite: column still visible — skip view reactivation: " + gridColumnLabel);
            return;
        }
        if (lastSortTableViewName != null && !lastSortTableViewName.isEmpty()) {
            reactivateSortTableView(lastSortTableViewName);
            return;
        }
        tableViewUtill.waitForOrdersGridReadyFast(3);
    }

    private boolean isSortPrepTimedOut() {
        return sortColumnPrepDeadlineMs > 0 && System.currentTimeMillis() > sortColumnPrepDeadlineMs;
    }

    private void startSortColumnPrepDeadline() {
        sortColumnPrepDeadlineMs = System.currentTimeMillis() + SORT_COLUMN_PREP_DEADLINE_MS;
    }

    private void reactivateSortTableView(String viewName) throws InterruptedException {
        FulfillmentJsUtil.refreshTableViewsUiState();
        if (!TableViewGraphqlClient.hasCachedViewId(viewName)) {
            TableViewGraphqlClient.cacheViewFromGraphql(viewName);
        }
        if (!FulfillmentJsUtil.selectSavedViewViaScript(viewName)) {
            tableViewUtill.activateSavedViewOnOrdersGrid(viewName);
        }
        FulfillmentJsUtil.clickRefreshOrdersTable();
        tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
    }

    private boolean applySortViewFast(String viewName, String columnName, String section, String columnId)
            throws InterruptedException {
        try {
            tableViewUtill.closeManageColumnsIfOpen();
            filterPanelUtil.ensureOrdersGridHome();
            FulfillmentJsUtil.refreshTableViewsUiState();
            FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
            if (!TableViewGraphqlClient.hasCachedViewId(viewName)) {
                TableViewGraphqlClient.cacheViewFromGraphql(viewName);
            }
            if (!FulfillmentJsUtil.selectSavedViewViaScript(viewName)) {
                tableViewUtill.activateSavedViewOnOrdersGrid(viewName);
            }
            FulfillmentJsUtil.clickRefreshOrdersTable();
            tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
            return waitForSortColumnVisibleFast(columnName, columnId, 5);
        } catch (Exception e) {
            if (SynergyRetryUtil.isConnectionError(e)) {
                Logger.logReportMessage("Sort prep: apply view Synergy error — " + e.getMessage());
                return false;
            }
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    private boolean waitForSortColumnVisibleFast(String columnName, String columnId, int timeoutSec)
            throws InterruptedException {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end && !isSortPrepTimedOut()) {
            if (isGridColumnReadyForSortFast(columnName, columnId)) {
                return true;
            }
            FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            Thread.sleep(250);
        }
        return isGridColumnReadyForSortFast(columnName, columnId);
    }

    protected boolean ensureColumnVisibleForSort(String columnName, String section) throws InterruptedException {
        startSortColumnPrepDeadline();
        filterPanelUtil.ensureOrdersGridHome();
        if (lastSortTableViewName != null && !lastSortTableViewName.isEmpty()) {
            reactivateSortTableView(lastSortTableViewName);
        } else if (!sortSuitePrepared) {
            prepareSortGridForColumn();
        } else {
            tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
        }
        String gridColumnLabel = sortGridColumnLabel(columnName);
        String columnId = gridSortUtil.resolveColumnId(gridColumnLabel, section);

        if (isGridColumnReadyForSortFast(gridColumnLabel, columnId)) {
            Logger.log("Column already visible on grid — skip prep: " + gridColumnLabel);
            return true;
        }

        if (isDefaultTitleSeasonEpisodeSortColumn(gridColumnLabel, columnName)) {
            if (tableViewUtill.isTitleSeasonEpisodeGridVisible() || tableViewUtill.isOrdersGridReady()) {
                Logger.log("Title/Season/Episode grid ready — skip Manage Columns prep");
                return true;
            }
        }

        FulfillmentJsUtil.scrollColumnHeaderIntoView(gridColumnLabel, columnId);
        if (isGridColumnReadyForSortFast(gridColumnLabel, columnId)) {
            return true;
        }

        if (isSortPrepTimedOut()) {
            Logger.log("Sort prep timed out for " + gridColumnLabel);
            return false;
        }

        if ("order".equalsIgnoreCase(section)) {
            String viewName = buildSortTableViewName(columnName);
            if (TableViewGraphqlClient.hasCachedViewId(viewName)) {
                Logger.log("Sort prep: apply cached GraphQL view " + viewName);
                if (applySortViewFast(viewName, columnName, section, columnId)) {
                    lastSortTableViewName = viewName;
                    return true;
                }
            }
        }

        Logger.log("Sort prep: quick Standard-view enable for " + gridColumnLabel);
        if (tryQuickStandardViewEnable(gridColumnLabel, section, columnId)) {
            return true;
        }

        if (isSortPrepTimedOut()) {
            return false;
        }

        if ("order".equalsIgnoreCase(section)) {
            Logger.log("Sort prep: GraphQL create/apply for " + gridColumnLabel);
            return tryGraphqlSortColumnView(columnName, section, columnId);
        }
        return false;
    }

    private boolean tryQuickStandardViewEnable(String gridColumnLabel, String section, String columnId)
            throws InterruptedException {
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        if (!waitForColumnListedForSort(gridColumnLabel, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        tableViewUtill.scrollToColumn(gridColumnLabel, section);
        if (!tableViewUtill.ensureColumnCheckboxEnabled(gridColumnLabel, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        tableViewUtill.closeManageColumns();
        tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
        return waitForSortColumnVisibleFast(gridColumnLabel, columnId, 4);
    }

    private boolean tryGraphqlSortColumnView(String columnName, String section, String columnId)
            throws InterruptedException {
        if (isSortPrepTimedOut()) {
            return false;
        }
        String viewName = buildSortTableViewName(columnName);
        Logger.log("Sort prep: GraphQL table view for " + columnName + " as " + viewName);
        TableViewGraphqlClient.warmUpGraphqlAuthIfNeeded();
        String[] requiredColumns = sortGraphqlRequiredColumns(columnName);

        if (TableViewGraphqlClient.hasCachedViewId(viewName)
                && applySortViewFast(viewName, columnName, section, columnId)) {
            lastSortTableViewName = viewName;
            Logger.log("Sort prep: reused cached GraphQL view " + viewName);
            return true;
        }

        if (TableViewGraphqlClient.saveTableViewViaGraphql(viewName, requiredColumns)
                && applySortViewFast(viewName, columnName, section, columnId)) {
            lastSortTableViewName = viewName;
            Logger.log("Sort prep: direct GraphQL save applied for " + columnName);
            return true;
        }

        if (isSortPrepTimedOut()) {
            return false;
        }
        Logger.log("Sort prep: Manage Columns enable then GraphQL save for " + columnName);
        if (!enableSortColumnInManageColumns(columnName, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        if (!TableViewGraphqlClient.saveTableViewViaGraphql(viewName, requiredColumns)) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        tableViewUtill.closeManageColumnsIfOpen();
        if (applySortViewFast(viewName, columnName, section, columnId)) {
            lastSortTableViewName = viewName;
            return true;
        }
        return false;
    }

    private String[] sortGraphqlRequiredColumns(String columnName) {
        if (isDefaultTitleSeasonEpisodeSortColumn(sortGridColumnLabel(columnName), columnName)) {
            return new String[] { columnName, "Order ID" };
        }
        return new String[] { "Title, Season, Episode", columnName, "Order ID" };
    }

    private boolean enableSortColumnInManageColumns(String columnName, String section)
            throws InterruptedException {
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        if (!waitForColumnListedForSort(columnName, section)) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        forceEnableColumnOnStandardView(columnName, section);
        return tableViewUtill.isColumnCheckboxSelected(columnName, section)
                || FulfillmentJsUtil.isColumnChecked(columnName, section);
    }

    private String buildSortTableViewName(String columnName) {
        String safe = columnName.replaceAll("[^A-Za-z0-9]+", "-");
        if (safe.isEmpty()) {
            safe = "Col";
        }
        return "FF-SORT-" + safe;
    }

    private boolean isDefaultTitleSeasonEpisodeSortColumn(String gridColumnLabel, String columnName) {
        return "Title, Season, Episode".equalsIgnoreCase(gridColumnLabel)
                || "Title".equalsIgnoreCase(columnName)
                || "Season".equalsIgnoreCase(columnName)
                || "Episode".equalsIgnoreCase(columnName);
    }

    private boolean isGridColumnReadyForSortFast(String gridColumnLabel, String columnId) {
        return FulfillmentJsUtil.isColumnVisibleInGrid(gridColumnLabel, columnId)
                || gridSortUtil.isSortableHeaderVisible(columnId, gridColumnLabel);
    }

    private boolean waitForColumnListedForSort(String columnName, String section) throws InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            if (isSortPrepTimedOut()) {
                return false;
            }
            if (tableViewUtill.isColumnListed(columnName, section)) {
                return true;
            }
            try {
                tableViewUtill.scrollToColumn(columnName, section);
            } catch (Exception e) {
                if (SynergyRetryUtil.isConnectionError(e)) {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                }
            }
            Thread.sleep(300);
        }
        return tableViewUtill.isColumnListed(columnName, section);
    }

    private String sortGridColumnLabel(String columnName) {
        if ("Season".equalsIgnoreCase(columnName) || "Episode".equalsIgnoreCase(columnName)) {
            return "Title, Season, Episode";
        }
        return columnName;
    }

    private String sortColumnKey(String columnName, String section) {
        return section + "|" + columnName;
    }

    private boolean prepareSortColumnIfNeeded(String columnName, String section, String sortLabel, String columnId)
            throws InterruptedException {
        String columnKey = sortColumnKey(columnName, section);
        if (columnKey.equals(lastPreparedSortColumnKey)) {
            ensureSortGridReadyLight(columnName, section);
            return true;
        }
        renewSortSessionIfNearLimit();
        if (!columnKey.equals(lastPreparedSortColumnKey)) {
            lastSortTableViewName = null;
        }
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                if (ensureColumnVisibleForSort(columnName, section)
                        || canProceedWithSortWithoutColumnPrep(sortLabel, columnId)) {
                    lastPreparedSortColumnKey = columnKey;
                    return true;
                }
                return false;
            } catch (Exception e) {
                if (!SynergyRetryUtil.isConnectionError(e) || attempt >= 2) {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                }
                Logger.logReportMessage("Sort prep: Synergy session error — renewing and retrying column prep");
                forceRenewSortSession();
            }
        }
        return false;
    }

    protected void runSortAscendingTest(String columnName, String section, String valueType)
            throws InterruptedException {
        prepareSortSuiteOnce();
        String sortLabel = sortGridColumnLabel(columnName);
        String columnId = gridSortUtil.resolveColumnId(sortLabel, section);
        if (!prepareSortColumnIfNeeded(columnName, section, sortLabel, columnId)) {
            Verify.softAssert(false, columnName + " could not be prepared for sort test");
            return;
        }
        tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
        String effectiveValueType = valueType;
        if ("Season".equalsIgnoreCase(columnName) || "Episode".equalsIgnoreCase(columnName)) {
            effectiveValueType = "STRING";
        }
        gridSortUtil.sortAscending(columnId, sortLabel);
        tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
        gridSortUtil.verifySortedAscending(sortLabel, columnId, effectiveValueType, 2);
    }

    protected void runSortDescendingTest(String columnName, String section, String valueType)
            throws InterruptedException {
        prepareSortSuiteOnce();
        String sortLabel = sortGridColumnLabel(columnName);
        String columnId = gridSortUtil.resolveColumnId(sortLabel, section);
        if (!prepareSortColumnIfNeeded(columnName, section, sortLabel, columnId)) {
            Verify.softAssert(false, columnName + " could not be prepared for sort test");
            return;
        }
        tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
        String effectiveValueType = valueType;
        if ("Season".equalsIgnoreCase(columnName) || "Episode".equalsIgnoreCase(columnName)) {
            effectiveValueType = "STRING";
        }
        gridSortUtil.sortDescending(columnId, sortLabel);
        tableViewUtill.waitForOrdersGridReadyFast(SORT_GRID_WAIT_SEC);
        gridSortUtil.verifySortedDescending(sortLabel, columnId, effectiveValueType, 2);
    }

    private boolean canProceedWithSortWithoutColumnPrep(String sortLabel, String columnId)
            throws InterruptedException {
        filterPanelUtil.ensureOrdersGridHome();
        tableViewUtill.waitForOrdersGridReadyFast(3);
        long end = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < end && !isSortPrepTimedOut()) {
            FulfillmentJsUtil.scrollColumnHeaderIntoView(sortLabel, columnId);
            if (gridSortUtil.isSortableHeaderVisible(columnId, sortLabel)) {
                Logger.log("Sort header visible via grid — proceed without Manage Columns prep: " + sortLabel);
                return true;
            }
            Thread.sleep(300);
        }
        return false;
    }

    public static void resetSortSuiteState() {
        sortSuitePrepared = false;
        sortViewsPrefetched = false;
        sortSessionStartMs = 0L;
        lastPreparedSortColumnKey = null;
        lastSortTableViewName = null;
        FilterPanel_Util.resetYesterdayDateFilterState();
        ManageColumnsBaseTest.resetFulfillmentSession();
    }
}
