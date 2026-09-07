package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Base setup for Orders tab per-filter left-filter validation tests.
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
    }

    @BeforeMethod(alwaysRun = true)
    public void ordersLeftFilterSetup() throws InterruptedException {
        OrdersDataRecoveryHelper.resetRecoveryAttempt();
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();

        softAssert = new SoftAssert("ordersLeftFilterSetup", getClass().getSimpleName());

        if (!LeftFilterSessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for Orders left-filter tests");
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            WaitUtil.waitForJSToLoad(15);
            ensureYesterdayCalendar(softAssert);
            Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                    "Login failed — filter panel not visible after login");
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            LeftFilterSessionHelper.markLoggedIn();
        } else {
            Logger.logReportMessage("Refreshing Fulfillment Console for next Orders left-filter test");
            driver.get().browser().refresh();
            WaitUtil.waitForJSToLoad(45);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            ensureYesterdayCalendar(softAssert);
            leftFilterPanelUtil.clearAllActiveFiltersIfPresent();
        }

        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        if (requiresAutomationViewSetup()) {
            automationTableViewSetup.ensureColumnEnabled(softAssert, ConsoleTab.ORDERS, manageColumnsColumnToEnable());
        }
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
     * Ensures toolbar date is Yesterday. After a browser refresh the app reloads with the default
     * range (e.g. Last 7 days) even when {@link LeftFilterSessionHelper} already recorded Yesterday.
     */
    protected void ensureYesterdayCalendar(SoftAssert softAssert) throws InterruptedException {
        CalendarSetupUtil calendarSetup = new CalendarSetupUtil();
        if (!calendarSetup.isYesterdaySelected()) {
            Logger.logReportMessage("Calendar is not Yesterday — applying Yesterday bookmark and date");
            calendarSetup.setDateRangeToYesterday(softAssert);
        } else {
            Logger.logReportMessage("Yesterday calendar already on toolbar — skip");
        }
        LeftFilterSessionHelper.markCalendarSetToYesterday();
    }
}
