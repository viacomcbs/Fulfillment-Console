package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.orderstatus.LeftFilterOrderStatusLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC802 — OrderStatus Scroll. */
public class LF_LI_TC802_OrderStatus_ScrollTest extends LeftFilterOrderStatusLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ORDER_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 2;

    @Test(priority = 1)
    @Description("TC802: OrderStatus — Scroll")
    public void tc802_orderStatusScroll() throws InterruptedException {
        softAssert = new SoftAssert("tc802_orderStatusScroll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.SCROLL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
