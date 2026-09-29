package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1122 — Job Clear filters. */
public class LF_O_TC1122_Job_ClearFiltersTest extends LeftFilterJobOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB.getDisplayName();
    private static final int FILTER_INDEX = 22;

    @Test(priority = 1)
    @Description("TC1122: Job — Clear filters")
    public void tc1122_jobClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1122_jobClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
