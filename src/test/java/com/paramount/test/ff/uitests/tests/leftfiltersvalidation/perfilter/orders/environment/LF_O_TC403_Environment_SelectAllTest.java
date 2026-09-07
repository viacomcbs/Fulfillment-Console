package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.environment;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.environment.LeftFilterEnvironmentOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC403 — Environment Select all. */
public class LF_O_TC403_Environment_SelectAllTest extends LeftFilterEnvironmentOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ENVIRONMENT.getDisplayName();
    private static final int FILTER_INDEX = 3;

    @Test(priority = 1)
    @Description("TC403: Environment — Select all")
    public void tc403_environmentSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc403_environmentSelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
