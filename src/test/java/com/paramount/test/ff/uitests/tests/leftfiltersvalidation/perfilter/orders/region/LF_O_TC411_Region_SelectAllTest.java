package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.region;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC411 — Region Select all. */
public class LF_O_TC411_Region_SelectAllTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.REGION.getDisplayName();
    private static final int FILTER_INDEX = 11;

    @Test(priority = 1)
    @Description("TC411: Region — Select all")
    public void tc411_regionSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc411_regionSelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
