package com.paramount.test.ff.uitests.tests.tablevalidation.dsid;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.dsid.DsidCalendarSetup;
import com.paramount.test.ff.uitests.helpers.dsid.DsidColumnUtil;
import com.paramount.test.ff.uitests.helpers.dsid.DsidSessionHelper;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagColumnUtil.ColumnSection;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/** Base for BSD-29870 — Today default, MetadataOnlyDelivery + PP YT FSP UK, shared session. */
public abstract class DsidOrdersBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    protected DsidColumnUtil dsidUtil;
    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();

    public DsidOrdersBaseTest() {
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
    public void dsidOrdersSetup() throws InterruptedException {
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();
        dsidUtil = new DsidColumnUtil();
        softAssert = new SoftAssert("dsidOrdersSetup", getClass().getSimpleName());

        if (!DsidSessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for DSID validation (BSD-29870)");
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                    "Login failed — filter panel not visible after login");
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            DsidSessionHelper.markLoggedIn();
            DsidCalendarSetup.setTodayAsDefaultOnce(softAssert);
        } else {
            Logger.logReportMessage("Reusing session for next DSID test — no refresh");
        }

        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        WaitUtil.waitForJSToLoad(15);
        Thread.sleep(FILTER_EXPAND_WAIT_MS);

        if (!DsidSessionHelper.isFiltersAppliedOnOrders()) {
            dsidUtil.applyMetadataOnlyDeliveryPartnerFilter(leftFilterPanelUtil, softAssert);
            DsidSessionHelper.markFiltersAppliedOnOrders();
        }
    }

    protected void ensureLineItemsTabWithFilters() throws InterruptedException {
        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        if (!DsidSessionHelper.isFiltersAppliedOnLineItems()) {
            dsidUtil.applyMetadataOnlyDeliveryPartnerFilter(leftFilterPanelUtil, softAssert);
            DsidSessionHelper.markFiltersAppliedOnLineItems();
        }
    }

    protected void enableDsidOnOrders(SoftAssert softAssert) throws InterruptedException {
        dsidUtil.enableDsidColumnIfNeeded(ColumnSection.ORDER, softAssert);
    }

    protected void enableDsidOnLineItems(SoftAssert softAssert) throws InterruptedException {
        dsidUtil.enableDsidColumnIfNeeded(ColumnSection.LINE_ITEM, softAssert);
    }

    @AfterClass(alwaysRun = true)
    public void dsidOrdersStopDriverAfterClass() {
        // Driver stopped by DsidSuiteListener on suite finish
    }
}
