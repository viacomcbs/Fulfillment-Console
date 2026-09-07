package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.activitytype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC620 — Activity Type table sync on Orders view.
 *
 * <ol>
 *   <li>Login + Yesterday calendar (suite {@code @BeforeMethod})</li>
 *   <li>Activity Type left filter — first option with count &gt; 0</li>
 *   <li>Manage columns — scroll to Activity Type, click once if unchecked; Save if enabled else close</li>
 *   <li>Wait for table refresh (no page refresh)</li>
 *   <li>Expand order → wait for line-item grid → verify at least one Activity Type matches filter</li>
 * </ol>
 */
public class LF_O_TC620_ActivityType_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName();

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("TC620: ActivityType — filter, Manage columns Activity Type, expanded row matches filter")
    public void tc620_activityTypeTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc620_activityTypeTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateActivityTypeTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
