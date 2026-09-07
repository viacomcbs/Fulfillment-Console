package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.errormessage;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC817 — ErrorMessage Scroll. */
public class LF_O_TC817_ErrorMessage_ScrollTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ERROR_MESSAGE.getDisplayName();
    private static final int FILTER_INDEX = 17;

    @Test(priority = 1)
    @Description("TC817: ErrorMessage — Scroll")
    public void tc817_errorMessageScroll() throws InterruptedException {
        softAssert = new SoftAssert("tc817_errorMessageScroll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SCROLL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
