package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.flag;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC408 — Flag Select all. */
public class LF_O_TC408_Flag_SelectAllTest extends LeftFilterFlagOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.FLAG.getDisplayName();
    private static final int FILTER_INDEX = 8;

    @Test(priority = 1)
    @Description("TC408: Flag — Select all")
    public void tc408_flagSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc408_flagSelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
