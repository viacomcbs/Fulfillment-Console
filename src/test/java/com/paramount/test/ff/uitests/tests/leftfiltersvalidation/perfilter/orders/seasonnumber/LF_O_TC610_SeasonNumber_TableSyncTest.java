package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seasonnumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC610 — SeasonNumber Table sync. */
public class LF_O_TC610_SeasonNumber_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SEASON_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 10;

    @Test(priority = 1)
    @Description("TC610: SeasonNumber — Table sync")
    public void tc610_seasonNumberTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc610_seasonNumberTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
