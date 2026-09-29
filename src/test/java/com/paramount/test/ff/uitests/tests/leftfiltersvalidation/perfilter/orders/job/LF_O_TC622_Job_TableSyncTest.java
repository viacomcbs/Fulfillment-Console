package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC622 — Job type table sync on Orders view.
 *
 * <ol>
 *   <li>Login + Yesterday calendar (suite {@code @BeforeMethod})</li>
 *   <li>Job type left filter — first option with count &gt; 0</li>
 *   <li>Expand first order row → first line item Type matches filter (no count sync, no Manage columns)</li>
 * </ol>
 */
public class LF_O_TC622_Job_TableSyncTest extends LeftFilterJobOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB.getDisplayName();

    @Test(priority = 1)
    @Description("TC622: Job type — expand first order, first line item Type matches filter")
    public void tc622_jobTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc622_jobTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateJobTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
