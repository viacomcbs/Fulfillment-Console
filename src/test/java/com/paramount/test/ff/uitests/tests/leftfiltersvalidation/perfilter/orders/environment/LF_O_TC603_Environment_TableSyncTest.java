package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.environment;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC603 — Environment Table sync. */
public class LF_O_TC603_Environment_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ENVIRONMENT.getDisplayName();
    private static final int FILTER_INDEX = 3;

    @Test(priority = 1)
    @Description("TC603: Environment — Table sync")
    public void tc603_environmentTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc603_environmentTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
