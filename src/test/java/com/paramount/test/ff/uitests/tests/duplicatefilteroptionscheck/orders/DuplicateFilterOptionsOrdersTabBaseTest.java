package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.orders;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import com.paramount.test.ff.uitests.helpers.orders.OrdersTabTestSetupHelper;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

/**
 * Base setup for Duplicate filter options check tests on the Orders tab.
 * Orders is the default landing tab after login (same as FilterPanelTest in FCAutomation_Framework).
 */
public abstract class DuplicateFilterOptionsOrdersTabBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    private final OrdersTabTestSetupHelper ordersTabTestSetupHelper = new OrdersTabTestSetupHelper();

    @BeforeMethod(alwaysRun = true)
    public void duplicateFilterOptionsOrdersSetup() throws InterruptedException {
        if (driver.get() == null) {
            throw new SkipException("WebDriver session is not available. Start Synergy server and ensure "
                    + "screen recording permission is enabled for the Synergy client.");
        }

        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();

        softAssert = new SoftAssert("duplicateFilterOptionsOrdersSetup", getClass().getSimpleName());
        Logger.logReportMessage("Launching Fulfillment Console for Orders duplicate filter options check");
        DriverUtil.launchApplicationOnBrowser();
        login.loginToFF();

        ordersTabTestSetupHelper.prepareOrdersTab(softAssert);
        softAssert.assertAll();
    }
}
