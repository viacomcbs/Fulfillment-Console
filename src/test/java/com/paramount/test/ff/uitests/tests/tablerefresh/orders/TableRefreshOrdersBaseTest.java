package com.paramount.test.ff.uitests.tests.tablerefresh.orders;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterSessionHelper;
import com.paramount.test.ff.uitests.helpers.orders.OrdersDataRecoveryHelper;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import com.paramount.test.ff.uitests.helpers.tablerefresh.TableRefreshUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/** Base setup for BSD-29582 Orders tab table refresh tests — shared session. */
public abstract class TableRefreshOrdersBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    protected TableRefreshUtil tableRefreshUtil;
    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();

    public TableRefreshOrdersBaseTest() {
        keepDriverAliveAfterTestMethod = true;
    }

    @BeforeClass(alwaysRun = true)
    @Override
    public void atStartSelenium() {
        try {
            if (driver.get() != null) {
                return;
            }
        } catch (Exception ignored) {
            // no driver yet
        }
        super.atStartSelenium();
    }

    @BeforeMethod(alwaysRun = true)
    public void tableRefreshOrdersSetup() throws InterruptedException {
        OrdersDataRecoveryHelper.resetRecoveryAttempt();
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();
        tableRefreshUtil = new TableRefreshUtil();
        softAssert = new SoftAssert("tableRefreshOrdersSetup", getClass().getSimpleName());

        if (!LeftFilterSessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for table refresh (Orders) tests");
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                    "Login failed — filter panel not visible after login");
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            LeftFilterSessionHelper.markLoggedIn();
            setCalendarToYesterdayOnce();
        } else {
            Logger.logReportMessage("Refreshing Fulfillment Console for next table refresh (Orders) test");
            driver.get().browser().refresh();
            WaitUtil.waitForJSToLoad(45);
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
        }

        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
    }

    private void setCalendarToYesterdayOnce() throws InterruptedException {
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
