package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.activitytype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1020 — ActivityType Active filters. */
public class LF_O_TC1020_ActivityType_ActiveFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 20;

    @Test(priority = 1)
    @Description("TC1020: ActivityType — Active filters")
    public void tc1020_activityTypeActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1020_activityTypeActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
