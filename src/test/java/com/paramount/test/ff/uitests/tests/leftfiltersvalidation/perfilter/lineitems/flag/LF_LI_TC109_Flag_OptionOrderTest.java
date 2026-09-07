package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.flag;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC109 — Flag Option list order. */
public class LF_LI_TC109_Flag_OptionOrderTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.FLAG.getDisplayName();
    private static final int FILTER_INDEX = 8;

    @Test(priority = 1)
    @Description("TC109: Flag — Option list order")
    public void tc109_flagOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc109_flagOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
