package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.language;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC615 — Language Table sync. */
public class LF_O_TC615_Language_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.LANGUAGE.getDisplayName();
    private static final int FILTER_INDEX = 15;

    @Test(priority = 1)
    @Description("TC615: Language — Table sync")
    public void tc615_languageTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc615_languageTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
