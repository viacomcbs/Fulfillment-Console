package com.paramount.test.ff.uitests.tests.tablevalidation.bsd29967;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967ColumnUtil;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967DetailsUtil;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967OpsConsoleUtil;
import com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967SessionHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.orders.OrdersDataRecoveryHelper;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/** Base for BSD-29967 — Yesterday, round-robin 3 non-zero + UWFFSP Environment workflows, max 2 orders per workflow when DSIDs blank, shared session. */
public abstract class Bsd29967OrdersBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    protected Bsd29967ColumnUtil columnUtil;
    protected Bsd29967DetailsUtil detailsUtil;
    protected Bsd29967OpsConsoleUtil opsConsoleUtil;
    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();

    public Bsd29967OrdersBaseTest() {
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
    public void bsd29967OrdersSetup() throws InterruptedException {
        OrdersDataRecoveryHelper.resetRecoveryAttempt();
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();
        columnUtil = new Bsd29967ColumnUtil();
        detailsUtil = new Bsd29967DetailsUtil();
        opsConsoleUtil = new Bsd29967OpsConsoleUtil();
        softAssert = new SoftAssert("bsd29967OrdersSetup", getClass().getSimpleName());

        if (!Bsd29967SessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for BSD-29967 validation");
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                    "Login failed — filter panel not visible after login");
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            setCalendarToYesterdayOnce();
            Bsd29967SessionHelper.markLoggedIn();
        } else {
            Logger.logReportMessage("Reusing session for next BSD-29967 test — no refresh");
        }

        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        WaitUtil.waitForJSToLoad(15);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);
        leftFilterPanelUtil.ensureLeftFilterPanelOpen();

        columnUtil.enableOrderStartDateColumnIfNeeded(softAssert);
        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
    }

    protected void setCalendarToYesterdayOnce() throws InterruptedException {
        if (!Bsd29967SessionHelper.isCalendarSetToYesterday()) {
            CalendarSetupUtil calendarSetup = new CalendarSetupUtil();
            calendarSetup.setYesterdayDefaultBookmarkOnce(softAssert);
            Bsd29967SessionHelper.markCalendarSetToYesterday();
        }
    }

    @AfterClass(alwaysRun = true)
    public void bsd29967OrdersStopDriverAfterClass() {
        // Driver stopped by Bsd29967SuiteListener on suite finish
    }
}
