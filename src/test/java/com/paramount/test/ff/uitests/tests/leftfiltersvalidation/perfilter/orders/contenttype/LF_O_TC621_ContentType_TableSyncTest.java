package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.contenttype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC621 — ContentType Table sync. */
public class LF_O_TC621_ContentType_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.CONTENT_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 21;

    @Test(priority = 1)
    @Description("TC621: ContentType — Table sync")
    public void tc621_contentTypeTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc621_contentTypeTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
