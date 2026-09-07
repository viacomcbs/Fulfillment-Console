package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.errormessage;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1117 — ErrorMessage Clear filters. */
public class LF_O_TC1117_ErrorMessage_ClearFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ERROR_MESSAGE.getDisplayName();
    private static final int FILTER_INDEX = 17;

    @Test(priority = 1)
    @Description("TC1117: ErrorMessage — Clear filters")
    public void tc1117_errorMessageClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1117_errorMessageClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
