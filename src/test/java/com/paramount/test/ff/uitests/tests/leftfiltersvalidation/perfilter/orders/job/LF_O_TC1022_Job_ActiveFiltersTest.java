package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1022 — Job Active filters. */
public class LF_O_TC1022_Job_ActiveFiltersTest extends LeftFilterJobOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB.getDisplayName();
    private static final int FILTER_INDEX = 22;

    @Test(priority = 1)
    @Description("TC1022: Job — Active filters")
    public void tc1022_jobActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1022_jobActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
