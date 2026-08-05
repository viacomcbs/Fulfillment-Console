package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seriestitle;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC607 — SeriesTitle Table sync. */
public class LF_O_TC607_SeriesTitle_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SERIES_TITLE.getDisplayName();
    private static final int FILTER_INDEX = 7;

    @Test(priority = 1)
    @Description("TC607: SeriesTitle — Table sync")
    public void tc607_seriesTitleTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc607_seriesTitleTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
