package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.demandsystem;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC616 — Demand system table sync on Orders view:
 * first non-zero option → filter count = table record count → click first order →
 * Details panel Job Request Demand system matches filter (PTS Packaging ID-style).
 */
public class LF_O_TC616_DemandSystem_TableSyncTest extends LeftFilterDemandSystemOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DEMAND_SYSTEM.getDisplayName();

    @Test(priority = 1)
    @Description("TC616: Demand system — count sync + Details panel Demand system matches filter selection")
    public void tc616_demandSystemTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc616_demandSystemTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateDemandSystemTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
