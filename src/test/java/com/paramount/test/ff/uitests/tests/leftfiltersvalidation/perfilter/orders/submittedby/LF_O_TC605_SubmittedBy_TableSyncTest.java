package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.submittedby;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC605 — Submitted by table sync on Orders view (order-level):
 * first non-zero option (not literal {@code All}) → filter count = table record count →
 * every visible Submitted By column cell shows initials of the selected name (no row expand).
 */
public class LF_O_TC605_SubmittedBy_TableSyncTest extends LeftFilterSubmittedByOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SUBMITTED_BY.getDisplayName();

    @Test(priority = 1)
    @Description("TC605: Submitted by — filter count matches table record count; all visible Submitted By cells show initials")
    public void tc605_submittedByTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc605_submittedByTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSubmittedByTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
