package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;
import com.paramount.test.ff.uitests.helpers.orders.OrdersDataRecoveryHelper;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterSessionHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Base setup for Orders tab left-filter validation tests (one @Test per subclass).
 * Uses a shared browser session when run via {@code LeftFilterValidation_DevServerSuite.xml}.
 */
public abstract class LeftFilterOrdersTabBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();

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
            Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                    "Login failed — filter panel not visible after login");
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            LeftFilterSessionHelper.markLoggedIn();
            setCalendarToYesterdayOnce();
        } else {
            Logger.logReportMessage("Refreshing Fulfillment Console for next Orders left-filter test");
            driver.get().browser().refresh();
            WaitUtil.waitForJSToLoad(45);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }

        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
    }

    protected void setCalendarToYesterdayOnce() throws InterruptedException {
        if (!LeftFilterSessionHelper.isCalendarSetToYesterday()) {
            CalendarSetupUtil calendarSetup = new CalendarSetupUtil();
            calendarSetup.setDateRangeToYesterday(softAssert);
            if (!calendarSetup.isYesterdaySelected()) {
                calendarSetup.hardRefreshAndSetYesterday(softAssert);
            }
            LeftFilterSessionHelper.markCalendarSetToYesterday();
        }
    }
}
