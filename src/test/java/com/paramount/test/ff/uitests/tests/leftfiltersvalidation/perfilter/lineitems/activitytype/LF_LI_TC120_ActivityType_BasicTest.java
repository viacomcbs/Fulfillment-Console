package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.activitytype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC120 — ActivityType Basic. */
public class LF_LI_TC120_ActivityType_BasicTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ACTIVITY_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 20;

    @Test(priority = 1)
    @Description("TC120: ActivityType — Basic")
    public void tc120_activityType_basic() throws InterruptedException {
        softAssert = new SoftAssert("tc120_activityType_basic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
