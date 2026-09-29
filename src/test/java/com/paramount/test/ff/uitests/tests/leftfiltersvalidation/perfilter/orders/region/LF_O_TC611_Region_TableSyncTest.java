package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.region;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC611 — Region table sync on Orders view:
 * first non-zero option → filter count = table record count.
 * When all Region options show count 0 on PROD, select a zero-count option and assert table count is 0.
 */
public class LF_O_TC611_Region_TableSyncTest extends LeftFilterRegionOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.REGION.getDisplayName();

    @Test(priority = 1)
    @Description("TC611: Region — filter count matches table record count; zero-count fallback when all options are 0")
    public void tc611_regionTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc611_regionTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateRegionTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
