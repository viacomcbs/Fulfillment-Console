package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.systemname;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC614 — SystemName Table sync. */
public class LF_O_TC614_SystemName_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SYSTEM_NAME.getDisplayName();
    private static final int FILTER_INDEX = 14;

    @Test(priority = 1)
    @Description("TC614: SystemName — Table sync")
    public void tc614_systemNameTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc614_systemNameTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
