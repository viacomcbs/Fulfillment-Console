package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.environment;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC104 — Environment Option list order. */
public class LF_LI_TC104_Environment_OptionOrderTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.ENVIRONMENT.getDisplayName();
    private static final int FILTER_INDEX = 3;

    @Test(priority = 1)
    @Description("TC104: Environment — Option list order")
    public void tc104_environmentOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc104_environmentOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
