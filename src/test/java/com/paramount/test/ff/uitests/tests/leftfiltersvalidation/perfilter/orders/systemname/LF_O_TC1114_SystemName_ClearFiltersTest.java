package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.systemname;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1114 — SystemName Clear filters. */
public class LF_O_TC1114_SystemName_ClearFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SYSTEM_NAME.getDisplayName();
    private static final int FILTER_INDEX = 14;

    @Test(priority = 1)
    @Description("TC1114: SystemName — Clear filters")
    public void tc1114_systemNameClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1114_systemNameClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
