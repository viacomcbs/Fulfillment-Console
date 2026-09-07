package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.environment;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.environment.LeftFilterEnvironmentOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1103 — Environment Clear filters. */
public class LF_O_TC1103_Environment_ClearFiltersTest extends LeftFilterEnvironmentOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ENVIRONMENT.getDisplayName();
    private static final int FILTER_INDEX = 3;

    @Test(priority = 1)
    @Description("TC1103: Environment — Clear filters")
    public void tc1103_environmentClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1103_environmentClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
