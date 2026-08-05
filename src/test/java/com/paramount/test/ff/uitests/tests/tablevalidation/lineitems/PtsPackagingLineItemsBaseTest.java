package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingCalendarSetup;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingExportUtil;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingIdTableUtil;
import com.paramount.test.ff.uitests.helpers.ptspackaging.PtsPackagingSessionHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/**
 * Base for BSD-29441 Line Items-tab tests — Demand system = PTS, shared session.
 */
public abstract class PtsPackagingLineItemsBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    protected PtsPackagingIdTableUtil ptsPackagingUtil;
    protected PtsPackagingExportUtil ptsExportUtil;
    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();

    public PtsPackagingLineItemsBaseTest() {
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
    public void ptsPackagingLineItemsSetup() throws InterruptedException {
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();
        ptsPackagingUtil = new PtsPackagingIdTableUtil();
        ptsExportUtil = new PtsPackagingExportUtil();
        softAssert = new SoftAssert("ptsPackagingLineItemsSetup", getClass().getSimpleName());

        if (!PtsPackagingSessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for PTS Packaging ID (Line Items)");
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                    "Login failed — filter panel not visible after login");
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            PtsPackagingSessionHelper.markLoggedIn();
            PtsPackagingCalendarSetup.setYesterdayAsDefaultOnce(softAssert);
        }

        leftFilterPanelUtil.ensureLeftFilterPanelOpen();

        if (!PtsPackagingSessionHelper.isLineItemsConsoleTabActive()) {
            Logger.logReportMessage("Switching to Line Items tab (one-time — calendar persists across tabs)");
            leftFilterPanelUtil.navigateToLineItemsTab(true);
            Verify.hardAssert(leftFilterPanelUtil.isLineItemsTabActive(),
                    "Line Items tab must be active before PTS Line Items tests (not Orders view)");
            PtsPackagingSessionHelper.markLineItemsConsoleTabActive();
            if (!PtsPackagingSessionHelper.isPtsDemandFilterAppliedOnLineItems()) {
                ptsPackagingUtil.applyPtsDemandSystemFilter(leftFilterPanelUtil, softAssert);
                PtsPackagingSessionHelper.markPtsDemandFilterAppliedOnLineItems();
            }
        } else {
            if (!leftFilterPanelUtil.isLineItemsTabActive()) {
                Logger.logReportMessage("Line Items session flag set but Orders view detected — re-clicking Line items tab");
                leftFilterPanelUtil.navigateToLineItemsTab(true);
            }
            Logger.logReportMessage("Reusing Line Items tab — Manage columns is Line-item-only flat list");
        }
    }

    @AfterClass(alwaysRun = true)
    public void ptsPackagingLineItemsStopDriverAfterClass() {
        if (PtsPackagingSessionHelper.isParallelClassesMode()) {
            PtsPackagingSessionHelper.stopSharedDriver();
        }
    }
}
