package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.assignedto;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC609 — Assigned To table sync on Orders view:
 * first non-zero person → filter count = table record count → Assigned to cells show initials;
 * when no person has count &gt; 0 → select Unassigned → all Assigned to cells are blank.
 */
public class LF_O_TC609_AssignedTo_TableSyncTest extends LeftFilterAssignedToOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ASSIGNED_TO.getDisplayName();

    @Test(priority = 1)
    @Description("TC609: Assigned To — filter count matches table; initials or blank (Unassigned)")
    public void tc609_assignedToTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc609_assignedToTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateAssignedToTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
