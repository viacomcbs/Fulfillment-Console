package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.franchise;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC618 — Franchise table sync on Orders view:
 * first non-zero option → filter count = table record count only.
 * No Franchise column on the grid — row/cell checks are not applicable.
 */
public class LF_O_TC618_Franchise_TableSyncTest extends LeftFilterFranchiseOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.FRANCHISE.getDisplayName();

    @Test(priority = 1)
    @Description("TC618: Franchise — filter count matches table record count (no column value check)")
    public void tc618_franchiseTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc618_franchiseTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateFranchiseTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
