package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.errormessage;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC617 — ErrorMessage Table sync. */
public class LF_O_TC617_ErrorMessage_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ERROR_MESSAGE.getDisplayName();
    private static final int FILTER_INDEX = 17;

    @Test(priority = 1)
    @Description("TC617: ErrorMessage — Table sync")
    public void tc617_errorMessageTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc617_errorMessageTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
