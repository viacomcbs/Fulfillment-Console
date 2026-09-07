package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.brand;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.brand.LeftFilterBrandOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC413 — Brand Select all. */
public class LF_O_TC413_Brand_SelectAllTest extends LeftFilterBrandOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.BRAND.getDisplayName();
    private static final int FILTER_INDEX = 13;

    @Test(priority = 1)
    @Description("TC413: Brand — Select all")
    public void tc413_brandSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc413_brandSelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
