package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.assignedto;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1009 — AssignedTo Active filters. */
public class LF_O_TC1009_AssignedTo_ActiveFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ASSIGNED_TO.getDisplayName();
    private static final int FILTER_INDEX = 9;

    @Test(priority = 1)
    @Description("TC1009: AssignedTo — Active filters")
    public void tc1009_assignedToActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1009_assignedToActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
