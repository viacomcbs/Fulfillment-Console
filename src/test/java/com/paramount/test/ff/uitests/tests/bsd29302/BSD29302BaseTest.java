package com.paramount.test.ff.uitests.tests.bsd29302;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.pageobjects.DetailsPanel;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.PackageDetails_util;
import io.qameta.allure.Link;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

@Link(name = "BSD-29302", url = "https://paramount.atlassian.net/browse/BSD-29302")
public abstract class BSD29302BaseTest extends ManageColumnsBaseTest {

    protected static final String COLUMN_ID = "shippingDockPackageId";
    protected static final String COLUMN_NAME = DetailsPanel.SHIPPING_DOCK_PACKAGE_ID_LABEL;
    protected static final String SECTION = "package";
    /** Known PROD order with Shipping Dock Package ID populated (Delivered). */
    protected static final String PROD_ORDER_ID = "wzr842617";

    private static boolean suiteLaunched;

    protected PackageDetails_util packageDetailsUtil;

    @BeforeClass(alwaysRun = true)
    public void bsd29302SuiteLaunch() throws InterruptedException {
        ensureHelpersInitialized();
        FulfillmentJsUtil.useFastElementTimeout();
        if (!suiteLaunched) {
            Logger.logReportMessage("BSD-29302 suite: one-time login and grid ready");
            launchFulfillmentConsole();
            suiteLaunched = true;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void initBsd29302Helpers() {
        FulfillmentJsUtil.useFastElementTimeout();
        ensureHelpersInitialized();
        if (packageDetailsUtil == null) {
            packageDetailsUtil = new PackageDetails_util();
        }
    }

    protected void openManageColumnsOnce() throws InterruptedException {
        launchFulfillmentConsole();
        tableViewUtill.openManageColumns();
        tableViewUtill.selectStandardView();
    }
}