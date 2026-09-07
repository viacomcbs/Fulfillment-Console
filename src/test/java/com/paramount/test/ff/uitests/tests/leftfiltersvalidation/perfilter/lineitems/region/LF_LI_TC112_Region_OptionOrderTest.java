package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.region;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC112 — Region Option list order. */
public class LF_LI_TC112_Region_OptionOrderTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.REGION.getDisplayName();
    private static final int FILTER_INDEX = 11;

    @Test(priority = 1)
    @Description("TC112: Region — Option list order")
    public void tc112_regionOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc112_regionOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
