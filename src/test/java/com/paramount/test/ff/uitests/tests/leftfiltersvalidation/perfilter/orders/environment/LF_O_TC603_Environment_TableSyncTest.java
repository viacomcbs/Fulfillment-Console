package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.environment;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC603 — Environment table sync on Orders view:
 * first non-zero option (not literal {@code All}) → filter count = table record count only.
 * No Environment column on the grid — row/cell checks are not applicable.
 */
public class LF_O_TC603_Environment_TableSyncTest extends LeftFilterEnvironmentOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ENVIRONMENT.getDisplayName();

    @Test(priority = 1)
    @Description("TC603: Environment — filter count matches table record count (no column value check)")
    public void tc603_environmentTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc603_environmentTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateEnvironmentTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
