package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.flag;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC608 — Flag table sync on Orders view:
 * Is not flagged → count matches table, no flag icon in Status;
 * Is flagged or child reason → count matches table, flag icon on every visible row.
 */
public class LF_O_TC608_Flag_TableSyncTest extends LeftFilterFlagOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.FLAG.getDisplayName();

    @Test(priority = 1)
    @Description("TC608: Flag — table count sync and Status flag icon presence")
    public void tc608_flagTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc608_flagTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateFlagTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
