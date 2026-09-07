package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.assignedto;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1109 — AssignedTo Clear filters. */
public class LF_O_TC1109_AssignedTo_ClearFiltersTest extends LeftFilterAssignedToOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ASSIGNED_TO.getDisplayName();
    private static final int FILTER_INDEX = 9;

    @Test(priority = 1)
    @Description("TC1109: AssignedTo — Clear filters")
    public void tc1109_assignedToClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1109_assignedToClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
