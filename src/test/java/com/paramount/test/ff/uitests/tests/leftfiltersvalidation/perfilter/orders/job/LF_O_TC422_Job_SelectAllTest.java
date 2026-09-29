package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC422 — Job Select all. */
public class LF_O_TC422_Job_SelectAllTest extends LeftFilterJobOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB.getDisplayName();
    private static final int FILTER_INDEX = 22;

    @Test(priority = 1)
    @Description("TC422: Job — Select all")
    public void tc422_jobSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc422_jobSelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
