package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.lineitemstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC401 — LineItemStatus Select all. */
public class LF_O_TC401_LineItemStatus_SelectAllTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.LINE_ITEM_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 1;

    @Test(priority = 1)
    @Description("TC401: LineItemStatus — Select all")
    public void tc401_lineItemStatusSelectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc401_lineItemStatusSelectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
