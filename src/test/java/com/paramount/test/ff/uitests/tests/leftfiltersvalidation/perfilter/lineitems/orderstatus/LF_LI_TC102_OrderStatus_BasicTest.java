package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.orderstatus.LeftFilterOrderStatusLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC102 — OrderStatus Basic. */
public class LF_LI_TC102_OrderStatus_BasicTest extends LeftFilterOrderStatusLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ORDER_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 2;

    @Test(priority = 1)
    @Description("TC102: OrderStatus — Basic")
    public void tc102_orderStatusBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc102_orderStatusBasic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
