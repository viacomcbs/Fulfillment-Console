package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.jobtype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1004 — JobType Active filters. */
public class LF_O_TC1004_JobType_ActiveFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 4;

    @Test(priority = 1)
    @Description("TC1004: JobType — Active filters")
    public void tc1004_jobTypeActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1004_jobTypeActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
