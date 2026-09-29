package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC606 — Partner table sync on Orders view (order-level):
 * first non-zero partner → filter count = table record count →
 * every visible Partner column cell matches (no row expand).
 */
public class LF_O_TC606_Partner_TableSyncTest extends LeftFilterPartnerOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.PARTNER.getDisplayName();

    @Test(priority = 1)
    @Description("TC606: Partner — filter count matches table; all visible Partner cells match filter")
    public void tc606_partnerTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc606_partnerTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validatePartnerTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
