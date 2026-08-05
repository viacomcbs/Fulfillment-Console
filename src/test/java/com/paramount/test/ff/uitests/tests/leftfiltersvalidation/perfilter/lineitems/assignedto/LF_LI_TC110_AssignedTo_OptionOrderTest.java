package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.assignedto;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC110 — AssignedTo Option list order. */
public class LF_LI_TC110_AssignedTo_OptionOrderTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ASSIGNED_TO.getDisplayName();
    private static final int FILTER_INDEX = 9;

    @Test(priority = 1)
    @Description("TC110: AssignedTo — Option list order")
    public void tc110_assignedToOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc110_assignedToOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
