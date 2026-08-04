package com.paramount.test.ff.uitests.tests.duplicatefilteroptionscheck.lineitems;

import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.loginUtil.DriverUtil;
import com.paramount.test.ff.common.loginUtil.Login;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.lineitems.LineItemsTabTestSetupHelper;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPanelUtil;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

/**
 * Base setup for Duplicate filter options check tests on the Line items tab.
 */
public abstract class DuplicateFilterOptionsLineItemsTabBaseTest extends BaseTest {

    protected Login login;
    protected LeftFilterPanelUtil leftFilterPanelUtil;
    private final LineItemsTabTestSetupHelper lineItemsTabSetup = new LineItemsTabTestSetupHelper();

    @BeforeMethod(alwaysRun = true)
    public void duplicateFilterOptionsLineItemsSetup() throws InterruptedException {
        if (driver.get() == null) {
            throw new SkipException("WebDriver session is not available. Start Synergy server and ensure "
                    + "screen recording permission is enabled for the Synergy client.");
        }

        login = new Login();
        leftFilterPanelUtil = new LeftFilterPanelUtil();

        softAssert = new SoftAssert("duplicateFilterOptionsLineItemsSetup", getClass().getSimpleName());
        Logger.logReportMessage("Launching Fulfillment Console for Line items duplicate filter options check");
        DriverUtil.launchApplicationOnBrowser();
        login.loginToFF();

        lineItemsTabSetup.prepareLineItemsTab(softAssert);
        softAssert.assertAll();
    }
}
