package com.paramount.test.ff.uitests.tests.bsd29791;

import com.paramount.test.ff.common.loginUtil.WaitUtil;
import com.paramount.test.ff.common.base.BaseTest;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.ShippingDock_util;
import com.paramount.test.ff.uitests.helpers.TarSize_util;
import io.qameta.allure.Link;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

@Link(name = "BSD-29791", url = "https://paramount.atlassian.net/browse/BSD-29791")
public abstract class BSD29791BaseTest extends ManageColumnsBaseTest {

    protected static final String PROD_ORDER_ID = TarSize_util.PROD_ORDER_ID;
    protected static final String PROD_PACKAGE_ID = TarSize_util.PROD_PACKAGE_ID;
    protected static final String SECTION = "package";

    private static boolean suiteLaunched;

    protected TarSize_util tarSizeUtil;
    protected ShippingDock_util shippingDockUtil;

    @BeforeClass(alwaysRun = true)
    public void bsd29791SuiteLaunch(ITestContext context) throws InterruptedException {
        ensureHelpersInitialized();
        FulfillmentJsUtil.useFastElementTimeout();
        if (!suiteLaunched) {
            Logger.logReportMessage("BSD-29791 suite: one-time login and grid ready");
            if (isShippingDockFirst(context)) {
                Logger.logReportMessage("TC_005: Shipping Dock login first — FF Console login deferred to test");
                BaseTest.driver.get().browser().getUrl(ShippingDock_util.SHIPPING_DOCK_LOGIN_URL);
                WaitUtil.waitForJSToLoad(20);
            } else {
                launchFulfillmentConsole();
            }
            suiteLaunched = true;
        }
    }

    private boolean isShippingDockFirst(ITestContext context) {
        if (context == null || context.getSuite() == null) {
            return false;
        }
        String flag = context.getSuite().getParameter("ShippingDockFirst");
        return flag != null && "true".equalsIgnoreCase(flag.trim());
    }

    @BeforeMethod(alwaysRun = true)
    public void initBsd29791Helpers() {
        FulfillmentJsUtil.useFastElementTimeout();
        ensureHelpersInitialized();
        if (tarSizeUtil == null) {
            tarSizeUtil = new TarSize_util();
        }
        if (shippingDockUtil == null) {
            shippingDockUtil = new ShippingDock_util();
        }
        tarSizeUtil.installArchiveFileSizeHook();
    }
}
