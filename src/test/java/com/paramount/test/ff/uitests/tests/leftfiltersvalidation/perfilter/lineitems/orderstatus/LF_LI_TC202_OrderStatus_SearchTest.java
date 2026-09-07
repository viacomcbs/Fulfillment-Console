package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.orderstatus.LeftFilterOrderStatusLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC202 — OrderStatus Search. */
public class LF_LI_TC202_OrderStatus_SearchTest extends LeftFilterOrderStatusLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ORDER_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 2;

    @Test(priority = 1)
    @Description("TC202: OrderStatus — Search")
    public void tc202_orderStatusSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc202_orderStatusSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
