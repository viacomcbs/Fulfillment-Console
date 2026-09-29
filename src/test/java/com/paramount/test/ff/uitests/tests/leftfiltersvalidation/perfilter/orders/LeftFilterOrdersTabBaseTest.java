package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.AutomationTableViewSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;
import com.paramount.test.ff.uitests.helpers.orders.OrdersDataRecoveryHelper;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterSessionHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;
import com.paramount.test.ff.common.listeners.PerFilterTestTrackerListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.ITestResult;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;
import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.POST_LOGIN_HOME_SETTLE_MS;

/**
 * Base setup for Orders tab per-filter left-filter validation tests.
 * Batch suites ({@code LeftFilterBatchMode=true}): one login, browser refresh between filter flow
 * tests, Synergy session renewal at ~20 min.
 */
@Listeners(PerFilterTestTrackerListener.class)
public abstract class LeftFilterOrdersTabBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();
    private final AutomationTableViewSetupUtil automationTableViewSetup = new AutomationTableViewSetupUtil();

    protected static final String DEFAULT_FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();
    protected static final String LARGE_OPTIONS_FILTER = OrdersLeftFilter.PARTNER.getDisplayName();
    protected static final String DEFAULT_FILTER_OPTION = "Done: Delivered";
    protected static final String DEFAULT_SEARCH_TEXT = "deliver";
    protected static final String PARTNER_FILTER_OPTION = "Antenna TV (Greece)";
    protected static final String PARTNER_SEARCH_TEXT = "antenna";

    public LeftFilterOrdersTabBaseTest() {
        keepDriverAliveAfterTestMethod = true;
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void atStartSelenium() {
        try {
            if (driver.get() != null) {
                Logger.logReportMessage("Reusing WebDriver for Orders left-filter tests");
                return;
            }
        } catch (Exception ignored) {
            // no driver yet
        }
        super.atStartSelenium();
        if (LeftFilterSessionHelper.getSynergySessionElapsedMs() == 0L && driver.get() != null) {
            LeftFilterSessionHelper.markSynergySessionStarted();
        }
    }

    /**
     * Override in each filter base — TC Basic expands once; TC Clear collapses at end.
     * FC preserves option selection until Clear or manual unselect (no re-check between tests).
     */
    protected String keepExpandedFilterName() {
        return null;
    }

    @BeforeClass(alwaysRun = true, dependsOnMethods = "atStartSelenium")
    public void enableKeepExpandedFilterForSuite() {
        String filter = keepExpandedFilterName();
        if (filter != null && !filter.isBlank()) {
            LeftFilterSessionHelper.markKeepFilterExpandedForSuite(filter);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void ordersLeftFilterSetup() throws InterruptedException {
        OrdersDataRecoveryHelper.resetRecoveryAttempt();
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();

        softAssert = new SoftAssert("ordersLeftFilterSetup", getClass().getSimpleName());

        LeftFilterSessionHelper.ensureBrowserSessionActive();
        renewSynergySessionIfNearLimit();

        if (!LeftFilterSessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for Orders left-filter tests");
            performFulfillmentLogin(softAssert);
        } else if (LeftFilterSessionHelper.shouldRefreshBetweenFilters()) {
            refreshBetweenFilters(softAssert);
        } else if (LeftFilterSessionHelper.shouldPreserveFilterSelectionOnSessionReuse()) {
            Logger.logReportMessage("Reusing Fulfillment Console session — preserving left-filter selection (no clear, no refresh)");
        } else {
            Logger.logReportMessage("Reusing Fulfillment Console session — clear filters only (no browser refresh)");
            leftFilterPanelUtil.clearAllActiveFiltersIfPresent();
        }

        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        if (requiresAutomationViewSetup()) {
            automationTableViewSetup.ensureColumnEnabled(softAssert, ConsoleTab.ORDERS, manageColumnsColumnToEnable());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterOrdersLeftFilterTest(ITestResult result) throws InterruptedException {
        markBatchFilterFlowCompleted(result);
        teardownKeepExpandedFilterIfClearFiltersTest(result);
        renewSynergySessionIfNearLimit();
    }

    private void markBatchFilterFlowCompleted(ITestResult result) {
        if (!LeftFilterSessionHelper.isBatchModeEnabled() || result == null) {
            return;
        }
        if (result.getStatus() == ITestResult.SKIP || result.getStatus() == ITestResult.CREATED) {
            return;
        }
        String className = getClass().getSimpleName();
        if (className.startsWith("LF_O_Flow_") || className.startsWith("LF_O_BatchFlow_")
                || className.endsWith("SelectAllTest")) {
            LeftFilterSessionHelper.markFilterFlowTestCompleted();
            Logger.logReportMessage("Batch mode: completed filter flow test #"
                    + LeftFilterSessionHelper.getCompletedFilterFlowTests() + " (" + className + ")");
        }
    }

    /** Clear-filters scenario — collapse accordion after clear so Select-all (if next) starts from a clean panel. */
    private void teardownKeepExpandedFilterIfClearFiltersTest(ITestResult result) {
        if (result == null || leftFilterPanelUtil == null) {
            return;
        }
        if (!getClass().getSimpleName().endsWith("ClearFiltersTest")) {
            return;
        }
        String filter = keepExpandedFilterName();
        if (filter == null || filter.isBlank()) {
            return;
        }
        try {
            LeftFilterSessionHelper.clearKeepFilterExpanded();
            leftFilterPanelUtil.forceCollapseFilter(filter);
            Logger.logReportMessage(filter + " filter suite complete — accordion collapsed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void performFulfillmentLogin(SoftAssert softAssert) throws InterruptedException {
        DriverUtil.launchApplicationOnBrowser();
        login.loginToFF();
        WaitUtil.waitForJSToLoad(15);
        FulfillmentJsUtil.dismissReleaseNotesModal();
        waitForPostLoginHomeSettleIfRequired();
        ensureYesterdayCalendar(softAssert);
        Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(true),
                "Login failed — filter panel not visible after login");
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        LeftFilterSessionHelper.markLoggedIn();
    }

    /**
     * Batch mode: browser refresh between filter flow tests — skips Synergy re-login but resets FC UI state.
     */
    private void refreshBetweenFilters(SoftAssert softAssert) throws InterruptedException {
        Logger.logReportMessage("Batch mode: browser refresh between filters (skip login, reset UI state)");
        driver.get().browser().refresh();
        ordersTabSetup.ensureFulfillmentConsoleReady();
        FulfillmentJsUtil.dismissReleaseNotesModal();
        ensureYesterdayCalendar(softAssert);
        Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                "Filter panel not visible after batch refresh between filters");
        leftFilterPanelUtil.clearAllActiveFiltersIfPresent();
        leftFilterPanelUtil.validateClearFiltersNotActive(softAssert);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
    }

    /**
     * Batch mode: stop Synergy driver and start a fresh 30-min session before the hard limit (~20 min elapsed).
     */
    private void renewSynergySessionIfNearLimit() throws InterruptedException {
        if (!LeftFilterSessionHelper.shouldRenewSynergySession()) {
            return;
        }
        long elapsedMin = LeftFilterSessionHelper.getSynergySessionElapsedMs() / 60_000L;
        Logger.logReportMessage("Batch mode: renewing Synergy session after ~" + elapsedMin
                + " min (before 30 min MaxTestTime limit)");
        try {
            if (driver.get() != null) {
                driver.get().stop();
            }
        } catch (Exception ignored) {
            // session may already be closed
        }
        driver.remove();
        LeftFilterSessionHelper.resetForSessionRenewal();
        BaseTest.ensureDriverStarted();
        if (driver.get() != null) {
            LeftFilterSessionHelper.markSynergySessionStarted();
        }
        performFulfillmentLogin(softAssert);
    }

    /**
     * 30s home-page settle after login success (zoom applied) before opening the calendar picker.
     */
    protected boolean requiresPostLoginHomeSettle() {
        return true;
    }

    private void waitForPostLoginHomeSettleIfRequired() throws InterruptedException {
        if (!requiresPostLoginHomeSettle() || LeftFilterSessionHelper.isPostLoginHomeSettleComplete()) {
            return;
        }
        Logger.logReportMessage("Post-login home page settle — waiting "
                + (POST_LOGIN_HOME_SETTLE_MS / 1000)
                + "s after login success before opening calendar picker");
        Thread.sleep(POST_LOGIN_HOME_SETTLE_MS);
        LeftFilterSessionHelper.markPostLoginHomeSettleComplete();
    }

    /** Override to {@code false} only for Order Status and Line Item Status (mandatory grid columns, not in Manage columns). */
    protected boolean requiresAutomationViewSetup() {
        return true;
    }

    /** Column to enable once per session via Manage columns before filter tests run. */
    protected String manageColumnsColumnToEnable() {
        return ManageColumnOptions.ACTIVITY_TYPE;
    }

    /**
     * Ensures toolbar date is Yesterday. If the toolbar already shows Yesterday on login, that means
     * Yesterday is the app default — skip the calendar picker entirely. After a browser refresh the
     * app may reload with a different range (e.g. Last 7 days); then we open the picker and apply Yesterday.
     */
    protected void ensureYesterdayCalendar(SoftAssert softAssert) throws InterruptedException {
        CalendarSetupUtil calendarSetup = new CalendarSetupUtil();
        if (LeftFilterSessionHelper.isCalendarSetToYesterday() && calendarSetup.isYesterdaySelected()) {
            Logger.logReportMessage("Yesterday already confirmed this session — skip calendar");
            return;
        }
        calendarSetup.setDateRangeToYesterday(softAssert);
        LeftFilterSessionHelper.markCalendarSetToYesterday();
    }
}
