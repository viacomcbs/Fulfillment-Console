package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.activitytype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC220 — ActivityType Search. */
public class LF_LI_TC220_ActivityType_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ACTIVITY_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 20;

    @Test(priority = 1)
    @Description("TC220: ActivityType — Search")
    public void tc220_activityType_search() throws InterruptedException {
        softAssert = new SoftAssert("tc220_activityType_search", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
