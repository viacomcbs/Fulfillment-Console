package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.brand;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC613 — Brand table sync on Orders view (order-level):
 * first non-zero brand (not literal {@code All}) → filter count = table record count →
 * every visible Brand column cell matches the selected brand (no row expand).
 */
public class LF_O_TC613_Brand_TableSyncTest extends LeftFilterBrandOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.BRAND.getDisplayName();

    @Test(priority = 1)
    @Description("TC613: Brand — filter count matches table record count; all visible Brand cells match filter")
    public void tc613_brandTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc613_brandTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateBrandTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
