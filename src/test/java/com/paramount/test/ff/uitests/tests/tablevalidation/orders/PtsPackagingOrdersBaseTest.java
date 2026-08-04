package com.paramount.test.ff.uitests.tests.tablevalidation.orders;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingCalendarSetup;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingExportUtil;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingSessionHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Base for BSD-29441 Orders-tab tests — Demand system = PTS, shared session.
 */
public abstract class PtsPackagingOrdersBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    protected PtsPackagingIdTableUtil ptsPackagingUtil;
    protected PtsPackagingExportUtil ptsExportUtil;
    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();

    public PtsPackagingOrdersBaseTest() {
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
    public void ptsPackagingOrdersSetup() throws InterruptedException {
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();
        ptsPackagingUtil = new PtsPackagingIdTableUtil();
        ptsExportUtil = new PtsPackagingExportUtil();
        softAssert = new SoftAssert("ptsPackagingOrdersSetup", getClass().getSimpleName());

        if (!PtsPackagingSessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for PTS Packaging ID (Orders)");
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                    "Login failed — filter panel not visible after login");
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            PtsPackagingSessionHelper.markLoggedIn();
            PtsPackagingCalendarSetup.setYesterdayAsDefaultOnce(softAssert);
        } else {
            Logger.logReportMessage("Reusing session for next PTS Packaging ID (Orders) test — no refresh");
        }

        // Same pattern as LeftFilterOrdersTabBaseTest / LF_O_TC101 — expand panel then Orders tab
        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);

        if (!PtsPackagingSessionHelper.isPtsDemandFilterAppliedOnOrders()) {
            ptsPackagingUtil.applyPtsDemandSystemFilter(leftFilterPanelUtil, softAssert);
            PtsPackagingSessionHelper.markPtsDemandFilterAppliedOnOrders();
        }
    }

    /** Parallel-classes mode: each test class owns its own Synergy browser session. */
    @AfterClass(alwaysRun = true)
    public void ptsPackagingOrdersStopDriverAfterClass() {
        if (PtsPackagingSessionHelper.isParallelClassesMode()) {
            PtsPackagingSessionHelper.stopSharedDriver();
        }
    }
}
