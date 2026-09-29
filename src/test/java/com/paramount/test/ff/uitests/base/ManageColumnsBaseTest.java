package com.paramount.test.ff.uitests.base;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.common.util.SynergyRetryUtil;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.OrderTabColumnData;
import com.paramount.test.ff.uitests.helpers.TableViewGraphqlClient;
import com.paramount.test.ff.uitests.helpers.HomePage_util;
import com.paramount.test.ff.uitests.helpers.TableView_utill;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Base class for Manage Columns / Table View Synergy tests (Orders tab).
 * Reuses one browser session across TC_001–TC_010 for faster suite runs.
 */
public abstract class ManageColumnsBaseTest extends BaseTest {

    protected ManageColumnsBaseTest() {
        keepDriverAliveAfterTestMethod = true;
    }

    private static boolean fulfillmentSessionReady;
    private static boolean columnTestGridPrepared;
    private static boolean bsd30034SuiteBootstrapped;
    private static boolean bsd30034DateColumnsEnabled;
    private static long columnSessionStartMs;
    /** Renew at 20 min — leaves ~10 min buffer before Synergy 30 min limit (renewal ~3–5 min). */
    private static final long COLUMN_SESSION_RENEW_MS = 20L * 60L * 1000L;
    private static final int COLUMN_GRID_WAIT_SEC = 8;
    private static final int COLUMN_HEADER_WAIT_SEC = 12;

    protected TableView_utill tableViewUtill;
    protected HomePage_util homePageUtil;
    protected FilterPanel_Util filterPanelUtil;
    protected Login login;

    @BeforeClass(alwaysRun = true)
    public void manageColumnsSetup() {
        ensureHelpersInitialized();
    }

    @BeforeMethod(alwaysRun = true)
    public void ensureColumnSessionHealthy() {
        FulfillmentJsUtil.useFastElementTimeout();
        try {
            BaseTest.requireDriver().getSessionID();
            try {
                renewColumnSessionIfNearLimit();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            Logger.logReportMessage("Column suite: Synergy session unhealthy — recovering before test");
            SynergyRetryUtil.recoverSessionIfNeeded();
            columnTestGridPrepared = false;
            fulfillmentSessionReady = false;
            columnSessionStartMs = 0L;
        }
        ensureHelpersInitialized();
    }

    protected void ensureHelpersInitialized() {
        if (tableViewUtill == null) {
            tableViewUtill = new TableView_utill();
        }
        if (homePageUtil == null) {
            homePageUtil = new HomePage_util();
        }
        if (filterPanelUtil == null) {
            filterPanelUtil = new FilterPanel_Util();
        }
        if (login == null) {
            login = new Login();
        }
    }

    protected void initSoftAssert(String methodName) {
        softAssert = new SoftAssert(methodName, getClass().getSimpleName());
    }

    protected void launchFulfillmentConsole() {
        ensureHelpersInitialized();
        FulfillmentJsUtil.useFastElementTimeout();

        if (fulfillmentSessionReady && isFulfillmentConsoleReady()) {
            Logger.logReportMessage("Reusing Fulfillment Console session (fast path)");
            login.ensureMaximizedWithPageZoom();
            tableViewUtill.waitForOrdersGridReadyFast(COLUMN_GRID_WAIT_SEC);
            return;
        }

        BaseTest.ensureDriverStarted();
        if (fulfillmentSessionReady || BaseTest.driver.get() != null) {
            if (!isFulfillmentConsoleReady()) {
                Logger.logReportMessage("Restoring Fulfillment Console (soft recovery, no cold start)");
                try {
                    filterPanelUtil.ensureOrdersGridHome();
                } catch (Exception e) {
                    Logger.logConsoleMessage("Orders grid home restore skipped: " + e.getMessage());
                }
                if (!isFulfillmentConsoleReady()) {
                    DriverUtil.launchApplicationOnBrowser();
                }
            } else {
                Logger.logReportMessage("Reusing Fulfillment Console session");
            }
        } else {
            Logger.logReportMessage("Fulfillment Launching application");
            DriverUtil.launchApplicationOnBrowser();
        }

        login.loginToFF();
        FulfillmentJsUtil.installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        fulfillmentSessionReady = true;
        if (columnSessionStartMs <= 0L) {
            columnSessionStartMs = System.currentTimeMillis();
        }
        tableViewUtill.waitForOrdersGridReadyFast(COLUMN_GRID_WAIT_SEC);
    }

    private void renewColumnSessionIfNearLimit() throws InterruptedException {
        if (columnSessionStartMs <= 0L
                || System.currentTimeMillis() - columnSessionStartMs < COLUMN_SESSION_RENEW_MS) {
            return;
        }
        Logger.logReportMessage("Column suite: renewing Synergy session before max test time");
        forceRenewColumnSession();
    }

    private void forceRenewColumnSession() throws InterruptedException {
        Logger.logReportMessage("Column suite: stopping Synergy session for renewal");
        try {
            BaseTest.requireDriver().stop();
        } catch (Exception ignored) {
        }
        BaseTest.driver.remove();
        resetFulfillmentSession();
        BaseTest.ensureDriverStarted();
        DriverUtil.launchApplicationOnBrowser();
        login.loginToFF();
        FulfillmentJsUtil.installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        fulfillmentSessionReady = true;
        FulfillmentJsUtil.useFastElementTimeout();
        filterPanelUtil.ensureOrdersGridHome();
        if (!FilterPanel_Util.isYesterdayDateFilterCached()) {
            filterPanelUtil.applyYesterdayDateFilter();
        }
        tableViewUtill.activateStandardViewOnOrdersGrid();
        tableViewUtill.waitForOrdersGridReadyFast(COLUMN_GRID_WAIT_SEC);
        columnTestGridPrepared = true;
        columnSessionStartMs = System.currentTimeMillis();
    }

    /** Login, apply Yesterday date filter, and wait for orders grid — run before column tests. */
    protected void prepareOrdersGridForColumnTest() throws InterruptedException {
        launchFulfillmentConsole();
        if (columnTestGridPrepared && FulfillmentJsUtil.isFulfillmentConsoleReady()) {
            filterPanelUtil.ensureOrdersGridHome();
            tableViewUtill.waitForOrdersGridReadyFast(5);
            return;
        }
        FulfillmentJsUtil.useFastElementTimeout();
        filterPanelUtil.ensureOrdersGridHome();
        if (!FilterPanel_Util.isYesterdayDateFilterCached()) {
            filterPanelUtil.applyYesterdayDateFilter();
        }
        tableViewUtill.activateStandardViewOnOrdersGrid();
        tableViewUtill.waitForOrdersGridReadyFast(COLUMN_GRID_WAIT_SEC);
        columnTestGridPrepared = true;
        if (columnSessionStartMs <= 0L) {
            columnSessionStartMs = System.currentTimeMillis();
        }
    }

    private boolean isFulfillmentConsoleReady() {
        return tableViewUtill.isOrdersGridReady();
    }

    /**
     * One-time BSD-30034 suite setup: login, Yesterday filter, and pre-enable delivery date columns.
     */
    public void prepareBsd30034SuiteOnce(String deliveryDateName, String offsetDeliveryDateName, String section)
            throws InterruptedException {
        synchronized (ManageColumnsBaseTest.class) {
            if (bsd30034SuiteBootstrapped) {
                return;
            }
            ensureHelpersInitialized();
            FulfillmentJsUtil.useFastElementTimeout();
            Logger.logReportMessage("BSD-30034 suite: one-time login, Yesterday filter, orders grid ready");
            launchFulfillmentConsole();
            TableViewGraphqlClient.warmUpGraphqlAuth();
            tableViewUtill.waitForOrdersGridReadyFast(25);
            FulfillmentJsUtil.waitForOrdersGridRendered(20);
            if (!FulfillmentJsUtil.waitForOrdersGridRendered(5)) {
                FulfillmentJsUtil.clickRefreshOrdersTable();
                Thread.sleep(2500);
                tableViewUtill.waitForOrdersGridReadyFast(15);
            }
            try {
                filterPanelUtil.applyYesterdayDateFilter();
            } catch (Exception e) {
                Logger.logConsoleMessage("Yesterday filter skipped during BSD-30034 bootstrap: " + e.getMessage());
            }
            tableViewUtill.waitForOrdersGridReadyFast(20);
            if (!FulfillmentJsUtil.waitForOrdersGridRendered(15)) {
                FulfillmentJsUtil.clickRefreshOrdersTable();
                Thread.sleep(2500);
                tableViewUtill.waitForOrdersGridReadyFast(15);
            }
            tableViewUtill.openManageColumns();
            tableViewUtill.selectStandardView();
            enableBsd30034ColumnIfListed(deliveryDateName, section);
            enableBsd30034ColumnIfListed(offsetDeliveryDateName, section);
            tableViewUtill.closeManageColumns();
            tableViewUtill.closeManageColumnsIfOpen();
            FulfillmentJsUtil.dismissBlockingOverlays();
            Thread.sleep(800);
            tableViewUtill.waitForOrdersGridReadyFast(15);
            FulfillmentJsUtil.waitForOrdersGridRendered(15);
            boolean deliveryVisible = tableViewUtill.isGridColumnHeaderVisibleForSearch(deliveryDateName, section);
            boolean offsetVisible = tableViewUtill.isGridColumnHeaderVisibleForSearch(offsetDeliveryDateName, section);
            bsd30034DateColumnsEnabled = deliveryVisible && offsetVisible;
            bsd30034SuiteBootstrapped = true;
            columnTestGridPrepared = true;
            FulfillmentJsUtil.installExportCaptureHook();
            Logger.logReportMessage("BSD-30034 suite: Delivery date columns pre-enabled for search/sort/export (enabled="
                    + bsd30034DateColumnsEnabled + ")");
        }
    }

    /**
     * TC024 export bootstrap: grid ready first, then Yesterday + Done filters, then columns only if missing.
     */
    public void prepareBsd30034ExportSuiteOnce(String deliveryDateName, String offsetDeliveryDateName, String section)
            throws InterruptedException {
        synchronized (ManageColumnsBaseTest.class) {
            if (bsd30034SuiteBootstrapped) {
                return;
            }
            ensureHelpersInitialized();
            FulfillmentJsUtil.useFastElementTimeout();
            Logger.logReportMessage("BSD-30034 export suite: TC024 login and filter bootstrap");
            try {
                launchExportSuiteWithRecovery();
                if (!ensureExportBootstrapGridReady(90)) {
                    Logger.logReportMessage("Export bootstrap: grid not ready after recovery chain");
                    FulfillmentJsUtil.logOrdersGridDiagnostics();
                }
                if (FulfillmentJsUtil.isOrdersGridRendered()) {
                    applyExportTc024Filters();
                    ensureExportColumnsOnGrid(deliveryDateName, offsetDeliveryDateName, section);
                } else {
                    Logger.logReportMessage("Export bootstrap: skipping filters — grid never rendered");
                    bsd30034DateColumnsEnabled = false;
                }
                bsd30034SuiteBootstrapped = true;
                columnTestGridPrepared = true;
                FulfillmentJsUtil.installExportCaptureHook();
                Logger.logReportMessage("BSD-30034 export suite bootstrap complete (columns="
                        + bsd30034DateColumnsEnabled + ", rows="
                        + FulfillmentJsUtil.countOrdersGridTopRows() + ")");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (RuntimeException e) {
                if (SynergyRetryUtil.isConnectionError(e)) {
                    SynergyRetryUtil.recoverSessionIfNeeded();
                }
                throw e;
            }
        }
    }

    private void launchExportSuiteWithRecovery() throws InterruptedException {
        try {
            launchFulfillmentConsole();
            TableViewGraphqlClient.warmUpGraphqlAuth();
        } catch (Exception e) {
            if (!SynergyRetryUtil.isConnectionError(e)) {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            }
            Logger.logReportMessage("Export suite: launch failed — recovering WebDriver and retrying");
            SynergyRetryUtil.recoverSessionIfNeeded();
            launchFulfillmentConsole();
            TableViewGraphqlClient.warmUpGraphqlAuth();
        }
    }

    private boolean ensureExportBootstrapGridReady(int timeoutSec) throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        FulfillmentJsUtil.applyPageZoom("0.67");
        filterPanelUtil.ensureOrdersGridHome();
        tableViewUtill.waitForOrdersGridReadyFast(20);
        if (FulfillmentJsUtil.waitForOrdersGridReadyForExport(Math.min(timeoutSec, 45))) {
            return true;
        }
        if (!FulfillmentJsUtil.isFulfillmentConsoleReady()) {
            Logger.logReportMessage("Export bootstrap: console shell not ready — recovering session");
            try {
                resetFulfillmentSession();
                launchExportSuiteWithRecovery();
            } catch (Exception e) {
                if (SynergyRetryUtil.isConnectionError(e)) {
                    SynergyRetryUtil.recoverSessionIfNeeded();
                    resetFulfillmentSession();
                    launchExportSuiteWithRecovery();
                } else {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                }
            }
            FulfillmentJsUtil.applyPageZoom("0.67");
            filterPanelUtil.ensureOrdersGridHome();
            tableViewUtill.waitForOrdersGridReadyFast(20);
        }
        if (FulfillmentJsUtil.isOrdersGridRendered()) {
            return true;
        }
        Logger.logReportMessage("Export bootstrap: activating Standard view on Orders grid");
        tableViewUtill.activateStandardViewOnOrdersGrid();
        if (FulfillmentJsUtil.waitForOrdersGridReadyForExport(30)) {
            return true;
        }
        Logger.logReportMessage("Export bootstrap: reloading fulfillment home");
        FulfillmentJsUtil.reloadFulfillmentPage();
        FulfillmentJsUtil.applyPageZoom("0.67");
        filterPanelUtil.ensureOrdersGridHome();
        tableViewUtill.activateStandardViewOnOrdersGrid();
        if (FulfillmentJsUtil.waitForOrdersGridReadyForExport(Math.min(timeoutSec, 45))) {
            return true;
        }
        FulfillmentJsUtil.clickRefreshOrdersTable();
        Thread.sleep(2000);
        return FulfillmentJsUtil.waitForOrdersGridReadyForExport(Math.min(timeoutSec, 30));
    }

    private void applyExportTc024Filters() throws InterruptedException {
        if (!FilterPanel_Util.isYesterdayDateFilterAttempted()
                || !filterPanelUtil.isYesterdayDateFilterActiveStrict()) {
            filterPanelUtil.ensureYesterdayDateFilterForExport();
            FulfillmentJsUtil.waitForOrdersGridRendered(20);
        }
        if (!FulfillmentJsUtil.isOrdersGridRendered()) {
            Logger.logReportMessage("Skipping Done filter - grid not rendered after Yesterday");
            return;
        }
        filterPanelUtil.filterDeliveredOrdersOnly();
        Thread.sleep(1200);
        FulfillmentJsUtil.waitForOrdersGridRendered(15);
        filterPanelUtil.ensureTc024EnvironmentFilterForExport();
        FulfillmentJsUtil.waitForOrdersGridRendered(12);
        tableViewUtill.activateStandardViewOnOrdersGrid();
        FulfillmentJsUtil.waitForOrdersGridHeadersReady(30);
    }

    private void ensureExportColumnsOnGrid(String deliveryDateName, String offsetDeliveryDateName, String section)
            throws InterruptedException {
        if (!FulfillmentJsUtil.isOrdersGridRendered()) {
            bsd30034DateColumnsEnabled = false;
            Logger.logReportMessage("Skipping manage columns - orders grid not rendered");
            return;
        }
        boolean deliveryVisible = isExportColumnVisibleOnGrid(deliveryDateName, "deliveryDate");
        boolean offsetVisible = isExportColumnVisibleOnGrid(offsetDeliveryDateName, "deliveryOffset");
        if (deliveryVisible && offsetVisible) {
            bsd30034DateColumnsEnabled = true;
            return;
        }
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        enableBsd30034ColumnIfListed(deliveryDateName, section);
        enableBsd30034ColumnIfListed(offsetDeliveryDateName, section);
        tableViewUtill.closeManageColumns();
        tableViewUtill.closeManageColumnsIfOpen();
        FulfillmentJsUtil.closeManageColumnsPanel();
        FulfillmentJsUtil.dismissBlockingOverlays();
        FulfillmentJsUtil.clickRefreshOrdersTable();
        Thread.sleep(2000);
        FulfillmentJsUtil.waitForOrdersGridHeadersReady(20);
        deliveryVisible = isExportColumnVisibleOnGrid(deliveryDateName, "deliveryDate");
        offsetVisible = isExportColumnVisibleOnGrid(offsetDeliveryDateName, "deliveryOffset");
        bsd30034DateColumnsEnabled = deliveryVisible && offsetVisible;
    }

    private boolean isExportColumnVisibleOnGrid(String columnName, String columnId) throws InterruptedException {
        if (!FulfillmentJsUtil.isOrdersGridRendered()) {
            return false;
        }
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        return FulfillmentJsUtil.isColumnVisibleInGrid(columnName, columnId);
    }

    public void refreshBsd30034GridState() throws InterruptedException {
        launchFulfillmentConsole();
        filterPanelUtil.ensureOrdersGridHome();
        tableViewUtill.waitForOrdersGridReadyFast(10);
    }

    public static boolean isBsd30034DateColumnsEnabled() {
        return bsd30034DateColumnsEnabled;
    }

    private void enableBsd30034ColumnIfListed(String columnName, String section) throws InterruptedException {
        if (!tableViewUtill.isColumnListed(columnName, section)) {
            Logger.log("BSD-30034 bootstrap: column not listed - " + columnName);
            return;
        }
        tableViewUtill.scrollToColumn(columnName, section);
        tableViewUtill.ensureColumnCheckboxEnabled(columnName, section);
    }

    protected void launchAndOpenManageColumns() throws InterruptedException {
        prepareOrdersGridForColumnTest();
        if (tableViewUtill.isManageColumnsPanelOpen() && tableViewUtill.isStandardViewSelected()) {
            Logger.logReportMessage("Manage Columns already open with Standard View");
            return;
        }
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
    }

    /**
     * TC021 / TC_025 column-show flow for all Manage Columns Show tests.
     * Scroll to column, enable (toggle off→on when already checked), refresh grid, verify header.
     */
    protected void runColumnShowTest(String columnName, String section) throws InterruptedException {
        runColumnShowTestWithHorizontalScroll(columnName, section, resolveColumnIdForShow(columnName, section));
    }

    /** @deprecated Use {@link #runColumnShowTest(String, String)} — column id is resolved automatically. */
    protected void runColumnShowTestWithHorizontalScroll(String columnName, String section, String columnId)
            throws InterruptedException {
        prepareOrdersGridForColumnTest();
        ensureStandardViewForColumnTest();
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        tableViewUtill.scrollToColumn(columnName, section);

        if (!tableViewUtill.isColumnListed(columnName, section)
                && !FulfillmentJsUtil.isColumnListed(columnName, section)) {
            Verify.softAssert(false,
                    columnName + " is not listed in Manage Columns [" + section + "]");
            tableViewUtill.closeManageColumns();
            return;
        }

        tableViewUtill.scrollToColumn(columnName, section);
        enableColumnForShowOnStandardView(columnName, section);
        Verify.softAssert(
                tableViewUtill.isColumnCheckboxSelected(columnName, section),
                columnName + " checkbox is selected in Manage Columns");

        tableViewUtill.closeManageColumns();
        tableViewUtill.closeManageColumnsIfOpen();
        refreshOrdersGridAfterColumnChange();
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);

        boolean visible = tableViewUtill.waitForGridColumnVisibleForSearch(columnName, section, COLUMN_HEADER_WAIT_SEC);
        if (!visible && tableViewUtill.isColumnCheckboxSelected(columnName, section)) {
            Logger.logReportMessage("Checkbox selected but grid header missing — force toggle for " + columnName);
            tableViewUtill.openManageColumns();
            tableViewUtill.selectStandardView();
            forceEnableColumnOnStandardView(columnName, section);
            tableViewUtill.closeManageColumns();
            tableViewUtill.closeManageColumnsIfOpen();
            refreshOrdersGridAfterColumnChange();
            FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
            visible = tableViewUtill.waitForGridColumnVisibleForSearch(columnName, section, COLUMN_HEADER_WAIT_SEC);
        }

        if (!visible && "order".equalsIgnoreCase(section)) {
            Logger.logReportMessage("Standard-view close did not surface column — GraphQL view for " + columnName);
            visible = tryGraphqlColumnShowView(columnName, section, columnId);
        }

        if (!visible) {
            FulfillmentJsUtil.logOrdersGridHeaderLabels();
        }
        Verify.softAssert(visible, columnName + " is visible on Orders grid");
    }

    /** TC021 parity: enable when unchecked; when already checked, leave panel state until grid verify fails. */
    private void enableColumnForShowOnStandardView(String columnName, String section)
            throws InterruptedException {
        tableViewUtill.scrollToColumn(columnName, section);
        if (!tableViewUtill.isColumnCheckboxSelected(columnName, section)) {
            tableViewUtill.enableColumn(columnName, section);
            Thread.sleep(400);
        }
    }

    private void refreshOrdersGridAfterColumnChange() throws InterruptedException {
        tableViewUtill.waitForOrdersGridReadyFast(6);
        FulfillmentJsUtil.clickRefreshOrdersTable();
        tableViewUtill.waitForOrdersGridReadyFast(6);
        Thread.sleep(200);
    }

    /** GraphQL saved view with Order start date + Order ID only — faster than UI save on PROD. */
    private boolean tryGraphqlColumnShowView(String columnName, String section, String columnId)
            throws InterruptedException {
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        if (!tableViewUtill.isColumnListed(columnName, section)) {
            tableViewUtill.closeManageColumns();
            return false;
        }
        tableViewUtill.scrollToColumn(columnName, section);
        forceEnableColumnOnStandardView(columnName, section);

        String viewName = buildColumnShowViewName(columnName);
        FulfillmentJsUtil.installNetworkCaptureHook();
        TableViewGraphqlClient.installAuthCaptureHook();
        if (!TableViewGraphqlClient.saveTableViewViaGraphql(viewName, columnName, "Order ID")) {
            tableViewUtill.closeManageColumnsIfOpen();
            return false;
        }
        tableViewUtill.closeManageColumnsIfOpen();
        filterPanelUtil.ensureOrdersGridHome();
        FulfillmentJsUtil.refreshTableViewsUiState();
        FulfillmentJsUtil.cacheSavedViewFromApi(viewName);
        TableViewGraphqlClient.cacheViewFromGraphql(viewName);
        FulfillmentJsUtil.selectSavedViewViaScript(viewName);
        tableViewUtill.closeManageColumnsIfOpen();
        refreshOrdersGridAfterColumnChange();
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if (tableViewUtill.waitForGridColumnVisibleForSearch(columnName, section, COLUMN_HEADER_WAIT_SEC)) {
            return true;
        }
        return tableViewUtill.applySavedTableViewForColumnSearch(viewName, columnName, section);
    }

    private void ensureStandardViewForColumnTest() throws InterruptedException {
        if (FulfillmentJsUtil.isActiveTableView("Standard view")) {
            return;
        }
        tableViewUtill.activateStandardViewOnOrdersGrid();
    }

    protected void forceEnableColumnOnStandardView(String columnName, String section) throws InterruptedException {
        tableViewUtill.scrollToColumn(columnName, section);
        if (FulfillmentJsUtil.isColumnChecked(columnName, section)) {
            FulfillmentJsUtil.setColumnChecked(columnName, section, false);
            Thread.sleep(200);
        }
        tableViewUtill.enableColumn(columnName, section);
        Thread.sleep(250);
        if (!tableViewUtill.isColumnCheckboxSelected(columnName, section)) {
            FulfillmentJsUtil.setColumnChecked(columnName, section, true);
            Thread.sleep(250);
        }
    }

    private String resolveColumnIdForShow(String columnName, String section) {
        OrderTabColumnData.ColumnDef column = OrderTabColumnData.findByLabelAndSection(columnName, section);
        if (column != null) {
            return column.id;
        }
        column = OrderTabColumnData.findByIdAndSection(columnName, section);
        if (column != null) {
            return column.id;
        }
        return columnName.replaceAll("[^A-Za-z0-9]+", "");
    }

    private String buildColumnShowViewName(String columnName) {
        String safe = columnName.replaceAll("[^A-Za-z0-9]+", "");
        if (safe.isEmpty()) {
            safe = "Col";
        }
        return "AutoColShow_" + safe + "_" + System.currentTimeMillis();
    }

    protected void runColumnHideTest(String columnName, String section) throws InterruptedException {
        prepareOrdersGridForColumnTest();
        ensureStandardViewForColumnTest();
        tableViewUtill.closeManageColumnsIfOpen();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
        tableViewUtill.scrollToColumn(columnName, section);

        if (!tableViewUtill.isColumnListed(columnName, section)
                && !FulfillmentJsUtil.isColumnListed(columnName, section)) {
            Verify.softAssert(false,
                    columnName + " is not listed in Manage Columns [" + section + "]");
            tableViewUtill.closeManageColumns();
            return;
        }
        tableViewUtill.scrollToColumn(columnName, section);
        tableViewUtill.disableColumn(columnName, section);
        if (tableViewUtill.isColumnCheckboxSelected(columnName, section)) {
            FulfillmentJsUtil.setColumnChecked(columnName, section, false);
        }
        boolean columnHidden = !tableViewUtill.isColumnCheckboxSelected(columnName, section);
        tableViewUtill.closeManageColumns();
        tableViewUtill.waitForOrdersGridReadyFast(COLUMN_GRID_WAIT_SEC);
        if ("package".equalsIgnoreCase(section) || "lineitem".equalsIgnoreCase(section)) {
            Verify.softAssert(columnHidden, columnName + " is hidden on Orders grid");
        } else {
            tableViewUtill.verifyGridHeaderHidden(columnName, section);
        }
    }

    public static void resetFulfillmentSession() {
        fulfillmentSessionReady = false;
        columnTestGridPrepared = false;
        columnSessionStartMs = 0L;
        bsd30034SuiteBootstrapped = false;
        bsd30034DateColumnsEnabled = false;
        FilterPanel_Util.resetYesterdayDateFilterState();
        Login.resetSessionState();
    }

    protected static void syncColumnSessionRenewed() {
        columnSessionStartMs = System.currentTimeMillis();
    }
}
