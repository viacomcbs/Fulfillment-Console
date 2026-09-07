package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1006 — Partner Active filters. */
public class LF_O_TC1006_Partner_ActiveFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.PARTNER.getDisplayName();
    private static final int FILTER_INDEX = 6;

    @Test(priority = 1)
    @Description("TC1006: Partner — Active filters")
    public void tc1006_partnerActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1006_partnerActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
