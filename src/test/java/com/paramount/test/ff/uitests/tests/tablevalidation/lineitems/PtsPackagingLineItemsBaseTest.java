package com.paramount.test.ff.uitests.tests.tablevalidation.lineitems;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
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
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            PtsPackagingSessionHelper.markLoggedIn();
            PtsPackagingCalendarSetup.setYesterdayAsDefaultOnce(softAssert);
        } else {
            Logger.logReportMessage("Reusing session — navigating to Line Items tab (no refresh, no calendar)");
        }

        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);

        if (!PtsPackagingSessionHelper.isPtsDemandFilterAppliedOnLineItems()) {
            ptsPackagingUtil.applyPtsDemandSystemFilter(leftFilterPanelUtil, softAssert);
            PtsPackagingSessionHelper.markPtsDemandFilterAppliedOnLineItems();
        }
    }

    @AfterClass(alwaysRun = true)
    public void ptsPackagingLineItemsStopDriverAfterClass() {
        if (PtsPackagingSessionHelper.isParallelClassesMode()) {
            PtsPackagingSessionHelper.stopSharedDriver();
        }
    }
}
