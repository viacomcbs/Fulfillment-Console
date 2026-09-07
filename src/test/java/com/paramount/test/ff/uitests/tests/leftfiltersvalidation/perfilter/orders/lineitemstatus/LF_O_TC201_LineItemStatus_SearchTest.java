package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.lineitemstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.lineitemstatus.LeftFilterLineItemStatusOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC201 — LineItemStatus Search. */
public class LF_O_TC201_LineItemStatus_SearchTest extends LeftFilterLineItemStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.LINE_ITEM_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 1;

    @Test(priority = 1)
    @Description("TC201: LineItemStatus — Search")
    public void tc201_lineItemStatusSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc201_lineItemStatusSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
