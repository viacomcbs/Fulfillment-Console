package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.demandsystem;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1116 — DemandSystem Clear filters. */
public class LF_O_TC1116_DemandSystem_ClearFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DEMAND_SYSTEM.getDisplayName();
    private static final int FILTER_INDEX = 16;

    @Test(priority = 1)
    @Description("TC1116: DemandSystem — Clear filters")
    public void tc1116_demandSystemClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1116_demandSystemClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
