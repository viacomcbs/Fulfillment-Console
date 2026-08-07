package com.paramount.test.ff.uitests.tests.featureflag.bsd28459;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagNetworkUtil;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagSessionHelper;
import com.paramount.test.ff.uitests.helpers.featureflag.FeatureFlagValidationUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.CalendarSetupUtil;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS;

/** Shared login + Yesterday calendar for BSD-28459 feature-flag cleanup tests. */
public abstract class FeatureFlagCleanupBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    protected FeatureFlagValidationUtil validationUtil;
    protected FeatureFlagNetworkUtil networkUtil;

    private final OrdersTabTestSetupHelper ordersTabSetup = new OrdersTabTestSetupHelper();

    public FeatureFlagCleanupBaseTest() {
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
    public void featureFlagCleanupSetup() throws InterruptedException {
        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();
        validationUtil = new FeatureFlagValidationUtil();
        networkUtil = new FeatureFlagNetworkUtil();
        softAssert = new SoftAssert("featureFlagCleanupSetup", getClass().getSimpleName());

        if (!FeatureFlagSessionHelper.isLoggedIn()) {
            Logger.logReportMessage("Launching Fulfillment Console for BSD-28459 feature-flag cleanup");
            networkUtil.installCaptureHook();
            networkUtil.installConsoleErrorHook();
            DriverUtil.launchApplicationOnBrowser();
            login.loginToFF();
            Verify.hardAssert(ordersTabSetup.waitForLoginReadyForLeftFilters(),
                    "Login failed — filter panel not visible after login");
            Thread.sleep(FILTER_EXPAND_WAIT_MS);
            FeatureFlagSessionHelper.markLoggedIn();
            setCalendarToYesterdayOnce();
        } else {
            Logger.logReportMessage("Reusing session for next BSD-28459 feature-flag test");
        }

        leftFilterPanelUtil.ensureLeftFilterPanelOpen();
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.ensureOrdersDataLoaded(softAssert);
    }

    protected void setCalendarToYesterdayOnce() throws InterruptedException {
        if (!FeatureFlagSessionHelper.isCalendarSetToYesterday()) {
            CalendarSetupUtil calendarSetup = new CalendarSetupUtil();
            calendarSetup.setDateRangeToYesterday(softAssert);
            if (!calendarSetup.isYesterdaySelected()) {
                calendarSetup.hardRefreshAndSetYesterday(softAssert);
            }
            FeatureFlagSessionHelper.markCalendarSetToYesterday();
        }
    }

    @AfterClass(alwaysRun = true)
    public void featureFlagCleanupStopDriverAfterClass() {
        // Sequential suite stops driver in FeatureFlagSuiteListener.onFinish
    }
}
