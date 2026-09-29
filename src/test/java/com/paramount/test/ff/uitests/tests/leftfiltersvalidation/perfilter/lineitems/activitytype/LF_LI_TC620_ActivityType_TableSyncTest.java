package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.activitytype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC620 — Activity Type table sync on Line Items view.
 * Reads {@code td.activity-type-col} on the main grid (no row expand).
 * Filter count must match table total; first visible Activity Type must match the filter option.
 */
public class LF_LI_TC620_ActivityType_TableSyncTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ACTIVITY_TYPE.getDisplayName();

    @Test(priority = 1)
    @Description("TC620: Activity Type — filter count sync; first line item Activity Type matches filter")
    public void tc620_activityType_tableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc620_activityType_tableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateActivityTypeTableSyncOnLineItemsTab(softAssert, FILTER);
        softAssert.assertAll();
    }
}
