package com.paramount.test.ff.uitests.tests.bsd29174;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SynergyRetryUtil;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.OrderStatusTooltip_util;
import io.qameta.allure.Link;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

@Link(name = "BSD-29174", url = "https://paramount.atlassian.net/browse/BSD-29174")
public abstract class BSD29174BaseTest extends ManageColumnsBaseTest {

    protected static final String PRIMARY_ORDER_ID = OrderStatusTooltip_util.PROD_ORDER_MULTI_STATUS;
    protected static final String ALT_ORDER_ID = OrderStatusTooltip_util.PROD_ORDER_ALT;
    protected static final String KNOWN_ORDER_ID = OrderStatusTooltip_util.PROD_ORDER_KNOWN;

    private static final Object SUITE_LOCK = new Object();
    private static boolean suiteLaunched;

    protected OrderStatusTooltip_util orderStatusTooltipUtil;
    protected FilterPanel_Util filterPanelUtil;

    @BeforeClass(alwaysRun = true)
    public void bsd29174SuiteLaunch() throws InterruptedException {
        ensureHelpersInitialized();
        FulfillmentJsUtil.useFastElementTimeout();
        synchronized (SUITE_LOCK) {
            if (suiteLaunched) {
                return;
            }
            Logger.logReportMessage("BSD-29174 suite: one-time login, Yesterday filter, orders grid ready");
            launchFulfillmentConsole();
            if (filterPanelUtil == null) {
                filterPanelUtil = new FilterPanel_Util();
            }
            try {
                filterPanelUtil.applyYesterdayDateFilter();
            } catch (Exception e) {
                Logger.logConsoleMessage("Yesterday filter skipped: " + e.getMessage());
            }
            if (orderStatusTooltipUtil == null) {
                orderStatusTooltipUtil = new OrderStatusTooltip_util();
            }
            orderStatusTooltipUtil.installTooltipCaptureHook();
            tableViewUtill.waitForOrdersGridReady();
            Thread.sleep(3000);
            suiteLaunched = true;
        }
    }

    public static void resetSuiteLaunchState() {
        synchronized (SUITE_LOCK) {
            suiteLaunched = false;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void initBsd29174Helpers() throws InterruptedException {
        FulfillmentJsUtil.useFastElementTimeout();
        try {
            requireDriver().getSessionID();
        } catch (Exception e) {
            Logger.logReportMessage("BSD-29174: Synergy session unhealthy — recovering before test");
            SynergyRetryUtil.recoverSessionIfNeeded();
            FilterPanel_Util.resetYesterdayDateFilterState();
            resetSuiteLaunchState();
            bsd29174SuiteLaunch();
            return;
        }
        ensureHelpersInitialized();
        if (filterPanelUtil == null) {
            filterPanelUtil = new FilterPanel_Util();
        }
        if (orderStatusTooltipUtil == null) {
            orderStatusTooltipUtil = new OrderStatusTooltip_util();
        }
        orderStatusTooltipUtil.installTooltipCaptureHook();
    }
}
